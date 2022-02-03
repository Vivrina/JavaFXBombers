package ru.itis.sockets;

import ru.itis.clients.Client;
import ru.itis.lobbies.Lobby;
import ru.itis.protocol.Message;
import ru.itis.protocol.MessageType;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {
    private ServerSocket serverSocket;

    private List<Client> clients = new CopyOnWriteArrayList<>();

    private Map<String, Lobby> lobbies = new ConcurrentHashMap<>();

    private static GameServer gameServer;

    public static GameServer getInstance() {
        if (gameServer == null) {
            gameServer = new GameServer();
        }
        return gameServer;
    }

    private GameServer() {
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(8888);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Socket client = serverSocket.accept();
                            Client player = new Client(client);
                            clients.add(player);
                            player.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            //console log
        }
    }

    public List<Client> getClients() {
        return clients;
    }

    public Map<String, Lobby> getLobbies() {
        return lobbies;
    }

    public void setLobbies(Map<String, Lobby> lobbies) {
        this.lobbies = lobbies;
    }

    public void addNewLobby(String code, Client client){
        if(!lobbies.containsKey(code)) {
            Lobby lobby = new Lobby();
            lobby.enterLobby(client, "player1");

            this.lobbies.put(code, lobby);
        }else {
            lobbies.get(code).enterLobby(client, "player2");

            Client player1 = lobbies.get(code).getClients().get("player1");

            String mapToPlay = lobbies.get(code).getMapFromMapPool();

            Message message = new Message();
            message.setType(MessageType.LOBBY);
            message.addHeader("code", code);
            message.addHeader("isReady", "true");
            message.addHeader("role", "player2");
            message.addHeader("map", mapToPlay);
            message.addHeader("enemyName", player1.getName());
            message.addHeader("enemyCat", player1.getCat());
            client.sendMessage(message);

            Client player2 = lobbies.get(code).getClients().get("player2");
            Message mess = new Message();
            mess.setType(MessageType.LOBBY);
            mess.addHeader("code", code);
            mess.addHeader("isReady", "true");
            mess.addHeader("role", "player1");
            mess.addHeader("map", mapToPlay);
            mess.addHeader("enemyName", player2.getName());
            mess.addHeader("enemyCat", player2.getCat());

            player1.sendMessage(mess);
        }
    }

    public void removeLobby(String code){
        lobbies.remove(code);
    }
}
