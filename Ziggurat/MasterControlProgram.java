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
    /** which environment to use */
    private static String envName = "flipsystem";
    
    /** how many rewards to complete before stopping? */
    private static int targetRewards = 100;
    /** the random number seed to use.  The default, -1, indicates not to use a
     * fixed seed */
    private static int seed = -1;          

    /**
     * main
     *
     * Loads the environment specified by the command line and runs it.
     *
     * Valid Arguments (all are optional):
     *   env=<name>   - the name of the environment to use
     *   trials=<num> - the number of rewards the agent should complete
     *   seed=<num>   - a fixed random number seed for this run
     *
     */
	public static void main(String args[]) 
    {
        //Process command line args
        for(String s : args)
        {
            processArg(s);
        }
        
		// Initialize an environment
		Environment env = null;
        env = initEnvironment(envName);
        if(env == null) 
        {
				System.err.println("You requested an invalid environment: " + envName);
                System.err.println("Known environments are:");
				System.err.println("\tflipsystem");
				System.err.println("\tflippredict");
				System.exit(0);
        }// if
		
		// Initialize our agent
		Ziggurat zigg = new Ziggurat(env);

        //Set the random number seed if specified
        if (seed != -1)
        {
            zigg.setRandSeed(seed);
        }

        //%%%CONTROL WITH FLAG LATER
        zigg.setMonitor(new MonitorNull());

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
		else if(name.equals("flippredict")) return new FlipPredictEnvironment();
		else return null;
	}// initEnvironment

    /**
     * processArg
     *
     * parses a command line argument of the form:
     *   name=value
     * See the file header for a list of valid args.  
     */
    private static void processArg(String arg)
    {
        //Split the command
        int equalPos = arg.indexOf("=");
        if ((equalPos == -1) || (equalPos == arg.length() - 1)) return;
        String name = arg.substring(0,equalPos);
        String value = arg.substring(equalPos+1);

        //Handle the command
        if (name.equals("env"))
        {
            envName = value;
        }
        else if (name.equals("trials"))
        {
            try
            {
                int num = Integer.parseInt(value);
                if (num > 0) targetRewards = num;
            }
            catch(NumberFormatException nfe) {}
        }
        else if (name.equals("seed"))
        {
            try
            {
                int num = Integer.parseInt(value);
                if (num >= 0) seed = num;
            }
            catch(NumberFormatException nfe) {}
        }

    }//processArg
    
}// [class] MasterControlProgram
