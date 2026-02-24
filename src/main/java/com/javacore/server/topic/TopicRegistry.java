package com.javacore.server.topic;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry of all Java Core topics. Single source of truth for topic list.
 */
public final class TopicRegistry {

    private static final Map<String, JavaCoreTopic> BY_SLUG = new ConcurrentHashMap<>();

    static {
        register(new OopTopic());
        register(new CollectionsTopic());
        register(new IoNioTopic());
        register(new ConcurrencyTopic());
        register(new JvmMemoryTopic());
        register(new ExceptionTopic());
        register(new GenericsTopic());
        register(new LambdaStreamTopic());
    }

    private TopicRegistry() {
    }

    private static void register(JavaCoreTopic topic) {
        BY_SLUG.put(topic.getSlug(), topic);
    }

    public static Optional<JavaCoreTopic> findBySlug(String slug) {
        return Optional.ofNullable(BY_SLUG.get(slug));
    }

    public static Collection<JavaCoreTopic> getAll() {
        return List.copyOf(BY_SLUG.values());
    }

    private static final List<String> DISPLAY_ORDER = List.of(
            "oop", "collections", "io-nio", "concurrency", "jvm-memory",
            "exception", "generics", "lambda"
    );

    public static List<JavaCoreTopic> getTopicsInDisplayOrder() {
        return DISPLAY_ORDER.stream()
                .map(BY_SLUG::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
