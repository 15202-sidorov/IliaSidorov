import com.sun.net.httpserver.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;


public class Server {
    private static HashMap<Integer ,User> onlineUsers;
    private static HashMap<Integer, Boolean> usersRefreshFlags;
    private static LinkedList<MessageInfo> messages;
    private static final String mainPageName = "\\MainPage.html";
    private static final String startPageName = "\\StartPage.html";
    private static final String javaScriptFileName = "\\client.js";
    private static final String filePath = ".\\src";

    public static void main( String[] args ) {
        try {
            System.out.println("Starting server...");
            System.out.println("Local address : " + InetAddress.getLocalHost().getHostAddress());
            HttpServer server = HttpServer.create( new InetSocketAddress(8000), 0);
            onlineUsers = new HashMap<>();
            usersRefreshFlags = new HashMap<>();
            messages = new LinkedList<>();

            Timer refreshUsersTimer = new Timer();
            refreshUsersTimer.scheduleAtFixedRate(new CheckOnlineUsers(), 5000, 5000);

            server.createContext("/", new RootHandler());
            server.createContext("/login", new LoginHandler());
            server.createContext("/main", new getMainPageHandler());
            server.createContext("/client.js", new getJS());
            server.createContext("/logout", new LogoutHandler());
            server.createContext("/users", new getUsersHandler());
            server.createContext("/messages", new MsgHandler());

            server.setExecutor(null);
            server.start();
        }
        catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    static class CheckOnlineUsers extends TimerTask {
        @Override
        public void run() {
            for( Integer token : usersRefreshFlags.keySet() ) {
                if ( usersRefreshFlags.get(token) ) {
                    usersRefreshFlags.replace(token, false);
                }
                else {
                    System.out.println("No ping from user " + onlineUsers.get(token)+ ",removing...");
                    onlineUsers.remove(token);
                    usersRefreshFlags.remove(token);
                }
            }
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle( HttpExchange exchange ) throws IOException{
            if ( exchange.getRequestMethod().equals("POST")) {
                try {
                    System.out.println("Login received.");
                    Headers head = exchange.getResponseHeaders();
                    String JSONReceived = getRequestBody(exchange);
                    JSONObject jsonObject = new JSONObject(JSONReceived);
                    String userName = jsonObject.getString("username");

                    if (onlineUsers.containsKey(userName.hashCode())) {
                        System.out.println("User name" + userName + " is already in use...");
                        head.add("WWW-Authenticate", "Token realm='UserName is already in use'");
                        exchange.sendResponseHeaders(401, 0);
                        return;
                    }

                    User newUser = new User(userName);
                    onlineUsers.put(newUser.getToken(), newUser);
                    usersRefreshFlags.put(newUser.getToken(), true);
                    System.out.println("User is trying to log in : " + userName);
                    jsonObject.append("id", newUser.getId());
                    jsonObject.append("online", newUser.isOnline());
                    jsonObject.append("token", newUser.getToken());

                    byte[] data = jsonObject.toString().getBytes();
                    head.add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, data.length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(data);
                    os.close();
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }

    }

    static class getJS implements HttpHandler {
        @Override
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
        @Override
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
        @Override
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
        @Override
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                try {


                //parsing client's request
                Headers incomingHeaders = exchange.getRequestHeaders();
                int token  = getAuthenticToken(incomingHeaders);
                System.out.println("Token to logout : " + token);
                System.out.println("User is logging out : " + onlineUsers.get(token).getUserName());
                onlineUsers.remove(token);
                usersRefreshFlags.remove(token);
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
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    static class getUsersHandler implements HttpHandler {
        @Override
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {
                try {


                Headers incomingHeaders = exchange.getRequestHeaders();
                int token = getAuthenticToken(incomingHeaders);
                usersRefreshFlags.replace(token, true);
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
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }
    }

    static class MsgHandler implements HttpHandler {
        @Override
        public void handle( HttpExchange exchange ) throws IOException {
            if ( exchange.getRequestMethod().equals("GET") ) {

                Headers incomingHeaders = exchange.getRequestHeaders();
                int token = getAuthenticToken(incomingHeaders);
                usersRefreshFlags.replace(token, true);
                if ( !onlineUsers.containsKey(token) ) {
                    return;
                }

                try {
                    int offset = Integer.parseInt(incomingHeaders.getFirst("offset"));
                    int temp = Integer.parseInt(incomingHeaders.getFirst("count"));
                    int count;
                    if(temp > 100){
                        count = 100;
                    } else {
                        count = temp;
                    }
                    LinkedList<MessageInfo> list = getListMessage(messages, offset, count);

                    JSONArray array = new JSONArray();

                    for (MessageInfo msg : list ) {
                        JSONObject JSONUser = new JSONObject();
                        System.out.println("send: " + msg.getMessage());
                        JSONUser.put("id", msg.getId());
                        JSONUser.put("message", msg.getMessage());
                        JSONUser.put("author", msg.getAuthor());
                        array.put(JSONUser);
                    }

                    JSONObject jsonToSend = new JSONObject();
                    jsonToSend.put("messages", array);
                    byte[] data = jsonToSend.toString().getBytes();

                    Headers resHeaders = exchange.getResponseHeaders();
                    resHeaders.add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, data.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(data);
                    os.close();

                } catch (JSONException e){
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, 0);
                }



            }

            if ( exchange.getRequestMethod().equals("POST") ) {
                Headers head = exchange.getRequestHeaders();
                int token = getAuthenticToken(head);
                if ( !onlineUsers.containsKey(token) ) {
                    return;
                }
                String JSONReceived = getRequestBody(exchange);
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(JSONReceived);
                    String message = jsonObject.getString("message");

                    messages.add(new MessageInfo(messages.size() -1, message, onlineUsers.get(token).getUserName()));
                    JSONObject jsonToSend = new JSONObject();
                    jsonToSend.put("id", messages.size() -1);
                    jsonToSend.put("message", message);
                    byte[] data = jsonToSend.toString().getBytes();

                    Headers resHead = exchange.getResponseHeaders();
                    resHead.add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, data.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(data);
                    os.close();

                } catch (JSONException e){
                    e.printStackTrace();
                    exchange.sendResponseHeaders(500, 0);
                }
            }

        }
    }

    private static class MessageInfo{
        private int id;
        private String message;
        private String author;
        MessageInfo(int id, String message, String author){
            this.id = id;
            this.message = message;
            this.author = author;
        }
        int getId() {
            return id;
        }
        String getMessage() {
            return message;
        }
        String getAuthor() {
            return author;
        }

    }




    private static int getAuthenticToken( Headers head ) {
        return Integer.parseInt(head.getFirst("Authorization").split(" ")[1]);
    }

    private static String getDocument(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String line;
        while ( (line = br.readLine()) != null ) {
            sb.append(line);
        }

        return sb.toString();
    }

    private static LinkedList<MessageInfo> getListMessage(LinkedList<MessageInfo> list, int offset, int count){
        int max;
        LinkedList<MessageInfo> newList = new LinkedList<>();
        if(list.size() < count){
            max = list.size();
        }else {
            if (count < list.size() - offset) {
                max = list.size() - offset;
            } else {
                max = count;
            }
        }
        for(int i = offset; i < max; ++i){
            newList.add(list.get(i));
        }
        return newList;
    }

    private static String getRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
        BufferedReader br = new BufferedReader(reader);
        StringBuilder builder = new StringBuilder();
        String line;

        while ( (line = br.readLine()) != null ) {
            builder.append(line);
        }

        return builder.toString();

    }





}