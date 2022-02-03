package ru.itits.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ru.itis.sockets.ClientSocket;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.*;

public class MainController implements Initializable {
    private String cur_image;
    private String cur_map;
    private String username;
    private String lobbyCode;

    private ClientSocket clientSocket;

    @FXML
    public Button mainButton;

    @FXML
    public Button  enter_lobby;

    @FXML
    private Button rightButton;

    @FXML
    private Button leftButton;

    @FXML
    private VBox CatV;

    @FXML
    private HBox CatH;

    @FXML
    private VBox MapV;

    @FXML
    private HBox MapH;

    @FXML
    private VBox lobby;

    @FXML
    private ImageView bg;

    @FXML
    private ImageView cat;

    @FXML
    private ImageView map;

    @FXML
    public StackPane menu;

    @FXML
    private VBox info;

    @FXML
    private TextField nameField;

    @FXML
    private TextField codeField;



    private static final List<String> cats =
            Arrays.asList("src/main/resources/img/cat/fat1.png", "src/main/resources/img/cat/fat2.png"
                    , "src/main/resources/img/cat/fat3.png", "src/main/resources/img/cat/fat4.png");

    private static final List<String> maps =
            Arrays.asList("src/main/resources/img/maps/map1.png", "src/main/resources/img/maps/map2.png"
                    , "src/main/resources/img/maps/map3.png", "src/main/resources/img/maps/map4.png");

    private static final Map<String, String> mapsHashMap = new HashMap<String, String>() {{
        put("src/main/resources/img/maps/map1.png", "CASTLE");
        put("src/main/resources/img/maps/map2.png", "SAND");
        put("src/main/resources/img/maps/map3.png", "NEON");
        put("src/main/resources/img/maps/map4.png", "JUNGLE");
    }};



    @FXML
    public void pressStartButton(ActionEvent event) throws Exception {
        bg.setImage(new Image(new FileInputStream("src/main/resources/img/bg2.png")));
        mainButton.setVisible(false);
        enter_lobby.setVisible(false);
        info.setVisible(true);
    }

    @FXML
    public void pressSetCodeAndUsername(ActionEvent event) throws Exception {
        bg.setImage(new Image(new FileInputStream("src/main/resources/img/bg2.png")));
        lobbyCode = codeField.getText();
        username = nameField.getText();
        System.out.println(lobbyCode);
        System.out.println(username);
        info.setVisible(false);
        CatV.setVisible(true);
    }


    @FXML
    public void rightCharacter(ActionEvent event) throws Exception {
        int index = cats.indexOf(cur_image);
        if(index<cats.size()-1){
            index++;
            cur_image = cats.get(index);
            cat.setImage(new Image(new FileInputStream(cur_image)));
        }else{
            cur_image = cats.get(0);
            cat.setImage(new Image(new FileInputStream(cur_image)));
        }
    }

    @FXML
    public void leftCharacter(ActionEvent event) throws Exception {
        int index = cats.indexOf(cur_image);
        if(index>0){
            index--;
            cur_image = cats.get(index);
            cat.setImage(new Image(new FileInputStream(cur_image)));
        }else{
            cur_image = cats.get(cats.size()-1);
            cat.setImage(new Image(new FileInputStream(cur_image)));
        }
    }


    @FXML
    public void pressFirstReadyButton(ActionEvent event) throws Exception {
        bg.setImage(new Image(new FileInputStream("src/main/resources/img/bg3.png")));
        CatV.setVisible(false);
        MapV.setVisible(true);
    }

    @FXML
    public void rightMap(ActionEvent event) throws Exception {
        int index = maps.indexOf(cur_map);
        if(index<maps.size()-1){
            index++;
            cur_map = maps.get(index);
            map.setImage(new Image(new FileInputStream(cur_map)));
        }else{
            cur_map = maps.get(0);
            map.setImage(new Image(new FileInputStream(cur_map)));
        }
    }

    @FXML
    public void leftMap(ActionEvent event) throws Exception {
        int index = maps.indexOf(cur_map);
        if(index>0){
            index--;
            cur_map = maps.get(index);
            map.setImage(new Image(new FileInputStream(cur_map)));
        }else{
            cur_map = maps.get(maps.size()-1);
            map.setImage(new Image(new FileInputStream(cur_map)));
        }
    }

    @FXML
    public void pressTwoReadyButton (ActionEvent event) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
        Node node=(Node) event.getSource();
        Stage stage=(Stage) node.getScene().getWindow();
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        GameController gameController = fxmlLoader.getController();
        System.out.println(username + " " + lobbyCode + " " + cur_image + " " + cur_map);
        gameController.setPlayerUsername(username);
        gameController.setPlayerUsername(lobbyCode);
        gameController.setPlayerCat(cur_image);
        gameController.setPickedMap(mapsHashMap.get(cur_map));
        gameController.setLobbyCode(lobbyCode);
        gameController.doConnect();
        gameController.doLobbyConnect();
        scene.setOnKeyPressed(gameController.getPlayerControlEvent());
        stage.show();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        menu.setVisible(true);
        CatV.setVisible(false);
        CatV.setSpacing(50);
        CatH.setSpacing(200);
        MapV.setVisible(false);
        MapV.setSpacing(50);
        MapH.setSpacing(200);
        lobby.setVisible(true);
        lobby.setSpacing(10);
        info.setVisible(false);
        try {
            cur_image = "src/main/resources/img/cat/fat1.png";
            cur_map = "src/main/resources/img/maps/map1.png";


            bg.setImage(new Image(new FileInputStream("src/main/resources/img/bg1.png")));
            cat.setImage(new Image(new FileInputStream(cur_image)));
            map.setImage(new Image(new FileInputStream(cur_map)));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}