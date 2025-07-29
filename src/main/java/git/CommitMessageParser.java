package git;

import log.LogEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommitMessageParser {
    private final Pattern FEAT_PATTERN = Pattern.compile("(?i).*((add|feat|feature|new|implement).*)", Pattern.CASE_INSENSITIVE);
    private final Pattern FIX_PATTERN = Pattern.compile("(?i).*((fix|bug|correct|resolve|patch).*)", Pattern.CASE_INSENSITIVE);
    private final Pattern DOCS_PATTERN = Pattern.compile("(?i).*((doc|readme|comment).*)", Pattern.CASE_INSENSITIVE);
    private final Pattern REFRACTOR_PATTERN = Pattern.compile("(?i).*((refactor|clean|improve|optimize).*)", Pattern.CASE_INSENSITIVE);

    private final Pattern CONVENTION_PATTERN = Pattern.compile("^(feat|fix|docs|style|refactor|test|chore)(\\(.+\\))?!?:\\s*(.+)$");

    /**
     * @param commitMessage
     * @return
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
     * @param message
     * @return
     */
    private String cleanupMessage(String message) {
        // Nettoyer le message (supprimer les prefixes comme "fix:", etc.)
        return message.replaceAll("^(feat|fix|add|implement|refactor|update|docs?):\\s*", "")
                .replaceAll("^(Added|Fixed|Updated|Implemented)\\s+", "")
                .trim();
    }

    /**
     * @param type
     * @return
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
     * @param message
     * @return
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
     * @param commitMessage
     * @return
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
     * @param commiList
     * @return
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
