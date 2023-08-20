package com.adamk.myszki;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.ColorRes;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class BoardActivity extends AppCompatActivity {

    private int[] playerPosition;
    private int playerCount;
    private ImageView[] playerViews;
    private RelativeLayout parentLayout;
    private double width;
    private double height;
    private double startPositionOnScreenX;
    private double startPositionOnScreenY;
    private double intervalToMovePawn;
    private int currPlayer = 0;
    public Drawable[] diceImages = new Drawable[6];
    public ImageView diceView;
    final Handler handler = new Handler();
    private static final String PLAYER_POSITION_TAG = "playerPositionSaved";
    private static final String CURR_PLAYER_TAG = "currPlayerSaved";
    private static final String PLAYER_COUNT_TAG = "playerCountSaved";
    private static boolean playerMoving=false;

    private int[] powerups = new int[100];

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putIntArray(PLAYER_POSITION_TAG, playerPosition);
        savedInstanceState.putInt(CURR_PLAYER_TAG, currPlayer);
        savedInstanceState.putInt(PLAYER_COUNT_TAG, playerCount);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        Intent playerCountIntent = getIntent();
        playerCount = playerCountIntent.getIntExtra(ChoosePlayerCountActivity.PLAYER_COUNT_EXTRA, -1);

        playerPosition = new int[playerCount];
        playerViews = new ImageView[playerCount];


        setContentView(R.layout.activity_board);

        parentLayout = (RelativeLayout) findViewById(R.id.relativeLayout_board);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            parentLayout.setBackground(getResources().getDrawable(MenuActivity.currMap, getTheme()));
        } else {
            parentLayout.setBackground(getResources().getDrawable(MenuActivity.currMap));
        }

        if (savedInstanceState != null) {
            playerPosition = savedInstanceState.getIntArray(PLAYER_POSITION_TAG);
            currPlayer = savedInstanceState.getInt(CURR_PLAYER_TAG);
            playerCount = savedInstanceState.getInt(PLAYER_COUNT_TAG);
        }

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        height = size.y;
        width = 16.0 / 9.0 * height;

        diceView = (ImageView) findViewById(R.id.diceView);

        Drawable[] playerImages = new Drawable[8];
        Drawable templatePlayerImage;
        Drawable test = null;
        Drawable map = null;

        templatePlayerImage=getDrawableResource(R.drawable.templateplayer);

        int[] diceImagesIds={R.drawable.dice1,R.drawable.dice2,R.drawable.dice3,
                R.drawable.dice4,R.drawable.dice5,R.drawable.dice6,};
        for (int i=0;i<6;i++){
            diceImages[i]=getDrawableResource(diceImagesIds[i]);
        }


        templatePlayerImage = scaleDrawable(templatePlayerImage, height / (1080));

        int[] playerColorIds = {R.color.colorGreenPlayer, R.color.colorPurplePlayer,
                R.color.colorYellowPlayer, R.color.colorPinkPlayer, R.color.colorBrownPlayer,
                R.color.colorBluePlayer, R.color.colorPlayerGrey, R.color.colorPlayerWhite};

        for (int i = 0; i < playerCount; i++) {
            playerImages[i] = setPlayerImage(templatePlayerImage, playerColorIds[i]);
        }

        String mapName = "map" + MenuActivity.currMapIndex + "Info.txt";
        powerups = getMapInfoFromAssets(this, mapName);

        for (int i = 0; i < playerCount; i++) {
            ImageView currPlayerView = new ImageView(getApplicationContext());
            currPlayerView.setImageDrawable(playerImages[i]);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            currPlayerView.setLayoutParams(params);

            parentLayout.addView(currPlayerView);
            playerViews[i] = currPlayerView;
        }


        float playerViewHeight = playerViews[0].getDrawable().getIntrinsicHeight();
        float playerViewWidth = playerViews[0].getDrawable().getIntrinsicWidth();

        intervalToMovePawn = height / (float) 10.0;

        startPositionOnScreenX=(float)(width)*7f/32f-playerViewWidth;
        startPositionOnScreenY=(float)height-playerViewHeight;

        for (int i = 0; i < playerCount; i++) {
            if (savedInstanceState==null){
                playerViews[i].setX((float)startPositionOnScreenX);
                playerViews[i].setY((float)startPositionOnScreenY);
                continue;
            }
            double[] playerPositionOnStart = convertPositionToPointOnScreen(playerPosition[i]);
            playerViews[i].setX((float) playerPositionOnStart[0]);
            playerViews[i].setY((float) playerPositionOnStart[1]);
        }
    }

    public void rollDice(View view) {
        //Roll dice, the run() method in RollDice calls movePlayer()
        if (playerMoving){
            return;
        }else{
            playerMoving=true;
        }
        final Handler handler = new Handler();
        final RollDice rollDice = new RollDice();
        handler.post(rollDice);
    }

    public void goHome(View view) {
        Intent goHomeIntent=new Intent(this,MenuActivity.class);
        startActivity(goHomeIntent);
    }

    public void restartGame(View view) {
        Intent intent=getIntent();
        finish();
        startActivity(intent);
    }

    private class RollDice implements Runnable {
        private int currDiceTurn = 0;
        private int currDiceImage;
        private int prevFace = 0;

        int numberOfDiceTurns = 0;
        Random random = new Random();

        @Override
        public void run() {
            if (numberOfDiceTurns == 0) {
                numberOfDiceTurns = random.nextInt(10) + 5;
            }
            while (prevFace == currDiceImage) {
                currDiceImage = random.nextInt(6);
            }

            prevFace = currDiceImage;
            diceView.setImageDrawable(diceImages[currDiceImage]);


            if (currDiceTurn < numberOfDiceTurns) {
                currDiceTurn++;
                handler.postDelayed(this, 100);
            } else {
                movePlayer(currDiceImage + 1);
            }
        }
    }

    private class MovePlayer implements Runnable {
        private int positionsToMove;
        private boolean isLastStep=false;

        public MovePlayer(int positionsToMove) {
            this.positionsToMove = positionsToMove;
        }

        public void run() {
            playerPosition[currPlayer]++;

            if (playerPosition[currPlayer] == 100) {
                Toast.makeText(new BoardActivity(),"Player",Toast.LENGTH_LONG);
            }

            double[] nextCoordinates = convertPositionToPointOnScreen(playerPosition[currPlayer]);
            playerViews[currPlayer].setX((float) nextCoordinates[0]);
            playerViews[currPlayer].setY((float) nextCoordinates[1]);


            if (positionsToMove > 1) {
                positionsToMove--;
                handler.postDelayed(this, 500);
            } else {
                playerMoving=false;
                int currPowerup = powerups[playerPosition[currPlayer]];
                if (currPowerup != 0) {
                    double[] positionToMove = convertPositionToPointOnScreen(playerPosition[currPlayer] + 1);
                    playerViews[currPlayer].setX((float) positionToMove[0]);
                    playerViews[currPlayer].setY((float) positionToMove[1]);

                    positionToMove = convertPositionToPointOnScreen(currPowerup);
                    playerViews[currPlayer].setX((float) positionToMove[0]);
                    playerViews[currPlayer].setY((float) positionToMove[1]);

                    playerPosition[currPlayer] = currPowerup;
                }
            }
        }
    }

    private void movePlayer(int positionsToMove) {
        MovePlayer movePlayer = new MovePlayer(positionsToMove);
        final Handler handler = new Handler();
        handler.post(movePlayer);
        currPlayer = (currPlayer + 1) % playerCount;
    }
    private double[] convertPositionToPointOnScreen(int position) {
        if (position==0){
            return new double[]{startPositionOnScreenX,startPositionOnScreenY};
        }
        int currRow = (position-1)/ 10;
        int currColumn;
        if (currRow % 2 == 0) {
            currColumn = (position-1)% 10;
        } else {
            currColumn = 9 - ((position-1) % 10);
        }

        double positionOfFirstTileX=startPositionOnScreenX+intervalToMovePawn;
        double positionOfFirstTileY=startPositionOnScreenY;

        double positionOnScreenX = currColumn * intervalToMovePawn + positionOfFirstTileX;
        double positionOnScreenY = -currRow * intervalToMovePawn + positionOfFirstTileY;
        double[] positionOnScreen = {positionOnScreenX, positionOnScreenY};

        return positionOnScreen;
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public static int[] getMapInfoFromAssets(Context context, String textFileName) {
        int[] mapInfo = new int[100];
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(textFileName);
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            for (int i = 0; i < 100; i++) {
                mapInfo[i] = Integer.parseInt(bufferedReader.readLine());
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mapInfo;
    }

    private static Drawable scaleDrawable(Drawable drawable, double scalar) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap scaledBitmap = bitmap.createScaledBitmap(bitmap, (int) (drawable.getIntrinsicWidth() * scalar), (int) (drawable.getIntrinsicHeight() * scalar), false);
        return new BitmapDrawable(scaledBitmap);
    }

    private Drawable setPlayerImage(Drawable playerTemplate, int colorId) {
        playerTemplate.setColorFilter(getResources().getColor(colorId), PorterDuff.Mode.MULTIPLY);
        return playerTemplate.getConstantState().newDrawable().mutate();
    }

    private Drawable getDrawableResource(int resourceId) {
        Drawable drawable = null;
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            drawable = getResources().getDrawable(resourceId, getTheme());
        } else {
            drawable = getResources().getDrawable(resourceId);
        }
        return drawable;
    }
}