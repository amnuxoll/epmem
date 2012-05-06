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
        for(Action expectedAction : route1Actions)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(expectedAction));
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
        for(Action expectedAction : route2Actions)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(expectedAction));
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
        Action currAction = null;
        for(Action expectedAction : route3Actions)
        {
            currAction = r.getCurrAction();
            assertTrue(currAction.equals(expectedAction));
            nextAction = r.advance();
        }

        //Since this is not a level 0 route, we should be able to squeeze one
        //more "fake" action out whose LHS is the RHS of the last action
        assertTrue(nextAction.equals(
                       new Action(currAction.getRHS(), ElementalEpisode.EMPTY)));
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
        for(Action expectedAction : acts)
        {
            Action a = r.getCurrAction();
            assertTrue(a.equals(expectedAction));
            r.advance();
        }
    }//test_applyReplacement

    @Test 
    public void test_episodeCount1()
    {
        Route r = route1.clone();

        assertTrue(r.numElementalEpisodes() == route1Actions.length);

        //Note that the loop does an extra iteration to verify the "null" return value
        for(int i = 0; i <= r.numElementalEpisodes(); i++)
        {
            assertTrue(r.numElementalEpisodes() == r.remainingElementalEpisodes() + i);
            r.advance();
        }
    }

    @Test 
    public void test_episodeCount2()
    {
        Route r = route2.clone();

        assertTrue(r.numElementalEpisodes() == route2Actions.length);
        //Note that the loop does an extra iteration to verify the "null" return value
        for(int i = 0; i <= r.numElementalEpisodes(); i++)
        {
            assertTrue(r.numElementalEpisodes() == r.remainingElementalEpisodes() + i);
            r.advance();
        }
    }

    @Test 
    public void test_episodeCount3()
    {
        Route r = route3.clone();
        assertTrue(r.numElementalEpisodes() == (route3Actions.length*4 + 4) );
        for(int i = 0; i < r.numElementalEpisodes(); i+=4)
        {
            // Special Case:  double count on the last one to account for RHS
            if (r.numElementalEpisodes() - i == 4) i += 4; 
            
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
        rt.test_episodeCount1();
        rt.test_episodeCount2();
        rt.test_episodeCount3();
    }
	
}//class RouteTest
