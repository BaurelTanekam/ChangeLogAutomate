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
            content.append("### ").append(category).append("\n");
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
}
