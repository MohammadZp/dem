package org.example.websocket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.Base64;

public class WebSocketServer {

    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("WebSocket server started on port " + PORT);

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "Client connected: " + socket.getRemoteSocketAddress()
                );

                handleClient(socket);
            }
        }
    }

    private static void handleClient(Socket socket) {

        try (
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream()
        ) {

            // --------------------------------
            // 1. Read HTTP handshake
            // --------------------------------

            String request = readHttpRequest(in);

            System.out.println("----- HTTP REQUEST -----");
            System.out.println(request);

            // --------------------------------
            // 2. Extract Sec-WebSocket-Key
            // --------------------------------

            String key = extractWebSocketKey(request);

            if (key == null) {
                System.out.println("Not a WebSocket request");
                socket.close();
                return;
            }

            // --------------------------------
            // 3. Calculate Sec-WebSocket-Accept
            // --------------------------------

            String acceptKey = createAcceptKey(key);

            // --------------------------------
            // 4. Send HTTP 101 response
            // --------------------------------

            String response =
                    "HTTP/1.1 101 Switching Protocols\r\n" +
                            "Upgrade: websocket\r\n" +
                            "Connection: Upgrade\r\n" +
                            "Sec-WebSocket-Accept: " + acceptKey + "\r\n" +
                            "\r\n";

            out.write(response.getBytes(StandardCharsets.UTF_8));
            out.flush();

            System.out.println("WebSocket handshake completed!");

            // --------------------------------
            // 5. Now we speak WebSocket
            // --------------------------------

            while (true) {

                WebSocketFrame frame = readFrame(in);

                if (frame == null) {
                    break;
                }

                System.out.println(
                        "Received opcode=" + frame.opcode +
                                " message=" + frame.payload
                );

                // Text frame
                if (frame.opcode == 1) {

                    String message = frame.payload;

                    System.out.println("Client says: " + message);

                    // Echo message back
                    sendTextFrame(out, "Server received: " + message);
                }

                // Close frame
                else if (frame.opcode == 8) {

                    System.out.println("Client closed connection");

                    sendCloseFrame(out);
                    break;
                }

                // Ping
                else if (frame.opcode == 9) {

                    System.out.println("Ping received");

                    sendPongFrame(out, frame.payload);
                }
            }

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    // ============================================================
    // HTTP HANDSHAKE
    // ============================================================

    private static String readHttpRequest(InputStream in)
            throws IOException {

        ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        int previous = -1;
        int current;

        while ((current = in.read()) != -1) {

            buffer.write(current);

            // Detect \r\n\r\n
            if (previous == '\r' && current == '\n') {

                byte[] data = buffer.toByteArray();

                String request =
                        new String(
                                data,
                                StandardCharsets.UTF_8
                        );

                if (request.endsWith("\r\n\r\n")) {
                    return request;
                }
            }

            previous = current;
        }

        throw new IOException("Incomplete HTTP request");
    }

    private static String extractWebSocketKey(String request) {

        for (String line : request.split("\r\n")) {

            if (line.toLowerCase()
                    .startsWith("sec-websocket-key:")) {

                return line.substring(
                        line.indexOf(":") + 1
                ).trim();
            }
        }

        return null;
    }

    private static String createAcceptKey(String clientKey)
            throws Exception {

        String magic =
                "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

        String value = clientKey + magic;

        MessageDigest sha1 =
                MessageDigest.getInstance("SHA-1");

        byte[] hash =
                sha1.digest(
                        value.getBytes(StandardCharsets.UTF_8)
                );

        return Base64.getEncoder().encodeToString(hash);
    }

    // ============================================================
    // WEBSOCKET FRAME
    // ============================================================

    private static WebSocketFrame readFrame(InputStream in)
            throws IOException {

        int firstByte = in.read();

        if (firstByte == -1) {
            return null;
        }

        int secondByte = in.read();

        if (secondByte == -1) {
            return null;
        }

        boolean fin = (firstByte & 0x80) != 0;

        int opcode = firstByte & 0x0F;

        boolean masked = (secondByte & 0x80) != 0;

        long payloadLength = secondByte & 0x7F;

        // Extended payload length
        if (payloadLength == 126) {

            payloadLength =
                    ((in.read() & 0xFF) << 8) |
                            (in.read() & 0xFF);
        }

        else if (payloadLength == 127) {

            payloadLength = 0;

            for (int i = 0; i < 8; i++) {

                payloadLength =
                        (payloadLength << 8) |
                                (in.read() & 0xFF);
            }
        }

        // Client -> Server frames MUST be masked
        byte[] maskingKey = null;

        if (masked) {

            maskingKey = in.readNBytes(4);
        }

        byte[] payload =
                in.readNBytes((int) payloadLength);

        // Unmask
        if (masked) {

            for (int i = 0; i < payload.length; i++) {

                payload[i] =
                        (byte) (
                                payload[i] ^
                                        maskingKey[i % 4]
                        );
            }
        }

        String message =
                new String(
                        payload,
                        StandardCharsets.UTF_8
                );

        return new WebSocketFrame(
                fin,
                opcode,
                message
        );
    }

    // ============================================================
    // SEND TEXT FRAME
    // ============================================================

    private static void sendTextFrame(
            OutputStream out,
            String message
    ) throws IOException {

        byte[] payload =
                message.getBytes(StandardCharsets.UTF_8);

        ByteArrayOutputStream frame =
                new ByteArrayOutputStream();

        // FIN = 1
        // OPCODE = 1 (text)
        frame.write(0x81);

        if (payload.length <= 125) {

            frame.write(payload.length);
        }

        else if (payload.length <= 65535) {

            frame.write(126);

            frame.write((payload.length >> 8) & 0xFF);
            frame.write(payload.length & 0xFF);
        }

        else {

            frame.write(127);

            long length = payload.length;

            for (int i = 7; i >= 0; i--) {

                frame.write(
                        (int) (length >> (8 * i)) & 0xFF
                );
            }
        }

        frame.write(payload);

        out.write(frame.toByteArray());
        out.flush();
    }

    // ============================================================
    // PONG
    // ============================================================

    private static void sendPongFrame(
            OutputStream out,
            String message
    ) throws IOException {

        byte[] payload =
                message.getBytes(StandardCharsets.UTF_8);

        out.write(0x8A); // FIN + PONG
        out.write(payload.length);
        out.write(payload);
        out.flush();
    }

    // ============================================================
    // CLOSE
    // ============================================================

    private static void sendCloseFrame(
            OutputStream out
    ) throws IOException {

        out.write(0x88); // FIN + CLOSE
        out.write(0);
        out.flush();
    }

    // ============================================================
    // FRAME CLASS
    // ============================================================

    private static class WebSocketFrame {

        boolean fin;
        int opcode;
        String payload;

        WebSocketFrame(
                boolean fin,
                int opcode,
                String payload
        ) {
            this.fin = fin;
            this.opcode = opcode;
            this.payload = payload;
        }
    }
}
