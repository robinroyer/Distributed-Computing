package ca.polymtl.inf4410.tp2.server;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This thread has one only purpose
 *          Checking if the other threads should stop to work
 * @author robinroyer
 */
public class CoordinateThread extends Thread {
    
        /**
         * period the trhead should sleep between two checks
         */
        private static final int CHECKING_PERIOD = 800; // => 0,8 s
    
        /**
         * Ref to the repartitor
         */
        private Repartitor repartitor;

        /**
         * Array of length 1 containing result
         */        
        private int[] result;
        
        /**
         * Semaphore protecting lock
         */
        private Semaphore resulLock;
        
        private int operationNumber;
        
      
        CoordinateThread(Repartitor repart,  int[] globalResult, Semaphore globalResultLock, int operationNum) {
                repartitor = repart;
                result = globalResult;
                resulLock = globalResultLock;
                operationNumber = operationNum;
        }

	@Override
	public void run() {
                
		while (repartitor.threadsShouldContinue()){                                			
                    try {
                        resulLock.acquire();
                        if(result[1] == operationNumber){
                                repartitor.stopTheThreads();
                        }
                        resulLock.release();
                        // check for all operations done
                        sleep(CHECKING_PERIOD);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(CoordinateThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
		}
	}

}
