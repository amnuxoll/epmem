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
 * verifies that the right structure is created.
 */
public class ZiggSimpleRoombaTest
{
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
         *            4  ^  0  -> Goal
         *             3   1
         *               2
         */
        private int facing = 6;

        /**
         * a handy debugging method for printing the current state and command
         * in human readable format.
         */
        private void printState(int cmd)
        {
            System.out.print("Facing ");
            switch(this.facing)
            {
                case 0:  
                    System.out.print("EAST");
                    break;
                case 1:  
                    System.out.print("SOUTHEAST");
                    break;
                case 2:  
                    System.out.print("SOUTH");
                    break;
                case 3:  
                    System.out.print("SOUTHWEST");
                    break;
                case 4:  
                    System.out.print("WEST");
                    break;
                case 5:  
                    System.out.print("NORTHWEST");
                    break;
                case 6:  
                    System.out.print("NORTH");
                    break;
                case 7:  
                    System.out.print("NORTHEAST");
                    break;
                default:  
                    System.out.print("?????");
                    break;
            }//switch

            switch(cmd)
            {
                case LEFT:
                    System.out.println(" turning LEFT");
                    break;
                case RIGHT:
                    System.out.println(" turning RIGHT");
                    break;
                case FORWARD:
                    System.out.println(" turning FORWARD");
                    break;
                default:
                    System.out.println(" turning ????");
                    break;
            }//switch                    
            
        }//printState


        
        public WMESet takeStep(int cmd)
        {
            // //%%%DEBUG
            // printState(cmd);
            
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
                SequenceEpisode seqEp = (SequenceEpisode)ep;
                return "[" + stringify(seqEp.getSequence()) + "]";
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
            String result = "";
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
            
            return result;
        }//stringify sequence
    
        public String stringify(Replacement repl)
        {
            //This is the LHS of the replacement rule
            String result = "{ ";
            Vector<Action> lhsVec = repl.getLHS();
            for(int i = 0; i < lhsVec.size(); i++)
            {
                Action act = lhsVec.elementAt(i);
                
                //precede all but first sequence with a comma separator
                if (i > 0) result += ", ";  
                
                result += stringify(act);
            }
            result += " }";
            
            //Arrow
            result += "==>";
            
            //RHS of the replacement rule
            result += stringify(repl.getRHS());
            
            return result;
        }
        
        /** convert a given replacement to a string */
        public String stringify(Route route)
        {
            String result = "{";
            boolean first = true;
            int count = 0;
            for(Action a : route.getActions())
            {
                //Precede all but the first action with a comma separator
                if (count > 0)
                {
                    result += ", ";
                    //Add additional spaces depending upon level so that the line is
                    //more readable
                    for(int i = 0; i < route.getLevel(); i++)
                    {
                        result += " ";
                    }
                }//else
                    
                result += stringify(a);

                //If this is the current action, put an asterisk behind it
                if (count == route.getCurrActIndex()) result += "*";

                count++;
            }//for
            result += "}";

            return result;
        }//stringify route

        public String stringify(Plan plan)
        {
            String result = "{\n";
            int len = plan.getNumLevels();
            for(int i = 0; i < len; i ++)
            {
                Route r = plan.getRoute(i);
                result += "  Level " + i + ": ";
                result += stringify(r);
                result += "\n";
            }//for
            result += "}";

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
        zigg.setRandSeed(8);
        zigg.setMonitor(new MonitorNull(env));
        
        //Run enough ticks to make a sizeable memory
        WMESet sensors = env.generateCurrentWMESet();
        for(int i = 0; i < 50; i++)
        {
            int cmd = zigg.tick(sensors);
            sensors = env.takeStep(cmd);
        }


        //Verify that there are level-1 sequences
        Vector<Vector<Sequence>> seqs = zigg.getSequences();
        assertTrue(seqs.size() >= 2);

        // Verify the number of level-0 sequences lines up with the
        // number of level-1 episodes
        Vector<Sequence> lvl0Seqs = seqs.elementAt(0);
        Vector<Vector<Episode>> eps = zigg.getEpmems();
        Vector<Episode> lvl1Eps = eps.elementAt(1);
        assertTrue(lvl0Seqs.size() - 1 == lvl1Eps.size());
        
	}//test_Zigg

	// END Test cases --------------------------------------

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
