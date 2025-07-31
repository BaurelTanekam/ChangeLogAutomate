package jira;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JiraIssueFetcher {
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;
    private String jiraBaseUrl;
    private String authHeader;

    //Fields to receive

    private final String FIELDS = "priority,issuetype,status,summary,created,updated,assignee,reporter,comment";

    public JiraIssueFetcher(String jiraBaseUrl, String baseToken){
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.jiraBaseUrl = jiraBaseUrl.endsWith("/") ? jiraBaseUrl : jiraBaseUrl + "/";
        this.authHeader = baseToken;
    }

    public JiraService fetchIssue(String issueKey) throws IOException, JiraException, ProtocolException {
        String url = jiraBaseUrl + "rest/api/latest/issue/" + issueKey + "?fields=" + FIELDS;

        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "application/json");
        request.setHeader("Authorization",  "Bearer "+ authHeader);

        HttpClientResponseHandler<JiraService> responseHandler = new HttpClientResponseHandler<JiraService>() {
            public JiraService handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();
                String responseBody = entity != null ? EntityUtils.toString(entity) : "";


                // Check if response is empty or not JSON
                if (responseBody.trim().isEmpty()) {
                    throw new RuntimeException(new JiraException("Server returned empty response"));
                }

                // Check if response is HTML (common error indicator)
                if (responseBody.trim().startsWith("<")) {
                    throw new RuntimeException(new JiraException("Server returned HTML instead of JSON. This usually indicates authentication issues or wrong URL. Response: " + responseBody.substring(0, Math.min(200, responseBody.length()))));
                }

                // Check if response looks like JSON
                if (!responseBody.trim().startsWith("{") && !responseBody.trim().startsWith("[")) {
                    throw new RuntimeException(new JiraException("Server returned non-JSON response: " + responseBody.substring(0, Math.min(200, responseBody.length()))));
                }

                if(statusCode == HttpStatus.SC_OK){

                    return  parseJiraIssue(responseBody, issueKey);

                }else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    try {
                        throw new JiraException("Issue: " + issueKey + " not founded.");
                    } catch (JiraException e) {
                        throw new RuntimeException(e);
                    }
                }else if(statusCode == HttpStatus.SC_UNAUTHORIZED){
                    try {
                        throw new JiraException("Failed Authentification. Please verify your Credentials.");
                    } catch (JiraException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    try {
                        throw new JiraException("Jira Error: " + statusCode + " - " + responseBody);
                    } catch (JiraException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        try {
            return httpClient.execute(request, responseHandler);
        } catch(IOException e){
            throw new IOException("Error executing request: " + e.getMessage(), e);
        }
    }

    public List<JiraService> fetchMultplesIssues(List<String> issueKeys){
        //Store issue
        List<JiraService> issues = new ArrayList<>();

        for (String issueKey : issueKeys){
            try{
                JiraService issue = fetchIssue(issueKey);

                //Add issue in the list
                issues.add(issue);
            }catch (JiraException e){
                System.err.println("Error for the issue " + issueKey + ": " + e.getMessage());
            } catch (IOException | ProtocolException e) {
                System.err.println("Connection Error for this issue: " + issueKey + e.getMessage());
            }catch (Exception e){
                System.err.println("Unexpected error for this issue: " + issueKey + e.getMessage());
            }
        }
        //return
        return issues;
    }

    private JiraService parseJiraIssue(String responseBody, String issueKey) throws IOException {

        try{
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode fields = root.path("fields");

                JiraService issue = new JiraService();
                issue.setKey(issueKey);

                // Fixed field access - remove the "name: " prefix and access correctly
                JsonNode priority = fields.path("priority");
                if (!priority.isMissingNode() && !priority.isNull()){
                    issue.setPriority(priority.path("name").asText());
                }

                JsonNode issueType = fields.path("issuetype"); // Fixed: was "issueType"
                if (!issueType.isMissingNode() && !issueType.isNull()){
                    issue.setIssueType(issueType.path("name").asText());
                }

                JsonNode status = fields.path("status");
                if (!status.isMissingNode() && !status.isNull()){
                    issue.setStatus(status.path("name").asText());
                }

                JsonNode assignee = fields.path("assignee"); // Fixed: was "assigned"
                if (!assignee.isMissingNode() && !assignee.isNull()) {
                    issue.setAssigned(assignee.path("displayName").asText());
                }

                JsonNode reporter = fields.path("reporter");
                if (!reporter.isMissingNode() && !reporter.isNull()) {
                    issue.setReporter(reporter.path("displayName").asText());
                }

                // Fixed: access summary field
                JsonNode summary = fields.path("summary");
                if (!summary.isMissingNode() && !summary.isNull()) {
                    issue.setTitle(summary.asText());
                }

                JsonNode commentNode = fields.path("comment");
                if (!commentNode.isMissingNode() && !commentNode.isNull()){
                    issue.setComments(parseComments(commentNode));
                }

            return issue;
        }catch(JsonParseException e){
            System.err.println("JSON Parse Error. Response body was:");
            System.err.println("'" + responseBody + "'");
            System.err.println("Response length: " + responseBody.length());
            System.err.println("First 100 characters: '" + responseBody.substring(0, Math.min(100, responseBody.length())) + "'");
            throw new IOException("Failed to parse JSON response: " + e.getMessage() + ". Response: " + responseBody.substring(0, Math.min(200, responseBody.length())), e);
        }

    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() ? null : field.asText();
    }

    private List<JiraComment> parseComments(JsonNode commentNode) {
    List<JiraComment> comments = new ArrayList<>();

    if (!commentNode.isMissingNode() && !commentNode.isNull()){
        JsonNode commentsArray = commentNode.path("comments");
        if (commentsArray.isArray()){
            for (JsonNode commentJson : commentsArray){
                JiraComment comment = new JiraComment();
                comment.setId(commentJson.path("id").asText());

                // Handle body - it might be a string or complex object
                JsonNode bodyNode = commentJson.path("body");
                if (bodyNode.isTextual()) {
                    comment.setBodyComment(bodyNode.asText());
                } else if (bodyNode.isObject()) {
                    // For newer JIRA versions, body might be in ADF format
                    comment.setBodyComment(bodyNode.toString()); // Or parse ADF if needed
                }

                JsonNode author = commentJson.path("author");
                if (!author.isMissingNode() && !author.isNull()) {
                    comment.setAuthor(author.path("displayName").asText());
                }
                comment.setCreatedComment(parseDateTime(getTextValue(commentJson, "created")));
                comment.setUpdatedComment(parseDateTime(getTextValue(commentJson, "updated")));
                comments.add(comment);
            }
        }
    }
    return comments;
}

    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.isEmpty()){
            return null;
        }
        try{
            return LocalDateTime.parse(dateString.substring(0,19), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }
    //TODO Immer die Http Verbindung zumachen
    public void closeHttp() throws IOException{
        if (httpClient != null){
            httpClient.close();
        }
    }

    public void printIssue(String issueKey) {
        try {
            JiraService issue = fetchIssue(issueKey);
            System.out.println(issue.toString());
        } catch (Exception e) {
            System.err.println("Error: " + issueKey + ": " + e.getMessage());
        }
    }
}
