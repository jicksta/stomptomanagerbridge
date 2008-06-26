/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package us.twinners;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import org.asteriskjava.manager.action.ManagerAction;
import org.asteriskjava.manager.internal.ManagerUtil;
import org.asteriskjava.manager.internal.ManagerWriterImpl;

/**
 *
 * @author jicksta
 */
public class DynamicManagerWriter extends ManagerWriterImpl {

    protected final String LINE_SEPARATOR = "\r\n";

    public void sendAction(final ManagerAction managerAction, final String internalActionId) throws IOException {

        if (managerAction instanceof DynamicManagerAction) {

            DynamicManagerAction dynamicAction = (DynamicManagerAction) managerAction;
            String actionName = dynamicAction.getAction();
            Map<String, String> properties = dynamicAction.properties;

            StringBuilder buffer = new StringBuilder();
            // WHERE I LEFT OFF: SUPER MUST BE CALLED. THIS METHOD IS NOT DONE BUILDING A MANAGER ACTION PROPERLY. WHAT ELSE DID I LEVAE OUT? CHECK OTHER MANAGERACTION IMPLEMENTATION.
            // Add the action name
            appendKeyValue(buffer, "Action: ", actionName);

            // Ensure we've sent an ActionID through
            if (internalActionId != null) {
                String actionId = ManagerUtil.addInternalActionId(dynamicAction.getActionId(), internalActionId);
                appendKeyValue(buffer, "actionid: ", actionId);
            } else if (dynamicAction.getActionId() != null) {
                appendKeyValue(buffer, "actionid: ", dynamicAction.getActionId());
            }

            // Inject across our dynamic fields building up a String.
            Iterator propertyIterator = properties.entrySet().iterator();
            while (propertyIterator.hasNext()) {
                Map.Entry<String, String> pairs = (Map.Entry<String, String>) propertyIterator.next();
                appendKeyValue(buffer, pairs.getKey(), pairs.getValue());
            }
            String action = buffer.toString();

            if (this.socket == null) {
                throw new IllegalStateException("Unable to send action: socket is null");
            }

            // Finally write to the socket
            synchronized (this.socket) {
                this.socket.write(action);
                this.socket.flush();
            }
        } else {
            // This is copied from ManagerWriterImpl.java
            final String actionString;
            if (socket == null) {
                throw new IllegalStateException("Unable to send action: socket is null");
            }
            synchronized (socket) {
                actionString = actionBuilder.buildAction(managerAction, internalActionId);

                socket.write(actionString);
                socket.flush();
            }
        }
    }

    private void appendKeyValue(StringBuilder buffer, String key, String value) {
        buffer.append(key);
        buffer.append(": ");
        buffer.append(value);
        buffer.append(LINE_SEPARATOR);
    }
}
