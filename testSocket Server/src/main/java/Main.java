import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    static class Client implements Runnable {
        private Socket socket;
        PrintWriter out;

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            receiver();

        }

        public void send(String event, Object data) {
            try {
                JSONObject payload = new JSONObject();

                payload.put("event_name", event);
                payload.put("data", data);

                System.out.println("S: " + payload);
                out.println(payload.toString());
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        private void receiver() {
            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String line = "";
                    while ((line = in.readLine()) != null) {
                        JSONObject payload = new JSONObject(line);
                        System.out.println("R: " + payload);
                        this.onEvent(payload.getString("event_name"), payload.get("data"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JSONObject disconnected = new JSONObject();
                    try {
                        disconnected.put("user", nickname);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    clients.forEach(client -> client.send("disconnected", disconnected));
                    groups.forEach((s, clients1) -> clients1.removeIf(client -> client.nickname.equals(this.nickname)));
                }
            }).start();
        }

        public void onEvent(String event, Object data) throws JSONException {
            System.out.println("received event: " + event + " / Data: " + data);

            if (event.equals("message")) {
                JSONObject msgData = (JSONObject) data;
                groups.get(msgData.getString("group")).forEach(c -> {
                    try {
                        JSONObject messageData = new JSONObject();
                        messageData.put("user", nickname);
                        messageData.put("content", msgData.getString("content"));
                        c.send("message", messageData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } else if (event.equals("login")) {
                JSONObject loginData = (JSONObject) data;
                this.nickname = loginData.getString("nickname");
                clients.add(this);

                JSONObject connected = new JSONObject();
                connected.put("user", nickname);
                clients.forEach(client -> client.send("connected", connected));

            }

            if (event.equals("join")) {
                JSONObject joinData = (JSONObject) data;
                String group_name = joinData.getString("group_name");

                if (!groups.containsKey(group_name)) {
                    groups.put(group_name, new ArrayList<>());
                }
                groups.get(group_name).add(this);
            }

            if (event.equals("leave")) {
                JSONObject leaveData = (JSONObject) data;
                String group_name = leaveData.getString("group_name");

                if (groups.containsKey(group_name)) {
                    groups.get(group_name).removeIf(client -> client.nickname.equals(this.nickname));
                }
            }
        }

        public String nickname = "";

    }

    static List<Client> clients = new ArrayList<>();
    static Map<String, List<Client>> groups = new HashMap<>();

    public static void main(String[] args) throws IOException {
        int port = 2022;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket skt = serverSocket.accept();
                System.out.println("Server has connected with client         " + skt.getInetAddress());
                Client client = new Client(skt);
                new Thread(client).start();
            }
        } catch (
                IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


}
