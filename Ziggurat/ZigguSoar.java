package Ziggurat;

public class ZigguSoar extends Ziggurat {
	
	
	public ZigguSoar(Environment env)
	{
		super(env);
	}

	
	/**
     * tick
     *
     * is normally called by the Environment at each time step so that 
     * Zigg can learn and decide it's next action.
     *
     * HERE, however, the environment is a stub that is communicating with Soar
     * and the agent (that is that part of the agent with agency) is Soar.
     * 
     * The model, thus is a bit different as a result of this indirection.
     * 
     * We IGNORE the sensors value passed in, and instead
     * as the environment to run soar one step.
     * 
     * This allows the environment to get a new state/action/reward information
     * that we can use to update the episodic memory.
     * 
     * @param sensors -- ignored!
     */
	@Override
    public int tick(WMESet sensors)
    {
        this.mon.enter("tick");
        System.out.println("Stepping Soar...");
        sensors = ((SoarListenerEnvironment)env).stepSoar();
        // now, sensors contains the agent's last perceived state
        // env.commandID contains a numeric ID of the *current* action (still to be executed)
        // sensors also contains reward information from the previous action
        
        
        System.out.println("Got sensors: " + sensors.toString() );
        
        // Create new Episode and update the hierarchy with it
        ElementalEpisode ep = new ElementalEpisode(sensors);
        ep.setCommand(((SoarListenerEnvironment)env).commandID);
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
        //int cmd = chooseCommand();
        //ep.setCommand(cmd);

        // we'll still call choose command since it does the state recognition
        chooseCommand();
        
        
        // in soar, we don't actually have control of this, we're just
        // recording what happens and speculating on "where" we are
        // in a historical trajectory
        int cmd = ((SoarListenerEnvironment)env).commandID;

        //Log the resulting episode
        this.mon.logPart("Using command " + env.stringify(cmd) + " to complete episode #" + (epmems.size()-1) + ":  ");
        this.mon.log(ep);

        //Return the result
        this.mon.exit("tick");
        return cmd;
	
    }//tick

}
