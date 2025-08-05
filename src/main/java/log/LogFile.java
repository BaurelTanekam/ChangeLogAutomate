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

    private static final String CHANGELOG_FILE = "src\\main\\java\\CHANGELOG_FILE.md";

    /**
     * Aktualisiert das Changelog, indem neue Änderungen hinzugefügt werden.
     *
     * Schritte:
     * Gruppiert die Einträge (`LogEntry`) nach Kategorien (z. B. "Added", "Fixed").
     * Generiert einen neuen Abschnitt `## [Unreleased]`, welcher die neuesten Änderungen enthält.
     * Liest das bestehende Changelog und fügt diese älteren Inhalte unter den neuen Bereich hinzu.
     * Speichert das gesamte aktualisierte Changelog in der Datei.
     *
     * @param entrieList Die Liste der Einträge (`LogEntry`), die im Changelog hinzugefügt werden sollen.
     * @throws IOException Wenn ein Problem beim Lesen oder Schreiben der Datei auftritt.
     */
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

    /**
     * Liest das bestehende Changelog und fügt seinen Inhalt in den neuen Inhalt ein.
     *
     * Schritte:
     * Öffnet die bestehende Changelog-Datei (`CHANGELOG_FILE`), wenn sie existiert.
     * Sucht nach der ersten Version im bestehenden Changelog (z. B. `## [1.0.0]`).
     * Fügt alle Zeilen ab der gefundenen Version in den neuen Inhalt ein.
     *
     * @param content Der neue Inhalt des Changelogs, in das das bestehende Changelog eingefügt wird.
     * @throws IOException Wenn ein Problem beim Lesen der bestehenden Datei auftritt.
     */
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

    /**
     * Fügt dem Changelog eine neue Version hinzu.
     *
     * Schritte:
     * Erstellt einen neuen Abschnitt für die angegebene Version (z. B. `## [2.0.1] - <Datum>`).
     * Gruppiert die Einträge (`entries`) nach Kategorien (mithilfe der Methode `groupedByCategory`).
     * Fügt die neuen Änderungen in die Kategorien ein, z. B. "### Added", "### Fixed".
     * Liest das bestehende Changelog und fügt es nach dem neuen Abschnitt ein.
     * Speichert das aktualisierte Changelog in die Datei.
     *
     * @param version Die neue Version, die hinzugefügt werden soll (z. B. `2.0.1`).
     * @param entries Die Liste der Einträge (`LogEntry`), die der Version zugeordnet sind.
     * @throws IOException Wenn ein Problem beim Lesen oder Schreiben der Datei auftritt.
     */
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

    /**
     * Gruppiert die Einträge (`LogEntry`) nach ihren Kategorien.
     *
     * Schritte:
     * Iteriert durch die gegebenen Entries.
     * Fügt jeden Eintrag in die passende Kategorie ein (z. B. "Added", "Fixed").
     * Gibt die Einträge in einer Map zurück, wobei der Schlüssel die Kategorie ist.
     *
     * @param entries Eine Liste von LogEntries, die gruppiert werden sollen.
     * @return Eine Map, die die Kategorien (`key`) und ihre zugehörigen Einträge (`value`) enthält.
     */
    private Map<String, List<LogEntry>> groupedByCategory(List<LogEntry> entries) {
        Map<String, List<LogEntry>> grouped = new HashMap<>();
        for (LogEntry entry : entries){
            grouped.computeIfAbsent(entry.getCategory(), k -> new ArrayList<>()).add(entry);
        }
        return grouped;
    }

    /**
     * Fügt neue JIRA-Kommentare zum Changelog hinzu.
     *
     * Schritte:
     * Überprüft, ob es gültige JIRA-Kommentare gibt. Falls NEIN, gibt die Methode eine Meldung aus und beendet.
     * Liest das existierende Changelog und speichert dessen Inhalt.
     * Prüft, ob ein Abschnitt `## Jira Comments` bereits existiert.
     * Wenn der Abschnitt existiert:
     *    - Fügt die neuen Kommentare am Ende des Abschnitts `## Jira Comments` hinzu.
     * Wenn der Abschnitt nicht existiert:
     *    - Erstellt einen neuen Abschnitt `## Jira Comments` und fügt die neuen Kommentare hinzu.
     * Schreibt das aktualisierte Changelog in die Datei.
     *
     * @param jiraComments Die Liste von JIRA-Kommentaren (`JiraComment`), die hinzugefügt werden sollen.
     * @throws IOException Wenn ein Problem beim Lesen oder Schreiben des Changelogs auftritt.
     */
    public void addJiraCommentsToChangeLog(List<JiraComment> jiraComments) throws IOException {
        //Verify
        if (jiraComments == null || jiraComments.isEmpty()){
            System.out.println("No Jira Comments available to add.");
            return;
        }

        //Load actuall ChangeLog file
        Path path = Paths.get(CHANGELOG_FILE);
        StringBuilder existingContent = new StringBuilder();
        List<String> existingLines = new ArrayList<>();

        if (Files.exists(path)){
            existingLines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String line: existingLines){
                existingContent.append(line).append("\n");
            }
        }

        StringBuilder newCommentSection = new StringBuilder();
        boolean jiraSectionFound = false;

        //Identify a "##Jira Comments" section
        for (String line : existingLines){
            if (line.trim().equalsIgnoreCase("## Jira Comments")){
                jiraSectionFound = true;
                break;
            }
        }

        if (jiraComments != null && !jiraComments.isEmpty()){
            newCommentSection.append("\n## Jira Comments");
            for (JiraComment comment : jiraComments){
                newCommentSection.append("- ").append(comment.getBodyComment()).append("\n");
            }
        }else {
            System.out.println("No new Jira comments to add.");
        }

        //new Logic
        String updatedContent;
        if (jiraSectionFound){
            StringBuilder finalContnet = new StringBuilder();
            boolean addNewComments = false;

            for (String line : existingLines){
                finalContnet.append("\n");

                if (line.trim().equalsIgnoreCase("## Jira Comments")){
                    addNewComments = true;
                    continue;
                }
            }
             //Add comment
            if (!addNewComments && (newCommentSection.length() > 0)){
                finalContnet.append(newCommentSection).append("\n");
            }

            updatedContent = finalContnet.toString();
        }else {
            //Add Jira Section, if it does not exist
            updatedContent = existingContent.append(newCommentSection).toString();
        }

        //rewrite in ChangeLog file
        Files.write(path, existingContent.toString().getBytes(StandardCharsets.UTF_8));
    }

}
