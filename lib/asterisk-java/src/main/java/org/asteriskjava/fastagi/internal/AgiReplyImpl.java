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
package org.asteriskjava.fastagi.internal;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.asteriskjava.fastagi.reply.AgiReply;


/**
 * Default implementation of the AgiReply interface.
 *
 * @author srt
 * @version $Id: AgiReplyImpl.java 1009 2008-03-28 01:22:38Z srt $
 */
public class AgiReplyImpl implements Serializable, AgiReply
{
    private static final Pattern STATUS_PATTERN = Pattern.compile("^(\\d{3})[ -]");
    private static final Pattern RESULT_PATTERN = Pattern.compile("^200 result= *(\\S+)");
    private static final Pattern PARENTHESIS_PATTERN = Pattern.compile("^200 result=\\S* +\\((.*)\\)");
    private static final Pattern ADDITIONAL_ATTRIBUTES_PATTERN = Pattern.compile("^200 result=\\S* +(\\(.*\\) )?(.+)$");
    private static final Pattern ADDITIONAL_ATTRIBUTE_PATTERN = Pattern.compile("(\\S+)=(\\S+)");
    private static final Pattern SYNOPSIS_PATTERN = Pattern.compile("^\\s*Usage:\\s*(.*)\\s*$");
    private static final String END_OF_PROPER_USAGE = "520 End of proper usage.";

    private Matcher matcher;

    /**
     * Serial version identifier.
     */
    private static final long serialVersionUID = 3256727294671337012L;

    private List<String> lines;
    private String firstLine;

    /**
     * The result, that is the part directly following the "result=" string.
     */
    private String result;

    /**
     * The status code.
     */
    private Integer status;

    /**
     * Additional attributes contained in this reply, for example endpos.
     */
    private Map<String, String> attributes;

    /**
     * The contents of the parenthesis.
     */
    private String extra;

    /**
     * In case of status == 520 (invalid command syntax) this attribute contains
     * the synopsis of the command.
     */
    private String synopsis;

    /**
     * In case of status == 520 (invalid command syntax) this attribute contains
     * the usage of the command.
     */
    private String usage;

    AgiReplyImpl()
    {
        super();
    }

    AgiReplyImpl(List<String> lines)
    {
        super();
        if (lines != null)
        {
            this.lines = new ArrayList<String>(lines);
            if (!lines.isEmpty())
            {
                firstLine = lines.get(0);
            }
        }
    }

    public String getFirstLine()
    {
        return firstLine;
    }

    public List<String> getLines()
    {
        return lines;
    }

    public int getResultCode()
    {
        String result;

        result = getResult();
        if (result == null)
        {
            return -1;
        }

        try
        {
            return Integer.parseInt(result);
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    public char getResultCodeAsChar()
    {
        int resultCode;

        resultCode = getResultCode();
        if (resultCode < 0)
        {
            return 0x0;
        }

        return (char) resultCode;
    }

    public String getResult()
    {
        if (result != null)
        {
            return result;
        }

        matcher = RESULT_PATTERN.matcher(firstLine);
        if (matcher.find())
        {
            result = matcher.group(1);
        }
        return result;
    }

    public int getStatus()
    {
        if (status != null)
        {
            return status;
        }

        matcher = STATUS_PATTERN.matcher(firstLine);
        if (matcher.find())
        {
            status = Integer.parseInt(matcher.group(1));
        }
        return status;
    }

    public String getAttribute(String name)
    {
        if (getStatus() != SC_SUCCESS)
        {
            return null;
        }

        if ("result".equalsIgnoreCase(name))
        {
            return getResult();
        }

        return getAttributes().get(name.toLowerCase(Locale.ENGLISH));
    }

    protected Map<String, String> getAttributes()
    {
        if (attributes != null)
        {
            return attributes;
        }

        attributes = new HashMap<String, String>();

        matcher = ADDITIONAL_ATTRIBUTES_PATTERN.matcher(firstLine);
        if (matcher.find())
        {
            String s;
            Matcher attributeMatcher;

            s = matcher.group(2);
            attributeMatcher = ADDITIONAL_ATTRIBUTE_PATTERN.matcher(s);
            while (attributeMatcher.find())
            {
                String key;
                String value;

                key = attributeMatcher.group(1);
                value = attributeMatcher.group(2);
                attributes.put(key.toLowerCase(Locale.ENGLISH), value);
            }
        }
        return attributes;
    }

    private boolean extraCreated;

    public String getExtra()
    {
        if (getStatus() != SC_SUCCESS)
        {
            return null;
        }

        if (extraCreated)
        {
            return extra;
        }

        matcher = PARENTHESIS_PATTERN.matcher(firstLine);
        if (matcher.find())
        {
            extra = matcher.group(1);
        }
        extraCreated = true;
        return extra;
    }

    public String getSynopsis()
    {
        if (getStatus() != SC_INVALID_COMMAND_SYNTAX)
        {
            return null;
        }

        if (synopsis == null)
        {
            if (lines.size() > 1)
            {
                String secondLine;
                Matcher synopsisMatcher;

                secondLine = lines.get(1);
                synopsisMatcher = SYNOPSIS_PATTERN.matcher(secondLine);
                if (synopsisMatcher.find())
                {
                    synopsis = synopsisMatcher.group(1);
                }
            }
        }
        return synopsis;
    }

    /**
     * Returns the usage of the command sent if Asterisk expected a different
     * syntax (getStatus() == SC_INVALID_COMMAND_SYNTAX).
     *
     * @return the usage of the command sent, <code>null</code> if there were
     *         no syntax errors.
     */
    public String getUsage()
    {
        if (usage == null)
        {
            StringBuilder usageSB;

            usageSB = new StringBuilder();
            for (int i = 2; i < lines.size(); i++)
            {
                String line;

                line = lines.get(i);
                if (END_OF_PROPER_USAGE.equals(line))
                {
                    break;
                }

                usageSB.append(line.trim());
                usageSB.append(" ");
            }
            usage = usageSB.toString().trim();
        }
        return usage;
    }

    @Override
    public String toString()
    {
        StringBuilder sb;

        sb = new StringBuilder("AgiReply[");
        sb.append("status=").append(getStatus()).append(",");
        if (status == SC_SUCCESS)
        {
            sb.append("result='").append(getResult()).append("',");
            sb.append("extra='").append(getExtra()).append("',");
            sb.append("attributes=").append(getAttributes()).append(",");
        }
        if (status == SC_INVALID_COMMAND_SYNTAX)
        {
            sb.append("synopsis='").append(getSynopsis()).append("',");
        }
        sb.append("systemHashcode=").append(System.identityHashCode(this));
        sb.append("]");

        return sb.toString();
    }
}
