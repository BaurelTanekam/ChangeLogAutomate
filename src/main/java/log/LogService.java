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
    /**
     * Generiert automatisch ein Changelog basierend auf den Commits im Git-Repository.
     *
     * Schritte:
     * Überprüft, ob die aktuelle Umgebung ein gültiges Git-Repository ist.
     * Ruft abhängig vom Parameter `fetchCommitsBeforeTag` entweder:
     *    - Die Commits VOR dem letzten Tag ab, oder
     *    - Die Commits SEIT dem letzten Tag.
     * Wenn keine neuen Commits vorhanden sind, beendet die Methode.
     * Erhöht die Version im Changelog und verarbeitet die Commits zu strukturierten Log-Einträgen (`LogEntry`).
     * Aktualisiert das bestehende Changelog und fügt die neue Version hinzu (mit den neuen Änderungen).
     * Schließt ggf. die HTTP-Verbindung (falls JIRA genutzt wurde).
     * Gibt Erfolgsmeldungen aus oder behandelt Fehler, falls etwas schiefgeht.
     *
     * @param fetchCommitsBeforeTag Wenn `true`, werden Commits vor dem letzten Tag betrachtet. Andernfalls werden Commits seit dem letzten Tag betrachtet.
     */
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

//            JiraService issue = fetcher.fetchIssue("MSPINTERN-2551");
//            if (issue.getComments() != null && !issue.getComments().isEmpty()){
//                logFile.addJiraCommentsToChangeLog(issue.getComments());
//            }

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

    /**
     * Überprüft, ob die aktuelle Umgebung ein gültiges Git-Repository ist.
     *
     * Schritte:
     * Führt den Git-Befehl `git rev-parse --git-dir` aus, der prüft, ob das aktuelle Verzeichnis ein Git-Repository ist.
     * Wenn der Befehl erfolgreich ist (Exit-Code 0), wird eine Meldung über das erkannte Repository ausgegeben.
     * Wenn der Befehl fehlschlägt, wird angegeben, dass kein Git-Repository gefunden wurde.
     * Gibt `true` zurück, wenn sich das Skript in einem Git-Repository befindet, und `false` andernfalls.
     *
     * @return `true`, wenn ein Git-Repository erkannt wird, sonst `false`.
     */
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

    /**
     * Hilfsmethode, um eine Fehlermeldung ins Log (Konsole) zu schreiben.
     *
     * Zusätzlich wird ein Stacktrace ausgegeben, um eine detaillierte Fehlersuche zu ermöglichen.
     *
     * @param msg Die Fehlermeldung, die ins Log geschrieben werden soll.
     * @param e Die verursachende Ausnahme (Exception), die ausgegeben werden soll.
     */
    private void logError(String msg, Exception e){
        System.err.println(msg);
        e.printStackTrace();
    }

    
}
