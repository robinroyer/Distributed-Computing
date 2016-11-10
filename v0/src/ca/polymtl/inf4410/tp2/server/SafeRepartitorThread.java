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
         * Strub server handle by this thread
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
         * What should be the server next capacity
         */
        protected int nextCapacity;
        
        /**
         * Boolean : true until the first overload
         */
        protected boolean shouldIncreaseLoad;        
        

        /**
         * SafeRepartitorThread constructor
         * 
         * @param repart Repartitor reference
         * @param server CalculousServerInterface server handle by the thread
         * @param calculations shared String ArrayList of calculous
         * @param calculationsSemaphore Semaphore protecting calculations
         * @param globalResult Array containging [0] shared result, [1] operations allready proceed 
         * @param globalResultLock Semaphore protecting globalResult
         */
        public SafeRepartitorThread(Repartitor repart, CalculousServerInterface server, ArrayList<String> calculations, Semaphore calculationsSemaphore, int[] globalResult, Semaphore globalResultLock) {
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

        /**
         * loop over calculous, safely take operations and proceed them until 
         * shared boolean threadsShouldContinue is set to true
         */
	@Override
	public void run() {
                int actualResult;
                int operationNumber;
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

        /**
         * Calling calculate on the Array of operation to the remote server
         * 
         * @param server CalculousServerInterface that will proceed the calculation
         * @param operations array of string containing the operations
         * @return result proceed by the server (it can be malicious)
         * @throws RemoteException Rmi exception
         * @throws OverloadedServerException exception thrown if we sent too
         *         much operations to the server
         */
	protected int calculate(CalculousServerInterface server, String operations[])
			throws RemoteException, OverloadedServerException {
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
            
        /**
         * Lower the nextCapacity & stop increasing capacity
         */
        protected void handleOverload(){
                shouldIncreaseLoad = false;
                nextCapacity-- ;            
        }
        
        /**
         * increase nextCapacity until one overloadException
         */
        protected void handleUnderload(){
                if(shouldIncreaseLoad)
                        nextCapacity++;                
        }
        
        /**
         * Safe method to add result to globalResult in repartitor
         * 
         * @param toAdd result of the operation
         * @param operationNumber number of operation proceed by the server
         * @throws InterruptedException 
         */
        protected void threadedAddingResult(int toAdd, int operationNumber) throws InterruptedException{
                resultLock.acquire();
                result[0] += toAdd;
                result[0] %= 4000;                
                result[1] += operationNumber;
                resultLock.release();            
        }
        
        /**
         * Safe Sending back calculous operation taken by the
         * thread to the shared calculous list
         * 
         * @throws InterruptedException 
         */
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
