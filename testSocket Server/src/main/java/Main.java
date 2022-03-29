import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    static class Client implements Runnable {
        private Socket socket;
        PrintWriter out;

        Validation validation = new Validation();

        public Client(Socket socket) throws IOException {
            this.socket = socket;
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        @Override
        public void run() {
            receiver();
            send("answer", "/" + Commands.getEnumByValue(0));
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
                        this.onEvent(payload.getString("event_name"), payload.has("data") ? payload.get("data") : null);
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

        List<String> auto = new ArrayList<>();


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

            } else if (event.equals("join")) {
                JSONObject joinData = (JSONObject) data;
                String group_name = joinData.getString("group_name");

                if (!groups.containsKey(group_name)) {
                    groups.put(group_name, new ArrayList<>());
                }
                groups.get(group_name).add(this);
            } else if (event.equals("leave")) {
                JSONObject leaveData = (JSONObject) data;
                String group_name = leaveData.getString("group_name");

                if (groups.containsKey(group_name)) {
                    groups.get(group_name).removeIf(client -> client.nickname.equals(this.nickname));
                }
            } else if (event.equals("/cmd")) {


                JSONObject jsonData = (JSONObject) data;
                Commands command = Commands.getEnumByValue(jsonData.getInt("cmd"));
                JSONArray args = jsonData.getJSONArray("args");
                System.err.println(command);

                if (command == Commands.grancel_nor_meqena) {
                    if (args.length() != 2) {
                        send("answer", "invalid command");
                        send("answer", "/" + Commands.getEnumByValue(0));
                        return;
                    }

                    String autoNumber = args.getString(0);
                    String phoneNumber = args.getString(1);

                    if (validation.isValidAutoNumber(autoNumber) == false) {
                        send("error_message", "Error Invalid autoNumber ");
                        return;
                    }
                    if (validation.isValidPhoneNumber(phoneNumber) == false) {
                        send("error_message", "Error Invalid phoneNumber ");
                        return;
                    }


                    auto.add((autoNumber) + "|" + (phoneNumber));
                    send("answer", "your car has been added");

                    send("answer", "/" + Commands.getEnumByValue(2));
                    send("answer", "/" + Commands.getEnumByValue(3));
                    send("answer", "/" + Commands.getEnumByValue(0));

                } else if (command == Commands.tesnel_bolor_meqenaner) {
                    if (auto.isEmpty()) {
                        send("auto", "you don't have a car");
                        send("answer", "/" + Commands.getEnumByValue(0));

                    } else {
                        auto.forEach(m -> {
                            send("auto", m);
                        });
                        send("answer", "/" + Commands.getEnumByValue(1) + "<autoNumber> <phoneNumber>");
                        send("answer", "/" + Commands.getEnumByValue(3) + "<phoneNumber>");
                    }

                } else if (command == Commands.heraxosa_hamarov_gtnel) {
                    List<String> phoneNumber = new ArrayList<>();

                    auto.forEach(m -> {
                        System.err.println(m.split("\\|")[0]);
                        System.err.println(m.split("\\|")[1]);
                        try {
                            if (m.split("\\|")[1].equals(args.get(0)))
                                phoneNumber.add(m);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                    if (phoneNumber.isEmpty()) {
                        send("answer", "not found");
                        send("answer", "/" + Commands.getEnumByValue(0));
                        return;
                    }
                    send("answer", "/" + Commands.getEnumByValue(1) + " <autoNumber> <phoneNumber>");
                    send("answer", "/" + Commands.getEnumByValue(2));
                    phoneNumber.forEach(auto -> {
                        send("answer", auto);

                    });
                } else if (command == Commands.glxavor_menu) {

                    send("answer", "/" + Commands.getEnumByValue(1) + " <autoNumber> <phoneNumber>");
                    send("answer", "/" + Commands.getEnumByValue(2));
                    send("answer", "/" + Commands.getEnumByValue(3) + "<phoneNumber>");
                } else
                    send("answer", "invalid command");
            } else
                send("answer", "invalid event");

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
                System.out.println("Server has connected with client" + skt.getInetAddress());
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
