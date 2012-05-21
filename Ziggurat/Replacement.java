package Ziggurat;

import java.util.*;

/**
 * <!-- class Replacement -->
 *
 * a replacement defines a way that a {@link Sequence} might shortened by
 * replacing N actions with a single action.
 *
 * At level 0, a replacement has this form:
 * <code>  Ax->B  By->C ==> Az->C </code>
 * where A,B and C are sensors (WMESets) and x,y and z are commands.
 * 
 * <p>To create a replacement action at level 0 create a new level 0 action whose
 * LHS and RHS sensors are drawn from LHS and RHS of the original two actions
 * respectively.  The new command ('z' in the example) can be any valid command
 * but it is better to pick commands such that the new level 0 action is one
 * that the agent has experienced before.
 *
 * At level 1+, a replacement has this form:
 * <code> [A->B]-->[B->C] [B->C]-->[C->D] ==> [A->Q]-->[Q->D] </code>
 * where A,B,C,D and Q are all level 1 episodes.
 *
 * <p>To create a replacement action at level 1+ create a new level 0 action
 * whose LHS and RHS episodes are drawn from LHS and RHS of the original two
 * actions respectively.  The new intervening episode ('Q' in the example) can
 * be any sequence episode whose internal first and last actions (from one level
 * below) match those of the preceding and postceding sequence episodes
 * respectively.  It is better to select episodes that the resulting action
 * (e.g., '[A->Q]-->[Q->D]' in the above example) is one the agent has
 * experienced before.  If that's not possible, it's preferable to select a
 * sequence episode ('Q' in this example) that the agent has seen before.
 * However, it is theoretically possible to select any valid Q.
 *
 * @see Ziggurat#makeNewReplacement 
 */

public class Replacement extends DecisionElement
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
    /** used for {@link #toString} */
    public static NullEnvironment nullEnv = new NullEnvironment();

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
     * Accessors
     *----------------------------------------------------------------------
     */
    /** access the LHS of the replacement rule */
    public Vector<Action> getLHS() { return this.original; }
    
    /** access the LHS of the replacement rule */
    public Action getRHS() { return this.replacement; }

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
     * Typically you want to use the printing facility in the specific
     * { @link Environment} class instead.
     *
     * @return a String representation of this replacement
     */
	public String toString() 
    {
        return nullEnv.stringify(this);
    }//toString
    
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
     * determines where this replacement can occur in a given sequence at or
     * beyond a given index
     *
     * @param seq is the given sequence
     * @param start is the lowest index in which it should be applied
     *
     * @return the first index where the replacement can occur, or -1 if it can
     * not be applied
     */
    public int applyPos(Sequence seq, int start)
    {
        //If the sequence is the wrong level then we don't bother checking
        if (seq.getLevel() != this.getLevel()) return -1;

        for(int i = start; i < seq.length() - 1; i++)
        {
            if (vecMatch(this.original, 0, seq.getActions(), i, original.size()))
            {
                return i;
            }
        }//for

        return -1;
    }//applyPos

    /** this version of applyPos assumes that you want to start the search at
    the beginning of the sequence */
    public int applyPos(Sequence seq)
    {
        return applyPos(seq, 0);
    }
    
    /**
     * canApply
     *
     * checks to see if this replacement can be applied to a given sequence at
     * the given position or subsequently.
     *
     * @param seq is the given sequence
     * @param start is the lowest index in which it should be applied
     *
     * @return true if it can be applied, false otherwise
     */
    public boolean canApply(Sequence seq, int start)
    {
        return applyPos(seq, start) != -1;
    }//canApply

    /** this version is used to ask if the replacement can be applied anywhere
      * in a sequence
      */
    public boolean canApply(Sequence seq)
    {
        return applyPos(seq, 0) != -1;
    }//canApply

    /**
     * apply
     *
     * applies this replacement to a <u>copy</u> of the given sequence.  The
     * replacement is applied successively to all valid places where it matches
     * the sequence.  The given sequence is not modified.
     *
     * @param orig is the sequence to apply the replacements to
     * @param start is the lowest index at which the repl should be applied
     *
     * @return the resulting sequence
     */
    public Sequence apply(Sequence orig, int start)
    {
        //Start with a copy of the original
        Sequence result = orig.clone();
        Vector<Action> vec = result.getActions();

        //Keep applying the replacement as long as you can
        int index = applyPos(result, start);
        while(index != -1)
        {
            vec.removeElementAt(index);
            vec.removeElementAt(index);
            vec.insertElementAt(this.replacement, index);

            index = applyPos(result, start);
        }//while

        return result;
    }//apply

    /** this version applies the replacement anywhere where it can be applied to
     * the sequence
     */
    public Sequence apply(Sequence orig)
    {
        return apply(orig, 0);
    }
        
    /**
     * extending this method to report the outcome to Ziggurat's monitor if it's
     * available
     */
    public void reward () 
    {
        super.reward();
    }//reward

    /**
     * extending this method to report the outcome to Ziggurat's monitor if it's
     * available
     */
    public void penalize () 
    {
        super.penalize();
    }//penalize
    
}//class Replacement
