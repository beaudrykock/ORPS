package com.beaudrykock.orps;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Class to connect SQL results to ListView in MainActivity
 * @author beaudry
 *
 */
public class CustomAdapter extends ArrayAdapter<HighScore> {

	Context context; 
    int layoutResourceId;    
    List<HighScore> data = null;
    
    public CustomAdapter(Context context, int layoutResourceId, List<HighScore> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        HighScoreHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new HighScoreHolder();
            holder.playerName = (TextView)row.findViewById(R.id.playerName);
            holder.highScore = (TextView)row.findViewById(R.id.highScore);
            
            row.setTag(holder);
        }
        else
        {
            holder = (HighScoreHolder)row.getTag();
        }
        
        HighScore score = data.get(position);
        holder.playerName.setText(score.getPlayerName());
        holder.highScore.setText(Integer.toString(score.getScore()));
        
        return row;
    }
    
    static class HighScoreHolder
    {
        TextView playerName;
        TextView highScore;
    }

}
