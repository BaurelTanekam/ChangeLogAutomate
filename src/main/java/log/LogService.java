package log;

import credentials.Connexion;
import git.CommitMessageParser;
import git.GitCommit;
import git.GitService;
import jira.JiraIssueFetcher;
import jira.JiraService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class LogService {
    private GitService gitService;
    private CommitMessageParser commitMessageParser;
    private LogFile logFile;
    JiraIssueFetcher fetcher = new JiraIssueFetcher(Connexion.URL, Connexion.ENCODED);


    public LogService() {
        this.gitService = new GitService();
        this.commitMessageParser = new CommitMessageParser();
        this.logFile = new LogFile();
    }

    //fetchCommitsBeforeTag = true || fetchCommitsBeforetag = false
    public void generateChangelog(boolean fetchCommitsBeforeTag){
        try {

            if(!isGitRepository()){
                System.err.println("Error: Not in a Git repository.");
                System.exit(1);
            }

            // Étape 1 : Récupérer les commits depuis le dernier tag
            System.out.println("Starting automatic changelog generation...");

            //lastag
            String lastTag = gitService.getLastTag();

            //Commits
            List<GitCommit> commits;
            if (fetchCommitsBeforeTag){
                System.out.println("Fetching commits before tag: " + lastTag);
                commits = gitService.getCommitsBeforeTag(lastTag);
            }else {
                System.out.println("Fetching commits since last tag: " + lastTag);
                commits = gitService.getCommitsSinceLastVersion();
            }

            if(commits.isEmpty()){
                System.out.println("No new commits found. Changelog is up to date.");
                return;
            }

            JiraService issue = fetcher.fetchIssue("MSPINTERN-2551");
            if (issue.getComments() != null && !issue.getComments().isEmpty()){
                logFile.addJiraCommentsToChangeLog(issue.getComments());
            }

            //List<GitCommit> commits = gitService.getCommitsSinceLastVersion();

            log("Found " + commits.size() + " commits to process.");

            //Tag increment and push
            String newVersion = gitService.incrementVerionTaginChangeLog(gitService.getLastTag());
            System.out.println("New Version: "+ newVersion);

            List<LogEntry> entries = commitMessageParser.parseCommits(commits);
            log("Generated " + entries.size() + " changedlog entries");
            System.out.println("\n Preview of changes:");
            entries.forEach(entry -> 
                System.out.println("  " + entry.getCategory() + ": " + entry.getDescription())
            );

            logFile.updateChangeLog(entries);
            logFile.addReleaseVersionToChangeLog(newVersion, entries);

            fetcher.closeHttp();
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
