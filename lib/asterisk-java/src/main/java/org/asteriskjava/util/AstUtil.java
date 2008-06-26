package org.asteriskjava.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Some static utility methods to imitate Asterisk specific logic.
 * <p>
 * See Asterisk's <code>util.c</code>.
 * <p>
 * Client code is not supposed to use this class.
 * 
 * @author srt
 * @version $Id: AstUtil.java 1067 2008-06-04 10:23:04Z srt $
 */
public class AstUtil
{
    private static final List<String> TRUE_LITERALS;
    private static final List<String> NULL_LITERALS;
    
    static
    {
        TRUE_LITERALS = new ArrayList<String>(20);
        TRUE_LITERALS.add("yes");
        TRUE_LITERALS.add("true");
        TRUE_LITERALS.add("y");
        TRUE_LITERALS.add("t");
        TRUE_LITERALS.add("1");
        TRUE_LITERALS.add("on");
        TRUE_LITERALS.add("Enabled");
        
        NULL_LITERALS = new ArrayList<String>(20);
        NULL_LITERALS.add("<unknown>");
        NULL_LITERALS.add("unknown");
        NULL_LITERALS.add("<none>");
        NULL_LITERALS.add("(None)");
        NULL_LITERALS.add("<Not set>");
        NULL_LITERALS.add("(not set)");
        NULL_LITERALS.add("<no name>");
        NULL_LITERALS.add("n/a"); // channel in AgentsEvent
        NULL_LITERALS.add("<null>");
    }
    
    private AstUtil()
    {
        //hide constructor
    }

    /**
     * Checks if a String represents <code>true</code> or <code>false</code>
     * according to Asterisk's logic.
     * <p>
     * The original implementation is <code>util.c</code> is as follows:
     * 
     * <pre>
     *     int ast_true(const char *s)
     *     {
     *         if (!s || ast_strlen_zero(s))
     *             return 0;
     *      
     *         if (!strcasecmp(s, &quot;yes&quot;) ||
     *             !strcasecmp(s, &quot;true&quot;) ||
     *             !strcasecmp(s, &quot;y&quot;) ||
     *             !strcasecmp(s, &quot;t&quot;) ||
     *             !strcasecmp(s, &quot;1&quot;) ||
     *             !strcasecmp(s, &quot;on&quot;))
     *             return -1;
     *     
     *         return 0;
     *     }
     * </pre>
     * 
     * To support the dnd property of
     * {@link org.asteriskjava.manager.event.ZapShowChannelsEvent} this method
     * also consideres the string "Enabled" as true.
     * 
     * @param s the String to check for <code>true</code>.
     * @return <code>true</code> if s represents <code>true</code>,
     *         <code>false</code> otherwise.
     */
    public static boolean isTrue(String s)
    {
        if (s == null || s.length() == 0)
        {
            return false;
        }

        for (String literal : TRUE_LITERALS)
        {
            if (literal.equalsIgnoreCase(s))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Parses a string for caller id information.
     * <p>
     * The caller id string should be in the form <code>"Some Name" &lt;1234&gt;</code>.
     * <p>
     * This resembles <code>ast_callerid_parse</code> in
     * <code>callerid.c</code> but strips any whitespace.
     * 
     * @param s the string to parse
     * @return a String[] with name (index 0) and number (index 1)
     */
    public static String[] parseCallerId(String s)
    {
        final String[] result = new String[2];
        final int lbPosition;
        final int rbPosition;
        String name;
        String number;

        if (s == null)
        {
            return result;
        }

        lbPosition = s.lastIndexOf('<');
        rbPosition = s.lastIndexOf('>');

        // no opening and closing brace? use value as CallerId name
        if (lbPosition < 0 || rbPosition < 0)
        {
            name = s.trim();
            if (name.length() == 0)
            {
                name = null;
            }
            result[0] = name;
            return result;
        }
        else
        {
            number = s.substring(lbPosition + 1, rbPosition).trim();
            if (number.length() == 0)
            {
                number = null;
            }
        }

        name = s.substring(0, lbPosition).trim();
        if (name.startsWith("\"") && name.endsWith("\"") && name.length() > 1)
        {
            name = name.substring(1, name.length() - 1).trim();
        }
        if (name.length() == 0)
        {
            name = null;
        }

        result[0] = name;
        result[1] = number;
        return result;
    }

    /**
     * Checks if the value of s was <code>null</code> in Asterisk.
     * <p>
     * This method is useful as Asterisk likes to replace <code>null</code>
     * values with different string values like "unknown", "&lt;unknown&gt;"
     * or "&lt;null&gt;".
     * <p>
     * To find such replacements search for <code>S_OR</code> in Asterisk's
     * source code. You will find things like
     * <pre>
     * S_OR(chan-&gt;cid.cid_num, "&lt;unknown&gt;")
     * fdprintf(fd, "agi_callerid: %s\n", S_OR(chan-&gt;cid.cid_num, "unknown"));
     * </pre>
     * and more...
     * 
     * @param s the string to test, may be <code>null</code>.
     * @return <code>true</code> if the s was <code>null</code> in Asterisk;
     *         <code>false</code> otherwise. 
     */
    public static boolean isNull(String s)
    {
        if (s == null)
        {
            return true;
        }

        for (String literal : NULL_LITERALS)
        {
            if (literal.equalsIgnoreCase(s))
            {
                return true;
            }
        }

        return false;
    }
}
