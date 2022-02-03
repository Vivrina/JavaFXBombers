package ru.itis.clients;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.itis.protocol.Message;
import ru.itis.sockets.GameServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client extends Thread {

    private String nickname;

    private String cat;

    private String map;

    private Socket client;

    private PrintWriter toClient;

    private BufferedReader fromClient;

    private GameServer gameServer;

    public Client(Socket client) {
        gameServer = GameServer.getInstance();
        this.client = client;
        try {
            // обернули потоки байтов в потоки символов
            this.toClient = new PrintWriter(client.getOutputStream(), true);
            this.fromClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void run() {
        while (true) {
            String messageFromClient;

            try {
                messageFromClient = fromClient.readLine();
                if (messageFromClient != null) {
                    System.out.println(nickname + ": " + messageFromClient);

                    Message message = new ObjectMapper().readValue(messageFromClient, Message.class);
                    System.out.println(message.getHeaders());

                    switch (message.getType()) {
                        case LOBBY: {
                            if(!message.getHeaders().containsKey("end")) {
                                this.cat = message.getHeader("cat");
                                System.out.println(cat);
                                this.map = message.getHeader("map");
                                System.out.println(map);
                                String code = message.getHeader("code");
                                gameServer.addNewLobby(code, this);
                            }else{
                                String code = message.getHeader("end");
                                gameServer.removeLobby(code);
                            }
                            break;
                        }
                        case CONNECT: {
                            this.nickname = message.getHeader("nickname");
                            break;
                        }
                        case CHAT: {
                            List<Client> clients = gameServer.getClients();
                            message.setBody(nickname + ": " + message.getBody());

                            for (Client client: clients) {
                                if (this != client){
                                    System.out.println(client.nickname);
                                    client.sendMessage(message);
                                }
                            }
                            break;
                        }
                        case ACTION: {
                            List<Client> clients = new ArrayList<>(gameServer.getClients());
                            System.out.println(clients.toString());
                            clients.remove(this);
                            for (Client client: clients) {
                                client.sendMessage(message);
                            }
                            break;
                        }

                    }

                }
            } catch (IOException e) {
                //
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            System.out.println("SEND messageTO: " + nickname + " " + jsonMessage);
            toClient.println(jsonMessage);
        } catch (JsonProcessingException e) {
            //console log
        }
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "Client{" +
                "nickname='" + nickname + '\'' +
                ", cat='" + cat + '\'' +
                ", map='" + map + '\'' +
                ", client=" + client +
                ", toClient=" + toClient +
                ", fromClient=" + fromClient +
                ", gameServer=" + gameServer +
                '}';
    }
}
