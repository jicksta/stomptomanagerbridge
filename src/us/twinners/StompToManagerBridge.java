/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.twinners;

import java.io.IOException;

import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.ManagerEvent;

import net.ser1.stomp.Client;

/**
 *
 * @author jicksta
 */
public class StompToManagerBridge implements ManagerEventListener,
        net.ser1.stomp.Listener {

    public final static int RECONNECT_RETRY_TIMEOUT_IN_SECONDS = 3;
    private DynamicManagerConnection managerConnection;
    private Client stompClient;

    public StompToManagerBridge(Client stompClient) throws IOException {
        this.stompClient = stompClient;
        this.managerConnection = new DynamicManagerConnection("xenu", "jicksta", "ohairoflcopter");
    }

    public void run() throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException {
        managerConnection.addEventListener(this);
        managerConnection.login();
    }

    // Forward Asterisk Manager Interface events to Stomp
    public void onManagerEvent(ManagerEvent event) {
        System.out.println("Handling event: " + event.toString());
        this.stompClient.send("ami", event.toString());
    }

    // Forward Stomp messages to the Asterisk Manager Interface
    public void message(Map headers, final String actionName) {
        try {
            managerConnection.sendAction(new DynamicManagerAction(actionName, headers));
        } catch (IOException ex) {
            Logger.getLogger(StompToManagerBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TimeoutException ex) {
            Logger.getLogger(StompToManagerBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(StompToManagerBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(StompToManagerBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws AuthenticationFailedException, InterruptedException, TimeoutException, LoginException {
        boolean successful = false;
        while (!successful) {
            Client stompClient;
            try {
                stompClient = new Client("twinners.us", 61613, "testing", "f13fa60c2ba5");
            } catch (IOException ioe) {
                System.err.println("Failed to connect to Stomp! Retrying in " +
                        String.valueOf(RECONNECT_RETRY_TIMEOUT_IN_SECONDS) + " seconds.");
                Thread.sleep(RECONNECT_RETRY_TIMEOUT_IN_SECONDS * 1000);
                continue;
            }

            StompToManagerBridge manager;
            try {
                manager = new StompToManagerBridge(stompClient);
            } catch (IOException ex) {
                System.err.println("Failed to connect to AMI! Retrying in " +
                        String.valueOf(RECONNECT_RETRY_TIMEOUT_IN_SECONDS) + " seconds.");
                Thread.sleep(RECONNECT_RETRY_TIMEOUT_IN_SECONDS * 1000);
                continue;
            }
            try {
                manager.run();
            } catch (Exception ex) {
                System.err.println("Exception while running bridge! Retrying in" +
                        String.valueOf(RECONNECT_RETRY_TIMEOUT_IN_SECONDS) + " seconds.");
                Thread.sleep(RECONNECT_RETRY_TIMEOUT_IN_SECONDS * 1000);
                continue;
            }
            successful = true;
        }
    }
}
