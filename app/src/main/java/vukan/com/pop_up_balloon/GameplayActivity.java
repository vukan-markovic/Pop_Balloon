package vukan.com.pop_up_balloon;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.games.Games;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import vukan.com.pop_up_balloon.utils.HighScoreHelper;
import vukan.com.pop_up_balloon.utils.SimpleAlertDialog;
import vukan.com.pop_up_balloon.utils.SoundHelper;

public class GameplayActivity extends AppCompatActivity implements Balloon.BalloonListener {
    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 1000;
    private static final int MAX_ANIMATION_DURATION = 6000;
    private static final int NUMBER_OF_HEARTS = 5;
    private final int[] mBalloonColors = {Color.YELLOW, Color.RED, Color.WHITE, Color.MAGENTA, Color.GREEN, Color.CYAN, Color.BLUE};
    private final List<ImageView> mHeartImages = new ArrayList<>();
    private final List<Balloon> mBalloons = new ArrayList<>();
    private final Random random = new Random();
    private int balloonsPerLevel = 10;
    private ViewGroup mContentView;
    private int mScreenWidth, mScreenHeight;
    private int mLevel, mScore, mHeartsUsed;
    private TextView mScoreDisplay;
    private TextView mLevelDisplay;
    private Button mGoButton;
    private boolean mPlaying, mSound;
    private boolean mGameStopped = true;
    private int mBalloonsPopped;
    private SoundHelper mSoundHelper;
    private Animation animation;

    @SuppressLint("FindViewByIdCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameplay);
        getWindow().setBackgroundDrawableResource(R.drawable.background_2);
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
        mSound = getIntent().getBooleanExtra(MainActivity.SOUND, true);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        animation.setDuration(100);
        findViewById(R.id.btn_back_gameplay).setOnClickListener(view -> {
            view.startAnimation(animation);
            finish();
        });
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
        if (mLevel == 3) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_reach_level_3));
                if (mHeartsUsed == 0) {
                    Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                            .unlock(getString(R.string.achievement_reach_level_3_without_losing_life));
                }
            }
        }
        if (mLevel == 5) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_reach_level_5));
                if (mHeartsUsed == 0) {
                    Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                            .unlock(getString(R.string.achievement_reach_level_5_without_losing_life));
                }
            }
        }
        if (mLevel == 10) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_reach_level_10));
                if (mHeartsUsed == 0) {
                    Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                            .unlock(getString(R.string.achievement_reach_level_10_without_losing_life));
                }
            }
        }
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

        if (mBalloonsPopped == 10) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_pop_10_balloons));
            }
        }

        if (mBalloonsPopped == 50) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_pop_50_balloons));
            }
        }

        if (mBalloonsPopped == 500) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_pop_500_balloons));
            }
        }

        if (mBalloonsPopped == 5000) {
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_pop_5000_balloons));
            }
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
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getLeaderboardsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .submitScore(getString(R.string.leaderboard_high_scores), mScore);
            }
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

    private void launchBalloon(int x) {
        Balloon balloon = new Balloon(this, mBalloonColors[random.nextInt(mBalloonColors.length)], 150);
        mBalloons.add(balloon);
        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);
        balloon.releaseBalloon(mScreenHeight, Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlaying) gameOver();
    }

    @SuppressLint("StaticFieldLeak")
    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {
        @Nullable
        @Override
        protected Void doInBackground(@NonNull Integer... params) {
            if (params.length != 1) throw new AssertionError(getString(R.string.assertion_message));
            int minDelay = Math.max(MIN_ANIMATION_DELAY, (MAX_ANIMATION_DELAY - ((params[0] - 1) * 500))) / 2;
            int balloonsLaunched = 0;

            while (mPlaying && balloonsLaunched < balloonsPerLevel) {
                Random random = new Random(new Date().getTime());
                publishProgress(random.nextInt(mScreenWidth - 200));
                balloonsLaunched++;

                try {
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
}