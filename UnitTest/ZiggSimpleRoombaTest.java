package UnitTest;

import java.util.Vector;
import java.util.Random;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.*;

/**
 * ZiggSimpleRoombaTest
 * 
 * This JUnit test runs a simple roomba-like environment for a few steps and
 * verifies the right structure is created.
 */
public class ZiggSimpleRoombaTest
{
    /**
     * This class is used to replace the normal random number generator so we can
     * create a fixed random number seed and thus get repeatable results.
     */
    public class FixedSeedRandom extends Random
    {
        public FixedSeedRandom()
        {
            super();
            this.setSeed(42);
        }
    }//class FixedSeedRandom

    /**
     * This class is a simplified Roomba Environment that simulates the
     * two-square maze with 45-degree turns and no extra sensors.
     */
    public class SimplifiedRoombaEnvironment extends Environment
    {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int FORWARD = 2;

        /* which direction is the roomba facing?  It begins by facing north.
         *               6
         *             5   7
         *            4  ^  0
         *             3   1
         *               2
         */
        private int facing = 6;
    
        public WMESet takeStep(int cmd)
        {
            //default sensor values
            String left = "0";
            String right = "0";
            String goal = "0.0";
            
            if (cmd == LEFT)
            {
                facing = (facing + 7) % 8;
            }
            else if (cmd == RIGHT)
            {
                facing = (facing + 1) % 8;
            }
            //At this point assume FORWARD
            else if (facing == 0)
            {
                facing = 6;     // reset for next run
                goal = "1.0";
            }
            else if (facing == 1)
            {
                right = "1";
            }
            else if (facing == 7)
            {
                left = "1";
            }
            else
            {
                right = "1";
                left = "1";
            }

            //build the WMESet with the sensor values
            String[] sensorsArr = {"left", left, "right", right, WME.REWARD_STRING, goal};
            return new WMESet(WMESet.makeSensors(sensorsArr));
        }//takeStep

        public WMESet generateCurrentWMESet()
        {
            String[] sensorsArr = {"left", "0", "right", "0", WME.REWARD_STRING, "0.0"};
            return new WMESet(WMESet.makeSensors(sensorsArr));
        }
        
        public int getNumCommands() { return 3; }

        public String stringify(int cmd)
        {
            if (cmd == LEFT) return "L";
            if (cmd == RIGHT) return "R";
            return "F";
        }
                

        public String stringify(Episode ep)
        {
            if (ep instanceof SequenceEpisode)
            {
                return ep.toString();
            }

            ElementalEpisode elEp = (ElementalEpisode)ep;
            int sensorSum = 0;
            if (elEp.getSensors().getAttr("left").getInt() == 1)
            {
                sensorSum += 2;
            }
            if (elEp.getSensors().getAttr("right").getInt() == 1)
            {
                sensorSum += 1;
            }
            String sensorString = "" + sensorSum;
            if (elEp.getSensors().getAttr(WME.REWARD_STRING).getDouble() > 0.0)
            {
                sensorString = "G";
            }

            return sensorString + stringify(elEp.getCommand());
        }//stringify episode

        public String stringify(Action act)
        {
            String result = stringify(act.getLHS()) + "->";
            Episode ep = act.getRHS();
            result += stringify(ep);
            if (ep instanceof ElementalEpisode)
            {
                result = result.substring(0, result.length() - 1);
            }

            return result;
        }//stringify Action

        public String stringify(Sequence seq)
        {
            String result = "[";
            boolean first = true;
            Vector<Action> actions = seq.getActions();
            for(Action a : actions)
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    result += ", ";
                }
                
                result += stringify(a);
            }
            result += "]";
            
            return result;
        }
        
    }//class SimplifiedRoombaEnvironment

    
	// BEGIN Test cases --------------------------------------

	@Test
	public void test_Zigg()
    {
        //Create a Zigg
        Environment env = new SimplifiedRoombaEnvironment();
        Ziggurat zigg = new Ziggurat(env);
        zigg.setRandGen(new FixedSeedRandom());
//%%%        zigg.setMonitor(new MonitorNull(env));
        
        //Run enough ticks to make a sizeable memory
        WMESet sensors = env.generateCurrentWMESet();
        for(int i = 0; i < 8; i++)
        {
            int cmd = zigg.tick(sensors);
            sensors = env.takeStep(cmd);
        }


        // //Verify that only two level-0 sequences exist (one will be empty)
        // Vector<Vector<Sequence>> seqs = zigg.getSequences();
        // assertTrue(seqs.size() == 2);
        // assertTrue(seqs.elementAt(0).size() == 2);

        // //Verify the sequences are the right size
        // Sequence seq1 = seqs.elementAt(0).elementAt(0);
        // Sequence seq2 = seqs.elementAt(0).elementAt(1);
        // assertTrue(seq1.length() == 7);
        // assertTrue(seq2.length() == 0);

        Vector<Vector<Episode>> epmems = zigg.getEpmems();
        // assertTrue(epmems.size() == 2);
        // assertTrue(epmems.elementAt(1).size() == 1);
        // SequenceEpisode seqEp = (SequenceEpisode)epmems.elementAt(1).elementAt(0);
        // assertTrue(seqEp.getSequence() == seq1);
        
	}//test_OneSequence

	// // BEGIN Test cases --------------------------------------

    /**
     * An easy way to run this test individually from the command line without
     * the JUnit jar file
     */
    public static void main(String[] args)
    {
        ZiggSimpleRoombaTest zsrt = new ZiggSimpleRoombaTest();
        zsrt.test_Zigg();
    }
	
}//class ZiggSimpleRoombaTest
