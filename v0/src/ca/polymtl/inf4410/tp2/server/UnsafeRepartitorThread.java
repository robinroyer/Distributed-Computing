package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;
import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;


public class UnsafeRepartitorThread extends SafeRepartitorThread {

       
        /**         
         * List of task proceed once by one other server
         */
	protected ArrayList<Task> tasks;
        
        /**
         * task list semaphore         
         */
        protected Semaphore tasksLock;

        
        /**
         * 
         * @param repart
         * @param server
         * @param calculations
         * @param calculationsSemaphore
         * @param globalResult
         * @param globalResultLock
         * @param taskList
         * @param taskListLock 
         */
        UnsafeRepartitorThread(
                Repartitor repart,
                CalculousServerInterface server,
                ArrayList<String> calculations,
                Semaphore calculationsSemaphore,
                int[] globalResult,
                Semaphore globalResultLock,
                ArrayList<Task> taskList,
                Semaphore taskListLock) {
                
                super(repart, server, calculations, calculationsSemaphore, globalResult, globalResultLock);

                tasks = taskList;        
                tasksLock = taskListLock;
        }

	@Override
	public void run() {
                int res = 0;
                int operationNumber = 0;
		while (repartitor.threadsShouldContinue()){                                			
                        try {
                                operationNumber = threadedPickingCalculous();
                                res = calculate(serverStub, calculousOwnedByThread);
                                threadedAddingTask(res, operationNumber);
                                handleUnderload();
                        }
                        catch(NoMoreWorkException | InterruptedException | RemoteException e){}
                        catch (OverloadedServerException ex) {
                            try {
                                pushBackThreadCalculousToCalculous();
                            } catch (InterruptedException ex1) {}
                            handleOverload();
                        }
		}
	}
        
        
        private void threadedAddingTask(int toAdd, int operationNumber) throws InterruptedException{
//                resultLock.acquire();
//                result[0] += toAdd % 4000;
//                result[1]+= operationNumber;
//                resultLock.release();            
        }
                
                     
}