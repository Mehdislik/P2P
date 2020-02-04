package client;

import controler.ClientControler;
import view.ClientView;
import java.io.IOException;
import javafx.application.Application;
import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.geometry.Orientation;
import java.util.ArrayList;
import javafx.util.Duration;

public class Client extends Application {

    private Stage primaryStage;

    public static void main(String[] args) {
        FSPClient client;
        String msg;

        client = new FSPClient("127.0.0.1", 50000);

        Application.launch(Client.class);
        /*
        client.connect();
        client.open();

        try {
            client.recevoirDescriptions(client.descriptionsFolder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        client.disconnect();
        client.close();
        */
    }

    @Override
    public void start(Stage stage) {
        //Group root;
        //root = new Group();
        ClientView view = new ClientView();
        Scene scene = new Scene(view, 400, 400, Color.WHITE);
        ClientControler controler = new ClientControler(view);

        primaryStage = stage;
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setTitle("File Sharing");

        // On affiche les éléments dans la fenêtre
        //view.root.getChildren().add(view.grid);
    }

    public ArrayList<String> samples() {
        ArrayList<String> a = new ArrayList<String>();
        a.add("localhost/truc.txt");
        a.add("poseidon/yojinbo");
        a.add("zeus/RAPPORT.pdf");

        return a;
    }
}

