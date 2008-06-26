/*
 *  Copyright 2004-2006 Stefan Reuter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.asteriskjava.manager.action;

/**
 * The HangupAction causes the pbx to hang up a given channel.
 * 
 * @author srt
 * @version $Id: HangupAction.java 938 2007-12-31 03:23:38Z srt $
 */
public class HangupAction extends AbstractManagerAction
{
    /**
     * Serializable version identifier
     */
    static final long serialVersionUID = 3479615884618713986L;

    private String channel;

    /**
     * Creates a new empty HangupAction.
     */
    public HangupAction()
    {

    }

    /**
     * Creates a new HangupAction that hangs up the given channel.
     * 
     * @param channel the name of the channel to hangup.
     * @since 0.2
     */
    public HangupAction(String channel)
    {
        this.channel = channel;
    }

    /**
     * Returns the name of this action, i.e. "Hangup".
     */
    @Override
   public String getAction()
    {
        return "Hangup";
    }

    /**
     * Returns the name of the channel to hangup.
     * 
     * @return the name of the channel to hangup.
     */
    public String getChannel()
    {
        return channel;
    }

    /**
     * Sets the name of the channel to hangup.
     * 
     * @param channel the name of the channel to hangup.
     */
    public void setChannel(String channel)
    {
        this.channel = channel;
    }
}
