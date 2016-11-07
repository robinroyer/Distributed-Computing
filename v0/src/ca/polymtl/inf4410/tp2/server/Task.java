package ca.polymtl.inf4410.tp2.server;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;

public class Task {

	private String calculous[];
	
	private CalculousServerInterface author;
	
	private int resulttoVerify;
	
	public Task(CalculousServerInterface server, String[] calculous) {
		this.calculous = calculous;
		this.author = server;
		this.resulttoVerify = -1;
	}
	
	public String[] getCalculous() {
		return calculous;
	}
	
	public CalculousServerInterface getAuthor() {
		return author;
	}

	/**
	 * @return the resulttoVerify
	 */
	public int getResulttoVerify() {
		return resulttoVerify;
	}

	/**
	 * @param resulttoVerify the resulttoVerify to set
	 */
	public void setResulttoVerify(int resulttoVerify) {
		this.resulttoVerify = resulttoVerify;
	}

	/**
	 * @param calculous the calculous to set
	 */
	public void setCalculous(String[] calculous) {
		this.calculous = calculous;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(CalculousServerInterface author) {
		this.author = author;
	}

}
