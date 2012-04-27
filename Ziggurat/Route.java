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

    /** creates a new route from the LHS of a given Action.  The Action must be
     * from level 1 or higher.
     *
     * @return the new Route or null on failure
     */
    public static Route newRouteFromParentAction(Action act)
    {
        assert(act.getLevel() >= 1);

        SequenceEpisode seqEp = (SequenceEpisode)act.getLHS();
        Sequence seq = seqEp.getSequence();
        return newRouteFromSequence(seq);
    }//newRouteFromParentAction

    /*======================================================================
     * Public Methods
     *----------------------------------------------------------------------
     */

    /** copy me! */
    public Object clone()
    {
        return new Route(this);
    }

    /** acessor */
    public int getCurrActIndex() { return this.currActIndex; }
    public int getCurrSeqIndex() { return this.currSeqIndex; }
    
    /**
     * advance
     *
     * advances this route by a single step.  This may require updating the
     * sequence pointer as well.  Furthermore, if there are active replacements
     * they need to be applied
     *
     * @return the new current action or null if we've reached the end of the
     * route 
     */
	public Action advance () 
    {
        //The only mandatory step
		(this.currActIndex)++;

        //If the new action index doesn't exceeds the current sequence then we
        //are done
        Sequence currSeq = this.getCurrSequence();
        if (this.currActIndex < currSeq.length())
        {
            return this.getCurrAction();
        }

        //If we reach this point, then advance to next sequence and reset the
        //action index
        (this.currSeqIndex)++;
        this.currActIndex = 0;
        this.replSeq = null;
        
        //If the new sequence index exceeds the array then there is no next
        //action to return
        if (this.currSeqIndex >= this.size()) return null;

        //Active replacements needs to be re-applied to this route
        //TBD%%%

        return this.getCurrAction();
	}//advance

    /**
     * @return the current action in the current sequence
     */
	public Action getCurrAction() 
    {
        assert(this.currActIndex != NONE);
        Sequence currSequence = this.getCurrSequence();
        assert(this.currActIndex < currSequence.length());
               
		return currSequence.getActionAtIndex(this.currActIndex);
	}//getCurrAction

    /**
     * @return a reference to the current sequence
     */
	public Sequence getCurrSequence() 
    {
        assert(this.currSeqIndex != NONE);
        assert(this.currSeqIndex < this.size());
		if(replSeq != null) return replSeq;
		else 				return this.elementAt(currSeqIndex);
	}//getCurrSequence

    /**
     * canApply
     *
     * checks to see if a given replacement can be applied to the current
     * sequence in this route at a position that is not beyond the current
     * action in the sequence.
     *
     * @param repl  the replacement to consider
     *
     * @return true if it can be applied, false otherwise
     */
    public boolean canApply(Replacement repl)
    {
        Sequence currSeq = this.getCurrSequence();
        int applyPos = repl.applyPos(currSeq);
        return (applyPos >= this.currActIndex);
    }//canApply

    /**
     * apply
     *
     * applies a given replacement to a route
     */
	public void apply(Replacement repl) 
    {
		replSeq = repl.apply(this.getCurrSequence());
	}//apply

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

    /**
     * @return the level of the sequences in this route
     */
    public int getLevel()
    {
        Sequence seq = this.getCurrSequence();
        return seq.getLevel();
    }

    
    
}//class Route
