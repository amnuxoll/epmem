package UnitTest;

import org.junit.* ;
import static org.junit.Assert.* ;
import Ziggurat.WME;

/**
 * class WMETest
 *
 * junit test for Episode, ElementalEpisode and SequenceEpisode
 */
public class WMETest
{
    //Some variables for testing
    public static WME wme1;
    public static WME wme2;
    public static WME wme3;
    public static WME wme4;
    

    /**
     * creates a WME from a given attribute and value string.  The type of the
     * WME is determined by looking for a particular hint which is sought in
     * this order:
     * <ul>
     *   <li>DOUBLE  - value contains a period ('.')
     *   <li>INT     - first char is a digit (note: so neg nums don't work)
     *   <li>CHAR    - value contains only one character
     *   <li>STRING  - anything else
     * </ul>
     * 
     */
    public static WME makeWME(String attr, String val)
    {
        if (val.contains("."))
        {
            return new WME(attr, val, WME.Type.DOUBLE);
        }

        if (Character.isDigit(val.charAt(0)))
        {
            return new WME(attr, val, WME.Type.INT);
        }

        if (val.length() == 1)
        {
            return new WME(attr, val, WME.Type.CHAR);
        }

        return new WME(attr, val, WME.Type.STRING);
    }//makeWME

    @BeforeClass
    public static void oneTimeSetUp()
    {
        //Create some WMEs
        wme1 = makeWME("hi", "there");
        wme2 = makeWME("hi", "5");
        wme3 = makeWME("hi", "f");
        wme4 = makeWME("hi", "5.0");
    }

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
