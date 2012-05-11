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
     *
     * @return the resulting WMESet
	 */
	public abstract WMESet takeStep(int commandIndex);
	
	/**
	 * get an initial WMESet config indicating we're beginning in a brand new
	 * environment.
	 */
	public abstract WMESet generateCurrentWMESet();
	
    /** define the number of available actions in this environment.  For
     * example, in Paper Scissors Rock there are three possible actions at each
     * time step.
     */
    public abstract int getNumCommands();

    /*
     * Default implementations of these stringify methods are provided.
     * However, all but a trivial environment should override them.
     */
    /** convert a given command to a string */
    public String stringify(int cmd) { return "" + cmd; }
    
    /** convert a given episode to a string */
    public String stringify(Episode ep) {return ep.toString(); }
        
    /** convert a given action to a string */
    public String stringify(Action act) { return act.toString(); }
    
    /** convert a given sequence to a string */
    public String stringify(Sequence seq) { return seq.toString(); }
    
    /** convert a given replacement to a string */
    public String stringify(Replacement repl) { return repl.toString(); }
    
    /** convert a given replacement to a string */
    public String stringify(Route route) { return route.toString(); }
    
    /** convert a given plan to a string */
    public String stringify(Plan plan) { return plan.toString(); }
    
    /** convert a given decision element to a string */
    public String stringify(DecisionElement de)
    {
        if (de instanceof Episode)
        {
            return stringify((Episode)de);
        }
        else if (de instanceof Action)
        {
            return stringify((Action)de);
        }
        else if (de instanceof Sequence)
        {
            return stringify((Sequence)de);
        }
        else if (de instanceof Replacement)
        {
            return stringify((Replacement)de);
        }
        else
        {
            return de.toString();
        }
    }//stringify DecisionElement
    
}//class Environment
