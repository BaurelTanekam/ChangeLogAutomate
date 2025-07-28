import java.util.Base64;

public class Connexion {
    public static final String CREDENTIALS = Ressources.USER + ":" + Ressources.TOKEN ;

    public static final String ENCODED = Base64.getEncoder().encodeToString(CREDENTIALS.getBytes());
}
