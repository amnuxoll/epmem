package Ziggurat;
 
import java.util.*;

/**
 * class Ziggurat
 *
 * This state contains the main learning algorithm for Ziggurat.  For an
 * overview of the algorithm please review:
 *
 * <ul> Zachary Faltersack, Brian Burns, Andrew Nuxoll and Tanya
 * L. Crenshaw. Ziggurat: Steps Toward a General Episodic Memory. <i>AAAI Fall
 * Symposium Series: Advances in Cognitive Systems</i>, 2011.
 *
 * <p>If you are new to this source code start by looking at the classes for
 * major entities: {@link Action}, {@link Episode} and {@link Sequence}.  In
 * this class, the following methods are fundamental: {@link #tick}, {@link
 * #update} and {@link #findRoute}.
 *
 */
public class Ziggurat
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** maximum number of levels in the Ziggurat
     *  (In practice, this may not be necessary as higher levels become
     *   increasingly harder to achieve. ) */
    public static final int MAX_LEVEL_DEPTH = 4;
    /** initial self confidence value */
    public static final double INIT_SELF_CONFIDENCE = 0.5;
    /** maximum confidence value */
    public static final double MAX_CONFIDENCE = 1.0;
    /** minimum confidence value */
    public static final double MIN_CONFIDENCE = 0.0;
    /** use this number down as a way to speed up findRoute().  Set to zero for
        unlimited route searching. */
    private static final int MAX_ROUTE_CANDS = 0;
    /** maximum number of replacements that can be applied at any one time */
    private static final int MAX_REPLS = 1;

    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** All episodes learned so far */
    private Vector<Vector<Episode>> epmems = new Vector<Vector<Episode>>();
    /** All actions learned so far */
    private Vector<Vector<Action>> actions = new Vector<Vector<Action>>();
    /** All sequences learned so far */
    private Vector<Vector<Sequence>> seqs = new Vector<Vector<Sequence>>();
    /** The agent's current plan for reaching a goal */
    private Plan currPlan = null;
    /** All replacement rules that the agent has tried */
    private Vector<Vector<Replacement>> repls = new Vector<Vector<Replacement>>();
    /** high confidence means the agent will try new things.  Value range [0..1] */
    private double selfConfidence = INIT_SELF_CONFIDENCE;
    /** the current environment provides the agent with a limited amount of
        information about itself */
    private Environment env = null;
    /** the monitor that is currently logging events in Zigg.  I'm making this
     * static so that other entities can log events to the monitor.  Maybe a
     * mistake?  But at the moment I can't think of a better approach. */
    private static Monitor mon = null;
    /** this is the highest level in the hierarchy that contains data.  This is
     * used by the findInterimStart methods */
    private int lastUpdateLevel = 0;
    /** this vector contains all @link{DecisionElement}s that have recently been
     * used to make a decision.  When the outcome of decision(s) is known these
     * active elements' utlities are adjusted based upon that outcome.
     */
    private Vector<DecisionElement> activeDecEls = new Vector<DecisionElement>();
    /** count how many goals we've reached so far */
    private int goalCount = 0;
    /** count how many steps we've taken since the last goal */
    private int stepsSoFar = 0;
    /** for all your random number geneation needs! */
    private Random randGen = new Random();

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /**
     * Ziggurat ctor
     *
     * initializes instance variables as necessary
     *
     * @param env   the Environment that the agent will be acting in.
     *              
     */
    public Ziggurat(Environment env)
    {
        //Init level 0 to an empty list
        this.epmems.add(new Vector<Episode>());
        this.actions.add(new Vector<Action>());
        this.seqs.add(new Vector<Sequence>());

        //record input parameters
        this.env = env;
        if (this.mon == null)
        {
            this.mon = new Monitor(env);
        }
        
    }//ctor


    /*======================================================================
     * Public Methods
     *----------------------------------------------------------------------
     */
    /**
     * tick
     *
     * is called by the Environment at each time step so that Zigg can learn and
     * decide it's next action.
     *
     * @param sensors   the agent's sensor readings at this time step
     *
     * @return which action to take: a number in the range 0..N-1 where N is
     *         #numCommands
     */
    public int tick(WMESet sensors)
    {
        this.mon.enter("tick");
        
        // Create new Episode and update the hierarchy with it
        ElementalEpisode ep = new ElementalEpisode(sensors);
        this.epmems.elementAt(0).add(ep);
        update(0);

        
        // If we receive a reward, update the memory to reflect this
        double reward = this.env.currReward();
        if(reward > 0.0)
        {
            this.mon.reward(reward);
       
            //If a a plan is in place, reward the agent and any outstanding replacements
            if (this.currPlan != null)
            {
                rewardDecEls();
                rewardAgent();
            }
       
            //The current, presumably successful, plan is no longer needed
            this.currPlan = null;

            //Report this success
            (this.goalCount)++;
            this.mon.log("Goal %d found after %d steps.",
                         this.goalCount, this.stepsSoFar);
            this.stepsSoFar = 0;
            
        }//if

        //Select the agent's next action
        int cmd = chooseCommand();
        ep.setCommand(cmd);


        //Log the resulting episode
        this.mon.log("Completed episode #" + (epmems.size()-1) + ":");
        this.mon.log(ep);

        //Return the result
        this.mon.exit("tick");
        return cmd;

    }//tick

    /** accessor for the monitor */
    public static Monitor getMonitor() { return Ziggurat.mon; }

    /*======================================================================
     * Private Methods
     *----------------------------------------------------------------------
     */
    /**
     * update                    <!-- RECURSIVE -->
     *
     * this method is used to update the entire Ziggurat hierarchy once a new
     * level 0 episode has been added from the agent's sensing.
     *
     * @param level  specifies the level that is being updated.  Recursive calls
     *               are made as necessary for higher levels.
     *
     * @return a success code (0) or error code (negative)
     */
    private int update(int level)
    {
        this.mon.enter("update(level " + level + ")");

        // Ensure that the level is within the accepted range for the vectors
        if(level < 0 || level >= MAX_LEVEL_DEPTH)
        {
            this.mon.log("Exiting update(): level %d out of range", level);
            return -3;
        }

        //Log the current state of the hierarchy at this level
        this.mon.log("Level %d Episodes >>>>>>>>>>>>>>>>>>>>>>>>>>>", level);
        this.mon.log(epmems.elementAt(level));
        this.mon.log("Level %d Actions >>>>>>>>>>>>>>>>>>>>>>>>>>>", level);
        this.mon.log(actions.elementAt(level));
        this.mon.log("Level %d Sequences >>>>>>>>>>>>>>>>>>>>>>>>>>>", level);
        this.mon.log(seqs.elementAt(level));
        this.mon.log("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< End of Level %i Data", level);

        // Create pointers to the two associated vectors we'll be working with
        Vector<Episode> episodeList = this.epmems.elementAt(level);
        Vector<Action> actionList = this.actions.elementAt(level);
        Vector<Sequence> sequenceList = this.seqs.elementAt(level);

        // You need a minimum of two episodes to make an action
        if(episodeList.size() <= 1)
        {
            this.mon.log("\tExiting update(): insufficient episodes (%d) at level %d",
                         episodeList.size(), level);
            return -1;
        }

        // Create a candidate action from this current episode.  We won't add it
        // to the rule list if we later discover that an identical action
        // already exists.
        Episode epLeft   = episodeList.elementAt(episodeList.size() - 2);
        Episode epRight  = episodeList.elementAt(episodeList.size() - 1);
        Action newAction = new Action(epLeft, epRight);
        this.mon.log(newAction);
        

        //Iterate over every action in the list and compare it to our new
        //candidate action.  If the candidate is unique, it'll be added to the
        //action list.  If it's a partial match (same LHS , different RHS) it's
        //added to the action list but also recorded as a cousin of the
        //partially matching action.  If the candidate completely matches an
        //existing action, it'll be discarded and the existing action's
        //frequency will be updated
        boolean matchComplete = false;
        boolean addNewAction = true;
        Action updateExistingAction = null;
        for(Action curr : actionList)
        {
            //If both match, then we can reuse the matching action 
            if (curr.equals(newAction))
            {
                matchComplete = true;
                addNewAction = false;
                updateExistingAction = curr;
                break;
            }

            //If just the left-hand-sides match, then we have a cousin
            if (curr.getLHS().equals(newAction.getLHS()))
            {
                //Iterate over all the cousins to see if there is an exact match
                Vector<Action> cousinsList = curr.getCousins();
                for(Action currCousin : cousinsList)
                {
                    //If both match, then we can reuse the matching action 
                    if (currCousin.equals(newAction))
                    {
                        matchComplete = true;
                        addNewAction = false;
                        updateExistingAction = curr;
                        break;
                    }
                }//for

                //If no exact match was found, we want to add this new action
                //but also include it in the cousins list
                if (updateExistingAction == null)
                {
                    cousinsList.add(newAction);
                    newAction.setCousins(cousinsList);
                    matchComplete = true;
                    addNewAction = true;
                }

                break;
            }//if
        }// for
            
        //Add the new action
        if(addNewAction)
        {
            this.mon.log("Adding new action to level %i action list: ", level);
            this.mon.log(newAction);
            actionList.add(newAction);

            //Add itself to its own cousins list to init that list
            Vector<Action> cousins = new Vector<Action>();
            cousins.add(newAction);
            newAction.setCousins(cousins);
            
            // set this flag so that we recursively update the next level
            // with this action
            updateExistingAction = newAction;
        }

        //Sanity Check
        if(updateExistingAction == null)
        {
            this.mon.log("ERROR:  I'm insane!");
            return -5;
        }
        
        //Bump the frequency for this new (or re-used) action
        updateExistingAction.incrementFreq();
        
        //Log that an update was completed at this level.  This is used by
        //findInterimStart()
        this.lastUpdateLevel = level;

        // add most recently seen action to current sequence
        Sequence currSequence = sequenceList.elementAt(sequenceList.size() - 1);
        currSequence.add(updateExistingAction);
        this.mon.log("Adding action #%d: ", sequenceList.size() - 1);
        this.mon.tab();
        this.mon.log(updateExistingAction);
        this.mon.log(" to current sequence:");
        this.mon.tab();
        this.mon.log(currSequence);

        // if the action we just added is indeterminate then end the current
        // sequence and start a new one
        if (updateExistingAction.isIndeterminate())
        {
            
            // if the sequence we just completed already exists replace it with
            // a reference to the one it is duplicating
            Sequence duplicate = currSequence.findEquivalent(sequenceList);
            if (duplicate != null)
            {
                sequenceList.setElementAt(duplicate, sequenceList.size() - 1);

                //set currSequence to the duplicate so it gets added
                //to the episode list in the code below
                currSequence = duplicate;
            }
                
            // now we can create a new, empty sequence at this level so it's
            // ready for future actions
            sequenceList.add(new Sequence());

            // this newly completed sequence becomes the next episode in the
            // next level's episodic memory unless the required level doesn't
            // exist
            if (level + 1 < MAX_LEVEL_DEPTH)
            {
                this.mon.log("Creating a new level %i episode with sequence: ", level + 1);
                this.mon.tab();
                this.mon.log(currSequence);

                Vector<Episode> parentEpList = epmems.elementAt(level + 1);
                parentEpList.add(new SequenceEpisode(currSequence));
                    
                // this sequence has become an episode in our next level so make
                // a recursive call to update.
                update(level + 1);
            }

        }//if (ended current sequence)

        this.mon.exit("update(level " + level + ")");
        return 0; //success!

    }//update

   /**
    * rewardAgent
    *
    * This method increases the agent's overall confidence so that its distance
    * from 1.0 is half of what it was.  Math:
    *                conf = conf + (1.0 - conf) / 2
    */
    void rewardAgent()
    {
        this.mon.log("Overall confidence increased from %g", this.selfConfidence);
   
        this.selfConfidence += (1.0 - this.selfConfidence) / 2.0;
   
        this.mon.log("to %g", this.selfConfidence);
    }//rewardAgent   

    /**
     * penalizeAgent
     *
     * This method halves the agent's overall confidence.
     */
    void penalizeAgent()
    {
        this.mon.log("Overall confidence decreased from %g", this.selfConfidence);
   
        this.selfConfidence /= 2.0;
   
        this.mon.log("to %g", this.selfConfidence);
    }//penalizeAgent   

    /**
     * rewardDecEls
     *
     * increases the confidence of all active decision elements.
     *
     * @see DecisionElement.reward
     *
     */
    void rewardDecEls()
    {
        for(DecisionElement de : this.activeDecEls)
        {
            de.reward();
        }

        //Reset the active replacements list
        activeDecEls.clear();
   
    }//rewardDecEls

    /**
     * penalizeDecEls
     *
     * decreases the confidences of all active decision elements
     *
     * @see DecisionElement.penalize
     *
     */
    void penalizeDecEls()
    {
        for(DecisionElement de : this.activeDecEls)
        {
            de.penalize();
        }

        //Reset the active replacements list
        activeDecEls.clear();
   
    }//penalizeDecEls

    /**
     * initPlan
     *
     * this method creates a plan for reaching the goal state from the starting
     * state.  A plan is a vector of routes (one per level)
     *
     * @return a pointer to the plan or null if no plan was found
     */
    Plan initPlan()
    {
        this.mon.enter("initPlan");
       
        //Try to figure out where I am.  I can't make plan without this.
        Route seedRoute = findInterimStart();
        if (seedRoute == null)
        {
            //Try a partial match
            seedRoute = findInterimStartPartialMatch();
            if (seedRoute == null)
            {
                this.mon.exit("initPlan");
                return null;        // I give up
            }
        }//if

        //Figure out what level the route is at
        int level = seedRoute.getLevel();
   
        //Try to initialize the route at the same level as the start sequence
        Route currRoute = findRoute(seedRoute);
        
        //Give up if no route can be found
        if (currRoute == null)
        {
            this.mon.log("findRoute failed");
            this.mon.exit("initPlan");
            
            return null;
        }//if

        //Initialize an incomplete plan using the new route
        Plan resultPlan = new Plan();
        resultPlan.setRoute(level, currRoute);

        //report
        this.mon.log("Success: found route to goal at level: %d:", level);
        this.mon.tab();
        this.mon.log(currRoute);
       
        //Initialize the route at levels below the level of the currRoute.  Each
        //route is based on the current sequence in the route at the previous
        //level
        /*%%%very important that this code is correct.  I'm not 100% sure that
          we are initializing with the correct episode here!-:AMN: */
        for(int i = level - 1; i >= 0; i--)
        {
            //Get the very first episode in the route (which must be a
            //SequenceEpisode because level+1 can't be zero)
            Route parentRoute = resultPlan.getRoute(i+1);
            Action parentAct = parentRoute.getCurrAction();
            SequenceEpisode parentEp = (SequenceEpisode)parentAct.getLHS();

            //parentEp is the sequence that comprises the route one level below
            Route newRoute = Route.newRouteFromSequence(parentEp.getSequence());
            resultPlan.setRoute(i, newRoute);
        }//for

        this.mon.exit("initPlan");
        return resultPlan;
    }// initPlan

    /**
     * findShortestRoute
     *
     * searches a given Vector<Route> to find the one that has the shortest
     * number of elemental episodes
     *
     * @param searchMe  the vector to search
     * @param startPos  start searching at this index
     *
     * @return index of shortest route (or -1 if given an empty vector)
     */
    private int findShortestRoute(Vector<Route> searchMe, int startPos)
    {
        if (searchMe.size() == 0) return -1;
        
        Route cand = searchMe.elementAt(startPos);
        int candLen = cand.numElementalEpisodes();
        int candPos = startPos;
        for(int i = startPos; i < searchMe.size(); i++)
        {
            Route possiblyShorter = searchMe.elementAt(i);
            int psLen = possiblyShorter.numElementalEpisodes();

            //If a shorter one is found, update cand, candLen and candPos
            if (psLen < candLen)
            {
                cand = possiblyShorter;
                candLen = psLen;
                candPos = i;
            }//if
        }//for

        return candPos;

        
    }//findShortestRoute

    /** this version of findShortestRoute doesn't require a start position */
    private int findShortestRoute(Vector<Route> searchMe)
    {
        return findShortestRoute(searchMe, 0);
    }

    /**
     * findContainingEpisode
     *
     * finds an episode in a given vector that contains a given sequence
     *
     * @param seq  the sequence to search with
     * @param vec  the vector to search
     * @return the Episode that contains the given sequence (or null if not
     *         found)
     */
    private Episode findContainingEpisode(Sequence seq, Vector<Episode> vec)
    {
        for(Episode ep : vec)
        {
            if (! (ep instanceof SequenceEpisode)) break;

            SequenceEpisode seqEp = (SequenceEpisode)ep;
            if (seqEp.getSequence().equals(seq)) return seqEp;
        }

        return null;
    }//findContainingEpisode

    /** This version assumes you wish to search all episodes at the level above
     *  the given sequence.
     */
    private Episode findContainingEpisode(Sequence seq)
    {
        int level = seq.getLevel();
        Vector<Episode> parentEps = this.epmems.elementAt(level + 1);

        return findContainingEpisode(seq, parentEps);
        
    }//findContainingEpisode

    


    /**
     * findCousinList
     *
     * locates the cousins lists containing all actions that begin with a given
     * left-hand-side.  It is presumed that the action's level is the same level
     * as the given episode.
     *
     * @param lhs   the Episode that is the lhs of the cousins
     * @return a Vector<Action> or null if none found
     */
    public Vector<Action> findCousinList(Episode lhs)
    {
        //Extract the vector of all the actions on this level
        int level = lhs.getLevel();
        Vector<Action> actionList = this.actions.elementAt(level);

        //Search that list until a matching action is found
        Action foundAct = null;
        for(Action act : actionList)
        {
            if (lhs.equals(act.getLHS()))
            {
                foundAct = act;
                break;
            }
        }//for

        //If none was found, return null
        if (foundAct == null) return null;

        //return the cousins list
        return foundAct.getCousins();
        
    }//findCousinList

    /**
     * findRoute
     *
     * This method uses a breadth-first search to find a shortest path from a
     * given start state to a goal state at a given level.  
     *
     * CAVEAT:  initRoute does not verify that the given sequence and route are
     *          valid/allocated
     *
     * @arg seedRoute  a starting Route containing just the first sequence
     *
     * @return a Route object
     */
    Route findRoute(Route seedRoute)
    {
        this.mon.enter("findRoute");
        
        // can't build plan without level+1 actions
        int level = seedRoute.getLevel();
        assert(level + 1 < MAX_LEVEL_DEPTH);

        //This vector contains all the incomplete routes that have been or will
        //be considered by this routine as it builds its route
        Vector<Route> candRoutes = new Vector<Route>();
        candRoutes.add(seedRoute);

        /*--------------------------------------------------------------------------
         * Iterate over the candidate routes expanding them until the shortest
         * route to the goal is found (breadth-first search).  (Note: the size
         * of candRoutes will grow as the search continues.)
         */
        for(int i = 0; i < candRoutes.size(); i++)
        {
            this.mon.think();  //to track "thinking time"

            //To avoid long delays, give up on planning after examining N candidate routes
            if ((MAX_ROUTE_CANDS > 0) && (i > MAX_ROUTE_CANDS))
            {
                break;
            }
        
            //Find the shortest route that hasn't been examined yet and swap it
            //to the i-th position in the array
            int candPos = findShortestRoute(candRoutes, i);
            Route cand = candRoutes.elementAt(candPos);
            if (candPos != i)
            {
                Route tmp = candRoutes.elementAt(i);
                candRoutes.set(i, cand);
                candRoutes.set(candPos, tmp);
            }

            //log the current shortest candidate
            this.mon.log(""); //to reset after the dots (see above)
            this.mon.log("examining next shortest unexamined candidate %d at %ld of size %d:",
                         new Integer(i), cand.toString(), new Integer(cand.numElementalEpisodes()));
       
            //SUCCESS! If the last sequence in this route contains the goal
            //state, we're done.  Copy the details of this route to the newRoute
            //struct we were given and exit the loop.
            Sequence lastSeq = cand.lastElement();
            if (lastSeq.containsReward())
            {
                this.mon.exit("findRoute");
                return cand;
            }//if

            /*----------------------------------------------------------------------
             * Search for sequences to find any that meet both these criteria:
             * 1.  The sequence is the right-hand-side of an action at
             *     level+1 such that the left-hand-side sequence is the
             *     one most recently added to the candidate route
             * 2.  the sequence is not already in the candidate route
             *
             * Then build new candidate routes by adding all sequences to the
             * current candidate route that meet these criteria .
             *
             * TODO:  convert this to a subroutine
             */

            //Find all actions whose LHS matches the current candidates' RHS
            Episode lastSeqEp = findContainingEpisode(lastSeq);
            Vector<Action> extensions = findCousinList(lastSeqEp);
            if (extensions == null) continue;
            
            //Create a new candidate route by extending the current candidate
            //with each action in the extensions list
            for(Action act : extensions)
            {
                //Extract the sequence associated with the rhs of this matching
                //action
                Episode rhs = act.getRHS();
                Sequence rhsSeq = ((SequenceEpisode)rhs).getSequence();

                //Verify this sequence isn't already in the route
                if (cand.contains(rhsSeq)) continue;

                //log the new candidate
                this.mon.log("extending candidate with action: ");
                this.mon.tab();
                this.mon.log(rhsSeq);
       
                //If we've reached this point, then we can create a new candidate
                //route that is an extension of the current one
                Route newCand = (Route)cand.clone();
                newCand.add(rhsSeq);
       
                //Add this new candidate route to the candRoutes array
                candRoutes.add(newCand);
            }//for

            this.mon.log("done searching for ways to extend from sequence: ");
            this.mon.tab();
            this.mon.log(lastSeq);
       
        }//for

        //If we reach this point, we failed to find a route
        this.mon.exit("findRoute");
        return null;
    }//findRoute

    /**
     * findInterimStart
     *
     * This method locates a past sequence that is a best match for the agent's
     * current "location". This is an episode that follows the longest series of
     * episodes that match the ones most recently created by the agent. The
     * returned sequence is returned as a "seed" route that findRoute can use to
     * build a full path to a reward.
     *
     * @see #initPlan
     *
     * NOTE:  This method does not search level 0 episodes.
     *        @see #findInterimStartPartialMatch()
     *
     * @return the "seed" route containing the sequence that was found or null
     *         if the most recently completed sequence is unique at every level.
     */
    Route findInterimStart()
    {
        Vector<Episode> currLevelEpMem = null;  //list of epmems currently being searched
        int bestMatchIndex = 0;       // position of best match so far
        int bestMatchLen = 0;         // length of best match so far
        int level = -1;               // the current level being searched

        this.mon.enter("findInterimStart");
   
        //Iterate over all levels that are not the very top or bottom
        for(level = this.lastUpdateLevel; level >= 1; level--)
        {
            this.mon.log("searching Level %d", level);
   
            //Set the current episode list and its size for this iteration
            currLevelEpMem = this.epmems.elementAt(level);
            int lastIndex = currLevelEpMem.size() - 1;

            //starting with the penultimate level iterate backwards looking for a
            //subsequence that matches the current position
            for(int i = lastIndex - 1; i >= 0; i--)
            {
                //Count the length of the match at this point
                int matchLen = 0;
                while(currLevelEpMem.elementAt(i-matchLen).equals(
                          currLevelEpMem.elementAt(lastIndex - matchLen)))
                {
                    matchLen++;

                    //don't fall off the edge
                    if (i - matchLen < 0) break;
                }

                //See if we've found a new best match
                if (matchLen > bestMatchLen)
                {
                    bestMatchLen = matchLen;
                    bestMatchIndex = i;
                }
            }//for

            //If any match was found at this level, then stop searching
            if (bestMatchLen > 0) break;
        }//for

        //Check for no match found
        if (bestMatchLen == 0)
        {
            this.mon.log("findInterimStart failed: the current sequence is unique.");
            this.mon.exit("findInterimStart");
            return null;
        }

        //***If we reach this point, we've found a match.
        Sequence bestMatch = ((SequenceEpisode)currLevelEpMem.elementAt(bestMatchIndex + 1)).getSequence();
        this.mon.log("Search Result of length %d at index %d in level %d:  ",
                     bestMatchLen, bestMatchIndex + 1, level);
        this.mon.tab();
        this.mon.log(bestMatch);
        this.mon.log(" which comes after: ");
        this.mon.tab();
        this.mon.log(((SequenceEpisode)currLevelEpMem.elementAt(bestMatchIndex)).getSequence());
        this.mon.log(" and which matches: ");
        this.mon.tab();
        this.mon.log(((SequenceEpisode)currLevelEpMem.lastElement()).getSequence());


        //done!
        this.mon.exit("findInterimStart");
        return Route.newRouteFromSequence(bestMatch);
   
    }//findInterimStart

    /**
     * findInterimStartPartialMatch
     *
     * Like findInterimStart(), this method searches episodes for the best match
     * to the present.  However, it only searches level 0 and since the planning
     * routines need a sequences to build plans, it takes its best match and
     * returns the level 0 sequence that contains it and an offset into that
     * sequence that corresponds to the end of the match.  The route is built
     * from the sequence but begins where the match left off
     *
     * @arg offset is the index of the action in the returned sequence that a new
     * plan should start with
     *
     * @return the "start" sequence that was found or NULL if there was no partial
     *         match
     */
    Route findInterimStartPartialMatch()
    {
        Vector<Episode> level0Eps = this.epmems.elementAt(0);
        Vector<Episode> level1Eps = this.epmems.elementAt(1);
        int lastIndex = level0Eps.size()-1; // where the match begins
        int bestMatchLen = 0;         // length of the best match so far
        int bestMatchIndex = -1;    // index of level 1 episode that is best match
        int bestMatchOffset = -1;   // index of first matching action in best match
        
        //There must be at least two level 1 episodes to do a partial match
        if (level1Eps.size() < 2) return null;
        this.mon.enter("findInterimStartPartialMatch");

        /*======================================================================
         * Find the best match by comparing the level 0 episode sequence to
         * itself.  We do this by iterating over the level 1 episodes so that
         * when a match is found it is "oriented" in the level 1 episodes.
         * ----------------------------------------------------------------------
         */
        int numLvl0Eps = level0Eps.size() - 1;
        int lvl0Index = level0Eps.size() - 2;
        for(int i = level1Eps.size() - 1; i >= 0; i--)
        {
            SequenceEpisode currLvl1Ep = (SequenceEpisode)level1Eps.elementAt(i);
            Vector<Action> seqActs = currLvl1Ep.getSequence().getActions();
            for(int j = seqActs.size() - 1; j >= 0; j--)
            {
                Episode currEp = seqActs.elementAt(j).getLHS();
                //sanity check
                assert(level0Eps.elementAt(lvl0Index) == currEp);

                //Each matching episode extends the length of the overall match
                int matchLen = 0;
                while(level0Eps.elementAt(lvl0Index - matchLen).equals(
                          level0Eps.elementAt(numLvl0Eps - matchLen)))
                {
                    matchLen++;

                    //don't fall off the edge
                    if (lvl0Index - matchLen < 0) break;
                }

                //See if we've found a new best match
                if (matchLen > bestMatchLen)
                {
                    bestMatchLen = matchLen;
                    bestMatchIndex = i;
                    bestMatchOffset = j;
                }

                lvl0Index--;
            }//for
        }//for

        //Check for no match found
        if (bestMatchLen == 0)
        {
            this.mon.log("findInterimStartPartialMatch failed: the current sequence is unique.");
            this.mon.exit("findInterimStartPartialMatch");
            return null;
        }

        //***If we reach this point, we've found a match.
        Sequence bestMatch = ((SequenceEpisode)level1Eps.elementAt(bestMatchIndex)).getSequence();
        this.mon.log("Search Result of length %d at index %d and offset %d:  ",
                     bestMatchLen, bestMatchIndex + 1, bestMatchOffset);
        this.mon.tab();
        this.mon.log(bestMatch);


        //done!
        this.mon.exit("findInterimStartPartialmatch");
        return Route.newRouteFromSequence(bestMatch, bestMatchOffset);
   
    }//findInterimStartPartialMatch

    /**
     * findBestReplacement
     *
     * Find a replacement in this.repls that could be applied to the current
     * sequence in this.currPlan. If there are multiple such replacements, the
     * one with the highest confidence is returned.
     * 
     * NOTE: This method does not find replacements across adjacent sequences in
     *       this.currPlan.  This might be something to consider in the future.
     *
     * NOTE: This is a pretty expensive which may matter someday.
     *
     * @return best replacement found or null no applicable replacements were
     *         found.
     */
    Replacement findBestReplacement()
    {
        this.mon.enter("findBestReplacement");
        
        Replacement result = null; // this will hold the return value
        double bestConf = -1.0;    // confidence in the best match so far

        assert(this.currPlan != null);

        // iterate through each level of the plan
        for(int level = this.currPlan.getNumLevels() - 1; level >= 0; level--)
        {
            //Extract the current sequence from the route at this level
            Route  route   = this.currPlan.getRoute(level);
            Sequence currSeq = route.getCurrSequence();

            //If there are no replacements available at this level, skip this
            //iteration
            if (this.repls.size() <= level) continue;

            //Iterate over the replacement rules for this level to find the best
            //match
            Vector<Replacement> levelRepls = this.repls.elementAt(level);
            for(Replacement cand : levelRepls)
            {
                if ((route.canApply(cand)) && (cand.getUtility() > bestConf))
                {
                    result = cand;
                    bestConf = cand.getUtility();
                }
            }                
        }//for (each level)

        //Log the outcome
        if (result == null)
        {
            this.mon.log("no applicable replacement found");
        }
        else
        {
            this.mon.log("best existing repl: ");
            this.mon.tab();
            this.mon.log(result);
        }   

        //If a match wasn't found, then result will still be null
        this.mon.exit("findBestReplacement");
        return result;
    }//findBestReplacement

    /**
     * replacementExists
     *
     * searches this.repls to see if the equivalent of a given replacement is
     * already present.
     *
     * @arg findMe  the replacement to search for
     *
     * @return true if the equivalent is present and false otherwise
     */
    private boolean replacementExists(Replacement findMe)
    {
        //get the existing replacements that are the same level as this one
        int level = findMe.getLevel();
        Vector<Replacement> replList = this.repls.elementAt(level);
        
        //Iterate through the list looking for matches
        for(Replacement repl : replList)
        {
            if (repl.equals(findMe))
            {
                return true;
            }
        }//for

        return false;
   
    }//replacementExists

    
    /**
     * makeNewReplacement
     *
     * this method examines the current plan and creates a new replacement rule
     * at the lowest available level such that a) the rule would apply to the
     * current position in the current plan and b) the rule is not a duplicate
     * of any existing one in {@link #repls}.
     *
     * CAVEAT:  Caller is responsible for guaranteeing that the current plan is valid
     *
     * @return a new Replacement struct (or NULL if something goes wrong)
     *
     */
    Replacement makeNewReplacement()
    {
        this.mon.enter("makeNewReplacement");
        
        //Search all levels starting at the bottom
        for(int level = 0; level < this.currPlan.getNumLevels(); level++)
        {
            //Extract the current sequence from the route at this level
            Route  route   = this.currPlan.getRoute(level);
            Sequence currSeq = route.getCurrSequence();

            //There must be at least two actions left or don't bother
            int actIdx = route.getCurrActIndex();
            if (actIdx + 1 >= currSeq.length())
            {
                this.mon.log("remainder of sequence too short for replacement");
                this.mon.exit("makeNewReplacement");
                return null;
            }

            //Extract the next two actions from the current sequence
            Action act1 = currSeq.getActionAtIndex(actIdx);
            Action act2 = currSeq.getActionAtIndex(actIdx + 1);

            this.mon.log("Constructing a new replacment for these two actions:");
            this.mon.tab();
            this.mon.log(act1);
            this.mon.tab();
            this.mon.log(act2);
       
            //Pick a random starting position in actions list for this level. We
            //start the search in a random position so that the agent won't
            //always default to the lowest numbered command.
            Vector<Action> actList = this.actions.elementAt(level);
            int start = this.randGen.nextInt(actList.size());

            //Starting at the random start position, try all possible actions
            //until we find one that creates a new, unique replacement
            for(int i = 0; i < actList.size(); i++)
            {
                //Retrieve the candidate action
                int index = (start + i) % actList.size();
                Action candAct = actList.elementAt(index);

                //See if the candidate is compatible with these to-be-replaced
                //actions.  This comparision is done differently at level 0 than
                //other levels
                if (level == 0)
                {
                    //The LHS sensors of candidate must match the LHS sensors of
                    //act1
                    ElementalEpisode candLHS = (ElementalEpisode)candAct.getLHS();
                    ElementalEpisode act1LHS = (ElementalEpisode)act1.getLHS();
                    if (! candLHS.equalSensors(act1LHS))
                    {
                        continue;   // bad match, try a different candidate
                    }

                    //the RHS sensors of the candidate must match the RHS
                    //sensors of act2.
                    ElementalEpisode candRHS = (ElementalEpisode)candAct.getRHS();
                    ElementalEpisode act2RHS = (ElementalEpisode)act1.getRHS();
                    if (! candRHS.equalSensors(act2RHS))
                    {
                        continue;   // bad match, try a different candidate
                    }
                }//if
                else                // level 1 or higher
                {
                    //The first action of the LHS subsequence must match
                    SequenceEpisode candLHS = (SequenceEpisode)candAct.getLHS();
                    SequenceEpisode act1LHS = (SequenceEpisode)act1.getLHS();
                    Action candLHSSubAct = candLHS.getSequence().firstAction();
                    Action act1LHSSubAct = act1LHS.getSequence().firstAction();
                    if (! candLHSSubAct.equals(act1LHSSubAct))
                    {
                        continue;   // bad match, try a different candidate
                    }

                    //the last action of the RHS subsequence must match
                    SequenceEpisode candRHS = (SequenceEpisode)candAct.getRHS();
                    SequenceEpisode act2RHS = (SequenceEpisode)act2.getRHS();
                    Action candRHSSubAct = candRHS.getSequence().lastAction();
                    Action act2RHSSubAct = act2RHS.getSequence().lastAction();
                    if (candRHSSubAct != act2RHSSubAct)
                    {
                        continue;   // bad match, try a different candidate
                    }

                }//else

                //If we reach this point then the candidate is compatible.
                //Create the replacement and see if it is a duplicate
                Replacement result = new Replacement(act1, act2, candAct);
                if (replacementExists(result))
                {
                    continue;  //duplicate replacement
                }

                //All checks passed. Success!  Add the replacement it creates to
                //the list of known replacements and return it to the caller
                Vector<Replacement> replList = this.repls.elementAt(level);
                replList.add(result);
                return result;

            }//for
           
        }//for

        // No new replacement can be made.  This happens when replacmeents are
        // only possible at some levels and at those levels all valid candidates
        // already exist with low confidence.
        return null;
    }//makeNewReplacement

    /**
     * considerReplacement
     *
     * See if there is a replacement rule that the agent is confident enough to
     * apply to the current plan and apply it.
     *
     */
    protected void considerReplacement()
    {
        //See if the current plan can handle any more replacements before
        //proceeding 
        if ((this.currPlan == null) 
            || (this.currPlan.numRepls() >= MAX_REPLS)) return;
        this.mon.enter("considerReplacement");

        Replacement selectedRepl = null;  //this will hold the repl we select

        //Retrieve the best matching existing replacement 
        Replacement existingCand = findBestReplacement();

        //Also create a new replacement if the agent is confident enough
        Replacement newCand = null;
        if ( this.selfConfidence >= (1.0 - DecisionElement.INIT_UTILITY))
        {
            newCand = makeNewReplacement();
        }

        //If both are null, no dice
        if ((existingCand == null) && (newCand == null))
        {
            this.mon.log("No valid replacement found.");
            this.mon.exit("considerReplacement");
            return;
        }
        //If either is null, then the choice is easy
        else if (existingCand == null)
        {
            selectedRepl = newCand;
        }
        else if (newCand == null)
        {
            selectedRepl = existingCand;
        }
        //if both are non-null select the one with highest confidence
        else if (newCand.getUtility() > existingCand.getUtility())
        {
            selectedRepl = newCand;
        }
        else
        {
            selectedRepl = existingCand;
        }

        //Make sure the agent is confident enough to use the selected replacement
        if (this.selfConfidence < (1.0 - selectedRepl.getUtility()))
        {
            this.mon.log("No valid replacement found.  Agent confidence (%g) too low for new replacement.\n",
                         this.selfConfidence);
            this.mon.exit("considerReplacement");
            return;
        }

        //Apply the replacement (repl) to the current plan
        this.currPlan.applyReplacement(selectedRepl);

        this.mon.log("Applied replacement:");
        this.mon.tab();
        this.mon.log(selectedRepl);
        this.mon.exit("considerReplacement");
   
    }//considerReplacement


    
    /**
     * chooseCommand_SemiRandom
     *
     * This function selects a random command that would create a new action
     * based upon the agent's most recent sensing.  If no such new action can be
     * made, then it just chooses a random command without qualification.
     *
     * CAVEAT:  This routine assumes that the lowest numbered command is 0.
     *
     */
    int chooseCommand_SemiRandom()
    {
        //Make an array of boolean values (one per command) and init them all to
        //true. This array will eventually indicates whether the given command
        //would create a unique episode (true) or not (false).
        int numCmds = this.env.getNumCommands();
        boolean valid[] = new boolean[numCmds];
        for(int i = 0; i < numCmds; i++)
        {
            valid[i] = true;        // innocent until proven guilty
        }

        //Retrieve the cousins list of actions that have the agent's current
        //sensing on the LHS
        Episode nowEp = this.epmems.firstElement().lastElement();
        Vector<Action> cousins = findCousinList(nowEp);

        //Mark the commands associated with the cousins as invalid
        for(Action act : cousins)
        {
            ElementalEpisode currEp = (ElementalEpisode)act.getLHS();
            valid[currEp.getCommand()] = false; // guilty!
        }

        //Start from a random starting position in the valid array and return
        //the first random entry found.  If no unique command is found, the
        //original random value is used
        int cmd = randGen.nextInt(numCmds);
        for(int i = 0; i < numCmds; i++)
        {
            int next = (cmd + i) % numCmds;
            if (valid[next])
            {
                cmd = next;
                break;
            }
        }

        this.mon.log("Choosing a semi-random command: %d", cmd);
        return cmd;

    }//chooseCommand_SemiRandom

    /**
     * chooseCommand_WithPlan
     *
     * This function increments to the next action in the current plan and
     * extracts the associated cmd to return to the caller.
     *
     * CAVEAT: this.currPlan should contain a valid plan that does not need
     *         recalc.
     */
    int chooseCommand_WithPlan()
    {
        this.mon.log("Choosing command from plan:");
        this.mon.tab();
        this.mon.log(this.currPlan);
   
        //Before executing the next command in the plan, see if there is a
        //replacement rule that the agent is confident enough to apply to the
        //current plan.  If so, apply it.
        considerReplacement();

        //Get the current level 0 action from the plan
        Route level0Route = this.currPlan.getRoute(0);
        Action currAction = level0Route.getCurrAction();

        //extract the command prescribed by the current action
        ElementalEpisode lhs = (ElementalEpisode)currAction.getLHS();        
        int cmd = lhs.getCommand();

        //advance the "current action" pointer to the next action as a result of
        //taking this action
        this.currPlan.advance(0);

        //Return the selected command to the environment
        return cmd;

    }//chooseCommand_WithPlan
    

    /**
     * chooseCommand
     *
     * This function decides what command to issue next.  Typically this
     * selection will be the next step in a plan.  However, the agent may decide
     * to modify the plan as part of the command selection process.  If no plan
     * exists, a random command is selected.
     *
     * @return int the command that was chosen
     */
    int chooseCommand()
    {
        this.mon.enter("chooseCommand");

        //Increment command counter for data gathering
        (this.stepsSoFar)++;
           
        //If there is a plan in effect, first see if it's been effective so far.
        if (this.currPlan != null)
        {
            this.mon.log("Checking to see if the plan is still valid");

            //Check to see if the plan is still valid.
            Vector<Episode> lvl0Eps = this.epmems.firstElement();
            ElementalEpisode nowEp = (ElementalEpisode)lvl0Eps.lastElement();
            if (! this.currPlan.nextStepIsValid(nowEp))
            {
                this.mon.log("Current plan invalid.  Replanning...:");

                //"We now consecrate the bond of obedience."  The agent and all
                //active replacements are now to be penalized for causing this
                //failure.
                penalizeDecEls();
                penalizeAgent();

                //Remove the bad plan so we can replan
                this.currPlan = null;
           
            }//if
            else                // The plan is going swimmingly! 
            {
                this.mon.log("Plan successful so far.");

                //If a level 0 sequence has just completed then the agent's
                //confidence is increased due to the partial success
                Route lvl0Route = this.currPlan.getRoute(0);
                if ((lvl0Route.getCurrSeqIndex() > 0)
                    && (lvl0Route.getCurrActIndex() == 0))
                {
                    rewardAgent();
                }

            }//else
       
        }//if

        //If the current plan is invalid then we first need to make a new plan
        if ( (this.currPlan == null) || (this.currPlan.needsRecalc()) )
        {
            this.currPlan = initPlan();

            //If there still is no plan at this point then that means the agent
            //doesn't have enough experience yet.  Select a semi-random command
            //that would create a new action.
            if (this.currPlan == null)
            {
                this.mon.log("No plan can be found.  Taking a random action.");
                this.mon.exit("chooseCommand");
                return chooseCommand_SemiRandom();
            }//if

            //Log the new plan
            this.mon.log("New Plan:");
            this.mon.tab();
            this.mon.log(this.currPlan);
        }//if

        //%%%AMN:  I have ported this to Java in case we need it.  I sure hope
        // we don't.
        // //%%%TEMPORARY KLUDGE:  For Dustin and Ben.  
        // {
        //     //  adding a % chance of random action depending upon how long it's been
        //     //  since we've reached the goal
        //     int randDelay = 100;
        //     if (stepsSoFar > randDelay)
        //     {
        //         int rNum = this.randGen(1000);
        //         if (stepsSoFar - randDelay > rNum)
        //         {
        //             return chooseCommand_SemiRandom();
        //             stepsSoFar = 0;
        //         }
        //     }
        // }
    

        //If we've reached this point then there is a working plan so the agent
        //should select the next step with that plan.
        this.mon.exit("chooseCommand");
        return chooseCommand_WithPlan();

    }//chooseCommand
    
}//class Ziggurat


 
