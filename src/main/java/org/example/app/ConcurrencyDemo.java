package org.example.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConcurrencyDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("🧪 Concurrency Demo - Testing Multiple Concurrent Requests\n");
        
        // Start the server in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                DemoApplication.main(new String[]{});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Give the server time to start
        Thread.sleep(2000);
        
        System.out.println("📡 Server started. Sending 5 concurrent requests...\n");
        
        // Create 5 concurrent requests
        List<Thread> clientThreads = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            final int clientId = i;
            Thread clientThread = new Thread(() -> makeRequest(clientId));
            clientThreads.add(clientThread);
            clientThread.start();
        }
        
        // Wait for all client threads to complete
        for (Thread thread : clientThreads) {
            thread.join();
        }
        
        System.out.println("\n✅ All concurrent requests completed!");
    }
    
    private static void makeRequest(int clientId) {
        try {
            System.out.println("👤 Client-" + clientId + " starting request...");
            
            Socket socket = new Socket("localhost", 35000);
            
            // Send HTTP request
            String request = "GET /greeting?name=Client" + clientId + " HTTP/1.1\r\n"
                           + "Host: localhost:35000\r\n"
                           + "Connection: close\r\n"
                           + "\r\n";
            socket.getOutputStream().write(request.getBytes());
            socket.getOutputStream().flush();
            
            // Read response
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            String line;
            boolean foundBody = false;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    foundBody = true;
                } else if (foundBody) {
                    System.out.println("👤 Client-" + clientId + " received: " + line);
                    break;
                }
            }
            
            reader.close();
            socket.close();
            
        } catch (Exception e) {
            System.err.println("👤 Client-" + clientId + " error: " + e.getMessage());
        }
    }
}
