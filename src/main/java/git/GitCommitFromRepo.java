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

    /**
     * Holt alle Commits aus einem GitHub-Repository mit Hilfe der API.
     *
     * Schritte:
     * Ruft die Commits von der API ab, beginnend mit der ersten Seite.
     * Iteriert über die Ergebnisse von paginierten Antworten.
     * Fügt die Commit-SHA und Nachrichten zu einer Liste von `GitCommit` hinzu.
     * Gibt die endgültige Liste aller Commits zurück.
     *
     * @return Eine Liste aller Commits (`GitCommit`).
     * @throws IOException Wenn ein Fehler bei der API-Anfrage oder beim Parsen der Antwort auftritt.
     */
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

    /**
     * Holt eine bestimmte Anzahl von Commits aus einem GitHub-Repository.
     *
     * Schritte:
     * Beginnt bei der ersten Seite der Commits und paginiert durch die Antworten.
     * Iteriert über die Commits, bis die angegebene Grenze (`limit`) erreicht ist.
     * Gibt die Liste der Commits zurück, die die Grenze erfüllt.
     *
     * @param limit Die maximale Anzahl der Commits, die abgerufen werden sollen.
     * @return Eine Liste von `GitCommit`, beschränkt auf die angegebene Anzahl.
     * @throws IOException Wenn ein Fehler bei der API-Anfrage oder der Verarbeitung der Antwort auftritt.
     */
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

    /**
     * Filtert Commits basierend auf einer Ticketnummer (z. B. "[PROJ-123]") aus einer gegebenen Liste von Commits.
     *
     * Schritte:
     * Formatiert die Ticketnummer in eckigen Klammern (`[PROJ-123]`), falls nicht bereits vorhanden.
     * Iteriert durch die Liste der Commits und fügt diejenigen zur Ergebnisliste hinzu, die die Ticketnummer enthalten.
     * Gibt die gefilterte Liste der Commits zurück.
     *
     * @param commits Die Liste von Commits, die gefiltert werden soll.
     * @param ticketNummer Die Ticketnummer, nach der gefiltert werden soll.
     * @return Eine Liste von Commits, die die Ticketnummer enthalten.
     */
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

    /**
     * Filtert Commits basierend auf einer Liste von Ticketnummern.
     *
     * Schritte:
     * Iteriert durch die Liste der Ticketnummern.
     * Verwendet `fetchCommitsByTicket`, um Commits zu filtern, die zur jeweiligen Ticketnummer passen.
     * Kombiniert die gefilterten Commits zur Ergebnisliste.
     * Gibt die gefilterten Commits zurück.
     *
     * @param commits Die Liste der Commits, die gefiltert werden sollen.
     * @param ticketsList Die Liste der Ticketnummern, nach denen gefiltert werden soll.
     * @return Eine Liste von gefilterten Commits.
     */
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

    /**
     * Holt alle Commits, die vor einem bestimmten Tag im Repository enthalten sind.
     *
     * Schritte:
     * Ruft mit Hilfe der Methode `getTagInfo` die SHA und das Datum des Tags ab.
     * Holt alle Commits bis zu dem Datum mit der Methode `fetchCommitsUntilDate`.
     * Filtert die Commits, um alle nach dem Tag zu entfernen.
     * Gibt die gefilterten Commits zurück.
     *
     * @param tagName Der Name des Tags, vor dem die Commits abgerufen werden sollen.
     * @return Eine Liste der Commits vor dem angegebenen Tag.
     * @throws IOException Wenn der Tag nicht gefunden wird oder eine API-Anfrage fehlschlägt.
     */
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

    /**
     * Holt Informationen zu einem Git-Tag (wie die SHA und das Erstellungsdatum).
     *
     * Schritte:
     * Fragt den Tag-Referenz-Endpunkt ab, um die SHA des Tags zu erhalten.
     * Fragt die Commit-Details für die SHA ab, um das Erstellungsdatum des Tags zu erhalten.
     * Gibt die Taginformationen als `TagInfo`-Objekt zurück.
     *
     * @param tagName Der Name des Tags.
     * @return Ein `TagInfo`-Objekt mit SHA und Datum des Tags oder `null`, wenn nicht gefunden.
     */
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

    /**
     * Generiert ein Changelog basierend auf einer Liste von Commits und speichert es in einer Datei.
     *
     * Schritte:
     * Prüft, ob die übergebene Liste von Commits leer ist. Falls ja, gibt es eine Meldung aus und bricht ab.
     * Gruppiert die Commits in Kategorien ("New Functionality", "Bug Correction", "Others").
     * Fügt die gruppierten Abschnitte in die Changelog-Datei ein.
     * Speichert die generierten Informationen an den angegebenen Pfad.
     *
     * @param commits Die Liste der Commits, die im Changelog enthalten sein sollen.
     * @param filePath Der Pfad zur Changelog-Datei.
     * @throws IOException Wenn ein Fehler beim Schreiben der Datei auftritt.
     */
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

    /**
     * Generiert ein Changelog für alle Commits vor einem bestimmten Tag und speichert es in einer Datei.
     *
     * Schritte:
     * Holt die Commits vor dem Tag mit der Methode `getCommitsBeforeTag`.
     * Erstellt das Changelog für diese Commits mit `generateChangeLogForDistantRepo`.
     *
     * @param tagName Der Name des Tags, vor dem die Commits enthalten sein sollen.
     * @param filePath Der Pfad zur Changelog-Datei.
     * @throws IOException Wenn ein Fehler beim Abrufen von Commits oder beim Schreiben der Datei auftritt.
     */
    public void generateChangeLogBeforeTag(String tagName, String filePath) throws IOException {
        List<GitCommit> commits = getCommitsBeforeTag(tagName);
        generateChangeLogForDistantRepo(commits, filePath);
    }

    /**
     * Generiert ein Changelog für alle Commits seit dem letzten Tag und speichert es in einer Datei.
     *
     * Schritte:
     * Ruft den letzten Tag mit `getLastTag` ab.
     * Wenn kein Tag gefunden wird, werden alle Commits abgerufen. Andernfalls werden nur die Commits seit dem letzten Tag abgerufen.
     * Erstellt das Changelog mit den abgerufenen Commits und speichert es in der Datei.
     *
     * @param filePath Der Pfad zur Changelog-Datei.
     * @throws IOException Wenn ein Fehler beim Abrufen von Commits oder beim Schreiben der Datei auftritt.
     */
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

    /**
     * Holt den letzten Tag im Repository (über die GitHub-API).
     *
     * Schritte:
     * Ruft die GitHub-API für die Liste der Tags auf.
     * Wenn Tags gefunden werden, gibt sie den Namen des neuesten Tags zurück.
     * Gibt eine leere Zeichenkette zurück, wenn keine Tags gefunden wurden.
     *
     * @return Der Name des neuesten Tags oder eine leere Zeichenkette.
     */
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

    /**
     * Holt alle Commits, die seit einem bestimmten Tag gemacht wurden.
     *
     * Schritte:
     * Ruft Informationen zum gegebenen Tag ab (SHA und Datum).
     * Holt alle Commits ab dem Datum des Tags über die GitHub-API.
     * Filtert die Commits basierend darauf, ob sie den SHA des Tags enthalten.
     * Gibt die Liste der gefilterten Commits zurück.
     *
     * @param tagName Der Name des Tags, seit dem Commits geholt werden sollen.
     * @return Eine Liste von `GitCommit`, die seit diesem Tag erstellt wurden.
     * @throws IOException Wenn ein Fehler bei der API-Anfrage oder der Verarbeitung auftritt.
     */
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

    /**
     * Bereinigt die Nachricht eines Commits (entfernt überflüssige Zeilenumbrüche und Leerzeichen).
     *
     * @param message Die ursprüngliche Commit-Nachricht.
     * @return Die bereinigte Nachricht (erste Zeile, ohne Leerzeichen oder Zeilenumbrüche).
     */
    private String cleanCommitMessage(String message){
        if (message == null) return "";
        return message.split("\n")[0].trim();
    }

    /**
     * Holt alle Commits bis zu einem bestimmten Datum (über die GitHub-API).
     *
     * Schritte:
     * Baut die Anfrage-URL mit dem Parameter `until` auf (Datum).
     * Ruft alle Commits in Seiten von maximal 100 Commits gleichzeitig ab.
     * Fügt die Commits in die Liste ein.
     * Gibt die vollständige Liste der Commits zurück.
     *
     * @param untilDate Das Datum, bis zu dem die Commits geholt werden sollen (im ISO8601-Format).
     * @return Eine Liste von `GitCommit`, die bis zu diesem Datum erstellt wurden.
     * @throws IOException Wenn ein Fehler bei der API-Verarbeitung auftritt.
     */
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

    /**
     * Holt und parst ein JSON-Objekt über eine URL von der GitHub-API.
     *
     * Schritte:
     * Baut eine HTTP-Verbindung zur angegebenen URL auf.
     * Führt die Anfrage aus und prüft den Antwortstatus.
     * Liest den Antworttext und konvertiert ihn in ein JSON-Objekt.
     * Gibt das JSON-Objekt zurück.
     *
     * @param url Die URL, von der das JSON-Objekt geholt werden soll.
     * @return Ein `JSONObject`, das die Daten der API enthält.
     * @throws IOException Wenn ein Fehler bei der Anfrage oder beim Parsen auftritt.
     */
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

    /**
     * Holt die SHA eines bestimmten Tags im Repository.
     *
     * Schritte:
     * Ruft die Liste der Tags vom Repository ab.
     * Durchsucht die Tags nach dem gegebenen Tag-Namen.
     * Gibt die SHA des Tags zurück, wenn dieser gefunden wird.
     * Wirft eine Ausnahme, wenn der gegebene Tag nicht gefunden wird.
     *
     * @param tag Der Name des Tags, dessen SHA geholt werden soll.
     * @return Die SHA (Commit-Hash) des Tags.
     * @throws IOException Wenn ein Fehler bei der Anfrage auftritt.
     */
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

    /**
     * Filtert eine Liste von Commits und gibt alle Commits zurück, die **vor** einer bestimmten SHA liegen.
     *
     * Schritte:
     * Iteriert über die Liste der Commits.
     * Fügt alle Commits zur Rückgabewert-Liste hinzu, bis der angegebene SHA erreicht wird.
     * Gibt die gefilterte Liste zurück.
     *
     * @param commits Die Liste der zu filternden Commits.
     * @param sha Der SHA, bis zu dem die Commits inkludiert werden sollen.
     * @return Eine Liste gefilterter Commits.
     */
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

    /**
     * Filtert eine Liste von Commits und gibt alle Commits zurück, die **nach** einer bestimmten SHA liegen.
     *
     * Schritte:
     * Iteriert über die Liste der Commits.
     * Beginnt mit der Aufnahme von Commits, nachdem der SHA gefunden wurde.
     * Gibt die gefilterte Liste der Commits zurück.
     *
     * @param commits Die Liste der zu filternden Commits.
     * @param sha Der SHA, nach dem die Commits inkludiert werden sollen.
     * @return Eine Liste gefilterter Commits.
     */
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

    /**
     * Schreibt eine Liste von Commits in eine angegebene Datei.
     *
     * Schritte:
     * Erstellt oder überschreibt die Datei am angegebenen Pfad.
     * Iteriert durch die Liste der Commits und schreibt jeden Commit in einer neuen Zeile in die Datei.
     *
     * @param commits Die Liste der Commits, die gespeichert werden sollen.
     * @param filePath Der Pfad zur Datei, in der die Commits gespeichert werden sollen.
     * @throws IOException Wenn ein Fehler beim Schreiben der Datei auftritt.
     */
    public void writeCommitsToFile(List<GitCommit> commits, String filePath) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))){
            for (GitCommit commit : commits){
                writer.write(commit.toString());
                writer.newLine();
            }
        }
    }

    /**
     * Holt und parst ein JSON-Array über eine URL von der GitHub-API.
     *
     * Schritte:
     * Baut eine HTTP-Verbindung zur angegebenen URL auf.
     * Führt die Anfrage aus und prüft den Antwortstatus.
     * Liest den Antworttext und konvertiert ihn in ein JSON-Array.
     * Gibt das JSON-Array zurück.
     *
     * @param url Die URL, von der das JSON-Array geholt werden soll.
     * @return Ein `JSONArray`, das die Daten der API enthält.
     * @throws IOException Wenn ein Fehler bei der Anfrage oder beim Parsen auftritt.
     */
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

    /**
     * Konfiguriert die HTTP-Verbindung für die GitHub-API.
     *
     * Schritte:
     * Fügt den Bearer-Token-Header für die Authentifizierung hinzu.
     * Fügt zusätzliche Header wie "User-Agent" und "Accept"-Typ hinzu.
     *
     * @param conn Die HTTP-Verbindung, die konfiguriert werden soll.
     */
    private void setUpConnection(HttpURLConnection conn) {
        if (authToken != null && !authToken.isEmpty()){
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        conn.setRequestProperty("Accept", "application/vnd.github+json");
        //conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        conn.setRequestProperty("User-Agent", "baurel.tanekam@medien-systempartner");
    }

    /**
     * Holt paginierte Commits von einer gegebenen GitHub-API-URL.
     *
     * Schritte:
     * Baut die HTTP-Verbindung zur gegebenen URL auf.
     * Holt die Antwort und liest ihre Inhalte in ein JSON-Array ein.
     * Parst den "Link"-Header für die nächste Seite (falls vorhanden).
     * Gibt ein `PaginatedResponse`-Objekt zurück, das die Commits und die URL der nächsten Seite enthält.
     *
     * @param url Die URL für die API-Anfrage nach Commits.
     * @return Eine `PaginatedResponse`, die die Commit-Daten und die nächste Seite enthält.
     * @throws IOException Wenn ein Fehler bei der Anfrage oder der Verarbeitung auftritt.
     */
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

    /**
     * Extrahiert die URL der nächsten Seite aus einem "Link"-HTTP-Header.
     *
     * Schritte:
     * Teilt den Header in einzelne Links auf, wenn er mehrere enthält (Komma getrennt).
     * Sucht nach der Link-Beschreibung `rel="next"` und extrahiert die URL.
     * Gibt `null` zurück, wenn keine nächste Seite vorhanden ist.
     *
     * @param link Der Link-Header als String.
     * @return Die URL der nächsten Seite oder `null`, falls keine vorhanden.
     */
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

    /**
     * Testet die Verbindung zur GitHub-API für das aktuelle Repository.
     *
     * Schritte:
     * Führt eine Anfrage an die API-URL des Repositories aus.
     * Überprüft den HTTP-Antwortstatus und gibt die entsprechende Diagnosemeldung aus.
     * Gibt Details zu Fehlern wie ungültigem Token, verbotener Zugriff oder Repository nicht gefunden.
     *
     * @throws IOException Wenn ein Fehler beim Aufbau der Verbindung oder der Anfrage auftritt.
     */
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
