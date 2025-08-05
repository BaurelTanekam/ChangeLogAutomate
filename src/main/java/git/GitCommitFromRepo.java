package git;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import credentials.Connexion;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitCommitFromRepo {
    private static final String API_URL = "https://api.github.com/repos/";
    private final String OWNER;
    private final String REPO;
    private final String authToken = Connexion.AUTH_FOR_REPO;


    public GitCommitFromRepo(String REPO, String OWNER){
        this.OWNER = OWNER;
        this.REPO = REPO;
    }

    public List<GitCommit> fetchAllCommits() throws IOException{
        List<GitCommit> commits = new ArrayList<>();
        String url = API_URL + OWNER + "/" + REPO + "/commits";
        System.out.println(url);

        while(url != null){
            PaginatedResponse response = fetchCommitsFromUrl(url);
            JSONArray commitArray = response.getData();

            for (int i = 0; i < commitArray.length(); i++) {
                JSONObject commitObj = commitArray.getJSONObject(i);
                String sha = commitObj.getString("sha");
                String message = commitObj.getJSONObject("commit").getString("message");

                commits.add(new GitCommit(sha, message));
            }
            url = response.getNextUrl();
        }
        return commits;
    }

    public List<GitCommit> fetchLimitedCommits(int limit) throws IOException {
        if (limit < 0){
            throw new IllegalArgumentException("The limit must be greater than 0.");
        }

        List<GitCommit> commits = new ArrayList<>();
        String url = API_URL +  OWNER + "/" + REPO + "/commits?per_page=100";
        int commitCount = 0;

        while (url != null && commitCount < limit){
            PaginatedResponse response = fetchCommitsFromUrl(url);
            JSONArray commitArray = response.getData();

            for (int i = 0; i < commitArray.length() && commitCount < limit; i++) {
                JSONObject commitObj = commitArray.getJSONObject(i);
                String sha = commitObj.getString("sha");
                String message = commitObj.getJSONObject("commit").getString("message");
                commits.add(new GitCommit(sha, message));
                commitCount++;
            }
            url = (commitCount < limit) ? response.getNextUrl() : null;
        }
        System.out.printf("Total commits fetched: %d(requested: %d)%n", commitCount, limit);
        return commits;
    }

    public List<GitCommit> fetchCommitsByTicket(List<GitCommit> commits, String ticketNummer){
        if (ticketNummer == null || ticketNummer.trim().isEmpty()){
            throw new IllegalArgumentException("Ticket number cannot be null or empty.");
        }

        String formattedTicket = ticketNummer.startsWith("[") && ticketNummer.endsWith("]") ? ticketNummer
                : "["+ ticketNummer + "]";
        List<GitCommit> filteredCommits = new ArrayList<>();
        for (GitCommit commit : commits){
            if (commit.getMessage().contains(formattedTicket)) filteredCommits.add(commit);
        }
        System.out.println("Filtered " + filteredCommits.size() + " commits containing ticket: " + formattedTicket);
        return filteredCommits;
    }

    public List<GitCommit> fetchCommitsByTicketList(List<GitCommit> commits, List<String> ticketsList){
        if (ticketsList == null || ticketsList.isEmpty()){
            System.out.println("empty or null ticket list provided.");
            return new ArrayList<>();
        }
        List<GitCommit> filteredCommits = new ArrayList<>();
        for (String ticket : ticketsList) {
            if (ticket != null && !ticket.trim().isEmpty()){
                List<GitCommit> commitsByTicket = fetchCommitsByTicket(commits, ticket);

                filteredCommits.addAll(commitsByTicket);
            }
        }
        return filteredCommits;
    }

    public List<GitCommit> getCommitsBeforeTag(String tagName) throws IOException {
        if (tagName == null || tagName.trim().isEmpty()){
            throw new IllegalArgumentException("TagName cannot be null.");
        }
        System.out.println("Commits before tag: " + tagName);

        TagInfo tagInfo = getTagInfo(tagName);
        if(tagInfo == null){
            throw new IOException("Tag " + tagName + "is not found");
        }
        List<GitCommit> allCommits = fetchCommitsUntilDate(tagInfo.getDate());

        //Filtered Tag until the date of tag
        List<GitCommit> commitsBeforetag = new ArrayList<>();
        for (GitCommit commit : allCommits){
            if (!commit.getHash().startsWith(tagInfo.getSha().substring(0, 7))){
                commitsBeforetag.add(commit);
            }
        }
        System.out.println("Number of commits before the tag: "+ commitsBeforetag.size());
        return commitsBeforetag;
    }

    private TagInfo getTagInfo(String tagName) {
        String tagRefUrl = API_URL + OWNER + "/" + REPO + "/git/refs/tags/" + tagName;

        try{
            JSONObject tagRef = fetchJsonObjectFromUrl(tagRefUrl);
            String sha = tagRef.getJSONObject("object").getString("sha");

            //get the date
            String commitUrl = API_URL + OWNER + "/" + REPO + "/git/commits/" + sha;
            JSONObject commitDetails = fetchJsonObjectFromUrl(commitUrl);
            String date = commitDetails.getJSONObject("author").getString("date");

            return new TagInfo(sha, date);
        }catch(IOException e){
            System.out.println("Tag "+ tagName + " not found" + e.getMessage());
            return null;
        }
    }

    public void generateChangeLogForDistantRepo(List<GitCommit> commits, String filePath) throws IOException {
        if (commits.isEmpty()){
            System.out.println("No Commit");
            return;
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))){
            writer.write("\n## Version ");
            writer.write(" - " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            writer.write("\n\n");

            List<GitCommit> features = new ArrayList<>();
            List<GitCommit> fixes = new ArrayList<>();
            List<GitCommit> others = new ArrayList<>();

            for (GitCommit commit : commits){
                String message = commit.getMessage().toLowerCase();
                if (message.contains("feat") || message.contains("feature") || message.contains("add")){
                    features.add(commit);
                } else if (message.contains("fix")||message.contains("bug")||message.contains("error")) {
                    fixes.add(commit);
                }else {
                    others.add(commit);
                }
            }

            //New Fontionnality
            if (!features.isEmpty()){
                writer.write("### New Fontionality\n");
                for (GitCommit commit : fixes){
                    writer.write(String.format("- %s", cleanCommitMessage(commit.getMessage())));
                }
                writer.write("\n");
            }
            writer.write("\n");

            //write correction
            if (!fixes.isEmpty()){
                writer.write("### Bug Correction");
                for (GitCommit commit : fixes){
                    writer.write(String.format("- %s", cleanCommitMessage(commit.getMessage())));
                    writer.write("\n");
                }
                writer.write("\n");
            }

            //Others changements
            if (!others.isEmpty()){
                writer.write("### Others changements");
                for (GitCommit commit : others){
                    writer.write(String.format("- %s", cleanCommitMessage(commit.getMessage())));
                    writer.write("\n");
                }
                writer.write("\n");
            }
            writer.write("---\n");
            System.out.println("Changelog generated in: " + filePath);
        }
    }

    public void generateChangeLogBeforeTag(String tagName, String filePath) throws IOException {
        List<GitCommit> commits = getCommitsBeforeTag(tagName);
        generateChangeLogForDistantRepo(commits, filePath);
    }

    public void generateChangeLogSinceLastTag(String filePath) throws IOException {
        String lastTag = getLastTag();
        List<GitCommit> commits;

        if (lastTag.isEmpty()){
            commits = fetchAllCommits();
        }else {
            commits = getCommitsSincetag(lastTag);
        }
        generateChangeLogForDistantRepo(commits, filePath);
    }

    private String getLastTag(){
        try{
            String url = API_URL + OWNER + "/" + REPO + "/tags";
            JSONArray tags = fetchJsonArrayFromUrl(url);

            if (tags.length() > 0){
                return tags.getJSONObject(0).getString("name");
            }
        }catch(IOException e){
            System.out.println("Impossible to found the tags: "+ e.getMessage());
        }
        return "";
    }

    private List<GitCommit> getCommitsSincetag(String tagName) throws IOException {
        TagInfo tagInfo = getTagInfo(tagName);
        if (tagInfo == null){
            throw  new IOException("Tag "+ tagName + " not found.");
        }
        List<GitCommit> commits = new ArrayList<>();
        String url = API_URL + OWNER + "/" + REPO + "/commits?since=" + tagInfo.getDate() + "&per_page=100";

        while (url!=null){
            PaginatedResponse response = fetchCommitsFromUrl(url);
            JSONArray commitArray = response.getData();

            for (int i = 0; i < commitArray.length(); i++) {
                JSONObject commitObj = commitArray.getJSONObject(i);
                String sha = commitObj.getString("sha");

                if (!sha.startsWith(tagInfo.getSha().substring(0, 7))){
                    String message = commitObj.getJSONObject("commit").getString("message");
                    commits.add(new GitCommit(sha, message));
                }
            }
            url = response.getNextUrl();
        }
        return commits;
    }

    private String cleanCommitMessage(String message){
        if (message == null) return "";
        return message.split("\n")[0].trim();
    }

    private List<GitCommit> fetchCommitsUntilDate(String untilDate) throws IOException {
        List<GitCommit> commits = new ArrayList<>();
        String url = API_URL + OWNER + "/" + REPO + "/commits?until=" + untilDate + "&per_page=100";
        while(url!=null){
            PaginatedResponse response = fetchCommitsFromUrl(url);
            JSONArray commitArray = response.getData();

            for (int i = 0; i < commitArray.length(); i++) {
                JSONObject commitObj = commitArray.getJSONObject(i);
                String sha = commitObj.getString("sha");
                String message = commitObj.getJSONObject("commit").getString("message");

                commits.add(new GitCommit(sha, message));
            }
            url = response.getNextUrl();
        }
        return  commits;
    }

    private JSONObject fetchJsonObjectFromUrl(String url) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try{
            conn = (HttpURLConnection) new URL(url).openConnection();
            setUpConnection(conn);
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK){
                throw new IOException("HTTP error code: " + responseCode + " - " + conn.getResponseMessage());
            }

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonStr = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null){
                jsonStr.append(line);
            }
            return new JSONObject(jsonStr.toString());
        }finally {
            if (reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (conn!=null){
                conn.disconnect();
            }
        }
    }

    public String getCommitShaForTag(String tag) throws IOException {
        String url = API_URL + OWNER + "/" + REPO + "/tags";
        JSONArray tags = fetchJsonArrayFromUrl(url);

        for(int i = 0; i < tags.length(); i++){
            JSONObject tagObj = tags.getJSONObject(i);
            if (tag.equalsIgnoreCase(tagObj.getString("name"))){
                return tagObj.getJSONObject("commit").getString("sha");
            }
        }
        throw  new IllegalArgumentException("Tag not found: " + tag);
    }

    public List<GitCommit> filterCommitsBeforeSha(List<GitCommit> commits, String sha){
        List<GitCommit> filtered = new ArrayList<>();
        for (GitCommit commit : commits){
            if (commit.getHash().equalsIgnoreCase(sha)) break;
            filtered.add(commit);
        }
        return filtered;
    }

    public List<GitCommit> filterCommitAfterSha(List<GitCommit> commits, String sha){
        List<GitCommit> filtered = new ArrayList<>();
        for (GitCommit commit : commits){
            if (commit.getHash().equalsIgnoreCase(sha)) break;
            filtered.add(commit);
        }
        return filtered;
    }

    public List<GitCommit> filterCommitsAfterSha(List<GitCommit> commits, String sha){
        List<GitCommit> filtered = new ArrayList<>();
        boolean found = false;
        for (GitCommit commit : commits){
            if (found){
                filtered.add(commit);
            }
            if (commit.getHash().equalsIgnoreCase(sha)){
                found = true;
            }
        }
            return filtered;
    }

    public void writeCommitsToFile(List<GitCommit> commits, String filePath) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
            for (GitCommit commit : commits){
                writer.write(commit.toString());
                writer.newLine();
            }
        }
    }

    private JSONArray fetchJsonArrayFromUrl(String url) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try{
            conn = (HttpURLConnection) new URL(url).openConnection();
            setUpConnection(conn);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK){
                throw  new IOException("HTTP error code: " + responseCode + " - " + conn.getResponseMessage());
            }
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonStr = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                jsonStr.append(line);
            }
            return new JSONArray(jsonStr.toString());
        }finally {
            if (reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (conn != null) conn.disconnect();
        }
    }

    private void setUpConnection(HttpURLConnection conn) {
        if (authToken != null && !authToken.isEmpty()){
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        //conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setRequestProperty("User-Agent", "baurel.tanekam@medien-systempartner");
    }

    private PaginatedResponse fetchCommitsFromUrl(String url) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader reader = null;

        try{
            conn= (HttpURLConnection) new URL(url).openConnection();
            setUpConnection(conn);

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK){
                throw new IOException("HTTP error code: " + responseCode + " - " + conn.getResponseMessage());
            }
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonStr = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null) jsonStr.append(line);

            JSONArray data = new JSONArray(jsonStr.toString());
            String nextUrl = parseNextPage(conn.getHeaderField("Link"));

            return new PaginatedResponse(data, nextUrl);
        }finally {
            if (reader != null){
                try{
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (conn != null) conn.disconnect();

    }
}

    private String parseNextPage(String link) {
        if (link == null){
            return null;
        }
        String[] parts = link.split(",");
        for (String part : parts){
            part = part.trim();
            if (part.contains("rel=\"next")){
                int start = part.indexOf("<") + 1;
                int end = part.indexOf(">");
                if (start > 0 && end > start){
                    return part.substring(start, end);
                }
            }
        }
        return null;
    }

    public void testConnection() throws IOException {
        System.out.println("Try Connection to GitHub-API ...");
        System.out.println("Repository: " + OWNER + "/" + REPO);

        String testUrl = API_URL + OWNER + "/" + REPO;
        System.out.println(testUrl);
        HttpURLConnection conn = null;
        try{
            conn = (HttpURLConnection) new URL(testUrl).openConnection();
            setUpConnection(conn);

            int responseCode = conn.getResponseCode();
            System.out.println("ResponseCode: " + responseCode);

            switch (responseCode){
                case 200:
                    System.err.println("Successfully Connection.");
                    break;
                case 401:
                    System.err.println("Invalid API Key.");
                    break;
                case 403:
                    System.err.println("Forbidden Access.");
                    System.err.println("Verify your rights.");
                    break;
                case 404:
                    System.err.println("Repository not found.");
                    System.err.println("Owner's name: " + OWNER);
                    System.err.println("Repository's name: " + REPO);
                    break;
                case 500:
                    System.err.println("Please try again later.");
                    break;
                case 429:
                    System.err.println("Rate limit exceded. Please try again later."); break;
                default:
                    throw new IOException();
            }
        }finally {
            if (conn!=null) conn.disconnect();
        }
    }

}
