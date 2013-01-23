package com.beaudrykock.orps;

/**
 * Simple class to encapsulate a few of the game mechanics functions
 * @author beaudry
 *
 */
public class GameEngine {
	
	
	/**
	 * Finds the winner based on two throws
	 * @param humanChoice the throw chosen by human player
	 * @param androidChoice the throw chosen by computer player
	 * @return the winning player
	 */
	public int findWinner(int humanChoice, int androidChoice)
	{
		if (humanChoice == Parameters.NO_MOVE && androidChoice != Parameters.NO_MOVE)
		{
			return Parameters.ANDROID;
		}
		
		if (humanChoice != Parameters.NO_MOVE && androidChoice == Parameters.NO_MOVE)
		{
			return Parameters.HUMAN;
		}
		
		if (humanChoice == Parameters.ROCK && androidChoice == Parameters.SCISSORS)
		{
			return Parameters.HUMAN;
		}
		if (humanChoice == Parameters.ROCK && androidChoice == Parameters.PAPER)
		{
			return Parameters.ANDROID;
		}
		if (humanChoice == Parameters.SCISSORS && androidChoice == Parameters.ROCK)
		{
			return Parameters.ANDROID;
		}
		if (humanChoice == Parameters.SCISSORS && androidChoice == Parameters.PAPER)
		{
			return Parameters.HUMAN;
		}
		if (humanChoice == Parameters.PAPER && androidChoice == Parameters.ROCK)
		{
			return Parameters.HUMAN;
		}
		if (humanChoice == Parameters.PAPER && androidChoice == Parameters.SCISSORS)
		{
			return Parameters.ANDROID;
		}
		return Parameters.NO_WINNER;
	}
	
	/**
	 * Finds the right image to display in the results boxes, depending on throw and if winner or not
	 * @param move the throw chosen
	 * @param win whether or not this is a winning box
	 * @return
	 */
	public int imageForMoveAndWin(int move, boolean win)
	{
		if (win)
		{
			switch(move)
			{
			case Parameters.ROCK:
				return R.drawable.rock_win;
			case Parameters.PAPER:
				return R.drawable.paper_win;
			case Parameters.SCISSORS:
				return R.drawable.scissors_win;
			}
		}
		else
		{
			switch(move)
			{
			case Parameters.ROCK:
				return R.drawable.rock_loss;
			case Parameters.PAPER:
				return R.drawable.paper_loss;
			case Parameters.SCISSORS:
				return R.drawable.scissors_loss;
			}
		}
		return R.drawable.rock_win;
	}
}
