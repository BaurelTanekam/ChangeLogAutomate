package git;

public class TagInfo {
    private String sha;
    private String date;

    public TagInfo(String sha, String date) {
        this.sha = sha;
        this.date = date;
    }

    public String getSha() {
        return sha;
    }

    public String getDate() {
        return date;
    }
}
