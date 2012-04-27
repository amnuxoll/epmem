package UnitTest;

import org.junit.*;

import static org.junit.Assert.*;

import Ziggurat.Sequence;
import Ziggurat.SequenceEpisode;

/**
 * SequenceEpisodeTest
 *
 * This JUnit test case ensures SequenceEpisode is working.
 */
public class SequenceEpisodeTest {

	// Create some SequenceEpisodes we can use here, and access in other test files if necessary
	public static SequenceEpisode seep1 = new SequenceEpisode(SequenceTest.sEE1);
	public static SequenceEpisode seep2 = new SequenceEpisode(SequenceTest.sEE2);
	public static SequenceEpisode seep3 = new SequenceEpisode(SequenceTest.sEE3);
	
	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_equals()
	{
		assertTrue(seep1.equals(seep2));
		assertFalse(seep1.equals(seep3));
	}
	
	@Test
	public void test_getSequence()
	{
		assertTrue(seep1.getSequence().equals(SequenceTest.sEE1));
	}
	
	@Test
	public void test_clone()
	{
		SequenceEpisode s = seep1.clone();
		assertTrue(s.equals(seep1));
		assertFalse(s == seep1);
	}
}//class SequenceEpisodeTest