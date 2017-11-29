import java.util.UUID;

public class User {
    public User ( String inputUserName ) {
        userName = inputUserName;
        id = UUID.randomUUID();
        isOnline = true;
        token = userName.hashCode();
    }

    public String getUserName() { return userName; }
    public UUID getId() { return id; }
    public boolean isOnline() { return isOnline; }
    public int getToken() { return token; }

    private String userName;
    private UUID id;
    private boolean isOnline;
    private int token;
}
