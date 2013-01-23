package com.beaudrykock.orps;

/**
 * Class for encapsulating details about a given game play;
 * not currently used
 * @author beaudry
 *
 */
public class GamePlayRecord {
	private int winnerID;
	private int loserID;
	private int winningMove;
	private int losingMove;
	private String winnerName;
	private String loserName;
	
	public GamePlayRecord(int winnerID, int loserID, int winningMove,
			int losingMove, String winnerName, String loserName) {
		super();
		this.winnerID = winnerID;
		this.loserID = loserID;
		this.winningMove = winningMove;
		this.losingMove = losingMove;
		this.winnerName = winnerName;
		this.loserName = loserName;
	}
	/**
	 * @return the winnerName
	 */
	public String getWinnerName() {
		return winnerName;
	}
	/**
	 * @return the loserName
	 */
	public String getLoserName() {
		return loserName;
	}
	
	/**
	 * @return the winnerID
	 */
	public int getWinnerID() {
		return winnerID;
	}
	/**
	 * @param winnerID the winnerID to set
	 */
	public void setWinnerID(int winnerID) {
		this.winnerID = winnerID;
	}
	/**
	 * @return the loserID
	 */
	public int getLoserID() {
		return loserID;
	}
	/**
	 * @param loserID the loserID to set
	 */
	public void setLoserID(int loserID) {
		this.loserID = loserID;
	}
	/**
	 * @return the winningMove
	 */
	public int getWinningMove() {
		return winningMove;
	}
	/**
	 * @param winningMove the winningMove to set
	 */
	public void setWinningMove(int winningMove) {
		this.winningMove = winningMove;
	}
	/**
	 * @return the losingMove
	 */
	public int getLosingMove() {
		return losingMove;
	}
	/**
	 * @param losingMove the losingMove to set
	 */
	public void setLosingMove(int losingMove) {
		this.losingMove = losingMove;
	}
}
