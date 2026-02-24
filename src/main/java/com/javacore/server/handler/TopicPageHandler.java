package com.javacore.server.handler;

import com.javacore.server.server.HttpRequest;
import com.javacore.server.server.HttpResponse;
import com.javacore.server.topic.JavaCoreTopic;
import com.javacore.server.topic.TopicRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Handles /topics/:slug - renders topic page with layout.
 */
public class TopicPageHandler implements HttpHandler {

    private static final String LAYOUT_PATH = "web/topics/_layout.html";
    private static final String TITLE_PLACEHOLDER = "{{title}}";
    private static final String CONTENT_PLACEHOLDER = "{{content}}";
    private static final String TOPICS_NAV_PLACEHOLDER = "{{topicsNav}}";

    @Override
    public void handle(HttpRequest req, HttpResponse res) {
        String slug = extractSlug(req.getPathForRouting());
        if (slug == null || slug.isEmpty()) {
            res.setStatusCode(404);
            res.setBody("<h1>404 Not Found</h1>");
            return;
        }

        TopicRegistry.findBySlug(slug)
                .ifPresentOrElse(
                        topic -> {
                            String content = topic.getContentHtml();
                            String topicsNav = buildTopicsNav(slug);
                            String html = wrapInLayout(topic.getTitle(), content, topicsNav);
                            res.setBody(html);
                        },
                        () -> {
                            res.setStatusCode(404);
                            res.setBody("<h1>404 Not Found</h1>");
                        });
    }

    private String extractSlug(String path) {
        if (path == null || !path.startsWith("/topics/")) {
            return null;
        }
        String slug = path.substring("/topics/".length());
        int query = slug.indexOf('?');
        if (query >= 0) {
            slug = slug.substring(0, query);
        }
        return slug.trim().isEmpty() ? null : slug;
    }

    private String wrapInLayout(String title, String content, String topicsNav) {
        String layout = loadLayout();
        if (layout == null) {
            return "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>" + title + "</title></head><body>"
                    + content + "</body></html>";
        }
        return layout.replace(TITLE_PLACEHOLDER, title != null ? title : "")
                .replace(TOPICS_NAV_PLACEHOLDER, topicsNav != null ? topicsNav : "")
                .replace(CONTENT_PLACEHOLDER, content != null ? content : "");
    }

    private String buildTopicsNav(String currentSlug) {
        List<JavaCoreTopic> topics = TopicRegistry.getTopicsInDisplayOrder();
        StringBuilder sb = new StringBuilder();
        sb.append("<nav class=\"topic-sidebar\"><ul>");
        for (JavaCoreTopic topic : topics) {
            boolean isActive = topic.getSlug().equals(currentSlug);
            String linkClass = isActive ? " class=\"topic-sidebar-link--active\"" : "";
            sb.append("<li><a href=\"/topics/").append(topic.getSlug()).append("\"")
                    .append(linkClass).append(">").append(escapeHtml(topic.getTitle()))
                    .append("</a></li>");
        }
        sb.append("</ul></nav>");
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String loadLayout() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(LAYOUT_PATH)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }
}
