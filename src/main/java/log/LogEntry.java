package log;

import java.time.LocalDate;

public class LogEntry {
    private String category;
    private String description;
    private LocalDate date;

    public LogEntry(String category, String description) {
        this.category = category;
        this.description = description;
        this.date = LocalDate.now();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
