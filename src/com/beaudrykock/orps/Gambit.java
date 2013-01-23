package com.beaudrykock.orps;

import java.io.Serializable;
import java.util.Random;

/**
 * Gambit class holds important variables relevant for a given Gambit
 * @author beaudry
 *
 */
public class Gambit implements Comparable, Serializable {
	private int value; // number of wins
	private String description; // name of gambit
	private int useCount; // number of throws with this gambit
	private int[] moves; // throws involved in this gambit
	final static long serialVersionUID = 1000L;
	private int moveIndex = 0; // current position in cycle of move choice (0, 1 or 2 allowed)
	
	/**
	 * @return the useCount
	 */
	public int getUseCount() {
		return useCount;
	}
	/**
	 * @param useCount the useCount to set
	 */
	public void setUseCount(int useCount) {
		this.useCount = useCount;
	}
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Gambit(int value, String description, int[] moves)
	{
		this.value = value;
		this.description = description;
		this.moves = moves;
		
		this.useCount = 0;
		this.moveIndex = 0;
	}
	
	/**
	 * @return the moves
	 */
	public int[] getMoves() {
		return moves;
	}
	/**
	 * @param moves the moves to set
	 */
	public void setMoves(int[] moves) {
		this.moves = moves;
	}
	
	public int compareTo(Object otherObject)
	{
		Gambit other = (Gambit)otherObject;
		if (value < other.value) return -1;
		if (value > other.value) return 1;
		return 0;
	}
	
	/**
	 * 
	 * @return the next throw choice
	 */
	public int nextMove()
	{
		return moves[moveIndex++];	
	}
	
	/**
	 * Adds a use of the Gambit
	 */
	public void addUse()
	{
		useCount++;
	}
	
	/**
	 * Adds value to the Gambit (i.e. contributed to winning >=2 in a row)
	 */
	public void gainValue()
	{
		value++;
	}
	
	/**
	 * Didn't contribute a win
	 */
	public void loseValue()
	{
		if (value>0) value--;
	}
	
	/**
	 * Has Gambit reached end of moves
	 * @return
	 */
	public boolean isFinished()
	{
		return (moveIndex==3);
	}
	
	/**
	 * Resets move index counter
	 */
	public void reset()
	{
		moveIndex = 0;
	}
	
}
