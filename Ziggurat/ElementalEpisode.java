package Ziggurat;

import java.util.*;

/**
 * <!-- class ElementalEpisode -->
 *
 * This class contains information about an atomic episode from the agent's
 * experience.  In essence, it is a snapshot of an instant in time from the
 * agent's perspective.
 *
 * Currently, it consists of an sequences of WMEs representing the agent's
 * sensing at that instant in time paired with the command that it issued and
 * the utility it associates with that experience (which may change post hoc).
 *
 */
public class ElementalEpisode extends Episode 
{
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /* %%%AMN:  Do we really need an ID? %%% */
    /** used to assign a unique id to each episode (assigned sequentially) */
    protected static int nextId = 0;
    /** a unique identify for this episode */
    protected int id;
    /** the agents sensors in this episode */
	protected WMESet sensors;
    /** the command the agent selected */
	protected int cmd;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */

    /** default ctor sets default values*/
    private ElementalEpisode()
    {
        this.cmd     = 0;
        this.level   = 0;
    }
        
    
    /** this ctor initializes all instance variables */
	public ElementalEpisode(int id, WMESet sensors, int cmd, double utility) 
    {
        super(utility);
        
		this.id      = id;
        this.sensors = sensors;
        this.cmd     = cmd;
        this.level   = 0;
	}//ctor

    /** partial ctor leaves the command set to a default value */
	public ElementalEpisode(int id, WMESet sensors) 
    {
        this();
		this.id      = id;
        this.sensors = sensors;
	}//ctor

    /** partial ctor leaves command as a default value and sets the id as the
     * next one iteratively (using #nextId) */
	public ElementalEpisode(WMESet sensors) 
    {
        this();
		this.id      = nextId;
        nextId++;
        this.sensors = sensors;
	}//ctor

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
	/** episodes are equal if they contain the same knowlege */
	public boolean equals(Object other) 
    {
        //must both be ElementalEpisode
        if (! (other instanceof ElementalEpisode)) return false;
        ElementalEpisode ee = (ElementalEpisode)other;

        //Catch the obvious cases
        if (ee == this) return true;
        if (ee.level != this.level) return false;

        //verify that all relevant instance variables match
        return (equalSensors(ee) && this.cmd == ee.cmd);
	}//equals

	/** returns true if the sensors of two given elemental episodes match */
	public boolean equalSensors(ElementalEpisode other) 
    {
		return this.sensors.equals(other.sensors);
	}

	/**
     * Typically you want to use the printing facility in the current
     * Environment# class instead.
     *
     * @return a string containing all the WMEs in the episode + command.
     */
	public String toString() 
    {
        return sensors.toString() + cmd;
	}

    /**
     * @return a string representation of this episode's sensors
     */
    public String sensorsToString()
    {
        return sensors.toString();
    }

	/** returns the WME */
	public boolean containsAttr(String attr) 
    {
        return sensors.hasAttr(attr);
	}

    /** create a close of this episode */
	public ElementalEpisode clone()
	{
		return new ElementalEpisode(this.id, this.sensors.clone(), this.cmd, this.utility);
	}

    /** set the value of cmd */
    public void setCommand(int cmd)
    {
        this.cmd = cmd;
    }

    /** get the value of the cmd */
    public int getCommand()
    {
        return this.cmd;
    }

    /**
     * @return true if this episode contains a reward
     */
    public boolean containsReward()
    {
        return this.sensors.hasAttr(WME.REWARD_STRING);
    }
                
}//class ElementalEpisode

