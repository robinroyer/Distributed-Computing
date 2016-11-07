package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;

public class RepartitorThread extends Thread {

	private Repartitor repartitor;

	private String calculous[];

	private CalculousServerInterface serverStub;

	public RepartitorThread(Repartitor repartitor, CalculousServerInterface stub) {
		this.repartitor = repartitor;
		this.serverStub = stub;
	}

	@Override
	public void run() {
		// TODO : implements thread logic
		int result = -1;
		boolean verify = false;

		// On récupère la liste des calculs à faire
		try {
			System.out.println("Vérification des calculs à faire ...");
			calculous = repartitor.getSomeCalculousToVerify(serverStub);
			verify = true;
		} catch (NoMoreWorkToVerifyException e1) {
			System.out.println("Pas de calculs à vérifier pour moi ...");
			
			System.out.println("Vérification des calculs à faire ...");
			try {
				calculous = repartitor.getSomeCalculous();
			} catch (NoMoreWorkException e) {
				System.out.println("Plus de calculs à faire ...");
			}
		}

		doTheJob(verify);
	}
	
	private void doTheJob(Boolean verification) {
		if(verification) {
			doVerification();
		} else {
			proceedToCalculous();
		}
	}
	
	private void doVerification() {
		int result = -1;
		try {
			result = calculate(serverStub, calculous);
		}  catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverloadedServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void proceedToCalculous() {
		int result = -1;
		try {
			result = calculate(serverStub, calculous);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OverloadedServerException e) {
			// If the server is overloaded, send back the calculous
			repartitor.addCalculous(calculous);
			e.printStackTrace();
		}

		repartitor.addCalculousToVerify(serverStub, calculous);
	}
	
	public int calculate(CalculousServerInterface server, String operations[]) throws RemoteException, OverloadedServerException {
		return server.calculate(operations);
	}

}
