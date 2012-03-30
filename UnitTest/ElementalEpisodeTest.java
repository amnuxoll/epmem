package UnitTest;

import org.junit.* ;
import static org.junit.Assert.* ;
import java.util.*;

import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.WME;

/**
 * class EpisodeTest
 *
 * junit test for Episode, ElementalEpisode and SequenceEpisode
 */
public class ElementalEpisodeTest 
{
    public static ElementalEpisode ep1 = new ElementalEpisode(44, WMESetTest.set1.clone(), 1, 1.0);
    public static ElementalEpisode ep2 = new ElementalEpisode(45, WMESetTest.set1.clone(), 1, 1.0);
    public static ElementalEpisode ep3 = new ElementalEpisode(46, WMESetTest.set1.clone(), 2, 0.5);
    public static ElementalEpisode ep4 = new ElementalEpisode(46, WMESetTest.set2.clone(), 2, 0.5);
    public static ElementalEpisode ep5 = new ElementalEpisode(46, WMESetTest.set2.clone(), 2, 0.5);

    @BeforeClass
    public static void oneTimeSetUp()
    {
        //Create some sensor readings
    	//Hashtable<String,WME> sensors11 = WMESetTest.makeSensors(fozzy);
    	//Hashtable<String,WME> sensors12 = WMESetTest.makeSensors(fozzy);
        //Hashtable<String,WME> sensors21 = WMESetTest.makeSensors(pi);
        
        //ep1 = new ElementalEpisode(44, sensors11, 1, 1.0);
        //ep2 = new ElementalEpisode(45, sensors12, 1, 1.0);
        //ep3 = new ElementalEpisode(46, sensors12, 2, 0.5);
        //ep4 = new ElementalEpisode(46, sensors21, 2, 0.5);
        //ep5 = new ElementalEpisode(46, sensors21, 2, 0.5);
    }

    @Test
    public void test_equals()
    {
        assertTrue(ep1.equals(ep2));
        assertTrue(ep4.equals(ep5));
        assertFalse(ep1.equals(ep3));
    }
    
    @Test
    public void test_equalSenses()
    {
    	assertTrue(ep1.equalSensors(ep2));
    	assertFalse(ep1.equalSensors(ep4));
    }
    
    @Test
    public void test_containsAttr()
    {
    	assertTrue(ep1.containsAttr("attr11"));
    }
    
    @Test
    public void test_clone()
    {
    	ElementalEpisode ee = ep1.clone();
    	assertTrue(ee.equals(ep1));
    	assertFalse(ee == ep1);
    }

    // /** ctor initializes instance variables */
	// public ElementalEpisode(int id, Hashtable<String,WME> sensors, int cmd) 
    // {
	// 	this.id      = id;
    //     this.sensors = sensors == null ? new Hashtable<String,WME>() : sensors;
    //     this.cmd     = cmd;
    //     this.utility  = utility;
	// }//ctor

    // /** partial ctor leaves cmd and utility set to zero */
	// public ElementalEpisode(int id, Hashtable<String,WME> sensors) 
    // {
	// 	this.id      = id;
    //     this.sensors = sensors == null ? new Hashtable<String,WME>() : sensors;
    //     this.cmd     = 0;
    //     this.utility  = 0.0;
	// }

	// /** episodes are equal if they contain the same knowlege */
	// public boolean equals(Episode other) 
    // {
    //     //must both be ElementalEpisode
    //     if (! (other instanceof ElementalEpisode)) return false;

    //     //verify that all relevant instance variables match
    //     ElementalEpisode ee = (ElementalEpisode)other;
    //     return (equalSensors(ee) && this.cmd == ee.cmd);
	// }//equals

	// /** returns true if the sensors of two given elemental episodes match */
	// public boolean equalSensors(ElementalEpisode other) 
    // {
	// 	return this.sensors.equals(other.sensors);
	// }

	// /**
    //  * @return a string containing all the WMEs in the episode + command.
    //  * Typically you want to use the printing facility in the current
    //  * Environment# class instead.
    //  */
	// public String toString() 
    // {
    //     return sensors.toString() + cmd;
	// }

    // /**
    //  * @return a string reprsentation of this episode's sensors
    //  */
    // public String sensorsToString()
    // {
    //     return sensors.toString();
    // }

	// /** returns the WME */
	// public boolean containsAttr(String attr) 
    // {
    //     return sensors.containsKey(attr);
	// }

    // @Test
    // public void test_returnEuro() 
    // {
    //     System.out.println("Test if pricePerMonth returns Euro...") ;
    //     Subscription S = new Subscription(200,2) ;
    //     assertTrue(S.pricePerMonth() == 1.0) ;
    // }

    // @Test
    // public void test_roundUp() 
    // {
    //     System.out.println("Test if pricePerMonth rounds up correctly...") ;
    //     Subscription S = new Subscription(200,3) ;
    //     assertTrue(S.pricePerMonth() == 0.67) ;
    // }

}//class EpisodeTest
