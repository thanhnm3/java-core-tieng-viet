package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Collections topic - loads content from multiple HTML fragments.
 */
public final class CollectionsTopic implements JavaCoreTopic {

    private static final String FRAGMENT_BASE = "web/topics/collections/";
    private static final List<String> FRAGMENTS = List.of(
            "01-intro.html",
            "02-list.html",
            "03-set.html",
            "04-map.html",
            "05-queue.html"
    );

    @Override
    public String getSlug() {
        return "collections";
    }

    @Override
    public String getTitle() {
        return "Collections - List, Set, Map, Iterator";
    }

    @Override
    public String getDescription() {
        return "List, Set, Map, Iterator, Queue, Deque";
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
