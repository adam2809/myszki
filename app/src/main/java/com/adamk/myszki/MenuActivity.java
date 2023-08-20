package com.adamk.myszki;

import android.content.Intent;
import android.support.annotation.DrawableRes;
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

public class MenuActivity extends AppCompatActivity {

    RelativeLayout background;
    public static int currMapIndex = 1;
    public static
    @DrawableRes int currMap;
    private TextView mapIndexDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_menu);

        currMap = R.drawable.map1;
        background = (RelativeLayout) findViewById(R.id.relativeLayout_menu);

        ImageView menuImage=(ImageView)findViewById(R.id.menu);
        menuImage.setOnTouchListener(new menuTouchListener());
    }
    private class menuTouchListener implements View.OnTouchListener{
        boolean doubleTouchIssueFixer=false;
        private int mapsCount = 3;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!doubleTouchIssueFixer){
                doubleTouchIssueFixer=true;
            }else{
                doubleTouchIssueFixer=false;
                return true;
            }
            if (v.getId()!=R.id.menu){
                return false;
            }
            double oneFortiethOfMenuHeight=v.getHeight()/32.0;
            double viewX = event.getX();
            double viewY = event.getY();

            if (viewY>=oneFortiethOfMenuHeight*2&&viewY<=oneFortiethOfMenuHeight*9){
                startNewGame();
            }else if(viewY>=oneFortiethOfMenuHeight*24&&viewY<=oneFortiethOfMenuHeight*30){
                if (viewX<v.getWidth()/2){
                    if (currMapIndex == 1) {
                        return true;
                    }
                    currMapIndex--;
                }else{
                    if (currMapIndex == mapsCount) {
                        return true;
                    }
                    currMapIndex++;
                }
                if (currMapIndex == 1) {
                    currMap = R.drawable.map1;
                    updateMap();
                }
                if (currMapIndex == 2) {
                    currMap = R.drawable.map2;
                    updateMap();
                }
                if (currMapIndex==3){
                    currMap= R.drawable.map3;
                    updateMap();
                }
            }
            return true;
        }
    }

    public void startNewGame() {
        Intent intentChoosePlayers = new Intent(this, ChoosePlayerCountActivity.class);
        startActivity(intentChoosePlayers);
    }

    private void updateMap() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            background.setBackground(getResources().getDrawable(currMap, getTheme()));
        } else {
            background.setBackground(getResources().getDrawable(currMap));
        }
    }
}
