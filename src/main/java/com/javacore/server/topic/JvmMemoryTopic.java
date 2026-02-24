package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JVM & Memory topic - loads content from multiple HTML fragments.
 */
public final class JvmMemoryTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/jvm-memory/";
    private static final List<String> FRAGMENTS = List.of(
            "01-architecture.html",
            "02-runtime-areas.html",
            "03-heap-generations.html",
            "04-stack-vs-heap.html",
            "05-gc.html",
            "06-tuning.html",
            "07-advanced.html"
    );

    @Override
    public String getSlug() {
        return "jvm-memory";
    }

    @Override
    public String getTitle() {
        return "JVM & Memory - Heap, Stack, GC";
    }

    @Override
    public String getDescription() {
        return "Heap, Stack, Metaspace, GC, JVM Tuning";
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
