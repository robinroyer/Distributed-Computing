package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;
import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
import java.util.ArrayList;
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
        
        private Task taskToCheck;

        
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
                int res;
                int operationNumber;
		while (repartitor.threadsShouldContinue()){
                        
                        // Picking unit operation to calculate
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
                        
                        // picking a task to verify
                        try{
                                threadedPickingTaskToVerify();
                        
                        }catch(Exception e){}
		}
	}
        
        
        private void threadedAddingTask(int toAdd, int operationNumber) throws InterruptedException{
                Task newTask = new Task(serverStub, calculousOwnedByThread, toAdd, operationNumber);
                // CRITICAL ZONE
                tasksLock.acquire();
                tasks.add(newTask);
                tasksLock.release();            
        }
                
        private void threadedPickingTaskToVerify() throws InterruptedException{
                // CRITICAL ZONE
                tasksLock.acquire();
                for (Task task : tasks) {
                        if (task.shouldBeChecked()) {
                                taskToCheck = task;
                                taskToCheck.attributeVerificationToServer(this.serverStub);
                        }
                }
                tasksLock.release();
        }             
}