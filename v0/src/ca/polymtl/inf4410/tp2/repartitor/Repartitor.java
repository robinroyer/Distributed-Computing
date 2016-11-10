package ca.polymtl.inf4410.tp2.repartitor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;

public class Repartitor {

	/**
	 * Path to the calculous servers configuration
	 */
	private static final String PATH_CONFIG_CALCULOUS_SERVERS = "./config/servers.config";

	/**
	 * Command to compute the file
	 */
	private static final String CMD_COMPUTE = "compute";

	/**
	 * Command to exit the program
	 */
	private static final String CMD_EXIT = "exit";

	/**
	 * Number of token for the calculous semaphore
	 */
	private static final int SEM_C_NUMBER_OF_TOKEN = 1;

	/**
	 * Number of token for the to verify calculous semaphore
	 */
	private static final int SEM_TVC_NUMBER_OF_TOKEN = 1;

	/**
	 * Number of token for the result semaphore
	 */
	private static final int SEM_R_NUMBER_OF_TOKEN = 1;
	/**
	 * Argument of the command line for to enable the safe mode
	 */
	private static final String SAFE_ARGUMENT = "-S";
	/**
	 * Error exit IO code
	 */
	private static final int ERROR_IO = -10;

	/**
	 * Error exit RMI code
	 */
	private static final int ERROR_RMI = -20;

	/**
	 * Error exit not bound code
	 */
	private static final int ERROR_NOT_BOUND = -30;

	/**
	 * Error exit access code
	 */
	private static final int ERROR_ACCESS = -40;

	/**
	 * ArrayList to store the calculations to do
	 */
	private ArrayList<String> calculations;

	/**
	 * Semaphore to provide access to the calculations structure
	 */
	private Semaphore calculationsSemaphore;

	/**
	 * ArrayList to store the calculations to verify
	 */
	private ArrayList<Task> toVerifyCalculations;

	/**
	 * Semaphore to provide access to the calculations to verify structure
	 */
	private Semaphore toVerifyCalculationsSemaphore;

	/**
	 * Boolean to know if the repartitor is in safe mode or not
	 */
	private boolean safeMode;

	/**
	 * Boolean to control threads ending
	 */
	private boolean threadsShouldEnd = false;

	/**
	 * Arrayslist of our different thread
	 */
	private ArrayList<Thread> threads;

	/**
	 * GlobalResult is an array of size 2 in order to be able to give it by
	 * reference to threads index 0 => the result index 1 => number of operation
	 * stacked in result
	 */
	private int[] globalResult;

	/**
	 * Semaphore protecting globalresult
	 */
	private Semaphore globalResultLock;

	/**
	 * number of operation provided to the repartitor
	 */
	private int operationNumber;

	/**
	 * Array containing all the server instance
	 */
	private ArrayList<CalculousServerInterface> CalculousServeurs;

	/**
	 * Public constructor to create a Repartiteur instance
	 * 
	 */
	public Repartitor(boolean isSafe) {

		// Enable of not the safe mode
		safeMode = isSafe;

		// Creation of calculous data structure
		calculationsSemaphore = new Semaphore(SEM_C_NUMBER_OF_TOKEN);
		calculations = new ArrayList<>();

		// Creation of to verify calculous data structure
		toVerifyCalculationsSemaphore = new Semaphore(SEM_TVC_NUMBER_OF_TOKEN);
		toVerifyCalculations = new ArrayList<>();

		// Create the list of servers
		CalculousServeurs = new ArrayList<>();

		// Create the list of threads
		threads = new ArrayList<>();

		// Initialize the result structure
		globalResult = new int[2];
		globalResult[0] = 0;
		globalResult[1] = 0;
		globalResultLock = new Semaphore(SEM_R_NUMBER_OF_TOKEN);

		// Set the security manager
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}
	}

	/**
	 * Main point of the program
	 * 
	 * @param args
	 * @throws java.lang.InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {

		// Checking safe mode
		boolean isSafe = false;
		if (args.length >= 1 && args[0].equals(SAFE_ARGUMENT)) {
			isSafe = true;
			System.out.println("Mode \"safe\" detecte : les calculs seront tous verifies.");
		} else {
			System.out.println("Mode \"non safe\" detecte : les calculs ne seront pas verifies.");
		}

		// Creation of the repartitor instance
		Repartitor repartiteur = new Repartitor(isSafe);
		// Start repartitor's job
		repartiteur.runRepartitor();
	}

	/**
	 * Main private method to run the repartitor
	 */
	private void runRepartitor() throws InterruptedException {

		String commande = null;
		String commandes[] = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Chargement des serveurs ...");
		loadServerFromConfigFile();

		System.out.println("Attente des commandes ...");
		try {
			while ((commande = reader.readLine()) != null) {
				commandes = commande.split(" ");
				String command = commandes[0];

				switch (command) {
				case CMD_COMPUTE:
					String filename = commandes[1];
					try {
						parseCalculousFileToCalculous(filename);
					} catch (IOException e) {
						System.err.println("Le fichier " + filename + " est inacessible.");
					}
					// Start the threads
					startThreadsThenJoin();
					break;
				case CMD_EXIT:
					System.out.println("Sortie du programme ...");
					System.exit(0);
					break;
				default:
					System.out.println("Commande inconnue : compute nomFichier ou exit sont les "
							+ "deux seules commandes possibles.");
					break;

				}
			}
		} catch (IOException e) {
			System.err.println("Erreur dans la lecture du flux d'entree sortie.");
			e.printStackTrace();
			System.exit(ERROR_IO);
		}
	}

	/**
	 * Load all the configuration of the calculous servers
	 */
	private void loadServerFromConfigFile() {
		try {
			FileReader fr = new FileReader(PATH_CONFIG_CALCULOUS_SERVERS);
			BufferedReader br = new BufferedReader(fr);
			String line = null;

			while ((line = br.readLine()) != null) {
				String[] array = line.split(" ");
				CalculousServerInterface csi = loadServerStub(array[0], Integer.parseInt(array[1]));
				CalculousServeurs.add(csi);
				// Avoid empty line crash
				if(array.length == 0) {
					continue;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Private method to load the server
	 * 
	 * @param hostname
	 * @return
	 */
	private CalculousServerInterface loadServerStub(String hostname, int port) {
		CalculousServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname, port);
			stub = (CalculousServerInterface) registry.lookup(hostname);
		} catch (NotBoundException e) {
			System.err.println("Erreur : Le nom  " + e.getMessage() + " n'est pas defini dans le registre.");
			System.exit(ERROR_NOT_BOUND);
		} catch (AccessException e) {
			System.err.println("Erreur : " + e.getMessage());
			System.err.println("test");
			System.exit(ERROR_ACCESS);
		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
			System.err.println("test2");
			System.exit(ERROR_RMI);
		} 

		return stub;
	}

	/**
	 * Private method to launch one thread by server, one coordination thread
	 * and then wait the end of threads work to print the resul
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void startThreadsThenJoin() throws IOException, InterruptedException {
		long firstTime = System.currentTimeMillis();

		// Create threads depending on the safety parameter
		if (!safeMode) {
			for (CalculousServerInterface server : CalculousServeurs) {
				SafeRepartitorThread thread = new SafeRepartitorThread(this, server, calculations,
						calculationsSemaphore, globalResult, globalResultLock);
				threads.add(thread);
				thread.start();
			}
		} else {
			for (CalculousServerInterface server : CalculousServeurs) {
				UnsafeRepartitorThread thread = new UnsafeRepartitorThread(this, server, calculations,
						calculationsSemaphore, globalResult, globalResultLock, toVerifyCalculations,
						toVerifyCalculationsSemaphore);
				threads.add(thread);
				thread.start();
			}
		}

		// Create the thread to coordinate all the results
		Thread coordinationThread = new CoordinateThread(this, globalResult, globalResultLock, operationNumber);
		coordinationThread.start();

		// Threads synchronisation
		coordinationThread.join();
		long secondTime = System.currentTimeMillis();
		for (Thread thread : threads) {
			thread.join();
		}

		// Print the results
		System.out.println("Resultats des calculs ! " + globalResult[0]);
		System.out.println("Ce calcul a ete effectue en : " + (secondTime - firstTime) + " millisecondes");
	}

	/**
	 * Private method to store the initial calculations to do
	 * 
	 * @param filename
	 * @throws IOException
	 */
	private void parseCalculousFileToCalculous(String filename) throws IOException {
		// Some declarations
		String line = null;
		FileInputStream fis = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
		BufferedReader br = new BufferedReader(isr);

		// Add each line of the file to the datastructure
		while ((line = br.readLine()) != null) {
			calculations.add(line);
		}

		// Close the buffer
		br.close();
		operationNumber = calculations.size();
	}

	/**
	 * safeMode getter
	 * 
	 * @return safeMode
	 */
	public boolean isSafeMode() {
		return this.safeMode;
	}

	/**
	 * threadsShouldEnd getter
	 * 
	 * @return the inverse of threadsShouldEnd
	 */
	public boolean threadsShouldContinue() {
		return !threadsShouldEnd;
	}

	/**
	 * set threadsShouldEnd to true
	 */
	public void stopTheThreads() {
		threadsShouldEnd = true;
	}
}
