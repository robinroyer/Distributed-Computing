package ca.polymtl.inf4410.tp2.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;

public class Repartitor {

	/**
	 * IP of the remove server
	 */
	private static final String REMOTE_SERVER_IP = "127.0.0.1";

	private static final String PATH_CONFIG_CALCULOUS_SERVERS = "/config/servers";
	
	private static final int SEM_C_NUMBER_OF_TOKEN = 1;
	private static final int SEM_TVC_NUMBER_OF_TOKEN = 1;
	
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
	 * Minimum number of unit operation to do by a server (q value)
	 */
	private static final int MINIMUM_NUMBER_OF_OPERATIONS = 2;
	
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
	 * ArrayList to store the calculations to verify
	 */
	private HashMap<CalculousServerInterface, CalculousServerInformation> serverInformations;
	
	/**
	 * Semaphore to provide access to the calculations to verify structure
	 */
	private Semaphore toVerifyCalculationsSemaphore;
	
	/**
	 * Boolean to know if the repartitor is in safe mode or not
	 */
	private boolean safeMode;
	
	/**
	 * Hashmap to match the servers with its informations
	 */
	private HashMap<CalculousServerInterface, CalculousServerInformation> servers;
	
	
	private ArrayList<CalculousServerInterface> CalculousServeurs;
	
	/**
	 * The distant servers used for our project
	 */
	private CalculousServerInterface distantServerStub = null;

	private int port = 5010;


	/**
	 * Public constructor to create a Repartiteur instance.
	 * 
	 * @param distantServerHostname
	 *            The IP used to connect to the remote server
	 */
	public Repartitor(String distantServerHostname) {

		calculationsSemaphore = new Semaphore(SEM_C_NUMBER_OF_TOKEN);
		calculations = new ArrayList<>();
		
		toVerifyCalculationsSemaphore = new Semaphore(SEM_TVC_NUMBER_OF_TOKEN);
		toVerifyCalculations = new ArrayList<>();
		
		serverInformations = new HashMap<>();
		
		CalculousServeurs = new ArrayList<>();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		loadServer();
		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname, port);
		}
		
	}

	private void loadServer() {
		try {
			FileReader fr = new FileReader(PATH_CONFIG_CALCULOUS_SERVERS);
			BufferedReader br = new BufferedReader(fr);
			String line = null;

			while ((line = br.readLine()) != null) {
				String[] array = line.split(" ");
				CalculousServerInterface csi = loadServerStub(array[0], Integer.parseInt(array[1]));
				CalculousServeurs.add(csi);
				serverInformations.put(csi, new CalculousServerInformation(MINIMUM_NUMBER_OF_OPERATIONS));
				
			}
		} catch (IOException e)
		{	// TODO : gestion exception proprement
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		// Creation of the repartitor instance
		Repartitor repartiteur = new Repartitor(REMOTE_SERVER_IP);

		System.out.println("Lancement du repartiteur ...");

		// Check is safemode is enable or not
		if (args[1] == "-S") {
			repartiteur.setSafeMode(true);
			System.out.println("Safe mode detecte.");
		}
		
		// Start repartitor's job
		repartiteur.runRepartitor();
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
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (CalculousServerInterface) registry.lookup("server" + port);
			try {
				System.out.println(InetAddress.getLocalHost().getHostName());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (NotBoundException e) {
			System.err.println("Erreur: Le nom  " + e.getMessage() + "  n est pas defini dans le registre.");
			System.exit(ERROR_NOT_BOUND);
		} catch (AccessException e) {
			System.err.println("Erreur: " + e.getMessage());
			System.exit(ERROR_ACCESS);
		} catch (RemoteException e) {
			System.err.println("Erreur: " + e.getMessage());
			System.exit(ERROR_RMI);
		}

		return stub;
	}

	private void runRepartitor() {

		String commande = null;
		String split[] = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Chargement des serveurs ...");
		loadServer();
		
		System.out.println("Attente des commandes ...");
		try {
			while ((commande = reader.readLine()) != null) {
				split = commande.split(" ");
				String command = split[0];
				String arg1 = split[1];

				if (command.equals("compute")) {
					// Load the calculous servers
					try {
						storeCalculations(arg1);
					} catch (IOException e) {
						System.err.println("Probleme d'acces au fichier : \"" + arg1 + "\".");
					}
					// Do the job
					execute();
				}
			}
		} catch (IOException e) {
			System.err.println("Erreur dans la lecture du flux d'entree sortie.");
			e.printStackTrace();
			System.exit(ERROR_IO);
		}
	}

	private void execute() throws IOException {
		// TODO : change to manage all servers
		RepartitorThread thread = new RepartitorThread(this, distantServerStub);
		thread.start();
	}

	/**
	 * Private method to store the initial calculations to do
	 * 
	 * @param filename
	 * @throws IOException
	 */
	private void storeCalculations(String filename) throws IOException {
		// Some declarations ...
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
	}
	
	public String[] getSomeCalculousToVerify(CalculousServerInterface stub) throws NoMoreWorkToVerifyException {

		// If the map is empty we don't need to do the job
		if(toVerifyCalculations.isEmpty()) {
			throw new NoMoreWorkToVerifyException();
		}

		// Protect the datastructure by using a semaphore
		try {
			toVerifyCalculationsSemaphore.acquire(1);
		} catch (InterruptedException e) {
			System.err.println("Probleme de semaphore.");
		}
		
		String[] calculous = null;
		for(Task task : toVerifyCalculations) {

			if(!stub.equals(task.getAuthor())) {
				calculous = task.getCalculous();
			} else {
				continue;
			}
		}
		
		// Case all calculous to verify have been done by the available server
		if(calculous.equals(null)) {
			throw new NoMoreWorkToVerifyException();
		}
		
		// Release the token to the semaphore
		toVerifyCalculationsSemaphore.release(1);

		return calculous;
	}
	
	/**
	 * Get some calculous from the calculous datastructure
	 * @return a minimum number of calculous to do
	 */
	public String[] getSomeCalculous() throws NoMoreWorkException {

		// If the data structure is empty we don't need to do the job
		if(calculations.isEmpty()) {
			throw new NoMoreWorkException();
		}
		
		// Protect the datastructure by using semaphore
		try {
			calculationsSemaphore.acquire(1);
		} catch (InterruptedException e) {
			System.err.println("Probleme de semaphore.");
		}

		String[] calculous = null;
		int compteur = 0;
		
		// Store the calculous information into an array
		Iterator<String> i = calculations.iterator();
		while (i.hasNext()) {
			
			// Get the calculous from the calculations datastructure
			calculous[compteur] = i.next();
			// Remove it from the calculations datastructure
			i.remove();
			
			// Set the apropriate number of minimum operations to do
			if (compteur != MINIMUM_NUMBER_OF_OPERATIONS) {
				compteur++;
			} else {
				break;
			}
		}
		
		// Release the token from the semaphore
		calculationsSemaphore.release(1);
		
		return calculous;
	}

	/**
	 * SafeMode setter
	 * 
	 * @param true
	 *            if the mode is enable, false otherwise
	 */
	public void setSafeMode(boolean b) {
		this.safeMode = b;
	}

	public void addCalculousToVerify(CalculousServerInterface stub, String[] calculous) {
		toVerifyCalculations.add(new Task(stub, calculous));	
	}

	public void addCalculous(String[] calculous) {
		try {
			toVerifyCalculationsSemaphore.acquire(1);
		} catch (InterruptedException e) {
			// TODO : gerer
			e.printStackTrace();
		}
		
		for(int i = 0; i < calculous.length; i++) {
			calculations.add(calculous[i]);
		}
		
		toVerifyCalculationsSemaphore.release(1);
	}

	// TODO
	public void reduceCapacity(CalculousServerInterface serverStub) {

	}

}
