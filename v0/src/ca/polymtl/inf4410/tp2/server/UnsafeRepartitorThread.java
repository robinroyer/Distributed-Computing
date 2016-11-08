package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;

public class UnsafeRepartitorThread extends Thread {

	private Repartitor repartitor;

	private String calculous[];

	private CalculousServerInterface serverStub;

	public UnsafeRepartitorThread(Repartitor repartitor, CalculousServerInterface stub) {
		this.repartitor = repartitor;
		this.serverStub = stub;
	}

	@Override
	public void run() {

		Task taskToVerify;
		while (!repartitor.getThreadsShouldEnd()){
                        taskToVerify = null;
                                
			if (!repartitor.isSafeMode()) {
				// On recupere la liste des calculs a verifier
				try {
					//System.out.println("Verification des calculs a faire ...");
					taskToVerify = repartitor.getTasksToVerify(serverStub);
				} catch (NoMoreWorkToVerifyException nmwtve) {
					System.out.println("Pas de calculs a verifier pour moi ...");
                                        // => c'est normal de ne plus avoir de calculs 
				}

				// On procede a la verification
				proceedToVerification(taskToVerify);
			}else{
                            //System.out.println("Verification des calculs a faire ...");
                            try {
                                    calculous = repartitor.getSomeCalculous();
                                    proceedToCalculous();
                            } catch (NoMoreWorkException nmwe) {
                                    System.out.println("Plus de calculs a faire ...");
                            }
                        }
		}
	}

	private void proceedToVerification(Task task) {
		if (task != null){
			int result = -1;
			try {
				result = calculate(serverStub, task.getCalculous());
			} catch (RemoteException | OverloadedServerException e) {
				e.printStackTrace();
			}
			
			if(result == task.getResulttoVerify()) {
				repartitor.removeTaskToVerify(task);
				repartitor.storeResult(result);
			}		
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
			repartitor.updateCapacity(serverStub, -1);
			repartitor.updateOverloadedSituation(serverStub, true);

			e.printStackTrace();
		}

		if(repartitor.isSafeMode()) {
			repartitor.addCalculousToVerify(serverStub, calculous, result);
		} else {
			repartitor.storeResult(result);
		}
	}

	public int calculate(CalculousServerInterface server, String operations[])
			throws RemoteException, OverloadedServerException {
		return server.calculate(operations);
	}

}
