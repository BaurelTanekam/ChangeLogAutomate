import log.LogService;

public class App {
    public static void main(String[] args) {
        LogService logService = new LogService();
        logService.generateChangelog();
    }
}
