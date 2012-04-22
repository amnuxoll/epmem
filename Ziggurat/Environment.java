package Ziggurat;
 
import java.util.*;

/**
 * class Environment
 *
 * Any agent environment must subclass this class so that Zigg can interact with
 * that environment
 *
 */
public abstract class Environment
{
    /** define the number of available actions in this environment.  For
     * example, in Paper Scissors Rock there are three possible actions at each
     * time step.
     */
    public abstract int getNumCommands();

    /** the current agent reward can be retrieved via the environment.
     * Currently only positive rewards are supported.
     */
    public abstract double currReward();

    /**
     * convert a given episode to a string
     */
    public abstract String stringify(Episode ep);
        
    /**
     * convert a given action to a string
     */
    public abstract String stringify(Action act);
    
    /**
     * convert a given sequence to a string
     */
    public abstract String stringify(Sequence seq);
    
    /**
     * convert a given replacement to a string
     */
    public abstract String stringify(Replacement repl);
    
    /**
     * convert a given plan to a string
     */
    public abstract String stringify(Plan plan);
    
}//class Environment
