package credentials;

import io.github.cdimascio.dotenv.Dotenv;

public class Ressources {
    private static final Dotenv dotenv = Dotenv.load();

    //Get Username and Token
    public static final String TOKEN = dotenv.get("API_TOKEN");
    public static final String USER = dotenv.get("USERNAME");
    public static final String URI = dotenv.get("URI");

    //
    static {
        if (TOKEN == null || USER == null){
            throw new RuntimeException("Please verify the Token or the username.");
        }
    }
}
