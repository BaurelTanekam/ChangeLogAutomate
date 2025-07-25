import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GitService {
    public List<GitCommit> getCommitsSinceLastVersion() throws IOException {
        List<GitCommit> commits = new ArrayList<>();
        String lastTag = getLastTag();

        String gitCommand = lastTag.isEmpty()
                ? "git log -- online --reverse"
                : "git log " + lastTag + "..HEAD --online --reverse";

        Process process = new ProcessBuilder(gitCommand.split(" ")).start();

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
        } catch (Exception e) {
            throw new IOException("Failed to fetch commits from Git.");
        }

        return commits;
    }


    private String getLastTag() {
        try {
            Process process = new ProcessBuilder("git", "describe", "--tags", "--abbrev=0").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.readLine().trim();
            }
        } catch (IOException e) {
            System.out.println("No tags found in the repository.");
        }
        return "";
    }
}
