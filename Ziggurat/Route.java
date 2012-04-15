package Ziggurat;

import java.util.*;

/**
 * Route
 * 
 * This class defines a Route. A Route is primarily comprised of
 * a vector of Sequences that the agents expects/intends will take it from its
 * current state to some goal/reward state.
 * 
 * NOTES:
 * When is applyReplacement() called? Must be at minimum .between. sequences 
 * 	else nextAction() is incorrect
 *
 * @author Zachary Paul Faltersack
 * 
 */
public class Route
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** this value is used to indicate that there is no current sequence or action */
    public static final int NONE = -1;
    
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    
    /** the route consists of these sequences in order.  All sequences must be
        at the same level. */
	private Vector<Sequence> sequences;

    /** if a @link{Replacement} has been applied to the sequence the agent is
        currently executing, this the modified version is stored here. */
	private Sequence replSeq;

    /** if the agent is currently executing a sequence in this route, then this variable
        contains the index of that sequence in @link{Route#sequences} */
	private int currSeqIndex;

    /** this is the index of the next action in the current sequence that is to
	be executed */
	private int currActIndex;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */

    /** creates an empty route by default */
	public Route() 
    {
        sequences = new Vector<Sequence>();
        replSeq = null;
        currSeqIndex = Route.NONE;
        currActIndex = Route.NONE;
	}//ctor

    /** creates a route from a given sequence */
	public Route(Vector<Sequence> initSeq)
    {
        if ((initSeq == null) || initSeq.size() == 0)
        {
            this();
        }
        else
        {
            this.sequences = initSeq;
            currSeqIndex = 0;
            currActIndex = 0;
        }
	}//ctor

    /*======================================================================
     * Public Methods
     *----------------------------------------------------------------------
     */

    /** %%%TBD */
	public String toString() 
    {
		return "";
	}

	/** auto increments the currAction/currSequence pointers	 */
	public Action nextAction() 
    {
		currActIndex++;
		if(replSeq != null)
		{
			if(currActIndex >= replSeq.length())
			{
				replSeq = null;
				currActIndex = 0;
				currSeqIndex++;
			}
			else
			{
				return replSeq.getActionAtIndex(currActIndex);
			}
		}
		else if(currActIndex >= sequences.elementAt(currSeqIndex).length())
        {
            currActIndex = 0;
            currSeqIndex++;
        }
		
        if(currSeqIndex >= sequences.size())
        {
            return null;
        }
        else
        {
        	return sequences.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
        }
	}//nextAction

    /**
     * getCurrAction
     *
     * retrieves the current action in the current sequence
     *
     * @return null if there is no current action
     */
	public Action getCurrAction() 
    {
		if(replSeq != null) return replSeq.getActionAtIndex(currActIndex);
        else                return sequences.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
	}//getCurrAction

    /**
     * getCurrSequence
     *
     * retrieves a reference to the current sequence
     *
     * @return null if there is no current sequence
     */
	public Sequence getCurrSequence() 
    {
		if(replSeq != null) return replSeq;
		else 				return sequences.elementAt(currSeqIndex);
	}

    /**
     * applyReplacement
     *
     * applies a given replacement to a route
     */
	public void applyReplacement(Replacement repl) 
    {
		replSeq = repl.apply(this.getCurrentSequence());
	}

    /**
     * numElementalEpisodes
     *
     * counts the total number of elemental episodes in this sequence.  Note:
     * This method is somewhat expensive.
     */
	public int numElementalEpisodes() 
    {
        int count = 0;
        for(Sequence s : sequences)
        {
            count += s.numElementalEpisodes();
        }
		return count;
	}

    /**
     * counts the number of elemental episodes remaining in the sequence
     * assuming that the agent begins executing from the current action of the
     * current sequence
     */
	public int remainingElementalEpisodes() 
    {
        int count = 0;
        for(int i = currSeqIndex; i < sequences.size(); ++i)
        {
            count += sequences.elementAt(i).numElementalEpisodes();
        }
		return count - currActIndex;
	}
}//class Route
