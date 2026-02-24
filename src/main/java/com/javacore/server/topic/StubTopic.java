package com.javacore.server.topic;

/**
 * Placeholder topic for content not yet implemented.
 */
public final class StubTopic implements JavaCoreTopic {

    private final String slug;
    private final String title;
    private final String description;

    public StubTopic(String slug, String title, String description) {
        this.slug = slug;
        this.title = title;
        this.description = description;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getContentHtml() {
        return """
            <section class="coming-soon">
              <h2>%s</h2>
              <p>Nội dung đang được biên soạn.</p>
            </section>
            """.formatted(title);
    }
}
