package org.example.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class HttpServer {

    public static Map<String, Route> endPoints = new HashMap<>();
    public static String staticFilesLocation = null;
    private static String appPath = "";
    private static int threadCounter = 0;
    private static volatile boolean running = true;
    private static ServerSocket serverSocket = null;

    public static void staticfiles(String location) {
        staticFilesLocation = location;
    }

    public static void path(String prefix) {
        appPath = prefix;
    }

    public static void start() throws IOException, URISyntaxException {
        main(new String[]{});
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        setupShutdownHook();
        
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        System.out.println("HTTP Server started on port 35000 (concurrent mode)");
        System.out.println("Press Ctrl+C to shutdown");
        
        while (running) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
                threadCounter++;
                Thread clientThread = new Thread(new ClientHandler(clientSocket), "Client-" + threadCounter);
                clientThread.start();
            } catch (IOException e) {
                if (running) {
                    System.err.println("Accept failed: " + e.getMessage());
                }
            }
        }
    }

    private static void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            gracefulShutdown();
        }, "ShutdownHook"));
    }

    public static void gracefulShutdown() {
        System.out.println("\n⏹️  Shutdown signal received. Closing server ...");
        running = false;
        
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                System.out.println("✅ Server socket closed");
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        
        System.out.println("✅ HTTP Server stopped");
    }

    public static void get(String path, WebMethod wm){
        endPoints.put(appPath + path, (req, res) -> wm.execute());
    }

    public static void get(String path, Route route){
        endPoints.put(appPath + path, route);
    }

    private static Request parseRequest(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();
        if (query != null) {
            for (String pair : query.split("&")) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    params.put(kv[0], kv[1]);
                } else if (kv.length == 1) {
                    params.put(kv[0], "");
                }
            }
        }
        return new Request(params);
    }

    private static void serveStaticFile(String reqPath, OutputStream out) throws IOException {
        if (staticFilesLocation != null) {
            String base = staticFilesLocation.startsWith("/") ? staticFilesLocation : "/" + staticFilesLocation;
            String resourcePath = base + reqPath;
            try (InputStream fileStream = HttpServer.class.getResourceAsStream(resourcePath)) {
                if (fileStream != null) {
                    byte[] body = fileStream.readAllBytes();
                    String contentType = getContentTypeStatic(reqPath);
                    String headers = "HTTP/1.1 200 OK\r\n"
                            + "Content-Type: " + contentType + "\r\n"
                            + "Content-Length: " + body.length + "\r\n"
                            + "\r\n";
                    out.write(headers.getBytes("UTF-8"));
                    out.write(body);
                    return;
                }
            }
        }
        byte[] body = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Not Found</title></head>"
                .concat("<body>404 - Not Found</body></html>").getBytes("UTF-8");
        String headers = "HTTP/1.1 404 Not Found\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "\r\n";
        out.write(headers.getBytes("UTF-8"));
        out.write(body);
    }

    public static String getContentTypeStatic(String path) {
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif"))  return "image/gif";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        return "text/html; charset=UTF-8";
    }

    private static String getContentType(String path) {
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".gif"))  return "image/gif";
        if (path.endsWith(".css"))  return "text/css";
        if (path.endsWith(".js"))   return "application/javascript";
        return "text/html; charset=UTF-8";
    }


}
