/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package us.twinners;

import java.util.Map;
import org.asteriskjava.manager.action.ManagerAction;

/**
 *
 * @author jicksta
 */
public class DynamicManagerAction implements ManagerAction {

    protected String actionName;
    protected String actionId;
    
    protected Map<String,String> properties;
    
    DynamicManagerAction(String actionName, Map<String,String> properties) {
        this.actionName = actionName;
        this.properties = properties;
    }
    
    public String getAction() {
        return actionName;
    }

    public String getActionId() {
        return String.valueOf(hashCode());
    }
    
    public Map<String,String> getProperties() {
        return this.properties;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
    
    @Override
    public String toString() {
        return actionName + ": " + properties.toString();
    }
    
}
