package Ziggurat;

import java.util.*;


/**
 * <!-- class Sequence -->
 *
 * Each instance of this class models a temporal sequence of {@link Action}.
 * Sequences are divided by indeterminate actions.  Whenever an indeterminate
 * action occurs, it becomes the last action in the sequence and a new sequence
 * begins with the next action.
 */
public class Sequence extends DecisionElement
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** used for {@link #toString} */
    public static NullEnvironment nullEnv = new NullEnvironment();

    /** used to generate a unique id for each sequence */
    protected static int nextID = 0;

    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** each sequence is assigned a unique id */
    private int id = -1;
    
    /** these are the actions that comprise the sequence (order matters) */
    protected Vector<Action> actions;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /** default ctor creates an empty sequence */
    public Sequence()
    {
    	actions = new Vector<Action>();
        id = nextID++;
    }

    /** this ctor inits the new Sequence with the given actions */
    public Sequence(Vector<Action> acts)
    {
    	this.actions = acts;

        //inherit level from actions
        if (acts.size() > 0)
        {
            this.level = acts.elementAt(0).getLevel();
        }

        id = nextID++;
    }
    
    /*======================================================================
     * Accessors
     *----------------------------------------------------------------------
     */
    public Vector<Action> getActions() { return this.actions; }
    public Action getActionAtIndex(int i) { return (this.actions.size() > i ? this.actions.elementAt(i) : null); }
    public Action firstAction() { return getActionAtIndex(0); }
    public Action lastAction() { return getActionAtIndex(this.actions.size() - 1); }
    public int length() { return this.actions.size(); }
    
    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /**
     * @return true if the given Sequence matches this one.
     *
     * CAVEAT: This method is fairly expensive.  Use it sparingly.
     */
    public boolean equals(Object other) 
    {
        //Verify we've been given a sequence
        if (! (other instanceof Sequence)) return false;
        Sequence seq = (Sequence)other;
        
        //Catch the obvious case
        if (seq == this) return true;

        //must be same length or don't bother
        int len = this.length();
        if (seq.length() != len) return false;

        //compare the constituent actions
        for(int i = 0; i < len; i++)
        {
            Action a1 = this.actions.elementAt(i);
            Action a2 = seq.actions.elementAt(i);

            if (! a1.equals(a2) ) return false;
        }//for

        return true;
    }//equals

    /**
     * clone
     *
     * creates a deep copy of this sequence
     * 
     * @return a duplicate of this sequence with a reset utility
     *
     */
    @SuppressWarnings("unchecked")
    public Sequence clone()
    {
        Sequence seq = new Sequence();

        for(Action a : this.actions)
        {
        	seq.add(a.clone());
        }

        //These values inherited from DecisionElement
        seq.utility = this.utility;
        seq.level = this.level;

        return seq;
    }//clone

    /** 
     * Typically you want to use the printing facility in the specific
     * Environment# class instead.
     *
     * @return a string representation of this sequence
     */
    public String toString() 
    {
        return nullEnv.stringify(this);
    }//toString

    /** appends a given action to the end of the sequence */
    public void add(Action act) 
    {
        actions.add(act);

        //If this is the first action added to the sequence, then inherit its
        //level
        if (this.actions.size() == 1)
        {
            this.level = act.getLevel();
        }
    }//add

    /**
     * numElementalEpisodes          *RECURSIVE*
     *
     * counts the number of ElementalEpisodes a sequence contains
     *
     * @return the total number of elemental episdoes in this sequence
     *
     * CAVEAT:  This method is expensive.  Use only when necessary.
     */
    public int numElementalEpisodes() 
    {
        int count = 0;
        //Extract the episodes from each action in the sequence
        for(Action a : actions)
        {
        	Episode ep = a.getLHS();
        	if(ep instanceof ElementalEpisode)
        	{
        		count++;
        	}
        	else
    		{
    			Sequence seq =  ((SequenceEpisode)ep).getSequence();
                count += seq.numElementalEpisodes();  //recurse
    		}
        }//for

        return count;
    }//numElementalEpisodes

    /**
     * findEquivalent
     *
     * determines whether a given Vector<Sequence> contains a Sequence that is
     * equivalent to this one.
     *
     * @param vec  the vector to search
     *
     * @return the equivalent sequence if found, null otherwise
     */
    public Sequence findEquivalent(Vector<Sequence> vec)
    {
        Sequence found = null;
        for(Sequence seq : vec)
        {
            //don't compare it to itself
            if (seq == this) continue;

            //See if they are equivalent
            if (seq.equals(this))
            {
                found = seq;
                break;
            }
        }

        return found;
        
    }//findEquivalent

    /**
     * containsReward
     *
     * @return true if one of its constituent episodes contains a reward
     */
    public boolean containsReward()
    {
        for(Action act : this.actions)
        {
            if (act.containsReward()) return true;
        }

        return false;
    }
                
    
    
}//class Sequence

