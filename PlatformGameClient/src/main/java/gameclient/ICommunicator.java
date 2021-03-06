/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gameclient;

import PlatformGameShared.Interfaces.IPlatformGameClient;
import PlatformGameShared.Messages.Client.PlatformGameMessage;

/**
 * Interface of Communicator in order to 
 * 1) start and stop connection;
 * 2) register and unregister properties;
 * 3) subscribe to and unsubscribe from properties;
 * 4) update properties by sending a message all clients that are
 *    subscribed to the property of the message.
 * 
 * @author Nico Kuijpers
 */
public interface ICommunicator {
    
    /**
     * Start the connection.
     */
    public void start();
    
    /**
     * Stop the connection.
     */
    public void stop();
    

    /**
     * Sends a message to a server
     * @param message the message to be sent
     */
    public void sendMessage(PlatformGameMessage message);


    /**
     * Sets the game client to do things with on message responses
     * @param platformGameClient the instance where we will send back stuff to
     */
    public void setGameClient(IPlatformGameClient platformGameClient);

}
