import java.util.List;

public class LogService {
    private GitService gitService;
    private CommitMessageParser commitMessageParser;
    private LogFile logFile;

    public LogService() {
        this.gitService = new GitService();
        this.commitMessageParser = new CommitMessageParser();
        this.logFile = new LogFile();
    }

    public void generateChangelol(){
        try {
            List<GitCommit> commits = gitService.getCommitsSinceLastVersion();
            log("Found " + commits.size() + " commits to process.");

            List<LogEntry> entries = commitMessageParser.parseCommits(commits);
            log("Generated " + entries.size() + " changedlog entries");

            logFile.updateChangeLog(entries);
            log("changelog updated succesfully.");
        } catch (Exception e) {
            logError("Error during changelog generation: " + e.getMessage(), e);
        }
    }

    private void log(String string) {
        System.out.println(string);
    }

    private void logError(String msg, Exception e){
        System.err.println(msg);
        e.printStackTrace();
    }

    
}
