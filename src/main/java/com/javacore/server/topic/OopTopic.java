package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * OOP topic - loads content from multiple HTML fragments.
 */
public final class OopTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/oop/";
    private static final List<String> FRAGMENTS = List.of(
            "01-intro.html",
            "02-encapsulation.html",
            "03-inheritance.html",
            "04-polymorphism.html",
            "05-abstraction.html"
    );

    @Override
    public String getSlug() {
        return "oop";
    }

    @Override
    public String getTitle() {
        return "OOP - Lập trình hướng đối tượng";
    }

    @Override
    public String getDescription() {
        return "Class, Object, Kế thừa, Đa hình, Đóng gói";
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
