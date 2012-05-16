package Ziggurat;

import java.util.*;

/**
 * Route
 * 
 * This class defines a Route. A Route is primarily comprised of
 * a vector of Sequences that the agents expects/intends will take it from its
 * current state to some goal/reward state.
 * 
 * <h4>An Important Note About Routes</h4>
 *
 * Consider a route that looks like this: <code>{ A->B, B->C, C->D}, {D->E,
 * E->F, F->G}</code> where the letters are the episodes and the arrows show LHS
 * and RHS of actions and the curly braces show sequences.  If this is a level 0
 * route, then executing the route is as simple as issuing the command in each
 * of the episodes A through F.  The G part is the goal state that we're trying
 * to reach and, if this route is correct, it will be reached immediately upon
 * executing the command in F.
 *
 * <p>However, if this route is at level 1 or higher then each of those episodes
 * (A through G) is a SequenceEpisode.  That means that the 'G' in the last
 * action, F->G, contains a sequence from one level lower that must be completed
 * in order to actually reach the goal state.
 *
 * <p>This difference has an important impact on both the {@link #advance} and
 * {@link #remainingElementalEpisodes} methods
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
    
    /** if a {@link Replacement} has been applied to the sequence the agent is
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

    /**
     * the current level of this route as determined by its constituent
     * sequences
     */
    protected int level = -1;

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

    /**
     * creates a route from a given vector of sequence.
     *
     * <p><b>CAVEAT</b>: This routine does not verify that the given sequence
     * creates a valid route.  In particular, it does not verify that the RHS of
     * the last action in each sequence matches the LHS of the first action in
     * the next sequence.
     * 
     */
	public Route(Vector<Sequence> initSeq)
    {
        super(initSeq);
        
        defaultInit();
        if ((initSeq == null) || initSeq.size() == 0) return;

        currSeqIndex = 0;
        currActIndex = 0;
        Sequence seq = this.elementAt(this.currSeqIndex);
        this.level = seq.getLevel();
        
	}//ctor

    /** creates a new route that contains just one sequence */
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
        //The given action must be of level 1 or higher
        if (act.getLevel() < 1) return null;

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
    public Sequence getReplSeq() { return this.replSeq; }

    /** @return the number of active replacements on this plan*/
    public int numRepls()   { return this.repls.size(); }
    
    
    /** return a deep copy of this Route */
    public Route clone()
    {
        //Clone the sequences
        Vector<Sequence> copySeq = new Vector<Sequence>();
        for(Sequence s : this)
        {
            copySeq.add(s.clone());
        }
        
        Route copy = new Route(copySeq);
        copy.currSeqIndex = this.currSeqIndex;
        copy.currActIndex = this.currActIndex;
        copy.level = this.level;
        if (this.replSeq != null)
        {
            copy.replSeq = this.replSeq.clone();
        }
        else
        {
            copy.replSeq = null;
        }

        return copy;
        
    }//clone

    /** create an environment-inspecific String representation of this route
     * which is a comma-separated list of sequences enclosed in curly braces. */
	public String toString() 
    {

        //If the route contains more than one sequence, we'll put an asterisk
        //after the current sequence so it can be easily spotted.  To do this we
        //need to note the current sequence
        Sequence currSeq = (this.size() == 1) ? null : this.getCurrSequence();

        //This loop creates the result string
        String result = "{ ";   // return value
        for(int i = 0; i < this.size(); i++)
        {
            //Get the i-th sequence
            Sequence seq = this.elementAt(i);
            if ((i == this.currSeqIndex) && (this.replSeq != null))
            {
                seq = this.replSeq;
            }
            
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
     * they need to be applied.
     *
     *
     * @return the new current action or null if we've reached the end of the
     * route 
     */
	public Action advance() 
    {
        //The only mandatory step
		(this.currActIndex)++;

        //If the new action index doesn't exceed the current sequence then we
        //are done
        Sequence currSeq = this.getCurrSequence();
        if (currSeq == null) return null;
        if (this.currActIndex < currSeq.length())
        {
            return this.getCurrAction();
        }

        //If we reach this point, then advance to next sequence and reset the
        //action index
        (this.currSeqIndex)++;
        this.currActIndex = 0;
        this.replSeq = null;
        
        //Special Case: If the new action index has just reached the end of the
        //last sequence in the route then we still have to execute the RHS of
        //the last action (unless this is a level 0 route).  To "fake it",
        //create a temporary Action whose LHS is the real last action's RHS.
        //For more explanation, see "An Important Note About Routes" at the top
        //of this file.
        if ((this.currSeqIndex == this.size()) && (this.getLevel() > 0))
        {
            Action lastAct = currSeq.lastAction();
            SequenceEpisode seqEp = (SequenceEpisode)lastAct.getRHS();
            Action result = new Action(seqEp, ElementalEpisode.EMPTY);
            return result;
        }

        //If the new sequence index exceeds the array then there is no next
        //action to return
        if (this.currSeqIndex >= this.size()) return null;

        //If we reach this point, we've started in on a new sequence at this
        //level.  Active replacements needs to be re-applied to the new sequence
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
     * @return a reference to the current action in the current sequence or null
     *         if there is none
     */
	public Action getCurrAction() 
    {
        if (this.currActIndex == NONE) return null;
        Sequence currSequence = this.getCurrSequence();
        if (currSequence == null) return null;
        if(this.currActIndex >= currSequence.length()) return null;
               
		return currSequence.getActionAtIndex(this.currActIndex);
	}//getCurrAction

    /**
     * @return a reference to a <i>clone</i> of the next action after the
     * current action in this route (or null if there is none)
     *         
     */
	public Action getSecondAction() 
    {
        //First make sure there is a current action
        if (this.getCurrAction() == null) return null;

        //The easy case:  sufficient actions remain in the current sequence
        Sequence currSequence = this.getCurrSequence();
        if(this.currActIndex + 1 < currSequence.length())
        {
            return currSequence.getActionAtIndex(this.currActIndex + 1).clone();
        }

        //The hard case: we need to get an action from the next sequence.  This
        //is not trivial.  There may not be a next sequence or we may need to
        //use the RHS of the current (last) sequence.  Also a replacement may
        //need to be applied.  All of these problems have already been addressed
        //by the advance() method so I've elected to use it via a clone of this
        //Route.  This is a bit more expensive and it also means that the action
        //we must return will be a *clone* of actual second action.  But,
        //ultimately, I think this is the best option.
        Route clone = this.clone();
        clone.advance();
		return clone.getCurrAction();
	}//getSecondAction

    /**
     * @return a reference to the current sequence or null if there is none
     */
	public Sequence getCurrSequence() 
    {
        //Case:  This sequence has not been initialized
        if (this.currSeqIndex == NONE) return null;

        //Case:  The sequence index has exceeded the vector
        if (this.currSeqIndex >= this.size()) return null;

        //Case:  If a replacement sequence is in place, us it
		if(replSeq != null) return replSeq;

        //Default/Normal Case:  Get the sequence at the currSeqIndex
        return this.elementAt(this.currSeqIndex);
	}//getCurrSequence

    /**
     * @return a reference to the last action in the last sequence of this route
     */
	public Action lastAction() 
    {
        Sequence seq = this.lastElement();
        if (seq == null) return null;
               
		return seq.lastAction();
	}//getCurrAction

    /**
     * applyReplacement
     *
     * applies a given replacement to this route
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

        //At level 1+, add the ElementalEpisodes in the RHS of the last action
        //of the last sequence
        if (this.getLevel() > 0)
        {
            Action a = this.lastElement().lastAction();
            SequenceEpisode seqEp = (SequenceEpisode)a.getRHS();
            count += seqEp.getSequence().numElementalEpisodes();
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
        //Special case:  nothing left in the route
        Sequence currSeq = this.getCurrSequence();
        if (currSeq == null)
        {
            return 0; // no sequences left
        }

        
        //Count the number of elemental eps left in the current sequence
        int count = 0;
        if (this.getLevel() == 0)
        {
            count += currSeq.length() - this.currActIndex;
        }
        else //level 1+
        {
            //Add up the length of the each action's LHS sequence
            for(int i = currActIndex; i < currSeq.length(); i++)
            {
                Action a = currSeq.getActionAtIndex(i);
                SequenceEpisode seqEp = (SequenceEpisode)a.getLHS();
                count += seqEp.getSequence().numElementalEpisodes();
            }
        }//else
    
        //Count the number of elemental eps left in the subsequent sequences
        for(int i = currSeqIndex+1; i < this.size(); ++i)
        {
            count += this.elementAt(i).numElementalEpisodes();
        }
        
        //At level 1+, add the ElementalEpisodes in the RHS of the last action
        //of the last sequence
        if (this.getLevel() > 0)
        {
            Action a = this.lastElement().lastAction();
            SequenceEpisode seqEp = (SequenceEpisode)a.getRHS();
            count += seqEp.getSequence().numElementalEpisodes();
        }
		return count;
	}//remainingElementalEpisodes

    /**
     * @return the level of the sequences in this route
     */
    public int getLevel()
    {
        return this.level;
    }

}//class Route
