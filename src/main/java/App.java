import jira.JiraException;
import jira.JiraIssueFetcher;
import jira.JiraService;
import org.apache.hc.core5.http.ProtocolException;

import java.io.IOException;

public class App {
    public static void main(String[] args) {
        JiraIssueFetcher fetcher = new JiraIssueFetcher(Connexion.URL, Connexion.ENCODED);

        try{
            System.out.println("\"=== Issue complète ===\"");
            String issueKey = "MSPINTERN-2551";
            JiraService issue = fetcher.fetchIssue(issueKey);
            System.out.println(issue.toString());
            issue.printComment();
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
