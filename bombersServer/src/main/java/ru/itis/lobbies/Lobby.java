package ru.itis.lobbies;

import ru.itis.clients.Client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

public class Lobby {
    private HashMap<String, Client> clients;
    private boolean isReady;

    public Lobby(){
        this.clients = new HashMap<>();
        this.isReady = false;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public HashMap<String, Client> getClients() {
        return clients;
    }

    public void setClients(HashMap<String, Client> clients) {
        this.clients = clients;
    }

    public void enterLobby(Client client, String role){
        this.clients.put(role, client);
    }

    public String getMapFromMapPool(){
        Random random = new Random();
        ArrayList<String> mapPool = new ArrayList<>();
        Set<String> roles = clients.keySet();
        for(String role : roles) {
            mapPool.add(clients.get(role).getMap());
        }
        return mapPool.get(random.nextInt(mapPool.size()));
    }

}
