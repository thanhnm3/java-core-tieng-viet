package com.javacore.server.server;

import com.javacore.server.handler.HttpHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Routes HTTP requests to handlers by path prefix.
 * First matching rule wins.
 */
public class Router {

    private final List<RouteRule> rules = new ArrayList<>();

    public void addRoute(String pathPrefix, HttpHandler handler) {
        rules.add(new RouteRule(pathPrefix, handler));
    }

    public HttpHandler route(HttpRequest request) {
        String path = request.getPathForRouting();
        if (path.isEmpty()) {
            path = "/";
        }
        for (RouteRule rule : rules) {
            if ("/".equals(rule.pathPrefix)) {
                if ("/".equals(path)) {
                    return rule.handler;
                }
            } else if (path.startsWith(rule.pathPrefix)) {
                return rule.handler;
            }
        }
        return null;
    }

    private record RouteRule(String pathPrefix, HttpHandler handler) {
    }
}
