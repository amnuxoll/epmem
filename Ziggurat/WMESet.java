package Ziggurat;

import java.util.*;

/**
 * WMESet
 * 
 * A wrapper class that simplifies the handling of a set
 * of WMEs. This class allows us to more accurately and
 * deliberately test a set of WMEs for equality.
 *
 */
public class WMESet {
	
	// This hashtable saves the WMEs indexed by their name
	private Hashtable<String,WME> sensors;
	
	/*
	 * WMESet
	 * 
	 * This constructor takes a Hashtable of WMEs and saves
	 * it after checking for null.
	 */
	public WMESet(Hashtable<String, WME> sensors)
	{
		this.sensors = sensors == null ? new Hashtable<String,WME>() : sensors;
	}//ctor
	
	/*
	 * hasAttr
	 * 
	 * This method takes a WME name and determines if this
	 * set contains a WME with that name,
	 */
	public boolean hasAttr(String attr)
	{
		return sensors.containsKey(attr);
	}//hasAttr
	
	/*
	 * getAttr
	 * 
	 * This method takes a WME name, confirms that it actually
	 * has the WME, and returns it if it does.
	 */
	public WME getAttr(String attr)
	{
		if(!this.hasAttr(attr)) return null;
		return this.sensors.get(attr);
	}//getAttr
	
	/*
	 * equals
	 * 
	 * This method takes another WMESet and tests that they 
	 * contain equivalent WMEs.
	 */
	public boolean equals(WMESet other)
	{
		// Check sizes
		if(this.sensors.size() != other.sensors.size()) return false;
		
		// Iterate through and make sure they each contain the same
		// WMEs with the same values.
		Enumeration<String> keys = this.sensors.keys();
		while(keys.hasMoreElements())
		{
			String key = keys.nextElement();
			if(!other.sensors.containsKey(key) || !this.sensors.get(key).equals(other.sensors.get(key)))
			{
				return false;
			}
		}
		return true;
	}//equals
	
	/*
	 * toString
	 * 
	 * Return the string representation of this WMESet.
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
	
	/*
	 * clone
	 * 
	 * This method creates a clone of this WMESet.
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
	
	/*
	 * getSensorKeys
	 * 
	 * Return a Set of strings that contains the names of all
	 * the WMEs in this set.
	 */
	public Set<String> getSensorKeys()
	{
		return sensors.keySet();
	}//getSensorKeys
}//class WMESet
