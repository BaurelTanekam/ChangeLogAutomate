package git;

import java.util.Objects;

public class GitCommit {
    private String hash;
    private String message;

    public GitCommit(String hash, String message) {
        this.hash = hash;
        this.message = message;
    }

    public String getHash() {
        return hash;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return hash + " " + message;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        GitCommit gitCommit = (GitCommit) object;
        return Objects.equals(hash, gitCommit.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
