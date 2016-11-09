package ca.polymtl.inf4410.tp2.server;

public class NoMoreWorkToVerifyException extends Exception {
	/**
	 * Best practices for each class extenting or implenting Serializable interface.
	 */
	private static final long serialVersionUID = 592932191259440100L;
	

	/**
	 * Default constructor
	 */
	public NoMoreWorkToVerifyException() {
	}

	/**
	 * Normal constructor
         * @param message The error message
        */
	public NoMoreWorkToVerifyException(String message) {
		super(message);	
	}	

	/**
	 * @param cause
	 */
	public NoMoreWorkToVerifyException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoMoreWorkToVerifyException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public NoMoreWorkToVerifyException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	@Override
	public String toString() {
		return this.getMessage();
	}
}
