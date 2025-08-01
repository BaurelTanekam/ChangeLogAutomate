package git;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GitService {
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

    private String stripPrefix(String tag) {
        if (tag.startsWith("v")) {
            return tag.substring(1); // Supprime le préfixe 'v'
        }
        return tag; // Retourne directement le tag s'il n'y a pas de préfixe
    }

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

    // Crée un nouveau tag dans le dépôt
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
