package UnitTest;

import org.junit.*;

import static org.junit.Assert.*;

import Ziggurat.Action;
import Ziggurat.Sequence;

/**
 * SequenceTest
 * 
 * This JUnit test case ensures Sequence is working with ElementalEpisode.
 */
public class SequenceTest {

	// Create some Sequences we can use here, and access in other test files if necessary
	public static Sequence sEE1 = new Sequence(ActionTest.makeActionVectorElementalEpisodes1());
	public static Sequence sEE2 = new Sequence(ActionTest.makeActionVectorElementalEpisodes1());
	public static Sequence sEE3 = new Sequence(ActionTest.makeActionVectorElementalEpisodes2());

	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_equals()
	{
		assertTrue(sEE1.equals(sEE2));
		assertFalse(sEE1.equals(sEE3));
	}
	
	@Test
	public void test_getActions()
	{
		Sequence s = new Sequence(sEE1.getActions());
		assertTrue(s.equals(sEE2));
	}
	
	@Test
	public void test_getActionAtIndex()
	{
		Action a = sEE1.getActionAtIndex(1);
		assertTrue(a.equals(ActionTest.aEE2));
	}
	
	@Test
	public void test_length()
	{
		assertTrue(sEE1.length() == 4);
	}
	
	@Test
	public void test_clone()
	{
		Sequence s = sEE1.clone();
		assertTrue(s.equals(sEE1));
		assertFalse(s == sEE1);
		// Test each internal action is distinct as well
		Action a1 = s.getActionAtIndex(2);
		Action a2 = sEE1.getActionAtIndex(2);
		assertTrue(a1.equals(a2));
		assertFalse(a1 == a2);
	}
	
	@Test
	public void test_numElementalEpisodes()
	{
		assertTrue(sEE1.numElementalEpisodes() == 4);
		sEE1.add(ActionTest.aEE2);
		assertTrue(sEE1.numElementalEpisodes() == 5);
	}
}//class SequenceTest
