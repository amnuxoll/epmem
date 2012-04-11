package UnitTest;

import java.util.Vector;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.Action;
import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.SequenceEpisode;

/**
 * ActionTest
 * 
 * This JUnit test checks if Action is working correctly with ElementalEpisode.
 */
public class ActionTest {

	// Create some Actions we can use here, and access in other test files if necessary
	public static Action aEE1 = new Action(ElementalEpisodeTest.ep1.clone(), ElementalEpisodeTest.ep2.clone());
	public static Action aEE2 = new Action(ElementalEpisodeTest.ep3.clone(), ElementalEpisodeTest.ep4.clone());
	public static Action aEE3 = new Action(ElementalEpisodeTest.ep1.clone(), ElementalEpisodeTest.ep2.clone());

	// Note: We do not need to make sure the actions overlap correctly for any of these tests
	// These two methods compile 4 actions each into a vector accessible in other test files
	public static Vector<Action> makeActionVectorElementalEpisodes1() {
		Vector<Action> actions = new Vector<Action>();
		actions.add(aEE1.clone());
		actions.add(aEE2.clone());
		actions.add(aEE3.clone());
		actions.add(aEE1.clone());
		
		return actions;
	}
	public static Vector<Action> makeActionVectorElementalEpisodes2() {
		Vector<Action> actions = new Vector<Action>();
		actions.add(aEE2.clone());
		actions.add(aEE2.clone());
		actions.add(aEE1.clone());
		actions.add(aEE3.clone());
		
		return actions;
	}

	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_equals()
	{
		assertTrue(aEE1.equals(aEE3));
		assertFalse(aEE1.equals(aEE2));
	}
	
	@Test
	public void test_getLHS()
	{
		Episode e1 = aEE1.getLHS();
		assertTrue(e1.equals(ElementalEpisodeTest.ep1));
		Episode e2 = aEE3.getLHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getRHS()
	{
		Episode e1 = aEE1.getRHS();
		assertTrue(e1.equals(ElementalEpisodeTest.ep2));
		Episode e2 = aEE3.getRHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getEpisodes()
	{
		Episode[] eps = aEE1.getEpisodes();
		assertTrue(eps[0].equals(ElementalEpisodeTest.ep1));
		assertTrue(eps[1].equals(ElementalEpisodeTest.ep2));
	}
	
	@Test
	public void test_clone()
	{
		Action a = aEE1.clone();
		assertTrue(a.equals(aEE1));
		assertFalse(a == aEE1);
	}
}//class ActionTest
