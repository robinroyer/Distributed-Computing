package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;
import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
import java.util.ArrayList;
import java.util.Arrays;
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
         */
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
                        calculousOwnedByThread = null;
                        
                        // picking a task to verify
                        try{
                                taskToCheck = threadedPickingTaskToVerify();
                                if(taskToCheck != null){
                                        while(!taskToCheck.isTaskVerified()){
                                            
                                            try {
                                                proceedTaskVerification(taskToCheck);
                                                handleUnderload();
                                            } catch (Exception e) {
                                                handleOverload();
                                            }
                                        }
                                }
                                
                                // verification is over
                                if(taskToCheck.isTaskCorrect()){
                                    threadedAddingTaskToResult(taskToCheck);
                                }else{
                                    threadedInvalidateTask(taskToCheck);
                                }
                                taskToCheck = null;
                        }catch(Exception e){}
		}
	}
        
        /**
         * 
         * @param toAdd
         * @param operationNumber
         * @throws InterruptedException 
         */
        private void threadedAddingTask(int toAdd, int operationNumber) throws InterruptedException{
                Task newTask = new Task(serverStub, calculousOwnedByThread, toAdd, operationNumber);
                // CRITICAL ZONE
                tasksLock.acquire();
                tasks.add(newTask);
                tasksLock.release();            
        }
         
        /**
         * 
         * @return
         * @throws InterruptedException 
         */
        private Task threadedPickingTaskToVerify() throws InterruptedException{
                Task pivot = null;
                // CRITICAL ZONE                
                tasksLock.acquire();
                for (Task task : tasks) {
                        if (task.shouldBeChecked()) {
                                pivot = task;
                                pivot.attributeVerificationToServer(this.serverStub);
                        }
                }
                tasksLock.release();
                return pivot;
        }           
        
        /**
         * 
         * @param task
         * @throws InterruptedException 
         */
        private void threadedAddingTaskToResult(Task task) throws InterruptedException{
                // CRITICAL ZONE                
                resultLock.acquire();
                result[0] += task.getSecondResult() % 4000;
                result[1]+= task.getInitialOperationNumber();
                resultLock.release();            
        }
         
        /**
         * 
         * @param task
         * @throws InterruptedException 
         */
        private void threadedInvalidateTask(Task task) throws InterruptedException{
                String [] calculousToPushBack = new String[task.calculousList.size()];
                for (int i = 0; i < task.calculousList.size(); i++) {
                    calculousToPushBack[i] = task.calculousList.get(i);            
                }
                // CRITICAL ZONE                
                calculousLock.acquire();
                calculous.addAll(Arrays.asList(calculousToPushBack));
                calculousLock.release();
        }
        
        /**
         * 
         * @param t
         * @throws RemoteException 
         */
        private void proceedTaskVerification(Task t) throws RemoteException{
                calculousOwnedByThread = t.getCalculous(nextCapacity);
                try {
                        actualResult = calculate(serverStub, calculousOwnedByThread);
                        taskToCheck.addVerificationResult(actualResult, calculousOwnedByThread, calculousOwnedByThread.length);
                        handleUnderload();
                } catch (OverloadedServerException e) {
                        // rendre les calculous to thread
                        handleOverload();
                }                
        }                
}