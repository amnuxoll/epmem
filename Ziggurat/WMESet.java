package Ziggurat;

import java.util.*;

public class WMESet {
	protected Hashtable<String,WME> sensors;
	
	public WMESet(Hashtable<String, WME> sensors)
	{
		this.sensors = sensors == null ? new Hashtable<String,WME>() : sensors;
	}
	
	public boolean hasAttr(String attr)
	{
		return sensors.containsKey(attr);
	}
	
	public WME getAttr(String attr)
	{
		if(!this.hasAttr(attr)) return null;
		return this.sensors.get(attr);
	}
	
	public boolean equals(WMESet other)
	{
		if(this.sensors.size() != other.sensors.size()) return false;
		
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
	}
	
	public String toString()
	{
		String rtnVal = "{";
		List<String> keys = new ArrayList<String>(this.sensors.keySet());
		Collections.sort(keys);
		Iterator<String> iter = keys.iterator();
		while(iter.hasNext())
		{
			rtnVal += this.sensors.get(iter.next()) + (iter.hasNext() ? "," : "");
		}
		rtnVal += "}";
		return rtnVal;
	}
}
