package Ziggurat;

import java.util.*;

/*
 * Route
 * 
 * This will be a route class.
 * 
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

	public void Route(Vector<Sequence> initSeq) 
    {
        sequences = initSeq == null ? new Vector<Sequence>() : initSeq;
        replSeq = null;
        currSeqIndex = -1;
        currActIndex = -1;
		
	}

	public String toString() 
    {
		return "";
	}

	public String toStringLong() 
    {
		return "";
	}

	/** auto increments tthe currAction/currSequence pointers	 */
	public Action nextAction() 
    {
		currActIndex++;
        if(currActIndex >= sequences.elementAt(currSeqIndex).length())
        {
            currActIndex = 0;
            currSeqIndex++;
            if(currSeqIndex >= sequences.size())
            {
                return null;
            }
        }
        return sequences.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
	}

	public Action getCurrAction() 
    {
        return sequences.elementAt(currSeqIndex).getActionAtIndex(currActIndex);
	}

	public Sequence getCurrSequence() 
    {
        return sequences.elementAt(currSeqIndex);
	}

	public void applyReplacement(Replacement repl) 
    {
		
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
