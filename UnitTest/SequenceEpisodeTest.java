<<<<<<< local
package UnitTest;

import org.junit.*;

import static org.junit.Assert.*;

import Ziggurat.Sequence;
import Ziggurat.SequenceEpisode;

public class SequenceEpisodeTest {

	public static SequenceEpisode seep1 = new SequenceEpisode(SequenceTest.sEE1);
	public static SequenceEpisode seep2 = new SequenceEpisode(SequenceTest.sEE2);
	public static SequenceEpisode seep3 = new SequenceEpisode(SequenceTest.sEE3);
	
	@Test
	public void test_equals()
	{
		//assertTrue(seep1.equals(seep2));
		//assertFalse(seep1.equals(seep3));
	}
	
	@Test
	public void test_getSequence()
	{
		//assertTrue(seep1.getSequence().equals(SequenceTest.sEE1));
	}
	
	@Test
	public void test_clone()
	{
		//SequenceEpisode s = seep1.clone();
		//assertTrue(s.equals(seep1));
		//assertFalse(s == seep1);
	}
}
=======
/** A dummy class to fill in until Zach checks in the real deal */
package UnitTest;

import Ziggurat.Episode;

public class SequenceEpisodeTest extends Episode
{
    String name = "foo";
    
    public static SequenceEpisodeTest seep1 = new SequenceEpisodeTest("seep1");
    public static SequenceEpisodeTest seep2 = new SequenceEpisodeTest("seep2");
    public static SequenceEpisodeTest seep3 = new SequenceEpisodeTest("seep3");

    public SequenceEpisodeTest() {}

    public SequenceEpisodeTest(String s)
    {
        name = s;
    }
    
    public SequenceEpisodeTest clone()
    {
        return new SequenceEpisodeTest();
    }

    public String toString() { return name; }
    public boolean equals(Episode other)
    {
        if (! (other instanceof SequenceEpisodeTest))
        {
            return false;
        }

        return ( ((SequenceEpisodeTest)other).name.equals(this.name));
    }


}
>>>>>>> other
