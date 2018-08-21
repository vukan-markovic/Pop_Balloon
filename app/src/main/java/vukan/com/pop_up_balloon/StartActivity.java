package vukan.com.pop_up_balloon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import vukan.com.pop_up_balloon.utils.HighScoreHelper;
import vukan.com.pop_up_balloon.utils.SoundHelper;

public class StartActivity extends AppCompatActivity {

    public static final String SOUND = "SOUND";
    private TextView highScore;
    private SoundHelper mSoundHelper;
    private boolean mMusic = true, mSound = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getWindow().setBackgroundDrawableResource(R.drawable.start_background);
        ViewGroup mContentView = findViewById(R.id.activity_start);
        setToFullScreen();

        mContentView.setOnClickListener(view -> setToFullScreen());

        Button btnStart = findViewById(R.id.btn_start);
        Button btnInstructions = findViewById(R.id.btn_instructions);
        Button btnExit = findViewById(R.id.btn_exit);
        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);
        final Button btnMusic = findViewById(R.id.btn_music);
        final Button btnSound = findViewById(R.id.btn_sound);

        btnStart.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra(SOUND, mSound);
            startActivity(intent);
        });

        btnInstructions.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), InstructionsActivity.class)));

        btnExit.setOnClickListener(view -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });

        highScore = findViewById(R.id.high_score);
        highScore.setText(String.valueOf(HighScoreHelper.getTopScore(this)));

        mSoundHelper.playMusic();

        btnMusic.setOnClickListener(view -> {
            if (mMusic) {
                mSoundHelper.pauseMusic();
                mMusic = false;
                btnMusic.setBackgroundResource(R.drawable.ic_music_note_black_off_24dp);
            } else {
                mSoundHelper.playMusic();
                mMusic = true;
                btnMusic.setBackgroundResource(R.drawable.ic_music_note_black_24dp);
            }
        });

        btnSound.setOnClickListener(view -> {
            if (mSound) {
                mSound = false;
                btnSound.setBackgroundResource(R.drawable.ic_volume_off_black_24dp);
            } else {
                mSound = true;
                btnSound.setBackgroundResource(R.drawable.ic_volume_up_black_24dp);
            }
        });
    }

    private void setToFullScreen() {
        findViewById(R.id.activity_start).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
        highScore.setText(String.valueOf(HighScoreHelper.getTopScore(this)));
    }
}