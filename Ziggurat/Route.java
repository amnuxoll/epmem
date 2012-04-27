package Ziggurat;

import java.util.*;

/**
 * Route
 * 
 * This class defines a Route. A Route is primarily comprised of
 * a vector of Sequences that the agents expects/intends will take it from its
 * current state to some goal/reward state.
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

    /** a list of all the replacements that are currently active for this route */
    protected Vector<Replacement> repls = new Vector<Replacement>();

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
        currActIndex = orig.currActIndex;
        repls = orig.repls;
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

    /** accessors */
    public int getCurrActIndex() { return this.currActIndex; }
    public int getCurrSeqIndex() { return this.currSeqIndex; }
    public Vector<Replacement> getRepls() { return this.repls; }

    /** @return the number of active replacements on this plan*/
    public int numRepls()   { return this.repls.size(); }
    
    
    /** copy me! */
    public Object clone()
    {
        return new Route(this);
    }

    /** create an environment-inspecific String representation of this route
     * which is a comma-separated list of sequences enclosed in curly braces. */
	public String toString () 
    {

        //If the route contains more than one sequence, we'll put an asterisk
        //after the current sequence so it can be easily spotted.  To do this we
        //need to note the current sequence
        Sequence currSeq = (this.size() == 1) ? null : this.getCurrSequence();

        //This loop creates the result string
        String result = "{ ";   // return value
        for(int i = 0; i < this.size(); i++)
        {
            Sequence seq = this.elementAt(i);
            
            //precede all but first sequence with a comma separator
            if (i > 0) result += ", ";  

            result += seq.toString();

            // demark current sequence if seen
            if (seq == currSeq) result += "*";
        }
        result += " }";

        //Mark the current action in the current sequence with an asterisk using
        //some fancy and expensive string manipulation
        String[] parts = result.split(",", this.currActIndex + 2);
        parts[this.currActIndex] += "*";
        result = "";
        for(String s : parts)
        {
            result += s + ",";
        }

        return result;
	}//toString

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

        //If the new action index doesn't exceed the current sequence then we
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

        //Active replacements needs to be re-applied to the new sequence
        currSeq = this.getCurrSequence();
        for(Replacement repl : this.repls)
        {
            if (repl.canApply(currSeq))
            {
                currSeq = repl.apply(currSeq);
                this.replSeq = currSeq;
            }
        }//for
                
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
     * applyReplacement
     *
     * applies a given replacement to a route
     */
	public void applyReplacement(Replacement repl) 
    {
        //Apply the replacement
        Sequence currSeq = this.getCurrSequence();
        
        //Sanity Check:  the replacement should not modify the route before the
        //current action
        assert(repl.applyPos(currSeq) >= this.currActIndex);
        
        //Save this result into the route
        replSeq = repl.apply(currSeq);

        //Save the replacement to apply to future sequences
        this.repls.add(repl);

	}//applyReplacement

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
