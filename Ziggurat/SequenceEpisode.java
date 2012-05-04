package Ziggurat;

/**
 * <!-- class SequenceEpisode -->
 *
 * A SequenceEpisode is an episode at some level N that contains a sequence at
 * level N-1.  As such, it's mostly just a wrapper class.
 */
public class SequenceEpisode extends Episode 
{
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** the episode consists of this sequence at one level below */
    protected Sequence sequence;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    public SequenceEpisode(Sequence sequence)
    {
        this.sequence = sequence;

        //this is always one level higher than the constituent sequence
        this.level = sequence.getLevel() + 1;
    }

    /*======================================================================
     * Accessors
     *----------------------------------------------------------------------
     */
    public Sequence getSequence()
    {
        return sequence;
    }

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /** standard equivalance comparison */
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

    /** 
     * Typically you want to use the printing facility in the current
     * { @link Environment} class instead.
     *
     * @return a String representation of this SequenceEpisode
     */
    public String toString() 
    {
        return this.sequence.toString();
    }

    /** this creates a deep copy */
    public SequenceEpisode clone()
    {
    	SequenceEpisode rtn = new SequenceEpisode(this.sequence.clone());

        //These values inherited from DecisionElement
        rtn.utility = this.utility;
        rtn.level = this.level;

        return rtn;
    }//clone

    /**
     * @return true if its sequence contains a reward
     */
    public boolean containsReward()
    {
        return this.sequence.containsReward();
    }
                


}//class SequenceEpisode
