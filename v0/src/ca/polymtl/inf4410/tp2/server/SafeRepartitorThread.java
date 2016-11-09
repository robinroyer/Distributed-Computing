package ca.polymtl.inf4410.tp2.server;

import java.rmi.RemoteException;
import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;


public class SafeRepartitorThread extends Thread {

        /**
         * The default capacity of server
         */
        protected static final int DEFAULT_CAPACITY = 3;
        
        /**
         * Strub server
         */
	protected CalculousServerInterface serverStub; 
        
        /**
         * Ref to the repartitor
         */
        protected Repartitor repartitor;
        
        /**         
         * String list of unit operation
         */
	protected ArrayList<String> calculous;
        
        /**
         * calculous semaphore         
         */
        protected Semaphore calculousLock;
             
        /**
         * Array of length 1 containing result
         */        
        protected int[] result;
        
        /**
         * Lock to protect global result
         */
        protected Semaphore resultLock;
        
        /**
         * Array of calculous owned by this thread
         */
        protected String[] calculousOwnedByThread;
        
        
        /**
         * What should be the next capacity
         */
        protected int nextCapacity;
        
        /**
         * Boolean : true until the first overload
         */
        protected boolean shouldIncreaseLoad;
        
        /**
         * 
         */
        protected int actualResult;

        SafeRepartitorThread(Repartitor repart, CalculousServerInterface server, ArrayList<String> calculations, Semaphore calculationsSemaphore, int[] globalResult, Semaphore globalResultLock) {
                // stub and repartitor ref
                repartitor = repart;
                serverStub = server;
                // calculous and semaphore
                calculous = calculations;
                calculousLock = calculationsSemaphore;
                //result and semaphore
                result = globalResult;
                resultLock = globalResultLock;
                // init the capacity and boolean with the default value
                nextCapacity = DEFAULT_CAPACITY;
                shouldIncreaseLoad = true;
        }

	@Override
	public void run() {
                actualResult = 0;
                int operationNumber = 0;
		while (repartitor.threadsShouldContinue()){                                			
                        try {
                                operationNumber = threadedPickingCalculous();
                                actualResult = calculate(serverStub, calculousOwnedByThread);
                                threadedAddingResult(actualResult, operationNumber);
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
		}
	}


	protected int calculate(CalculousServerInterface server, String operations[])
			throws RemoteException, OverloadedServerException {
                System.out.println("Sending operations =>");
                for (String operation : operations) {
                        System.out.println(operation);
                }
		return server.calculate(operations);
	}
        
        protected int threadedPickingCalculous() throws NoMoreWorkException, InterruptedException{        
		if (calculous.isEmpty())
			throw new NoMoreWorkException();	                               
                
                // local var and iterator
                int calculousNumber = 0;
                
                ArrayList<String> temp = new ArrayList<>();

		// Protect the datastructure by using semaphore  /!\ CRITICAL ZONE                
                calculousLock.acquire();
                Iterator<String> i = calculous.iterator();
		while (i.hasNext() && calculousNumber < nextCapacity) {
                        temp.add(i.next());
                        i.remove();
                        calculousNumber++;
		}
                calculousLock.release();
                
                calculousOwnedByThread = new String[temp.size()];
                calculousOwnedByThread = temp.toArray(calculousOwnedByThread); 
                
                return calculousNumber;
        }
                    
        protected void handleOverload(){
                shouldIncreaseLoad = false;
                nextCapacity-- ;            
        }
        
        protected void handleUnderload(){
                if(shouldIncreaseLoad)
                        nextCapacity++;                
        }
        
        protected void threadedAddingResult(int toAdd, int operationNumber) throws InterruptedException{
                resultLock.acquire();
                result[0] += toAdd % 4000;
                result[1]+= operationNumber;
                resultLock.release();            
        }
        
        protected void pushBackThreadCalculousToCalculous() throws InterruptedException{	
                calculousLock.acquire();
                for (String calc : calculousOwnedByThread) {
                        if(!calc.isEmpty()){
                                calculous.add(calc);
                        }
                }
                calculousLock.release();
		calculousOwnedByThread = null;                
        }              
}
