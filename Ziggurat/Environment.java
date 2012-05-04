package Ziggurat;
 
import java.util.*;

/**
 * <!-- class Environment -->
 *
 * Any agent environment must subclass this class so that Zigg can interact with
 * that environment
 *
 */
public abstract class Environment
{
	/**
	 * take a command and apply it to the environment.
	 * return the resulting WMESet
	 */
	public abstract WMESet takeStep(int commandIndex);
	
	/**
	 * get an initial WMESet config indicating we're beginning
	 * in a brand new environment.
	 */
	public abstract WMESet generateCurrentWMESet();
	
    /** define the number of available actions in this environment.  For
     * example, in Paper Scissors Rock there are three possible actions at each
     * time step.
     */
    public abstract int getNumCommands();

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
