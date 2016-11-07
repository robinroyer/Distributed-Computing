package ca.polymtl.inf4410.tp2.server;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;

public class Task {

	private String calculous[];
	
	private CalculousServerInterface author;
	
	public Task(CalculousServerInterface server, String[] calculous) {
		this.calculous = calculous;
		this.author = server;
	}
	
	public String[] getCalculous() {
		return calculous;
	}
	
	public CalculousServerInterface getAuthor() {
		return author;
	}

}
