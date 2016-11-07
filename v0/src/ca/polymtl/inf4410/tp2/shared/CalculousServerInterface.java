package ca.polymtl.inf4410.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Server interface : contains the signature of all the methods we must
 * implement on the server side.
 * 
 * @author robinroyer
 * 
 */
public interface CalculousServerInterface extends Remote {
	/**
	 * Give operations to calculServer
	 * 
	 * @param operations
	 *            the array of operations sent to calculServer
	 * @return the result of the operations
	 * @throws RemoteException
	 * @throws ca.polymtl.inf4410.tp2.shared.OverloadedServerException
	 */
	int calculate(String[] operations) throws RemoteException,
			OverloadedServerException;
}