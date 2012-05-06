package UnitTest;

import java.util.Vector;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.*;

/**
 * PlanTest
 * 
 * This JUnit test checks if Plan is working correctly
 */
public class PlanTest
{
	// BEGIN Test cases --------------------------------------

    //This helper method tests that a given plan contains a given array of
    //actions (in order)

    public void matchPlan(Plan p, Action[] actions)
    {
        //Iterate thorugh each action and compare it to the current action in
        //the plan.  
        Action nextAction = null;
        for(Action a : actions)
        {
            //Verify the next level 0 action from the plan matches expectations
            Route level0Route = p.getRoute(0);
            Action currAction = level0Route.getCurrAction();
            assertTrue(a.equals(currAction));

            //advance the "current action" pointer to the next action as a
            //result of taking this action
            nextAction = p.advance(0);
        }//for

        assertTrue(nextAction == null);
        assertTrue(p.advance(0) == null);
    }
    
	@Test
	public void test_advance1()
    {
        Plan p = new Plan(RouteTest.route1);
        matchPlan(p, RouteTest.route1Actions);
	}

	@Test
	public void test_advance2()
    {
        Plan p = new Plan(RouteTest.route2);
        matchPlan(p, RouteTest.route2Actions);
	}

	@Test
	public void test_advance3()
    {
        Plan p = new Plan(RouteTest.route3);

        Action[] actions = {
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1,
            ActionTest.aEE1, ActionTest.aEE2, ActionTest.aEE3, ActionTest.aEE1};

        matchPlan(p, actions);
	}


	// // BEGIN Test cases --------------------------------------

    /**
     * An easy way to run this test individually from the command line without
     * the JUnit jar file
     */
    public static void main(String[] args)
    {
        PlanTest pt = new PlanTest();
        pt.test_advance1();
        pt.test_advance2();
        pt.test_advance3();
    }
	
}//class PlanTest
