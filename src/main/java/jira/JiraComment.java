package jira;

import java.time.LocalDateTime;

public class JiraComment {
    private String id;
    private String author;
    private String bodyComment;
    private LocalDateTime createdComment;
    private LocalDateTime updatedComment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBodyComment() {
        return bodyComment;
    }

    public void setBodyComment(String bodyComment) {
        this.bodyComment = bodyComment;
    }

    public LocalDateTime getUpdatedComment() {
        return updatedComment;
    }

    public void setUpdatedComment(LocalDateTime updatedComment) {
        this.updatedComment = updatedComment;
    }

    public LocalDateTime getCreatedComment() {
        return createdComment;
    }

    public void setCreatedComment(LocalDateTime createdComment) {
        this.createdComment = createdComment;
    }

    @Override
    public String toString() {
        return String.format("👤 %s :\n" + //
                        "📝 %s\n" + //
                        "📅 Created: %s\n" + //
                        "📅 Updated: %s\n",
                author != null ? author : "Unknown Author",
                bodyComment != null ? bodyComment : "No Comment",
                createdComment != null ? createdComment.toString() : "Unknown Date",
                updatedComment != null ? updatedComment.toString() : "Unknown Date");
    }
}
