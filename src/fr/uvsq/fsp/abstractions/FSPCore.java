package fr.uvsq.fsp.abstractions;

import fr.uvsq.fsp.util.Command;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FSPCore {

	/** Ecriture dans une socket */
	public BufferedReader reader;

	/** Lecture dans une socket */
	public PrintWriter writer;

	// Fichier
	public DataOutputStream dos;
	public DataInputStream dis;

	/** Adresse IP du serveur */
	public String adresseIPServeur = "127.0.0.1";

	public int port = 50000;

	public Socket socket;

	/**
	 */
	public FSPCore(String serverIP, int portNumber) {
		adresseIPServeur = serverIP;
		port = portNumber;
	}

	public FSPCore(int portNumber) {
		port = portNumber;
	}

	public FSPCore(Socket sock) {
		socket = sock;
	}

	/**
	 * Ouvre des ressources pour écrire/lire dans des sockets
	 */
	public void open2() {
		try {
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF8"));
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8")), true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void open() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Envoie toutes les descriptions
	 * Envoie d'abord le nombre de fichiers à envoyer
	 *
	 * descriptionsDir est le répertoire où sont les descriptions
	 * Server
	 */
	public void envoyerDescriptions(String descriptionsDir) throws IOException {
		File dir;

		dir = new File(descriptionsDir);

		// On envoie d'abord le nombre de fichiers à envoyer
		envoyerMessage("FILECOUNT " + dir.list().length);

		// On liste les fichiers partagés
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(descriptionsDir))) {
			for (Path path : stream) {
				envoyerFichier(path.toString());
			}
		}
	}

	/**
	 * dir est le répertoire où sont les descriptions
	 * Central
	 */
	public void saveDescriptions(String dir, int fileCount) throws IOException {
		for (int i = 0; i != fileCount; i++) {
			lireFichier(dir);
		}
	}

	/**
	 * Lit un message (une ligne) envoyé par socket
	 */
	public String lireMessage2() throws IOException {
		return reader.readLine();
	}

	public String lireMessage() throws IOException {
		return dis.readUTF();
	}

	/**
	 * Envoie une message à travers une socket
	 */
	public void envoyerMessage2(String msg) throws IOException {
		System.out.println("> " + msg);
		writer.println(msg);
	}

	public void envoyerMessage(String msg) throws IOException {
		dos.writeUTF(msg);
		dos.flush();
	}
	/**
	 * Libère les ressources
	 */
	public void close2() {
		try {
			reader.close();
			writer.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			dos.close();
			dis.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Envoie UN fichier par socket
	 */
	public void envoyerContenu2(String filePath) throws IOException {
		BufferedReader br;
		File file;
		FileReader fr;
		String line;
		int fileSize;

		file = new File(filePath);
		fileSize = (int) file.length();// pas utilisée
		fr = new FileReader(file);
		br = new BufferedReader(fr);

		// On envoie le fichier ligne par ligne
		while ((line = br.readLine()) != null) {
			envoyerMessage(line);
		}

		// L'étiquette END marque la fin du fichier
		// TODO Une alternative serait d'utiliser la taille du fichier,
		// mais c'est trop compliqué. Toutefois, qu'est-ce qu'il se passe
		// si le fichier contient END ?
		// Ou bien le nombre de lignes, mais obtenir une méthode efficace
		// qui compte le nombre de lignes, c'est pas facile
		envoyerMessage("END");
		fr.close();
	}

	public void envoyerContenu(String filePath) throws IOException {
		File file;
		InputStream in;
		int fileSize;
		byte[] bytes;
		int count;

		file = new File(filePath);
		fileSize = (int) file.length();// pas utilisée
		in = new FileInputStream(file);
		bytes = new byte[fileSize];

		// On envoie le fichier ligne par ligne
		while ((count = in.read(bytes)) > 0) {
			System.out.println(count);
			dos.write(bytes, 0, count);
			dos.flush();
		}

		System.out.println("Envoi fini");
		in.close();
	}

	public void enregistrerContenu(String filePath, int fileSize) throws IOException {
		int count;
		byte[] bytes = new byte[fileSize];
		FileOutputStream fos;
		int sum;

		fos = new FileOutputStream(filePath);
		sum = 0;

		while ((count = dis.read(bytes)) > 0 && (sum < fileSize)) {
			sum += count;
			System.out.println("Lu : " + count);
			System.out.println("Total lu : " + sum);
			System.out.println("Reste : " + (fileSize - sum));
			fos.write(bytes, 0, count);
			fos.flush();
		}

		System.out.println("Lecture finie");
		fos.close();
	}

	/**
	 * Récupère UN fichier envoyé par socket
	 *
	 * Ici on a un BufferedReader qu'on pourrait mettre en attribut
	 */
	public void enregistrerContenu2(String filePath) throws IOException {
		BufferedWriter bw;
		File file;
		FileWriter fw;
		String msg;

		file = new File(filePath);
		fw = new FileWriter(file);
		bw = new BufferedWriter(fw);

		// Chaque ligne du fichier nous est envoyée
		// L'étiquette END marque la fin de la transmission
		while (!(msg = lireMessage()).equals("END")) {
			bw.write(msg + "\n");
		}

		bw.close();
	}

	/**
	 * On envoie : le nom du fichier et sa taille (en octets)
	 * La taille n'est pas utile pour le moment, mais elle pourrait
	 * l'être plus tard ! (voir enregistrerContenu() envoyerContenu())
	 * Ensuite on envoie le contenu
	 */
	public void envoyerFichier(String filePath) throws IOException {
		String fileName;
		File file;

		file = new File(filePath);
		fileName = file.getName();
				// filePath.substring(filePath.lastIndexOf('/') + 1);

		// L'étiquette FILE va indiquer qu'on envoie le nom et la taille
		envoyerMessage("FILE " + fileName + " " + file.length());
		envoyerContenu(filePath);
	}


	/**
	 * On récupère le nom de fichier et sa taille, ensuite le contenu du
	 * fichier
	 */
	public void lireFichier(String dir) throws IOException {
		String msg;
		Command ftpCmd;
		String fileName;
		int fileSize;

		// On attend un message avec pour commande FILE
		//do {
			msg = lireMessage();
			ftpCmd = Command.parseCommand(msg);
		//} while (!ftpCmd.command.equals("FILE"));

		fileName = ftpCmd.content.split(" ")[0];
		fileSize = Integer.parseInt(ftpCmd.content.split(" ")[1]);
		System.out.println(dir + fileName + ", " + fileSize);
		enregistrerContenu(dir + fileName, fileSize);
	}
}

