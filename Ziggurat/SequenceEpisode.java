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
    public boolean equals(Object other) 
    {
        if (other instanceof SequenceEpisode)
        {
            SequenceEpisode seqEp = (SequenceEpisode)other;

            //Catch the obvious cases
            if (seqEp == this) return true;
            if (seqEp.level != this.level) return false;
        
            return seqEp.sequence.equals(this.sequence);
        }

        return false;
    }

    /** guess what this does? */
    public String toString() 
    {
        return this.sequence.toString();
    }

    /** copy me! */
    public SequenceEpisode clone()
    {
    	SequenceEpisode rtn = new SequenceEpisode(this.sequence.clone());

        //These values inherited from DecisionElement
        rtn.utility = this.utility;
        rtn.level = this.level;

        return rtn;
    }

    /**
     * @return true if its sequence contains a reward
     */
    public boolean containsReward()
    {
        return this.sequence.containsReward();
    }
                


}//class SequenceEpisode
