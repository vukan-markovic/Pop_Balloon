package vukan.com.pop_up_balloon;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import vukan.com.pop_up_balloon.utils.HighScoreHelper;

public class MainActivity extends AppCompatActivity {
    public static final String SOUND = "SOUND", MUSIC = "MUSIC";
    private static final int REQUEST_INVITE = 1, RC_SIGN_IN = 2, RC_LEADERBOARD_UI = 9004, RC_ACHIEVEMENT_UI = 9003;
    private boolean denied, mMusic = true, mSound = true;
    private TextView highScore;
    private Animation animation;
    private ImageButton btnLeaderboard, btnAchievements;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.drawable.background);
        setToFullScreen();
        Button btnStart = findViewById(R.id.btn_start);
        Button btnInstructions = findViewById(R.id.btn_instructions);
        Button btnInviteFriends = findViewById(R.id.btn_invite_friends);
        ImageButton btnExit = findViewById(R.id.btn_exit);
        final ImageButton btnMusic = findViewById(R.id.btn_music);
        final ImageButton btnSound = findViewById(R.id.btn_sound);
        btnLeaderboard = findViewById(R.id.btn_leaderboard);
        btnAchievements = findViewById(R.id.btn_achievements);
        btnLeaderboard.setVisibility(View.GONE);
        btnAchievements.setVisibility(View.GONE);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade);
        animation.setDuration(100);
        highScore = findViewById(R.id.high_score);
        highScore.setText(String.valueOf(HighScoreHelper.getTopScore(this)));
        btnInstructions.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), InstructionsActivity.class)));
        findViewById(R.id.activity_start).setOnClickListener(view -> setToFullScreen());

        btnStart.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), GameplayActivity.class);
            intent.putExtra(SOUND, mSound);
            intent.putExtra(MUSIC, mMusic);
            startActivity(intent);
        });

        btnExit.setOnClickListener(view -> {
            view.startAnimation(animation);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        });

        btnMusic.setOnClickListener(view -> {
            if (mMusic) {
                mMusic = false;
                btnMusic.setBackgroundResource(R.drawable.music_note_off);
            } else {
                mMusic = true;
                btnMusic.setBackgroundResource(R.drawable.music_note);
            }
        });

        btnSound.setOnClickListener(view -> {
            if (mSound) {
                mSound = false;
                btnSound.setBackgroundResource(R.drawable.volume_off);
            } else {
                mSound = true;
                btnSound.setBackgroundResource(R.drawable.volume_up);
            }
        });

        btnAchievements.setOnClickListener(view -> {
            view.startAnimation(animation);
            if (GoogleSignIn.getLastSignedInAccount(this) != null) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .getAchievementsIntent()
                        .addOnSuccessListener(intent -> startActivityForResult(intent, RC_ACHIEVEMENT_UI));
            }
        });

        btnLeaderboard.setOnClickListener(view -> {
            view.startAnimation(animation);
            if (GoogleSignIn.getLastSignedInAccount(this) != null && isConnected()) {
                Games.getLeaderboardsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .getLeaderboardIntent(getString(R.string.leaderboard_high_scores))
                        .addOnSuccessListener(intent -> startActivityForResult(intent, RC_LEADERBOARD_UI));
            }
        });

        btnInviteFriends.setOnClickListener(view -> {
            Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invite_title))
                    .setMessage(getString(R.string.invite_message))
                    .setDeepLink(Uri.parse(getString(R.string.dynamic_link)))
                    .setCustomImage(Uri.parse(getString(R.string.email_cover)))
                    .setCallToActionText(getString(R.string.invite_action))
                    .build();
            startActivityForResult(intent, REQUEST_INVITE);
        });

        FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent()).addOnFailureListener(this, e -> Toast.makeText(this, R.string.dynamic_link_fail, Toast.LENGTH_SHORT).show());
    }

    private void setToFullScreen() {
        findViewById(R.id.activity_start).setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
        highScore.setText(String.valueOf(HighScoreHelper.getTopScore(this)));
        if (GoogleSignIn.getLastSignedInAccount(this) == null && isConnected() && !denied)
            signInSilently();
        else if (GoogleSignIn.getLastSignedInAccount(this) != null && isConnected() && btnLeaderboard.getVisibility() == View.GONE && btnAchievements.getVisibility() == View.GONE) {
            btnLeaderboard.setVisibility(View.VISIBLE);
            btnAchievements.setVisibility(View.VISIBLE);
        }
    }

    private void signInSilently() {
        GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).silentSignIn().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, getString(R.string.signed_in), Toast.LENGTH_SHORT).show();
                btnLeaderboard.setVisibility(View.VISIBLE);
                btnAchievements.setVisibility(View.VISIBLE);
                denied = false;
            } else startSignInIntent();
        });
    }

    private void startSignInIntent() {
        startActivityForResult(GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                Toast.makeText(this, getString(R.string.signed_in), Toast.LENGTH_SHORT).show();
                btnLeaderboard.setVisibility(View.VISIBLE);
                btnAchievements.setVisibility(View.VISIBLE);
                denied = false;
            } else {
                denied = true;
            }
        } else if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                Games.getAchievementsClient(this, Objects.requireNonNull(GoogleSignIn.getLastSignedInAccount(this)))
                        .unlock(getString(R.string.achievement_invite_friends));
            }
        }
    }

    private boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null)
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}