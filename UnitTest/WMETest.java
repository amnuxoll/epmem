package UnitTest;

import org.junit.* ;
import static org.junit.Assert.* ;
import Ziggurat.WME;

/**
 * class WMETest
 *
 * This JUnit test case ensures that WME is working.
 */
public class WMETest {
	
	// Create some WMESet we can use here, and access in other test files if necessary
    public static WME wme1;
    public static WME wme2;
    public static WME wme3;
    public static WME wme4;
    

    @BeforeClass
    public static void oneTimeSetUp()
    {
        //Create some WMEs
        wme1 = WME.makeWME("hi", "there");
        wme2 = WME.makeWME("hi", "5");
        wme3 = WME.makeWME("hi", "f");
        wme4 = WME.makeWME("hi", "5.0");
    }

	// BEGIN Test cases --------------------------------------
    
    @Test
    public void test_equals()
    {
        WME w1 = new WME("h" + "i", "5.0", WME.Type.DOUBLE);
        assertFalse(w1.equals(wme1));
        assertFalse(w1.equals(wme2));
        assertFalse(w1.equals(wme3));
        assertTrue(w1.equals(wme4));
                    
        WME w2 = new WME("h" + "i", "5", WME.Type.INT);
        assertFalse(w2.equals(wme1));
        assertTrue(w2.equals(wme2));
        assertFalse(w2.equals(wme3));
        assertFalse(w2.equals(wme4));
    }

    @Test
    public void test_accessors()
    {
        assertTrue(wme1.getChar() == '?');
        assertTrue(wme3.getChar() == 'f');
        assertTrue(wme1.getStr().equals("there"));
        assertTrue(wme1.getInt() == 0);
        assertTrue(wme2.getInt() == 5);
        assertTrue(wme1.getDouble() == 0.0);
        assertTrue(wme4.getDouble() == 5.0);
        
    }

    @Test
    public void test_toString()
    {
        assertTrue(wme1.toString().equals("hi:there"));
    }
    
}//class WMETest
