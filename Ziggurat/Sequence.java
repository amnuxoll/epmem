package Ziggurat;

import java.util.*;


/**
 * class Sequence
 *
 * Each instance of this class models a temporal sequence of #Action.
 */
public class Sequence extends DecisionElement
{
    protected Vector<Action> actions;
//%%%AMN: Still necessary?:     protected double utility;
//%%%AMN: Forgot what this is for:  protected boolean valid; 

    public Sequence()
    {
    	actions = new Vector<Action>();
    }
    
    public Sequence(Vector<Action> acts)
    {
    	this.actions = acts;
    }
    
    /** accessor methods */
    public Vector<Action> getActions() { return this.actions; }
    public Action getActionAtIndex(int i) { return (this.actions.size() > i ? this.actions.elementAt(i) : null); }
    public int length() { return this.actions.size(); }
    
    /**
     * @return true if the given Sequence matches this one.
     *
     * CAVEAT: This method is fairly expensive.  Use it sparingly.
     */
    public boolean equals(Sequence other) 
    {
        int len = this.length();
        if (other.length() != len) return false;

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
        	seq.addEntry(a.clone());
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
    public void addEntry(Action act) 
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
        	Episode[] eps = a.getEpisodes();
            for (Episode ep : eps)
            {
                //Count them if elemental, otherwise, recurse
                if (ep instanceof ElementalEpisode)
                {
                    count++;
                    // Add 2 for final action
                    if(a == actions.lastElement()) count++;
                    break;	// Remember that Actions overlap...
                }
                else //SequenceEpisode
                {
                    Sequence seq =  ((SequenceEpisode)ep).getSequence();
                    count += seq.numElementalEpisodes();  //recurse
                }
            }//for
        }//for

        return count;
    }//numElementalEpisodes

}//class Sequence

