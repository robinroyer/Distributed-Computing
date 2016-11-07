package ca.polymtl.inf4410.tp2.server;

public class CalculousServerInformation {

	private int capacity;
	private int result;
	private boolean isOptimized;
	private boolean previousCalculHasBeenOverloaded;

	/**
	 * Default constructor
	 */
	public CalculousServerInformation() {}

	/**
	 * Normal construtor with capacity as parameter
	 * @param capacity
	 */
	public CalculousServerInformation(int capacity) {
		this.isOptimized = false;
		this.previousCalculHasBeenOverloaded = false;
		this.result = -1;
		this.capacity = capacity;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity() {
		return capacity;
	}

	/**
	 * @param capacity the capacity to set
	 */
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * @return the result
	 */
	public int getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(int result) {
		this.result = result;
	}

	/**
	 * @return the isOptimized
	 */
	public boolean isOptimized() {
		return isOptimized;
	}

	/**
	 * @param isOptimized the isOptimized to set
	 */
	public void setOptimized(boolean isOptimized) {
		this.isOptimized = isOptimized;
	}
	
	/**
	 * @return the previousCalculHasBeenOverloaded
	 */
	public boolean previousCalculHasBeenOverloaded() {
		return previousCalculHasBeenOverloaded;
	}

	/**
	 * @param isOptimized the previousCalculHasBeenOverloaded to set
	 */
	public void setPreviousCalculHasBeenOverloaded(boolean previousCalculHasBeenOverloaded) {
		this.previousCalculHasBeenOverloaded = previousCalculHasBeenOverloaded;
	}
}
