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

    /** ctor just sets the instance variables */
    public SequenceEpisode(Sequence sequence)
    {
        this.sequence = sequence;
    }

    /** accessor methods */
    public Sequence getSequence()
    {
        return sequence;
    }

    public boolean equals(Episode other) 
    {
        if (other instanceof SequenceEpisode)
        {
            Sequence seq = ((SequenceEpisode)other).sequence;
        
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

}//class SequenceEpisode
