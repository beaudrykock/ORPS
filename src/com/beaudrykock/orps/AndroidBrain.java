package com.beaudrykock.orps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import android.content.Context;
import android.util.Log;

public class AndroidBrain  {
	
	private int strategy = Parameters.NEWBIE; // defines Android strategy
	private int gameCount; // number of games played this Activity
	private int opponentChoiceLastGame; // tracks human's choice last game
	private HashMap<String, Serializable> gambits = null; // stores the Gambits used by the Android
	private Gambit currentGambit = null; // stores the current Gambit
	private MaxHeap gambitHeap = null; // max-heap for storing most valued Gambits
	private Context context;
	
	/**
	 * @return the strategy
	 */
	public int getStrategy() {
		return strategy;
	}

	/**
	 * @param strategy the strategy to set
	 */
	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}

	/**
	 * @return the gamesThisActivity
	 */
	public int getGameCount() {
		return gameCount;
	}

	/**
	 * @param gamesThisActivity the gamesThisActivity to set
	 */
	public void setGameCounty(int gameCount) {
		this.gameCount = gameCount;
	}

	/**
	 * @return the opponentChoiceLastGame
	 */
	public int getOpponentChoiceLastGame() {
		return opponentChoiceLastGame;
	}

	/**
	 * @param opponentChoiceLastGame the opponentChoiceLastGame to set
	 */
	public void setOpponentChoiceLastGame(int opponentChoiceLastGame) {
		this.opponentChoiceLastGame = opponentChoiceLastGame;
	}

	public AndroidBrain()
	{
		
	}
	
	public AndroidBrain(Context context, int strategy)
	{
		this.context = context;
		this.strategy = strategy;
		boolean read = false;
		opponentChoiceLastGame = Parameters.NO_MOVE;
		
		try
		{
			read = readGambitsFromFile();
		}
		catch (Exception e)
		{
			Log.e("Error", "Error", e);
		}
		
		if (!read)
		{
			Log.d("AndroidBrain", "Creating gambits afresh");
			gambits = new HashMap<String, Serializable>();
			gambits.put(Parameters.AVALANCHE, new Gambit(1,Parameters.AVALANCHE, new int[]{Parameters.ROCK,Parameters.ROCK,Parameters.ROCK}));
			gambits.put(Parameters.BUREAUCRAT, new Gambit(1,Parameters.BUREAUCRAT, new int[]{Parameters.PAPER,Parameters.PAPER,Parameters.PAPER}));
			gambits.put(Parameters.CRESCENDO, new Gambit(1,Parameters.CRESCENDO, new int[]{Parameters.PAPER,Parameters.SCISSORS,Parameters.ROCK}));
			gambits.put(Parameters.DENOUEMENT, new Gambit(1,Parameters.DENOUEMENT, new int[]{Parameters.ROCK,Parameters.SCISSORS,Parameters.PAPER}));
			gambits.put(Parameters.FISTFUL, new Gambit(1,Parameters.FISTFUL, new int[]{Parameters.ROCK,Parameters.PAPER,Parameters.PAPER}));
			gambits.put(Parameters.PAPERDOLLS, new Gambit(1,Parameters.PAPERDOLLS, new int[]{Parameters.PAPER,Parameters.SCISSORS,Parameters.SCISSORS}));
			gambits.put(Parameters.SCISSORSANDWICH, new Gambit(1,Parameters.SCISSORSANDWICH, new int[]{Parameters.PAPER,Parameters.SCISSORS,Parameters.PAPER}));
			gambits.put(Parameters.TOOLBOX, new Gambit(1,Parameters.TOOLBOX, new int[]{Parameters.SCISSORS,Parameters.SCISSORS,Parameters.SCISSORS}));
		}
		
		pickNextGambit();
	}
	
	/**
	 * 
	 * @return true if difficulty strategy level is enthusiast or higher
	 */
	public boolean isObfuscating()
	{
		return strategy >= Parameters.ENTHUSIAST;
	}
	
	/**
	 * 
	 * @return the obfuscation choice
	 */
	public int getObfuscationChoice()
	{
		int choice;
		
		choice = randomThrow();
		
		return choice;
	}
	
/**
 * Generates a new max heap and adds all the gambits to the heap
 */
	private void generateGambitHeap()
	{
		gambitHeap = new MaxHeap();
		
		for (String string : gambits.keySet())
		{
			gambitHeap.add((Gambit)gambits.get(string));
		}
	}
	
	/**
	 * Choose a throw based on the difficult level of the game.
	 * Newbies simply choose whatever the opponent chose last time
	 * Amateurs choose randomly
	 * Enthusiasts and higher choose based on the current Gambit
	 * @return
	 */
	public int chooseThrow()
	{
		int move = Parameters.NO_MOVE;
		gameCount++;
		
		switch(strategy)
		{
		case Parameters.NEWBIE:
			move = opponentMatchThrow();
			break;
			
		case Parameters.AMATEUR:
			move = randomThrow();
			break;
			
		case Parameters.ENTHUSIAST:
			move = currentGambit.nextMove();
			break;
			
		case Parameters.SEMIPRO:
			move = currentGambit.nextMove();
			break;
			
		case Parameters.PRO:
			move = currentGambit.nextMove();
			break;
		}
		
		return move;
	}

	/**
	 * 
	 * @return a random throw
	 */
	private int randomThrow()
	{
		int move = Parameters.NO_MOVE;
		Random generator = new Random();
		move = generator.nextInt(3);
		if (move==0) move++;
		return move;
	}
	
	/**
	 * 
	 * @return whatever the opponent picked last time
	 */
	private int opponentMatchThrow()
	{
		if (opponentChoiceLastGame==Parameters.NO_MOVE)
			opponentChoiceLastGame = randomThrow();
			
		return opponentChoiceLastGame;
	}
	
	/**
	 * Called from the Play activity; allows AndroidBrain to update its stats
	 * @param winner the winner of the game
	 * @param opponentChoice the opponent's last choice
	 */
	public void gameEnded(int winner, int opponentChoice)
	{
		setOpponentChoiceLastGame(opponentChoice);
		
		if (currentGambit!=null)	
		{
			if (winner == Parameters.ANDROID)
			{
				currentGambit.gainValue();
			}
			else
			{
				currentGambit.loseValue();
			}
						
			if (currentGambit.isFinished())
			{
				currentGambit.reset();
				
				// only pro-level uses heap
				if (strategy == Parameters.PRO)
					gambitHeap.add(currentGambit);
				
				pickNextGambit();
			}
		}
	}
	
	/**
	 * Picking the next Gambit
	 * Enthusiasts get the least used gambit
	 * Semi-pro picks randomly
	 * Pro picks the highest value Gambit
	 */
	private void pickNextGambit()
	{
		Random generator = new Random();
		Object[] gambitNames = gambits.keySet().toArray();
		
		switch(strategy)
		{
		
		case Parameters.ENTHUSIAST:
			// pick least used gambit
			int smallestCount = Integer.MAX_VALUE;
			String smallestGambitName = Parameters.AVALANCHE; // default
			for (String gambitName : gambits.keySet())
			{
				Gambit gambit = (Gambit)gambits.get(gambitName);
				if (gambit.getUseCount()<smallestCount)
				{
					smallestCount = gambit.getUseCount();
					smallestGambitName = gambitName;
				}
			}
			
			currentGambit = (Gambit)gambits.get(smallestGambitName);
			currentGambit.addUse();
			
			break;
			
		case Parameters.SEMIPRO:
			
			// pick gambit randomly
			String name = (String)gambitNames[generator.nextInt(gambitNames.length)];
			currentGambit = (Gambit)gambits.get(name);
			currentGambit.addUse();
			
			break;
			
		case Parameters.PRO:
			// pick highest value gambit
			if (gambitHeap==null)
				generateGambitHeap();
			currentGambit = (Gambit)gambitHeap.remove();
			currentGambit.addUse();
			break;
		}
	}
	
	/**
	 * Writes Gambits to file for next time Play activity is launched
	 * Gives the AndroidBrain some memory
	 * @throws IOException
	 */
	private void writeGambitsToFile() throws IOException
	{
		Log.d("AndroidBrain", "Writing gambits to file");
		for (String key : gambits.keySet())
		{
			Gambit gambit = (Gambit)gambits.get(key);
			Log.d("AndroidBrain", "Gambit uses, value = "+gambit.getUseCount()+", "+gambit.getValue());
		}
		
		String filePath = context.getFilesDir().getPath().toString() + "/gabmits";
		File file = new File(filePath);
		FileOutputStream f = new FileOutputStream(file);  
		ObjectOutputStream s = new ObjectOutputStream(f);          
		s.writeObject(gambits);
		s.flush();
		s.close();
	}
	
	/**
	 * Reads Gambit hashmap from memory
	 * @return true if successfully read
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private boolean readGambitsFromFile() throws IOException, ClassNotFoundException
	{
		Log.d("AndroidBrain", "Beginning to read gambits from file");
		boolean read = false;
		
		String filePath = context.getFilesDir().getPath().toString() + "/gabmits";
		File file = new File(filePath);  
		if (file.exists())
		{
			FileInputStream f = new FileInputStream(file);  
			ObjectInputStream s = new ObjectInputStream(f);          
			gambits = (HashMap<String, Serializable>)s.readObject();
			s.close();
			read = true;
			Log.d("AndroidBrain", "read gambits from file, object count "+gambits.size());
		}
		
		return read;
	}
	
	/**
	 * Called from lifecycle methods in Play activity
	 * Writes all the Gambits to file
	 */
	public void onEndActivity()
	{
		try
		{
			writeGambitsToFile();
		}
		catch (IOException e)
		{
			Log.e("Error", "Error", e);
		}
	}

}