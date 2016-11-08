//package ca.polymtl.inf4410.tp2.server;
//
//import java.rmi.RemoteException;
//
//import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
//import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
//
//public class RepartitorThread extends Thread {
//
//	private Repartitor repartitor;
//
//	private String calculous[];
//
//	private CalculousServerInterface serverStub;
//
//	public RepartitorThread(Repartitor repartitor, CalculousServerInterface stub) {
//		this.repartitor = repartitor;
//		this.serverStub = stub;
//	}
//
//	@Override
//	public void run() {
//		// TODO : implements thread logic
//		Task taskToVerify = null;
//		while (!repartitor.threadsShouldEnd){
//			if (repartitor.getSafeMore()) {
//				// On recupere la liste des calculs a verifier
//				try {
//					System.out.println("Verification des calculs a faire ...");
//					taskToVerify = repartitor.getTasksToVerify(serverStub);
//				} catch (NoMoreWorkToVerifyException nmwtve) {
//					System.out.println("Pas de calculs a verifier pour moi ...");
//
//				}
//
//				// On procede a la verification
//				proceedToVerification(taskToVerify);
//			}
//
//			// TODO
//			// Si on a pas de verification a faire, on procede aux calculs classiques
//			System.out.println("Verification des calculs a faire ...");
//			try {
//				calculous = repartitor.getSomeCalculous();
//				proceedToCalculous();
//			} catch (NoMoreWorkException nmwe) {
//				System.out.println("Plus de calculs a faire ...");
//			}
//		}
//	}
//
//	private void proceedToVerification(Task task) {
//		if (task != null){
//			int result = -1;
//			try {
//				result = calculate(serverStub, task.getCalculous());
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (OverloadedServerException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			if(result == task.getResulttoVerify()) {
//				repartitor.removeTaskToVerify(task);
//				repartitor.storeResult(result);
//			}		
//		}
//	}
//
//	private void proceedToCalculous() {
//		int result = -1;
//		try {
//			result = calculate(serverStub, calculous);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (OverloadedServerException e) {
//			// If the server is overloaded, send back the calculous
//			repartitor.addCalculous(calculous);
//			repartitor.updateCapacity(serverStub, -1);
//			repartitor.updateOverloadedSituation(serverStub, true);
//
//			e.printStackTrace();
//		}
//
//		if(repartitor.getSafeMore()) {
//			repartitor.addCalculousToVerify(serverStub, calculous, result);
//		} else {
//			repartitor.storeResult(result);
//		}
//	}
//
//	public int calculate(CalculousServerInterface server, String operations[])
//			throws RemoteException, OverloadedServerException {
//		return server.calculate(operations);
//	}
//
//}
