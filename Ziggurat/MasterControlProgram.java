package Ziggurat;

/**
 * <!-- class MasterControlProgram -->
 *
 * This is the start point for training Ziggurat.  It create an Environment and
 * an instance of Ziggurat and connects them together.
 *
 *
 */
public class MasterControlProgram 
{
    /**
     * main
     *
     * Loads the environment specified by the command line and runs it.
     */
	public static void main(String args[]) 
    {
        //how many rewards to complete before stopping?
        int targetRewards = 100;
    
		// Initialize an environment
		Environment env = null;
		if(args.length == 0) 
        {
			env = new FlipSystemEnvironment();
		} else 
        {
			initEnvironment(args[0]);
		
			if(env == null) 
            {
				System.out.println("You requested an invalid environment: " + args[0] 
                                   + "\nKnown environments are:\n");
				System.out.println("flipsystem");
				System.exit(0);
			}// if
		}// else]
		
		// Initialize our agent
		Ziggurat zigg = new Ziggurat(env);

        //%%%DEBUG!
        zigg.setRandSeed(30);

		WMESet currentSensors = env.generateCurrentWMESet();
        int numRewards = 0;
        while(numRewards < targetRewards)
        {
			// Capture new sensor data resulting from the command
			// Ziggurat sent to the environment based on the previous
			// sensor data.
			currentSensors = env.takeStep(zigg.tick(currentSensors));

            //Stop after N goals
            WME w = currentSensors.getAttr(WME.REWARD_STRING);
            if (w.getDouble() > 0.0) numRewards++;
		}
	}//main
	
	/**
     * Given some predetermined environment names, initialize the 
	 * correct type. Null if none match.
     *
     * @param name   the name of the environment to use
     */
	private static Environment initEnvironment(String name) 
    {
		if(name.equals("flipsystem")) return new FlipSystemEnvironment();
		else return null;
	}// initEnvironment
}// [class] MasterControlProgram
