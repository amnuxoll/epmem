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
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Episode ep1 = new ElementalEpisode(1, EpisodeTest.makeSensors(sensors1), 1, 1.0);
		Episode ep2 = new ElementalEpisode(2, EpisodeTest.makeSensors(sensors2), 2, 1.0);
		Episode ep3 = new ElementalEpisode(3, EpisodeTest.makeSensors(sensors1), 1, 1.0);
		Episode ep4 = new ElementalEpisode(4, EpisodeTest.makeSensors(sensors2), 2, 1.0);
		
		a1 = new Action(ep1, ep2);
		a2 = new Action(ep3, ep4);
	}

	@Test
	public void test_equals()
	{
		assertTrue(a1.equals(a2));
	}
}
