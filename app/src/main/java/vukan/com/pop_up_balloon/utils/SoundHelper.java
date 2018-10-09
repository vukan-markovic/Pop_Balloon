package vukan.com.pop_up_balloon.utils;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import vukan.com.pop_up_balloon.R;

/**
 * <h1>SoundHelper</h1>
 *
 * <p><b>SoundHelper</b> class is responsible for reproducing game sound and music.</p>
 */
public class SoundHelper {
    private MediaPlayer mMusicPlayer;
    private final SoundPool mSoundPool;
    private final int mSoundID;
    private final float mVolume;
    private boolean mLoaded;

    public SoundHelper(AppCompatActivity activity) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        mVolume = (float) (audioManager != null ? audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 0) / (float) (audioManager != null ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 0);
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setAudioAttributes(audioAttributes).setMaxStreams(6).build();
        } else {
            //noinspection deprecation
            mSoundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> mLoaded = true);
        mSoundID = mSoundPool.load(activity, R.raw.balloon_pop, 1);
    }

    /**
     * This method is responsible to play game sound.
     *
     * @see SoundPool#play(int, float, float, int, int, float)
     */
    public void playSound() {
        if (mLoaded) mSoundPool.play(mSoundID, mVolume, mVolume, 1, 0, 1f);
    }

    /**
     * This method is responsible for preparation of music player which is instance of MediaPlayer class.
     *
     * @param context Context of activity which call this method.
     * @see MediaPlayer
     */
    public void prepareMusicPlayer(@NonNull Context context) {
        mMusicPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.game_music);
        mMusicPlayer.setVolume(.5f, .5f);
        mMusicPlayer.setLooping(true);
    }

    /**
     * This method is responsible for start playing game music.
     *
     * @see MediaPlayer#start()
     */
    public void playMusic() {
        if (mMusicPlayer != null) mMusicPlayer.start();
    }

    /**
     * This method is responsible for pausing game music.
     *
     * @see MediaPlayer#isPlaying()
     * @see MediaPlayer#pause()
     */
    public void pauseMusic() {
        if (mMusicPlayer != null && mMusicPlayer.isPlaying()) mMusicPlayer.pause();
    }
}