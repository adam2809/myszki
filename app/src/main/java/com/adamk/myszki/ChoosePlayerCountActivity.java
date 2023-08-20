package com.adamk.myszki;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class ChoosePlayerCountActivity extends AppCompatActivity {

    int currPlayerCount=2;
    TextView playerCountDisplay;

    public static final String PLAYER_COUNT_EXTRA="PlayerCount";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_choose_player_count);

        RelativeLayout background=(RelativeLayout)findViewById(R.id.relativeLayout_choose_player_count);
        playerCountDisplay=(TextView)findViewById(R.id.playerCountDispaly);

        if (Build.VERSION.SDK_INT >= 21){
            background.setBackground( getResources().getDrawable(MenuActivity.currMap, getTheme()));
        }else{
            background.setBackground(getResources().getDrawable(MenuActivity.currMap));
        }
        ImageView playerCountMenu=(ImageView)findViewById(R.id.choose_player_count_menu);

        playerCountMenu.setOnTouchListener(new choosePlayerCountTouchListener());
        AssetManager assetManager=getAssets();
        Typeface pixelTypeface= Typeface.createFromAsset(assetManager,String.format(Locale.US, "fonts/%s", "Pixel.ttf"));
        playerCountDisplay.setTypeface(pixelTypeface);
    }
    private class choosePlayerCountTouchListener implements View.OnTouchListener{
        private int maxPlayerCount = 8;
        boolean doubleTouchIssueFixer=false;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.getId() != R.id.choose_player_count_menu) {
                return false;
            }
            if (doubleTouchIssueFixer){
                doubleTouchIssueFixer=false;
                return true;
            }else{
                doubleTouchIssueFixer=true;
            }
            Log.d("detectTouch", "Touch detected!");
            double oneFortiethOfMenuHeight = v.getHeight() / 32.0;
            double viewX = event.getX();
            double viewY = event.getY();

            if (viewY >= oneFortiethOfMenuHeight * 2 && viewY <= oneFortiethOfMenuHeight * 9) {
                startGame();
            } else if (viewY >= oneFortiethOfMenuHeight * 24 && viewY <= oneFortiethOfMenuHeight * 30) {
                if (viewX < v.getWidth() / 2) {
                    if (currPlayerCount == 2) {
                        return true;
                    }
                    currPlayerCount--;
                    updatePlayerCountDisplay();
                } else {
                    if (currPlayerCount == maxPlayerCount) {
                        return true;
                    }
                    currPlayerCount++;
                    updatePlayerCountDisplay();
                }
            }
            return true;
        }
    }


    public void startGame(){
        Intent intentStartGame=new Intent(this,BoardActivity.class);
        intentStartGame.putExtra(PLAYER_COUNT_EXTRA,currPlayerCount);
        startActivity(intentStartGame);
    }
    private void updatePlayerCountDisplay(){
        playerCountDisplay.setText(currPlayerCount+"");
    }
}
