package Ziggurat;

import java.util.*;

/**
 * class ElementalEpisode
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
	protected int id;  //a unique identifier, assigned sequentially
	protected WMESet sensors;
	protected int cmd;

    /** ctor initializes instance variables */
	public ElementalEpisode(int id, Hashtable<String,WME> sensors, int cmd, double utility) 
    {
        super(utility);
        
		this.id      = id;
        this.sensors = new WMESet(sensors);
        this.cmd     = cmd;
	}//ctor
	
	public ElementalEpisode(int id, WMESet sensors, int cmd, double utility) 
    {
        super(utility);
        
		this.id      = id;
        this.sensors = sensors;
        this.cmd     = cmd;
	}//ctor

    /** partial ctor leaves some items set to default values */
	public ElementalEpisode(int id, Hashtable<String,WME> sensors) 
    {
		this.id      = id;
        this.sensors = new WMESet(sensors);
        this.cmd     = 0;
	}
	
	public ElementalEpisode(int id, WMESet sensors) 
    {
		this.id      = id;
        this.sensors = sensors;
        this.cmd     = 0;
	}

	/** episodes are equal if they contain the same knowlege */
	public boolean equals(Episode other) 
    {
        //must both be ElementalEpisode
        if (! (other instanceof ElementalEpisode)) return false;

        //verify that all relevant instance variables match
        ElementalEpisode ee = (ElementalEpisode)other;
        return (equalSensors(ee) && this.cmd == ee.cmd);
	}//equals

	/** returns true if the sensors of two given elemental episodes match */
	public boolean equalSensors(ElementalEpisode other) 
    {
		return this.sensors.equals(other.sensors);
	}

	/**
     * @return a string containing all the WMEs in the episode + command.
     * Typically you want to use the printing facility in the current
     * Environment# class instead.
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
	
	public ElementalEpisode clone()
	{
		return new ElementalEpisode(this.id, this.sensors.clone(), this.cmd, 0.0);
	}

}//class ElementalEpisode

