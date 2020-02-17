package fr.uvsq.fsp.client;

import fr.uvsq.fsp.abstractions.Yoda;
import java.util.ArrayList;
import fr.uvsq.fsp.util.Checksum;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.net.InetAddress;

public class FSPClient extends Yoda {

	/** Nom d'hôte de l'utilisateur */
	public String hostname;

	/** Identifiant de l'utilisateur */
	public String id = " ";

	/** Mot de passe */
	public String mdp = " ";

	/** Répertoire qui contient les descriptions des fichiers partagés par le serveur */
	public final String descriptionsFolder;// = "src/fr/uvsq/fsp/client/descriptions/";

	public FSPClient(String serverIP, int port, String descFolder) {
		super(serverIP, port);
        descriptionsFolder = descFolder;
		new File(descriptionsFolder).mkdirs();

		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void connect() throws UnknownHostException, IOException {
		socket = new Socket(adresseIPServeur, port);
	}

	/**
	 * Authentification
	 * Server
	 */
	public void login() {
		Scanner scan;
		String reponse;

		scan = new Scanner(System.in);

		try {
			// On entre et on envoie l'identifiant ...
			do {
				System.out.print("Identifiant : ");
				id = scan.nextLine();
				super.envoyerMessage("USER " + id);
				reponse = super.lireMessage();
				System.out.println(reponse);
			} while (!reponse.startsWith("2"));

			// ... puis le mot de passe
			do {
				System.out.print("Mot de passe : ");
				mdp = scan.nextLine();
				super.envoyerMessage("PASS " + Checksum.getMD5Hash(this.mdp));
				reponse = super.lireMessage();
				System.out.println(reponse);
			} while (!reponse.startsWith("2"));

			System.out.println("Authentification réussie !");
			hostname();
			envoyerDescriptions(descriptionsFolder);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean login(String id, String mdp) {
		String reponse;

		try {
			super.envoyerMessage("USER " + id);
			reponse = super.lireMessage();
			System.out.println(reponse);
			if (!reponse.startsWith("2")) return false;

			super.envoyerMessage("PASS " + Checksum.getMD5Hash(mdp));
			reponse = super.lireMessage();
			System.out.println(reponse);
			if (!reponse.startsWith("2")) return false;

			System.out.println("Authentification réussie !");
			hostname();
			envoyerDescriptions(descriptionsFolder);

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}
	/**
	 * Envoie le nom d'hôte de l'utilisateur
	 */
	public void hostname() throws IOException {
		envoyerMessage("HOSTNAME " + hostname);
	}

	/**
	 * Envoie le hostname au serveur centrale et
	 * verifie qu'il existe sur usersConnected
	 * s'il n'existe pas il ferme la connection
	 * @throws IOException
	 */
	public boolean verifieHostname() throws IOException {
		String reponse;

		try {
			super.envoyerMessage("HOST " + hostname);
			reponse = super.lireMessage();
			System.out.println(reponse);

			// return reponse.startsWith("2");
			if (reponse.startsWith("2")) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Laisse l'utilisateur interroger le serveur centralisé
	 * si l'utilisateur n'a rien entré, rien n'est envoyé
	 * si l'utilisateur tape QUIT, on quitte la méthode
	 */
	public void queryCentral() {
		Scanner scan;
		String reponse;
		String query;
		boolean loop = true;

		System.out.println("Interrogez le serveur. Tapez QUIT pour quitter le programme.");
		scan = new Scanner(System.in);

		try {
			while (loop) {
				System.out.print("> ");
				query = scan.nextLine();

				if (query.equals("QUIT")) {
					loop = false;
				} else if (!query.isEmpty()) {
					search(query);
					System.out.println(lireMessage());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> parseFilesFound(String content) {
		ArrayList<String> filesMatching;
		String[] files;

		filesMatching = new ArrayList<String>();
		files = content.split(" ");

		for (String file : files) {
			filesMatching.add(file);
		}

		return filesMatching;
	}

	/**
	 * Interroge le serveur : est-ce qu'un fichier contient ce mot-clef ?
	 *
	 * SEARCH film
	 */
	public void search(String keyword) throws IOException {
		super.envoyerMessage("SEARCH " + keyword);
	}

	/**
	 * Méthode à appeler quand l'utilisateur veut quitter la session
	 * Doit recevoir un accusé réception
	 */
	public void quit() throws IOException {
		super.envoyerMessage("QUIT");
	}
}

