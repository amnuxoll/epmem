package Ziggurat;

import java.util.Hashtable;
import java.util.Vector;


/**
 * <!-- class RoombaEnvironment -->
 *
 * This environment is a simulation of the Roomba blind nagivation task:<br>
 *
 * Zachary Faltersack, Brian Burns, Andrew Nuxoll and Tanya L. Crenshaw.
 * Ziggurat: Steps Toward a General Episodic Memory. In Papers from the
 * Association for the Advancement of Artificial Intelligence (AAAI) Fall
 * Symposium Series: Advances in Cognitive Systems (ACS), 2011.
 * 
 * <p>The agent is in a maze and its only sensors are bumpers that detect when
 * the agent bumps into a wall.  Unlike the real world task, the environment is
 * a gridworld.  
 *
 */
public class RoombaEnvironment extends Environment
{
    /*======================================================================
     * Constants
     *----------------------------------------------------------------------
     */
	// The agent can be facing in one of eight directions
    public static final int EAST      = 0;
    public static final int SOUTHEAST = 1;
    public static final int SOUTH     = 2;
    public static final int SOUTHWEST = 3;
    public static final int WEST      = 4;
    public static final int NORTHWEST = 5;
    public static final int NORTH     = 6;
    public static final int NORTHEAST = 7;

	// The agent selects from these commands
    public static final int CMD_NO_OP 			= 0;
    public static final int CMD_FORWARD			= 1;
    public static final int CMD_LEFT			= 2;
    public static final int CMD_RIGHT			= 3;
    public static final int CMD_ADJUST_LEFT		= 4;
    public static final int CMD_ADJUST_RIGHT	= 5;
    public static final int CMD_SACC			= 6;
	public static final int NUM_COMMANDS 	    = 7;

    /** This array is indexed by the facing contants. The character at each
     * index is the ASCII representation of the robot facing that direction.
     */
    public static final char[] facingChars = { '>','J','v','L','<','F','^','7' };

    /**
     * This array is indexed by facing.  It species how the robot's x-coordinate
     * changes if it issues a forward command while in each facing.
     */
    public static final int[] xdiff = { 1, 1, 0, -1, -1, -1, 0, 1 };
    
    /** this is the same as xdiff but for the y-coordinate */
    public static final int[] ydiff = { 0, 1, 1, 1, 0, -1, -1, -1 };
    
    //These chracters represent other things that are on the map
    public static final char WALL = 'W';
    public static final char HALL = ' ';
    public static final char GOAL = 'G';
	
    /*======================================================================
     * Instance Variables
     *----------------------------------------------------------------------
     */
    /**
     * the map is defined as an array of string that looks like the map. This
     * variable is initialized with a simple two-square map with robot starting
     * facing north.
     */
    protected String[] map = { "WWWW",
                               "W^GW",
                               "WWWW" };
    /** The agent's current facing.  This is also maintained in the map */
    protected int facing = NORTH;
    /** The agent's current x position */
    protected int xpos = 1;
    /** The agent's current x position */
    protected int ypos = 1;
    /** current left bumper sensor value */
    protected int leftBump = 0;
    /** current right bumper sensor value */
    protected int rightBump = 0;
    /** current right goal (IR) sensor value */
    protected int goal = 0;

    //These values are used to reset the agent when it finds a goal
    /** the agent's starting x position */
    protected int xStart = 1;
    /** the agent's starting y position */
    protected int yStart = 1;
    /** the agent's starting facing */
    protected int facingStart = NORTH;

    
    
    /*======================================================================
     * Constructors
     *----------------------------------------------------------------------
     */
	public RoombaEnvironment()
    {
	}// ctor
	
    /*======================================================================
     * Methods
     *----------------------------------------------------------------------
     */
    /** retrieves the symbol at a particular map coordinate */
    public int getMap(int x, int y)
    {
        //retrieve the proper row
        if (y >= map.length) return '?';
        String row = map[y];

        //retrieve the proper char in the row
        if (x >= row.length()) return '?';
        return row.charAt(x);
        
    }//getMap

    /** sets the symbol at a particular position on the map
     * CAVEAT:  if illegal x,y values are passed, this method does nothing
     */
    public void setMap(int x, int y, char c)
    {
        //extract the proper row
        if (y >= map.length) return;
        StringBuffer row = new StringBuffer(map[y]);

        //set the char in the row
        if (x >= row.length()) return;
        row.setCharAt(x, c);

        //place the row back into the map
        map[y] = row.toString();
    }//setMap

    /** print the current map including the agent's position in it.
     *  NOTE:  '++' is prepended for ease of grepping.
     */
    public void printMap()
    {
        for(int i = 0; i < map.length; i++)
        {
            System.out.println("++" + map[i]);
        }
    }
       
    
	/**
	 * take a command and apply it to the environment.
     *
     * @return the resulting WMESet
	 */
	public WMESet takeStep(int commandIndex)
    {
        //default sensor values
        this.rightBump = 0;
        this.leftBump = 0;
        this.goal = 0;

        //act based upon the command
        switch(commandIndex)
        {
            case  CMD_NO_OP:
            case  CMD_ADJUST_LEFT:
            case  CMD_ADJUST_RIGHT:
            case  CMD_SACC:
                //do nothing
                break;
            case  CMD_LEFT:
                //Adding seven and modulus is a nifty shorthand for substracting
                //one but rolling -1 over to 7.
                this.facing = (facing + 7) % 8;
                setMap(this.xpos, this.ypos, this.facingChars[this.facing]);
                break;
            case  CMD_RIGHT:
                //add 1 but modulus to let it rollover to 0 (EAST)
                this.facing = (facing + 1) % 8;
                setMap(this.xpos, this.ypos, this.facingChars[this.facing]);
                break;
            case  CMD_FORWARD:
                //calc new x,y positions
                int newX = xpos + xdiff[facing];
                int newY = ypos + ydiff[facing];

                //The result depends on what's in that new position
                switch(getMap(newX,newY))
                {
                    case GOAL:
                        //found goal!  reset to start position and give reward
                        setMap(this.xpos, this.ypos, HALL);
                        this.xpos = this.xStart;
                        this.ypos = this.yStart;
                        this.facing = this.facingStart;
                        setMap(this.xpos, this.ypos, this.facingChars[this.facing]);
                        this.goal = 1;
                        break;
                    case HALL:
                        //successful move to the new position
                        setMap(this.xpos, this.ypos, HALL);
                        this.xpos = newX;
                        this.ypos = newY;
                        setMap(this.xpos, this.ypos, this.facingChars[this.facing]);
                        break;
                    case WALL:
                        //The bumper setting depends upon facing
                        //(triple-nested switch?!  Are you kidding?!?)
                        switch(facing)
                        {
                            //non-diagonal movement into a wall always sets both bumpers
                            case NORTH:
                            case EAST:
                            case SOUTH:
                            case WEST:
                                this.leftBump = 1;
                                this.rightBump = 1;
                                break;
                            case NORTHWEST:
                            case SOUTHEAST:
                                if (getMap(xpos + xdiff[facing], ypos) == WALL)
                                {
                                    this.leftBump = 1;
                                }
                                if (getMap(xpos, ypos + ydiff[facing]) == WALL)
                                {
                                    this.rightBump = 1;
                                }
                                break;
                            case NORTHEAST:
                            case SOUTHWEST:
                                if (getMap(xpos + xdiff[facing], ypos) == WALL)
                                {
                                    this.rightBump = 1;
                                }
                                if (getMap(xpos, ypos + ydiff[facing]) == WALL)
                                {
                                    this.leftBump = 1;
                                }
                                break;
                            default:
                                System.err.println("Encountered unknown facing: " + facing + "!");
                                System.exit(-1);
                        }//switch
                        break;
                    default:
                        System.err.println("Encountered Unknown map symbol: '" + getMap(newX,newY) + "'!");
                        System.exit(-2);
                }//switch
                break;
            default:
                System.err.println("Illegal Command: " + commandIndex + "!");
                System.exit(-3);
        }//switch

        //%%%DEBUG
        printMap();
        
        //Report the new sensor values
        return generateCurrentWMESet();

    }//takeStep
	
	/**
	 * get an initial WMESet config indicating we're beginning in a brand new
	 * environment.
	 */
	public WMESet generateCurrentWMESet()
    {
        //build the WMESet with the sensor values
        String[] sensorsArr = {WME.REWARD_STRING, ""+this.goal+".0",
                               "rdrop", "0",
                               "ldrop", "0",
                               "cdrop", "0",
                               "lcliff", "0",
                               "rcliff", "0",
                               "flcliff", "0",
                               "frcliff", "0",
                               "left", ""+this.leftBump,
                               "right", ""+this.rightBump
                               };
        return new WMESet(WMESet.makeSensors(sensorsArr));
    }//generateCurrentWMESet
	
    /**
     * the number of actions is defined by the NUM_COMMANDS const
     */
    public int getNumCommands()
    {
        return NUM_COMMANDS;
    }

    @Override
    /** use a two letter abbreviation for each command */
    public String stringify(int cmd)
    {
        //act based upon the command
        switch(cmd)
        {
            case  CMD_NO_OP:
                return "NO";
            case  CMD_ADJUST_LEFT:
                return "AL";
            case  CMD_ADJUST_RIGHT:
                return "AR";
            case  CMD_SACC:
                return "SA";
            case  CMD_LEFT:
                return "LT";
            case  CMD_RIGHT:
                return "RT";
            case  CMD_FORWARD:
                return "FD";
            default:
                return "??";
        }//switch
    }//stringify command
                

    @Override
    /**
     * create a binary number by treating the left and right bump sensors as a
     * binary number.  Example strings:
     *<ul>
     *     <li>0AL - sensed nothing and then adjusted left
     *     <li>1FD - right bumper went off, moved forward
     *     <li>GRT - found goal, then turned right
     *</ul>
     */
    public String stringify(WMESet sensors)
    {
        double goal = sensors.getAttr(WME.REWARD_STRING).getDouble();
        int left = sensors.getAttr("left").getInt();
        int right = sensors.getAttr("right").getInt();

        //If goal is set, hard-code to "G"
        if (goal > 0) return "G";

        //construct binary number
        int value = 0;
        if (left > 0) value += 2;
        if (right > 0) value += 1;

        return "" + value;
        
    }//stringify episode


    
}// [class] RoombaEnvironment
