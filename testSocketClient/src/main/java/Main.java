import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public Socket skt;
    public PrintWriter out;
    public BufferedReader in;
    public String nick;
    public String group;


    public Main(String nick, String group, Socket skt) throws IOException, JSONException {
        this.skt = skt;
        this.group = group;
        this.nick = nick;


        this.out = new PrintWriter(skt.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(skt.getInputStream()));

        JSONObject loginData = new JSONObject();
        loginData.put("nickname", nick);
        this.send("login", loginData);

//        JSONObject joinData = new JSONObject();
//        joinData.put("group_name", group);
//        this.send("join", joinData);


        this.receiver();

        while (true) {
            String msg = new Scanner(System.in).nextLine();
            String[] command = msg.split(" ");
            String commandName = command[0].replace("/", "");
            JSONArray arguments = new JSONArray();
            for (int i = 1; i < command.length; i++) {
                arguments.put(command[i]);
            }

            Commands commands = Commands.getEnum(commandName);
            if (commands == null) {
                System.out.println("invalid command");
                continue;
            }
            System.out.println("command: '" + commands.ordinal() + "' / Argument: " + arguments);
            JSONObject cmdData = new JSONObject();
            cmdData.put("cmd", commands.ordinal());
            cmdData.put("args", arguments);
            this.send("/cmd", cmdData);
        }
    }

    public void send(String event, Object data) {
        try {
            JSONObject payload = new JSONObject();

            payload.put("event_name", event);
            payload.put("data", data);

            out.println(payload.toString());
            out.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void receiver() {
        new Thread(() -> {
            try {
                String line = "";
                while ((line = in.readLine()) != null) {
                    JSONObject payload = new JSONObject(line);
                    this.onEvent(payload.getString("event_name"), payload.get("data"));
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }).start();
    }

    public void onEvent(String event, Object data) throws JSONException {
        System.out.println("received event: " + event + " / Data: " + data);

        if (event.equals("message")) {
            JSONObject messageData = (JSONObject) data;
            System.out.println("MESSAGE: " + messageData.getString("user") + ": " + messageData.getString("content"));
        }

        if (event.equals("connected")) {
            JSONObject connected = (JSONObject) data;
            System.out.println("connected nwu user " + " " + connected.getString("user"));
        }

        if (event.equals("disconnected")) {
            JSONObject disconnected = (JSONObject) data;
            System.out.println("disconnected user " + " " + disconnected.getString("user"));
        }
    }

    public static BufferedReader br;

    public static void main(String[] args) throws IOException {
        br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter nick:");
        String nick = br.readLine();
//        System.out.println("Enter group:");
//        String group = br.readLine();

        String hostname = "127.0.0.1";
        int port = 2022;

        try {
            Socket skt = new Socket(hostname, port);
            System.out.println("Client has connected with server " + hostname + ":" + port);
            new Main(nick, null, skt);
        } catch (IOException | JSONException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }

    }
}
