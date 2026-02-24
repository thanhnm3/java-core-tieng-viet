package com.javacore.server.server;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Mutable HTTP response model.
 * Default status 200, UTF-8 charset.
 */
public class HttpResponse {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private int statusCode = 200;
    private String statusMessage = "OK";
    private final Map<String, String> headers = new HashMap<>();
    private byte[] body = new byte[0];

    public HttpResponse() {
        setContentType("text/html; charset=utf-8");
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        this.statusMessage = getDefaultStatusMessage(statusCode);
    }

    public void setStatus(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage != null ? statusMessage : getDefaultStatusMessage(statusCode);
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public Map<String, String> getHeaders() {
        return Map.copyOf(headers);
    }

    public void setHeader(String name, String value) {
        if (name != null && value != null) {
            headers.put(name, value);
        }
    }

    public void setContentType(String contentType) {
        setHeader("Content-Type", contentType);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body != null ? body : new byte[0];
    }

    public void setBody(String text, Charset charset) {
        this.body = text != null ? text.getBytes(charset) : new byte[0];
    }

    public void setBody(String text) {
        setBody(text, DEFAULT_CHARSET);
    }

    private static String getDefaultStatusMessage(int code) {
        return switch (code) {
            case 200 -> "OK";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 400 -> "Bad Request";
            case 413 -> "Payload Too Large";
            default -> "Unknown";
        };
    }
}
