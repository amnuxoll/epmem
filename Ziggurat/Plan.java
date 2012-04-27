package Ziggurat;
 
import java.util.*;

/**
 * Plan
 * 
 * This class defines a Plan, which is a collection of Route objects (one per
 * level in the hierarchy) that comprise the current intended path to a goal
 * state.  Each route is a component of the route one level above it. 
 * 
 * @author Zachary Paul Faltersack
 * 
 */
public class Plan
{
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
        assert(initRoutes != null);
        this.routes = initRoutes;
        needsRecalc = false;
	}

    /*======================================================================
     * Accessor Methods
     *----------------------------------------------------------------------
     */
    /** @return @link{#needsRecalc} */
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
    
    /** create an environment-inspecific String representation of this plan
     * which is just a list of routes separates by newlines */
	public String toString () 
    {
        String result = "";
        for(int i = this.routes.size() - 1; i >= 0; i--)
        {
            result += i + ": ";
            Route r = this.getRoute(i);
            result += r.toString();
            if (i > 0) result += "\n";
        }
		return result;
	}//toString

    /** @return how many levels are in this plan */
	public int getNumLevels () 
    {
        return this.routes.size();
	}

    /*======================================================================
     * Public Methods
     *----------------------------------------------------------------------
     */
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
     * @param level   advancement is performed at this level
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
        mon.tab();
        mon.log(route);

        // Advance the route and return if that was successful
        Action nextAct = route.advance();

        //If the advance was successful we're done
        if (nextAct != null)
        {
            mon.exit("Plan.advance");
            return nextAct;
        }//if

        //Since the current route has been exhausted, recursively call advance to
        //get the action from one level up.  This will be used to create a new
        //route at htis level.
        Action parentAct = this.advance(level+1);

        //If the parent route is unavailable or also exhausted this route can
        //not be advanced.
        if (parentAct == null)
        {
            //If we can't advance at level 0 then this plan is no longer valid
            if (level == 0)
            {
                needsRecalc = true;
            }
            
            mon.exit("Plan.advance");
            return null;
        }

        //Create a new route at this level based upon the newly updated parent
        //route
        Route newRoute = Route.newRouteFromParentAction(parentAct);

        //reapply replacements from the old route to the new route
        for(Replacement repl : route.getRepls())
        {
            newRoute.applyReplacement(repl);
        }
        
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
