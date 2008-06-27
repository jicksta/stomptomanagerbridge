package org.asteriskjava.manager.event;

/* Example:
Event: ConferenceDTMF
Privilege: call,all
ConferenceName: 1234567890
Channel: SIP/jsgoecke-081e4c60
CallerID: 9999
CallerIDName: Jason Goecke
Key: 2
 */

public class ConferenceDTMFEvent extends ManagerEvent {

	private static final long serialVersionUID = 3257845467553184784L;

    private String key; // DTMF key pressed
    private String conferenceName;
    private String channel;
    private String callerIdNum;
    private String callerIdName;

    public ConferenceDTMFEvent(Object source) {
        super(source);
    }
    
    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCallerId() {
        return callerIdNum;
    }

    public void setCallerId(String callerId) {
        this.callerIdNum = callerId;
    }

    public String getCallerIdNum() {
        return callerIdNum;
    }

    public void setCallerIdNum(String callerIdNum) {
        this.callerIdNum = callerIdNum;
    }

    public String getCallerIdName() {
        return callerIdName;
    }

    public void setCallerIdName(String callerIdName) {
        this.callerIdName = callerIdName;
    }
   
}