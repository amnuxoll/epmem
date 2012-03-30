package UnitTest;

import java.util.Vector;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.Action;
import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.SequenceEpisode;

public class ActionTest {

	public static Action aEE1 = new Action(ElementalEpisodeTest.ep1.clone(), ElementalEpisodeTest.ep2.clone());
	public static Action aEE2 = new Action(ElementalEpisodeTest.ep3.clone(), ElementalEpisodeTest.ep4.clone());
	public static Action aEE3 = new Action(ElementalEpisodeTest.ep1.clone(), ElementalEpisodeTest.ep2.clone());

	public static Action aSE1 = new Action(SequenceEpisodeTest.seep1.clone(), SequenceEpisodeTest.seep2.clone());
	public static Action aSE2 = new Action(SequenceEpisodeTest.seep1.clone(), SequenceEpisodeTest.seep2.clone());
	public static Action aSE3 = new Action(SequenceEpisodeTest.seep2.clone(), SequenceEpisodeTest.seep3.clone());
	
	
	// Note: We do not need to make sure the actions overlap correctly for any of these tests
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
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		//a1 = new Action(ep1, ep2);
		//a2 = new Action(ep2, ep3);
		//a3 = new Action(ep3, ep4);
	}

	@Test
	public void test_equals()
	{
		// Actions with ElementalEpisodes
		assertTrue(aEE1.equals(aEE3));
		assertFalse(aEE1.equals(aEE2));
		
		// Actions with SequenceEpisodes
		assertTrue(aSE1.equals(aSE2));
		assertFalse(aSE1.equals(aSE3));
	}
	
	@Test
	public void test_getLHS()
	{
		// ElementalEpisodes
		Episode e1 = aEE1.getLHS();
		assertTrue(e1.equals(ElementalEpisodeTest.ep1));
		Episode e2 = aEE3.getLHS();
		assertTrue(e1.equals(e2));
		
		// SequenceEpisodes
		e1 = aSE1.getLHS();
		assertTrue(e1.equals(SequenceEpisodeTest.seep1));
		e2 = aSE2.getLHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getRHS()
	{
		// ElementalEpisodes
		Episode e1 = aEE1.getRHS();
		assertTrue(e1.equals(ElementalEpisodeTest.ep2));
		Episode e2 = aEE3.getRHS();
		assertTrue(e1.equals(e2));
		
		// SequenceEpisodes
		e1 = aSE1.getRHS();
		assertTrue(e1.equals(SequenceEpisodeTest.seep2));
		e2 = aSE2.getRHS();
		assertTrue(e1.equals(e2));
	}
	
	@Test
	public void test_getEpisodes()
	{
		// ElementalEpisodes
		Episode[] eps = aEE1.getEpisodes();
		assertTrue(eps[0].equals(ElementalEpisodeTest.ep1));
		assertTrue(eps[1].equals(ElementalEpisodeTest.ep2));
		
		eps = aSE1.getEpisodes();
		assertTrue(eps[0].equals(SequenceEpisodeTest.seep1));
		assertTrue(eps[1].equals(SequenceEpisodeTest.seep2));
	}
	
	@Test
	public void test_clone()
	{
		Action a = aEE1.clone();
		assertTrue(a.equals(aEE1));
		assertFalse(a == aEE1);
		
		a = aSE1.clone();
		assertTrue(a.equals(aSE1));
		assertFalse(a == aSE1);
	}
}
