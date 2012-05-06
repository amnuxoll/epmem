package Ziggurat;

/**
 * <!-- class WME -->
 *
 * This class contains a simplified representation of a working memory element
 * in the Soar cognitive architecture.  In the long-term, the actual Soar WME
 * struct would be used here.
 *
 * <p>A WME is an atomic unit of knowledge: an attribute and value pair combined
 * with an ID that connects it to other WMEs.  For Zigg's purposes, the ID is
 * currently not implemented.  For more complex environments it will be
 * essential.
 */
public class WME 
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
	/** this is the attribute string associated with rewards */
    public static final String REWARD_STRING = "reward";


    /**
     * a WME's value can be a string, integer, double or char.  Internally, it
	 * is always stored as a String and converted to other types as necessary.
     */
	public enum Type {STRING, INT, DOUBLE, CHAR}

    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
	public String attr;
	public String value;
    public Type type;

    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
    /**
     * initializes instance variables
     */
	public WME(String attr, String value, Type type) 
    {
        this.attr = attr;
        this.value = value;
        this.type = type;
	}

    /*======================================================================
     * Accessors
     *----------------------------------------------------------------------
     */
    /** returns the WME's value as a char */
	public char getChar() 
    {
		char c = '?';
        if ((this.type == Type.CHAR) && (this.value.length() > 0))
        {
            c = this.value.charAt(0);
        }

        return c;
	}//getChar

    /** returns the WME's value as a String */
	public String getStr () 
    {
        if (this.type == Type.STRING)
        {
            return this.value;
        }

        return "?";
	}//getStr

    /** returns the WME's value as an int */
	public int getInt () 
    {
		if (this.type == Type.INT)
        {
            return Integer.parseInt(this.value);
        }

        return 0;
	}//getInt

    /** returns the WME's value as a double */
	public double getDouble () 
    {
        if (this.type == Type.DOUBLE)
        {
            return Double.parseDouble(this.value);
        }

        return 0.0;
	}//getDouble

    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /** returns true if the given WME equals this one */
	public boolean equals(Object other) 
    {
        //Verify we've been given a WME
        if (! (other instanceof WME)) return false;
        WME w = (WME)other;

        //compare
		return w.attr.equals(this.attr)
            && w.value.equals(this.value)
            && (w.type == this.type);
	}//equals

    /** returns a String representation of this WME */
	public String toString () 
    {
		String result = this.attr + ":";
        switch(this.type)
        {
            case CHAR:
                result += this.getChar();
                break;
            case INT:
                result += this.getInt();
                break;
            case STRING:
                result += this.getStr();
                break;
            case DOUBLE:
                result += this.getDouble();
                break;
            default:
                result += this.value;
                break;
        }//switch

        return result;
	}//toString

    /** make a copy of 'this' */
	public WME clone()
	{
		return new WME(this.attr, this.value, this.type);
	}
	
    /**
     * creates a WME from a given attribute and value string.  The type of the
     * WME is determined by looking for a particular hint which is sought in
     * this order:
     * <ul>
     *   <li>DOUBLE  - value contains a period ('.')
     *   <li>INT     - first char is a digit (note: so neg nums don't work)
     *   <li>CHAR    - value contains only one character
     *   <li>STRING  - anything else
     * </ul>
     * 
     */
    public static WME makeWME(String attr, String val)
    {
        if (val.contains("."))
        {
            return new WME(attr, val, WME.Type.DOUBLE);
        }

        if (Character.isDigit(val.charAt(0)))
        {
            return new WME(attr, val, WME.Type.INT);
        }

        if (val.length() == 1)
        {
            return new WME(attr, val, WME.Type.CHAR);
        }

        return new WME(attr, val, WME.Type.STRING);
    }//makeWME

}//class WME
