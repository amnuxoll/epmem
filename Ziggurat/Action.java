package Ziggurat;
 
import java.util.*;

/**
 * class Action
 *
 * An action consists of an Episode+Action-->Resulting Episode
 * triplet. In English, that's "I was in this state, I took this
 * action and ended up in this new resulting state."
 */
public class Action extends DecisionElement
{
	protected Episode lhs;  //left-hand side episode
	protected Episode rhs;  //right-hand side episode
	protected int freq;     //how many times the agent has experienced this action
	protected Vector<Action> cousins;   //other rules with same lhs, different LHS
	protected double utility;           //utility of this action

	/** ctor initializes the episodes that make up the action and then
     * initializes the other variables with default starting values. */
	public Action(Episode lhs, Episode rhs) 
    {
        this.level = lhs.getLevel();  //inherit level from constituent episodes
		this.lhs = lhs;
        this.rhs = rhs;
        this.freq = 0;
        cousins = new Vector<Action>();
        utility = 0.0;
	}//ctor


    /** accessor methods */
    public Episode getLHS() { return lhs; }
    public Episode getRHS() { return rhs; }
    public Episode[] getEpisodes() { return new Episode[] {lhs,rhs}; }
    public Vector<Action> getCousins() { return cousins; }
    public void incrementFreq() { this.freq++; }
    public void setCousins(Vector<Action> newList) { this.cousins = newList; }
    
    
    /**
     * compares two actions.  If the actions contain elemental episodes then the
	 * rhs only need the sensors to match.
     */
	public boolean equals(Action other) 
    {
        //Catch the obvious case
        if (other == this) return true;

        //compare left-hand-sides
        if (!this.lhs.equals(other.lhs))
        {
            return false;
        }

        //compare right-hand-side (non-ElementalEpisode)
        if (! (other.rhs instanceof ElementalEpisode) )
        {
            return this.rhs.equals(other.rhs);
        }

        //When comparing Elemental RHS, only the sensors matter 
        if (other.rhs instanceof ElementalEpisode)
        {
            ElementalEpisode ee1 = (ElementalEpisode)this.rhs;
            ElementalEpisode ee2 = (ElementalEpisode)other.rhs;
            
            return ee1.equalSensors(ee2);
        }
        
        return false;
           
	}//equals

    /** @return a String representation of this action.
     * Typically you want to use the printing facility in the current
     * Environment# class instead.
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

    /** @return true if this is an indeterminate action. */
    public boolean isIndeterminate()
    {
        return (this.cousins.size() > 0);
    }
    
    public Action clone()
    {
    	Action rtn = new Action(this.lhs.clone(), this.rhs.clone());
    	rtn.cousins.addAll(this.cousins);
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


 
