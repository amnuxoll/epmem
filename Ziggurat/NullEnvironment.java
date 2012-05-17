package Ziggurat;
 
import java.util.*;

/**
 * <!-- class NullEnvironment -->
 *
 * This is a do-nothing environment that can be instantiated so that the
 * stringify methods can be used.
 *
 */
public class NullEnvironment extends Environment
{
    @Override
	public WMESet takeStep(int commandIndex) { return null; }
    @Override
	public WMESet generateCurrentWMESet()  { return null; }
    @Override
    public int getNumCommands()  { return -1; }
    
}//class NullEnvironment
