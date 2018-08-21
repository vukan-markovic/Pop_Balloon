package vukan.com.pop_up_balloon.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class HighScoreHelper {

    private static final String PREFS_GLOBAL = "prefs_global";
    private static final String PREF_TOP_SCORE = "pref_top_score";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_GLOBAL, Context.MODE_PRIVATE);
    }

    //  Setters and getters for global preferences
    public static boolean isTopScore(@NonNull Context context, int newScore) {
        int topScore = getPreferences(context).getInt(PREF_TOP_SCORE, 0);
        return newScore > topScore;
    }

    public static int getTopScore(@NonNull Context context) {
        return getPreferences(context).getInt(PREF_TOP_SCORE, 0);
    }

    public static void setTopScore(@NonNull Context context, int score) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(PREF_TOP_SCORE, score);
        editor.apply();
    }
}