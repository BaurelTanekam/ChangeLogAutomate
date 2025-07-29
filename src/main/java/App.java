import jira.JiraComment;
import jira.JiraException;
import jira.JiraIssueFetcher;
import jira.JiraService;
import log.LogService;
import org.apache.hc.core5.http.ProtocolException;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) {
        JiraIssueFetcher fetcher = new JiraIssueFetcher(Connexion.URL, Connexion.ENCODED);

        try{
            System.out.println("\"=== Issue complète ===\"");
            String issueKey = "MSPINTERN-2551";
            JiraService issue = fetcher.fetchIssue(issueKey);
            System.out.println(issue.toString());
            if (issue.getComments() != null && ! issue.getComments().isEmpty()){
                System.out.println("\n=== Commentaires ====");

                for (JiraComment comment : issue.getComments()){
                    String authorDisplayName = comment.getAuthor();
                    if (authorDisplayName == null || authorDisplayName.trim().isEmpty()){
                        authorDisplayName = "Unknown Author";
                    }

                    String body = comment.getBodyComment();
                    if (body == null || body.isEmpty()){
                        body = "Kein Kommentar";
                    }

                    System.out.println("💬 " + authorDisplayName + ": "
                            + body.substring(0, Math.min(100, body.length()))
                            + "...");
                }
            }else {
                System.out.println("Aucun Commentaire.");
            }
        } catch (JiraException e) {
            System.err.println("Error Jira: " + e.getMessage());
        } catch (IOException | ProtocolException e) {
            System.err.println("Error : " + e.getMessage());
            e.printStackTrace();
        }finally {
            try{
                fetcher.closeHttp();
            }catch (Exception e){
                System.err.println("Error to close." + e.getMessage());
            }
        }
    }
}
