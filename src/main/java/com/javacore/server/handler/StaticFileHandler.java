package com.javacore.server.handler;

import com.javacore.server.server.HttpRequest;
import com.javacore.server.server.HttpResponse;
import com.javacore.server.util.ContentTypes;

import java.io.IOException;
import java.io.InputStream;

/**
 * Serves static files from classpath resources under web/.
 * Rejects path traversal (..).
 */
public class StaticFileHandler implements HttpHandler {

    private static final String WEB_BASE = "web";
    private static final String PATH_TRAVERSAL = "..";

    @Override
    public void handle(HttpRequest req, HttpResponse res) {
        String path = req.getPathForRouting();
        if (path.contains(PATH_TRAVERSAL)) {
            res.setStatusCode(404);
            res.setBody("<h1>404 Not Found</h1>");
            return;
        }

        String resourcePath = WEB_BASE + path.replace("//", "/");
        if (resourcePath.endsWith("/")) {
            resourcePath = resourcePath + "index.html";
        }

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                res.setStatusCode(404);
                res.setBody("<h1>404 Not Found</h1>");
                return;
            }
            byte[] bytes = in.readAllBytes();
            res.setContentType(ContentTypes.forPath(resourcePath));
            res.setBody(bytes);
        } catch (IOException e) {
            res.setStatusCode(500);
            res.setBody("<h1>500 Internal Server Error</h1>");
        }
    }
}
