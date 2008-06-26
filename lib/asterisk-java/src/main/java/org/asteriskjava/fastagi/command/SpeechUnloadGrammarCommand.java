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
package org.asteriskjava.fastagi.command;

/**
 * Unloads the specified grammar.<p>
 * Available since Asterisk 1.6.
 *
 * @author srt
 * @version $Id: SpeechUnloadGrammarCommand.java 1013 2008-03-31 06:51:03Z srt $
 * @see SpeechLoadGrammarCommand
 * @since 1.0.0
 */
public class SpeechUnloadGrammarCommand extends AbstractAgiCommand
{
    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 1L;
    private String name;

    /**
     * Creates a new SpeechUnloadGrammarCommand that unloads the given grammer.
     *
     * @param name the name of the grammar.
     */
    public SpeechUnloadGrammarCommand(String name)
    {
        this.name = name;
    }

    /**
     * Returns the name of the grammar.
     *
     * @return the name of the grammar.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Sets the name of the grammar.
     *
     * @param name the name of the grammar.
     */
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String buildCommand()
    {
        return "SPEECH UNLOAD GRAMMAR " + escapeAndQuote(name);
    }
}