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
public class Route extends Vector<Sequence>
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
    
    /** if a @link{Replacement} has been applied to the sequence the agent is
        currently executing, this the modified version is stored here. */
	protected Sequence replSeq;

    /** if the agent is currently executing a sequence in this route, then this variable
        contains the index of that sequence in this vector */
	protected int currSeqIndex;

    /** this is the index of the next action in the current sequence that is to
	be executed */
	protected int currActIndex;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */

    /** shared initialization routine */
    protected void defaultInit()
    {
        replSeq = null;
        currSeqIndex = Route.NONE;
        currActIndex = Route.NONE;
    }
    
    /** creates an empty route by default */
	public Route() 
    {
        defaultInit();
	}//ctor

    /** creates a route from a given vector of sequence */
	public Route(Vector<Sequence> initSeq)
    {
        super(initSeq);
        
        defaultInit();
        if ((initSeq == null) || initSeq.size() == 0) return;

        currSeqIndex = 0;
        currActIndex = 0;
	}//ctor

    /** copy ctor */
    public Route(Route orig)
    {
        super(orig);
        replSeq = orig.replSeq;
        currSeqIndex = orig.currSeqIndex;
        currActIndex = currActIndex;
    }

    /** creates a new route that contains a given sequence */
    public static Route newRouteFromSequence(Sequence seq)
    {
        Vector<Sequence> vec = new Vector<Sequence>();
        vec.add(seq);
        return new Route(vec);
    }

    /** creates a new route that contains a given sequence and starts at a
     * given offset into that sequence*/
    public static Route newRouteFromSequence(Sequence seq, int offset)
    {
        Route result = newRouteFromSequence(seq);
        result.currActIndex = offset;
        return result;
    }

    /*======================================================================
     * Public Methods
     *----------------------------------------------------------------------
     */

    /** copy me! */
    public Object clone()
    {
        return new Route(this);
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
		else if(currActIndex >= this.elementAt(currSeqIndex).length())
        {
            currActIndex = 0;
            currSeqIndex++;
        }
		
        if(currSeqIndex >= this.size())
        {
            return null;
        }
        else
        {
        	return this.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
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
        else                return this.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
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
		else 				return this.elementAt(currSeqIndex);
	}

    /**
     * applyReplacement
     *
     * applies a given replacement to a route
     */
	public void applyReplacement(Replacement repl) 
    {
		replSeq = repl.apply(this.getCurrSequence());
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
        for(Sequence s : this)
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
        for(int i = currSeqIndex; i < this.size(); ++i)
        {
            count += this.elementAt(i).numElementalEpisodes();
        }
		return count - currActIndex;
	}

    /** @return true if this route contains a given sequence */
    
}//class Route
