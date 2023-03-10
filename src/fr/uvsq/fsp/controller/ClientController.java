package fr.uvsq.fsp.controller;

import fr.uvsq.fsp.util.FileLister;
import fr.uvsq.fsp.client.FSPClient;
import fr.uvsq.fsp.util.Command;
import fr.uvsq.fsp.view.ClientView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ClientController {

	/**
	 * Files matching the search
	 */
	public ArrayList<String> filesMatching;

	/**
	 * Files shared by the user
	 */
	public ArrayList<String> filesShared;

	/**
	 * Files downloaded by the user
	 */
	public ArrayList<String> filesDownloaded;

	public FileChooser fileChooser;

	public ClientView scene;
	public Stage stage;
	public FSPClient client;
	public boolean isConnected = false;

	public ClientController(Stage stage, ClientView view, FSPClient fspClient) {
		this.scene = view;
		this.client = fspClient;
		this.stage = stage;

		scene.serverIPField.setText(client.adresseIPServeur);
		if (client.port != 0)
		{
			scene.portField.setText(String.valueOf(client.port));
		}

		filesShared = FileLister.list(client.clientSharedFolder);
		scene.setListView(scene.sharedList, filesShared);

		filesDownloaded = FileLister.listWithLevel(client.clientDownloadsFolder, 1);
		scene.setListView(scene.downloadList, filesDownloaded);

		scene.refreshConnectionButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (scene.serverIPField.getText() == null
					|| scene.portField.getText() == null
					|| scene.serverIPField.getText().equals("")
					|| scene.portField.getText().equals(""))
					return;

				client.port = Integer.parseInt(scene.portField.getText());
				client.adresseIPServeur = scene.serverIPField.getText();

				try {
					client.connect();
					client.open();
					client.type();
					if (client.verifieHostname()) {
						scene.displayConnection(true);
						isConnected = true;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					scene.displayConnection(false);
				}
			}
		});

		// ??v??nements
		/*
		 * Recherche par mot clef
		 */
		scene.searchButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				if (isConnected && !scene.searchField.getText().equals("")) {
					try {
						searchEvent();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		scene.searchField.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (isConnected && ke.getCode().equals(KeyCode.ENTER)) {
					if (!scene.searchField.getText().equals("")) {
						try {
							searchEvent();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});
		scene.fileList.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					try {
						downloadEvent();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});


		scene.downloadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					downloadEvent();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		fileChooser = new FileChooser();
		scene.uploadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				try {
					uploadEvent();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});

		scene.downloadList.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				System.out.println(scene.downloadList.getSelectionModel().getSelectedItem());
			}
		});

		scene.sendDescriptionButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				try {
					descriptionEvent();
				} catch (FileNotFoundException ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Gives some file samples for the list view
	 */
	public ArrayList<String> samples() {
		ArrayList<String> a = new ArrayList<String>();
		a.add("localhost/truc.txt");
		a.add("poseidon/yojinbo");
		a.add("zeus/RAPPORT.pdf");

		return a;
	}

	public void downloadEvent() throws IOException {
		ObservableList<Integer> indices = scene.fileList.getSelectionModel().getSelectedIndices();
		ArrayList<String> downloads;

		downloads = new ArrayList<String>();

		// Downloading each file
		for (Integer index : indices) {
			System.out.println("T??l??chargement du fichier " + filesMatching.get(index));
			System.out.println("DOWNLOAD " + filesMatching.get(index));
			String tmp[] = filesMatching.get(index).split("\\\\");
			System.out.println(tmp.length);
			try {
				
				client.download(tmp[0], tmp[1]);
				System.out.println("Fichier " + tmp[1] + "t??l??charg??");
				scene.setNotification("Fichier " + tmp[1] + "t??l??charg??", "greenFont");
			} catch (IOException e) {
				e.printStackTrace();
				scene.setNotification(e.getMessage(), "redFont");
			}
		}

		filesDownloaded = FileLister.listWithLevel(client.clientDownloadsFolder, 1);
		scene.setListView(scene.downloadList, filesDownloaded);
		displayUploadMessage(indices.size());
	}

	/**
	 * Query the server for files and display the results in the view.
	 */
	public void searchEvent() throws IOException {
		String msg;
		Command ftpCmd;

		System.out.println("SEARCH " + scene.searchField.getText());

		client.search(scene.searchField.getText());
		msg = client.lireMessage();
		ftpCmd = Command.parseCommand(msg);
		System.out.println(msg);

		if (ftpCmd.command.equals("FOUND")) {
			filesMatching = client.parseFilesFound(ftpCmd.content);
			scene.setNotification(filesMatching.size() + " fichier(s) trouv??(s)", "greenFont");
			scene.updateListView(filesMatching);
		} else {
			scene.setNotification("Pas de fichier trouv??", "redFont");
		}
	}

	public void uploadEvent() throws IOException {
		// get the file selected
		File file = fileChooser.showOpenDialog(stage);

		if (file != null) {
			try {
				Path sourceDirectory = Paths.get(file.getAbsolutePath());
				Path targetDirectory = Paths.get(client.clientSharedFolder+sourceDirectory.getFileName().toString());

				//copy source to target using Files Class
				Files.copy(sourceDirectory, targetDirectory);
				scene.setNotification("Fichier " + file + " t??l??vers??", "greenFont");
				filesShared = FileLister.list(client.clientSharedFolder);
				scene.setListView(scene.sharedList, filesShared);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void descriptionEvent() throws FileNotFoundException {
		String file;

		file = scene.sharedList.getSelectionModel().getSelectedItem();

		if (null != file) {
			System.out.println("Creating the description for the file : " + file);
			System.out.println("> " + scene.descriptionArea.getText());
			try (PrintWriter out = new PrintWriter(client.descriptionsFolder + file)) {
				out.println(scene.descriptionArea.getText());
				scene.setNotification("Description cr????e pour le fichier " + file , "greenFont");
				client.envoyerMessage("FILECOUNT 1");
				client.envoyerFichier(client.descriptionsFolder + file);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} else {
			System.out.println("Pas de fichier s??lectionn??");
			scene.setNotification("Pas de fichier s??lectionn??", "redFont");
		}
	}

	public void displayUploadMessage(int fileCount) {
		scene.messageLabel.getStyleClass().clear();
		if (fileCount > 0) {
			scene.messageLabel.getStyleClass().add("greenFont");
			scene.messageLabel.setText("T??l??charg?? " + fileCount + " fichier(s)");
		} else {
			scene.messageLabel.getStyleClass().add("redFont");
			scene.messageLabel.setText("Erreur");
		}
		scene.fadeAnimation(scene.messageLabel, 2000);
	}
}

