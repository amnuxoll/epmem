package Ziggurat;
 
import java.util.*;

/**
 * class Monitor
 *
 * An instance of this class is used to monitor events in Ziggurat and the
 * environment.
 *
 * Currently this is just a console logger.  In the long term, Monitor may
 * become an abstract class with different subclasses that behave in different
 * ways.
 *
 */
public class Monitor
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** specifies indent amount */
    public static int INDENT_SIZE = 4;
    
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** this specifies how far to indent messages. */
    private int m_indent = 0;

    /** keeps track of the current stack of method calls as best as it is able */
    private Vector<String> m_stack = new Vector<String>();

    /** keeps count of how many rewards have been received so far */
    private int m_rewardCount = 0;

    /** monitor can use an environment to create environment-specific,
     * pretty-printed versions of key objects.
     */
    private Environment env = null;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /** default ctor does nothing */
    public Monitor() { }

    /** a ctor can be consructed with an environment */
    public Monitor(Environment env)
    {
        this.env = env;
    }
    
    
    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /**
     * indent a given string with spaces
     *
     * @param s  the string to pad
     * @param n  the numbe of spaces to prepend
     */
    public static String padLeft(String s, int n)
    {
        return String.format("%1$#" + n + "s", s);
    }
        
    
    /**
     * logs a single generic event
     *
     * @param s the message to log
     */
   public void log(String s)
    {
        s = padLeft(s, m_indent);
        System.out.println(s);
    }//log

    /**
     * logs entering a method
     *
     * @param name the name of the method 
     */
    public void enter(String name)
    {
        log("Enter: " + name);
        m_indent += INDENT_SIZE;
        m_stack.add(name);
    }//enter

    /** 
     * logs exiting a method
     * 
     * @param name the name of the method 
     */
    public void exit(String name)
    {
        //Find this method in the stack
        int pos = -1;
        for(int i = m_stack.size() - 1; i >= 0; i--)
        {
            String next = m_stack.elementAt(i);
            if (next.equalsIgnoreCase(name))
            {
                pos = i;
                break;
            }
        }

        //if found, adjust indent and pop it off the stack
        if (pos >= 0)
        {
            m_indent = Math.max(0, m_indent - (m_stack.size() - pos) * INDENT_SIZE);

            while(pos < m_stack.size())
            {
                m_stack.remove(pos);
            }
        }//if
        
        //Print to log
        log("Exit: " + name);

    }//exit
    
    /** 
     * logs receiving a reward
     *
     * @param amt  amount of reward (currently unused)
     */
    public void reward(double amt)
    {
        m_rewardCount++;
        log("REWARD #" + m_rewardCount);
    }//reward

    /**
     * log
     *
     * this version prints an episode to the log
     *
     * @param ep   the episode to print
     */
    public void log(Episode ep)
    {
        String output = "";
        
        if (env != null)
        {
            output = env.stringify(ep);
        }
        else
        {
            output = ep.toString();
        }

        log(output);
    }//log
    

}//class Monitor
