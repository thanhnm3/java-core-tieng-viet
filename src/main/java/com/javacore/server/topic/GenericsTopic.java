package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Generics topic - loads content from multiple HTML fragments.
 */
public final class GenericsTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/generics/";
    private static final List<String> FRAGMENTS = List.of(
            "01-intro.html",
            "02-class-method.html",
            "03-bounded.html",
            "04-wildcards.html",
            "05-pecs.html",
            "06-erasure-limits.html"
    );

    @Override
    public String getSlug() {
        return "generics";
    }

    @Override
    public String getTitle() {
        return "Generics - Type parameters, wildcards";
    }

    @Override
    public String getDescription() {
        return "Type parameters, wildcards, PECS, type erasure";
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
