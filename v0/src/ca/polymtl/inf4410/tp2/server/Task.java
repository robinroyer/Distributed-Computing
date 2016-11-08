package ca.polymtl.inf4410.tp2.server;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import java.util.ArrayList;

public class Task {

	public ArrayList<String> calculousList;
        public ArrayList<String> calculousToCheck;  
        public ArrayList<String> calculousChecked;  
        
	private final int operationNumberToCheck;
        private int operationNumberChecked;

        
        
        
	public CalculousServerInterface firstServer;	
        public CalculousServerInterface secondServer;

	private final int firstResult;
        private int secondResult;

        
        
	
	public Task(CalculousServerInterface server, String[] calculous, int result, int operationNumber) {
                // init the lists
                calculousList = new ArrayList<>();                
                for (String calc : calculous) {
                        calculousList.add(calc);
                }                
                calculousToCheck = new ArrayList<String>(calculousList);
                calculousChecked = new ArrayList<String>();
                
		this.firstServer = server;
                this.secondServer = null;
                
		this.firstResult = result;
                this.secondResult = 0;
                
                this.operationNumberToCheck = operationNumber;                
                this.operationNumberChecked = 0;                                              
	}
	        
        
        public void addVerificationResult(int result, String[] list, int numberOfOperations){
            secondResult += result;
            for (String calc : list) {
                    calculousChecked.add(calc);
            }
            operationNumberChecked += numberOfOperations;
        }
        
        public boolean shouldBeChecked(){ return secondServer == null; }       
	       
        public boolean isTaskCorrect(int secondResult){ return secondResult == firstResult; }
        
        public boolean isTaskVerified(){
            return operationNumberChecked == operationNumberToCheck && calculousToCheck.isEmpty();
        }
        
        public void attributeVerificationToServer(CalculousServerInterface server){
            secondServer = server;
        }
        
}
