import credentials.Connexion;
import credentials.Ressources;
import git.GitCommitFromRepo;
import git.GitService;
import gpt.GptService;
import jira.JiraException;
import jira.JiraIssueFetcher;
import jira.JiraService;
import log.LogFile;
import log.LogService;
import org.apache.hc.core5.http.ProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        JiraIssueFetcher fetcher = new JiraIssueFetcher(Connexion.URL, Connexion.ENCODED);
        List<String> issues = new ArrayList<>();
        issues.add("NWTDL-571");
        issues.add("NWTDL-573");
        issues.add("NWTDL-567");
        issues.add("NWTDL-566");
        issues.add("NWTDL-559");
        issues.add("NWTDL-560");
        issues.add("NWTDL-556");
        issues.add("NWTDL-559");
        issues.add("NWTDL-560");
        issues.add("NWTDL-556");
        issues.add("NWTDL-554");
        issues.add("NWTDL-553");
        issues.add("NWTDL-551");
        issues.add("NWTDL-549");
        issues.add("NWTDL-546");
        issues.add("NWTDL-545");
        issues.add("NWTDL-542");
        issues.add("NWTDL-544");
        issues.add("NWTDL-527");
        issues.add("NWTDL-539");
        issues.add("NWTDL-537");
        issues.add("NWTDL-468");

//        try{
//            System.out.println("\"=== Issue complète ===\"");
//            //String issueKey = "NWTDL-517";
//            List<JiraService> issueList = fetcher.fetchMultplesIssues(issues);
//            LogFile logFile = new LogFile();
//            for (JiraService issue : issueList){
//                logFile.addJiraCommentsToChangeLog(issue.getComments());
//            }
//            //logFile.addJiraCommentsToChangeLog();
//            //System.out.println(issueList.toString());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } finally {
//            try{
//                fetcher.closeHttp();
//            }catch (Exception e){
//                System.err.println("Error to close." + e.getMessage());
//            }
//        }
//        LogService logService = new LogService();
//        logService.generateChangelog(true);
//        GptService gptService = new GptService("src/main/java");
//        try {
//            gptService.processChangeLog("src/main/java/CHANGELOG.md");
//        } catch (GptService.ChatGPTException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        GitCommitFromRepo gitCommitFromRepo = new GitCommitFromRepo(Ressources.NAME_REPO, Ressources.OWNER_REPO);
        try {
            gitCommitFromRepo.testConnection();
            //gitCommitFromRepo.generateChangeLogForDistantRepo(gitCommitFromRepo.fetchAllCommits(), "src/main/java/CHANGELOG_FILE.md");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
