/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.client;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;

/**
 * CalculServer implement the server that will procceed calculation
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
	 */
	public static void main(String[] args) {
            
                CalculousServer server;
                        
                try {
                        server = new CalculousServer(args[0],   // ip
                            Integer.parseInt(args[1]),          // port
                            Integer.parseInt(args[2]),          // confidence
                            Integer.parseInt(args[3]));         // capacity
                } catch (Exception e) {
                        System.out.println("Parameters are missing Defult is => ./client ip port confidence capacity");
                        System.out.println("... => Launching server with default configuration");
                        server = new CalculousServer("127.0.0.1", 5010, 0, 3);
                }

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
			Registry registry = LocateRegistry.getRegistry(this.getPort());
			registry.rebind(this.getIp(), stub);
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
         * @param ip server ip
         * @param port server listening port
         * @param confidence percentage of maliciousness on a scale from 0 to 100
         * @param capacity number of operation from where the server will begin to response as overloaded
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
         * @param operations Array of string containing operation
         * @return
         * @throws RemoteException Rmi error
         * @throws OverloadedServerException Call when the server receive too much operation
         */
	@Override
	public int calculate(String[] operations) throws RemoteException, OverloadedServerException {

		if (isOverloaded(operations.length)) {
			System.err.println("Refus de calcul: il y a " + operations.length
					+ " operations. Cela depasse la capacite du serveur de calcul.");
			throw new OverloadedServerException();
		}
                
                int result = 0;
		String[] operation;
                
                if(isMalicious()){                       
                        result =  (int) (Math.random() * 4000);
                        System.out.println("The server is malicious, le faux resultat est : " + result);
                        return result;
                }		

		for (int i = 0; i < operations.length; i++) {

			// parse arrays of operations
			operation = operations[i].split(" ");

			if (null != operation[0]) // call the relevant operation
				switch (operation[0]) {
				case PELL:
					result += proceedPellAndModulo(operation[1]);
					break;
				case PRIME:
					result += proceedPrimeAndModulo(operation[1]);
					break;
				}
		}
		result = result % 4000;
		System.out.println("Le resultat de ces " + operations.length + " operations est : " + result);
		return result;
	}
        
        /**
         * Private method to check if the server will launch a malicious response
         * @return true is the server should be malicious
         */
        private boolean isMalicious(){
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
		double refusingRate = (operationNumber - capacity) / (4 * capacity) ;
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
	
	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

}
