package org.asteriskjava.config.dialplan;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.asteriskjava.config.Category;
import org.asteriskjava.config.ConfigElement;
import org.asteriskjava.config.ConfigFile;
import org.asteriskjava.config.ConfigFileImpl;
import org.asteriskjava.config.ConfigFileReader;
import org.asteriskjava.config.ConfigParseException;
import org.asteriskjava.config.ConfigVariable;

/*
 *   Interprets extensions.conf as a special kind of config file, the dialplan.
 *   - Line numbers correspond with pbx_config.c, tags/1.4.19 revision 96024
 */
public class ExtensionsConfigFileReader extends ConfigFileReader
{
    /*
     * This method corresponds to an iteration of the loop at line 2212 Notes:
     *      1. [general] and [globals] are allowed to be a context here if they contain only configvariables
     *      2. switch and ignorepat are treated like regular ConfigVariable.
     */
    protected ConfigElement processTextLine(String configfile, int lineno, String line) throws ConfigParseException
    {
        ConfigElement configElement;
        
        if(
                (line.trim().startsWith("exten") || line.trim().startsWith("include")) &&
                currentCategory != null && 
                (currentCategory.getName().equals("general") || currentCategory.getName().equals("globals"))
        )
            throw new ConfigParseException(configfile, lineno, "cannot have 'exten' or 'include' in global or general sections");

        
        /*
         * Goal here is to break out anything unique that we might want to
         * look at and parse separately. For now, only exten and include fit
         * that criteria. Eventually, I could see parsing sections for things
         * from macros, contexts to differentiate them from categories, switch
         * for realtime, and more. 
         */
        if (line.trim().startsWith("exten"))
        {
            configElement = parseExtension(configfile, lineno, line);
            currentCategory.addElement(configElement);
            return configElement;
        }
        else if(line.trim().startsWith("include"))
        {
            // use parseVariable since we have access to it
            ConfigVariable configvar = parseVariable(configfile, lineno, line);
            configElement = new ConfigInclude(configfile, lineno, configvar.getValue());
            currentCategory.addElement(configElement);
            return configElement;
        }
        
        // leave everything else the same
        configElement = super.processTextLine(configfile, lineno, line);
        
        return configElement;
    }
    
    /* Roughly corresponds to pbx_config.c:2222 */
    protected ConfigExtension parseExtension(String configfile, int lineno, String line) throws ConfigParseException
    {
        String dupline = new String(line);
        
        ConfigVariable initialVariable = parseVariable(configfile, lineno, dupline);
        
        
        if(!initialVariable.getName().equals("exten"))
            throw new ConfigParseException(configfile, lineno, "missing 'exten' near " + dupline);
        dupline = initialVariable.getValue().trim();

        int nameIndex = dupline.indexOf(",", 0);
        if(nameIndex == -1)
            throw new ConfigParseException(configfile, lineno, "missing extension name near " + dupline);
        String name = dupline.substring(0, nameIndex);
        dupline = dupline.substring(name.length()+1, dupline.length()).trim();
        
        int priorityDelimiter = dupline.indexOf(",", 0);
        if(priorityDelimiter == -1)
            throw new ConfigParseException(configfile, lineno, "missing extension priority near " + dupline);
        String priority = dupline.substring(0, priorityDelimiter);
        dupline = dupline.substring(priority.length()+1, dupline.length()).trim();

        String [] application = harvestApplicationWithArguments(dupline);
        
        return new ConfigExtension(configfile,lineno,name,priority,application);
    }
    
    /* Roughly corresponds to pbx_config.c:2276 */
    private static String [] harvestApplicationWithArguments(String arg)
    {
        List<String> args = new ArrayList<String>();
        
        if(args != null && arg.trim().length() >= 0)
        {
            String appl = "", data = "";
            
            /* Find the first occurrence of either '(' or ',' */
            int firstc = arg.indexOf(',');
            int firstp = arg.indexOf('(');
            
            if (firstc != -1 && (firstp == -1 || firstc < firstp)) {
                /* comma found, no parenthesis */
                /* or both found, but comma found first */
                String [] split = arg.split(",");
                appl = split[0];
                for(int i = 1; i < split.length; i++)
                    data += split[i] + (i+1<split.length?",":"");
            } else if (firstc == -1 && firstp == -1) {
                /* Neither found */
                data = "";
            } else {
                /* Final remaining case is parenthesis found first */
                String [] split = arg.split("\\(");
                appl = split[0];
                for(int i = 1; i < split.length; i++)
                    data += split[i] + (i+1<split.length?"(":"");
                int end = data.lastIndexOf(')');
                if (end == -1) {
                    //ast_log(LOG_WARNING, "No closing parenthesis found? '%s(%s'\n", appl, data);
                } else if(end == data.length()-1) {
                    data = data.substring(0, end);
                }
                data = processQuotesAndSlashes(data, ',', '|');
            }
            
            if(!appl.trim().equals(""))
            {
                args.add(appl.trim());
                if(!data.trim().equals(""))
                {
                    String [] dataSplit = data.split("\\|");
                    for(int i = 0; i < dataSplit.length; i++)
                        args.add(dataSplit[i].trim());
                }
            }
        }
        
        return args.toArray(new String[0]);
    }

    public ExtensionsConfigFile readExtensionsFile(String configfile) throws IOException, ConfigParseException
    {
        super.readFile(configfile);
        /* at some point, we may want to resolve back references */
        /* that include or goto from one context to another */
        return new ExtensionsConfigFile(configfile, categories);
    }
    
    /* ast_process_quotes_and_slashes rewritten to be java friendly */
    private static String processQuotesAndSlashes(String start, char find, char replace_with)
    {
        String dataPut = "";
        int inEscape = 0;
        int inQuotes = 0;
        
        char [] startChars = start.toCharArray();
        for (int i = 0; i < startChars.length; i++) {
            if (inEscape != 0) {
                dataPut += startChars[i];       /* Always goes verbatim */
                inEscape = 0;
            } else {
                if (startChars[i] == '\\') {
                    inEscape = 1;      /* Do not copy \ into the data */
                } else if (startChars[i] == '\'') {
                    inQuotes = 1 - inQuotes;   /* Do not copy ' into the data */
                } else {
                    /* Replace , with |, unless in quotes */
                    dataPut += inQuotes != 0 ? startChars[i] : ((startChars[i] == find) ? replace_with : startChars[i]);
                }
            }
        }
        return dataPut;
    }
}
