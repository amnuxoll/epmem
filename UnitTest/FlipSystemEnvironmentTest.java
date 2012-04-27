package UnitTest;

import java.util.Hashtable;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.FlipSystemEnvironment;
import Ziggurat.WME;
import Ziggurat.WMESet;

public class FlipSystemEnvironmentTest {
	public static FlipSystemEnvironment fs = new FlipSystemEnvironment();
	
	public static WMESet generateWinWMESet() {
		WME wme = new WME("reward", "1", WME.Type.INT);
		Hashtable<String, WME> rtnVal = new Hashtable<String, WME>();
		rtnVal.put(wme.attr, wme);
		
		return new WMESet(rtnVal);
	}// genereateWinWMESet
	
	public static WMESet generateLoseWMESet() {
		WME wme = new WME("reward", "0", WME.Type.INT);
		Hashtable<String, WME> rtnVal = new Hashtable<String, WME>();
		rtnVal.put(wme.attr, wme);
		
		return new WMESet(rtnVal);
	}// generateLoseWMESet
	
	@Test
	public void test_generateCurrentWMESet() {
		WMESet wmeset = fs.generateCurrentWMESet();
		WMESet toTest = this.generateLoseWMESet();
		
		assertTrue(wmeset.equals(toTest));
	}
	
	@Test
	public void test_getNumCommands() {
		assertTrue(fs.getNumCommands() == 3);
	}
	
	// From here we test to see if we receive the expected 
	// result given a sequence of commands
	
	@Test
	public void test_takeStep() {
		// 3 commands:
		//  0 - left
		//  1 - right
		//  2 - up
		
		// 2 states:
		//  1 - left  = reward:0
		//  1 - right = reward:1
		//  1 - up 	  = reward:0
		//  2 - left  = reward:1
		//  2 - right = reward:0
		//  2 - up 	  = reward:0
		
		// Init state: 1
		// We've already tested that the initial senses are correct
		
		WMESet senses = null;
		int command = 0; //left
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateLoseWMESet()));
		
		command = 2; // up
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateLoseWMESet()));
		
		command = 1; // right
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateWinWMESet()));
		
		// Have entered state 2
		
		command = 1; // right
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateLoseWMESet()));
		
		command = 2; // up
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateLoseWMESet()));
		
		command = 0; // left
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateWinWMESet()));
		
		// back in state 1
		
		command = 0; //left
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateLoseWMESet()));

		command = 1; // right
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateWinWMESet()));
		
		// back in state 2

		command = 0; // left
		senses = fs.takeStep(command);
		assertTrue(senses.equals(this.generateWinWMESet()));
		
		// back in state 1
	}// test_takeStep
	
}
