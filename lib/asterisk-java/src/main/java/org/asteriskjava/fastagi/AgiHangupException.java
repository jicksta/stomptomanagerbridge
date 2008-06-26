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
package org.asteriskjava.fastagi;

/**
 * The AgiHangupException is thrown if the channel is hung up while
 * processing the {@link org.asteriskjava.fastagi.AgiRequest}.
 * 
 * @author srt
 * @version $Id: AgiHangupException.java 938 2007-12-31 03:23:38Z srt $
 */
public class AgiHangupException extends AgiException
{
    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 3256444698691252274L;
    
    /**
     * Creates a new AgiHangupException.
     */
    public AgiHangupException()
    {
        super("Channel was hung up.");
    }
}
