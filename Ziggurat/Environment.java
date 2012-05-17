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
    public String stringify(int cmd)
    {
        return (cmd < 0) ?  "?" :  "" + cmd;
    }
    
    /** convert a given Episode to a string */
    public String stringify(Episode ep)
    {
        if (ep instanceof SequenceEpisode)
        {
            return stringify((SequenceEpisode)ep);
        }
        return stringify((ElementalEpisode)ep);
    }//stringify episode

    /** convert a given ElementalEpisode to a string */
    public String stringify(ElementalEpisode elEp)
    {
        return elEp.getSensors().toString() + stringify(elEp.getCommand());
    }//stringify episode

    /** convert a given SequenceEpisode to a string */
    public String stringify(SequenceEpisode seqEp)
    {
        return "[" + stringify(seqEp.getSequence()) + "]";
    }//stringify episode

    /** convert a given Action to a string */
    public String stringify(Action act)
    {
        String result = stringify(act.getLHS()) + "-";
        //if the action is indeterminate, the connecting arrow contains
        //an indication of the percent
        if (act.isIndeterminate())
        {
            int totalFreq = 0;
            for (Action cousin : act.getCousins())
            {
                totalFreq += cousin.getFreq();
            }
                
            int pct = act.getFreq() * 100 / totalFreq;
            pct = Math.min(99, pct);
            pct = Math.max(00, pct);
            result += pct;
        }
        else
        {
            result += "--";
        }
        result += "->";

        //Add the RHS to the result string
        Episode ep = act.getRHS();
        result += stringify(ep);
        if (ep instanceof ElementalEpisode)
        {
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }//stringify Action

    /** convert a given Sequence to a string */
    public String stringify(Sequence seq)
    {
        String result = "";
        boolean first = true;
        Vector<Action> actions = seq.getActions();
        for(Action a : actions)
        {
            if (first)
            {
                first = false;
            }
            else
            {
                result += ", ";
                //Add additional spaces depending upon level so that the line is
                //more readable
                for(int i = 0; i < seq.getLevel(); i++)
                {
                    result += " ";
                }
            }//else
                
            result += stringify(a);
        }
            
        return result;
    }//stringify sequence
    
    /** convert a given Replacement to a string */
    public String stringify(Replacement repl)
    {
        //This is the LHS of the replacement rule
        String result = "{ ";
        Vector<Action> lhsVec = repl.getLHS();
        for(int i = 0; i < lhsVec.size(); i++)
        {
            Action act = lhsVec.elementAt(i);
                
            //precede all but first sequence with a comma separator
            if (i > 0) result += ", ";  
                
            result += stringify(act);
        }
        result += " }";
            
        //Arrow
        result += "==>";
            
        //RHS of the replacement rule
        result += stringify(repl.getRHS());
            
        return result;
    }
        
    /** convert a given Route to a string */
    public String stringify(Route route)
    {
        String result = "{";
        boolean first = true;
        int count = 0;
        for(Action a : route.getActions())
        {
            //Precede all but the first action with a comma separator
            if (count > 0)
            {
                result += ", ";
                //Add additional spaces depending upon level so that the line is
                //more readable
                for(int i = 0; i < route.getLevel(); i++)
                {
                    result += " ";
                }
            }//else
                    
            result += stringify(a);

            //If this is the current action, put an asterisk behind it
            if (count == route.getCurrActIndex()) result += "*";

            count++;
        }//for
        result += "}";

        return result;
    }//stringify route


    /** convert a given Plan to a string */
    public String stringify(Plan plan)
    {
        String result = "{\n";
        int len = plan.getNumLevels();
        for(int i = 0; i < len; i ++)
        {
            Route r = plan.getRoute(i);
            result += "  Level " + i + ": ";
            result += stringify(r);
            result += "\n";
        }//for
        result += "}";

        return result;
    }

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
