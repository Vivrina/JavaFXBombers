package ru.itis;

import ru.itis.sockets.GameServer;

public class ServerApp {

    public static void main(String[] args) {

        GameServer gameServer = GameServer.getInstance();
        gameServer.start();
    }
}
