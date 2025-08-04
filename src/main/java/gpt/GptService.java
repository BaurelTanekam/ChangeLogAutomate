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
    
    public String processChangeLog(String changeLogFilePath) throws ChatGPTException, IOException {
        System.out.println("Processing changelog file: " + changeLogFilePath);
        
        //Read changelog content
        String changelogContent = readFile(changeLogFilePath);
        if (changelogContent.trim().isEmpty()){
            throw new ChatGPTException("Changelog file is empty or could not be read");
        }
        
        //Create prompt for ChatGPT
        String prompt = createChangelogPrompt(changelogContent);
        
        //send Request to ChatGPT
        String response = sendChatGPTRequest(prompt);
        
        //Save response to ChatGPT
        String outPutFileName = genetrateOutputFileName();
        saveMarkdownFile(response, outPutFileName);

        System.out.println("✅ Summary generated succesfully: " + outPutFileName);
        return response;
    }

    /**
     * Process changelog with additional JIRA context
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

    private String generateOutputFileName(String s) {
        String timeStamp = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return "release-" + s + "-" + timeStamp + ".md";
    }

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

    private String genetrateOutputFileName() {
        return generateOutputFileName("Summary");
    }

    private String createChangelogPrompt(String changelogContent) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional technical writer who specializes in creating clear, user-friendly, and comprehensive release notes for end users.\n\n");

        prompt.append("Your task is to analyze the following unstructured changelog data, Git commit messages, and JIRA comments, and create a polished, easy-to-read release summary for a non-technical audience.\n");
        prompt.append("The resulting summary must be informative, GDPR-compliant, and written in plain language.\n\n");

        prompt.append("**Instructions:**\n");
        prompt.append("1. Group related updates into clear categories:\n");
        prompt.append("   - New Features\n");
        prompt.append("   - Improvements\n");
        prompt.append("   - Bug Fixes\n");
        prompt.append("   - Other Changes (if necessary)\n");
        prompt.append("2. Use simple, non-technical language—avoid technical jargon or implementation details.\n");
        prompt.append("3. Focus on the benefits or impact of each change for the user.\n");
        prompt.append("4. If helpful, incorporate relevant context from JIRA comments, but ensure all descriptions are anonymous and free of personal or sensitive data.\n");
        prompt.append("5. Do **not** include ticket numbers (e.g., PROJ-123) unless essential for user understanding, and even then, present them generically.\n");
        prompt.append("6. Begin with a short, friendly introduction summarizing the purpose and highlights of this release (e.g., new functionality, performance upgrades, or fixes for known issues).\n");
        prompt.append("7. Ensure the final output is fully compliant with GDPR (DSGVO)—do not include any personally identifiable information or references to individuals.\n");
        prompt.append("8. Format the final release notes as clean, readable Markdown using appropriate headings and bullet points.\n\n");

        prompt.append("**Raw Change Data:**\n");
        prompt.append("```\n");
        prompt.append(changelogContent);
        prompt.append("\n```\n\n");

        prompt.append("Now, generate the user-friendly, GDPR-compliant release summary in Markdown format:");

        return prompt.toString();

    }

    private String readFile(String changeLogFilePath) throws IOException {
        Path path = Paths.get(changeLogFilePath);
        if (!Files.exists(path)){
            throw new IOException("file not found: " + changeLogFilePath);
        }

        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes);
    }

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

    public static class ChatGPTException extends Exception{
        public ChatGPTException(String message){
            super(message);
        }
        
        public ChatGPTException(String message, Throwable cause){
            super(message, cause);
        }
    }
    
}
