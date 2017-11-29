import com.sun.net.httpserver.*;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;


public class Server {
    public static void main( String[] args ) {
        try {
            System.out.println("Starting server...");
            System.out.println("Local address : " + InetAddress.getLocalHost().getHostAddress());
            HttpServer server = HttpServer.create( new InetSocketAddress(8000), 0);
            onlineUsers = new HashMap<Integer, User>();
            messages = new LinkedList<String>();

            server.createContext("/", new RootHandler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/main", new getMainPageHandler());
            server.createContext("/client.js", new getJS());
            server.createContext("/logout", new LogoutHandler());
            server.createContext("/users", new getUsersHandler());
            server.setExecutor(null);
            server.start();
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    static class LoginHandler implements HttpHandler {
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("POST")) {
                System.out.println("Login received.");
                Headers head = exchange.getResponseHeaders();
                String JSONReceived = getRequestBody(exchange);
                JSONObject jsonObject = new JSONObject(JSONReceived);
                String userName = jsonObject.getString("username");

                if ( onlineUsers.containsKey(userName.hashCode()) ) {
                    System.out.println("User name" + userName + " is already in use...");
                    head.add("WWW-Authenticate","Token realm='UserName is already in use'");
                    exchange.sendResponseHeaders(401, 0);
                    return;
                }

                User newUser = new User(userName);
                onlineUsers.put(newUser.getToken(), newUser);
                System.out.println("User is tyring to log in : " + userName);
                jsonObject.append("id", newUser.getId());
                jsonObject.append("online", newUser.isOnline());
                jsonObject.append("token", newUser.getToken());

                byte[] data = jsonObject.toString().getBytes();
                head.add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, data.length);

                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            }
        }

    }

    static class getJS implements HttpHandler {
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                String response = getDocument(filePath + javaScriptFileName);

                byte[] data = response.getBytes();
                Headers head = exchange.getResponseHeaders();
                head.set("Content-Type", "application/javascript");
                exchange.sendResponseHeaders(200, data.length);
                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            }
        }
    }

    static class RootHandler implements HttpHandler {
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                String response = getDocument(filePath + startPageName);

                byte[] data = response.getBytes();
                Headers head = exchange.getResponseHeaders();
                head.set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, data.length);
                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            }
        }
    }

    static class getMainPageHandler implements HttpHandler {
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                String response = getDocument(filePath + mainPageName);
                byte[] data = response.getBytes();
                Headers head = exchange.getResponseHeaders();
                head.add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, data.length);

                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                //parsing client's request
                Headers incomingHeaders = exchange.getRequestHeaders();
                int token  = getAuthenticToken(incomingHeaders);
                System.out.println("Token to logout : " + token);
                System.out.println("User is logging out : " + onlineUsers.get(token).getUserName());
                onlineUsers.remove(token);
                //sending response
                Headers head = exchange.getResponseHeaders();
                head.add("Content-Type", "application/json");
                JSONObject respond = new JSONObject();
                respond = respond.put("message", "bye");
                byte[] data = respond.toString().getBytes();
                exchange.sendResponseHeaders(200, data.length);
                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            }
        }
    }

    static class getUsersHandler implements HttpHandler {
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                Headers incomingHeaders = exchange.getRequestHeaders();
                int token = getAuthenticToken(incomingHeaders);
                if ( !onlineUsers.containsKey(token) ) {
                    return;
                }

                JSONObject jsonToSend = new JSONObject();
                JSONArray array = new JSONArray();
                for ( int currentUserToken : onlineUsers.keySet() ) {
                    JSONObject JSONUser = new JSONObject();
                    JSONUser.put("id", onlineUsers.get(currentUserToken).getId());
                    JSONUser.put("username", onlineUsers.get(currentUserToken).getUserName());
                    JSONUser.put("online", onlineUsers.get(currentUserToken).isOnline());
                    array.put(JSONUser);
                }
                jsonToSend.put("users", array);
                Headers head = exchange.getResponseHeaders();
                head.add("Content-Type", "application/json");
                byte[] data = jsonToSend.toString().getBytes();
                exchange.sendResponseHeaders(200, data.length);
                OutputStream os = exchange.getResponseBody();
                os.write(data);
                os.close();
            }
        }
    }


    private static int getAuthenticToken( Headers head ) {
        return Integer.parseInt(head.getFirst("Authorization").split(" ")[1]);
    }

    private static String getDocument(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ( (line = br.readLine()) != null ) {
            sb.append(line);
        }

        return sb.toString();
    }

    private static String getRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
        BufferedReader br = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        String line = null;

        while ( (line = br.readLine()) != null ) {
            builder.append(line);
        }

        return builder.toString();

    }


    private static HashMap<Integer ,User> onlineUsers;
    private static LinkedList<String> messages;
    private static final String mainPageName = "\\MainPage.html";
    private static final String startPageName = "\\StartPage.html";
    private static final String javaScriptFileName = "\\client.js";
    private static final String filePath = ".\\src";

}
