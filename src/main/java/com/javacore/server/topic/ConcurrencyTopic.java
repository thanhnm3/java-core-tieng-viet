package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Concurrency topic - loads content from multiple HTML fragments.
 */
public final class ConcurrencyTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/concurrency/";
    private static final List<String> FRAGMENTS = List.of(
            "01-intro.html",
            "02-lifecycle.html",
            "03-sync.html",
            "04-executor.html",
            "05-concurrent-collections.html",
            "06-modern.html",
            "07-pitfalls.html"
    );

    @Override
    public String getSlug() {
        return "concurrency";
    }

    @Override
    public String getTitle() {
        return "Concurrency - Thread, Executor, synchronized";
    }

    @Override
    public String getDescription() {
        return "Thread, Executor, synchronized, ConcurrentHashMap, CompletableFuture";
    }

    @Override
    public String getContentHtml() {
        StringBuilder sb = new StringBuilder();
        ClassLoader loader = getClass().getClassLoader();

        for (String fragment : FRAGMENTS) {
            String path = FRAGMENT_BASE + fragment;
            String content = loadFragment(loader, path);
            sb.append(content);
        }

        return sb.toString();
    }

    private String loadFragment(ClassLoader loader, String path) {
        try (InputStream in = loader.getResourceAsStream(path)) {
            if (in == null) {
                return "<p>Fragment not found: " + path + "</p>";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<p>Error loading: " + path + "</p>";
        }
    }
}
