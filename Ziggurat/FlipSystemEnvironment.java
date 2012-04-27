package Ziggurat;

import java.util.Hashtable;

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
		this.WME_reward = new WME("reward", "0", WME.Type.INT);
	}// *ctor
	
	/**
	 * The agent will always end in State_1
	 */
	private void executeCMD_LEFT() {
		switch(this.currentState) {
		case STATE_1:
			this.WME_reward = new WME("reward", "0", WME.Type.INT);
			break;
		case STATE_2:
			this.WME_reward = new WME("reward", "1", WME.Type.INT);
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
			this.WME_reward = new WME("reward", "1", WME.Type.INT);
			break;
		case STATE_2:
			this.WME_reward = new WME("reward", "0", WME.Type.INT);
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
			this.WME_reward = new WME("reward", "0", WME.Type.INT);
			break;
		case STATE_2:
			this.WME_reward = new WME("reward", "0", WME.Type.INT);
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

    /** convert a given episode to a string */
    public String stringify(Episode ep) {return ep.toString(); }
        
    /** convert a given action to a string */
    public String stringify(Action act) { return act.toString(); }
    
    /** convert a given sequence to a string */
    public String stringify(Sequence seq) { return seq.toString(); }
    
    /** convert a given replacement to a string */
    public String stringify(Replacement repl) { return repl.toString(); }
    
    /** convert a given plan to a string */
    public String stringify(Plan plan) { return plan.toString(); }
	
}// [class] FlipSystemEnvironment
