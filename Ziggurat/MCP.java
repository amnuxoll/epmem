package Ziggurat;

import java.util.Arrays;

/**
 * <!-- class MCP -->
 *
 * This is the start point for training Ziggurat.  It create an Environment and
 * an instance of Ziggurat and connects them together.  (MCP stands for "Master
 * Control Program".)  It has a short name because I'll be typing it in a lot.
 *
 * Follow these steps to install a new environment:
 * 1. Update validEnvStrs to include a new id string for your environment
 * 2. Update initEnvironment to include a new else-clause for your environment
 *
 */
public class MCP 
{
    /** which environment to use */
    private static String envName = "flipsystem";
    
    /** how many rewards to complete before stopping? */
    private static int targetRewards = 100;
    /** the random number seed to use.  The default, -1, indicates not to use a
     * fixed seed */
    private static int seed = -1;
    /** this boolean is set to indicate that Zigg should use a null monitor.
     * This is usually used to maximize the speed of data gathering.
     */
    private static boolean nullMonitor = false;

    /** A list of the names of valid environments.  Please keep this up to date! */
    private static String[] validEnvStrs = { "flipsystem", "flippredict", "roomba" };

    /**
     * main
     *
     * Loads the environment specified by the command line and runs it.
     *
     * See the USAGE message below for a list of valid arguments.  All arguments
     *   are optional but at least one argument (even if bogus) must be
     *   specified to prevent the usage message from printing.
     *
     */
	public static void main(String args[]) 
    {
        //If there are no arguments, then print a usage message and exit
        if (args.length == 0)
        {
            System.out.println("USAGE:");
            
            System.out.println("\tenv=<name>   - the name of the environment to use");
            System.out.println("\t               current known envs: " + Arrays.asList(validEnvStrs));
            System.out.println("\ttrials=<num> - the number of rewards the agent should complete");
            System.out.println("\tseed=<num>   - a fixed random number seed for this run");
            System.out.println("\tmon=null     - use a MonitorNull object");

            return;
        }
        
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
                System.err.println("Known environments are: " + Arrays.asList(validEnvStrs));
				System.exit(0);
        }// if
		
		// Initialize our agent
		Ziggurat zigg = env.createAgentForEnvironment();

        //Set the random number seed if specified
        if (seed != -1)
        {
            zigg.setRandSeed(seed);
        }

        //If instructed to do so, turn off all monitor output
        if (nullMonitor)
        {
            zigg.setMonitor(new MonitorNull());
        }

		WMESet currentSensors = env.generateCurrentWMESet();
        int numRewards = 0;
        while(numRewards < targetRewards)
        {
			// Capture new sensor data resulting from the command
			// Ziggurat sent to the environment based on the previous
			// sensor data.
        	int action = zigg.tick(currentSensors);
			currentSensors = env.takeStep(action);

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
		else if(name.equals("roomba")) return new RoombaEnvironment();
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
        else if (name.equals("mon"))
        {
            if (value.equals("null"))
            {
                nullMonitor = true;
            }
        }

    }//processArg
    
}// [class] MCP
