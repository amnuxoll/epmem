import org.junit.* ;
import static org.junit.Assert.* ;
import java.util.*;

/**
 * class EpisodeTest
 *
 * junit test for Episode, ElementalEpisode and SequenceEpisode
 */
public class EpisodeTest 
{
    public static String[] fozzy = {"fozzy", "bear",
                                    "sex", "m",
                                    "age", "16",
                                    "cash", "0.0"};
    public static String[] pi = {"pi", "3.14159"};
    public static Episode ep1;
    public static Episode ep2;
    public static Episode ep3;

    /**
     * creates a sensor hashtable from an array of String.  The string is
     * considered to be a sequence of attribute,value,attribute,value,...
     * These are passed to WMETest#makeWME.
     * 
     */
    public static Hashtable<String,WME> makeSensors(String[] data)
    {
        Hashtable<String,WME> result = new Hashtable<String,WME>();
        
        for(int i = 1; i < data.length; i += 2)
        {
            WME w = WMETest.makeWME(data[i-1], data[i]);
            result.put(data[i-1], w);
        }

        return result;
    }//makeSensors

    
    @BeforeClass
    public static void oneTimeSetUp()
    {
        //Create some sensor readings
        Hashtable<String,WME> sensors1 = makeSensors(fozzy);
        Hashtable<String,WME> sensors2 = makeSensors(pi);
        
        ep1 = new ElementalEpisode(44, sensors1, 1, 1.0);
        ep2 = new ElementalEpisode(45, sensors1);
        ep2 = new ElementalEpisode(45, sensors2, 2, 0.5);
    }

    @Test
    public void test_equals()
    {
        assertTrue(ep1.equals(ep1));
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
