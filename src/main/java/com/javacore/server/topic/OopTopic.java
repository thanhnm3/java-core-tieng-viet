package com.javacore.server.topic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * OOP topic - fully implemented with real content.
 */
public final class OopTopic implements JavaCoreTopic {

    private static final String RESOURCE_PATH = "web/topics/oop.html";

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
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                return "<p>Content not found.</p>";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<p>Error loading content.</p>";
        }
    }
}
