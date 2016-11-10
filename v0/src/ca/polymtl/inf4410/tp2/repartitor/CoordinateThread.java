package ca.polymtl.inf4410.tp2.repartitor;

import java.util.concurrent.Semaphore;

/**
 * This thread has only one only : checking if the other threads should stop
 * working or not.
 * 
 * @author robinroyer
 */
public class CoordinateThread extends Thread {

	/**
	 * period the trhead should sleep between two checks
	 */
	private static final int CHECKING_PERIOD = 800;

	/**
	 * Ref to the repartitor
	 */
	private final Repartitor repartitor;

	/**
	 * Array of length 1 containing result
	 */
	private final int[] result;

	/**
	 * Semaphore protecting lock
	 */
	private final Semaphore resulLock;

	/**
	 * The number of operation the repartitor received
	 */
	private final int operationNumber;

	/**
	 * CoordinateThread constructor
	 * 
	 * @param repart
	 *            repartitor reference
	 * @param globalResult
	 *            Array ref containing the result [0] & the number of operation
	 *            [1]
	 * @param globalResultLock
	 *            Semaphore on globalResult
	 * @param operationNum
	 *            initial number of operation that should be calculated
	 */
	public CoordinateThread(Repartitor repart, int[] globalResult, Semaphore globalResultLock, int operationNum) {
		repartitor = repart;
		result = globalResult;
		resulLock = globalResultLock;
		operationNumber = operationNum;
	}

	@Override
	public void run() {
		ThreadedCoordination();
	}

	/**
	 * Checking if every operation has been validated in order to coordinate the
	 * threads terminaison every CHECKING_PERIOD milliseconds
	 */
	private void ThreadedCoordination() {
		while (repartitor.threadsShouldContinue()) {
			try {
				resulLock.acquire();
				if (result[1] == operationNumber) {
					repartitor.stopTheThreads();
				}
				// check for all operations done
				sleep(CHECKING_PERIOD);
			} catch (InterruptedException ex) {
				System.err.println("Interruption de thread reçue.");
			} finally {
				// security : unlock the semaphore
				resulLock.release();
			}
		}
	}
}
