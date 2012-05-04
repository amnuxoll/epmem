package Ziggurat;

import java.util.*;

/**
 * <!-- class WMESet -->
 * 
 * A wrapper class that simplifies the handling of a set of WMEs. This class
 * allows us to more accurately and deliberately test a set of WMEs for
 * equality.
 *
 */
public class WMESet
{
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** This hashtable saves the WMEs indexed by their name */
	protected Hashtable<String,WME> sensors;
	
    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
	/**
	 * WMESet
	 * 
	 * constructor takes a Hashtable of WMEs and saves it after checking for
	 * null.
	 */
	public WMESet(Hashtable<String, WME> sensors)
	{
		this.sensors = sensors == null ? new Hashtable<String,WME>() : sensors;
	}//ctor
	
    /*======================================================================
     * Accessors
     *----------------------------------------------------------------------
     */
	/**
	 * hasAttr
	 * 
	 * takes a WME name and determines if this set contains a WME with that
	 * name,
	 */
	public boolean hasAttr(String attr)
	{
		return sensors.containsKey(attr);
	}//hasAttr
	
	/**
	 * getAttr
	 * 
	 * takes a WME name, confirms that it actually has the WME, and returns it
	 * if it does.
	 */
	public WME getAttr(String attr)
	{
		if(!this.hasAttr(attr)) return null;
		return this.sensors.get(attr);
	}//getAttr
	
    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
	/**
	 * standard equivalence comparison
	 */
	public boolean equals(Object other)
	{
        //Verify we've been given a WMESet
        if (! (other instanceof WMESet)) return false;
        WMESet wset = (WMESet)other;

		// Check sizes
		if(this.sensors.size() != wset.sensors.size()) return false;
		
		// Iterate through and make sure they each contain the same
		// WMEs with the same values.
		Enumeration<String> keys = this.sensors.keys();
		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
			if(!wset.sensors.containsKey(key) || !this.sensors.get(key).equals(wset.sensors.get(key)))
			{
				return false;
			}
		}
		return true;
	}//equals
	
	/**
	 * @return the string representation of this WMESet.
	 */
	public String toString()
	{
		String rtnVal = "{";
		List<String> keys = new ArrayList<String>(this.sensors.keySet());
		Collections.sort(keys);
		// Iterate through and generate string return value
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext())
		{
			rtnVal += this.sensors.get(iter.next()) + (iter.hasNext() ? "," : "");
		}
		rtnVal += "}";
		return rtnVal;
	}//toString
	
	/**
	 * clone
	 * 
	 * @return a copy of this WMESet.
	 */
	public WMESet clone()
	{
		Hashtable<String, WME> newSenses = new Hashtable<String,WME>();
		Set<String> ss = sensors.keySet();
		for(String s : ss)
		{
			newSenses.put(s, sensors.get(s).clone());
		}
		return new WMESet(newSenses);
	}//clone
	
	/**
	 * getSensorKeys
	 * 
	 * @return a Set of the names of all the WMEs in this WMESet
	 */
	public Set<String> getSensorKeys()
	{
		return sensors.keySet();
	}//getSensorKeys
}//class WMESet
