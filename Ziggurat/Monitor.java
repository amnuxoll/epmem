package Ziggurat;
 
import java.util.*;
import Ziggurat.Episode;

/**
 * <!-- class Monitor -->
 *
 * A class that wishes to monitor events in Ziggurat and the environment must
 * inherit from this class and implement the print() and println() methods that
 * record given strings to a log.  The constructors must also be modified to
 * call the corresponding super().
 *
 * @see MonitorStdOut
 * @see MonitorNull
 *
 */
public abstract class Monitor
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
    private int indent = 0;

    /** keeps track of the current stack of method calls as best as it is able */
    private Vector<String> stack = new Vector<String>();

    /** keeps count of how many rewards have been received so far */
    private int rewardCount = 0;

    /** monitor can use an environment to create environment-specific,
     * pretty-printed versions of key objects.
     */
    private Environment env = null;

    /**
     * a user can temporarily increase the indent level.  This variable tracks
     * that temporary indent amount.
     */
    private int tempIndent = 0;

    /**
     * a user can log a line in parts using {@link #logPart}.  This boolean
     * tracks whether a partial log has been added yet.
     */
    private boolean inPart = false;

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
     * Abstract Methods
     *----------------------------------------------------------------------
     */
    /**
     * print
     *
     * prints a partial line to the log
     *
     * @param s the message to log
     */
    public abstract void print(String s);

    /**
     * println
     *
     * prints a single line to the log
     *
     * @param s the message to log
     */
    public abstract void println(String s);

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /**
     * logPart
     *
     * prints a partial log entry.  NOTE:  There is a lot of copy/paste here
     * from log().  I don't know how to avoid that
     */
    public void logPart(String s)
    {
        if (!inPart)
        {
            s = padLeft(s, this.indent + this.tempIndent);
            inPart = true;
        }
        this.print(s);
        
        //if there is a temporary indent in place, remove it now
        if (this.tempIndent != 0)
        {
            this.tempIndent = Math.max(0, tempIndent - INDENT_SIZE);
        }
    }

    /**
     * logs a single generic event
     *
     * @param s the message to log
     */
   public void log(String s)
    {
        if (!inPart)
        {
            s = padLeft(s, this.indent + this.tempIndent);
        }
        this.println(s);

        //if there is a temporary indent in place, remove it now
        if (this.tempIndent != 0)
        {
            this.tempIndent = Math.max(0, tempIndent - INDENT_SIZE);
        }

        //end any partial entry
        inPart = false;
    }//log

    /**
     * indent a given string with spaces
     *
     * @param s  the string to pad
     * @param n  the numbe of spaces to prepend
     */
    public static String padLeft(String s, int n)
    {
        String pad = String.format("%1$-#" + (n+1) + "s", "\n");
        s = s.replace("\n", pad);
        n = n + s.length();
        return String.format("%1$#" + n + "s", s);
    }

    /**
     * tab
     *
     * allows the user to increase the indent level for the next item that's
     * printed to the log
     */
    public void tab()
    {
        this.tempIndent += INDENT_SIZE;
    }//tab

    /**
     * prints a single dot ('.') to the screen.  Multiple calls to this method
     * can be used to give a qualitative measure of how much the agent is
     * "thinking" when it executes an expensive algorithm.  For example: {@link
     * Ziggurat.Ziggurat#findRoute}
     *
     * 
     */
    public void think() { logPart("."); }
    
    /**
     * logs a single generic event but also supports printf-style string formatting
     *
     * @param s     the format of the message to log
     * @param args  the arguments for printf
     */
   public void log(String s, Object... args)
    {
        //Construct the output message
        Formatter f = new Formatter();
        f.format(s, args);
        s = f.toString();

        //Send it to the log
        log(s);
    }//log

    /**
     * logs a string with a single int argument that requires formatting
     *
     * @param s     the format of the message to log
     * @param arg1  the int argument for printf
     */
   public void log(String s, int arg1)
    {
        this.log(s, (Object)(new Integer(arg1)));
    }//log

    /**
     * logs a string with two int arguments that require formatting
     *
     * @param s     the format of the message to log
     * @param arg1  the first int argument for printf
     * @param arg2  the second int argument for printf
     */
   public void log(String s, int arg1, int arg2)
    {
        this.log(s, (Object)(new Integer(arg1)), (Object)(new Integer(arg2)));
    }//log


    /**
     * logs a string with a single double argument that requires formatting
     *
     * @param s     the format of the message to log
     * @param arg1  the double argument for printf
     */
   public void log(String s, double arg1)
    {
        this.log(s, (Object)(new Double(arg1)));
    }//log

    /**
     * enter
     * 
     * logs entering a method.  Only call this when entering a major function as
     * it increases the indent level of the log.
     *
     * @param name the name of the method 
     */
    public void enter(String name)
    {
        log("Enter: " + name);
        this.indent += INDENT_SIZE;
        this.stack.add(name);
    }//enter

    /**
     * exit
     * 
     * logs exiting a method.  Only call this when exiting a major method.
     * 
     * @param name the name of the method 
     */
    public void exit(String name)
    {
        //Find this method in the stack
        int pos = -1;
        for(int i = this.stack.size() - 1; i >= 0; i--)
        {
            String next = this.stack.elementAt(i);
            if (next.equalsIgnoreCase(name))
            {
                pos = i;
                break;
            }
        }

        //if found, adjust indent and pop it off the stack
        if (pos >= 0)
        {
            this.indent = Math.max(0, this.indent - (this.stack.size() - pos) * INDENT_SIZE);

            while(pos < this.stack.size())
            {
                this.stack.remove(pos);
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
        this.rewardCount++;
        log("REWARD #" + this.rewardCount);
    }//reward

    /**
     * log
     *
     * prints an episode to the log
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
    
    /**
     * log
     *
     * prints an action to the log
     *
     * @param act   the action to print
     */
    public void log(Action act)
    {
        String output = "";
        
        if (env != null)
        {
            output = env.stringify(act);
        }
        else
        {
            output = act.toString();
        }

        log(output);
    }//log
    
    /**
     * log
     *
     * prints a Sequence to the log
     *
     * @param seq   the sequence to print
     */
    public void log(Sequence seq)
    {
        String output = "";
        
        if (env != null)
        {
            output = env.stringify(seq);
        }
        else
        {
            output = seq.toString();
        }

        log(output);
    }//log
    
    /**
     * log
     *
     * prints a Replacement to the log
     *
     * @param repl  the Replacement to print
     */
    public void log(Replacement repl)
    {
        String output = "";
        
        if (env != null)
        {
            output = env.stringify(repl);
        }
        else
        {
            output = repl.toString();
        }

        log(output);
    }//log
    
    /**
     * log
     *
     * prints a Plan to the log
     *
     * @param plan  the Plan to print
     */
    public void log(Plan plan)
    {
        String output = "";
        
        if (env != null)
        {
            output = env.stringify(plan);
        }
        else
        {
            output = plan.toString();
        }

        log(output);
    }//log
    
    /**
     * log
     *
     * prints a Vector of objects to the log.
     *
     * Presumably the objects in this vector are of a type that can be handled
     * by one of the other log methods.  If it isn't, then the default
     * toString() will kick in.  Java won't let you create overloaded methods
     * that handle generics so it has to be done like this.
     *
     * @param vec   the vector to print
     */
    public void log(Vector vec) 
    {
        log("{");
        int count = 0;
        for(Object obj : vec)
        {
            //Label each entry with its index
            logPart("  " + count + ": ");
            count++;

            //Convert the entry to a string
            if (this.env == null)
            {
                log(obj.toString());
            }
            else if (obj instanceof Episode)
            {
                log(this.env.stringify((Episode)obj));
            }
            else if (obj instanceof Action)
            {
                log(this.env.stringify((Action)obj));
            }
            else if (obj instanceof Sequence)
            {
                log(this.env.stringify((Sequence)obj));
            }
            else
            {
                log(obj.toString());
            }
        }//for
        log("}");

    }//log
    
}//class Monitor
