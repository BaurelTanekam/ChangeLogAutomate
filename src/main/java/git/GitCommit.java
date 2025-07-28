package git;

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
}
