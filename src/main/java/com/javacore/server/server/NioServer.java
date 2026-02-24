package com.javacore.server.server;

import com.javacore.server.handler.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO-based HTTP server using Selector for multiplexing connections.
 * Accepts connections, reads request, routes to handler, writes response, closes connection.
 */
public class NioServer {

    private static final int BUFFER_SIZE = 8192;
    private static final int MAX_REQUEST_SIZE = 64 * 1024;

    private final int port;
    private final Router router;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private volatile boolean running;

    public NioServer(int port, Router router) {
        this.port = port;
        this.router = router;
    }

    public void start() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(port));

        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        running = true;
        System.out.println("Server listening on port " + port);

        while (running) {
            int ready = selector.select(1000);
            if (ready == 0) {
                continue;
            }

            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    } else if (key.isWritable()) {
                        handleWrite(key);
                    }
                } catch (Exception e) {
                    closeChannel(key);
                }
            }
        }
    }

    public void stop() {
        running = false;
        if (selector != null && selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        if (client == null) {
            return;
        }
        client.configureBlocking(false);
        ConnectionState state = new ConnectionState();
        client.register(selector, SelectionKey.OP_READ, state);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectionState state = (ConnectionState) key.attachment();

        int read = channel.read(state.readBuffer);
        if (read == -1) {
            closeChannel(key);
            return;
        }

        state.readBuffer.flip();
        state.accumulator.write(state.readBuffer.array(), state.readBuffer.position(), state.readBuffer.remaining());
        state.readBuffer.clear();

        byte[] bytes = state.accumulator.toByteArray();
        if (bytes.length > MAX_REQUEST_SIZE) {
            sendErrorAndClose(channel, 413);
            return;
        }

        if (!state.hasParsed) {
            int headerEnd = findHeaderEnd(bytes);
            if (headerEnd < 0) {
                return;
            }
            String contentLengthStr = extractHeader(bytes, "Content-Length");
            int bodyLength = 0;
            if (contentLengthStr != null) {
                try {
                    bodyLength = Integer.parseInt(contentLengthStr.trim());
                } catch (NumberFormatException ignored) {
                }
            }
            int expectedTotal = headerEnd + bodyLength;
            if (bytes.length < expectedTotal) {
                return;
            }

            try {
                state.request = HttpParser.parse(bytes);
                state.hasParsed = true;
            } catch (HttpParser.HttpParseException e) {
                sendErrorAndClose(channel, 400);
                return;
            }

            HttpResponse response = new HttpResponse();
            HttpHandler handler = router.route(state.request);
            if (handler != null) {
                handler.handle(state.request, response);
            } else {
                response.setStatusCode(404);
                response.setBody("<h1>404 Not Found</h1>");
            }

            state.responseBytes = buildResponseBytes(response);
            key.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ConnectionState state = (ConnectionState) key.attachment();

        ByteBuffer toWrite = ByteBuffer.wrap(state.responseBytes);
        toWrite.position(state.bytesWritten);
        int written = channel.write(toWrite);
        state.bytesWritten += written;

        if (state.bytesWritten >= state.responseBytes.length) {
            closeChannel(key);
        }
    }

    private void closeChannel(SelectionKey key) {
        try {
            key.cancel();
            if (key.channel().isOpen()) {
                key.channel().close();
            }
        } catch (IOException ignored) {
        }
    }

    private void sendErrorAndClose(SocketChannel channel, int statusCode) throws IOException {
        String body = "<h1>" + statusCode + " " + (statusCode == 413 ? "Payload Too Large" : "Bad Request") + "</h1>";
        String response = "HTTP/1.1 " + statusCode + " " + (statusCode == 413 ? "Payload Too Large" : "Bad Request") + "\r\n"
                + "Content-Type: text/html; charset=utf-8\r\n"
                + "Content-Length: " + body.getBytes(StandardCharsets.UTF_8).length + "\r\n"
                + "Connection: close\r\n\r\n"
                + body;
        ByteBuffer buf = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
        while (buf.hasRemaining()) {
            channel.write(buf);
        }
        channel.close();
    }

    private int findHeaderEnd(byte[] bytes) {
        for (int i = 0; i <= bytes.length - 4; i++) {
            if (bytes[i] == '\r' && bytes[i + 1] == '\n'
                    && bytes[i + 2] == '\r' && bytes[i + 3] == '\n') {
                return i + 4;
            }
        }
        return -1;
    }

    private String extractHeader(byte[] bytes, String headerName) {
        String raw = new String(bytes, StandardCharsets.UTF_8);
        String target = headerName + ":";
        int idx = raw.indexOf(target);
        if (idx < 0) {
            return null;
        }
        int start = idx + target.length();
        int end = raw.indexOf("\r\n", start);
        if (end < 0) {
            end = raw.length();
        }
        return raw.substring(start, end).trim();
    }

    private byte[] buildResponseBytes(HttpResponse res) {
        String statusLine = "HTTP/1.1 " + res.getStatusCode() + " " + res.getStatusMessage() + "\r\n";
        StringBuilder headerSection = new StringBuilder();
        res.getHeaders().forEach((name, value) ->
                headerSection.append(name).append(": ").append(value).append("\r\n"));
        if (!res.getHeaders().containsKey("Content-Length")) {
            headerSection.append("Content-Length: ").append(res.getBody().length).append("\r\n");
        }
        headerSection.append("Connection: close\r\n\r\n");

        byte[] statusBytes = statusLine.getBytes(StandardCharsets.UTF_8);
        byte[] headerBytes = headerSection.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bodyBytes = res.getBody();

        byte[] result = new byte[statusBytes.length + headerBytes.length + bodyBytes.length];
        System.arraycopy(statusBytes, 0, result, 0, statusBytes.length);
        System.arraycopy(headerBytes, 0, result, statusBytes.length, headerBytes.length);
        System.arraycopy(bodyBytes, 0, result, statusBytes.length + headerBytes.length, bodyBytes.length);

        return result;
    }

    public void close() throws IOException {
        running = false;
        if (selector != null && selector.isOpen()) {
            selector.close();
        }
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close();
        }
    }

    private static class ConnectionState {
        final ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        final ByteArrayOutputStream accumulator = new ByteArrayOutputStream();
        boolean hasParsed;
        HttpRequest request;
        byte[] responseBytes;
        int bytesWritten;
    }
}
