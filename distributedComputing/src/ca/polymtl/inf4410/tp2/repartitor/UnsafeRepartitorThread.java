package ca.polymtl.inf4410.tp2.repartitor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;

public class UnsafeRepartitorThread extends SafeRepartitorThread {

	/**
	 * List of task proceed once by one other server
	 */
	private final ArrayList<Task> tasks;

	/**
	 * task list semaphore
	 */
	private final Semaphore tasksLock;

	/**
	 * task that is now checked by the server
	 */
	private Task taskToCheck;

	/**
	 * UnsafeRepartitorThread constructor
	 * 
	 * @param repart
	 *            Reference to the shared repartitor
	 * @param server
	 *            Reference to the server handle by this thread
	 * @param calculations
	 *            shared list of calculous
	 * @param calculationsSemaphore
	 *            Semaphore protecting calculations
	 * @param globalResult
	 * @param globalResultLock
	 * @param taskList
	 * @param taskListLock
	 */
	public UnsafeRepartitorThread(Repartitor repart, CalculousServerInterface server, ArrayList<String> calculations,
			Semaphore calculationsSemaphore, int[] globalResult, Semaphore globalResultLock, ArrayList<Task> taskList,
			Semaphore taskListLock) {

		super(repart, server, calculations, calculationsSemaphore, globalResult, globalResultLock);

		tasks = taskList;
		tasksLock = taskListLock;
	}

	/**
	 * looping over the calculous list and the task list until the coordination
	 * thread toggle the shared boolean threadsShouldContinue()
	 */
	@Override
	public void run() {
		int res;
		int operationNumber;
		while (repartitor.threadsShouldContinue()) {

			// Picking unit operation to calculate
			try {
				operationNumber = threadedPickingCalculous();
				res = calculate(serverStub, calculousOwnedByThread);
				threadedAddingTask(res, operationNumber, calculousOwnedByThread);
				handleUnderload();
			} catch (NoMoreWorkException | InterruptedException | RemoteException e) {
			} catch (OverloadedServerException ex) {
				try {
					pushBackThreadCalculousToCalculous();
				} catch (InterruptedException ex1) {
				}
				handleOverload();
			}
			calculousOwnedByThread = null;

			// picking a task to verify
			try {
				taskToCheck = threadedPickingTaskToVerify();
				while (!taskToCheck.isTaskVerified()) {
					try {
						proceedTaskVerification(taskToCheck);
						handleUnderload();
					} catch (Exception e) {
						handleOverload();
					}
				}

				// verification is over
				if (taskToCheck.isTaskCorrect()) {
					threadedAddingResult(taskToCheck.getSecondResult(), taskToCheck.getInitialOperationNumber());
				} else {
					threadedInvalidateTask(taskToCheck);
				}
				taskToCheck = null;
			} catch (NoMoreWorkToVerifyException e) {
				// => continue in loop
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			calculousOwnedByThread = null;
		}
	}

	/**
	 * Create a new task and safe adding it to the shared task list
	 * 
	 * @param toAdd
	 *            result corresponding to the calculous Array in parameters
	 * @param operationNumber
	 *            number of operation proceeded
	 * @param calculous
	 *            string array of calculous proceeded
	 * @throws InterruptedException
	 */
	private void threadedAddingTask(int toAdd, int operationNumber, String[] calculous) throws InterruptedException {
		Task newTask = new Task(serverStub, calculous, toAdd, operationNumber);
		// CRITICAL ZONE
		tasksLock.acquire();
		tasks.add(newTask);
		tasksLock.release();
	}

	/**
	 * Safe removing a thread chosen to be verified by serverStub and returning
	 * it
	 * 
	 * @return the task chosen
	 * @throws InterruptedException
	 */
	private Task threadedPickingTaskToVerify() throws InterruptedException, NoMoreWorkToVerifyException {
		Task pivot = null;
		// CRITICAL ZONE
		tasksLock.acquire();
		for (Task task : tasks) {
			if (task.shouldBeCheckedBy(this.serverStub)) {
				pivot = task;
				pivot.attributeVerificationToServer(this.serverStub);
				break;
			}
		}
		tasksLock.release();

		if (pivot == null)
			throw new NoMoreWorkToVerifyException();

		return pivot;
	}

	/**
	 * Invalide a task when the first and second result are not the same split
	 * it by putting back its calculous to the shared calculous list
	 * 
	 * @param task
	 *            Invalide task to split
	 * @throws InterruptedException
	 */
	private void threadedInvalidateTask(Task task) throws InterruptedException {
		String[] calculousToPushBack = new String[task.getCalculousList().size()];
		for (int i = 0; i < task.getCalculousList().size(); i++) {
			calculousToPushBack[i] = task.getCalculousList().get(i);
		}
		// CRITICAL ZONE
		calculousLock.acquire();
		calculous.addAll(Arrays.asList(calculousToPushBack));
		calculousLock.release();
	}

	/**
	 * get a number of calculous from the task, depending on the nextCapacity
	 * and request the server to calculate them and add that result to the task
	 * 
	 * @param task
	 *            the task that should be verificate
	 * @throws RemoteException
	 */
	private void proceedTaskVerification(Task task) throws RemoteException {
		calculousOwnedByThread = task.getCalculous(nextCapacity);
		int actualResult;
		try {
			actualResult = calculate(serverStub, calculousOwnedByThread);
			taskToCheck.addVerificationResult(actualResult, calculousOwnedByThread, calculousOwnedByThread.length);
			handleUnderload();
		} catch (OverloadedServerException e) {
			task.pushBackCalculousToTask(calculousOwnedByThread);
			handleOverload();
		}
	}
}