package Ziggurat;

/**
 * class SequenceEpisode
 *
 * a SequenceEpisode is an episode at some level N that contains a sequence at
 * level N-1.  As such, it's mostly just a wrapper class.
 */
public class SequenceEpisode extends Episode 
{
    protected Sequence sequence;

    /** ctor  */
    public SequenceEpisode(Sequence sequence)
    {
        this.sequence = sequence;

        //this is always one level higher than the constituent sequence
        this.level = sequence.getLevel() + 1;
    }

    /** accessor methods */
    public Sequence getSequence()
    {
        return sequence;
    }

    /** guess what this does? */
    public boolean equals(Episode other) 
    {
        if (other instanceof SequenceEpisode)
        {
            Sequence seq = ((SequenceEpisode)other).sequence;

            //Catch the obvious case
            if (other == this) return true;
        
            return seq.equals(this.sequence);
        }

        return false;
    }

    public String toString() 
    {
        return this.sequence.toString();
    }
    
    public SequenceEpisode clone()
    {
    	return new SequenceEpisode(this.sequence.clone());
    }

    /**
     * @return true if its sequence contains a reward
     */
    public boolean containsReward()
    {
        return this.sequence.containsReward();
    }
                


}//class SequenceEpisode
