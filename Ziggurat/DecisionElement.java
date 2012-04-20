package Ziggurat;

/**
 * class DecisionElement
 * 
 * as the agent makes decisions, it bases them on information that may not be
 * 100% reliable.  Over time, Ziggurat tries to learn how reliable they are by
 * observing the outcome of decisons it makes based upon that element.  This
 * reliability or utility is adjusted whenever the decision leads to a reward
 * outcome.  Many other classes in Zigg inherit from this one.
 */
public abstract class DecisionElement
{
    /** constants */
    public static final double INIT_UTILITY = 1.0;

    /** the overall effectiveness  */
    protected double utility;

    /** the level of the hierarchy associated with this element */
    protected int level = -1;

    /** default starts with a default utility */
    public DecisionElement()
    {
        this(INIT_UTILITY);
    }
    
    /** this ctor sets the initial utility */
    public DecisionElement(double utility)
    {
        this.utility = utility;
    }
    
    
    /** accessor methods */
	public double getUtility() { return utility; }

    /** if this entity is involved in a decsions sequence that leads to a good
     * action, reward it.
     */
    public void reward () 
    {
        utility += (1.0 - utility) / 2;
    }

    /** if this entity is involved in a decsions sequence that leads to a bad
     * action, penalize it.
     */
    public void penalize () 
    {
        utility /= 2;
    }

    /**
     * @return the level of this decision element in the hierarchy
     */
    public int getLevel()
    {
        return level;
    }
        
    
}//class DecisionElement
