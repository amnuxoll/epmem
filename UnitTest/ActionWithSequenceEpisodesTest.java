package UnitTest;

import java.util.Vector;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.Action;
import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.SequenceEpisode;

/**
 * ActionWithSequenceEpisodesTest
 * 
 * This JUnit test checks if Action is working correctly with SequenceEpisode.
 */
public class ActionWithSequenceEpisodesTest {

	// Create some Actions we can use here, and access in other test files if necessary
	public static Action aSE1 = new Action(SequenceEpisodeTest.seep1.clone(), SequenceEpisodeTest.seep2.clone());
	public static Action aSE2 = new Action(SequenceEpisodeTest.seep1.clone(), SequenceEpisodeTest.seep2.clone());
	public static Action aSE3 = new Action(SequenceEpisodeTest.seep2.clone(), SequenceEpisodeTest.seep3.clone());
	
	// Note: We do not need to make sure the actions overlap correctly for any of these tests
	// These two methods compile 4 actions each into a vector accessible in other test files
	public static Vector<Action> makeActionVectorSequenceEpisodes1() {
		Vector<Action> actions = new Vector<Action>();
		actions.add(aSE1.clone());
		actions.add(aSE2.clone());
		actions.add(aSE3.clone());
		actions.add(aSE1.clone());
		
		return actions;
	}
	public static Vector<Action> makeActionVectorSequenceEpisodes2() {
		Vector<Action> actions = new Vector<Action>();
		actions.add(aSE2.clone());
		actions.add(aSE2.clone());
		actions.add(aSE1.clone());
		actions.add(aSE3.clone());
		
		return actions;
	}

	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_equals()
	{
		assertTrue(aSE1.equals(aSE2));
		assertFalse(aSE1.equals(aSE3));
	}
	
	@Test
	public void test_getLHS()
	{
		Episode e1 = aSE1.getLHS();
		assertTrue(e1.equals(SequenceEpisodeTest.seep1));
		Episode e2 = aSE2.getLHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getRHS()
	{
		Episode e1 = aSE1.getRHS();
		assertTrue(e1.equals(SequenceEpisodeTest.seep2));
		Episode e2 = aSE2.getRHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getEpisodes()
	{
		Episode[] eps = aSE1.getEpisodes();
		assertTrue(eps[0].equals(SequenceEpisodeTest.seep1));
		assertTrue(eps[1].equals(SequenceEpisodeTest.seep2));
	}
	
	@Test
	public void test_clone()
	{
		Action a = aSE1.clone();
		assertTrue(a.equals(aSE1));
		assertFalse(a == aSE1);
	}
}//class ActionWithSequenceEpisodesTest
