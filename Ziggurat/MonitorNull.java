package Ziggurat;
 
import java.util.*;
import Ziggurat.Episode;

/**
 * class MonitorNull
 *
 * This monitor does not actually log anything.
 *
 */
public class MonitorNull extends Monitor
{
    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /** default ctor does nothing */
    public MonitorNull() { super(); }

    /** a ctor can be consructed with an environment */
    public MonitorNull(Environment env)
    {
        super(env);
    }
    
    /*======================================================================
     * Methods are all stubbed out...
     *----------------------------------------------------------------------
     */
    public void print(String s) {}
    public void println(String s) {}
    public void logPart(String s) {}
    public void log(String s) {}
    public void tab() {}
    public void think() {}
    public void log(String s, Object... args) {}
    public void log(String s, int arg1) {}
    public void log(String s, int arg1, int arg2) {}
    public void log(String s, double arg1) {}
    public void enter(String name) {}
    public void exit(String name) {}
    public void reward(double amt) {}
    public void log(Episode ep) {}
    public void log(Action act) {}
    public void log(Sequence seq) {}
    public void log(Replacement repl) {}
    public void log(Plan plan) {}
    public void log(Vector vec) {}
    
}//class MonitorNull
