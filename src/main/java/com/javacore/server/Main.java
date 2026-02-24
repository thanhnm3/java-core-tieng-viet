package com.javacore.server;

import com.javacore.server.handler.MetricsHandler;
import com.javacore.server.handler.StaticFileHandler;
import com.javacore.server.handler.TopicPageHandler;
import com.javacore.server.server.NioServer;
import com.javacore.server.server.Router;

import java.io.IOException;

/**
 * Entry point for Java Core Custom Web Server.
 * Listens on port 9091 by default.
 */
public final class Main {

    private static final int DEFAULT_PORT = 9091;

    public static void main(String[] args) {
        int port = parsePort(args);
        Router router = buildRouter();

        NioServer server = new NioServer(port, router);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
            server.stop();
            try {
                server.close();
            } catch (IOException e) {
                System.err.println("Error closing server: " + e.getMessage());
            }
        }));

        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static int parsePort(String[] args) {
        if (args.length > 0) {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port, using default: " + DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }

    private static Router buildRouter() {
        Router router = new Router();
        router.addRoute("/topics/", new TopicPageHandler());
        router.addRoute("/api/", new MetricsHandler());
        router.addRoute("/css/", new StaticFileHandler());
        router.addRoute("/js/", new StaticFileHandler());
        router.addRoute("/", new StaticFileHandler());
        return router;
    }
}
