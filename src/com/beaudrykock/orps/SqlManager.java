package com.beaudrykock.orps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Opens and manages the database
 * @author beaudry
 *
 */
public class SqlManager extends SQLiteOpenHelper {
	public static final String DBNAME = "gameplaysdb.sqlite";
	public static final int VERSION = 1;
	
	// table names
	public static final String GAME_PLAYS_TABLE_NAME = "game_plays";
	public static final String PLAYERS_TABLE_NAME = "players";
	
	// table columns
	// game plays
	public static final String GAME_ID = "game_id";
	public static final String WINNER = "winner";
	public static final String LOSER = "loser";
	public static final String WINNING_MOVE = "winning_move";
	public static final String LOSING_MOVE = "losing_move";
	
	// players
	public static final String PLAYER_NAME = "player_name";
	public static final String PLAYER_ID = "player_id";
	public static final String WINS = "wins";
	public static final String LOSSES = "losses";
	public static final String SCORE = "score";
	
	private SQLiteDatabase database = null;
	
	public SqlManager(Context context)
	{

		super(context, DBNAME, null, VERSION);
		
	}
	
	public void onCreate(SQLiteDatabase db) {
		
		createDatabase(db);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("DROP TABLE IF EXISTS "+GAME_PLAYS_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+PLAYERS_TABLE_NAME);
		  
		onCreate(db);
	}
	
	public void open() throws SQLException {
		
		database = getWritableDatabase();
		
		// for debugging purposes - set to true to drop tables when opening the DB
		if (false)
		{
			Log.d("SqlManager", "dropping tables");
			database.execSQL("DROP TABLE IF EXISTS "+GAME_PLAYS_TABLE_NAME);
			database.execSQL("DROP TABLE IF EXISTS "+PLAYERS_TABLE_NAME);
			
			onCreate(database);
		}
	  }
	
	public void close() throws SQLException {
		
		database.close();
	}
	
	private void createDatabase(SQLiteDatabase db) {
		
		db.execSQL("create table "+PLAYERS_TABLE_NAME +"(" + PLAYER_ID +" integer primary key autoincrement, " 
				+ PLAYER_NAME +" text unique, "+WINS+" integer, "+LOSSES+" integer, "+SCORE+" integer);");
		
		db.execSQL("create table "+GAME_PLAYS_TABLE_NAME +" (" + GAME_ID +" integer primary key autoincrement, "+PLAYER_ID+" integer ,"+WINNER+" integer, "+LOSER+" integer,"+WINNING_MOVE+" integer,"+
				LOSING_MOVE+" integer);");
		
		
		// always has at least one player, Android
		db.execSQL("INSERT INTO "
			     + PLAYERS_TABLE_NAME
			     + " (player_name, wins, losses, score)"
			     + " VALUES ('Android', 0, 0, 0);");
	}
	
	//================================================================================
    // Public methods
    //================================================================================
	public int getIdForName(String name)
	{
		int id = 0;
		try
		{
			if (database==null)
				database = this.getWritableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_NAME+" = \""+name+"\";", null);

			c.moveToFirst();
			if (c != null) 
			{
				id = c.getInt(c.getColumnIndex(PLAYER_ID));
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return id;
	}
	
	public String getNameForId(int id)
	{
		String name = null;
		try
		{
			if (database==null)
				database = this.getWritableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+id+";", null);

			c.moveToFirst();
			if (c != null) 
			{
				name = c.getString(c.getColumnIndex(PLAYER_NAME));
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		}
		return name;
	}
	
	public void addCompletedGameWithNames(String winnerName, String loserName, int winningMove, int losingMove, boolean bestOfThree)
	{
		int winnerID = getIdForName(winnerName);
		int loserID = getIdForName(loserName);
		addCompletedGame(winnerID, loserID, winningMove, losingMove, bestOfThree);
	}
	
	public void addCompletedGame(int winnerID, int loserID, int winningMove, int losingMove, boolean bestOfThree)
	{
		try
		{
			if (database==null)
				database = this.getWritableDatabase();
			
			// insert a new game into the game plays table
			database.execSQL("INSERT INTO "+GAME_PLAYS_TABLE_NAME+" ("+WINNER+","+LOSER+","+WINNING_MOVE+","+LOSING_MOVE+")" +
					"VALUES("+winnerID+","+loserID+","+winningMove+","+losingMove+");");
			
			// update number of wins for winning player
			int currentWins = 0;
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+winnerID+";", null);
			
			int winsCol = c.getColumnIndex(WINS);
			 
			c.moveToFirst();
			if (c != null) {
				currentWins = c.getInt(winsCol);
			}
			
			currentWins++;
			
			// update score for winning player
			int currentScore = 0;
			Cursor c_score = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+winnerID+";", null);
						
			c_score.moveToFirst();
			if (c_score != null) {
				currentScore= c_score.getInt(c_score.getColumnIndex(SCORE));
			}
						
			currentScore++;
			
			if (bestOfThree) currentScore += 2;
			
			database.execSQL("UPDATE "+PLAYERS_TABLE_NAME+" SET "+WINS+" = "+currentWins+" WHERE "+PLAYER_ID+" = "+winnerID+";");
			database.execSQL("UPDATE "+PLAYERS_TABLE_NAME+" SET "+SCORE+" = "+currentScore+" WHERE "+PLAYER_ID+" = "+winnerID+";");
			
			// update number of losses for losing player
			int currentLosses = 0;
			Cursor c1 = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+loserID+";", null);

			int lossesCol = c.getColumnIndex(LOSSES);

			c1.moveToFirst();
			if (c1 != null) {
				currentLosses = c1.getInt(lossesCol);
			}

			currentLosses++;

			database.execSQL("UPDATE "+PLAYERS_TABLE_NAME+" SET "+LOSSES+" = "+currentLosses+" WHERE "+PLAYER_ID+" = "+loserID+";");
			
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
	
	}

	public void addPlayer(String playerName)
	{
		try
		{
			if (database==null)
				database = this.getWritableDatabase();
			
			// insert if doesn't already exist
			database.execSQL("INSERT OR IGNORE INTO "+PLAYERS_TABLE_NAME+"("+PLAYER_NAME+", "+WINS+", "+LOSSES+","+SCORE+") " +
					"VALUES(\""+playerName+"\","+0+","+0+","+0+");");
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
	}
	
	public int getWinsForPlayer(int playerID)
	{
		int currentWins = 0;
		try
		{
			if (database==null)
				database = this.getWritableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+playerID+";", null);

			int winsCol = c.getColumnIndex(WINS);

			c.moveToFirst();
			if (c != null) 
			{
				//do {
				currentWins = c.getInt(winsCol);
				//}
				//while(c.moveToNext());
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return currentWins;
	}
	
	public int getScoreForPlayerName(String playerName)
	{
		return getScoreForPlayer(getIdForName(playerName));
	}
	
	public int getScoreForPlayer(int playerID)
	{
		int score = 0;
		try
		{
			if (database==null)
				database = this.getWritableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+playerID+";", null);

			int scoreCol = c.getColumnIndex(SCORE);

			c.moveToFirst();
			if (c != null) 
			{
				score = c.getInt(scoreCol);
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return score;
	}
	
	public int getLossesForPlayer(int playerID)
	{
		int currentLosses = 0;
		try
		{
			if (database==null)
				database = this.getReadableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+playerID+";", null);

			int lossesCol = c.getColumnIndex(LOSSES);

			c.moveToFirst();
			if (c != null) 
			{
				//do {
				currentLosses = c.getInt(lossesCol);
				//}
				//while(c.moveToNext());
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return currentLosses;
	}
	
	public int getLastWinningMove(int playerID)
	{
		int currentLosses = 0;
		try
		{
			if (database==null)
				database = this.getReadableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+" WHERE "+PLAYER_ID+" = "+playerID+";", null);

			int lossesCol = c.getColumnIndex(LOSSES);

			c.moveToFirst();
			if (c != null) 
			{
				currentLosses = c.getInt(lossesCol);
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return currentLosses;
	}
	
	public ArrayList<HighScore> getHighScores()
	{
		ArrayList<HighScore> scores= new ArrayList<HighScore>();
		
		try
		{
			if (database==null)
				database = this.getReadableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+PLAYERS_TABLE_NAME+";", null);
			
			Log.d("SqlManager", "count = "+Integer.toString(c.getCount()));
			if (c.getCount()>0)
			{
				c.moveToFirst();
				HighScore hs = null;
				while (!c.isAfterLast())
				{
					int playerID = c.getInt(c.getColumnIndex(PLAYER_ID));
					String playerName = c.getString(c.getColumnIndex(PLAYER_NAME));
					int score = c.getInt(c.getColumnIndex(SCORE));
					hs = new HighScore(score, playerID, playerName);
					scores.add(hs);
					c.moveToNext();
				}
				
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		
		// return sorted in descending order (compareTo method in HighScore handles reverse sorting)
		Collections.sort(scores);
		
		return scores;
	}
	
	public ArrayList<GamePlayRecord> getAllGamePlayRecords()
	{
		ArrayList<GamePlayRecord> records = new ArrayList<GamePlayRecord>();
		
		try
		{
			if (database==null)
				database = this.getReadableDatabase();
			
			Cursor c = database.rawQuery("SELECT * FROM "+GAME_PLAYS_TABLE_NAME+";", null);
			
			c.moveToFirst();
			GamePlayRecord gpr = null;
			if (!c.isAfterLast())
			{
				int winnerID = c.getInt(c.getColumnIndex(WINNER));
				int loserID = c.getInt(c.getColumnIndex(LOSER));
				int winningMove = c.getInt(c.getColumnIndex(WINNING_MOVE));
				int losingMove = c.getInt(c.getColumnIndex(LOSING_MOVE));
				String loser, winner;
				loser = getNameForId(loserID);
				winner = getNameForId(winnerID);
				gpr = new GamePlayRecord(winnerID, loserID, winningMove, losingMove, winner, loser);
				records.add(gpr);
				c.moveToNext();
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return records;
	}
	
	public int getWinningMoveLastGame()
	{
		int winningMove = Parameters.ROCK; // default is rock
		
		try
		{
			if (database==null)
				database = this.getReadableDatabase();
			Cursor c = database.rawQuery("SELECT * FROM "+GAME_PLAYS_TABLE_NAME+" " +
					"WHERE "+GAME_ID+" = (SELECT MAX("+GAME_ID+") FROM "+GAME_PLAYS_TABLE_NAME+");", null);
			c.moveToFirst();
			if (c != null) 
			{
				winningMove = c.getInt(c.getColumnIndex(WINNING_MOVE));
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return winningMove;
	}
	
	public int getLosingMoveLastGame()
	{
		int losingMove = Parameters.ROCK; // default is rock
		
		try
		{
			if (database==null)
				database = this.getReadableDatabase();
			Cursor c = database.rawQuery("SELECT * FROM "+GAME_PLAYS_TABLE_NAME+" " +
					"WHERE "+GAME_ID+" = (SELECT MAX("+GAME_ID+") FROM "+GAME_PLAYS_TABLE_NAME+");", null);
			c.moveToFirst();
			if (c != null) 
			{
				losingMove = c.getInt(c.getColumnIndex(LOSING_MOVE));
			}
		}
		catch(Exception e) {
			 Log.e("Error", "Error", e);
		} 
		return losingMove;
	}
}