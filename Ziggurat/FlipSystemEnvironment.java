package Ziggurat;

import java.util.Hashtable;

/**
 * <!-- class FlipSystemEnvironment -->
 *
 * This a very simple environment that is drawn from:<br>
 *
 * Michael Holmes and Charles L. Isbell. Schema Learning: Experience-based
 * Construction of Predictive Action Models. In Advances in Neural Information
 * Processing Systems (NIPS) 17, pages 585-562, 2005.
 * 
 * <p>There are two states and three commands ( left, right and up ).  Moving
 * right from state 1 puts you in state 2.  Moving left from state 2 puts you in
 * state 1.  All other commands return you to the same state.  Whenever you
 * change states you get a reward.
 *
 */
public class FlipSystemEnvironment extends Environment {

	// Define possible states
	private enum State { STATE_1, STATE_2 };
	private State currentState;
	
	// Define command related variables
	private final int CMD_LEFT 		= 0;
	private final int CMD_RIGHT 	= 1;
	private final int CMD_UP 		= 2;
	private final int NUM_COMMANDS 	= 3;
	
	// Define sense related variables
	private WME WME_reward;
	
	public FlipSystemEnvironment() {
		this.currentState = State.STATE_1;
		this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
	}// *ctor
	
	/**
	 * The agent will always end in State_1
	 */
	private void executeCMD_LEFT() {
		switch(this.currentState) {
		case STATE_1:
			this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
			break;
		case STATE_2:
			this.WME_reward = new WME("reward", "1.0", WME.Type.DOUBLE);
			break;
		}
		this.currentState = State.STATE_1;
	}// executeCMD_LEFT
	
	/**
	 * The agent will always end in State_2
	 */
	private void executeCMD_RIGHT() {
		switch(this.currentState) {
		case STATE_1:
			this.WME_reward = new WME("reward", "1.0", WME.Type.DOUBLE);
			break;
		case STATE_2:
			this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
			break;
		}
		this.currentState = State.STATE_2;
	}// executeCMD_RIGHT
	
	/**
	 * At no point will "up" produce a reward.
	 * The agent will remain in the current state.
	 */
	private void executeCMD_UP() {
		switch(this.currentState) {
		case STATE_1:
			this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
			break;
		case STATE_2:
			this.WME_reward = new WME("reward", "0.0", WME.Type.DOUBLE);
			break;
		}
	}// executeCMD_UP
	
	// Completed abstract Environment methods ************************
	
	/**
	 * take a command and apply it to the environment.
	 * return the resulting WMESet
	 */
	public WMESet takeStep(int commandIndex) {
		// Execute appropriate command
		switch(commandIndex) {
		case CMD_LEFT: 	executeCMD_LEFT(); 		break;
		case CMD_RIGHT: executeCMD_RIGHT(); 	break;
		case CMD_UP: 	executeCMD_UP(); 		break;
		default: 		return null;	// Invalid command...
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
	public WMESet generateCurrentWMESet() {
		// Generate and fill the hashtable for our return value
		Hashtable<String, WME> rtnVals = new Hashtable<String, WME>();
		rtnVals.put(this.WME_reward.attr, this.WME_reward);
		
		return new WMESet(rtnVals);
	}// generateCurrentWMESet
	
	/** return the number of available commands in this environment */
    public int getNumCommands() { return NUM_COMMANDS; }

}// [class] FlipSystemEnvironment
