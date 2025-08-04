package credentials;

import io.github.cdimascio.dotenv.Dotenv;

public class Ressources {
    private static final Dotenv dotenv = Dotenv.load();

    // Statisch definierte Konstanten für die Umgebungsvariablen (aus der .env-Datei):
    // Token, Benutzername, URI, API-Key und Repository-Details.
    // Diese werden aus den entsprechenden Schlüssel-Werten aus der .env-Datei bezogen.

    //Get Username and Token
    public static final String TOKEN = dotenv.get("API_TOKEN");
    public static final String USER = dotenv.get("USERNAME");
    public static final String URI = dotenv.get("URI");
    public static final String API_GPT = dotenv.get("API_TOKEN_LANGDOCK");
    public static final String OWNER_REPO = dotenv.get("REPO_OWNER");
    public static final String NAME_REPO = dotenv.get("REPO_NAME");
    public static final String AUTH_REPO = dotenv.get("REPO_AUTH");

    //
    static {
        if (TOKEN == null || USER == null){
            throw new RuntimeException("Please verify the Token or the username.");
        }
    }
}
