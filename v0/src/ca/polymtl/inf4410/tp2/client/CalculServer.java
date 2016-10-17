/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.polymtl.inf4410.tp2.client;
import ca.polymtl.inf4410.tp2.shared.CalculServerInterface;
import ca.polymtl.inf4410.tp2.shared.OverloadedServerException;
import ca.polymtl.inf4410.tp2.shared.ServerInterface;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * CalculServer implement the server that will procceed calculation
 * 
 * @author robinroyer
 */
public class CalculServer implements CalculServerInterface{

    int capacity;
    int confidence;
    
    
    /**
    * Main to run the server.
    * 
    * @param args
    */
    public static void main(String[] args) {
        //TODO: add args to constructor
        CalculServer server = new CalculServer();
        server.run();
    }
    
    /**
    * Main method to run the server.
    */
    private void run() {
        if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
        }

        try {
                ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(this, 0);
                Registry registry = LocateRegistry.getRegistry();
                registry.rebind("server", stub);
                System.out.println("Server ready.");
        } catch (ConnectException e) {
                System.err.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lance ?");
                System.err.println();
                System.err.println("Erreur: " + e.getMessage());
        } catch (Exception e) {
                System.err.println("Erreur: " + e.getMessage());
        }
    }
    
    
    /**
     * Constructor with args
     */
    public CalculServer(int port){        
    }  
    
    /**
     * Deafault Constructor
     */
    public CalculServer(){        
    }    

    @Override
    public int calculate(String[] operations) throws RemoteException, OverloadedServerException {
        // TODO: implement calcul from operations
        return -1;
    }
    
}
