package com.beaudrykock.orps;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Play extends Activity implements SensorEventListener {

	private TextView tv_difficultyLevel; // difficulty level text view
	private TextView prepText; // text displaying prep messages
	private TextView humanScore; // score human achieves
	private TextView androidScore; // score android achieves
	private ImageView redLight;
	private ImageView orangeLight;
	private ImageView greenLight;
	private ImageView humanChoice; // image displaying human choice
	private ImageView androidChoice; // image displaying android choice
	private SettingsManager settingsManager = null;
	private int state = Parameters.OFF;
	private int timer_interval = 1000; // length of time each light shows for
	private Handler timer_action_handler;
	private boolean gameActive; // true if game is active
	private boolean gamePrepped; // true if game is ready to start
	private int humanMove = 0; // human throw
	private int androidMove = 0; // android throw
	private GameEngine gameEngine = null;
	private AndroidBrain androidBrain; // android strategy engine
	private SqlManager dbManager; // database manager
	private int gameCount; // reset to 0 every time Play Activity starts
	private int playerWins; // wins in current best of 3
	private int androidWins; // wins in current best of 3
	private boolean postGame; // true if in post-game period
	private ImageView animatedChoice; // animation of tapping a choice button
	
	// shaking: not currently activated, but available if curious!
	private float lastX, lastY, lastZ;
    private boolean sensorInitialized;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final float NOISE = (float) 8.0;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.playscreen);
		
		gameEngine = new GameEngine();
		redLight = (ImageView) findViewById(R.id.iv_redLight);
		orangeLight = (ImageView) findViewById(R.id.iv_orangeLight);
		greenLight = (ImageView) findViewById(R.id.iv_greenLight);
		humanChoice = (ImageView) findViewById(R.id.iv_humanChoice);
		androidChoice = (ImageView) findViewById(R.id.iv_androidChoice);
		prepText = (TextView) findViewById(R.id.prepText);
		humanScore = (TextView) findViewById(R.id.humanScoreTxt);
		androidScore = (TextView) findViewById(R.id.androidScoreTxt);
		settingsManager = new SettingsManager(this);
		androidBrain = new AndroidBrain(this, settingsManager.getDifficultyLevel());
		gamePrepped = true;
		dbManager = new SqlManager(this);
		dbManager.open();
		gameCount = 0;
		playerWins = 0;
		androidWins = 0;
		postGame = false;
		tv_difficultyLevel = (TextView) findViewById(R.id.tv_difficultyLevel);
		
		updateDifficultyLevelIndicator();
		updateScores();
		addMoveButtonListeners();
		
		// If want to use a shake function to prime the throw
//		if (!settingsManager.shouldAutoprime())
//		{
//			initializeShakeListener();
//		}
//		else
//		{
//			startGame();
//		}
		
		startGame();
	}
	
	private void initializeShakeListener()
	{
		 sensorInitialized = false;
	     sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	     accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	     sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * Updates the difficulty level textview and layout based on what settings manager
	 * records the difficulty level as
	 */
	private void updateDifficultyLevelIndicator()
	{
		View diffView = findViewById(R.id.ll_difficulty);
		int diffLevel = settingsManager.getDifficultyLevel();
		
		switch(diffLevel)
		{
		case Parameters.NEWBIE:
			diffView.setBackgroundColor(getResources().getColor(R.color.opower_blue));
			tv_difficultyLevel.setText("Difficulty Level: Newbie");
			break;
		case Parameters.AMATEUR:
			diffView.setBackgroundColor(getResources().getColor(R.color.diff_color_amateur));
			tv_difficultyLevel.setText("Difficulty Level: Amateur");
			break;
		case Parameters.ENTHUSIAST:
			diffView.setBackgroundColor(getResources().getColor(R.color.diff_color_enthusiast));
			tv_difficultyLevel.setText("Difficulty Level: Enthusiast");
			break;
		case Parameters.SEMIPRO:
			diffView.setBackgroundColor(getResources().getColor(R.color.diff_color_semipro));
			tv_difficultyLevel.setText("Difficulty Level: Semi-pro");
			break;
		case Parameters.PRO:
			diffView.setBackgroundColor(getResources().getColor(R.color.diff_color_pro));
			tv_difficultyLevel.setText("Difficulty Level: Pro");
			break;
		}
		
	}
	
	//================================================================================
    // Lifecycle
    //================================================================================

	protected void onResume() {
        super.onResume();
        dbManager.open();
        if (sensorManager != null)
        	sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        try
        {
        	dbManager.close();
        }
        catch (Exception e)
        {
        	Log.e("Error", "Error", e);
        }
        
        androidBrain.onEndActivity();
        
        if (sensorManager != null)
        	sensorManager.unregisterListener(this);
    }
    
    protected void onStop()
    {
    	super.onStop();
    	
    	androidBrain.onEndActivity();
    	try
        {
        	dbManager.close();
        }
        catch (Exception e)
        {
        	Log.e("Error", "Error", e);
        }
    	
        if (sensorManager != null)
        	sensorManager.unregisterListener(this);
    }

    //================================================================================
    // Sensors
    //================================================================================

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignored in bare bones
    }

    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        if (!sensorInitialized) {
            lastX = x;
            lastY = y;
            lastZ = z;
            sensorInitialized = true;
        } else {
            float deltaX = Math.abs(lastX - x);
            float deltaY = Math.abs(lastY - y);
            float deltaZ = Math.abs(lastZ - z);
            if (deltaX < NOISE) deltaX = (float)0.0;
            if (deltaY < NOISE) deltaY = (float)0.0;
            if (deltaZ < NOISE) deltaZ = (float)0.0;
            lastX = x;
            lastY = y;
            lastZ = z;
            if (deltaX > deltaY) {
            	if (gameActive)
   	    	 {	
            		updateLights();
   	    	 }
            } else if (deltaY > deltaX) {
            	if (gameActive)
      	    	 {	
               		updateLights();
      	    	 }
            }
        }
    }
	
    //================================================================================
    // Game play
    //================================================================================
	/**
	 * Grabs the latest scores for Android and human player, using the player name
	 * passed in the intent
	 */
	private void updateScores()
	{
		String name = settingsManager.getCurrentPlayerName();
		
		int playerScore = dbManager.getScoreForPlayerName(name); 
		int robotScore = dbManager.getScoreForPlayerName("Android"); 
		
		humanScore.setText(Integer.toString(playerScore)+" | "+Integer.toString(playerWins)+"/3");
		androidScore.setText(Integer.toString(robotScore)+" | "+Integer.toString(androidWins)+"/3");
	}
	
	/**
	 * Adds appropriate listeners to the throwing choices
	 * and handles different game state cases to prevent throw
	 * being made too early, etc
	 */
	public void addMoveButtonListeners()
	{
		ImageButton rockButton = (ImageButton) findViewById(R.id.btn_rock);
		rockButton.setTag("rock");
		
		ImageButton paperButton = (ImageButton) findViewById(R.id.btn_paper);
		paperButton.setTag("paper");
		
		ImageButton scissorsButton = (ImageButton) findViewById(R.id.btn_scissors);
		scissorsButton.setTag("scissors");
		
		OnClickListener buttonClick = new OnClickListener() {
 			public void onClick(View clickedView) {
 				ImageButton button = (ImageButton)clickedView;
 				
 				if (!gameActive && !gamePrepped && !postGame)
 				{
	 				showTooLateAlert();
 				}
 				else if (!gameActive && !gamePrepped && postGame)
 				{
	 				// do nothing
 				}
 				else if (!gameActive && gamePrepped)
 				{
 					// do nothing - game not started yet
 				}
 				else if (gameActive  && gamePrepped && state==Parameters.GREEN)
 				{
 					
 					if (button.getTag().toString().equalsIgnoreCase("rock"))
	 				{
 						RunAnimations(R.drawable.rock_anim, R.id.btn_rock_frame);
 						humanMove = Parameters.ROCK;
 						humanChoice.setImageResource(R.drawable.rock_choice);
	 				}
	 				else if (button.getTag().toString().equalsIgnoreCase("paper"))
	 				{
	 					RunAnimations(R.drawable.paper_anim, R.id.btn_paper_frame);
	 					humanMove = Parameters.PAPER;
	 					humanChoice.setImageResource(R.drawable.paper_choice);
	 				}
	 				else
	 				{
	 					RunAnimations(R.drawable.scissors_anim, R.id.btn_scissors_frame);
	 					humanMove = Parameters.SCISSORS;
	 					humanChoice.setImageResource(R.drawable.scissors_choice);
	 				}
 					androidChoice();
 				}
			}
		};
		
		rockButton.setOnClickListener(buttonClick);
		paperButton.setOnClickListener(buttonClick);
		scissorsButton.setOnClickListener(buttonClick);
	}
	
	/**
	 * Runs animations on the throw choice button
	 * @param imageID the animation image to use
	 * @param containerID the framelayout into which the animation should be added
	 */
	private void RunAnimations(int imageID, int containerID) {
	    Animation a = AnimationUtils.loadAnimation(this, R.anim.move_choice_anim_set);
	    a.reset();
	    a.setAnimationListener(new AnimationListener() {
	        public void onAnimationStart(Animation animation) {}
	        public void onAnimationRepeat(Animation animation) {}
	        public void onAnimationEnd(Animation animation) {
	            ((ViewGroup)animatedChoice.getParent()).removeView(animatedChoice);
	        }
	    });
	    
	    animatedChoice = new ImageView(this);
	    LinearLayout.LayoutParams vp = 
	        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
	                        LayoutParams.WRAP_CONTENT, Gravity.CENTER);
	    animatedChoice.setLayoutParams(vp);        
	    animatedChoice.setImageResource(imageID);        
	    animatedChoice.bringToFront();
	    prepText.invalidate();
	    FrameLayout container = (FrameLayout)findViewById(containerID);
	    container.addView(animatedChoice); 
	    
	    animatedChoice.clearAnimation();
	    animatedChoice.startAnimation(a);
	}
	
	/**
	 * Calls the AndroidBrain instance to choose a throw
	 * Also sets the relevant image in the android choice box
	 */
	public void androidChoice()
	{
		androidMove = androidBrain.chooseThrow();
		
		if (androidMove == Parameters.ROCK)
		{
			androidChoice.setImageResource(R.drawable.rock_choice);
		}
		else if (androidMove == Parameters.PAPER)
		{
			androidChoice.setImageResource(R.drawable.paper_choice);
		}
		else if (androidMove == Parameters.SCISSORS)
		{
			androidChoice.setImageResource(R.drawable.scissors_choice);
		}
	}
	
	/**
	 * Resets choice images, preps game, starts it
	 */
	public void newGame()
	{
		androidChoice.setImageResource(R.drawable.move_placeholder);
		humanChoice.setImageResource(R.drawable.move_placeholder);
		gamePrepped = true;
		startGame();
	}
	
	/**
	 * Alert displayed if throw made after game period ended
	 */
	public void showTooLateAlert()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("Too late!")
		.setMessage("Sorry - missed your chance")
		.setPositiveButton("OK", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				newGame();
			}
		})
		.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
		
	}
	
	/**
	 * Alert displayed if no winner
	 */
	private void showNoWinnerAlert()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("No winner")
		.setMessage("Sorry - no winner this time")
		.setPositiveButton("New game", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				newGame();
			}
		})
		.setNegativeButton("Back to start screen", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				backToStartScreen();
			}
		})
		.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
		
	}
	
	/**
	 * Alert displayed if human winner
	 */
	private void showHumanWinnerAlert()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("Way to go!")
		.setMessage("You put the robot in its place. Another game?")
		.setPositiveButton("Yeah - hit me", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				newGame();
			}
		})
		.setNegativeButton("Nope - I want out", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				backToStartScreen();
			}
		})
		.create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.show();
		
	}
	
	/**
	 * Alert displayed if the android wins
	 */
	private void showAndroidWinnerAlert()
	{
		AlertDialog alertDialog = new AlertDialog.Builder(this)
		.setTitle("Cyborgs don't feel pain")
		.setMessage("The 'droid won this time. Want to try again?")
		.setPositiveButton("Yeah, bring it on", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				newGame();
			}
		})
		.setNegativeButton("Nope - I want out", new AlertDialog.OnClickListener(){
			public void onClick(DialogInterface dialog, int which){
				backToStartScreen();
			}
		})
		.create();
		alertDialog.show();
		
	}
	
	/**
	 * Kicks back to the MainActivity, without splash
	 */
	private void backToStartScreen()
	{
		Intent intent = new Intent(Play.this, MainActivity.class);
		intent.putExtra("noSplash", "noSplash");
		startActivity(intent);
	}
	
	/**
	 * Timer to call updates on game state
	 */
	Runnable updateLightsTask = new Runnable()
	{
	     @Override 
	     public void run() {
	    	 if (gameActive)
	    	 {
		          updateLights(); 
		          timer_action_handler.postDelayed(updateLightsTask, timer_interval);
	    	 }
	     }
	};
	
	/**
	 * Updates game state
	 */
	public void updateLights()
	{
		if (state==Parameters.OFF)
		{
			redLight.setImageResource(R.drawable.red_light);
			state=Parameters.RED;
		}
		else if (state==Parameters.RED)
        {
     	   redLight.setImageResource(R.drawable.off_light);
     	   orangeLight.setImageResource(R.drawable.orange_light);
     	   state=Parameters.ORANGE;
     	   prepText.setText("Set?");
     	   
     	   if (androidBrain.isObfuscating())
     	   {
     		   int psychChoice = androidBrain.getObfuscationChoice();
     		   switch (psychChoice)
     		   {
     		   case Parameters.ROCK:
     			  androidChoice.setImageResource(R.drawable.rock_psych);
     			   break;
     		   case Parameters.PAPER:
     			  androidChoice.setImageResource(R.drawable.paper_psych);
     			   break;
     		   case Parameters.SCISSORS:
     			  androidChoice.setImageResource(R.drawable.scissors_psych);
     			   break;
     		   }
     		  
     	   }
        }
        else if (state==Parameters.ORANGE)
        {
     	   orangeLight.setImageResource(R.drawable.off_light);
     	   greenLight.setImageResource(R.drawable.green_light);
     	   state=Parameters.GREEN;
     	   prepText.setText("Pick your move!");
        }
        else
        {
        	postGame = true;
        	greenLight.setImageResource(R.drawable.off_light);
        	state=Parameters.OFF;
        	prepText.setText("Game over");
        	endGame();
        }
	}

	/**
	 * Ends the game; de-preps, sets inactive, removes timer.
	 * Finds the winner, shows appropriate alerts
	 */
	void endGame()
	{
		gamePrepped = false;
		gameActive = false;
	    timer_action_handler.removeCallbacks(updateLightsTask);
	    
	    // evaluate winner
	    int winner = gameEngine.findWinner(humanMove, androidMove);
	    
	    androidBrain.gameEnded(winner, humanMove);
	    
	    if (winner == Parameters.NO_WINNER)
	    {
	    	showNoWinnerAlert();
	    }
	    else if (winner==Parameters.HUMAN)
	    {
	    	gameCount++;
	    	humanChoice.setImageResource(gameEngine.imageForMoveAndWin(humanMove, true));
	    	androidChoice.setImageResource(gameEngine.imageForMoveAndWin(androidMove, false));
	    	addCompletedGameWithWinForPlayer(Parameters.HUMAN);
	    	
	    	new Handler().postDelayed(new Runnable() {
	    	    @Override
	    	    public void run() {
	    	    	showHumanWinnerAlert();
	    	    }
	    	}, 1000);
	    	
	    	
	    }
	    else if (winner==Parameters.ANDROID){
	    	gameCount++;
	    	humanChoice.setImageResource(gameEngine.imageForMoveAndWin(humanMove, false));
	    	androidChoice.setImageResource(gameEngine.imageForMoveAndWin(androidMove, true));
	    	addCompletedGameWithWinForPlayer(Parameters.ANDROID);
	    	new Handler().postDelayed(new Runnable() {
	    	    @Override
	    	    public void run() {
	    	    	showAndroidWinnerAlert();
	    	    }
	    	}, 1000);
	    }
	    
	}
	
	/**
	 * Records win in the database
	 * @param playerType human or android winner
	 */
	private void addCompletedGameWithWinForPlayer(int playerType)
	{
		String name = settingsManager.getCurrentPlayerName();
		boolean winnerOfBOT = false;
		
		if (playerType == Parameters.HUMAN)
		{
			playerWins++;
			if (playerWins==3) winnerOfBOT = true;
			dbManager.addCompletedGameWithNames(name, "Android", humanMove, androidMove, winnerOfBOT);
		}
		else
		{
			androidWins++;
			if (androidWins==3) winnerOfBOT = true;
			dbManager.addCompletedGameWithNames("Android", name, androidMove, humanMove, winnerOfBOT);
		}
		
		if (gameCount%3==0 && gameCount>2)
		{
			playerWins = 0;
			androidWins = 0;
		}
		
		updateScores();
	}
	
	/**
	 * Starts a new game; resets parameters; re-creates timer handler
	 */
	private void startGame()
	{
		humanMove = Parameters.NO_MOVE;
		androidMove = Parameters.NO_MOVE;
		
		timer_action_handler = new Handler();
		timer_action_handler.removeCallbacks(updateLightsTask);
        timer_action_handler.postDelayed(updateLightsTask, timer_interval);
   	
		gameActive = true;
		postGame = false;
	}
	
	
	
}
