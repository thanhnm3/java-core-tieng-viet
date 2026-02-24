package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Exception topic - loads content from multiple HTML fragments.
 */
public final class ExceptionTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/exception/";
    private static final List<String> FRAGMENTS = List.of(
            "01-hierarchy.html",
            "02-keywords.html",
            "03-try-with-resources.html",
            "04-propagation.html",
            "05-jvm-cost.html",
            "06-best-practices.html"
    );

    @Override
    public String getSlug() {
        return "exception";
    }

    @Override
    public String getTitle() {
        return "Exception - try-catch, checked/unchecked";
    }

    @Override
    public String getDescription() {
        return "try-catch, checked/unchecked, try-with-resources, propagation";
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
