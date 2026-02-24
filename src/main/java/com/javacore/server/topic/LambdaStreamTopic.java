package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Lambda & Stream topic — functional interfaces, Stream API, Optional.
 */
public final class LambdaStreamTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/lambda/";
    private static final List<String> FRAGMENTS = List.of(
            "01-intro.html",
            "02-functional.html",
            "03-syntax.html",
            "04-stream-intro.html",
            "05-intermediate.html",
            "06-terminal.html",
            "07-advanced.html"
    );

    @Override
    public String getSlug() {
        return "lambda";
    }

    @Override
    public String getTitle() {
        return "Lambda & Stream";
    }

    @Override
    public String getDescription() {
        return "Functional interface, Stream API, Optional, Collectors";
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
