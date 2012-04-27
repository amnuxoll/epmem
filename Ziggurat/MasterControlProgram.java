package Ziggurat;

public class MasterControlProgram {
	
	public static void main(String args[]) {
		// Initialize an environment and our agent
		Environment env = initEnvironment(args[0]);
		if(env == null) {
			System.out.println("You requested an invalid environment: " + args[0] 
			                 + "\nKnown environments are:\n");
			System.out.println("flipsystem");
			System.exit(0);
		}
		Ziggurat zigg = new Ziggurat(env);

		WMESet currentSensors = env.generateCurrentWMESet();
		while(true /*%%% update later*/) {
			// Capture new sensor data resulting from the command
			// Ziggurat sent to the environment based on the previous
			// sensor data.
			currentSensors = env.takeStep(zigg.tick(currentSensors));
		}
	}//main
	
	// Given some predetermined environment names, initialize the 
	// correct type. Null if none match.
	private static Environment initEnvironment(String name) {
		if(name.equals("flipsystem")) return new FlipSystemEnvironment();
		else return null;
	}// initEnvironment
}// [class] MasterControlProgram
