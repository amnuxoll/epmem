package Ziggurat;
 
import java.util.*;
import Ziggurat.Episode;

/**
 * <!-- class MonitorStdOut -->
 *
 * This Monitor is a console logger.  
 *
 */
public class MonitorStdOut extends Monitor
{
    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /** default ctor does nothing */
    public MonitorStdOut() { super(); }

    /** a ctor can be consructed with an environment */
    public MonitorStdOut(Environment env)
    {
        super(env);
    }
    
    
    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /**
     * print
     *
     * prints a partial line to the console
     *
     * @param s the message to log
     */
    public void print(String s)
    {
        System.out.print(s);
    }

    /**
     * println
     *
     * prints a single line to the console
     *
     * @param s the message to log
     */
    public void println(String s)
    {
        System.out.println(s);
    }


    
}//class MonitorStdOut
