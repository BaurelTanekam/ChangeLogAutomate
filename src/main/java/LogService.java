import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    public void generateChangelog(){
        try {

            if(!isGitRepository()){
                System.err.println("Error: Not in a Git repository.");
                System.exit(1);
            }

            System.out.println("Starting automatic chnagelog genration...");

            List<GitCommit> commits = gitService.getCommitsSinceLastVersion();
            if(commits.isEmpty()){
                System.out.println("No new commits found. Changelog is up to date.");
                return;
            }

            log("Found " + commits.size() + " commits to process.");

            List<LogEntry> entries = commitMessageParser.parseCommits(commits);
            log("Generated " + entries.size() + " changedlog entries");
            System.out.println("\n Preview of changes:");
            entries.forEach(entry -> 
                System.out.println("  " + entry.getCategory() + ": " + entry.getDescription())
            );

            logFile.updateChangeLog(entries);
            log("changelog updated succesfully.");
        } catch (Exception e) {
            logError("Error during changelog generation: " + e.getMessage(), e);
        }
    }

    private boolean isGitRepository(){
        try {
            ProcessBuilder pb = new ProcessBuilder("git", "rev-parse", "--git-dir");
            Process process = pb.start();

            int exitCode = process.waitFor();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            if (exitCode == 0) {
                System.out.println("Git repository detected: " + output);
            } else {
                System.out.println("Not a Git repository.");
            }

            return exitCode == 0;  // Retourne vrai si la commande réussit
        } catch (Exception e) {
            System.err.println("Error determining Git repository: " + e.getMessage());
            return false;  // Si une exception est levée, ce n'est pas un dépôt Git
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
