package Ziggurat;
 
import java.util.*;

/**
 * <!-- class Plan -->
 * 
 * This class defines a Plan, which is a collection of Route objects (one per
 * level in the hierarchy) that comprise the current intended path to a goal
 * state.  Each route is a component of the route one level above it except the
 * top-most route which is a complete route from a certain point to a goal.  Of
 * course, there is no guarantee that a plan will actually work!
 * 
 * @author Zachary Paul Faltersack
 * 
 */
public class Plan
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** used for {@link #toString} */
    public static NullEnvironment nullEnv = new NullEnvironment();
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** the plan is comprised of these routes */
	protected Vector<Route> routes;
    /** when set, this is an indication that the plan is no longer valid and
	should not be used.  This typically happens when the agent completes a
	plan. */
	protected boolean needsRecalc = true;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /** default ctor creates an empty, invalid plan */
	public Plan() 
    {
		this.routes = new Vector<Route>();
        needsRecalc = true;
	}

    /** this ctor initializes a plan with the given vector of routes.
     *
     * CAVEAT:  minimal checking is performed to verify that initRoutes is valid
     */
	public Plan (Vector<Route> initRoutes) 
    {
        this();
        
        if (initRoutes != null)
        {
            this.routes = initRoutes;
            needsRecalc = false;
        }
	}

    /** this ctor initializes a plan with the given route. If the route is not
     * level 0, then subroutes are constructed.
     *
     */
	public Plan (Route initRoute) 
    {
        //default init for a bad given route
        this();
        if (initRoute == null) return;

        //insert the given route
        int level = initRoute.getLevel();
        this.setRoute(level, initRoute);

        //Now calculate each subroute
        
        //Initialize the route at levels below the given route.  Each route is
        //based on the current sequence in the route at the previous level
        for(int i = level - 1; i >= 0; i--)
        {
            //Get the very first episode in the route (which must be a
            //SequenceEpisode because level+1 can't be zero)
            Route parentRoute = this.getRoute(i+1);
            Action parentAct = parentRoute.getCurrAction();
            SequenceEpisode parentEp = (SequenceEpisode)parentAct.getLHS();

            //parentEp is the sequence that comprises the route one level below
            Route newRoute = new Route(parentEp.getSequence());
            this.setRoute(i, newRoute);
        }//for

        
        needsRecalc = false;
	}//ctor

    /*======================================================================
     * Accessor Methods
     *----------------------------------------------------------------------
     */
    /** @return the value of {@link #needsRecalc} */
    public boolean needsRecalc() { return this.needsRecalc; }

    /** calculates the number of active replacements on this plan*/
    public int numRepls()
    {
        int count = 0;
        for(Route r : this.routes)
        {
            count += r.numRepls();
        }

        return count;
    }//numRepls

    /** @return the route at a given level */
    public Route getRoute(int level)
    {
        while (this.routes.size() <= level)
        {
            this.routes.add(new Route());
        }
        
        return this.routes.elementAt(level);
    }
    /** set the route at a given level */
    public void setRoute(int level, Route r)
    {
        while (this.routes.size() <= level)
        {
            this.routes.add(new Route());
        }
        
        this.routes.set(level, r);
    }
    
    /** return the highest level route in the plan */
    public Route getTopRoute(int level) { return this.routes.elementAt(level); }
    
    /** 
     * Typically you want to use the printing facility in the specific
     * { @link Environment} class instead.
     *
     * @return a String representation of this route
     */
	public String toString() 
    {
        return nullEnv.stringify(this);
	}//toString

    /** @return how many levels are in this plan */
	public int getNumLevels () 
    {
        return this.routes.size();
	}

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /** @return a copy of this Plan */
    public Plan clone()
    {
        Plan result = new Plan();
        for(Route r : this.routes)
        {
            result.routes.add(r.clone());
        }

        result.needsRecalc = this.needsRecalc;

        return result;
    }
    
    /**
     * @return true if the LHS of the current action in the plan matches the
	 * given episode
     */
	public boolean nextStepIsValid (ElementalEpisode latestEpisode) 
    {
        //Ignore this request for degenerate plans
        if ((this.routes.size() == 0) || (this.needsRecalc))
        {
            return false;
        }

        //Retrieve the LHS of the action we are about to execute
        Route level0Route = this.routes.elementAt(0);
        Action currAct = level0Route.getCurrAction();
        ElementalEpisode lhsEp = (ElementalEpisode)currAct.getLHS();

        //Report the comparison to the monitor
        Monitor mon = Ziggurat.getMonitor();
        mon.log("Intended Next Action:\n");
        mon.tab();
        mon.log(currAct);
        mon.log("verifying that it matches the current episode's sensing:");
        mon.tab();
        mon.log(latestEpisode);

        return latestEpisode.equalSensors(lhsEp);
   
	}//nextStepIsValid

    /**
     * advance                 <!-- RECURSIVE -->
     *
     * advances this plan by a single step.  The current sequence and action in
     * each higher level route is adjusted as necessary via recursive calls.
     *
     * @param level   advancement is performed at this level.  If called
     *                externally, this parameter should usually be 0.  Higher
     *                level advancements are reserved for recursive calls.
     *
     * @return the next action to be executed in the route at the given level or
     * null if there is no next action at this level
     */
	public Action advance (int level) 
    {
        //If there is no route at this level, report this
        if (this.routes.size() <= level)
        {
            return null;
        }

        Monitor mon = Ziggurat.getMonitor();
        mon.enter("Plan.advance");

        //Retrieve the route at this level
        Route route = this.routes.elementAt(level);
        mon.log("Updating Level %d Route:", level);
        mon.log(route);

        // Advance the route and return if that was successful
        Action nextAct = route.advance();

        //If the advance was successful we're done
        if (nextAct != null)
        {
            mon.log("Moved to next action (" + route.getCurrActIndex()  + ") in current level " + level + " route.");
            mon.exit("Plan.advance");
            return nextAct;
        }//if

        //If the current route has been exhausted, recursively call advance to
        //get the action from one level up and use it to create a new route at
        //this level.
        Action parentAct = this.advance(level+1);

        //If the parent route is exhausted then this route can't be renewed
        if (parentAct == null)
        {
            //If we are at level 0 then this entire plan is exhausted
            if (level == 0)
            {
                needsRecalc = true;
            }

            mon.log("Plan at level " + (level+1) + " is exhausted.");
            mon.exit("Plan.advance");
            return null;
        }//if
        
        //Create a new route at this level based upon the newly updated parent
        //route
        Route newRoute = new Route(parentAct);

        //reapply replacements from the old route to the new route
        for(Replacement repl : route.getRepls())
        {
            newRoute.applyReplacement(repl);
        }
        
        //Place the newly initalized route in the plan and return its first action
        this.setRoute(level, newRoute);
        mon.log("Extracted new route at level " + level + " from parent.");
        mon.exit("Plan.advance");

        return newRoute.getCurrAction();
	}//advance

    /**
     * applies the given replacement to the appropriate level of this plan 
     */
	public void applyReplacement (Replacement repl) 
    {
        //apply the replacement
        int level = repl.getLevel();
        Route r = getRoute(level);
        r.applyReplacement(repl);
	}//applyReplacement

}//class Plan
