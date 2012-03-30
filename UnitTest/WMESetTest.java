package UnitTest;

import java.util.Hashtable;
import java.util.Set;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.WME;
import Ziggurat.WMESet;

public class WMESetTest {
	public static String[] sensors1 = {"attr11", "val11",
		"attr12", "val12",
		"attr13", "val13"};

	public static String[] sensors2 = {"attr21", "val21",
		"attr22", "val22",
		"attr23", "val23"};
	
	public static String[] sensors3 = {"attr31", "val31",
		"attr32", "val32",
		"attr33", "val33"};
	
	public static WMESet set1 = new WMESet(makeSensors(sensors1));
	public static WMESet set2 = new WMESet(makeSensors(sensors2));
	public static WMESet set3 = new WMESet(makeSensors(sensors3));
	
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
	public static void setUpBeforeClass() throws Exception {
		//set1 = new WMESet(makeSensors(sensors1));
		//set2 = new WMESet(makeSensors(sensors1));
		//set3 = new WMESet(makeSensors(sensors2));
	}

	@Test
	public void test_equals()
	{
		WMESet temp = set1.clone();
		assertFalse(temp == set1);
		assertTrue(set1.equals(temp));
		assertFalse(set1.equals(set3));
	}
	
	@Test
	public void test_hasAttr() 
	{
		assertTrue(set1.hasAttr("attr11"));
		assertTrue(set1.hasAttr("attr12"));
		assertTrue(set1.hasAttr("attr13"));
		assertFalse(set1.hasAttr("attr14"));
	}
	
	@Test
	public void test_toString()
	{
		assertTrue(set1.toString().equals("{attr11:val11,attr12:val12,attr13:val13}"));
		assertTrue(set2.toString().equals("{attr21:val21,attr22:val22,attr23:val23}"));
		assertTrue(set3.toString().equals("{attr31:val31,attr32:val32,attr33:val33}"));
	}
	
	@Test
	public void test_getAttr()
	{
		assertTrue(set1.getAttr("attr11").toString().equals("attr11:val11"));
		assertTrue(set1.getAttr("attr14") == null);
	}
	
	@Test
	public void test_clone()
	{
		WMESet newSet = set1.clone();
		assertTrue(set1.equals(newSet));
		Set<String> sense1Keys = set1.getSensorKeys();
		for(String s : sense1Keys)
		{
			assertTrue(newSet.hasAttr(s) && newSet.getAttr(s).equals(set1.getAttr(s)));
			assertFalse(newSet.getAttr(s) == set1.getAttr(s));
		}
	}
}