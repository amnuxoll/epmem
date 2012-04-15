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
    /** the plan is comprised of these routes */
	protected Vector<Route> routes;
    /** when set, this is an indication that the plan is no longer valid and
	should not be used.  This typically happens when the plan incorrectly
	predicts the outcome of an agent's action. */
	protected boolean needsRecalc;

    /** default ctor creates an empty, invalid plan */
	public Plan() 
    {
		routes = new Vector<Route>();
        needsRecalc = true;
	}

    /** this ctor initializes a plan with the given vector of routes.
     *
     * CAVEAT:  minimal checking is performed to verify that initRoutes is valid
     */
	public Plan (Vector<Route> initRoutes) 
    {
        this();
        
        if ( (initRoutes == null) || (initRoutes.size() == 0)) return;

        this.routes = initRoutes;
        needsRecalc = false;
	}

    /** create an environment-inspecific String representation of this plan */
	public String toString () 
    {
        //%%%TBD
		return "";
	}

    /**
     * @return true if the LHS of the current action in the plan matches the
	 * given episode
     */
	public boolean nextStepIsValid (ElementalEpisode latestEpisode) 
    {
        //Ignore this request for degenerate plans
        if ((this.routes.size() == 0) || (needsRecalc))
        {
            return false;
        }

        //Retrieve the LHS of the action we are about to execute
        Route level0Route = this.routes.elementAt(0);
        Action currAct = level0Route.getCurrAction();
        ElementalEpisode lhsEp = (ElementalEpisode)currAct.getLHS();

        //Report the comparison to the monitor
        Monitor mon = Ziggurat.getMonitor();
        if (mon != null)
        {
            mon.log("Intended Next Action:\n");
            mon.addTempIndent();
            mon.log(currAct);
            mon.log("verifying that it matches the current episode's sensing:");
            mon.addTempIndent();
            mon.log(latestEpisode);
        }

        return latestEpisode.equalSensors(lhsEp);
   
	}//nextStepIsValid

    /** @return how many levels are in this plan */
	public int getNumLevels () 
    {
        return this.routes.size();
	}

    /**
     * update
     *
     * advances this plan by a single step.  The current sequence and action in
     * each routes is adjusted as necessary.
     *
     * @return success code (0) or error code (negative)
     */
	public int update (int level) 
    {
        //%%%TBD
        return -1;
	}//update

    /**
     * applies the given replacement to the appropriate level of this plan
     */
	public void applyReplacement (Replacement repl) 
    {
		//%%%TBD
	}

    /**
     * initRouteFromParent
     *
     * examines the parent route of a given level and based upon its current
     * action creates a route at the given level containing the appropriate
     * sequence.
     *
     * CAVEAT: Caller is responsible for guaranteeing that the parent route
     *         exists!
     *
     * @arg level  the level that needs to be updated.
     * @arg LHS    specifies whether the sequence should be extracted from the
     *             left-hand-side (TRUE) or right-hand-side (FALSE) of the parent
     *             action.  Usually you will want to set this to TRUE.
     */
	protected void initRouteFromParent (int level, boolean LHS) 
    {
		//%%%TBD
	}//initRouteFromParent

    

}//class Plan
