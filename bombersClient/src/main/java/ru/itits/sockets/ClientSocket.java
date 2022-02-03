package ru.itits.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import ru.itis.controller.GameController;
import ru.itis.protocol.Message;
import ru.itis.protocol.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket extends Thread {

    private Socket clientSocket;

    private final String HOST = "localhost";
    private final int PORT = 8888;

    private PrintWriter out;
    private BufferedReader fromServer;

    private GameController gameController;

    public void connect(GameController gameController, String nickname) {
        try {
            this.gameController = gameController;
            clientSocket = new Socket(HOST, PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            fromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Message message = new Message();
            message.setType(MessageType.CONNECT);
            message.addHeader("nickname", nickname);
            sendMessage(message);
            this.start();

        } catch (IOException e) {
            throw new RuntimeException();
        }
    }




    public void sendMessage(Message message) {
        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(message);
            out.println(jsonMessage);
        } catch (JsonProcessingException e) {
        }
    }

    @Override
    public void run() {
        while (true) {
            String messageFromServer;
            Message message = null;
            try {
                messageFromServer = fromServer.readLine();
                message = new ObjectMapper().readValue(messageFromServer, Message.class);
            } catch (IOException e) {
            }
            switch (message.getType()) {
                case LOBBY: {
                    String code = message.getHeader("code");
                    String isReady = message.getHeader("isReady");
                    String role = message.getHeader("role");
                    String map = message.getHeader("map");
                    String enemyName = message.getHeader("enemyName");
                    String enemyCat = message.getHeader("enemyCat");

                    Platform.runLater(() -> gameController.startGame(code, map, role, enemyCat, enemyName));
                    break;
                }
                case CHAT: {
                    Label label = new Label();
                    label.setText(message.getBody());
                    label.setFont(Font.font("Arial"));
                    Platform.runLater(() -> gameController.getMessages().getChildren().add(label));
                    break;
                }
                case ACTION:
                    switch (message.getBody()) {
                        case("right"): {
                            ImageView enemy = gameController.getEnemy();
                            Platform.runLater(() -> gameController.goRight(enemy));
                            break;
                        }
                        case("left"): {
                            ImageView enemy = gameController.getEnemy();
                            Platform.runLater(() ->gameController.goLeft(enemy));
                            break;
                        }
                        case("up"): {
                            ImageView enemy = gameController.getEnemy();
                            Platform.runLater(() -> gameController.goUp(enemy));
                            break;
                        }
                        case("down"): {
                            ImageView enemy = gameController.getEnemy();
                            Platform.runLater(() ->gameController.goDown(enemy));
                            break;
                        }
                        case("bomb"): {
                            ImageView enemy = gameController.getEnemy();
                            Platform.runLater(() ->gameController.bomb(enemy));
                            break;
                        }
                        case("lose"): {
                            Platform.runLater(() ->gameController.lose());
                            break;
                        }
                        case("win"): {
                            Platform.runLater(() ->gameController.win());
                            break;
                        }

                    }
                    break;
            }
        }
    }
}
