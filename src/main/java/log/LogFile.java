package log;

import jira.JiraComment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogFile {

    private static final String CHANGELOG_FILE = "src\\main\\java\\CHANGELOG.md";

    public void updateChangeLog(List<LogEntry> entrieList) throws IOException{
        Map<String, List<LogEntry>> grouped = new HashMap();

        for (LogEntry entry : entrieList) {
            String category = entry.getCategory();
            if(!grouped.containsKey(category))
            {
                grouped.put(category, new ArrayList<>());
            }
            grouped.get(category).add(entry);
        }

        //generate Changelog 
        StringBuilder content = new StringBuilder();
        content.append("# Changelog\n\n");
        content.append("## [Unreleased] - ").append(LocalDate.now()).append("\n\n");

        //add section manually
        List<String> categories = Arrays.asList("Added", "Changed", "Fixed", "Removed");
        for (String category : categories) {
            content.append("## ").append(category).append("\n");
            List<LogEntry> categoryEntries = grouped.get(category);

            if (categoryEntries == null || categoryEntries.isEmpty()) {
                content.append("\n"); // Ajouter une ligne vide si aucune entrée
            } else {
                for (LogEntry entry : categoryEntries) {
                    content.append("- ").append(entry.getDescription()).append("\n"); // Ajouter chaque entrée
                }
                content.append("\n"); // Ligne vide après chaque section
            }
        }

        //add old version of a changelog
        appendexistingChangeLog(content);

        //save
        Path path = Paths.get(CHANGELOG_FILE);
        Files.write(path, content.toString().getBytes(StandardCharsets.UTF_8));

    }

    private void appendexistingChangeLog(StringBuilder content) throws IOException {
        
        Path path = Paths.get(CHANGELOG_FILE);
        if (Files.exists(path)) {
            List<String> existingLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            boolean foundFirstVersion = false;

            for (String line : existingLines) {
                // Rechercher la première section contenant une version
                if (line.startsWith("## [") && !line.contains("[Unreleased]")) {
                    foundFirstVersion = true;
                }

                // Ajouter le contenu existant après la première version trouvée
                if (foundFirstVersion) {
                    content.append(line).append("\n");
                }
            }
        }
    }

    public void addReleaseVersionToChangeLog(String version, List<LogEntry> entries) throws IOException {
        StringBuilder content = new StringBuilder();

        String releaseHeader = "## [" + version + "] - " + LocalDate.now() + "\n";
        content.append("# Release").append(version).append("\n");
        content.append(releaseHeader).append("\n");

        //Add Categories and Entry
        Map<String, List<LogEntry>> groupedEntries = groupedByCategory(entries);
        for (String category : groupedEntries.keySet()){
            content.append("###").append(category).append("\n");
            for (LogEntry logEntry : groupedEntries.get(category)){
                content.append("- ").append(logEntry.getDescription()).append("\n");
            }
            content.append("\n");
        }
        // Ajouter le contenu existant du changelog en bas
         appendexistingChangeLog(content);

        Path path = Paths.get(CHANGELOG_FILE);
        Files.write(path, content.toString().getBytes(StandardCharsets.UTF_8));

        System.out.println("Changelog updated with version: " + version);
    }

    private Map<String, List<LogEntry>> groupedByCategory(List<LogEntry> entries) {
        Map<String, List<LogEntry>> grouped = new HashMap<>();
        for (LogEntry entry : entries){
            grouped.computeIfAbsent(entry.getCategory(), k -> new ArrayList<>()).add(entry);
        }
        return grouped;
    }

    public void addJiraCommentsToChangeLog(List<JiraComment> jiraComments) throws IOException {
        StringBuilder content = new StringBuilder();

        //Verify
        if (jiraComments == null || jiraComments.isEmpty()){
            System.out.println("No Jira Comments available to add.");
            return;
        }

        //Load actuall ChangeLog file
        Path path = Paths.get(CHANGELOG_FILE);
        List<String> existingLines = new ArrayList<>();
        if (Files.exists(path)){
            existingLines = Files.readAllLines(path, StandardCharsets.UTF_8);
        }

        //Comment - Section
        content.append("\n## Jira Comments\n");
        for (JiraComment comment : jiraComments){
            content.append("- ").append(comment).append("\n");
        }

        //Fusion
        for (String line : existingLines){
            content.append(line).append("\n");
        }

        //rewrite in ChangeLog file
        Files.write(path, content.toString().getBytes(StandardCharsets.UTF_8));
    }

}
