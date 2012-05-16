package Ziggurat;

import java.util.Hashtable;
import java.util.Vector;


/**
 * <!-- class FlipSystemEnvironment -->
 *
 * This a very simple environment that is drawn from:<br>
 *
 * Michael Holmes and Charles L. Isbell. Schema Learning: Experience-based
 * Construction of Predictive Action Models. In Advances in Neural Information
 * Processing Systems (NIPS) 17, pages 585-562, 2005.
 * 
 * <p>There are two states and three commands ( left, right and up ).  Moving
 * right from state 1 puts you in state 2.  Moving left from state 2 puts you in
 * state 1.  All other commands return you to the same state.  Whenever you
 * change states you get a reward.
 *
 */
public class FlipSystemEnvironment extends Environment {

	// Define possible states
	private enum State { STATE_1, STATE_2 };
	private State currentState;
	
	// Define command related variables
	private final int CMD_LEFT 		= 0;
	private final int CMD_RIGHT 	= 1;
	private final int CMD_UP 		= 2;
	private final int NUM_COMMANDS 	= 3;
	
	// Define sense related variables
	private WME WME_reward;
	
	public FlipSystemEnvironment()
    {
		this.currentState = State.STATE_1;
		this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
	}// *ctor
	
    /**
     * a handy debugging method for printing the current state and command
     * in human readable format.
     */
    private void printState(int cmd)
    {
        switch(this.currentState)
        {
            case STATE_1:  
                System.out.print("In State ONE ");
                break;
            case STATE_2:
                System.out.print("In State TWO ");
                break;
            default:
                System.out.print("In State UNKNOWN!! ");
                break;
        }//switch                    
            
        switch(cmd)
        {
            case CMD_LEFT:
                System.out.println(" moving LEFT");
                break;
            case CMD_RIGHT:
                System.out.println(" moving RIGHT");
                break;
            case CMD_UP:
                System.out.println(" moving UP");
                break;
            default:
                System.out.println(" moving ????");
                break;
        }//switch

    }//printState

	/**
	 * The agent will always end in State_1
	 */
	private void executeCMD_LEFT()
    {
		switch(this.currentState)
        {
            case STATE_1:
                this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
                break;
            case STATE_2:
                this.WME_reward = new WME("reward", "1.0", WME.Type.DOUBLE);
                break;
		}
		this.currentState = State.STATE_1;
	}// executeCMD_LEFT
	
	/**
	 * The agent will always end in State_2
	 */
	private void executeCMD_RIGHT()
    {
		switch(this.currentState)
        {
            case STATE_1:
                this.WME_reward = new WME("reward", "1.0", WME.Type.DOUBLE);
                break;
            case STATE_2:
                this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
                break;
		}
		this.currentState = State.STATE_2;
	}// executeCMD_RIGHT
	
	/**
	 * At no point will "up" produce a reward.
	 * The agent will remain in the current state.
	 */
	private void executeCMD_UP()
    {
        this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
	}// executeCMD_UP
	
	// Completed abstract Environment methods ************************
	
	/**
	 * take a command and apply it to the environment.
	 * return the resulting WMESet
	 */
	public WMESet takeStep(int commandIndex)
    {
        printState(commandIndex);
        
		// Execute appropriate command
		switch(commandIndex)
        {
            case CMD_LEFT: 	executeCMD_LEFT(); 		break;
            case CMD_RIGHT: executeCMD_RIGHT(); 	break;
            case CMD_UP: 	executeCMD_UP(); 		break;
            default: 		return null;	// Invalid command...
		}
		
		// Generate return data
		return generateCurrentWMESet();
	}// takeStep
	
	/**
	 * get an initial WMESet config indicating we're beginning
	 * in a brand new environment.
	 * If this is the first time then takeStep has never been called, 
	 * so our WMEs are the current initialized values from *ctor.
	 * Else we have update values to return.
	 */
	public WMESet generateCurrentWMESet()
    {
		// Generate and fill the hashtable for our return value
		Hashtable<String, WME> rtnVals = new Hashtable<String, WME>();
		rtnVals.put(this.WME_reward.attr, this.WME_reward);
		
		return new WMESet(rtnVals);
	}// generateCurrentWMESet
	
	/** return the number of available commands in this environment */
    public int getNumCommands() { return NUM_COMMANDS; }

    public String stringify(int cmd)
    {
        if (cmd == CMD_LEFT) return "L";
        if (cmd == CMD_RIGHT) return "R";
        if (cmd == CMD_UP) return "U";
        return "?";
    }
                

    public String stringify(Episode ep)
    {
        if (ep instanceof SequenceEpisode)
        {
            SequenceEpisode seqEp = (SequenceEpisode)ep;
            return "[" + stringify(seqEp.getSequence()) + "]";
        }

        ElementalEpisode elEp = (ElementalEpisode)ep;
        String sensorString = "0";
        if (elEp.getSensors().getAttr(WME.REWARD_STRING).getDouble() > 0.0)
        {
            sensorString = "1";
        }

        return sensorString + stringify(elEp.getCommand());
    }//stringify episode

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
        
    /** convert a given replacement to a string */
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

    
}// [class] FlipSystemEnvironment
