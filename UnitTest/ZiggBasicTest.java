package UnitTest;

import java.util.Vector;
import java.util.Random;

import org.junit.*;
import static org.junit.Assert.* ;

import Ziggurat.*;

/**
 * ZiggBasicTest
 * 
 * This JUnit test checks that Ziggurat can create level-0 episodes, actions and
 * sequences.
 */
public class ZiggBasicTest
{
    /**
     * This class is used to replace the normal random number generator so we can
     * control Zigg's behavior.  Currently we only need to override nextInt
     * since that's the only method that Zigg uses.
     */
    public class NonRandom extends Random
    {
        private int lastVal = 0;
        
         public int nextInt(int n)
         {
             lastVal = (lastVal + 1) % n;
             
             return lastVal;
         }
    }//class NonRandom

    /**
     * This class is a dummied up Environment so we have something to pass to
     * Zigg's constructor.
     */
    public class DummyEnv extends Environment
    {
        //never called
        public WMESet takeStep(int commandIndex) { return null; }

        //never called
        public WMESet generateCurrentWMESet() { return null; }
        
        public int getNumCommands() { return 3; }

    }//class DummyEnv

    
	// BEGIN Test cases --------------------------------------

	@Test
	public void test_tick()
    {
        //Create a Zigg
        Environment env = new DummyEnv();
        Ziggurat zigg = new Ziggurat(env);
        zigg.setRandGen(new NonRandom());
        zigg.setMonitor(new MonitorNull(env));  //To debug, omit this

        //Run one tick
        String[] sensorArr = { "state", "0" };
        WMESet sensors = new WMESet(WMESet.makeSensors(sensorArr));
        int cmd = zigg.tick(sensors);

        //Verify one episode exists
        Vector<Vector<Episode>> epmems = zigg.getEpmems();
        assertTrue(epmems.size() == 1);
        assertTrue(epmems.elementAt(0).size() == 1);

        //Verify the episode is correct
        ElementalEpisode elEp = (ElementalEpisode)epmems.elementAt(0).elementAt(0);
        assertTrue(elEp.toString().equals("{state:0}1"));

        //Verify that there are no actions
        Vector<Vector<Action>> actions = zigg.getActions();
        assertTrue(actions.size() == 1);
        assertTrue(actions.elementAt(0).size() == 0);
    }

	@Test
	public void test_OneAction()
    {
        //Create a Zigg
        Environment env = new DummyEnv();
        Ziggurat zigg = new Ziggurat(env);
        zigg.setRandGen(new NonRandom());
        zigg.setMonitor(new MonitorNull(env));
        
        //Run two ticks
        String[] sensorArr = { "state", "0" };
        WMESet sensors = new WMESet(WMESet.makeSensors(sensorArr));
        int cmd = zigg.tick(sensors);
        cmd = zigg.tick(sensors);

        //Verify two episodes exist
        Vector<Vector<Episode>> epmems = zigg.getEpmems();
        assertTrue(epmems.size() == 1);
        assertTrue(epmems.elementAt(0).size() == 2);

        //Verify one action exists
        Vector<Vector<Action>> actions = zigg.getActions();
        assertTrue(actions.size() == 1);
        assertTrue(actions.elementAt(0).size() == 1);

        //Verify the action
        Action act = actions.elementAt(0).elementAt(0);
        assertTrue(act.toString().equals("{state:0}1---->{state:0}"));

        //Verify that only one (incomplete) sequence exists
        Vector<Vector<Sequence>> seqs = zigg.getSequences();
        assertTrue(seqs.size() == 1);
        assertTrue(seqs.elementAt(0).size() == 1);
	}

	@Test
	public void test_OneSequence()
    {
        //Create a Zigg
        Environment env = new DummyEnv();
        Ziggurat zigg = new Ziggurat(env);
        zigg.setRandGen(new NonRandom());
        zigg.setMonitor(new MonitorNull(env));
        
        //Run enough ticks to complete a sequence
        for(int i = 0; i < 8; i++)
        {
            String[] sensorArr = { "state", "0" };
            if (i%2 == 1) sensorArr[1] = ("" + i);
            WMESet sensors = new WMESet(WMESet.makeSensors(sensorArr));
            int cmd = zigg.tick(sensors);
        }

        //Verify that only two level-0 sequences exist (one will be empty)
        Vector<Vector<Sequence>> seqs = zigg.getSequences();
        assertTrue(seqs.size() == 2);
        assertTrue(seqs.elementAt(0).size() == 2);

        //Verify the sequences are the right size
        Sequence seq1 = seqs.elementAt(0).elementAt(0);
        Sequence seq2 = seqs.elementAt(0).elementAt(1);
        assertTrue(seq1.length() == 7);
        assertTrue(seq2.length() == 0);

        //Verify that there is one level-1 episode that contains the first
        //sequence
        Vector<Vector<Episode>> epmems = zigg.getEpmems();
        assertTrue(epmems.size() == 2);
        assertTrue(epmems.elementAt(1).size() == 1);
        SequenceEpisode seqEp = (SequenceEpisode)epmems.elementAt(1).elementAt(0);
        assertTrue(seqEp.getSequence() == seq1);
        
	}//test_OneSequence

	// // BEGIN Test cases --------------------------------------

    /**
     * An easy way to run this test individually from the command line without
     * the JUnit jar file
     */
    public static void main(String[] args)
    {
        ZiggBasicTest zbt = new ZiggBasicTest();
        zbt.test_tick();
        zbt.test_OneAction();
        zbt.test_OneSequence();
    }
	
}//class ZiggBasicTest
