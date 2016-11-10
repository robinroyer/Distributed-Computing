package ca.polymtl.inf4410.tp2.server;

import ca.polymtl.inf4410.tp2.shared.CalculousServerInterface;
import java.util.ArrayList;
import java.util.Arrays;

public class Task {

        /**
         * The list of calculous represented by the task
         */
	private final ArrayList<String> calculousList;
        
        /**
         * list of calculous allready checked
         */
        private final ArrayList<String> calculousToCheck;  
        
        /**
         * list of calculous that need to be checked
         */
        private final ArrayList<String> calculousChecked;  
        
        /**
         * The number of operation the task is representing
         */
	private final int operationNumberToCheck;
        
        /**
         * the number of operation allready checked
         */
        private int operationNumberChecked;    
        
        /**
         * Reference to server that had calculate the first result
         */
	private final CalculousServerInterface firstServer;
        
        /**
         * Reference to the server that proceed the verification
         */
        private CalculousServerInterface secondServer;

        /**
         * The result of the task calculation
         */
	private final int firstResult;
        
        /**
         * The result of the task verification
         */
        private int secondResult;
        
        
        /**
         * Task constructor
         * 
         * @param server server that had perform the first calculation
         * @param calculous List of calculation proceed by the first server
         * @param result Result of the first calculation by the first server
         * @param operationNumber Number of operation proceed in the first calculation
         */
	public Task(CalculousServerInterface server, String[] calculous, int result, int operationNumber) {
                // init the lists
                calculousList = new ArrayList<>();                
                calculousList.addAll(Arrays.asList(calculous));                
                calculousToCheck = new ArrayList<>(calculousList);
                calculousChecked = new ArrayList<>();
                                
		this.firstServer = server;
                this.secondServer = null;
                
		this.firstResult = result;
                this.secondResult = 0;
                
                this.operationNumberToCheck = operationNumber;                
                this.operationNumberChecked = 0;                                              
	}
	        
        /**
         * Add a result and a number of operation to the task secondResult
         * @param result actual result to add
         * @param list list of calculous coresponding to that result
         * @param numberOfOperations number of operation proceed
         */
        public void addVerificationResult(int result, String[] list, int numberOfOperations){
                secondResult += result;
                secondResult %= 4000;
                calculousChecked.addAll(Arrays.asList(list));
                operationNumberChecked += numberOfOperations;
        }
        
        /**
         * check if a server has allready perform the first calcul and if the task
         * is allready checked
         * 
         * @param server CalculousServerInterface that we want to check 
         * @return true if server is not the firstServer
         */
        public boolean shouldBeCheckedBy(CalculousServerInterface server){ 
            return firstServer != server && secondServer == null;
        } 
        
        /**
         * secondResult getter
         * @return secondResult
         */
        public int getSecondResult(){ return secondResult; }
        
        /**
         * operationNumberToCheck getter
         * @return operationNumberToCheck
         */
        public int getInitialOperationNumber(){ return operationNumberToCheck; }
	 
        /**
         * Check that no malicious result has been proceed
         * @return True if first and second calculation give the same result
         */
        public boolean isTaskCorrect(){ return secondResult == firstResult; }
        
        /**
         * Check that verification is over
         * @return true if all calculation have been done
         */
        public boolean isTaskVerified(){
                return operationNumberChecked == operationNumberToCheck && calculousToCheck.isEmpty();
        }
        
        /**
         * Attribute a task to a server for verification
         * @param server the server the task is attribute to
         */
        public void attributeVerificationToServer(CalculousServerInterface server){
                secondServer = server;
        }
        
        /**
         * calculousList getter
         * @return calculousList
         */
        public ArrayList<String> getCalculousList(){ return calculousList; }

        /**
         * Asking the task a number of calculous that will be remove from calculousToCheck
         * getCalculous will return less than nextCapacity if calculousToCheck is empty
         * 
         * @param nextCapacity number of operation asked
         * @return return maximum of operation that can be send
         */
        public String[] getCalculous(int nextCapacity) {            
                ArrayList<String> temp = new ArrayList<>();              
                for (int i = 0; i < nextCapacity && i < calculousToCheck.size(); i++) {  
                    temp.add(calculousToCheck.remove(i));                     
                }
                String [] ret = new String[temp.size()];
                ret = temp.toArray(ret);
                return ret;
        }
        
        /**
         * push back an array of calculous to the calculousToCheck
         * 
         * @param calcs araay of calculous
         */
        public void pushBackCalculousToTask(String [] calcs) {	
            calculousToCheck.addAll(Arrays.asList(calcs));
        }     

        /**
         * Overide to string in a debug purpose
         * @return String representing a task
         */
        @Override
        public String toString() {
            return "TASK : \r\n"
                    + "operationNumberChecked is " + operationNumberChecked + "\r\n"
                    + "operationNumberToCheck is " + operationNumberToCheck + "\r\n"
                    + "firstResult is " + firstResult + "\r\n"
                    + "secondResult is " + secondResult + "\r\n";
        }               
}
