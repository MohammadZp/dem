package ir.dotin.exam.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

// Java Program to Set up a Basic HTTP Server
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

// Driver Class
public class SimpleHttpServer
{
    // Main Method
    public static void main(String[] args)
    {
        try {
            // Create an HttpServer instance
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // Create a context for a specific path and set the handler
            server.createContext("/", new MyHandler());
            server.createContext("/save",new SaveHandler());

            // Start the server
            server.setExecutor(null); // Use the default executor
            server.start();

            System.out.println("Server is running on port 8000");
        } catch (IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
    }

    // Define a custom HttpHandler
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException
        {
            // Handle the request
            String response = "Hello, this is a simple HTTP server response!";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class SaveHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // Print request information
            System.out.println("Method: " + exchange.getRequestMethod());
            System.out.println("URI: " + exchange.getRequestURI());

            // Print headers
            System.out.println("Headers:");
            exchange.getRequestHeaders()
                    .forEach((key, value) ->
                            System.out.println(key + ": " + value));

            // Read request body
            String requestBody = new String(
                    exchange.getRequestBody().readAllBytes()
            );

            System.out.println("Body:");
            System.out.println(requestBody);

            // Send response
            String response = "Saved successfully!";
            exchange.sendResponseHeaders(200, response.length());

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }
}
