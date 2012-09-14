package Ziggurat;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Random;


/**
 * <!-- class FlipPredictEnvironment -->
 *
 * This is a modified version of {@link FlipSystemEnvironment} wherein the agent
 * tries to predict the reward that will be received for a random action.  In
 * effect, the Left/Right/Up action becomes a feature of the state space and the
 * agent's actions become Reward=0 or Reward=1.
 *
 */
public class FlipPredictEnvironment extends Environment
{
    Random randGen = new Random();

	// Define possible states
	private enum State { STATE_1, STATE_2 };
	private State currentState;
    private int currentDirection;
	
	// Define command related variables
	private static final int DIR_LEFT 		 = 0;
	private static final int DIR_RIGHT 	 = 1;
	private static final int DIR_UP 		 = 2;
    private static final int NUM_DIRECTIONS = 3;

	// Define sense related variables
	private WME WME_reward;
	private WME WME_direction;
	
	public FlipPredictEnvironment()
    {
		this.currentState = State.STATE_1;
        this.currentDirection = randGen.nextInt(NUM_DIRECTIONS);
		this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
        this.WME_direction = new WME("dir", "" + this.currentDirection, WME.Type.INT);
	}// ctor

    /**
     * a handy debugging method for printing the current state and command
     * in human readable format.
     */
    private void printState(int cmd)
    {
        switch(this.currentState)
        {
            case STATE_1:  
                System.out.print("In State ONE ");
                break;
            case STATE_2:
                System.out.print("In State TWO ");
                break;
            default:
                System.out.print("In State UNKNOWN!! ");
                break;
        }//switch                    
            
        switch(currentDirection)
        {
            case DIR_LEFT:
                System.out.print(" moving LEFT");
                break;
            case DIR_RIGHT:
                System.out.print(" moving RIGHT");
                break;
            case DIR_UP:
                System.out.print(" moving UP");
                break;
            default:
                System.out.print(" moving ????");
                break;
        }//switch

        System.out.println(" expecting reward of " + cmd);
        
    }//printState

	/**
	 * take a command and apply it to the environment.
	 * return the resulting WMESet
	 */
	public WMESet takeStep(int expOutcome)
    {
        printState(expOutcome);

        //Calculate the reward the agent will actually get at this step
        //and update the current state as appropriate
        int actualOutcome = 0;
        if ( (this.currentState == State.STATE_1)
             && (this.currentDirection == DIR_RIGHT) )
        {
            this.currentState = State.STATE_2;
            actualOutcome = 1;
        }
        else if ( (this.currentState == State.STATE_2)
               && (this.currentDirection == DIR_LEFT) )
        {
            this.currentState = State.STATE_1;
            actualOutcome = 1;
        }

        //Calculate a new direction for the agent
        this.currentDirection = randGen.nextInt(NUM_DIRECTIONS);
        this.WME_direction = new WME("dir", "" + this.currentDirection, WME.Type.INT);

        //Calculate the agent's new reward sensors
        if (expOutcome == actualOutcome)
        {
            this.WME_reward = new WME("reward", "1.0", WME.Type.DOUBLE);
            System.out.println("predict error 0");
        }
        else
        {
            this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
            System.out.println("predict error 1");
        }

             
        
		// Generate return data
		return generateCurrentWMESet();
	}// takeStep
	
	/**
	 * get an initial WMESet config indicating we're beginning
	 * in a brand new environment.
	 * If this is the first time then takeStep has never been called, 
	 * so our WMEs are the current initialized values from *ctor.
	 * Else we have update values to return.
	 */
	public WMESet generateCurrentWMESet()
    {
		// Generate and fill the hashtable for our return value
		Hashtable<String, WME> rtnVals = new Hashtable<String, WME>();
		rtnVals.put(this.WME_reward.attr, this.WME_reward);
		rtnVals.put(this.WME_direction.attr, this.WME_direction);
		
		return new WMESet(rtnVals);
	}// generateCurrentWMESet
	
	/** return the number of available commands in this environment */
    public int getNumCommands() { return 2; }

    @Override
    /** the command is a prediction that I will either 1 (get a reward) or 0 (won't). */
    public String stringify(int cmd)
    {
        if ((cmd < 0) || (cmd > 1)) return "?";
        
        return "" + cmd;
    }

    @Override
    /**
     * converts an episode's sensors into a unique combination of two
     * characters
     *
     * 1 or 0   - reward
     * L,R or U - direction
     */
    public String stringify(WMESet sensors)
    {
        //Calculate the reward character
        String sensorString = "0";
        if (sensors.getAttr(WME.REWARD_STRING).getDouble() > 0.0)
        {
            sensorString = "1";
        }

        //Calculate the direction character

        int dir = sensors.getAttr("dir").getInt();
        switch(dir)
        {
            case DIR_UP:
                sensorString += 'U';
                break;
            case DIR_LEFT:
                sensorString += 'L';
                break;
            case DIR_RIGHT:
                sensorString += 'R';
                break;
            default:
                sensorString += '?';
                break;
        }

        return sensorString;
    }//stringify ElementalEpisode

    
}// [class] FlipPredictEnvironment
