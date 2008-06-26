package us.twinners;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.internal.ManagerConnectionImpl;
import org.asteriskjava.manager.internal.ManagerWriter;
import us.twinners.DynamicManagerWriter;

/**
 * This class behaves exactly as a normal connection, except we intercept the
 * createWriter() method to handle dynamic Actions.
 * @author jicksta
 */
public class DynamicManagerConnection extends ManagerConnectionImpl {
    
    public DynamicManagerConnection(String hostname, String username, String password) {
        super();
        setHostname(hostname);
        setUsername(username);
        setPassword(password);
    }
    
    @Override
    protected ManagerWriter createWriter() {
        return new DynamicManagerWriter();
    }

    public void login() throws IOException, AuthenticationFailedException, TimeoutException {
        super.login();
    }

}
