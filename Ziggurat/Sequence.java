package Ziggurat;

import java.util.*;


/**
 * class Sequence
 *
 * Each instance of this class models a temporal sequence of #Action.  Sequences
 * are divided by indeterminate actions.  Whenever an indeterminate action
 * occurs, it becomes the last action in the sequence and a new sequence begins
 * with the next action.
 */
public class Sequence extends DecisionElement
{
    /** these are the acitons that comprise the sequence (order matters) */
    protected Vector<Action> actions;

    /** default ctor creates an empty sequence */
    public Sequence()
    {
    	actions = new Vector<Action>();
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
    }
    
    /** accessor methods */
    public Vector<Action> getActions() { return this.actions; }
    public Action getActionAtIndex(int i) { return (this.actions.size() > i ? this.actions.elementAt(i) : null); }
    public Action firstAction() { return getActionAtIndex(0); }
    public Action lastAction() { return getActionAtIndex(this.actions.size() - 1); }
    public int length() { return this.actions.size(); }
    
    /**
     * @return true if the given Sequence matches this one.
     *
     * CAVEAT: This method is fairly expensive.  Use it sparingly.
     */
    public boolean equals(Sequence other) 
    {
        //Catch the obvious case
        if (other == this) return true;

        //must be same length or don't bother
        int len = this.length();
        if (other.length() != len) return false;

        //compare the constituent actions
        for(int i = 0; i < len; i++)
        {
            Action a1 = this.actions.elementAt(i);
            Action a2 = other.actions.elementAt(i);

            if (! a1.equals(a2) ) return false;
        }//for

        return true;
    }//equals

    /**
     * clone
     *
     * @return a duplicate of this sequence with a reset utility
     *
     * CAVEAT:  this method makes a shallow copy of its internal #Action vector
     */
    @SuppressWarnings("unchecked")
    public Sequence clone()
    {
        Sequence seq = new Sequence();

        for(Action a : this.actions)
        {
        	seq.add(a.clone());
        }

        return seq;
    }//clone

    /** @return a string representation of this sequence 
     * Typically you want to use the printing facility in the current
     * Environment# class instead.
     */
    public String toString() 
    {
        // a sequence is just a of actions separated by commas and surrounded by
        // square brackets
        String result = "[";
        boolean first = true;
        for(Action a : actions)
        {
            if (!first)
            {
                result += ",";
                first = false;
            }
                    
            result += a.toString();
        }
        result += "]";

        return result;
        
    }//toString

    /** appends a given action to the end of the sequence */
    public void add(Action act) 
    {
        actions.add(act);
    }

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

