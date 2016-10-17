package ca.polymtl.inf4410.tp2.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class Repartiteur {

	/**
	 * Error IO Code
	 */
	private static final int ERROR_IO = -10;

	public Repartiteur() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {

		Repartiteur repartiteur = new Repartiteur();

		System.out.println("Lancement du repartiteur ...");
		repartiteur.run();

	}

	private void run() {

		System.out.println("Attente des commandes ...");

		String commande = null;
		String split[] = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));

		try {
			while ((commande = reader.readLine()) != null) {

				split = commande.split(" ");

				if (split[0].equals("compute")) {
					// Start to call the calculous servers
					// TODO
					try {
						execute(split[1]);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			System.err
					.println("Erreur dans la lecture du flux d'entree sortie.");
			e.printStackTrace();
			System.exit(ERROR_IO);
		}
	}

	private void execute(String filename) throws IOException {

		String line = null;
		int compteur = 0;
		String message[] = null;

		FileInputStream fis = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fis,
				Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);

		while ((line = br.readLine()) != null) {
			message[compteur] = line;
			if (compteur == 2) {
				sendCalculous(message);
				compteur = 0;
			} else {
				compteur++;
			}

		}
	}

	private static void sendCalculous(String message[]) {

		
	}
}
