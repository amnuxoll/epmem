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
    protected Vector<Vector<Episode>> epmems = new Vector<Vector<Episode>>();
    /** All actions learned so far */
    protected Vector<Vector<Action>> actions = new Vector<Vector<Action>>();
    /** All sequences learned so far */
    protected Vector<Vector<Sequence>> seqs = new Vector<Vector<Sequence>>();
    /** The agent's current plan for reaching a goal */
    protected Plan currPlan = null;
    /** All replacement rules that the agent has tried */
    protected Vector<Vector<Replacement>> repls = new Vector<Vector<Replacement>>();
    /** high confidence means the agent will try new things.  Value range [0..1] */
    protected double selfConfidence = INIT_SELF_CONFIDENCE;
    /** the current environment provides the agent with a limited amount of
        information about itself */
    protected Environment env = null;
    /** the monitor that is currently logging events in Zigg.  I'm making this
     * static so that other entities can log events to the monitor.  Maybe a
     * mistake?  But at the moment I can't think of a better approach. (:AMN:, Apr 2012)
     */
    protected static Monitor mon = new MonitorNull();
    /** this is the highest level in the hierarchy that contains data.  This is
     * used by the orientation methods */
    protected int lastUpdateLevel = 0;
    /** this vector contains all {@link DecisionElement}s that have recently been
     * used to make a decision.  When the outcome of decision(s) is known these
     * active elements' utilities are adjusted based upon that outcome.
     */
    protected Vector<DecisionElement> activeDecEls = new Vector<DecisionElement>();
    /** count how many goals we've reached so far */
    protected int goalCount = 0;
    /** count how many steps we've taken since the last goal */
    protected int stepsSoFar = 0;
    /** for all your random number geneation needs! */
    protected Random randGen = new Random();

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
        //Init episodes, actions and sequences at level 0 
        this.epmems.add(new Vector<Episode>());
        this.actions.add(new Vector<Action>());
        Vector<Sequence> startSeq = new Vector<Sequence>();
        startSeq.add(new Sequence());
        this.seqs.add(startSeq);
        this.repls.add(new Vector<Replacement>());

        //record input parameters
        this.env = env;
        this.mon = new MonitorStdOut(env);
        
    }//ctor


    /*======================================================================
     * Accessor Methods
     *----------------------------------------------------------------------
     */
    /** accessor for the monitor.  */
    public static Monitor getMonitor() { return Ziggurat.mon; }

    /** accessor for the monitor.  */
    public static void setMonitor(Monitor newMon) { Ziggurat.mon = newMon; }

    /** retrieve all episodes */
    public Vector<Vector<Episode>> getEpmems() { return this.epmems; }

    /** retrieve all actions */
    public Vector<Vector<Action>> getActions() { return this.actions; }

    /** retrieve all sequences */
    public Vector<Vector<Sequence>> getSequences() { return this.seqs; }

    /** set the random number generator's seed */
    public void setRandSeed(int x) { this.randGen.setSeed(x); }

    /** set the random number generator itself */
    public void setRandGen(Random newGen) { this.randGen = newGen; }

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
        ep.setCommand(-1);
        this.epmems.elementAt(0).add(ep);
        update(0);

        
        // If we receive a reward, update the memory to reflect this
        WME rewardWME = sensors.getAttr(WME.REWARD_STRING);
        if((rewardWME != null) && (rewardWME.getDouble()  > 0.0))
        {
            this.mon.reward(rewardWME.getDouble());
       
            //If a plan is in place, reward the agent and any outstanding replacements
            if ((this.currPlan != null) && (this.currPlan.advance(0) == null))
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
        this.mon.logPart("Using command " + env.stringify(cmd) + " to complete episode #" + (epmems.size()-1) + ":  ");
        this.mon.log(ep);

        //Return the result
        this.mon.exit("tick");
        return cmd;

    }//tick

    /*======================================================================
     * Non-Public Methods
     *----------------------------------------------------------------------
     */
    /**
     * findClosestExistingAction
     *
     * given an action, this method a vector of actions to find the
     * one that is the most similar to this one.  If an exact match can not be
     * found, this method returns the first action it finds with the same LHS (a
     * "cousin").
     *
     * @param target is the target action to find a match for.  IMPORTANT:  This
     * action's level must be properly set.
     * @param actionList   is the vector to search
     *
     * @return a matching action if exists, otherwise a cousin if it exists,
     * otherwise null
     */
    protected Action findClosestExistingAction(Action target, Vector<Action> actionList)
    {
        for(Action curr : actionList)
        {
            //If we find a match, return it
            if (curr.equals(target))
            {
                return curr;
            }

            //If just the left-hand-sides match, then we have a cousin
            if (curr.getLHS().equals(target.getLHS()))
            {
                //Iterate over all the cousins to see if there is an exact match
                Vector<Action> cousinsList = curr.getCousins();
                for(Action currCousin : cousinsList)
                {
                    //If both match, then we can reuse the matching action 
                    if (currCousin.equals(target))
                    {
                        return currCousin;
                    }
                }//for

                //No exact match was found so return the cousin instead
                return curr;
            }

        }// for

        //No luck
        return null;
    
    }//findClosestExistingAction
    
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
    protected int update(int level)
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
        this.mon.log("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< End of Level %d Data", level);

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
        this.mon.log("Candidate Action: ");
        this.mon.tab();
        this.mon.log(newAction);
        

        //Iterate over every action in the list and compare it to our new
        //candidate action.  If the candidate is unique, it'll be added to the
        //action list.  If it's a partial match (same LHS , different RHS) it's
        //added to the action list but also recorded as a cousin of the
        //partially matching action.  If the candidate completely matches an
        //existing action, it'll be discarded and the existing action's
        //frequency will be updated
        boolean matchComplete = false;      // are we done searching yet?
        boolean addNewAction = true;        // whether the cand action is unique

        //Find the best matching action
        Action updateExistingAction = findClosestExistingAction(newAction, actionList);

        //If it's an exact match, we can reuse the matching action
        if ( (updateExistingAction != null) && (updateExistingAction.equals(newAction)) )
        {
            addNewAction = false;
        }
        //If it's a cousin, we want to add this new action but also include it
        //in the cousins list
        else if (updateExistingAction != null)
        {
            this.mon.logPart("found a cousin: ");
            this.mon.log(updateExistingAction);

            Vector<Action> cousinsList = updateExistingAction.getCousins();
            cousinsList.add(newAction);
            newAction.setCousins(cousinsList);
            addNewAction = true;
        }

        //Add the new action
        if(addNewAction)
        {
            this.mon.log("Adding new action to level %d action list: ", level);
            this.mon.log(newAction);
            actionList.add(newAction);

            // set this variable so that we recursively update the next level
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
        //findOrientation()
        this.lastUpdateLevel = level;

        // add most recently seen action to current sequence
        Sequence currSequence = sequenceList.elementAt(sequenceList.size() - 1);
        this.mon.log("Adding action #%d: ", currSequence.length());
        this.mon.tab();
        this.mon.log(updateExistingAction);
        this.mon.log(" to current sequence:");
        this.mon.tab();
        this.mon.log(currSequence);
        currSequence.add(updateExistingAction);

        // if the action we just added is indeterminate or has yielded a reward
        // then end the current sequence and start a new one
        if ( updateExistingAction.isIndeterminate() || updateExistingAction.containsReward() )
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
                this.mon.log("Creating a new level %d episode with sequence: ", level + 1);
                this.mon.tab();
                this.mon.log(currSequence);

                //Make sure the parent level exists!
                while (this.epmems.size() <= level + 1)
                {
                    this.epmems.add(new Vector<Episode>());
                    this.actions.add(new Vector<Action>());
                    Vector<Sequence> startSeq = new Vector<Sequence>();
                    startSeq.add(new Sequence());
                    this.seqs.add(startSeq);
                }
                    
                //Add the new episode
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
    protected void rewardAgent()
    {
        this.mon.logPart("Overall confidence increased from " + this.selfConfidence);
   
        this.selfConfidence += (1.0 - this.selfConfidence) / 2.0;
   
        this.mon.log(" to " + this.selfConfidence);
    }//rewardAgent   

    /**
     * penalizeAgent
     *
     * This method halves the agent's overall confidence.
     */
    protected void penalizeAgent()
    {
        this.mon.logPart("Overall confidence decreased from " + this.selfConfidence);
   
        this.selfConfidence /= 2.0;
   
        this.mon.log(" to " + this.selfConfidence);
    }//penalizeAgent   

    /**
     * rewardDecEls
     *
     * increases the confidence of all active decision elements.
     *
     * @see DecisionElement.reward
     *
     */
    protected void rewardDecEls()
    {
        for(DecisionElement de : this.activeDecEls)
        {
            this.mon.logPart("Increasing utility of " + env.stringify(de) + " from " + de.getUtility());
            de.reward();
            this.mon.log(" to " + de.getUtility());
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
    protected void penalizeDecEls()
    {
        for(DecisionElement de : this.activeDecEls)
        {
            this.mon.logPart("Decreasing utilty of " + env.stringify(de) + " from " + de.getUtility());
            de.penalize();
            this.mon.log(" to " + de.getUtility());
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
    protected Plan initPlan()
    {
        //If there are no level 1 episodes yet then there's not enough data to
        //create a plan
        if (this.epmems.size() < 2) return null;
        
        this.mon.enter("initPlan");

        //Try to figure out where I am.  I can't make plan without this.
        Route seedRoute = findOrientation();
        if (seedRoute == null)
        {
            //Try orienting via the level 0 episodes
            seedRoute = findElementalOrientation();
            if (seedRoute == null)
            {
                this.mon.exit("initPlan");
                return null;        // I give up
            }
        }//if

        //Try to initialize the route at the same level as the start sequence
        int level = seedRoute.getLevel();
        Route currRoute = findRoute(seedRoute);
        
        //Give up if no route can be found
        if (currRoute == null)
        {
            this.mon.log("findRoute failed");
            this.mon.exit("initPlan");
            
            return null;
        }//if

        //Initialize a plan using the new route
        Plan resultPlan = new Plan(currRoute);

        //report
        this.mon.log("Success: found route to goal at level: %d:", level);
        this.mon.log(env.stringify(currRoute));
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
    protected int findShortestRoute(Vector<Route> searchMe, int startPos)
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
    protected int findShortestRoute(Vector<Route> searchMe)
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
    protected Episode findContainingEpisode(Sequence seq, Vector<Episode> vec)
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
    protected Episode findContainingEpisode(Sequence seq)
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
     * @param seedRoute  a starting Route containing just the first sequence
     *
     * @return a Route object
     */
    public Route findRoute(Route seedRoute)
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
            this.mon.log("examining next shortest candidate #%d of size %d:",
                         i, cand.numElementalEpisodes());
            this.mon.log(cand);
       
            //SUCCESS! If the last action in this route contains the goal
            //state, we're done.  Copy the details of this route to the newRoute
            //struct we were given and exit the loop.
            Sequence lastSeq = cand.getLastSeq();
            if (lastSeq.containsReward())
            {
                this.mon.log("Selected this route to goal:");
                this.mon.log(cand);
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
                this.mon.logPart("extending candidate with action: ");
                this.mon.log(act);
       
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
     * findOrientation
     *
     * This method locates a past sequence at level 1 or higher that is a best
     * match for the agent's current "location". This is an episode that follows
     * the longest series of episodes that match the ones most recently created
     * by the agent. The returned sequence is returned as a "seed" route that
     * findRoute can use to build a full path to a reward.
     *
     * <p> It's best to explain this using an example: Consider the case where
     * the agent has experiened the following series of episodes at level 1 in
     * this order:<br>
     *            <ul>Z,A,B,R,A,C,A,D,A,B,R,A</ul>
     * where episode 'D' contains a goal.  The agent is trying to create a path
     * from its current position in the state space to the goal.  In order to
     * get there, it must first know where it is.  (Analogy: How can you drive
     * to your house if you don't know where you are?!?)  In order to orient
     * itself, the agent compares it's most recently experienced episodes with
     * past episodes to find the longest match.  In this case, it can find a
     * time in its past when it experienced the same sub-series of four
     * episodes (A,B,R,A) at index 1 in the series (right after the 'Z') that it
     * has just finished experiencing right now.  By using this as a "seed" the
     * agent guesses that it can reach the goal from its current position by
     * repeating episodes C,A,D.
     *
     * <p> FAQ: What about the 'leftover' episodes one level below?  Consider
     * the example used in the previous paragraph and assume that those episodes
     * are at level 1.  In this case, each of those episodes is comprised of a
     * sequence of one or more level 0 episodes.  So, even though the most
     * recent episode is 'A' at level 1, it's possible that there are level 0
     * episodes that have occurred since this 'A' but have not been 'recorded'
     * into a new level 1 episode yet.
     *
     * <p>ANS: The answer is that this is a legitimate concern but, at the
     * moment, Ziggurat's design guarantees this will never happen.  Zigg only
     * calls findOrientation when it needs to create a plan. Except for the
     * first plan, it only needs to create a plan when a previous plan has
     * failed or has been completed successfully.  As a result, it will only try
     * to create a plan when a predicted outcome of an action did not occur or
     * when it has just reached a goal state.  These are, by definition, the two
     * events that cause the current sequence to complete at <u>all levels</u>
     * of the hierarchy.  As a result, we are guaranteed that there are no
     * "loose ends" to worry about when trying to find a new start position.
     *
     * @see #initPlan
     * 
     * <p>NOTE:  This method does not search level 0 episodes.
     *           See {@link #findElementalOrientation}
     *
     * @return the "seed" route containing the sequence that was found or null
     *         if the most recently completed sequence is unique at every level.
     */
    protected Route findOrientation()
    {
        Vector<Episode> currLevelEpMem = null;  //list of epmems currently being searched
        int bestMatchIndex = 0;       // position of best match so far
        int bestMatchLen = 0;         // length of best match so far
        int level = -1;               // the current level being searched

        this.mon.enter("findOrientation");
   
        //Iterate over all levels that are not the very top or bottom
        
        //%%%DEBUG:  temporarily fixing at level 1
        for(level = 1; level >= 1; level--)
//%%%        for(level = this.lastUpdateLevel; level >= 1; level--)
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
            this.mon.log("findOrientation failed: the current sequence is unique.");
            this.mon.exit("findOrientation");
            return null;
        }

        //***If we reach this point, we've found a match.
        Sequence bestMatch = ((SequenceEpisode)currLevelEpMem.elementAt(bestMatchIndex + 1)).getSequence();
        this.mon.log("Search Result of length %d at index %d in the level %d episodes:  ",
                     bestMatchLen, bestMatchIndex, level);
        this.mon.tab();
        this.mon.log(bestMatch);
        this.mon.log(" which comes after: ");
        this.mon.tab();
        this.mon.log(((SequenceEpisode)currLevelEpMem.elementAt(bestMatchIndex)).getSequence());
        this.mon.log(" and which matches: ");
        this.mon.tab();
        this.mon.log(((SequenceEpisode)currLevelEpMem.lastElement()).getSequence());


        //done!
        this.mon.exit("findOrientation");
        return new Route(bestMatch);
   
    }//findOrientation

    /**
     * findElementalOrientation
     *
     * Like findOrientation(), this method searches episodes for the best match
     * to the present.  However, it only searches level 0 and since the planning
     * routines need a sequences to build plans, it takes its best match and
     * returns the level 0 sequence that contains it and an offset into that
     * sequence that corresponds to the end of the match.  The route is built
     * from the sequence but begins where the match left off
     *
     * <p>Note: This method is much more expensive than {@link #findOrientation}
     * and should only be called when that method fails.
     *
     * @arg offset is the index of the action in the returned sequence that a new
     * plan should start with
     *
     * @return the "start" sequence that was found or NULL if there was no partial
     *         match
     */
    protected Route findElementalOrientation()
    {
        Vector<Episode> level0Eps = this.epmems.elementAt(0);
        Vector<Episode> level1Eps = this.epmems.elementAt(1);
        int lastIndex = level0Eps.size()-1; // where the match begins
        int bestMatchLen = 0;         // length of the best match so far
        int bestMatchIndex = -1;    // index of level 1 episode that is best match
        int bestMatchOffset = -1;   // index of first matching action in best match
        
        //There must be at least two level 1 episodes to do a match
        if (level1Eps.size() < 2) return null;
        this.mon.enter("findElementalOrientation");

        /*======================================================================
         * Find the best match by comparing the level 0 episode sequence to
         * itself.  We do this by iterating over the level 1 episodes so that
         * when a match is found it is "oriented" in the level 1 episodes.
         * ----------------------------------------------------------------------
         */
        int lastLevel0EpIndex = level0Eps.size() - 1;

        //level0Index will be used to track the index of the current level 0
        //index.  This index needs to be initialized to refer to to the last
        //level 0 episode in the last SequenceEpisode at level 1.
        Sequence lastLevel0Seq = this.seqs.elementAt(0).lastElement();
        int level0Index = level0Eps.size() - lastLevel0Seq.length() - 2;
        for(int i = level1Eps.size() - 1; i >= 0; i--)
        {
            SequenceEpisode currLvl1Ep = (SequenceEpisode)level1Eps.elementAt(i);
            Vector<Action> seqActs = currLvl1Ep.getSequence().getActions();
            for(int j = seqActs.size() - 1; j >= 0; j--)
            {
                Episode currEp = seqActs.elementAt(j).getLHS();
                
                //sanity check: the current episode at lvl 0 should match the
                //one extracted via level 1
                if(! level0Eps.elementAt(level0Index).equals(currEp))
                {
                    System.err.println("ERROR!  findElementalOrientation got out of sync.");
                    System.exit(-3);
                }

                //Each matching episode extends the length of the overall match
                int matchLen = 0;
                while(level0Eps.elementAt(level0Index - matchLen).equals(
                          level0Eps.elementAt(lastLevel0EpIndex - matchLen)))
                {
                    matchLen++;

                    //don't fall off the edge
                    if (level0Index - matchLen < 0) break;
                }

                //See if we've found a new best match
                if (matchLen > bestMatchLen)
                {
                    bestMatchLen = matchLen;
                    bestMatchIndex = i;
                    bestMatchOffset = j;
                }

                level0Index--;
            }//for
        }//for

        //Check for no match found
        if (bestMatchLen == 0)
        {
            this.mon.log("findElementalOrientation failed: the current sequence is unique.");
            this.mon.exit("findElementalOrientation");
            return null;
        }

        //***If we reach this point, we've found a match.
        Sequence bestMatch = ((SequenceEpisode)level1Eps.elementAt(bestMatchIndex)).getSequence();
        this.mon.log("Search Result of length %d at index %d and offset %d:  ",
                     bestMatchLen, bestMatchIndex + 1, bestMatchOffset);
        this.mon.tab();
        this.mon.log(bestMatch);


        //done!
        this.mon.exit("findElementalOrientation");
        return new Route(bestMatch, bestMatchOffset);
   
    }//findElementalOrientation

    /**
     * findBestReplacement
     *
     * Find a replacement in this.repls that could be applied to the current
     * sequence in this.currPlan. If there are multiple such replacements, the
     * one with the highest confidence is returned.
     * 
     * <p>NOTE: This is a pretty method expensive which may matter someday.
     *
     * @return best replacement found or null no applicable replacements were
     *         found.
     */
    Replacement findBestReplacement()
    {
        this.mon.enter("findBestReplacement");
        
        Replacement result = null; // this will hold the return value
        double bestConf = -1.0;    // confidence in the best match so far

        //sanity check
        if (this.currPlan == null) return null;

        // iterate through each level of the plan
        for(int level = this.currPlan.getNumLevels() - 1; level >= 0; level--)
        {
            this.mon.log("searching for replacement at level " + level);

            //Find the best matching replacement rules at this level
            Route route = this.currPlan.getRoute(level);
            if (this.repls.size() <= level) break; // no repls at this level
            Vector<Replacement> levelRepls = this.repls.elementAt(level);
            for(Replacement cand : levelRepls)
            {
                if ((cand.canApply(route, route.getCurrActIndex())) && (cand.getUtility() > bestConf))
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
    protected boolean replacementExists(Replacement findMe)
    {
        //see if there are any existing replacements that are the same level as
        //this one
        int level = findMe.getLevel();
        if (this.repls.size() <= level) return false;

        //Retrieve the repls for this level
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
     * %%%TODO:  break this method up into smaller chunks
     *
     * @return a new Replacement struct (or NULL if something goes wrong)
     *
     */
    protected Replacement makeNewReplacement()
    {
        this.mon.enter("makeNewReplacement");
        
        //Search all levels starting at the bottom
        for(int level = 0; level < this.currPlan.getNumLevels(); level++)
        {
            //Verify that the route at this level has sufficient actions remaining
            Route  route   = this.currPlan.getRoute(level);
            if (route.length() - route.getCurrActIndex() < 2)
            {
                this.mon.log("remainder of route at level " + level + " is too short for replacement");
                this.mon.exit("makeNewReplacement");
                return null;
            }
            //Extract the next two actions from the plan at this level
            Action act1 = route.getActionAtIndex(route.getCurrActIndex());
            Action act2 = route.getActionAtIndex(route.getCurrActIndex() + 1);

            this.mon.log("Attempting to construct a new replacement for these two actions:");
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
                this.mon.logPart("Considering this action for the replacement: ");
                this.mon.log(candAct);

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
                        this.mon.log("LHS sensors don't match, try a different candidate");
                        continue;
                    }

                    //the RHS sensors of the candidate must match the RHS
                    //sensors of act2.
                    ElementalEpisode candRHS = (ElementalEpisode)candAct.getRHS();
                    ElementalEpisode act2RHS = (ElementalEpisode)act2.getRHS();
                    if (! candRHS.equalSensors(act2RHS))
                    {
                        this.mon.log("RHS sensors don't match, try a different candidate");
                        continue;
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
                        this.mon.log("LHS actions don't match, try a different candidate");
                        continue;   // bad match, try a different candidate
                    }

                    //the last action of the RHS subsequence must match
                    SequenceEpisode candRHS = (SequenceEpisode)candAct.getRHS();
                    SequenceEpisode act2RHS = (SequenceEpisode)act2.getRHS();
                    Action candRHSSubAct = candRHS.getSequence().lastAction();
                    Action act2RHSSubAct = act2RHS.getSequence().lastAction();
                    if (candRHSSubAct != act2RHSSubAct)
                    {
                        this.mon.log("RHS actions don't match, try a different candidate");
                        continue;   // bad match, try a different candidate
                    }

                }//else

                //If we reach this point then the candidate is compatible.
                //Create the replacement and see if it is a duplicate
                Replacement result = new Replacement(act1, act2, candAct);
                if (replacementExists(result))
                {
                    this.mon.log("replacement already exists (duplicate)");
                    continue;
                }

                //All checks passed. Success!  Add the replacement it creates to
                //the list of known replacements and return it to the caller
                this.mon.log("Success!  Creating a new replacement.");
                while (this.repls.size() <= level) this.repls.add(new Vector<Replacement>());
                Vector<Replacement> replList = this.repls.elementAt(level);
                replList.add(result);
                return result;

            }//for (all actions at this level)
           
            //If we reach this point, it's still possible to make a replacement but
            //the new replacement action will have to be one the agent has never
            //experienced before
            this.mon.log("No existing actions can be used to make a replacement at level " + level +".");
        
            //At level 0 this is a matter of selecting a command
            if (level == 0)
            {
                //The replacement action's LHS sensors must match the LHS of act1.
                //It's RHS sensors must match the RHS of act2.
                Action candAct = new Action(act1.getLHS().clone(), act2.getRHS().clone());

                //Starting with a random command, iterate through all commands until
                //you find one that creates a unique action
                int numCmds = this.env.getNumCommands();
                int startCmd = randGen.nextInt(numCmds);
                for(int i = 0; i < numCmds; i++)
                {
                    //Insert the candidate command into the candidate action
                    int candCmd = (startCmd + i) % numCmds;
                    ElementalEpisode lhsEp = (ElementalEpisode)candAct.getLHS();
                    lhsEp.setCommand(candCmd);

                    this.mon.logPart("Considering this action for the replacement: ");
                    this.mon.log(candAct);

                    //If I've seen this one before try something else
                    Action bestMatch = findClosestExistingAction(candAct, actList);
                    if ( (bestMatch != null) && bestMatch.equals(candAct) )
                    {
                        this.mon.log("action already exists (duplicate)");
                        continue;
                    }

                    //All checks passed. Success!  Add the replacement it creates to
                    //the list of known replacements and return it to the caller
                    Replacement result = new Replacement(act1, act2, candAct);
                    this.mon.log("Success!  Creating a new replacement.");
                    while (this.repls.size() <= level) this.repls.add(new Vector<Replacement>());
                    Vector<Replacement> replList = this.repls.elementAt(level);
                    replList.add(result);
                    return result;
                
                }//for
            }
            else //level 1+
            {
                //%%%To be implemented...
            }
        
        }//for

        // No new replacement can be made.  This happens when replacements are
        // only possible at some levels and at those levels all valid candidates
        // already exist.
        this.mon.log("No new replacement could be constructed from a fabricated action.");
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
        if (! this.activeDecEls.contains(selectedRepl))
        {
            this.activeDecEls.add(selectedRepl);
        }

        this.mon.logPart("Applied replacement: ");
        this.mon.log(selectedRepl);
        this.mon.log("to get this revised plan:");
        this.mon.log(currPlan);
        this.mon.exit("considerReplacement");
   
    }//considerReplacement


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
    protected int chooseCommand()
    {
        this.mon.enter("chooseCommand");

        //Increment command counter for data gathering
        (this.stepsSoFar)++;
           
        //If the agent should always select a random command if it will learn a
        //new action from that command
        Recommend randRec = recommendCommand_ViaUniqueness();
        if (randRec.degree > this.selfConfidence)
        {
            //If there was a plan in place, ditch it
            if (this.currPlan != null)
            {
                this.currPlan = null;
                activeDecEls.clear();
                this.mon.log("Opting to go with random command for new action.  Plan abandoned.");
            }

            
            this.mon.exit("chooseCommand");
            return randRec.command;
        }

        //If there is currently a plan in effect, first see if it's been
        //effective so far.
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
                return randRec.command;
            }//if

            //Log the new plan
            this.mon.log("New Plan:");
            this.mon.tab();
            this.mon.log(this.currPlan);
        }//if

        //If we reach this point, the agent has decided to stick to its current
        //plan and therefore should select the next step with that plan.
        this.mon.exit("chooseCommand");
        return recommendCommand_WithPlan().command;

    }//chooseCommand

    /**
     * class Recommend
     *
     * contains a recommended command and a degree of recommendation: a double
     * with a value in the range [0..1].  This is used by the methods below to
     * recommend a command to the chooseCommand method.  It's essentially a
     * clean way for these methods to have two return values.
     */
    protected class Recommend
    {
        public int command = -1;
        public double degree = 0.0;

        public Recommend(int initCmd, double initDegree)
        {
            this.command = initCmd;
            this.degree = initDegree;
        }
    }//class Recommend
        
    /**
     * calcUniqueness
     *
     * is a helper method for {@link #recommendCommand_ViaUniqueness}.  It
     * calculates how unique a sequence that a given command would create if it
     * were selected.  This is specified by a number that indicates how many of
     * previous episodes would have be added for the given command to be
     * unique.  For example, if the current episodic memory was:<br>
     *    <code>0U,3A,2B,3A,7B,2B,3?</code>
     * the agent is trying to select a command that will go where the question
     * mark is.  If the agent selects 'B' as the command it will create a unique
     * episode '3B'.  However, if it selects 'A' as the command, '3A' is hardly
     * unique.  In fact, you must include the previous two episodes (2B and 7B)
     * before it will be a unique sequence.
     *
     * @param cmd command under consideration
     * @param cap if the minimum return value reach this cap then return this
     *            cap value instead
     *
     * @return the number of previous episodes that would have to be included to
     * make the resulting episode part of a unique sequence
     */
    protected int calcUniqueness(int cmd, int cap)
    {
        int uniqueLen = 0;      // return value
        Vector<Episode> level0Eps = this.epmems.elementAt(0);

        //Iterate backwards over all positions in the current episode array
        for(int start = level0Eps.size()-2; start >= 0; start--)
        {
            //If the episode at this position doesn't have the right sensors or
            //command then it's a bust
            ElementalEpisode compEp = (ElementalEpisode)level0Eps.elementAt(start);
            if (compEp.getCommand() != cmd) continue;
            ElementalEpisode rootEp = (ElementalEpisode)level0Eps.elementAt(level0Eps.size() - 1);
            if (! compEp.equalSensors(rootEp)) continue;
            int matchLen = 1; // one match so far

            //Loop until we find a point where it doesn't match anymore
            int offset = 1;
            while(start - offset >= 0)
            {
                compEp = (ElementalEpisode)level0Eps.elementAt(start - offset);
                rootEp = (ElementalEpisode)level0Eps.elementAt(level0Eps.size() - 1 - offset);
                if (! compEp.equals(rootEp)) break;

                matchLen++;
                offset++;
            }
            
            //If this is the longest match we've seen so far, note that
            if (matchLen > uniqueLen)
            {
                uniqueLen = matchLen;

                //Honor the cap given by the caller
                if (uniqueLen >= cap) break;
            }
        }//for

        return uniqueLen;
        
    }//calcUniqueness
    
    
    /**
     * recommendCommand_ViaUniqueness
     *
     * This function selects the command that will create the most unique
     * subsequence of episodes.  Ties are broken randomly.
     *
     * CAVEAT:  This routine assumes that the lowest numbered command is 0.
     *
     */
    protected Recommend recommendCommand_ViaUniqueness()
    {
        int numCmds = this.env.getNumCommands(); // number of commands to select among

        //Start the search from a random command
        int startCmd = randGen.nextInt(numCmds);

        //iterate through all commands and select the one that will yield the
        //most unique series of episodes
        int bestUnique = this.epmems.elementAt(0).size() + 1;
        int bestCmd = startCmd;
        for(int i = 0; i < numCmds; i++)
        {
            int candCmd = (startCmd + i) % numCmds;
            int uniqueLen = calcUniqueness(candCmd, bestUnique);

            //Is this the best so far?
            if (uniqueLen < bestUnique)
            {
                bestUnique = uniqueLen;
                bestCmd = candCmd;

                //We can't do better than zero
                if (bestUnique == 0) break;
            }
        }//for

        //Wholly unique commands yield a maximum degree recommendation.  Other
        //commands do not.  In the future we may want to be less binary than
        //this.  I could see degree using (roughly) this formula:
        //   1.0 - x^U, where x is a number in the range (0..1) and U is bestUnique
        double degree = 0.0;
        if (bestUnique == 0) degree = 1.0;
        this.mon.log("Recommending a semi-random command: "
                     + env.stringify(bestCmd)
                     + "(" + (degree * 100) + "%)");
        return new Recommend(bestCmd, degree);

    }//recommendCommand_ViaUniqueness

    /**
     * recommendCommand_WithPlan
     *
     * This function increments to the next action in the current plan and
     * extracts the associated cmd to return to the caller.  The degree of
     * recommendation is currently equal to the agent's current confidence.
     *
     * CAVEAT: this.currPlan should contain a valid plan that does not need
     *         recalc.
     */
    protected Recommend recommendCommand_WithPlan()
    {
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

        this.mon.log("Recommending command " + env.stringify(cmd)
                     + "(" + (this.selfConfidence * 100) + "%)"
                     + " from this plan:");
        this.mon.log(this.currPlan);

        //Return the selected command to the environment
        return new Recommend(cmd, this.selfConfidence);

    }//recommendCommand_WithPlan
    

}//class Ziggurat
