package Ziggurat;
 
import java.util.*;

/**
 * <!-- class Action -->
 *
 * An Action consists of an Episode+Action-->Resulting Episode
 * triplet. In English, that's "I was in this state, I took this
 * action and ended up in this new resulting state."
 */
public class Action extends DecisionElement
{
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** left-hand-side episode */
	protected Episode lhs;
    /** right-hand-side episode */
	protected Episode rhs;
    /** how many times the agent has experienced this action */
	protected int freq;
    /** other rules with same lhs, different LHS */
	protected Vector<Action> cousins;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
	/** ctor initializes the episodes that make up the action and then
     * initializes the other variables with default starting values. */
	public Action(Episode lhs, Episode rhs) 
    {
        this.level = lhs.getLevel();  //inherit level from constituent episodes
		this.lhs = lhs;
        this.rhs = rhs;
        this.freq = 0;
        cousins = new Vector<Action>();
        cousins.add(this);
        utility = 0.0;
	}//ctor


    /* accessor methods */
    public Episode getLHS() { return lhs; }
    public Episode getRHS() { return rhs; }
    public Episode[] getEpisodes() { return new Episode[] {lhs,rhs}; }
    public Vector<Action> getCousins() { return cousins; }
    public void incrementFreq() { this.freq++; }
    public int getFreq() { return this.freq; }
    public void setCousins(Vector<Action> newList) { this.cousins = newList; }
    
    
    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /**
     * compares two actions.  If the actions contain elemental episodes then the
	 * rhs only need the sensors to match.
     */
	public boolean equals(Object other) 
    {
        //Verify we've been given an action
        if (! (other instanceof Action)) return false;
        Action act = (Action)other;
        
        //Catch the obvious cases
        if (act == this) return true;
        if (act.level != this.level) return false;

        //compare left-hand-sides
        if (!this.lhs.equals(act.lhs))
        {
            return false;
        }

        //compare right-hand-side (non-ElementalEpisode)
        if (! (act.rhs instanceof ElementalEpisode) )
        {
            return this.rhs.equals(act.rhs);
        }

        //When comparing Elemental RHS, only the sensors matter 
        if (act.rhs instanceof ElementalEpisode)
        {
            ElementalEpisode ee1 = (ElementalEpisode)this.rhs;
            ElementalEpisode ee2 = (ElementalEpisode)act.rhs;
            
            return ee1.equalSensors(ee2);
        }
        
        return false;
           
	}//equals

    /** 
     * Typically you want to use the printing facility in the current
     * { @link Environment} class instead.
     *
     * @return a String representation of this action.
     */
	public String toString() 
    {
       //Start with lhs
       String retVal = this.lhs.toString() + "-";

       //if the action is indeterminate, the connecting arrow contains
       //an indication of the percent
       if (this.isIndeterminate())
       {
           int pct = this.freq * 100 / this.cousins.size();
           pct = Math.min(99, pct);
           pct = Math.max(00, pct);
           retVal += pct;
       }
       else
       {
           retVal += "--";
       }

       //finish the arrow
       retVal += "->";

       //if this action contains elemental actions, we only want to print the
       //sensors
       if (this.rhs instanceof ElementalEpisode)
       {
           retVal += ((ElementalEpisode)this.rhs).sensorsToString();
       }
       else
       {
           retVal += this.rhs.toString();
       }

       return retVal;
	}//toString

    /**
     * an indeterminate action is one that yields different results depending on
     * past actions.
     * 
     * @return true if this is an indeterminate action.
     */
    public boolean isIndeterminate()
    {
        return (this.cousins.size() > 1);
    }

    /** this is a deep copy */
    public Action clone()
    {
    	Action rtn = new Action(this.lhs.clone(), this.rhs.clone());
    	rtn.cousins.addAll(this.cousins);

        //These values inherited from DecisionElement
        rtn.utility = this.utility;
        rtn.level = this.level;

        return rtn;
    }//clone

    /**
     * @return true if its rhs contains a reward
     */
    public boolean containsReward()
    {
        return this.rhs.containsReward();
    }
                
}//class Action


 
