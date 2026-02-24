package com.javacore.server.handler;

import com.javacore.server.server.HttpRequest;
import com.javacore.server.server.HttpResponse;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.charset.StandardCharsets;

/**
 * Serves JVM metrics as JSON for the Live Metrics dashboard.
 */
public class MetricsHandler implements HttpHandler {

    @Override
    public void handle(HttpRequest req, HttpResponse res) {
        String path = req.getPathForRouting();
        String normalized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        if (!"/api/metrics".equals(normalized)) {
            res.setStatusCode(404);
            res.setBody("<h1>404 Not Found</h1>");
            return;
        }
        res.setContentType("application/json; charset=utf-8");

        MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threads = ManagementFactory.getThreadMXBean();

        long heapUsed = memory.getHeapMemoryUsage().getUsed();
        long heapMax = memory.getHeapMemoryUsage().getMax() > 0
                ? memory.getHeapMemoryUsage().getMax()
                : memory.getHeapMemoryUsage().getCommitted();
        int threadCount = threads.getThreadCount();

        String json = String.format(
                "{\"heapUsed\":%d,\"heapMax\":%d,\"threadCount\":%d}",
                heapUsed, heapMax, threadCount
        );
        res.setBody(json.getBytes(StandardCharsets.UTF_8));
    }
}
