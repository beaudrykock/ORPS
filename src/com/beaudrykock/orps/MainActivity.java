package com.beaudrykock.orps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ViewFlipper;
import android.view.View;
import android.widget.TextView;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	private ImageButton playButton;
	private TextView title;
	private TextView welcomeText; // text at top of screen
	private EditText playerNameTextField; // text field for entering player name
	private ListView gamePlayList; // list of high scores, game plays
	private int counter = 0;
	private String currentPlayerName = null; // current player name
	private TextView tv_difficultyLevelLabel = null; // difficulty level
	private ImageButton ib_decreaseDiff = null; // button to increase difficulty
	private ImageButton ib_increaseDiff = null; // button to decrease difficulty
	private SettingsManager settingsManager = null; // settings manager 
	private SqlManager dbManager = null; // database manager
	//@Override
	
	private static final int MAX_SPLASH_SECONDS = 2;
	private Dialog splashDialog;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null)
		{
			String noSplash = extras.getString("noSplash");
			if (!noSplash.equalsIgnoreCase("noSplash"))
				showSplashScreen();
				
		}
		else
		{
			showSplashScreen();
		}
		
		setContentView(R.layout.activity_main);
		
		settingsManager = new SettingsManager(this);
		playButton = (ImageButton)findViewById(R.id.ib_play);
		title = (TextView)findViewById(R.id.textView1);
		gamePlayList = (ListView)findViewById(R.id.gamePlayList);
		tv_difficultyLevelLabel = (TextView)findViewById(R.id.difficultyLevelLabel);
		tv_difficultyLevelLabel.setText(settingsManager.getDifficultyLevelAsString());
		ib_decreaseDiff = (ImageButton)findViewById(R.id.ib_decreaseDiff);
		ib_increaseDiff = (ImageButton)findViewById(R.id.ib_increaseDiff);
		playerNameTextField = (EditText)findViewById(R.id.et_playerName);
		welcomeText = (TextView)findViewById(R.id.tv_welcome);
		
		// kick off database, etc
		dbManager = new SqlManager(this);
		dbManager.open();
		
		List<HighScore> values = dbManager.getHighScores();
		CustomAdapter adapter = new CustomAdapter(this, 
                R.layout.game_play_record_item, values);
        
        View header = (View)getLayoutInflater().inflate(R.layout.game_play_list_header_view, null);
        gamePlayList.addHeaderView(header);
        gamePlayList.setAdapter(adapter);
		
        // DEBUGGING ONLY
        //settingsManager.resetPreferences();
        
        // get current player name from settings
        currentPlayerName = settingsManager.getCurrentPlayerName();
        
        // if no player stored
        if (currentPlayerName.equalsIgnoreCase("NO_NAME"))
        {
        	welcomeText.setText("Welcome new player!");
        }
        // otherwise welcome the player
        else
        {
        	welcomeText.setText("Welcome "+currentPlayerName+"!");
        	playerNameTextField.setHint("or enter a new player name");
        }
        
        // for launching Play
		playButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (checkPlayerName())
				{
					Intent intent = new Intent(MainActivity.this, Play.class);
					startActivity(intent);
				}
			}
		});
		
		// increasing the difficulty level
		ib_decreaseDiff.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				settingsManager.decreaseDifficulty();
				tv_difficultyLevelLabel.setText(settingsManager.getDifficultyLevelAsString());
			}
		});
		
		// reducing the difficulty level
		ib_increaseDiff.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				settingsManager.increaseDifficulty();
				tv_difficultyLevelLabel.setText(settingsManager.getDifficultyLevelAsString());
			}
		});
	}
	
	/**
	 * Removing the splash screen
	 */
	private void removeSplashScreen() {
		if (splashDialog != null) {
			splashDialog.dismiss();
			splashDialog = null;
		}
	}

	/**
	 * Showing the splash screen
	 */
	private void showSplashScreen() {
		
		splashDialog = new Dialog(this, android.R.style.Theme_Light);
		splashDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); 
		splashDialog.setContentView(R.layout.splashscreen); 
		splashDialog.setCancelable(false);
		splashDialog.show();

		// Start background Handler to cancel it, to be save
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				removeSplashScreen();
			}	
		}, MAX_SPLASH_SECONDS * 1000);
	}
		
	/**
	 * Runs checks to see whether the player name has been set, or if one was set
	 * but a new one was re-set, or if none was set at all, initiate warning to 
	 * set it
	 * @return true if playername is now set
	 */
	public boolean checkPlayerName()
	{
		boolean nameIncluded = false;
		String textFieldContents = playerNameTextField.getText().toString();
		
		if (currentPlayerName.equalsIgnoreCase("NO_NAME") && 
				playerNameTextField.getText().length()==0 || 
				playerNameTextField.getText().toString().equalsIgnoreCase("enter player name"))
		{
			AlertDialog alertDialog = new AlertDialog.Builder(this)
			.setTitle("Player name missing")
			.setMessage("Please enter a name before proceeding")
			.setPositiveButton("OK", new AlertDialog.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					// nothing needs to happen on click
				}
			})
			.create();
			alertDialog.show();
		}
		else if (currentPlayerName.equalsIgnoreCase("NO_NAME"))
		{
			currentPlayerName = playerNameTextField.getText().toString();
			settingsManager.updatePlayerName(currentPlayerName);
			dbManager.addPlayer(currentPlayerName);
			nameIncluded = true;
		}
		else if (!currentPlayerName.equalsIgnoreCase("NO_NAME") && textFieldContents.equalsIgnoreCase("or enter a new player name"))
		{
			nameIncluded = true;
		}
		else if (!currentPlayerName.equalsIgnoreCase("NO_NAME") && !textFieldContents.equalsIgnoreCase("or enter a new player name"))
		{
			currentPlayerName = playerNameTextField.getText().toString();
			settingsManager.updatePlayerName(currentPlayerName);
			dbManager.addPlayer(currentPlayerName);
			nameIncluded = true;
		}
		
		return nameIncluded;
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void onClick(View view) {
		switch(view.getId())
		{
		
		
		}
	}
}
