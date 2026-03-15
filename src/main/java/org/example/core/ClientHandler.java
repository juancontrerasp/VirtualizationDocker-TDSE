package org.example.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClient();
        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void handleClient() throws IOException, URISyntaxException {
        OutputStream out = clientSocket.getOutputStream();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        clientSocket.getInputStream()));
        String inputLine;

        boolean isFirstLine = true;

        String reqPath = "";
        Request request = new Request(new HashMap<>());

        while ((inputLine = in.readLine()) != null) {
            if (isFirstLine) {
                System.out.println("[" + Thread.currentThread().getName() + "] Received: " + inputLine);
                String[] firstLineTokens = inputLine.split(" ");
                String uristr = firstLineTokens[1];
                URI requestedURI = new URI(uristr);
                reqPath = requestedURI.getPath();
                request = parseRequest(requestedURI);
                System.out.println("[" + Thread.currentThread().getName() + "] Path: " + reqPath);
                isFirstLine = false;
            } else if (inputLine.isEmpty()) {
                // Empty line marks end of HTTP headers
                break;
            }
        }

        Route route = HttpServer.endPoints.get(reqPath);

        if (route != null) {
            String body = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                    + "<title>Title of the document</title></head><body>"
                    + route.handle(request, new Response())
                    + "</body></html>";
            byte[] bodyBytes = body.getBytes("UTF-8");
            String headers = "HTTP/1.1 200 OK\r\n"
                    + "Content-Type: text/html; charset=UTF-8\r\n"
                    + "Content-Length: " + bodyBytes.length + "\r\n"
                    + "\r\n";
            out.write(headers.getBytes("UTF-8"));
            out.write(bodyBytes);
        } else {
            serveStaticFile(reqPath, out);
        }

        out.flush();
        out.close();
        in.close();
    }

    private Request parseRequest(URI uri) {
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

    private void serveStaticFile(String reqPath, OutputStream out) throws IOException {
        if (HttpServer.staticFilesLocation != null) {
            String base = HttpServer.staticFilesLocation.startsWith("/") ? HttpServer.staticFilesLocation : "/" + HttpServer.staticFilesLocation;
            String resourcePath = base + reqPath;
            try (InputStream fileStream = HttpServer.class.getResourceAsStream(resourcePath)) {
                if (fileStream != null) {
                    byte[] body = fileStream.readAllBytes();
                    String contentType = HttpServer.getContentTypeStatic(reqPath);
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
}
