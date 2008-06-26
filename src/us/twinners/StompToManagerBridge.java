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
import us.twinners.DynamicManagerConnection;

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
    public static void main(String[] args) throws AuthenticationFailedException,
            InterruptedException, TimeoutException, LoginException {
        boolean successful = false;
        while(!successful) {
            try {
                Client stompClient = new Client("twinners.us", 61613, "testing", "f13fa60c2ba5");
                StompToManagerBridge manager = new StompToManagerBridge(stompClient);
                manager.run();
                successful = true;
            } catch (IOException ioe) {
                System.err.println("IOException thrown! Trying again!");
                Thread.sleep(RECONNECT_RETRY_TIMEOUT_IN_SECONDS * 1000);
            }
        }
    }

}
