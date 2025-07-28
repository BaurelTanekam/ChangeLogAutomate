package jira;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JiraIssueFetcher {
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;
    private String jiraBaseUrl;
    private String authHeader;

    //Fields to receive

    private final String FIELDS = "priority,issueType,status,title,created,updated,assigned,reporter";

    public JiraIssueFetcher(String jiraBaseUrl, String baseToken){
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        this.jiraBaseUrl = jiraBaseUrl.endsWith("/") ? jiraBaseUrl : jiraBaseUrl + "/";
        this.authHeader = baseToken;
    }

    public JiraService fetchIssue(String issueKey) throws IOException, JiraException{
        String url = jiraBaseUrl + "rest/api/2/issue" + issueKey + "?fields=" + FIELDS;

        HttpGet request = new HttpGet(url);
        request.setHeader("Authorization", authHeader);
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");

        HttpClientResponseHandler<JiraService> responseHandler = new HttpClientResponseHandler<JiraService>() {
            public JiraService handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
                int statusCode = response.getCode();
                HttpEntity entity = response.getEntity();
                String responseBody = entity != null ? EntityUtils.toString(entity) : "";

                if(statusCode == HttpStatus.SC_OK){

                    return parseJiraIssue(responseBody, issueKey);

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
            throw new IOException("Error: " + e.getMessage(), e);
        }
    }

    private JiraService parseJiraIssue(String responseBody, String issueKey) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode fields = root.path("fields");

        JiraService issue = new JiraService();
        issue.setKey(issueKey);

        JsonNode priority = fields.path("priority");
        if (!priority.isMissingNode()){
            issue.setPriority(getTextValue(priority, "name: "));
        }

        JsonNode issueType = fields.path("issueType");
        if (!issueType.isMissingNode()){
            issue.setIssueType(getTextValue(issueType, "name: "));
        }

        JsonNode status = fields.path("status");
        if (!status.isMissingNode()){
            issue.setStatus(getTextValue(status, "name: "));
        }

        JsonNode assigned = fields.path("assigned");
        if (!assigned.isMissingNode()) {
            issue.setAssigned(getTextValue(assigned, "Assigned: "));
        }

        JsonNode reporter = fields.path("reporter");
        if (!reporter.isMissingNode()) {
            issue.setReporter(getTextValue(reporter, "Reporter: "));
        }

        JsonNode commentNode = fields.path("comment: ");
        if (!commentNode.isMissingNode()){
            issue.setComments(parseComments(commentNode));
        }
        return issue;
    }

    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() ? null : field.asText();
    }

    private List<JiraComment> parseComments(JsonNode commentNode) {
        List<JiraComment> comments = new ArrayList<>();

        if (!commentNode.isMissingNode()){
            JsonNode commentsArray = commentNode.path("comments");
            if (commentsArray.isArray()){
                for (JsonNode commentJson : commentsArray){
                    JiraComment comment = new JiraComment();
                    comment.setId(getTextValue(commentJson, "id"));
                    comment.setBodyComment(getTextValue(commentJson, "body"));

                    JsonNode author = commentJson.path("author");
                    if (!author.isMissingNode()) {
                        comment.setAuthor(getTextValue(author, "name"));
                    }

                    comment.setCreatedComment(comment.getCreatedComment());
                    comment.setUpdatedComment(comment.getUpdatedComment());

                    comments.add(comment);
                }
            }
        }
        return comments;
    }

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
