package UnitTest;

import java.util.Vector;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.Action;
import Ziggurat.Episode;
import Ziggurat.ElementalEpisode;
import Ziggurat.SequenceEpisode;
import Ziggurat.Sequence;
import Ziggurat.Replacement;

/**
 * ReplacementTest
 * 
 * This JUnit test checks if Action is working correctly with ElementalEpisode.
 */
public class ReplacementTest
{
    //Make some replacements we can try out
    public static Replacement repl1 =
        new Replacement(ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3);
    public static Replacement repl2 =
        new Replacement(ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE2);
    public static Replacement repl3 =
        new Replacement(ActionTest.aEE3, ActionTest.aEE1, ActionTest.aEE2);
    public static Replacement repl4 =
        new Replacement(ActionTest.aEE2, ActionTest.aEE2, ActionTest.aEE1);

    // For quick reference:
    //
    // SequenceTest.sEE1 = { aEE1, aEE2, aEE3, aEE1 }
    //
    // Each of the replacements above can apply to this sequence except repl4.
    //
    // It's important to note that aEE1 and aEE3 are THE SAME, so they can be
    // replaced interchangeably.
        
	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_repl_ctors()
	{
        //Build a replacement identical to repl1 using the other ctor
        Vector<Action> repl1Origs = new Vector<Action>();
        repl1Origs.add(ActionTest.aEE1);
        repl1Origs.add(ActionTest.aEE2);
        Replacement equivRepl = new Replacement(repl1Origs, ActionTest.aEE3);
		assertTrue(equivRepl.equals(repl1));
	}
	
	@Test
	public void test_applyPos()
	{
        assertTrue(repl1.applyPos(SequenceTest.sEE1.clone()) == 0);
        assertTrue(repl2.applyPos(SequenceTest.sEE1) == 1);
        assertTrue(repl3.applyPos(SequenceTest.sEE1) == 2);
        assertTrue(repl4.applyPos(SequenceTest.sEE1) == -1);
	}

	@Test
	public void test_apply()
	{
        Sequence orig = SequenceTest.sEE1.clone();

        //Test repl1
        Sequence seq1 = repl1.apply(orig);
        Sequence result1 = new Sequence();
        result1.add(ActionTest.aEE3);
        result1.add(ActionTest.aEE3);
        result1.add(ActionTest.aEE1);
        assertTrue(seq1.equals(result1));
        
        //Test repl2
        Sequence seq2 = repl2.apply(orig);
        Sequence result2 = new Sequence();
        result2.add(ActionTest.aEE1);
        result2.add(ActionTest.aEE2);
        assertTrue(seq2.equals(result2));
        
        //Test repl3
        Sequence seq3 = repl3.apply(orig);
        Sequence result3 = new Sequence();
        result3.add(ActionTest.aEE1);
        result3.add(ActionTest.aEE2);
        result3.add(ActionTest.aEE2);
        assertTrue(seq3.equals(result3));

        //Make sure the original is untouched
        assertTrue(orig.equals(SequenceTest.sEE1));
        
	}//test_apply


    //END tests

    /**
     * An easy way to run this test individually from the command line without
     * the JUnit jar file
     */
	public static void main(String[] args)
    {
        ReplacementTest rt = new ReplacementTest();
        rt.test_repl_ctors();
        rt.test_applyPos();
        rt.test_apply();
    }
}//class ReplacementTest
