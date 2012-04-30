package UnitTest;

import java.util.Vector;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.*;

/**
 * RouteTest
 * 
 * This JUnit test checks if Route is working correctly
 */
public class RouteTest
{
    public static Route route1 = Route.newRouteFromSequence(SequenceTest.sEE1);
    public static Route route2 = constructRoute2();
    public static Route route3 = constructRoute3();

    /** these arrays contain the same actions that the routes contain in the
    proper order.  They are used for verifying tests (see below) */
    public static Action[] route1Actions =
    {ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1};
    public static Action[] route2Actions =
    {ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
     ActionTest.aEE2, ActionTest.aEE2, ActionTest.aEE1, ActionTest.aEE3,
     ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1};
    public static Action[] route3Actions =
    {ActionWithSequenceEpisodesTest.aSE2, ActionWithSequenceEpisodesTest.aSE2, ActionWithSequenceEpisodesTest.aSE1, ActionWithSequenceEpisodesTest.aSE3,
     ActionWithSequenceEpisodesTest.aSE1, ActionWithSequenceEpisodesTest.aSE2, ActionWithSequenceEpisodesTest.aSE3, ActionWithSequenceEpisodesTest.aSE1,
     ActionWithSequenceEpisodesTest.aSE1, ActionWithSequenceEpisodesTest.aSE2, ActionWithSequenceEpisodesTest.aSE3, ActionWithSequenceEpisodesTest.aSE1};


    /** route2 is a level 1 route with multiple sequences */
    public static Route constructRoute2()
    {
        Vector<Sequence> vec = new Vector<Sequence>();
        vec.add(SequenceTest.sEE2);
        vec.add(SequenceTest.sEE3);
        vec.add(SequenceTest.sEE1);
        return new Route(vec);
    }

    /** route3 is a level 2 route with multiple sequences */
    public static Route constructRoute3()
    {
        Vector<Sequence> vec = new Vector<Sequence>();
        vec.add(SequenceWithSequenceEpisodesTest.sSE3);
        vec.add(SequenceWithSequenceEpisodesTest.sSE2);
        vec.add(SequenceWithSequenceEpisodesTest.sSE1);
        return new Route(vec);
    }

    
	// BEGIN Test cases --------------------------------------
	
	@Test
	public void test_advance1()
    {
        Route r = route1.clone();
        Action nextAction = null;
        for(Action currAction : route1Actions)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(currAction));
            nextAction = r.advance();
        }

        assertTrue(nextAction == null);
        assertTrue(r.advance() == null);
	}

	@Test
	public void test_advance2()
    {
        Route r = route2.clone();
        Action nextAction = null;
        for(Action currAction : route2Actions)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(currAction));
            nextAction = r.advance();
        }

        assertTrue(nextAction == null);
        assertTrue(r.advance() == null);
	}

	@Test
	public void test_advance3()
    {
        Route r = route3.clone();
        Action nextAction = null;
        for(Action currAction : route3Actions)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(currAction));
            nextAction = r.advance();
        }

        assertTrue(nextAction == null);
        assertTrue(r.advance() == null);
	}

    @Test
    public void test_applyReplacement()
    {
        //Apply the replacement
        Route r = route2.clone();
        r.applyReplacement(ReplacementTest.repl1);

        //The modified route should now iterate through these commands
        Action[] acts =
            {ActionTest.aEE3, ActionTest.aEE3, ActionTest.aEE1,
             ActionTest.aEE2, ActionTest.aEE2, ActionTest.aEE1, ActionTest.aEE3,
             ActionTest.aEE3, ActionTest.aEE3, ActionTest.aEE1};
        for(Action currAction : acts)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(currAction));
            r.advance();
        }
    }//test_applyReplacement

    @Test 
    public void test_episodeCounts()
    {
        Route r = route3.clone();
        assertTrue(r.numElementalEpisodes() == route3Actions.length*4);
        for(int i = 0; i <= r.numElementalEpisodes(); i+=4)
        {
            assertTrue(r.numElementalEpisodes() == r.remainingElementalEpisodes() + i);
            r.advance();
        }
    }

    
    
	// BEGIN Test cases --------------------------------------

    /**
     * An easy way to run this test individually from the command line without
     * the JUnit jar file
     */
    public static void main(String[] args)
    {
        RouteTest rt = new RouteTest();
        rt.test_advance1();
        rt.test_advance2();
        rt.test_advance3();
        rt.test_applyReplacement();
        rt.test_episodeCounts();
    }
	
}//class RouteTest
