package com.beaudrykock.orps;

/**
 * Class for encapsulating high score information, for ListView display in MainActivity
 * @author beaudry
 *
 */
public class HighScore implements Comparable {
	private int score;
	private int playerID;
	private String playerName;
	/**
	 * @return the score
	 */
	public int getScore() {
		return score;
	}
	/**
	 * @param score the score to set
	 */
	public void setScore(int score) {
		this.score = score;
	}
	/**
	 * @return the playerID
	 */
	public int getPlayerID() {
		return playerID;
	}
	/**
	 * @param playerID the playerID to set
	 */
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}
	/**
	 * @param playerName the playerName to set
	 */
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public HighScore(int score, int playerID, String playerName) {
		super();
		this.score = score;
		this.playerID = playerID;
		this.playerName = playerName;
	}
	
	public int compareTo(Object otherObject)
	{
		HighScore other = (HighScore)otherObject;
		if (this.score < other.score) return 1;
		if (this.score > other.score) return -1;
		return 0;
	}
	
}
