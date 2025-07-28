package jira;

import java.time.LocalDateTime;

public class JiraComment {
    private String id;
    private String author;
    private String bodyComment;
    private LocalDateTime createdComment;
    private LocalDateTime updatedComment;

    @Override
    public String toString(){
        return String.format("👤 %s (%s):\n" + //
                        "📝 %s\n" + //
                        "📅 %s\n 📅 %s\n", author, bodyComment, 
                        createdComment != null ? createdComment.toString() : "Unknown Date.", 
                        updatedComment != null ? updatedComment.toString() : "Unknown Date.");
    }
}
