package com.beaudrykock.orps;

import android.content.Context;

import android.content.SharedPreferences;

/**
 * Class for recording various settings, just difficulty level and player name at present
 * @author beaudry
 *
 */
public class SettingsManager {
	
	private Context context;
	
	public SettingsManager(Context context)
	{
		this.context = context;
	}
	
	/**
	 * Increase the difficulty if not already Pro level
	 */
	public void increaseDifficulty()
	{
		int currentDiff = getDifficultyLevel();
		
		if (currentDiff < Parameters.PRO)
		{
			currentDiff++;
			
			updateDifficultyLevel(currentDiff);
		}
	}
	
	/**
	 * Decrease the difficulty if not already newbie level
	 */
	public void decreaseDifficulty()
	{
		int currentDiff = getDifficultyLevel();
		
		if (currentDiff > Parameters.NEWBIE)
		{
			currentDiff--;
			
			updateDifficultyLevel(currentDiff);
		}
	}
	
	private void updateDifficultyLevel(int newDifficultyLevel)
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("difficultyLevel", newDifficultyLevel);
		editor.commit();
	}
	
	public void updateSettings(int difficultyLevel)
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.putInt("difficultyLevel", difficultyLevel);
		editor.commit();
	}
	
	public int getDifficultyLevel()
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);
		
		return preferences.getInt("difficultyLevel", Parameters.NEWBIE);
	}
	
	public String getDifficultyLevelAsString()
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);
		
		int diff = preferences.getInt("difficultyLevel", Parameters.NEWBIE);
		String level = null;
		switch(diff)
		{
		case Parameters.NEWBIE:
			level = "Newbie";
		break;
		case Parameters.AMATEUR:
			level = "Amateur";
		break;
		case Parameters.ENTHUSIAST:
			level = "Enthusiast";
		break;
		case Parameters.SEMIPRO:
			level = "Semi-pro";
		break;
		case Parameters.PRO:
			level = "Pro";
		break;
		default:
			level = "Newbie";
			break;
		}
		return level;
	}
	
	public String getCurrentPlayerName()
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);
		
		return preferences.getString("playerName", "NO_NAME");
	}
	
	public void updatePlayerName(String name)
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("playerName");
		editor.putString("playerName", name);
		editor.commit();
	}
	
	public void resetPreferences()
	{
		SharedPreferences preferences = context.getSharedPreferences("MyPreferences", android.content.Context.MODE_PRIVATE);  
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("playerName");
		editor.remove("difficultyLevel");
		editor.putInt("difficultyLevel", 1);
		editor.putString("playerName", "NO_NAME");
		editor.commit();
	}
}
