package jira;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class JiraService {
    private String key;
    private String priority;
    private String status;
    private String title;
    private LocalDate created;
    private LocalDate updated;
    private String assigned;
    private String reporter;
    private String issueType;
    private List<JiraComment> comments;

    public JiraService(){
        this.comments = new ArrayList<>();
    }

    
    //Getters und Setters

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getCreated() {
        return created;
    }

    public void setCreated(LocalDate created) {
        this.created = created;
    }

    public LocalDate getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDate updated) {
        this.updated = updated;
    }

    public String getAssigned() {
        return assigned;
    }

    public void setAssigned(String assigned) {
        this.assigned = assigned;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public String getIssueType() {
        return issueType;
    }


    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public List<JiraComment> getComments() {
        return comments;
    }

    public void setComments(List<JiraComment> comments) {
        this.comments = comments;
    }


    public void printComment(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (this.getComments() != null && !this.getComments().isEmpty()){
            System.out.println("\n=== Commentaires ====");

            for (JiraComment comment : this.getComments()){
                if (comment.getAuthor() == null || comment.getBodyComment() == null) {
                    comment.setAuthor("Unknown Author");
                    comment.setBodyComment("Kein Comment.");
                }
                String authorDisplayNmae = comment.getAuthor();
                String body = comment.getBodyComment();
                String created = (comment.getCreatedComment() != null ? comment.getCreatedComment().toString() : "Unknown Date");
                String updated = (comment.getUpdatedComment() != null ? comment.getUpdatedComment().format(formatter) : "Unknown Date");

                System.out.println("💬 " + authorDisplayNmae + ": "
                        + body.substring(0, Math.min(100, body.length()))
                        + "... ");
                System.out.println("📅 Created: " + created);
                System.out.println("📅 Updated: " + updated);

            }
        }
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("==== JIRA: ").append(key).append(" ===\n");
        sb.append("Priority: ").append(priority).append("\n");
        sb.append("IssueType: ").append(issueType).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("Created: ").append(created).append("\n");
        sb.append("Updated: ").append(updated).append("\n");
        sb.append("Assigned: ").append(assigned).append("\n");
        sb.append("Reporter: ").append(reporter).append("\n");
        
        if(!comments.isEmpty()){
            sb.append("\n==== Comments (").append(comments.size()).append(") ===\n");
            for(JiraComment comment : comments){
                sb.append(comment.toString()).append("\n");
            }
        }

        return sb.toString();
    }
}
