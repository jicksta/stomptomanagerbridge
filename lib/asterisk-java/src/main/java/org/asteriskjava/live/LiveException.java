/*
 *  Copyright 2005-2006 Stefan Reuter
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
package org.asteriskjava.live;

/**
 * Base class for exceptions thrown by the live package.
 * 
 * @author srt
 * @version $Id: LiveException.java 938 2007-12-31 03:23:38Z srt $
 */
public class LiveException extends Exception
{
    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = -7422839292479814036L;

    /**
     * Creates a new instance with the given message.
     * 
     * @param message the message
     */
    protected LiveException(String message)
    {
        super(message);
    }

    /**
     * Creates a new instance with the given message and cause.
     * 
     * @param message the message
     * @param cause the cause
     */
    protected LiveException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
