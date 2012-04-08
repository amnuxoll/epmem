/** A dummy class to fill in until Zach checks in the real deal */
package UnitTest;

import Ziggurat.Episode;

public class SequenceEpisodeTest extends Episode
{
    String name = "foo";
    
    public static SequenceEpisodeTest seep1 = new SequenceEpisodeTest("seep1");
    public static SequenceEpisodeTest seep2 = new SequenceEpisodeTest("seep2");
    public static SequenceEpisodeTest seep3 = new SequenceEpisodeTest("seep3");

    public SequenceEpisodeTest() {}

    public SequenceEpisodeTest(String s)
    {
        name = s;
    }
    
    public SequenceEpisodeTest clone()
    {
        return new SequenceEpisodeTest();
    }

    public String toString() { return name; }
    public boolean equals(Episode other)
    {
        if (! (other instanceof SequenceEpisodeTest))
        {
            return false;
        }

        return ( ((SequenceEpisodeTest)other).name.equals(this.name));
    }


}
