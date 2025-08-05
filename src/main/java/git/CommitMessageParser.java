package git;

import log.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitMessageParser {
    // Muster für verschiedene Kategorien von Commit-Nachrichten basierend auf häufig verwendeten Schlüsselwörtern
    private final Pattern FEAT_PATTERN = Pattern.compile("(?i).*((add|feat|feature|new|implement).*)", Pattern.CASE_INSENSITIVE);
    private final Pattern FIX_PATTERN = Pattern.compile("(?i).*((fix|bug|correct|resolve|patch).*)", Pattern.CASE_INSENSITIVE);
    private final Pattern DOCS_PATTERN = Pattern.compile("(?i).*((doc|readme|comment).*)", Pattern.CASE_INSENSITIVE);
    private final Pattern REFRACTOR_PATTERN = Pattern.compile("(?i).*((refactor|clean|improve|optimize).*)", Pattern.CASE_INSENSITIVE);
    // Muster, um das "Conventional Commits"-Format zu erkennen (z. B. 'feat(login): Added login feature')
    private final Pattern CONVENTION_PATTERN = Pattern.compile("^(feat|fix|docs|style|refactor|test|chore)(\\(.+\\))?!?:\\s*(.+)$");

    /**
     * Analysiert eine einzelne Commit-Nachricht und kategorisiert sie basierend auf
     * dem "Conventional Commits"-Format oder Keywords.
     *
     * @param commitMessage Die Commit-Nachricht, die analysiert werden soll
     * @return Ein LogEntry-Objekt, das die Kategorie und den bereinigten Nachrichtentext enthält,
     *         oder `null`, wenn die Nachricht leer oder ungültig ist.
     */
    public LogEntry parseCommitMessage(String commitMessage){
        if (commitMessage == null || commitMessage.trim().isEmpty()){
            return null;
        }

        String message = commitMessage.trim();

        Matcher conventionalMatcher = CONVENTION_PATTERN.matcher(message);

        if (conventionalMatcher.matches()){
            String type = conventionalMatcher.group(1);
            String description = conventionalMatcher.group(3);
            return new LogEntry(mapConventionalType(type), description);
        }
        
        String category = detectCategoryFromKeywords(message);
        return new LogEntry(category, cleanupMessage(message));
    }

    /**
     * Bereinigt den Inhalt einer Commit-Nachricht, indem bekannte Präfixe (z. B. "fix:") entfernt werden.
     *
     * @param message Die zu bereinigende Nachricht
     * @return Die bereinigte Nachricht ohne Präfixe oder überflüssige Wörter
     */
    private String cleanupMessage(String message) {
        return message.replaceAll("^(feat|fix|add|implement|refactor|update|docs?):\\s*", "")
                .replaceAll("^(Added|Fixed|Updated|Implemented)\\s+", "")
                .trim();
    }

    /**
     * Mapped den Typ einer Nachricht im "Conventional Commits"-Format (feat, fix, docs etc.)
     * zu einer allgemeineren Kategorie (Added, Fixed, Changed).
     *
     * @param type Der Typ der Nachricht (z. B. feat, fix, refactor)
     * @return Die gemappte Kategorie (z. B. Added, Fixed)
     */
    private String mapConventionalType(String type) {
        switch (type.toLowerCase()) {
            case "feat": return "Added";
            case "fix": return "Fixed";
            case "docs": return "Changed"; // Documentation updates
            case "refactor": return "Changed";
            case "style": return "Changed";
            default: return "Changed";
        }
    }

    /**
     * Bestimmt die Kategorie einer Commit-Nachricht basierend auf bekannten Schlüsselwörtern.
     *
     * @param message Die Nachricht, deren Kategorie erkannt werden soll
     * @return Die erkannte Kategorie ("Added", "Fixed", "Changed")
     */
    private  String detectCategoryFromKeywords(String message) {
        if (FEAT_PATTERN.matcher(message).matches()) {
            return "Added";
        } else if (FIX_PATTERN.matcher(message).matches()) {
            return "Fixed";
        } else if (DOCS_PATTERN.matcher(message).matches()) {
            return "Changed";
        } else if (REFRACTOR_PATTERN.matcher(message).matches()) {
            return "Changed";
        }
        return "Changed"; // As default value
    }

    /**
     * Prüft, ob eine Commit-Nachricht ignoriert werden soll (z. B. temporäre oder nicht relevante Commits).
     *
     * @param commitMessage Die Nachricht, die überprüft werden soll
     * @return `true`, wenn die Nachricht ignoriert werden soll, sonst `false`
     */
    private boolean shouldIgnoreCommit(String commitMessage){

        if (commitMessage == null) return true;

        String loweString = commitMessage.toLowerCase();
        return loweString.contains("merge") || 
                loweString.contains("wip") ||
                loweString.contains("temp") ||
                loweString.startsWith("revert") ||
                loweString.matches("^\\s*$");
    }

    /**
     * Parst eine Liste von Commits und kategorisiert sie in logische Einträge, die nicht ignoriert werden sollen.
     *
     * @param commiList Die Liste der Commits (jedes Commit enthält eine Nachricht)
     * @return Eine Liste von LogEntry-Objekten, die die relevanten Commit-Nachrichten enthalten
     */
    public List<LogEntry> parseCommits(List<GitCommit> commiList){
        List<LogEntry> result = new ArrayList<>();

        for (GitCommit commit : commiList) {
            String msg = commit.getMessage();

            if (shouldIgnoreCommit(msg)){
                continue;
            } 
            LogEntry entry = parseCommitMessage(msg);
            if(entry != null ){
                result.add(entry);
            }
        }
        return result;
    }

}
