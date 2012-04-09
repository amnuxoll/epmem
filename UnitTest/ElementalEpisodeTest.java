package UnitTest;

import org.junit.* ;
import static org.junit.Assert.* ;
import java.util.*;

import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.WME;

/**
 * class ElementalEpisodeTest
 *
 * junit test for ElementalEpisode
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


}//class EpisodeTest
