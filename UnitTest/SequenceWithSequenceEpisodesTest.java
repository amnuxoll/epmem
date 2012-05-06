package UnitTest;

import org.junit.*;

import static org.junit.Assert.*;

import Ziggurat.Action;
import Ziggurat.Sequence;

/**
 * SequenceWithSequenceEpisodesTest
 * 
 * This JUnit test case ensures Sequence is working with SequenceEpisodes.
 */
public class SequenceWithSequenceEpisodesTest
{

	// Create some Sequences we can use here, and access in other test files if necessary
	public static Sequence sSE1 = new Sequence(ActionWithSequenceEpisodesTest.makeActionVectorSequenceEpisodes1());
	public static Sequence sSE2 = new Sequence(ActionWithSequenceEpisodesTest.makeActionVectorSequenceEpisodes1());
	public static Sequence sSE3 = new Sequence(ActionWithSequenceEpisodesTest.makeActionVectorSequenceEpisodes2());
	
	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_equals()
	{
		assertTrue(sSE1.equals(sSE2));
		assertFalse(sSE1.equals(sSE3));
	}
	
	@Test
	public void test_getActions()
	{
		Sequence s = new Sequence(sSE1.getActions());
		assertTrue(s.equals(sSE2));
	}
	
	@Test
	public void test_getActionAtIndex()
	{
		Action a = sSE1.getActionAtIndex(1);
		assertTrue(a.equals(ActionWithSequenceEpisodesTest.aSE2));
	}
	
	@Test
	public void test_length()
	{
		assertTrue(sSE1.length() == 4);
	}
	
	@Test
	public void test_clone()
	{
		Sequence s = sSE1.clone();
		assertTrue(s.equals(sSE1));
		assertFalse(s == sSE1);
		// Test each internal action is distinct as well
		Action a1 = s.getActionAtIndex(2);
		Action a2 = sSE1.getActionAtIndex(2);
		assertTrue(a1.equals(a2));
		assertFalse(a1 == a2);
	}
	
	@Test
	public void test_numElementalEpisodes()
	{
		assertTrue(sSE1.numElementalEpisodes() == 16);
		sSE1.add(ActionWithSequenceEpisodesTest.aSE2);
		assertTrue(sSE1.numElementalEpisodes() == 20);
	}
}//class SequenceWithSequenceEpisodesTest
