package Ziggurat;

import java.util.*;

/**
 * Route
 * 
 * This class defines a Route. A Route is primarily comprised of a long Sequence
 * of Actions that the agents expects/intends will take it from its current
 * state to some goal/reward state.  Routes are constructed using multiple
 * sequences from the Ziggurat hierarchy.
 * 
 * <h4>An Important Note About Routes</h4>
 *
 * Consider a route that looks like this: <code>{A->B, B->C, C->D, D->E, E->F,
 * F->G}</code> where the letters are the episodes and the arrows show LHS and
 * RHS of actions.  If this is a level 0 route, then executing the route is as
 * simple as issuing the command in each of the episodes A through F.  The G
 * part is the goal state that we're trying to reach and, if this route is
 * correct, it will be reached immediately upon executing the command in F.
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

public class Route extends Sequence
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** this value is used to indicate that there is no current sequence or action */
    public static final int NONE = -1;
    /** used for {@link #toString} */
    public static NullEnvironment nullEnv = new NullEnvironment();
    
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    
    /** this is the index of the next action in this route that is to be
	executed */
	protected int currActIndex = 0;

    /** a list of all the replacements that are currently active for this route */
    protected Vector<Replacement> repls = new Vector<Replacement>();

    /** a vector of all the Sequences that have been added to this Route.  This
     * vector is used to help with Route construction.  It's important to note
     * that, due to possible replacements, iterating through the Actions in
     * these Sequences in order will *not* necessarily yield the same actions as
     * you would get from iterating through the route.
     */
    protected Vector<Sequence> seqs = new Vector<Sequence>();

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */

    /** it's ok to create an empty route */
	public Route() 
    {
        super();
	}//ctor

    /**
     * creates a route from a given sequence
     */
	public Route(Sequence initSeq)
    {
        super();
        if (initSeq == null) return;
        this.add(initSeq);

	}//ctor

    /**
     * creates a route from a given sequence and with a given staring value for
     * the current action index
     */
	public Route(Sequence initSeq, int offset)
    {
        this(initSeq);
        this.currActIndex = offset;
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
        super();
        
        if ((initSeq == null) || initSeq.size() == 0) return;

        for(Sequence seq : initSeq)
        {
            this.add(seq);
        }
	}//ctor

    /** creates a new route from the LHS of a given Action.  The Action must be
     * from level 1 or higher.
     *
     */
    public Route (Action parentAct)
    {
        super();
        
        //The given action must be of level 1 or higher
        if (parentAct.getLevel() < 1) return;

        SequenceEpisode seqEp = (SequenceEpisode)parentAct.getLHS();
        Sequence seq = seqEp.getSequence();
        this.add(seq);
    }//ctor (create new route from parent action)

    /*======================================================================
     * Public Methods
     *----------------------------------------------------------------------
     */

    /** accessors */
    public int getCurrActIndex() { return this.currActIndex; }
    public Vector<Replacement> getRepls() { return this.repls; }
    /** @return the number of sequences used to build this route*/
    public int numSeqs()   { return this.seqs.size(); }
    /** @return the number of active replacements on this route*/
    public int numRepls()   { return this.repls.size(); }
    /** @return the last sequence that was added to this route */
    public Sequence getLastSeq()
    {
        if (this.seqs.size() == 0) return null;
        return this.seqs.elementAt(this.seqs.size() - 1);
    }
    /** does this route contain a given sequence? */
    public boolean contains(Sequence seq) { return this.seqs.contains(seq); }

    /** @return a copy of this Route */
    public Route clone()
    {
        Sequence currActions = super.clone();
        Route result = new Route(currActions, this.currActIndex);
        for(Sequence seq : this.seqs)
        {
            result.seqs.add(seq);
        }

        return result;
    }//clone

    /** 
     * Typically you want to use the printing facility in the specific
     * { @link Environment} class instead.
     *
     * @return a String representation of this route
     */
	public String toString() 
    {
        return nullEnv.stringify(this);
	}//toString

    /**
     * I'm overriding this method to throw an error.  Actions should not be
     * added directly to a Route.  They should only be added via a containing
     * sequences.
     */
    @Override
    public void add(Action act) 
    {
        System.err.println("ABORT!  Tried to add an action directly to a Route.");
        Thread.dumpStack();
        System.exit(-2);
    }//add (Action)

    
    /** appends all the actions in a given sequence to the end of this route */
    public void add(Sequence seq) 
    {
        for(Action act : seq.actions)
        {
            this.actions.add(act);
        }
            
        this.seqs.add(seq);

        //Set the level if this is the first sequence
        if (this.seqs.size() == 1)
        {
            this.level = seq.getLevel();
        }

    }//add (Sequence)

        
    /**
     * advance
     *
     * advances this route by a single step.  
     *
     * @return the new current action or null if we've reached the end of the
     * route 
     */
	public Action advance() 
    {
        //The only mandatory step
		(this.currActIndex)++;

        //If the new action index is valid then we are done
        if (this.currActIndex < this.actions.size())
        {
            return this.getCurrAction();
        }

        //Special Case: If the new action index has just reached the end of the
        //last sequence in the route then we still have to execute the RHS of
        //the last action (unless this is a level 0 route).  To "fake it",
        //create a temporary Action whose LHS is the real last action's RHS.
        //For more explanation, see "An Important Note About Routes" at the top
        //of this file.
        if ((this.currActIndex == this.actions.size()) && (this.getLevel() > 0))
        {
            Action lastAct = this.lastAction();
            SequenceEpisode seqEp = (SequenceEpisode)lastAct.getRHS();
            Action result = new Action(seqEp, ElementalEpisode.EMPTY);
            return result;
        }

        //At this point there is nothing left to return
        return null;
	}//advance

    /**
     * @return a reference to the current action in the current sequence or null
     *         if there is none
     */
	public Action getCurrAction() 
    {
        if (this.currActIndex == NONE) return null;
		return this.getActionAtIndex(this.currActIndex);
	}//getCurrAction

    /**
     * applyReplacement
     *
     * applies a given replacement to this route
     */
	public void applyReplacement(Replacement repl) 
    {
        //Apply the Replacement
        Sequence replSeq = repl.apply(this, this.currActIndex);
        this.actions = replSeq.actions;

        //Log that this Replacement has ben applied
        this.repls.add(repl);

	}//applyReplacement

    /**
     * numElementalEpisodes         <!-- RECURSIVE -->
     *
     * counts the total number of elemental episodes in this route.
     *
     * <p><b>Note</b>: This method is somewhat expensive.
     */
	public int numElementalEpisodes() 
    {
        //At level 0 this is easy
        if (this.getLevel() == 0)
        {
            return this.actions.size();
        }

        //Count the episodes in each SequenceEpisode's inherent sequence
        int count = 0;
        for(Action act : this.actions)
        {
            SequenceEpisode seqEp = (SequenceEpisode)act.getLHS();
            count += seqEp.getSequence().numElementalEpisodes();  //recurse
        }

        //add the ElementalEpisodes in the RHS of the last action
        Action act = this.lastAction();
        SequenceEpisode seqEp = (SequenceEpisode)act.getRHS();
        count += seqEp.getSequence().numElementalEpisodes(); // recurse
        
		return count;
	}//numElementalEpisodes

    /**
     * @return the level of the actions in this route
     */
    public int getLevel()
    {
        return this.level;
    }

}//class Route
