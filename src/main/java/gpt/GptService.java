package gpt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import credentials.Connexion;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GptService {
    private static final String OPENAI_API_URL= "https://api.langdock.com/openai/eu/v1/chat/completions";
    private static final String MODEL = "gpt-4o-mini";

    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;
    private String API_KEY = Connexion.GPT;
    private String outPutDirectory;

    /**
     * Konstruktor für den GptService.
     * Initialisiert die benötigten Ressourcen wie das HTTP-Client, den JSON-Mapper, und erstellt das Ausgabe-Verzeichnis.
     *
     * @param outPutDirectory Das Verzeichnis, in dem Ausgabedateien gespeichert werden sollen.
     */
    public GptService(String outPutDirectory){
        this.outPutDirectory = outPutDirectory;
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
        
        try{
            Files.createDirectories(Paths.get(outPutDirectory));
        }catch(IOException e){
            System.err.println("Warning: Could not create output directory: " + e.getMessage());
        }
    }

    /**
     * Verarbeitet ein Changelog-File und erstellt eine benutzerfreundliche Zusammenfassung mithilfe von ChatGPT.
     *
     * Schritte:
     * Liest den Inhalt des Changelog-Files.
     * Sendet eine Anfrage an das ChatGPT-API, um eine entsprechende Antwort zu erhalten.
     * Speichert die Antwort als Markdown-Datei in das festgelegte Ausgabe-Verzeichnis.
     *
     * @param changeLogFilePath Der Pfad zum Changelog-File, das verarbeitet werden soll.
     * @return Die von ChatGPT generierte Zusammenfassung als String.
     * @throws ChatGPTException Wenn die Datei leer ist oder ein Fehler bei der Verarbeitung durch das ChatGPT-API auftritt.
     * @throws IOException Wenn ein Fehler beim Zugriff auf das Changelog-File auftritt.
     */
    public String processChangeLog(String changeLogFilePath) throws ChatGPTException, IOException {
        System.out.println("Processing changelog file: " + changeLogFilePath);
        
        //Read changelog content
        String changelogContent = readFile(changeLogFilePath);
        if (changelogContent.trim().isEmpty()){
            throw new ChatGPTException("Changelog file is empty or could not be read");
        }
        
        //Create prompt for ChatGPT
        String prompt = changelogPrompt(changelogContent);
        
        //send Request to ChatGPT
        String response = sendChatGPTRequest(prompt);
        
        //Save response to ChatGPT
        String outPutFileName = genetrateOutputFileName();
        saveMarkdownFile(response, outPutFileName);

        System.out.println("✅ Summary generated succesfully: " + outPutFileName);
        return response;
    }

    /**
     * Verarbeitet ein Changelog-File, fügt zusätzlichen JIRA-Kontext hinzu und erstellt eine benutzerfreundliche Zusammenfassung.
     *
     * Schritte:
     * Liest den Inhalt des Changelogs und prüft, ob es gültig ist.
     * Generiert einen erweiterten Prompt, der den Inhalt des Changelogs und zusätzliche JIRA-Kommentare kombiniert.
     * Sendet diesen erweiterten Prompt an das ChatGPT-API, um eine Antwort zu generieren.
     * Speichert die Antwort als Markdown-Datei in das festgelegte Ausgabe-Verzeichnis.
     *
     * @param changelogFilePath Der Pfad zum Changelog-File.
     * @param jiraComments Eine Liste von zusätzlichen Kommentaren aus JIRA-Tickets.
     * @return Die von ChatGPT generierte erweiterte Zusammenfassung als String.
     * @throws ChatGPTException Wenn das Changelog leer ist oder ein Fehler beim Verarbeiten der Anfrage auftritt.
     * @throws IOException Wenn ein Fehler beim Lesen des Changelog-Files auftritt.
     */
    public String processChangelogWithJiraContext(String changelogFilePath, List<String> jiraComments)
            throws IOException, ChatGPTException {

        System.out.println("Processing changelog with JIRA context...");

        String changelogContent = readFile(changelogFilePath);
        if (changelogContent.trim().isEmpty()) {
            throw new ChatGPTException("Changelog file is empty or could not be read");
        }

        // Create enhanced prompt with JIRA context
        String prompt = createEnhancedPrompt(changelogContent, jiraComments);

        // Send request to ChatGPT
        String response = sendChatGPTRequest(prompt);

        // Save response to markdown file
        String outputFileName = generateOutputFileName("with-jira-context");
        saveMarkdownFile(response, outputFileName);

        System.out.println("✅ Enhanced summary generated successfully: " + outputFileName);
        return response;
    }

    /**
     * Sendet eine Anfrage (Prompt) an die ChatGPT-API und erhält die generierte Antwort.
     *
     * Schritte:
     * Erstellt eine HTTP POST-Anfrage mit dem Prompt als JSON-Body.
     * Definiert einen ResponseHandler, der die Antwort interpretiert und auf Fehler prüft.
     * Falls die Antwort erfolgreich ist, wird sie gelesen und zurückgegeben.
     * Falls die Antwort ein Fehler ist (z. B. 400, 401, 429), wird dieser entsprechend behandelt.
     *
     * @param prompt Der von der Anwendung generierte Prompt, der an die ChatGPT-API gesendet werden soll.
     * @return Die Antwort von der ChatGPT-API als String.
     * @throws IOException Wenn ein Fehler bei der Anfrage oder der Verarbeitung auftritt.
     * @throws ChatGPTException Wenn ein spezifischer Fehler von der ChatGPT-API auftritt.
     */
    private String sendChatGPTRequest(String prompt) throws IOException, ChatGPTException, JsonProcessingException {
        System.out.println("Sending request to ChatGPT API... ");

        HttpPost request = new HttpPost(OPENAI_API_URL);

        request.setHeader("Authorization", API_KEY);
        request.setHeader("Content-Type", "application/json");

        //Create request Body
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("temperature", 0.3);

        //Create messages array
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);

        requestBody.set("messages", messages);
        //Set request body
        String jsonBody = objectMapper.writeValueAsString(requestBody);
        request.setEntity(new StringEntity(jsonBody));

        //Ressource management
        HttpClientResponseHandler<String> responseHandler = response -> {
            int statusCode = response.getCode();
            HttpEntity entity = response.getEntity();
            String responseBody = entity != null ? EntityUtils.toString(entity): "";

            System.out.println("ChatGPT API Status Code: " + statusCode);

            if (statusCode == HttpStatus.SC_OK){
                try {
                    return parseChatGPTResponse(responseBody);
                } catch (ChatGPTException e) {
                    throw new RuntimeException(e);
                }
            }else{
                try {
                    handleApierror(statusCode, responseBody);
                } catch (ChatGPTException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        };

        //Execueute request
        try{
            return httpClient.execute(request, responseHandler);
        } catch (IOException e) {
            throw new IOException("Error executing ChatGPT request: " + e.getMessage());
        }
    }

    /**
     * Behandelt mögliche API-Fehler, die von der ChatGPT-API zurückgegeben werden.
     *
     * Schritte:
     * Analysiert den Statuscode (z. B. 401, 429, 500) und wirft eine geeignete Ausnahme mit einer detaillierten Nachricht.
     * Falls die Antwort einen Fehler im JSON-Format enthält, wird die Fehlermeldung extrahiert.
     * Gibt einen allgemeinen Fehler zurück, falls der Statuscode unbekannt ist.
     *
     * @param statusCode Der von der API zurückgegebene HTTP-Statuscode.
     * @param responseBody Der Body der HTTP-Antwort, der den Fehler beschreibt.
     * @throws ChatGPTException Wenn ein spezifischer Fehler auftritt.
     */
    private void handleApierror(int statusCode, String responseBody) throws ChatGPTException, JsonProcessingException {
        String errorMessage = "ChatGPT API Error: " + statusCode;

        try{
            JsonNode errorRoot = objectMapper.readTree(responseBody);
            if (errorRoot.has("error")){
                JsonNode error = errorRoot.get("error");
                errorMessage = error.path("message").asText(errorMessage);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        switch (statusCode){
            case 401: throw new ChatGPTException("Inavlid API Key. Please check your OpenAi API Key.");
            case 429: throw new ChatGPTException("Rate limit exceeded. Please try again later.");
            case 400: throw new ChatGPTException("Bad request: " + errorMessage);
            case 500: throw new ChatGPTException("Open AI server error. Please try again later.");
            default: throw new ChatGPTException(errorMessage + " - Response: " + responseBody);
        }

    }

    /**
     * Verarbeitet die Antwort von der ChatGPT-API und extrahiert die nutzbare Antwort.
     *
     * Schritte:
     * Liest die JSON-Daten von der API-Antwort.
     * Überprüft, ob ein `error`-Feld vorhanden ist. Wenn ja, wird ein spezifischer Fehler ausgelöst.
     * Extrahiert den Inhalt des `choices`-Arrays und gibt die erste Antwort zurück.
     * Falls keine Antwort verfügbar ist oder der Inhalt leer ist, wird ein Fehler ausgelöst.
     *
     * @param responseBody Der JSON-Body der Antwort von der ChatGPT-API.
     * @return Der extrahierte Text aus der API-Antwort.
     * @throws ChatGPTException Wenn keine gültige Antwort verfügbar ist.
     */
    private String parseChatGPTResponse(String responseBody) throws ChatGPTException, JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("error")){
            JsonNode error = root.get("error");
            String errormessage = error.path("message").asText("Unknown error");
            String errorType = error.path("type").asText("Unknown");
            throw new ChatGPTException("ChatGPT API Error (" + errorType + "): " + errormessage);
        }

        JsonNode choices = root.path("choices");
        if (choices.isArray() && choices.size() > 0){
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.path("message");
            String content = message.path("content").asText();

            if (content.trim().isEmpty()){
                throw new ChatGPTException("ChatGPT returned empty response.");
            }
            return content.trim();
        }else {
            throw new ChatGPTException("No choices returned from ChatGPT API");
        }

    }

    /**
     * Generiert einen eindeutigen Dateinamen für die Ausgabedatei basierend auf dem aktuellen Datum.
     *
     * @param s Eine benutzerdefinierte Beschreibung, die im Dateinamen verwendet wird (z. B. "with-jira-context").
     * @return Der generierte Dateiname im Format `release-<Beschreibung>-<Datum>.md`.
     */
    private String generateOutputFileName(String s) {
        String timeStamp = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return "release-" + s + "-" + timeStamp + ".md";
    }

    /**
     * Erstellt einen erweiterten Prompt für ChatGPT, der das Changelog und zusätzliche JIRA-Kommentare kombiniert.
     *
     * Schritte:
     * Fügt allgemeine Anweisungen hinzu (z. B. Erstellen einer benutzerfreundlichen Zusammenfassung).
     * Integriert den Inhalt des Changelogs.
     * Falls JIRA-Kommentare vorhanden sind, integriert sie ebenfalls in den Prompt.
     *
     * @param changelogContent Der Textinhalt des Changelogs.
     * @param jiraComments Eine Liste von zusätzlichen Kommentaren aus JIRA-Tickets.
     * @return Der erstellte erweiterte Prompt als String.
     */
    private String createEnhancedPrompt(String changelogContent, List<String> jiraComments) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a technical writer creating user-friendly release notes. ");
        prompt.append("You have both git commit messages and additional context from JIRA tickets. ");
        prompt.append("Please create a comprehensive, non-technical summary.\n\n");

        prompt.append("**Instructions:**\n");
        prompt.append("1. Combine information from both git commits and JIRA context\n");
        prompt.append("2. Group changes into logical categories (New Features, Improvements, Bug Fixes, Security, etc.)\n");
        prompt.append("3. Use simple language that non-technical users can understand\n");
        prompt.append("4. Focus on user impact and benefits\n");
        prompt.append("5. Format as professional markdown with clear headings\n");
        prompt.append("6. Include a summary introduction and conclusion\n");
        prompt.append("7. If there are breaking changes or important notices, highlight them clearly\n\n");

        prompt.append("**Git Changelog:**\n");
        prompt.append("```\n");
        prompt.append(changelogContent);
        prompt.append("\n```\n\n");

        if (jiraComments != null && !jiraComments.isEmpty()) {
            prompt.append("**JIRA Ticket Context:**\n");
            for (int i = 0; i < jiraComments.size(); i++) {
                prompt.append("Ticket ").append(i + 1).append(":\n");
                prompt.append("```\n");
                prompt.append(jiraComments.get(i));
                prompt.append("\n```\n\n");
            }
        }

        prompt.append("Please create a comprehensive, user-friendly release summary in markdown format:");

        return prompt.toString();

    }

    /**
     * Speichert die ChatGPT-Antwort in einer Markdown-Datei im Ausgabe-Verzeichnis.
     *
     * Schritte:
     * Erstellt den vollständigen Pfad der Datei basierend auf dem Ausgabe-Verzeichnis und dem Dateinamen.
     * Formatiert den Inhalt als Markdown (Kopfzeile, Trennlinien, Antwortinhalt).
     * Schreibt den formatierten Inhalt in die Datei (erstellt sie, falls sie nicht existiert).
     *
     * @param response Die Antwort, die in der Datei gespeichert werden soll.
     * @param outPutFileName Der Name der Ausgabedatei.
     * @throws IOException Wenn ein Fehler beim Schreiben der Datei auftritt.
     */
    private void saveMarkdownFile(String response, String outPutFileName) throws IOException {
        Path outputPath = Paths.get(outPutDirectory, outPutFileName);

        StringBuilder markdownContent = new StringBuilder();
        markdownContent.append("\n Release Summary\n\n").append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        markdownContent.append("---\n\n");
        markdownContent.append(response);

        Files.write(outputPath, markdownContent.toString().getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("Markdown file saved: " + outputPath.toAbsolutePath());
    }

    /**
     * Erzeugt standardmäßig den Dateinamen für eine allgemeine Zusammenfassung.
     * Verwendet `generateOutputFileName` mit dem Standardwert "Summary".
     *
     * @return Der erzeugte Dateiname im Format "release-Summary-<Datum>.md".
     */
    private String genetrateOutputFileName() {
        return generateOutputFileName("Summary");
    }

    /**
     * Erstellt einen Prompt für ChatGPT, der nur auf dem Changelog basiert.
     * Bietet grundlegende Anweisungen, wie der Inhalt des Changelogs in benutzerfreundliche Release Notes umgewandelt werden soll.
     *
     * @param changelogContent Der Textinhalt des Changelogs.
     * @return Der erstellte Prompt als String.
     */
    private String changelogPrompt(String changelogContent) {
        StringBuilder prompt = new StringBuilder();
//        prompt.append("You are a professional technical writer who specializes in creating clear, user-friendly, and comprehensive release notes for end users.\n\n");
//
//        prompt.append("Your task is to analyze the following unstructured changelog data, Git commit messages, and JIRA comments, and create a polished, easy-to-read release summary for a non-technical audience.\n");
//        prompt.append("The resulting summary must be informative, GDPR-compliant, and written in plain language.\n\n");
//
//        prompt.append("**Instructions:**\n");
//        prompt.append("1. Group related updates into clear categories:\n");
//        prompt.append("   - New Features\n");
//        prompt.append("   - Improvements\n");
//        prompt.append("   - Bug Fixes\n");
//        prompt.append("   - Other Changes (if necessary)\n");
//        prompt.append("2. Use simple, non-technical language—avoid technical jargon or implementation details.\n");
//        prompt.append("3. Focus on the benefits or impact of each change for the user.\n");
//        prompt.append("4. If helpful, incorporate relevant context from JIRA comments, but ensure all descriptions are anonymous and free of personal or sensitive data.\n");
//        prompt.append("5. Do **not** include ticket numbers (e.g., PROJ-123) unless essential for user understanding, and even then, present them generically.\n");
//        prompt.append("6. Begin with a short, friendly introduction summarizing the purpose and highlights of this release (e.g., new functionality, performance upgrades, or fixes for known issues).\n");
//        prompt.append("7. Ensure the final output is fully compliant with GDPR (DSGVO)—do not include any personally identifiable information or references to individuals.\n");
//        prompt.append("8. Format the final release notes as clean, readable Markdown using appropriate headings and bullet points.\n\n");
//
//        prompt.append("**Raw Change Data:**\n");
//        prompt.append("### Zielsetzung\n");
//        prompt.append("Erstellen Sie präzise und informative Release Notes aus unstrukturierten Changelog-Daten, Git-Commit-Nachrichten und JIRA-Kommentaren für eine nicht-technische Zielgruppe, einschließlich Endbenutzern und Managern. Die Release Notes sollen sowohl in deutscher als auch in englischer Sprache verfasst werden.\n\n");
//        prompt.append("### Rückgabe Format\n");
//        prompt.append("Die Ergebnisse sind als gut strukturierte Markdown-Dokumente zu formatieren, die einzelne Änderungen klar gliedern und in einer neutralen und informativen Sprache verfasst sind. Achten Sie darauf, dass die Inhalte in beiden Sprachen konsistent übertragen werden.\n\n");
//        prompt.append("### Warnungen\n");
//        prompt.append("1. Betrachten Sie im Kontext nur die Änderungen und deren direkten Auswirkungen auf die Benutzer.\n");
//        prompt.append("2. Achten Sie darauf, dass die Notes DSGVO-konform sind und vermeiden Sie persönlich-identifizierbare Informationen oder sensible Daten.\n");
//        prompt.append("3. Setzen Sie realistische Erwartungen an die Details, die aus den Änderungen abgeleitet werden können, und beschränken Sie sich auf die wesentlichen Informationen.\n\n");
//        prompt.append("### Context Dump\n");
//        prompt.append("**Anweisungen:**\n");
//        prompt.append("1. Gruppieren Sie verwandte Updates in klare Kategorien:\n");
//        prompt.append("   - Neue Funktionen / New Features\n");
//        prompt.append("   - Verbesserungen / Improvements\n");
//        prompt.append("   - Fehlerbehebungen / Bug Fixes\n");
//        prompt.append("   - Sonstige Änderungen / Other Changes (falls notwendig)\n\n");
//        prompt.append("2. Verwenden Sie einfache, nicht-technische Sprache – vermeiden Sie technischen Jargon oder unnötige Details zur Implementierung in beiden Sprachen.\n");
//
//        prompt.append("3. Fokussieren Sie sich auf die Vorteile oder Auswirkungen jeder Änderung für den Endbenutzer in beiden Sprachen.\n");
//        prompt.append("4. Integrieren Sie relevante Kontexte aus den JIRA-Kommentaren, stellen Sie jedoch sicher, dass alle Beschreibungen anonym und ohne persönliche oder sensible Daten sind.\n");
//        prompt.append("5. Fügen Sie keine Ticketnummern (z. B. PROJ-123) hinzu, es sei denn, sie sind für das Verständnis der Änderungen unbedingt erforderlich. Wenn ja, präsentieren Sie diese dann generisch.\n");
//        prompt.append("6. Beginnen Sie mit einer kurzen, freundlichen Einführung, die den Zweck und die Höhepunkte dieses Releases zusammenfasst (z. B. neue Funktionalitäten, Leistungsverbesserungen oder Behebungen bekannter Probleme) in beiden Sprachen.\n");
//        prompt.append("7. Stellen Sie sicher, dass das endgültige Ergebnis vollständig DSGVO-konform ist und keine persönlich-identifizierbaren Informationen oder Verweise auf Einzelpersonen enthält.\n");
//        prompt.append("8. Formatieren Sie die endgültigen Release Notes als sauberes, lesbares Markdown mit angemessenen Überschriften und Aufzählungspunkten in beiden Sprachen.\n\n");
//        prompt.append("### Eingabe \n");
//        prompt.append("<Eingabebereich für den Text des Nutzers>\n\n");
//        prompt.append("### Umsetzbare Schritte:\n");
//        prompt.append("1. Analysieren Sie zunächst die gesammelten Daten aus Git und JIRA, um den Inhalt zu strukturieren.\n");
//        prompt.append("2. Sammeln Sie relevante Aspekte zu jeder Änderung, bevor Sie mit dem Schreiben beginnen.\n");
//        prompt.append("3. Verwenden Sie eine einfache Sprache, um den Schreibprozess zu führen, vermeiden Sie technische Begriffe in beiden Sprachen.\n");
//        prompt.append("4. Überprüfen Sie die fertigen Notes auf die Einhaltung der DSGVO-Richtlinien.\n");
//        prompt.append("5. Fügen Sie die Formatierung für Markdown hinzu, bevor Sie die Release Notes veröffentlichen.\n");
        prompt.append("Du bist ein sehr präzises, direktes Sprachmodell mit hohem analytischen Verständnis. Deine Aufgabe:\n");
        prompt.append("### Ziel\n");
        prompt.append("Erstelle Release Notes für eine **nicht-technische Zielgruppe** (Enduser, Manager), basierend auf unstrukturierten Changelog-Daten, Git‑Commit‑Texten und anonymisierten JIRA-Kommentaren.\n\n");
        prompt.append("### Anforderungen\n");
        prompt.append("- Gruppiere alle Änderungen in diese Kategorien:\n");
        prompt.append("  - Neue Funktionen\n");
        prompt.append("  - Verbesserungen\n");
        prompt.append("  - Fehlerbehebungen\n");
        prompt.append("  - Sonstige (optional)\n");
        prompt.append("- Formuliere **kurz, klar und ohne Jargon**, Schwerpunkt auf Nutzen für den Nutzer.\n");
        prompt.append("- Verzichte auf tech‑Details; beschreibe Effekte und Vorteile.\n");
        prompt.append("- Halte dich **DSGVO‑konform**: keine personenbezogenen Daten oder Ticketnummern (außer generisch, falls unbedingt nötig).\n");
        prompt.append("- Nutze anonymisierte Hinweise aus JIRA-Kommentaren, ohne Namen.\n");
        prompt.append("- Beginne mit einer kurzen, freundlichen **Einführung** mit Highlights des Releases.\n");
        prompt.append("- Verwende **Markdown‑Formatierung** mit Überschriften und Stichpunkten.\n");
        prompt.append("- Gib eine empfohlene Länge von **max. 250 Wörtern** vor.\n\n");
        prompt.append("### Vorgehensweise\n");
        prompt.append("1. Analysiere alle Eingabedaten (Changelog, Commits, JIRA-Kommentare).\n");
        prompt.append("2. Identifiziere Änderungen, gruppiere sie wie oben.\n");
        prompt.append("3. Formuliere die Notes in einfachem, nutzerzentriertem Stil.\n");
        prompt.append("4. Überprüfe das Ergebnis auf DSGVO-Konformität.\n");
        prompt.append("5. Füge Markdown‑Formatierung hinzu und liefere die Release Notes.\n\n");
        prompt.append("### Rückgabe\n");
        prompt.append("- Nutze direkt diesen gesamten Prompt beim Ausführen.\n");
        prompt.append("- Begründe optional kurz (2–3 Sätze), warum du diese Struktur gewählt hast.\n");
        prompt.append("```\n");
        prompt.append(changelogContent);
        prompt.append("\n```\n\n");

        //prompt.append("Now, generate the user-friendly, GDPR-compliant release summary in Markdown format:");

        return prompt.toString();

    }

    /**
     * Liest den Inhalt einer Datei und gibt ihn als String zurück.
     *
     * @param changeLogFilePath Der Pfad zur Datei, die gelesen werden soll.
     * @return Der gesamte Dateinhalt als String.
     * @throws IOException Wenn die Datei nicht existiert oder nicht gelesen werden kann.
     */
    private String readFile(String changeLogFilePath) throws IOException {
        Path path = Paths.get(changeLogFilePath);
        if (!Files.exists(path)){
            throw new IOException("file not found: " + changeLogFilePath);
        }

        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }

    /**
     * Verarbeitet mehrere Changelog-Dateien und generiert eine Zusammenfassung für jede.
     *
     * Schritte:
     * Iteriert durch die Liste der übergebenen Datei-Pfade.
     * Verarbeitet jede Datei mit `processChangeLog`.
     * Fügt die Ergebnisse in eine Liste ein.
     * Gibt die Liste der Ergebnisse zurück.
     *
     * @param changelogFiles Eine Liste von Datei-Pfaden zu Changelogs.
     * @return Eine Liste der generierten ChatGPT-Antworten.
     * @throws IOException Wenn ein Fehler beim Verarbeiten einer Datei auftritt.
     * @throws ChatGPTException Wenn ein Fehler bei der Verarbeitung durch ChatGPT auftritt.
     */
    public List<String> processMultipleChangeLogs(List<String> changelogFiles) throws IOException, ChatGPTException {
        List<String> results = new ArrayList<>();

        for (String file : changelogFiles){
            try{
                System.out.println("Processing file: " + file);
                String result = processChangeLog(file);
                results.add(result);

                //Delay
                Thread.sleep(1000);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
                throw  new ChatGPTException("Processing interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error processing " + file + ": " + e.getMessage());
                results.add("Error processing file: " + e.getMessage());
            }
        }
        return results;
    }

    /**
     * Eine benutzerdefinierte Ausnahme für Fehler, die bei der Verarbeitung von ChatGPT-Anfragen auftreten.
     */
    public static class ChatGPTException extends Exception{
        public ChatGPTException(String message){
            super(message);
        }
        
        public ChatGPTException(String message, Throwable cause){
            super(message, cause);
        }
    }
    
}
