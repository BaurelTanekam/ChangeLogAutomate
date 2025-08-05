package git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GitService {
    /**
     * Holt alle Commits, die seit dem letzten Tag erstellt wurden.
     *
     * Schritte:
     * Überprüft, ob ein Tag existiert:
     *    - Wenn NEIN (kein Tag vorhanden): Ruft alle Commits im Repository ab.
     *    - Wenn JA: Ruft nur die Commits ab, die nach dem letzten Tag erstellt wurden.
     * Führt den Git-Befehl aus und liest die Ausgabe.
     * Erstellt eine Liste von `GitCommit`, die aus SHA und Commit-Nachricht besteht.
     * Gibt die Liste der Commits zurück.
     *
     * @return Liste der GitCommits, die nach dem letzten Tag hinzugefügt wurden.
     * @throws IOException Wenn die Git-Befehle fehlschlagen oder andere E/A-Probleme auftreten.
     */
    public List<GitCommit> getCommitsSinceLastVersion() throws IOException {
        List<GitCommit> commits = new ArrayList<>();
        // last Tag check
        String lastTag = getLastTag();
        
        // git command construct
        ProcessBuilder pb;
        if (lastTag.isEmpty()) {
            pb = new ProcessBuilder("git", "log", "--oneline", "--reverse");
            System.out.println("Processing all commits (no previous tags)");
        } else {
            pb = new ProcessBuilder("git", "log", lastTag + "..HEAD", "--oneline", "--reverse");
            System.out.println("Processing commits since tag: " + lastTag);
        }

        try{
                Process process = pb.start();

            // Vérifier les erreurs du processus
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) 
                    {
                    String errorLine = errorReader.readLine();
                    if (errorLine != null) {
                        System.err.println("Git error: " + errorLine);
                    }
            }

            try(BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            )){
                String line;
                while ((line = reader.readLine()) != null){
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2){
                        commits.add(new GitCommit(parts[0], parts[1]));
                    }
                }
            }

            int exitCode = process.waitFor();
            if(exitCode != 0){
                throw new IOException("Git command failed with exit code: " + exitCode);
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Git command was interrupted", e);
        }

        return commits;
    }

    /**
     * Ruft den neuesten Tag im Repository ab.
     *
     * Schritte:
     * Führt den Git-Befehl `git describe --tags --abbrev=0` aus, der den letzten Tag beschreibt.
     * Falls ein Tag gefunden wird:
     *    - Gibt den Tag zurück.
     * Falls kein Tag gefunden wird:
     *    - Gibt eine leere Zeichenkette zurück und gibt eine entsprechende Nachricht aus.
     *
     * @return Der letzte Tag (z. B. v1.0.0) oder eine leere Zeichenkette, wenn kein Tag vorhanden ist.
     */
    public String getLastTag() {
        try {
            Process process = new ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String tag = reader.readLine();
                int exitCode = process.waitFor();
                
                // Si le processus a échoué (pas de tags), retourner une chaîne vide
                if (exitCode != 0 || tag == null) {
                    System.out.println("No previous tags found, processing all commits");
                    return "";
                }
                
                return tag.trim();
            }
        } catch (Exception e) {
            System.out.println("Could not retrieve tags: " + e.getMessage());
            return "";
        }
        
    }

    /**
     * Ruft die Commits ab, die VOR einem bestimmten Tag im Repository liegen.
     *
     * Schritte:
     * Überprüft, ob der übergebene Tag gültig ist (nicht NULL oder leer).
     * Führt den Git-Befehl `git log <tagName>^..<tagName> --oneline --reverse` aus:
     *    - Dieser Befehl holt die Commits, die direkt vor dem angegebenen Tag liegen, in chronologischer Reihenfolge.
     * Liest die Git-Ausgabe, erstellt für jeden Commit ein Objekt `GitCommit` (bestehend aus SHA und Nachricht).
     * Gibt die Liste der Commits zurück.
     *
     * @param tagName Der Name des Tags, dessen vorherige Commits abgerufen werden sollen.
     * @return Liste der GitCommits, die vor dem angegebenen Tag liegen.
     * @throws IOException Wenn der Git-Befehl fehlschlägt oder der Tag nicht existiert.
     */
    public List<GitCommit> getCommitsBeforeTag(String tagName) throws IOException {
        List<GitCommit> commits = new ArrayList<>();

        //Verify if Tag exists
        if (tagName == null || tagName.trim().isEmpty()){
            throw new IllegalArgumentException("Tag name cannot be null or empty.");
        }

        System.out.println("Fetching commits before tag: " + tagName);
        //Git command
        ProcessBuilder pb = new ProcessBuilder("git", "log", tagName +"^.." + tagName, "--oneline", "--reverse");

        try{
            //Command execute
            Process process = pb.start();

            //check error
            try(BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))){
                String errorLine = errorReader.readLine();
                if (errorLine != null){
                    System.err.println("Git error: " + errorLine);
                    throw new IOException("Git error: " + errorLine);
                }
            }

            //read
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                String line;
                while ((line = reader.readLine()) != null){
                    String[] parts = line.split(" ", 2);
                    if (parts.length == 2){
                        commits.add(new GitCommit(parts[0], parts[1]));
                    }
                }
            }

            //
            int exitCode = process.waitFor();
            if (exitCode != 0){
                throw new IOException("Git command failed with exit code: " + exitCode);
            }

        }catch (InterruptedException e ){
            Thread.currentThread().interrupt();
            throw new IOException("Git command was interrupt", e);
        }

        return commits;
    }

    /**
     * Entfernt den Präfix "v" (falls vorhanden) von einem übergebenen Tag.
     *
     * Schritte:
     * Wenn der Präfix "v" vorhanden ist:
     *    - Entfernt ihn aus der Zeichenkette.
     * Gibt den Tag ohne Präfix zurück.
     *
     * Beispiel:
     *  - Eingabe: "v1.2.3"
     *  - Ausgabe: "1.2.3"
     *
     * @param tag Der Tag-Name, dessen Präfix entfernt werden soll.
     * @return Der Tag ohne "v"-Präfix.
     */
    public static String stripPrefix(String tag) {
        if (tag.startsWith("v")) {
            return tag.substring(1); // Supprime le préfixe 'v'
        }
        return tag; // Retourne directement le tag s'il n'y a pas de préfixe
    }

    /**
     * Erhöht die Build-Nummer eines Versionstags.
     *
     * Schritte:
     * Entfernt den Präfix "v" (falls vorhanden) mit der Methode `stripPrefix`.
     * Teilt den Tag in seine Bestandteile `major.minor.build` auf.
     * Erhöht die `build`-Nummer um 1.
     * Gibt den neu erstellten Versionstag zurück.
     *
     * Beispiel:
     *  - Eingabe: v1.2.3
     *  - Ausgabe: 1.2.4
     *
     * @param version Der aktuelle Versionstag, der inkrementiert werden soll.
     * @return Der neue Tag mit erhöhter Build-Nummer.
     * @throws IllegalArgumentException Wenn der Tag kein gültiges Format hat.
     */
    public String incrementVerionTaginChangeLog(String version){
        String[] parts = stripPrefix(version).split("\\.");

        if (parts.length < 3){
            throw new IllegalArgumentException("Inavlid version format: " + version);
        }

        //
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int build = Integer.parseInt(parts[2]);

        build += 1;

        return major + "." + minor +"." + build;
    }

    /**
     * Erstellt einen neuen Tag im lokalen Repository und pusht ihn in das Remote-Repository.
     *
     * Schritte:
     * Führt den Git-Befehl `git tag <tagName>` aus, um den Tag lokal zu erstellen.
     * Führt den Git-Befehl `git push origin <tagName>` aus, um den erstellten Tag ins Remote-Repository zu pushen.
     * Gibt eine Nachricht aus, die den erfolgreichen Push bestätigt.
     *
     * @param tagName Der Name des neuen Tags, der erstellt und gepusht werden soll.
     * @throws IOException Wenn die Git-Befehle fehlschlagen.
     * @throws InterruptedException Wenn der Prozess unterbrochen wird.
     */
    public void createAndPushTag(String tagName) throws IOException, InterruptedException {
        // tag automatically created
        ProcessBuilder createTag = new ProcessBuilder("git", "tag", tagName);
        Process createProcess = createTag.start();
        createProcess.waitFor();

        // push
        ProcessBuilder pushTag = new ProcessBuilder("git", "push", "origin", tagName);
        Process pushProcess = pushTag.start();
        pushProcess.waitFor();

        System.out.println("Tag created and pushed: " + tagName);
    }
}
