package ca.polymtl.inf4410.tp2.calculous.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;

/**
 * CalculServer implement the server that will proceed to calculations.
 * 
 * @author robinroyer
 */
public class CalculousServer implements CalculousServerInterface {

	/**
	 * Name of the first operation sent to CalculServer
	 */
	private final static String PRIME = "prime";

	/**
	 * Name of the second operation sent to CalculServer
	 */
	private final static String PELL = "pell";

	/**
	 * Maximum capacity of the instance of CalculServer
	 */
	private final int capacity;

	/**
	 * Percentage of trusted return message
	 */
	private final int confidence;

	/**
	 * Server IP
	 */
	private String ip;

	/**
	 * Rmiregistry port
	 */
	private int port;

	/**
	 * Main to run the server.
	 * 
	 * @param args
	 *            IP, port, confidence and capacity
	 */
	public static void main(String[] args) {

		// Server instance declaration
		CalculousServer server;

		try {
			server = new CalculousServer(args[0], // ip
					Integer.parseInt(args[1]), // port
					Integer.parseInt(args[2]), // confidence
					Integer.parseInt(args[3])); // capacity
		} catch (Exception e) {
			System.out.println("Parametres manquants : ./client ip port confidence capacity");
			System.out.println("Lancement du serveur avec la configuration par defaut.");
			server = new CalculousServer("127.0.0.1", 5010, 0, 3);
		}

		// Run the server
		server.run();
	}

	/**
	 * Main method to run the server.
	 */
	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			CalculousServerInterface stub = (CalculousServerInterface) UnicastRemoteObject.exportObject(this, 0);
			Registry registry = LocateRegistry.getRegistry(port);
			registry.rebind(ip, stub);
			System.out.println("CalculServer ready.");
		} catch (ConnectException e) {
			System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lance ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/**
	 * Constructor from Args
	 * 
	 * @param ip
	 *            server ip
	 * @param port
	 *            server listening port
	 * @param confidence
	 *            percentage of maliciousness on a scale from 0 to 100
	 * @param capacity
	 *            number of operation from where the server will begin to
	 *            response as overloaded
	 */
	public CalculousServer(String ip, int port, int confidence, int capacity) {
		this.ip = ip;
		this.port = port;
		this.confidence = confidence;
		this.capacity = capacity;
	}

	/**
	 * Public methode call remotely by thread to send operations to be done
	 * 
	 * @param operations
	 *            Array of string containing operation
	 * @return
	 * @throws RemoteException
	 *             Rmi error
	 * @throws OverloadedServerException
	 *             Call when the server receive too much operation
	 */
	@Override
	public int calculate(String[] operations) throws RemoteException, OverloadedServerException {

		// Check overloaded situation
		if (isOverloaded(operations.length)) {
			System.err.println("Refus de calcul: il y a " + operations.length
					+ " operations. Cela depasse la capacite du serveur de calcul.");
			throw new OverloadedServerException();
		}

		int result = 0;
		String[] operation;

		// Check if the server is malicious
		if (isMalicious()) {
			result = (int) (Math.random() * 4000);
			System.out.println("Le serveur est malicieux. Le faux resultat est : " + result);
			return result;
		}

		// Proceed to calculous
		for (int i = 0; i < operations.length; i++) {

			// parse arrays of operations
			operation = operations[i].split(" ");
			String currentOperation = operation[0];

			if (currentOperation != null) // call the relevant operation
				switch (currentOperation) {
				case PELL:
					result += proceedPellAndModulo(operation[1]);
					break;
				case PRIME:
					result += proceedPrimeAndModulo(operation[1]);
					break;
				default:
					// Amelioration : make the system more robust
					System.err.println("Cette operation n'est pas valide : " + currentOperation);
					break;
				}
		}
		
		// Send the appropriate result 
		result = result % 4000;
		System.out.println("Le resultat de ces " + operations.length + " operations est : " + result);
		return result;
	}

	/**
	 * Private method to check if the server will launch a malicious response
	 * 
	 * @return true is the server should be malicious
	 */
	private boolean isMalicious() {
		return (Math.random() * 100) < confidence;
	}

	/**
	 * Test if the server is overloaded, in order to accept or refuse the
	 * operations
	 * 
	 * @param operationNumber
	 *            Number of operations sent to the CalculServer
	 * @return True if the operations is not be accepted
	 */
	private boolean isOverloaded(int operationNumber) {
		// Algorythm to calculate refusingRate: T = (U-Q)/(4*Q) * 100
		double refusingRate = (operationNumber - capacity) / (4 * capacity);
		return refusingRate > Math.random();
	}

	/**
	 * Call Operations.prime with the param as a string
	 * 
	 * @param numberAsString
	 *            Number to proceed as a string
	 * @return result of pell modulo 4000
	 */
	private int proceedPrimeAndModulo(String numberAsString) {
		return Operations.prime(Integer.parseInt(numberAsString)) % 4000;
	}

	/**
	 * Call Operations.pell with the param as a string
	 * 
	 * @param numberAsString
	 *            Number to proceed as a string
	 * @return result of pell modulo 4000
	 */
	private int proceedPellAndModulo(String numberAsString) {
		return Operations.pell(Integer.parseInt(numberAsString)) % 4000;
	}
}
