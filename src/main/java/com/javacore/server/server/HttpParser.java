package com.javacore.server.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses raw HTTP request bytes into HttpRequest.
 * Supports GET with simple path and headers. Limits line/header size to prevent abuse.
 */
public final class HttpParser {

    private static final int MAX_LINE_LENGTH = 8192;
    private static final int MAX_HEADERS = 64;

    private HttpParser() {
    }

    /**
     * Parses HTTP request from ByteBuffer.
     * Buffer position is advanced to end of parsed request.
     *
     * @param buffer input bytes (position updated)
     * @return parsed HttpRequest
     * @throws HttpParseException on parse error
     */
    public static HttpRequest parse(ByteBuffer buffer) throws HttpParseException {
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return parse(bytes);
    }

    /**
     * Parses HTTP request from byte array.
     *
     * @param bytes raw request bytes
     * @return parsed HttpRequest
     * @throws HttpParseException on parse error
     */
    public static HttpRequest parse(byte[] bytes) throws HttpParseException {
        try (Reader reader = new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.UTF_8)) {
            String requestLine = readLine(reader);
            if (requestLine == null || requestLine.isEmpty()) {
                throw new HttpParseException("Empty request line");
            }

            String[] parts = requestLine.split("\\s+", 3);
            if (parts.length < 2) {
                throw new HttpParseException("Invalid request line: " + requestLine);
            }
            String method = parts[0];
            String path = parts[1];

            Map<String, String> headers = new HashMap<>();
            String line;
            int headerCount = 0;
            while ((line = readLine(reader)) != null && !line.isEmpty()) {
                if (++headerCount > MAX_HEADERS) {
                    throw new HttpParseException("Too many headers");
                }
                int colon = line.indexOf(':');
                if (colon > 0) {
                    String name = line.substring(0, colon).trim();
                    String value = line.substring(colon + 1).trim();
                    headers.put(name, value);
                }
            }

            byte[] body = new byte[0];
            String contentLengthStr = headers.get("Content-Length");
            if (contentLengthStr != null) {
                try {
                    int contentLength = Integer.parseInt(contentLengthStr.trim());
                    if (contentLength < 0 || contentLength > 1024 * 1024) {
                        throw new HttpParseException("Invalid Content-Length");
                    }
                    int headerEnd = findHeaderEnd(bytes);
                    if (headerEnd >= 0 && headerEnd + contentLength <= bytes.length) {
                        body = new byte[contentLength];
                        System.arraycopy(bytes, headerEnd, body, 0, contentLength);
                    }
                } catch (NumberFormatException e) {
                    throw new HttpParseException("Invalid Content-Length: " + contentLengthStr);
                }
            }

            return HttpRequest.of(method, path, headers, body);
        } catch (IOException e) {
            throw new HttpParseException("Parse error: " + e.getMessage());
        }
    }

    private static String readLine(Reader reader) throws IOException, HttpParseException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '\r') {
                reader.read();
                break;
            }
            if (c == '\n') {
                break;
            }
            sb.append((char) c);
            if (sb.length() > MAX_LINE_LENGTH) {
                throw new HttpParseException("Line too long");
            }
        }
        return sb.toString();
    }

    private static int findHeaderEnd(byte[] bytes) {
        byte[] separator = {'\r', '\n', '\r', '\n'};
        for (int i = 0; i <= bytes.length - 4; i++) {
            if (bytes[i] == separator[0] && bytes[i + 1] == separator[1]
                    && bytes[i + 2] == separator[2] && bytes[i + 3] == separator[3]) {
                return i + 4;
            }
        }
        return -1;
    }

    public static class HttpParseException extends Exception {
        public HttpParseException(String message) {
            super(message);
        }
    }
}
