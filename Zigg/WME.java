/**
 * class WME
 *
 * This class contains a simplified representation of a working memory element
 * in the Soar cognitive architecture.  In the long-term, the actual Soar WME
 * struct will be used here.
 *
 * A WME is an atomic unit of knowledge:  an attribute and value pair combined
 * with an ID that connects it to other WMEs.  For Zigg's purposes, the ID is
 * currently not implemented.  For more complex environments it will be
 * essential. 
 */
public class WME 
{
    /*
     * a WME's value can be a string, integer, double or char.  Internally, it
	 * is always stored as a String and converted to other types as necessary.
     */
	public enum Type {STRING, INT, DOUBLE, CHAR}

    /* ---=== instance  variables ===--- */
	public String attr;
	public String value;
    public Type type;

    /**
     * WME ctor
     *
     * initializes instance variables
     */
	public WME(String attr, String value, Type type) 
    {
        this.attr = attr;
        this.value = value;
        this.type = type;
	}

    /** returns true if the given WME equals this one */
	public boolean equals(WME w) 
    {
		return w.attr.equals(this.attr)
            && w.value.equals(this.value)
            && (w.type == this.type);
	}

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

}//class WME
