package Ziggurat;
 
import java.util.*;

/**
 * class Ziggurat
 *
 * This state contains the main learning algorithm for Ziggurat
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
    public static int MAX_LEVELS = 4;

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
    private double selfCOnfidence = INIT_SELF_CONFIDENCE;
    /** the current environment provides the agent with a limited amount of
        information about itself */
    private Environment env = null;
    /** the monitor that is currently logging events in Zigg */
    private Monitor mon = null;


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
        this.mon = new Monitor(env);
        
    }//ctor


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
        //%%%TBD: updateAll(0);

        
        // If we receive a reward, update the memory to reflect this
        double reward = this.env.currReward();
        if(reward > 0.0)
        {
            this.mon.reward(reward);
       
            //If a a plan is in place, reward the agent and any outstanding replacements
            if (this.currPlan != null)
            {
                //%%%TBD: rewardReplacements();
                //%%%TBD: rewardAgent();
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

}//class Ziggurat


 
