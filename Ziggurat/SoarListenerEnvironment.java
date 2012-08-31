package Ziggurat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import sml.Agent;
import sml.Kernel;
import sml.smlUpdateEventId;

/**
 * ZigguratSoarListener: Listens to a Soar agent interact with an Environment via SML.
 * Assumes that:
 *  1) the environment is running on the same machine as this Listener
 *  2) there is only one Soar environment running
 *  3) the environment has only 1 agent
 *  
 * How to use:
 *  - Launch the environment
 *  - Launch this Listener
 *  - (optionally) Launch Soar Debugger, select Kernel->'Connect to Remote Soar' select default dialog options
 *  - (optionally) If using the Soar Debugger, set the "stop point" between output and input:
 *      set-stop-phase --before --input
 *      this will ensure that the debugger and SnoopDog are viewing the agent's memory at the same point in time
 *  - step through the environment with the debugger (or whatever)
 *  - NOTE that in ToH if you're stepping through with the debugger set as above, the environment look like
 *      it's a half phase ahead -- that's because it's not actually waiting the for input phase to update the world view...
 * 
 *
 * Interaction with Soar is a bit at-odds with the standard Ziggurat model.
 * 
 * Normally, Ziggurat flow would looks like this:
 *   - start the MCP
 *   - MCP creates environment
 *   - MCP asks environment to create Agent
 *   - MCP gets state 0 from environment
 *   - MCP tells agent state 0 and gets action
 *   - MCP gets state 1 from environment
 *   etc.
 *   
 *   Here, however, the 'external' Soar connection contains elements of
 *   both the agent AND the environment, so the distinction for us is not
 *   particularly clean -- at least with the callbacks that I currently use.
 *   As a result, the model here looks somewhat different:
 *   
 *    SoarEventListener:
 *      responsibilities:
 *        - maintain links to Soar Kernel, and Agent
 *        - read Agent input/output and reward links
 *        - make state/action/reward data available for other consumers (e.g., ZigguSoar agent)
 *        
 *    ZigguSoar:
 *      responsibilities:
 *        - perform episodic learning ala Ziggurat
 *        
 * @author wallaces
 *
 */
public class SoarListenerEnvironment extends Environment implements Kernel.UpdateEventInterface {

	private static Pattern SOAR_WME_PATTERN = Pattern.compile("\\((\\d+): (\\w+) \\^(\\S+) (\\S+)\\)");

	Kernel k;
	Agent agent;

	private String stateAsString;
	private String actionAsString;
	private String lastRewardAsString;
	int commandID;
	WMESet sensors;

	// this maps actions (a set of command features) to unique ids (integers)
	HashMap<Set<String>, Integer> action_id_map;
	HashMap<Integer, Set<String>> id_action_map;

	/*
	 * Create the Listener Environment and attach to a running soar process.
	 *  
	 */
	public SoarListenerEnvironment() {		
		k = Kernel.CreateRemoteConnection();
		if (k.HadError()) {
			System.err.println("Couldn't connect to a remote kernel, is the environment running?");
			System.err.println(k.GetLastErrorDescription());
			System.exit(0);
		}
		System.out.println("Connected to environment!");
		k.RegisterForUpdateEvent(smlUpdateEventId.smlEVENT_AFTER_ALL_GENERATED_OUTPUT, this, null) ;
		agent = k.GetAgentByIndex(0);
		System.out.println("Listening to the stream from agent: '" + agent.GetAgentName() + "'...");
		stateAsString = null;
		actionAsString = null;

		action_id_map = new HashMap<Set<String>,Integer>(500);
		id_action_map = new HashMap<Integer, Set<String>>(500);

	}

	/** This method is called when the "after_all_output_phases" event fires, at which point we update the world */
	public void updateEventHandler(int eventID, Object data, Kernel kernel, int runFlags)
	{
		try
		{

			// get the state...
			String state = agent.ExecuteCommandLine("print --depth 3 --internal i2").trim();
			System.out.println("reading tokens from input link data... '"+state+"'");

			// now do a similar thing for the action command....
			String action = agent.ExecuteCommandLine("print --depth 3 --internal i3").trim();
			System.out.println("reading tokens from output link data... '" + action + "'");

			String lr = agent.ExecuteCommandLine("print --depth 3 --internal r1").trim();
			System.out.println("reading tokens from reward link (for previous action)... '" + lr + "'");
			synchronized(this) {
				stateAsString = state;
				actionAsString = action;
				lastRewardAsString = lr;
			}

		} catch (Exception e) {
			System.err.println(e.toString());
			e.printStackTrace(System.err);
		}
	}


	@Override
	/*
	 *  returns the previously acquired sensor data
	 *  
	 * @see Ziggurat.Environment#takeStep(int)
	 */
	public WMESet takeStep(int commandIndex) {
		return sensors;
	}
	
	/**
	 * Ask Soar to Run until output.
	 * This will let us acquire new state/action/reward information
	 * 
	 * 'actions' here are tokenized so that Soar output link commands
	 *   are mapped to unique integers.  
	 * 
	 * @return a WMESet representing the current sensor information
	 */
	public WMESet stepSoar() {
		commandID = -1;
		sensors = null;
		System.out.println("You asked Soar to take a step....");

		// I thought this should be doable with just
		// a volatile String , but when 
		// I remove the synchronized blocks, I get a never-ending
		// series of 'wait...' messages....
		// I should look into this....
		synchronized(this) {
			actionAsString = null;
		}
		agent.RunSelfTilOutput();
		while( true ) {
			synchronized(this) {
				if (actionAsString != null) break;
			}
			try {
				System.out.println("waiting...");
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		synchronized(this) {
			// in theory, I shouldn't need a synchronized block here since
			// soar has run its cycle and it should be waiting so there should
			// be no thread contention for the shared variables.
			System.out.println("State is: " + stateAsString);
			System.out.println("Command is:" + actionAsString);
			// parse & tokenize the actionString
			commandID = getCommandId(actionAsString);
			sensors = getWMESetFromInputLinkString(stateAsString);
		}
		return sensors;
	}
	private WMESet getWMESetFromInputLinkString(String stateAsString) {
		Hashtable<String, WME> state = new Hashtable<String, WME>(40);
		Matcher m = SOAR_WME_PATTERN.matcher(stateAsString);

		String attr, val;
		int lastMatch = 0;

		while( m.find(lastMatch) ) {
			//tt = m.group(1);
			//root = m.group(2);
			attr = m.group(3);
			val = m.group(4);
			lastMatch = m.end();

			// take all the attribute value pairs and put them in
			// the representation
			// for now, EVERYTHING's a STRING
			state.put(attr, new WME(attr, val, WME.Type.STRING)); 

			// also put that dang REWARD thing on there...
			if (attr.equals("see"))
				state.put(WME.REWARD_STRING, new WME(WME.REWARD_STRING, val, WME.Type.DOUBLE));

		}
		return new WMESet(state);

	}
	/**
	 * Look up a command and return it's integer id.
	 * @param cmdset
	 * @return
	 */
	private int getCommandId(String cmdAsString) {
		
		Set<String> cmdset = new HashSet<String>(10);
		Matcher m = SOAR_WME_PATTERN.matcher(cmdAsString);

		String attr, val;
		int lastMatch = 0;

		while( m.find(lastMatch) ) {
			attr = m.group(3);
			val = m.group(4);
			lastMatch = m.end();

			// BUG BUG we just take the attribute name
			// this works fine if the command has no parameters...
			cmdset.add(attr);
			
			if (attr.equals("complete"))
				System.err.println("WARNING: status complete command read.");
		}
		if (cmdset.size() == 0) return 0;

		Integer actionid = action_id_map.get(cmdset);
		if (actionid != null) {
			System.out.println(" ++ This is action ID " + actionid  + "  (" + action_id_map.size() + " known actions) ++");
		}
		else {
			System.out.println(" ++ This action is new! (" + action_id_map.size() + " actions previously seen) ++");
			actionid = 100 + action_id_map.size(); // add 100 so it's easily distinguished from a state id
			action_id_map.put(cmdset, actionid);
			id_action_map.put(actionid, cmdset);
		}
		return actionid;
	}

	
	@Override
	public WMESet generateCurrentWMESet() {
		// TODO Auto-generated method stub

		agent.ExecuteCommandLine("init-soar");
		return new WMESet(new Hashtable<String, WME>());
	}

	@Override
	public int getNumCommands() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Ziggurat createAgentForEnvironment() {
		return new ZigguSoar(this);
	}
	
    public String stringify(int cmdID)
    {
    	Set<String> set = id_action_map.get(cmdID);
    	String cmdName = "";
    	if (set == null) {
    		cmdName = "?";
    	}
    	else {
    		cmdName = set.iterator().next();
    	}
    	return "(cmd: " + cmdID + ":" + cmdName + ") ";
    }//stringify Action

}
