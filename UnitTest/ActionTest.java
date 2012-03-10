package UnitTest;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.Action;
import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.SequenceEpisode;

public class ActionTest {
	public static String[] sensors1 = {"attr11", "val11",
								"attr12", "val12",
								"attr13", "val13"};
	
	public static String[] sensors2 = {"attr21", "val21",
								"attr22", "val22",
								"attr23", "val23"};

	public static Action a1;
	public static Action a2;
	public static Action a3;
	
	public static Episode ep1;
	public static Episode ep2;
	public static Episode ep3;
	public static Episode ep4;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		ep1 = new ElementalEpisode(1, ElementalEpisodeTest.makeSensors(sensors1), 1, 1.0);
		ep2 = new ElementalEpisode(2, ElementalEpisodeTest.makeSensors(sensors2), 2, 1.0);
		ep3 = new ElementalEpisode(3, ElementalEpisodeTest.makeSensors(sensors1), 1, 1.0);
		ep4 = new ElementalEpisode(4, ElementalEpisodeTest.makeSensors(sensors2), 2, 1.0);
		
		a1 = new Action(ep1, ep2);
		a2 = new Action(ep2, ep3);
		a3 = new Action(ep3, ep4);
		
	}

	@Test
	public void test_equals()
	{
		assertTrue(a1.equals(a3));
		assertFalse(a1.equals(a2));
	}
	
	@Test
	public void test_getLHS()
	{
		Episode e1 = a1.getLHS();
		assertTrue(e1.equals(ep1));
		Episode e2 = a3.getLHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getRHS()
	{
		Episode e1 = a1.getRHS();
		assertTrue(e1.equals(ep2));
		Episode e2 = a3.getRHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getEpisodes()
	{
		Episode[] eps = a1.getEpisodes();
		assertTrue(eps[0].equals(ep1));
		assertTrue(eps[1].equals(ep2));
	}
}
