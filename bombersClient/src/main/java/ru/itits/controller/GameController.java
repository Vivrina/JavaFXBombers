package ru.itits.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.itis.enums.Action;
import ru.itis.models.Cell;
import ru.itis.models.GameMap;
import ru.itis.models.MapPool;
import ru.itis.protocol.Message;
import ru.itis.protocol.MessageType;
import ru.itis.sockets.ClientSocket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GameController implements Initializable {
    private ClientSocket clientSocket;

    private ArrayList<Cell> bombs = new ArrayList<>();
    private static List<String> bombImages =
            Arrays.asList("src/main/resources/img/bombs/bomba1.png", "src/main/resources/img/bombs/bomba2.png",
                    "src/main/resources/img/bombs/bomba3.png", "src/main/resources/img/bombs/bomba4.png",
                    "src/main/resources/img/bombs/bomba5.png", "src/main/resources/img/bombs/bomba6.png",
                    "src/main/resources/img/bombs/bomba7.png", "src/main/resources/img/bombs/bomba8.png",
                    "src/main/resources/img/bombs/bomba9.png", "src/main/resources/img/bombs/bomba10.png");

    private String playerCat;

    private String playerUsername;

    private String pickedMap;

    private String lobbyCode;

    public String getLobbyCode() {
        return lobbyCode;
    }

    public void setLobbyCode(String lobbyCode) {
        this.lobbyCode = lobbyCode;
    }

    public void setPlayerUsername(String playerUsername) {
        this.playerUsername = playerUsername;
    }

    public void setPickedMap(String pickedMap) {
        this.pickedMap = pickedMap;
    }

    public void setPlayerCat(String playerCat) {
        this.playerCat = playerCat;
    }


    @FXML
    public AnchorPane gameZone;

    @FXML
    public HBox game;

    private ImageView player = new ImageView();

    private ImageView enemy = new ImageView();

    private GameMap gameMap = new GameMap(Enum.valueOf(MapPool.class , "CASTLE"));

    public ImageView getPlayer() {
        return player;
    }

    public void setPlayer(ImageView player) {
        this.player = player;
    }

    public ImageView getEnemy() {
        return enemy;
    }

    public void setEnemy(ImageView enemy) {
        this.enemy = enemy;
    }

    @FXML
    public GridPane gameTable;

    @FXML
    private ScrollPane messagesArea1;

    @FXML
    private VBox messages1;

    @FXML
    private VBox messageControl1;

    @FXML
    private TextField messageText1;

    @FXML
    private Button sendMessageButton1;

    @FXML
    public StackPane waitingBlock;

    @FXML
    public ImageView loadingImage;

    @FXML
    public ImageView bg0;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadingImage.setImage(new Image(new FileInputStream("src/main/resources/img/gif/loading.gif")));
            bg0.setImage(new Image(new FileInputStream("src/main/resources/img/bg5.png")));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        game.setVisible(false);
        waitingBlock.setVisible(true);
        messageControl1.setVisible(true);
        gameZone.requestFocus();
    }

    public void doConnect(){
        clientSocket = new ClientSocket();
        clientSocket.connect(this, playerUsername);
    }

    public void doLobbyConnect(){
        Message message = new Message();
        message.setType(MessageType.LOBBY);
        message.addHeader("code", lobbyCode);
        message.addHeader("isReady", "false");
        message.addHeader("cat", playerCat);
        message.addHeader("map", pickedMap);
        clientSocket.sendMessage(message);
    }

    public void startGame(String code, String map, String role, String enemyCat, String enemyUsername){
        MapPool mapName = Enum.valueOf(MapPool.class , map);
        gameMap = new GameMap(mapName);
        waitingBlock.setVisible(false);

        try {
            buildMap();
            setImages(enemyCat);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        putPlayers(role);

        game.setVisible(true);
        game.requestFocus();
        gameTable.requestFocus();

        try {
            player.setFitHeight(90.00);
            player.setFitWidth(90.00);
            player.setImage(new Image(new FileInputStream(playerCat)));

            enemy.setFitHeight(90.00);
            enemy.setFitWidth(90.00);
            enemy.setImage(new Image(new FileInputStream(enemyCat)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


    }

    public void buildMap() throws FileNotFoundException {
        for(int i = 0; i < 12; i++){
            for(int j = 0; j < 12; j++) {
                Cell cell = new Cell(i, j);
                ImageView bg = new ImageView();
                bg.setImage(new Image((new FileInputStream(gameMap.getMap().getBgSkin()))));
                bg.setFitHeight(90.00);
                bg.setFitWidth(90.00);
                gameTable.add(bg, cell.getColumn(), cell.getRow());
                if(gameMap.getEdges().contains(cell)){
                    ImageView border = new ImageView();
                    border.setImage(new Image((new FileInputStream(gameMap.getMap().getBorderSkin()))));
                    border.setFitHeight(90.00);
                    border.setFitWidth(90.00);
                    gameTable.add(border, cell.getColumn(), cell.getRow());
                }
            }
        }
    }

    public void setImages(String enemyCat) throws FileNotFoundException {
        player.setFitHeight(90.00);
        player.setFitWidth(90.00);
        player.setImage(new Image(new FileInputStream(playerCat)));

        enemy.setFitHeight(90.00);
        enemy.setFitWidth(90.00);
        enemy.setImage(new Image(new FileInputStream(enemyCat)));
    }

    public void putPlayers(String role){
        Cell spawnPlayer = null;
        Cell spawnEnemy = null;
        if(role.equals("player1")){
            spawnPlayer = new Cell(gameMap.getMap().getSpawnOne().getColumn(), gameMap.getMap().getSpawnOne().getRow());
            spawnEnemy = new Cell(gameMap.getMap().getSpawnTwo().getColumn(), gameMap.getMap().getSpawnTwo().getRow());
        }else{
            spawnEnemy = new Cell(gameMap.getMap().getSpawnOne().getColumn(), gameMap.getMap().getSpawnOne().getRow());
            spawnPlayer = new Cell(gameMap.getMap().getSpawnTwo().getColumn(), gameMap.getMap().getSpawnTwo().getRow());
        }
        gameTable.add(player, spawnPlayer.getColumn(), spawnPlayer.getRow());
        gameTable.add(enemy, spawnEnemy.getColumn(), spawnEnemy.getRow());
    }

    private final EventHandler<KeyEvent> playerControlEvent = event -> {
        switch (event.getCode()) {
            case W: {
                goUp(player);

                Message message = new Message();
                message.setType(MessageType.ACTION);
                message.setBody(Action.UP.getTitle());
                clientSocket.sendMessage(message);

                break;
            }
            case A: {
                goLeft(player);

                Message message = new Message();
                message.setType(MessageType.ACTION);
                message.setBody(Action.LEFT.getTitle());
                clientSocket.sendMessage(message);

                break;
            }
            case S: {
                goDown(player);

                Message message = new Message();
                message.setType(MessageType.ACTION);
                message.setBody(Action.DOWN.getTitle());
                clientSocket.sendMessage(message);

                break;
            }
            case D: {
                goRight(player);

                Message message = new Message();
                message.setType(MessageType.ACTION);
                message.setBody(Action.RIGHT.getTitle());
                clientSocket.sendMessage(message);

                break;
            }
            case ENTER: {
                bomb(player);

                Message message = new Message();
                message.setType(MessageType.ACTION);
                message.setBody(Action.BOMB.getTitle());
                clientSocket.sendMessage(message);

                break;
            }

        }
    };

    public void goLeft(ImageView gamer) {
        if(!gameMap.getMap().getBlockIndexes().contains(new Cell((GridPane.getColumnIndex(gamer) - 1), GridPane.getRowIndex(gamer)))) {
            GridPane.setColumnIndex(gamer, GridPane.getColumnIndex(gamer) - 1);
        }
    }

    public void goRight(ImageView gamer) {
        if(!gameMap.getMap().getBlockIndexes().contains(new Cell((GridPane.getColumnIndex(gamer) + 1), GridPane.getRowIndex(gamer)))) {
            GridPane.setColumnIndex(gamer, GridPane.getColumnIndex(gamer) + 1);
        }
    }

    public void goUp(ImageView gamer) {
        if(!gameMap.getMap().getBlockIndexes().contains(new Cell(GridPane.getColumnIndex(gamer), (GridPane.getRowIndex(gamer)-1)))) {
            GridPane.setRowIndex(gamer, GridPane.getRowIndex(gamer) - 1);
        }
    }

    public void goDown(ImageView gamer) {
        if(!gameMap.getMap().getBlockIndexes().contains(new Cell(GridPane.getColumnIndex(gamer), (GridPane.getRowIndex(gamer)+1)))) {
            GridPane.setRowIndex(gamer, GridPane.getRowIndex(gamer) + 1);
        }
    }

    public void bomb(ImageView gamer) {
        Cell cell = new Cell(GridPane.getColumnIndex(gamer), GridPane.getRowIndex(gamer));
        if (!bombs.contains(cell)) {
            ImageView bomb = new ImageView();
            try {
                bomb.setImage(new Image((new FileInputStream(String.valueOf(bombImages.get(0))))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            bomb.setFitHeight(90.00);
            bomb.setFitWidth(90.00);
            ImageView pl = gamer;
            gameTable.getChildren().remove(gamer);
            gameTable.add(bomb, cell.getColumn(), cell.getRow());
            gameTable.add(pl, cell.getColumn(), cell.getRow());
            startBombAnimation(bomb);
        }
    }

    void startBombAnimation (ImageView bomb){
        AtomicInteger i = new AtomicInteger(0);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.2), animation -> {
            i.getAndIncrement();
            try {
                bomb.setImage(new Image(new FileInputStream(bombImages.get(i.get()))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (i.get() == 8) {
                if (checkDead(GridPane.getColumnIndex(player), GridPane.getRowIndex(player),
                        GridPane.getColumnIndex(bomb), GridPane.getRowIndex(bomb))) {
                    try {
                        bomb.setImage(new Image(new FileInputStream(bombImages.get(9))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    lose();
                }
                if (checkDead(GridPane.getColumnIndex(enemy), GridPane.getRowIndex(enemy),
                        GridPane.getColumnIndex(bomb), GridPane.getRowIndex(bomb))) {
                    gameTable.getChildren().remove(enemy);
                    try {
                        bomb.setImage(new Image(new FileInputStream(bombImages.get(9))));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    win();
                }
            }

            if (i.get() == 9) {
                gameTable.getChildren().remove(bomb);
            }
        }));
        timeline.setCycleCount(9);
        timeline.play();
    }

    public void win(){


        gameZone.getScene().removeEventFilter(KeyEvent.KEY_PRESSED,getPlayerControlEvent());
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.setType(MessageType.ACTION);
        message.setBody(Action.LOSE.getTitle());
        clientSocket.sendMessage(message);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/End.fxml"));
        Stage stage=(Stage) gameZone.getScene().getWindow();
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        EndController endController = fxmlLoader.getController();
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        endController.changeBG("src/main/resources/img/win.png");
        stage.show();

    }

    public void lose(){
        gameTable.getChildren().remove(player);

        gameZone.getScene().removeEventFilter(KeyEvent.KEY_PRESSED,getPlayerControlEvent());
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Message message = new Message();
        message.setType(MessageType.ACTION);
        message.setBody(Action.WIN.getTitle());
        clientSocket.sendMessage(message);

        Message mess = new Message();
        mess.setType(MessageType.LOBBY);
        message.addHeader("end", lobbyCode);
        clientSocket.sendMessage(mess);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/End.fxml"));
        Stage stage=(Stage) gameZone.getScene().getWindow();
        Parent root = null;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root);
        EndController endController = fxmlLoader.getController();
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        endController.changeBG("src/main/resources/img/lose.png");
        stage.show();
    }

    boolean checkDead(int x1, int y1, int x2, int y2){
        if((x1==x2 && y1==y2) || (x1+1==x2 && y1==y2) || (x1-1==x2 && y1==y2) || (x1==x2 && y1+1==y2) || (x1==x2 && y1-1==y2)){
            return true;
        }
        return false;
    }




    @FXML
    private void sendMessage() {
        String chatText = messageText1.getText();
        messageText1.clear();
        Message message = new Message();
        message.setType(MessageType.CHAT);
        message.setBody(chatText);
        clientSocket.sendMessage(message);
        Label label = new Label();
        //label.setStyle("-fx-font: Corier new");
        label.setText("Я: " + chatText);

        messages1.getChildren().add(label);
    }

    public VBox getMessages() {
        return messages1;
    }

    public EventHandler<KeyEvent> getPlayerControlEvent() {
        return playerControlEvent;
    }
}
