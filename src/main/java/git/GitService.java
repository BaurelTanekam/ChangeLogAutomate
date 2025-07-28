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


    private String getLastTag() {
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
}
