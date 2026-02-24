package com.javacore.server.handler;

import com.javacore.server.server.HttpRequest;
import com.javacore.server.server.HttpResponse;

/**
 * Handler for HTTP requests. Implementations process request and populate response.
 */
@FunctionalInterface
public interface HttpHandler {

    void handle(HttpRequest req, HttpResponse res);
}
