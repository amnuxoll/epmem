package UnitTest;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.Action;
import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.SequenceEpisode;

public class ActionTest {

	public static Action a1 = new Action(ElementalEpisodeTest.ep1.clone(), ElementalEpisodeTest.ep2.clone());
	public static Action a2 = new Action(ElementalEpisodeTest.ep3.clone(), ElementalEpisodeTest.ep4.clone());
	public static Action a3 = new Action(ElementalEpisodeTest.ep1.clone(), ElementalEpisodeTest.ep2.clone());
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//a1 = new Action(ep1, ep2);
		//a2 = new Action(ep2, ep3);
		//a3 = new Action(ep3, ep4);
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
		assertTrue(e1.equals(ElementalEpisodeTest.ep1));
		Episode e2 = a3.getLHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getRHS()
	{
		Episode e1 = a1.getRHS();
		assertTrue(e1.equals(ElementalEpisodeTest.ep2));
		Episode e2 = a3.getRHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getEpisodes()
	{
		Episode[] eps = a1.getEpisodes();
		assertTrue(eps[0].equals(ElementalEpisodeTest.ep1));
		assertTrue(eps[1].equals(ElementalEpisodeTest.ep2));
	}
}
