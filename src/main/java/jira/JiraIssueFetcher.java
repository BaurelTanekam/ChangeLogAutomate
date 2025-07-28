package jira;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JiraIssueFetcher {
    private CloseableHttpClient httpClient;
    private ObjectMapper objectMapper;
}
