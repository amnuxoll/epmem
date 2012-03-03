package UnitTest;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.WMESet;

public class WMESetTest {
	public static String[] sensors1 = {"attr11", "val11",
		"attr12", "val12",
		"attr13", "val13"};

	public static String[] sensors2 = {"attr21", "val21",
		"attr22", "val22",
		"attr23", "val23"};
	
	public static WMESet set1;
	public static WMESet set2;
	public static WMESet set3;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		set1 = new WMESet(EpisodeTest.makeSensors(sensors1));
		set2 = new WMESet(EpisodeTest.makeSensors(sensors1));
		set3 = new WMESet(EpisodeTest.makeSensors(sensors2));
	}

	@Test
	public void test_equals()
	{
		assertTrue(set1.equals(set2));
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
		assertTrue(set2.toString().equals("{attr11:val11,attr12:val12,attr13:val13}"));
		assertTrue(set3.toString().equals("{attr21:val21,attr22:val22,attr23:val23}"));
	}
	
	@Test
	public void test_getAttr()
	{
		assertTrue(set1.getAttr("attr11").toString().equals("attr11:val11"));
		assertTrue(set1.getAttr("attr14") == null);
	}
}