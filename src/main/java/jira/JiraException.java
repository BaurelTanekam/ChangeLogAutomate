package jira;

public class JiraException extends Exception {
    public JiraException(String message){
        super(message);
    }

    public JiraException(String message, Throwable cause){
        super(message, cause);
    }
}
