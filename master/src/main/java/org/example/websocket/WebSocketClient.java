package org.example.websocket;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class WebSocketClient {

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket("localhost", 8080);
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();

        // ================================
        // 1. WebSocket HTTP handshake
        // ================================

        String key = createWebSocketKey();

        String request =
                "GET / HTTP/1.1\r\n" +
                        "Host: localhost:8080\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Key: " + key + "\r\n" +
                        "Sec-WebSocket-Version: 13\r\n" +
                        "\r\n";

        out.write(request.getBytes(StandardCharsets.UTF_8));
        out.flush();

        // Read server's 101 response
        String response = readHttpResponse(in);

        System.out.println("SERVER RESPONSE:");
        System.out.println(response);

        // ================================
        // 2. Send WebSocket message
        // ================================

        sendTextFrame(out, "Hello Server!");

        // ================================
        // 3. Read server response
        // ================================

        String message = readTextFrame(in);

        System.out.println("SERVER MESSAGE: " + message);

        socket.close();
    }

    private static String createWebSocketKey() {

        byte[] bytes = new byte[16];

        new SecureRandom().nextBytes(bytes);

        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String readHttpResponse(InputStream in)
            throws IOException {

        ByteArrayOutputStream buffer =
                new ByteArrayOutputStream();

        int previous = -1;
        int current;

        while ((current = in.read()) != -1) {

            buffer.write(current);

            if (previous == '\r' && current == '\n') {

                byte[] data = buffer.toByteArray();

                String response =
                        new String(
                                data,
                                StandardCharsets.UTF_8
                        );

                if (response.endsWith("\r\n\r\n")) {
                    return response;
                }
            }

            previous = current;
        }

        throw new IOException("Invalid HTTP response");
    }

    private static void sendTextFrame(
            OutputStream out,
            String message
    ) throws IOException {

        byte[] payload =
                message.getBytes(StandardCharsets.UTF_8);

        // --------------------------------
        // First byte
        //
        // FIN = 1
        // OPCODE = 1 (text)
        //
        // 1000 0001 = 0x81
        // --------------------------------

        out.write(0x81);

        // --------------------------------
        // Client MUST mask the frame
        // --------------------------------

        byte[] mask = new byte[4];

        new SecureRandom().nextBytes(mask);

        int length = payload.length;

        // MASK bit = 1
        out.write(0x80 | length);

        // Masking key
        out.write(mask);

        // Mask payload
        for (int i = 0; i < payload.length; i++) {

            payload[i] =
                    (byte) (
                            payload[i] ^
                                    mask[i % 4]
                    );
        }

        // Payload
        out.write(payload);

        out.flush();
    }

    private static String readTextFrame(
            InputStream in
    ) throws IOException {

        int firstByte = in.read();

        int secondByte = in.read();

        if (firstByte == -1 || secondByte == -1) {
            throw new IOException("Connection closed");
        }

        int opcode = firstByte & 0x0F;

        if (opcode != 1) {
            throw new IOException(
                    "Expected text frame but got opcode "
                            + opcode
            );
        }

        int payloadLength = secondByte & 0x7F;

        byte[] payload =
                in.readNBytes(payloadLength);

        return new String(
                payload,
                StandardCharsets.UTF_8
        );
    }
}