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
        private static final int DEFAULT_CAPACITY = 3;
        
        /**
         * Strub server
         */
	private CalculousServerInterface serverStub; 
        
        /**
         * Ref to the repartitor
         */
        private Repartitor repartitor;

        
        /**         
         * String list of unit operation
         */
	private ArrayList<String> calculous;
        
        /**
         * calculous semaphore         
         */
        private Semaphore calculousLock;
             
        /**
         * Array of length 1 containing result
         */        
        private int[] result;
        
        /**
         * Lock to protect global result
         */
        private Semaphore resultLock;
        
        /**
         * Array of calculous owned by this thread
         */
        private String[] calculousOwnedByThread;
        
        
        /**
         * What should be the next capacity
         */
        private int nextCapacity;
        
        /**
         * Boolean : true until the first overload
         */
        private boolean shouldIncreaseLoad;
        

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
                int res = 0;
                
		while (repartitor.threadsShouldContinue()){                                			
                        try {
                                threadedPickingCalculous();
                                res = calculate(serverStub, calculousOwnedByThread);
                                threadedAddingResult(res);
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


	private int calculate(CalculousServerInterface server, String operations[])
			throws RemoteException, OverloadedServerException {
		return server.calculate(operations);
	}
        
        private void threadedPickingCalculous() throws NoMoreWorkException, InterruptedException{        
		if (calculous.isEmpty())
			throw new NoMoreWorkException();	
                
                calculousOwnedByThread = new String[nextCapacity];
                
                // local var and iterator
                int calculousNumber = 0;
                Iterator<String> i = calculous.iterator();

		// Protect the datastructure by using semaphore  /!\ CRITICAL ZONE                
                calculousLock.acquire();
		while (i.hasNext() && calculousNumber < nextCapacity) {
                        String unitCalculous = i.next();
			calculousOwnedByThread[calculousNumber] = unitCalculous;                        
			calculous.remove(unitCalculous);
                        calculousNumber++;
		}
                calculousLock.release();
        }
                    
        private void handleOverload(){
                shouldIncreaseLoad = false;
                nextCapacity-- ;            
        }
        
        private void handleUnderload(){
                if(shouldIncreaseLoad)
                        nextCapacity++;                
        }
        
        private void threadedAddingResult(int toAdd) throws InterruptedException{
                resultLock.acquire();
                result[0] += toAdd % 4000;
                resultLock.release();            
        }
        
        private void pushBackThreadCalculousToCalculous() throws InterruptedException{	
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
