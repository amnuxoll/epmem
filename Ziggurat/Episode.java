package Ziggurat;
 
import java.util.*;

/**
 * class Episode
 *
 * Each instance of this class contains a single episode in the episodic memory.
 * This exact nature of the episode's contants are specified in a subclass.
 * Currently an episode can be elemental or encompass a sequence of episodes.
 *
 */
public abstract class Episode extends DecisionElement
{
	public abstract boolean equals(Episode other);

	public abstract String toString();

    /** default ctor does nothing */
    public Episode() { }
    
    /** this ctor passes a utility to DecisionElement */
    public Episode(double utility)
    {
        super(utility);
    }

    public abstract Episode clone();

    /**
     * @return true if this episode contains a reward
     */
    public abstract boolean containsReward();
}//class Episode
