package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;
import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;


public class UnsafeRepartitorThread extends SafeRepartitorThread {

       
        // WE SHOULD ADD UNSAFE LOGIC HERE

        UnsafeRepartitorThread(Repartitor repart, CalculousServerInterface server, ArrayList<String> calculations, Semaphore calculationsSemaphore, int[] globalResult, Semaphore globalResultLock) {
                super(repart, server, calculations, calculationsSemaphore, globalResult, globalResultLock);
        }

	@Override
	public void run() {
//                int res = 0;
//                int operationNumber = 0;
//		while (repartitor.threadsShouldContinue()){                                			
//                        try {
//                                operationNumber = threadedPickingCalculous();
//                                res = calculate(serverStub, calculousOwnedByThread);
//                                threadedAddingResult(res, operationNumber);
//                                handleUnderload();
//                        }
//                        catch(NoMoreWorkException | InterruptedException | RemoteException e){}
//                        catch (OverloadedServerException ex) {
//                            try {
//                                pushBackThreadCalculousToCalculous();
//                            } catch (InterruptedException ex1) {}
//                            handleOverload();
//                        }
//		}
	}
        
                     
}