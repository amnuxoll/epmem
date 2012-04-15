package Ziggurat;
 
import java.util.*;

/**
 * class Ziggurat
 *
 * This state contains the main learning algorithm for Ziggurat.  For an
 * overview of the algorithm please review:
 *
 * Zachary Faltersack, Brian Burns, Andrew Nuxoll and Tanya
 * L. Crenshaw. Ziggurat: Steps Toward a General Episodic Memory. AAAI Fall
 * Symposium Series: Advances in Cognitive Systems, 2011.
 *
 * If you are new to this source code start by looking at the classes for major
 * entities: {@link Action}, {@link Episode} and {@link Sequence}.  In this
 * class, the following methods are fundamental: {@link #tick}, {@link #update}
 * and {@link #findRoute}.
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
    public static int MAX_LEVEL_DEPTH = 4;

    /** initial self confidence value */
    public static double INIT_SELF_CONFIDENCE = 0.5;
    /** maximum confidence value */
    public static double MAX_CONFIDENCE = 1.0;
    /** minimum confidence value */
    public static double MIN_CONFIDENCE = 0.0;

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
    /** this is the highest level in the hierarchy that contains data */
    /* %%% Do we still need this? */
    private int lastUpdateLevel = 0;
    /** this vector contains all @link{DecisionElement}s that have recently been
     * used to make a decision.  When the outcome of decision(s) is known these
     * active elements' utlities are adjusted based upon that outcome.
     */
    private Vector<DecisionElement> activeDecEls = new Vector<DecisionElement>();


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
        }//if

        //Select the agent's next action
        int cmd = 44;//%%%TBD: chooseCommand();
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
     * update                    *RECURSIVE*
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
            this.mon.log("Exiting update(): level %d out of range\n", level);
            return -3;
        }

        //Log the current state of the hierarchy at this level
        this.mon.log("Level %d Episodes >>>>>>>>>>>>>>>>>>>>>>>>>>>\n", level);
        this.mon.log(epmems.elementAt(level));
        this.mon.log("Level %d Actions >>>>>>>>>>>>>>>>>>>>>>>>>>>\n", level);
        this.mon.log(actions.elementAt(level));
        this.mon.log("Level %d Sequences >>>>>>>>>>>>>>>>>>>>>>>>>>>\n", level);
        this.mon.log(seqs.elementAt(level));
        this.mon.log("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< End of Level %i Data\n", level);

        // Create pointers to the two associated vectors we'll be working with
        Vector<Episode> episodeList = this.epmems.elementAt(level);
        Vector<Action> actionList = this.actions.elementAt(level);
        Vector<Sequence> sequenceList = this.seqs.elementAt(level);

        // You need a minimum of two episodes to make an action
        if(episodeList.size() <= 1)
        {
            this.mon.log("\tExiting update(): insufficient episodes (%d) at level %d\n",
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
            
            // set this flag so that we recursively update the next level
            // with this action
            updateExistingAction = newAction;
        }

        //%%%SANITY CHECK
        if(updateExistingAction == null)
        {
            this.mon.log("ERROR:  I'm insane!");
            return -5;
        }
        
        //Bump the frequency for this new (or re-used) action
        updateExistingAction.incrementFreq();
        
        //Log that an update was completed at this level
        this.lastUpdateLevel = level;

        // add most recently seen action to current sequence
        Sequence currSequence = sequenceList.elementAt(sequenceList.size() - 1);
        currSequence.add(updateExistingAction);
        this.mon.log("Adding action #%d: ", sequenceList.size() - 1);
        this.mon.addTempIndent();
        this.mon.log(updateExistingAction);
        this.mon.log(" to current sequence:");
        this.mon.addTempIndent();
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
                this.mon.addTempIndent();
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
   
        this.mon.log("to %g\n", this.selfConfidence);
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
   
        this.mon.log("to %g\n", this.selfConfidence);
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


    
}//class Ziggurat


 
