package Ziggurat;

import java.util.*;

/*
 * Route
 * 
 * Author: Zachary Paul Faltersack
 * Last Edit: February 22, 2012
 * 
 * This class defines a Route. A Route is primarily comprised of
 * a vector of Sequences.
 * 
 * 
 * NOTES:
 * When is applyReplacement() called? Must be at minimum .between. sequences 
 * 	else nextAction() is incorrect
 */
public class Route
{
	private Vector<Sequence> sequences;
	private Sequence replSeq;
	private int currSeqIndex;
	private int currActIndex;

	public Route() 
    {
        sequences = new Vector<Sequence>();
        replSeq = null;
        currSeqIndex = -1;
        currActIndex = -1;
	}

	public Route(Vector<Sequence> initSeq)
    {
        sequences = initSeq == null ? new Vector<Sequence>() : initSeq;
        replSeq = null;
        currSeqIndex = 0;
        currActIndex = 0;
	}

	public String toString() 
    {
		return "";
	}

	public String toStringLong() 
    {
		return "";
	}

	/** auto increments the currAction/currSequence pointers	 */
	public Action nextAction() 
    {
		currActIndex++;
		if(replSeq != null)
		{
			if(currActIndex >= replSeq.length())
			{
				replSeq = null;
				currActIndex = 0;
				currSeqIndex++;
			}
			else
			{
				return replSeq.getActionAtIndex(currActIndex);
			}
		}
		else if(currActIndex >= sequences.elementAt(currSeqIndex).length())
        {
            currActIndex = 0;
            currSeqIndex++;
        }
		
        if(currSeqIndex >= sequences.size())
        {
            return null;
        }
        else
        {
        	return sequences.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
        }
	}

	public Action getCurrAction() 
    {
		if(replSeq != null) return replSeq.getActionAtIndex(currActIndex);
		else 				return sequences.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
	}

	public Sequence getCurrSequence() 
    {
		if(replSeq != null) return replSeq;
		else 				return sequences.elementAt(currSeqIndex);
	}

	public void applyReplacement(Replacement repl) 
    {
		replSeq = repl.apply(sequences.elementAt(currSeqIndex));
	}

	public int numElementalEpisodes() 
    {
        int count = 0;
        for(Sequence s : sequences)
        {
            count += s.numElementalEpisodes();
        }
		return count;
	}

	public int remainingElementalEpisodes() 
    {
        int count = 0;
        for(int i = currSeqIndex; i < sequences.size(); ++i)
        {
            count += sequences.elementAt(i).numElementalEpisodes();
        }
		return count - currActIndex;
	}
}//class Route
