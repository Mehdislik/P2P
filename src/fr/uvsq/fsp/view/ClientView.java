package fr.uvsq.fsp.view;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ClientView extends Group {

	/**
	 * Button used to query the server.
	 * A keyword is sent, and the server tries to find files that matches the keyword.
	 */
	public Button downloadButton;

	/**
	 * Button to query the server.
	 */
	public Button searchButton;

	/**
	 * Button to open a socket with the server
	 */
	public Button refreshConnectionButton;

	/**
	 * Button to upload a file for sharing
	 */
	public Button uploadButton;

	/**
	 * Button for sending the description of an uploaded file
	 */
	public Button sendDescriptionButton;

	/**
	 * List showing the results/files sent by the server.
	 */
	public ListView<String> fileList;
	public ListView<String> sharedList;
	public ListView<String> downloadList;

	/**
	 * Receives the keyword to be sent to the server.
	 */
	public TextField searchField;
	public TextField serverIPField;
	public TextField portField;

	/**
	 * Label that displays any kind of information:
	 * Connection, download, upload
	 */
	public Label messageLabel;

	public TextArea descriptionArea;


	public ClientView() {
		GridPane grid = new GridPane();

		getStylesheets().add("stylesheet.css");

		// Champ de l'adresse du serveur central
		serverIPField = new TextField();
		serverIPField.getStyleClass().add("removeLightGlow");
		serverIPField.getStyleClass().add("searchField");
		serverIPField.getStyleClass().add("littleFont");
		serverIPField.setPrefWidth(70);
		serverIPField.setPrefHeight(10);
		serverIPField.setPromptText("l'@IP du serveur");

		// Champ du port
		portField = new TextField();
		portField.getStyleClass().add("removeLightGlow");
		portField.getStyleClass().add("searchField");
		portField.getStyleClass().add("littleFont");
		portField.setPrefWidth(50);
		portField.setPrefHeight(10);
		portField.setPromptText(" le Port");

		Image refreshConnectionImage;
		// Bouton pour rafraichir la connexion
		refreshConnectionButton = new Button();
		try {
		    refreshConnectionImage = new Image(new FileInputStream("src/fr/uvsq/fsp/view/refresh.png"));
			refreshConnectionButton.setGraphic(new ImageView(refreshConnectionImage));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		refreshConnectionButton.getStyleClass().add("searchButton");
		refreshConnectionButton.getStyleClass().add("removeLightGlow");
		refreshConnectionButton.setPrefWidth(10);
		refreshConnectionButton.setPrefHeight(10);

		messageLabel = new Label();

		// Bouton de recherche
		searchButton = new Button("Rechercher");
		searchButton.setPrefWidth(120);
		searchButton.setPrefHeight(35);
		searchButton.getStyleClass().add("searchButton");
		searchButton.getStyleClass().add("removeLightGlow");


		// Champ de recherche
		searchField = new TextField();
		searchField.getStyleClass().add("removeLightGlow");
		searchField.getStyleClass().add("searchField");
		searchField.setPrefWidth(150);
		searchField.setPrefHeight(35);
		searchField.setPromptText("Mot clef");



		// Liste affichant les fichiers qui correspondent à la recherche
		fileList = new ListView<String>();
		fileList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		fileList.setPrefHeight(200);
		fileList.setPrefWidth(150);
		fileList.setOrientation(Orientation.VERTICAL);

		// Bouton de téléchargement des fichiers
		downloadButton = new Button("Télécharger");
		downloadButton.setPrefWidth(120);
		downloadButton.setPrefHeight(35);
		downloadButton.getStyleClass().add("searchButton");
		downloadButton.getStyleClass().add("removeLightGlow");
		

		// Liste affichant les fichiers téléchargés
		downloadList = new ListView<String>();
		downloadList.setPrefHeight(100);
		downloadList.setPrefWidth(250);
		downloadList.setOrientation(Orientation.VERTICAL);

		// Liste affichant les fichiers partagés par l'utilisateur
		sharedList = new ListView<String>();
		sharedList.setPrefHeight(200);
		sharedList.setPrefWidth(150);
		sharedList.setOrientation(Orientation.VERTICAL);

		uploadButton = new Button("Téléverser");
		uploadButton.setPrefWidth(120);
		uploadButton.setPrefHeight(35);
		uploadButton.getStyleClass().add("searchButton");
		uploadButton.getStyleClass().add("removeLightGlow");

		sendDescriptionButton = new Button("Partager");
		sendDescriptionButton.setPrefWidth(120);
		sendDescriptionButton.setPrefHeight(35);
		sendDescriptionButton.getStyleClass().add("searchButton");
		sendDescriptionButton.getStyleClass().add("removeLightGlow");

		descriptionArea = new TextArea();
		descriptionArea.getStyleClass().add("bold");
		descriptionArea.setPrefHeight(70);
		descriptionArea.setPrefWidth(150);
		descriptionArea.setPromptText("Description");

		grid.add(serverIPField, 0, 0, 1, 1);
		grid.add(portField, 1, 0, 1, 1);
		grid.add(refreshConnectionButton, 2, 0, 1, 1);
		grid.add(messageLabel, 4, 0, 2, 1);

		grid.add(searchField, 0, 1, 2, 1);
		grid.add(searchButton, 2, 1, 2, 1);
		grid.add(downloadList, 4, 1, 4, 5);

		grid.add(fileList, 0, 2, 2, 2);
		grid.add(downloadButton, 3, 1, 2, 1);

		grid.add(sharedList, 0, 4, 2, 2);
		grid.add(uploadButton, 2, 4, 1, 1);
		grid.add(sendDescriptionButton, 3, 4, 1, 1);
		grid.add(descriptionArea, 2, 5, 2, 1);

		grid.setHgap(10);
		grid.setVgap(10);
		grid.setLayoutX(10);
		grid.setLayoutY(10);

		// On affiche les éléments dans la fenêtre
		this.getChildren().add(grid);
	}

	/**
	 * Met à jour la liste de fichiers dans la vue
	 * Supprime les éléments qui étaient déjà dans la liste
	 */
	public void updateListView(ArrayList<String> filesMatching) {
		fileList.getItems().clear();

		for (int i = 0; i != filesMatching.size(); i++) {
			fileList.getItems().add(filesMatching.get(i));
		}
	}

	public void setListView(ListView<String> lv, ArrayList<String> items) {
		lv.getItems().clear();

		for (int i = 0; i != items.size(); i++) {
			lv.getItems().add(items.get(i));
		}
	}

	/**
	 * Animation sur un label : fait apparaître puis disparaître le text
	 */
	public void fadeAnimation(Label text, long duration) {
		FadeTransition ft = new FadeTransition(Duration.millis(duration), text);
		ft.setFromValue(0.0);
		ft.setToValue(1.0);
		ft.setCycleCount(2);
		ft.setAutoReverse(true);
		ft.playFromStart();
	}

	public void setNotification(String text, String styleClass) {
		messageLabel.getStyleClass().add(styleClass);
		messageLabel.setText(text);
	}

	public void displayConnection(boolean successful) {
		messageLabel.getStyleClass().clear();
		if (successful) {
			messageLabel.getStyleClass().add("greenFont");
			messageLabel.setText("Connection successful");
		} else {
			messageLabel.getStyleClass().add("redFont");
			messageLabel.setText("Connection failed");
		}
	}

	public WritableImage toImage(String text) {
		Text t = new Text(text);
		Scene scene = new Scene(new StackPane(t));
		return t.snapshot(null, null);
	}
}

