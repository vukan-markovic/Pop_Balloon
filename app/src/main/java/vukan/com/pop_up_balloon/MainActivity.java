package vukan.com.pop_up_balloon;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import vukan.com.pop_up_balloon.utils.HighScoreHelper;
import vukan.com.pop_up_balloon.utils.SimpleAlertDialog;
import vukan.com.pop_up_balloon.utils.SoundHelper;

public class MainActivity extends AppCompatActivity implements Balloon.BalloonListener {

    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 1000;
    private static final int MAX_ANIMATION_DURATION = 6000;
    private static final int NUMBER_OF_HEARTS = 5;
    private int balloonsPerLevel = 10;

    private ViewGroup mContentView;
    private final int[] mBalloonColors = {Color.YELLOW, Color.RED, Color.WHITE, Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE};
    private int mScreenWidth, mScreenHeight;
    private int mLevel, mScore, mHeartsUsed;
    private TextView mScoreDisplay;
    private TextView mLevelDisplay;
    private final List<ImageView> mHeartImages = new ArrayList<>();
    private final List<Balloon> mBalloons = new ArrayList<>();
    private Button mGoButton;
    private boolean mPlaying, mSound;
    private boolean mGameStopped = true;
    private int mBalloonsPopped;
    private SoundHelper mSoundHelper;
    private final Random random = new Random();

    @SuppressLint("FindViewByIdCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.drawable.modern_background);
        mContentView = findViewById(R.id.activity_main);
        setToFullScreen();
        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();

        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();
                }
            });
        }

        mContentView.setOnClickListener(view -> setToFullScreen());
        mScoreDisplay = findViewById(R.id.score_display);
        mLevelDisplay = findViewById(R.id.level_display);
        mHeartImages.add(findViewById(R.id.heart1));
        mHeartImages.add(findViewById(R.id.heart2));
        mHeartImages.add(findViewById(R.id.heart3));
        mHeartImages.add(findViewById(R.id.heart4));
        mHeartImages.add(findViewById(R.id.heart5));
        mGoButton = findViewById(R.id.go_button);
        updateDisplay();
        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);
        mSound = getIntent().getBooleanExtra(StartActivity.SOUND, true);
        findViewById(R.id.btn_exit).setOnClickListener(view -> finish());
    }

    private void setToFullScreen() {
        findViewById(R.id.activity_main).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
    }

    private void startGame() {
        setToFullScreen();
        mScore = 0;
        mLevel = 0;
        mHeartsUsed = 0;
        for (ImageView pin : mHeartImages) pin.setImageResource(R.drawable.heart);
        mGameStopped = false;
        startLevel();
    }

    private void startLevel() {
        mLevel++;
        updateDisplay();
        new BalloonLauncher().execute(mLevel);
        mPlaying = true;
        mBalloonsPopped = 0;
        mGoButton.setVisibility(View.INVISIBLE);
    }

    private void finishLevel() {
        Toast.makeText(this, getString(R.string.finish_level) + mLevel, Toast.LENGTH_SHORT).show();
        mPlaying = false;
        mGoButton.setText(MessageFormat.format("{0} {1}", getString(R.string.level_start), mLevel + 1));
        mGoButton.setVisibility(View.VISIBLE);
    }

    public void goButtonClickHandler(View view) {
        if (mGameStopped) startGame();
        else startLevel();
    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {
        mBalloonsPopped++;
        if (mSound) mSoundHelper.playSound();
        mContentView.removeView(balloon);
        mBalloons.remove(balloon);

        if (userTouch) mScore++;
        else {
            mHeartsUsed++;
            if (mHeartsUsed <= mHeartImages.size())
                mHeartImages.get(mHeartsUsed - 1).setImageResource(R.drawable.broken_heart);
            if (mHeartsUsed == NUMBER_OF_HEARTS) {
                gameOver();
                return;
            }
        }

        updateDisplay();

        if (mBalloonsPopped == balloonsPerLevel) {
            finishLevel();
            balloonsPerLevel += 10;
        }
    }

    private void gameOver() {
        Toast.makeText(this, R.string.game_over, Toast.LENGTH_SHORT).show();
        mSoundHelper.pauseMusic();

        for (Balloon balloon : mBalloons) {
            mContentView.removeView(balloon);
            balloon.setPopped(true);
        }

        mBalloons.clear();
        mPlaying = false;
        mGameStopped = true;
        mGoButton.setText(R.string.start_game);

        if (HighScoreHelper.isTopScore(this, mScore)) {
            HighScoreHelper.setTopScore(this, mScore);
            SimpleAlertDialog dialog = SimpleAlertDialog.newInstance(getString(R.string.new_high_score_title), getString(R.string.new_high_score_message) + mScore);
            dialog.show(getSupportFragmentManager(), null);
        }

        mGoButton.setVisibility(View.VISIBLE);
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
    }

    @SuppressLint("StaticFieldLeak")
    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {
        @Nullable
        @Override
        protected Void doInBackground(@NonNull Integer... params) {
            if (params.length != 1) throw new AssertionError("Expected 1 param for current level");
            int minDelay = Math.max(MIN_ANIMATION_DELAY, (MAX_ANIMATION_DELAY - ((params[0] - 1) * 500))) / 2;
            int balloonsLaunched = 0;

            while (mPlaying && balloonsLaunched < balloonsPerLevel) {
                // Get a random horizontal position for the next balloon
                Random random = new Random(new Date().getTime());
                publishProgress(random.nextInt(mScreenWidth - 200));
                balloonsLaunched++;

                try {
                    // Wait a random number of milliseconds before looping
                    Thread.sleep(random.nextInt(minDelay) + minDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            launchBalloon(values[0]);
        }
    }

    private void launchBalloon(int x) {
        Balloon balloon = new Balloon(this, mBalloonColors[random.nextInt(mBalloonColors.length)], 150);
        mBalloons.add(balloon);

        // Set balloon vertical position and dimensions, add to container
        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

        // Let balloons fly
        balloon.releaseBalloon(mScreenHeight, Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlaying) gameOver();
    }
}