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

    /**
     * Ruft eine JIRA-Issue basierend auf ihrem Issue-Key über die JIRA-API ab.
     *
     * Schritte:
     * Baut die Anfrage-URL für die JIRA-Issue-Details.
     * Sendet die HTTP GET-Anfrage an JIRA.
     * Liest die Antwort und prüft:
     *    - Antwortstatus (z. B. 200 OK).
     *    - Antwortinhalt (ob es sich um gültiges JSON handelt).
     * Wenn die Antwort gültig ist, ruft die `parseJiraIssue`-Methode auf, um die Daten in ein JiraService-Objekt zu konvertieren.
     *
     * @param issueKey Der Schlüssel der JIRA-Issue (z. B. "PROJ-123").
     * @return Ein `JiraService`-Objekt, das die Issue-Details enthält.
     * @throws IOException Wenn ein Problem mit der Verbindung oder Verarbeitung der Antwort auftritt.
     * @throws JiraException Wenn die API einen spezifischen Fehler zurückgibt (z. B. 404 oder 401).
     * @throws ProtocolException Wenn ein Problem mit dem HTTP-Protokoll auftritt.
     */
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

    /**
     * Ruft mehrere JIRA-Issues basierend auf einer Liste von Issue-Keys ab.
     *
     * Schritte:
     * Iteriert durch die Liste der bereitgestellten `issueKeys`.
     * Ruft jede Issue mit der Methode `fetchIssue` ab.
     * Fängt Fehler beim Abruf einzelner Issues ab und fährt mit den verbleibenden Issues fort.
     * Gibt eine Liste aller erfolgreich abgerufenen Issues zurück.
     *
     * @param issueKeys Eine Liste von JIRA-Issue-Keys (z. B. "PROJ-123", "PROJ-456").
     * @return Eine Liste von `JiraService`-Objekten, die die Details zu den Issues enthalten.
     */
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

    /**
     * Analysiert die API-Antwort und konvertiert sie in ein `JiraService`-Objekt.
     *
     * Schritte:
     * Liest die JSON-Daten aus der gegebenen Antwort (responseBody).
     * Überprüft und extrahiert die Felder (z. B. Priority, Status, Assignee).
     * Fügt zugehörige Kommentare hinzu, falls welche vorhanden sind.
     * Gibt das vollständig aufgebaute `JiraService`-Objekt zurück.
     *
     * @param responseBody Die API-Antwort als JSON-String.
     * @param issueKey Der Schlüssel der Issue (z. B. "PROJ-123").
     * @return Ein Objekt `JiraService`, das die Details der Issue abbildet.
     * @throws IOException Wenn ein Fehler beim Analysieren des JSON-Strings auftritt.
     */
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

    /**
     * Extrahiert den Wert eines bestimmten JSON-Felds als String.
     *
     * @param node Das JSON-Node-Objekt, aus dem der Wert extrahiert werden soll.
     * @param fieldName Der Name des Felds im JSON-Objekt.
     * @return Der extrahierte Wert als String oder `null`, falls das Feld fehlt.
     */
    private String getTextValue(JsonNode node, String fieldName) {
        JsonNode field = node.path(fieldName);
        return field.isMissingNode() ? null : field.asText();
    }

    /**
     * Analysiert den "comment"-Node einer API-Antwort und wandelt ihn in eine Liste von `JiraComment`-Objekten um.
     *
     * Schritte:
     * Überprüft, ob der Knoten gültig ist (nicht leer oder NULL).
     * Iteriert über das Array von Kommentaren und:
     *    - Extrahiert Felder wie den Autor, den Erstellungs- und Aktualisierungszeitpunkt und den Inhalt des Kommentars.
     *    - Fügt jedes Kommentar in ein JiraComment-Objekt ein.
     * Gibt alle analysierten Kommentare als Liste zurück.
     *
     * @param commentNode Der JSON-Node, der die Kommentare enthält.
     * @return Eine Liste von JiraComment-Objekten.
     */
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

    /**
     * Konvertiert einen Zeitstempel im ISO8601-Format in ein `LocalDateTime`-Objekt.
     *
     * @param dateString Der Zeitstempel als String.
     * @return Ein `LocalDateTime`-Objekt, das die gegebene Zeit repräsentiert, oder `null`, wenn die Eingabe ungültig ist.
     */
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
    /**
     * Schließt den HTTP-Client, um Ressourcen freizugeben.
     *
     * @throws IOException Wenn ein Fehler beim Schließen des Clients auftritt.
     */
    public void closeHttp() throws IOException{
        if (httpClient != null){
            httpClient.close();
        }
    }

    /**
     * Holt und druckt die Details einer JIRA-Issue auf die Konsole.
     *
     * Schritte:
     * Ruft die Methode `fetchIssue` mit dem angegebenen Issue-Key auf.
     * Gibt die Details der Issue auf die Konsole aus.
     * Handhabt mögliche Fehler, die beim Abrufen der Issue auftreten können.
     *
     * @param issueKey Der Schlüssel der JIRA-Issue (z. B. "PROJ-123").
     */
    public void printIssue(String issueKey) {
        try {
            JiraService issue = fetchIssue(issueKey);
            System.out.println(issue.toString());
        } catch (Exception e) {
            System.err.println("Error: " + issueKey + ": " + e.getMessage());
        }
    }
}
