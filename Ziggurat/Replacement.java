package Ziggurat;

import java.util.*;

/**
 * <!-- class Replacement -->
 *
 * a replacement defines a way that a {@link Sequence} might shortened by replacing N
 * actions with a single action.
 */

public class Replacement extends DecisionElement
{
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /** the series of actions that would be replaced */
    protected Vector<Action> original;
    /** the replacement action */
    protected Action replacement;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /** common initialization steps for all ctors */
    private void initRepl(Vector<Action> original, Action replacement)
    {
        this.level = original.elementAt(0).getLevel(); //inherit from constituents
        this.original = original;
        this.replacement = replacement;
    }

    /** this ctor just inits instance vars */
    public Replacement(Vector<Action> original, Action replacement) 
    {
        initRepl(original, replacement);
    }//ctor

    /** this ctor is convenient when you want to have exactly two actions in the
     * original vector */
    public Replacement(Action origAct1, Action origAct2, Action replacement) 
    {
        Vector<Action> vec = new Vector<Action>();
        vec.add(origAct1);
        vec.add(origAct2);

        initRepl(vec, replacement);
    }//ctor

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /** standard equivalence comparison */
    public boolean equals (Object other) 
    {
        //First, verify we've been given a replacement
        if (! (other instanceof Replacement)) return false;
        Replacement repl = (Replacement)other;

        //Catch the obvious cases
        if (repl == this) return true;
        if (repl.level != this.level) return false;
        
        return repl.original.equals(this.original)
            && repl.replacement.equals(this.replacement);
    }//equals

    /**
     * vecMatch
     *
     * compares a range of values within a pair of Vector<Action> 
     *
     * @param vec1    vec1 and vec2 are the vectors to compare
     * @param vec2    
     * @param pos1    pos1 and pos2 are the respective indexes with which to compare them
     * @param pos2    
     * @param len     how many to compare
     * 
     * @return true if two given Vectors match at given indexes.
     */
    protected static boolean vecMatch(Vector<Action> vec1, int pos1,
                                   Vector<Action> vec2, int pos2,
                                   int len)
    {
        for(int i = 0; i < len; i++)
        {
            Action o1 = vec1.get(pos1 + i);
            Action o2 = vec2.get(pos2 + i);

            if (! o1.equals(o2)) return false;
        }

        return true;
    }//vecMatch

    /**
     * applyPos
     *
     * determines where this replacement can occur in a given sequence
     *
     * @return the first index where the replacement can occur, or -1 if it can
     * not be applied
     */
    public int applyPos(Sequence seq)
    {
        //If the sequence is the wrong level then we don't bother checking
        if (seq.getLevel() != this.getLevel()) return -1;

        for(int i = 0; i < seq.length() - 1; i++)
        {
            if (vecMatch(this.original, 0, seq.getActions(), i, original.size()))
            {
                return i;
            }
        }//for

        return -1;
    }//applyPos

    /**
     * canApply
     *
     * checks to see if this replacement can be applied to a given sequence.  
     *
     * @return true if it can be applied, false otherwise
     */
    public boolean canApply(Sequence seq)
    {
        return applyPos(seq) != -1;
    }//canApply

    /**
     * apply
     *
     * applies this replacement to a copy of the given sequence.  The
     * replacement is applied successively to all valid places where it matches
     * the sequence.  The given sequence is not modified.
     *
     * @return the resulting sequence
     */
    public Sequence apply(Sequence orig)
    {
        //Start with a copy of the original
        Sequence result = orig.clone();
        Vector<Action> vec = result.getActions();

        //Keep applying the replacement as long as you can
        int index = applyPos(result);
        while(index != -1)
        {
            vec.removeElementAt(index);
            vec.removeElementAt(index);
            vec.insertElementAt(this.replacement, index);

            index = applyPos(result);
        }//while

        return result;
    }//apply

    /**
     * extending this method to report the outcome to Ziggurat's monitor if it's
     * available
     */
    public void reward () 
    {
        super.reward();

        //Record this event for posterity
        Monitor mon = Ziggurat.getMonitor();
        if (mon != null)
        {
            mon.log("Replacement succeeded:  ");
            mon.tab();
            mon.log(this);
        }        
    }//reward

    /**
     * extending this method to report the outcome to Ziggurat's monitor if it's
     * available
     */
    public void penalize () 
    {
        super.penalize();
        
        //Record this event for posterity
        Monitor mon = Ziggurat.getMonitor();
        if (mon != null)
        {
            mon.log("Replacement failed:  ");
            mon.tab();
            mon.log(this);
        }
    }//penalize
    
}//class Replacement
