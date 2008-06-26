/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.twinners;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.LoginException;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.ManagerEvent;

import net.ser1.stomp.Client;
import org.asteriskjava.manager.SendActionCallback;
import org.asteriskjava.manager.response.ManagerResponse;

/**
 *
 * @author jicksta
 */
public class StompToManagerBridge implements ManagerEventListener,
        net.ser1.stomp.Listener, SendActionCallback {

    public final static int RECONNECT_RETRY_TIMEOUT_IN_SECONDS = 3;
    private DynamicManagerConnection managerConnection;
    private Client stompClient;

    public StompToManagerBridge(Client stompClient) throws IOException {
        this.stompClient = stompClient;
        this.managerConnection = new DynamicManagerConnection("xenu", "jicksta", "ohairoflcopter");
    }

    public void run() throws IOException, AuthenticationFailedException, TimeoutException, InterruptedException {
        stompClient.subscribe("bridge", this);
        managerConnection.addEventListener(this);
        managerConnection.login();
    }

    // Forward Asterisk Manager Interface events to Stomp
    public void onManagerEvent(ManagerEvent event) {
        System.out.println("Handling event: " + event.toString());
        this.stompClient.send("ami", "event", event.properties());
    }

    // Forward Stomp messages to the Asterisk Manager Interface
    public void message(Map headers, final String actionName) {
        headers = stripStompRelatedHeaders(headers);
        System.out.println("Handling Stomp action " + actionName + ": " + headers.toString());
        try {
            managerConnection.sendAction(new DynamicManagerAction(actionName, headers), this);
        } catch (IOException ex) {
            Logger.getLogger(StompToManagerBridge.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalStateException ex) {
            Logger.getLogger(StompToManagerBridge.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Execute this when responses come back from AMI
    public void onResponse(ManagerResponse response) {
        System.out.println("Got AMI response " + response.toString());
    }

    private Map<String, String> stripStompRelatedHeaders(Map<String, String> headers) {
        /*Action: DBPut
        actionid: 10804947_4#2320607
        key: foo
        content-length: 5
        value: bar
        destination: bridge
        family: uberfam
        content-type: text/plain; charset=UTF-8
        message-id: msg-#stompcma-144
        */
        Map<String,String> newHeaders = new HashMap<String,String>();
        for(String key : headers.keySet()) {
            if(key.equals("content-length") || key.equals("message-id") || key.equals("content-type")) continue;
            newHeaders.put(key, headers.get(key));
        }
        return newHeaders;
    }

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
