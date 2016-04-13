package com.cromiumapps.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.cromiumapps.musicplayer.musicscheduler.MusicScheduler;

/**
 * Created by habeebahmed on 4/1/16.
 */
public class Preferences {

    private static final String MUSIC_PLAYER_PREFERENCES = "music_player_preferences";

    // keys
    private static final String PREF_NEXT_SONG_MODE = "next_song_mode";

    private SharedPreferences mSharedPrefs;

    public Preferences(Context context) {
        mSharedPrefs = context.getSharedPreferences(MUSIC_PLAYER_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void setNextSongMode(MusicScheduler.NextSongMode nextSongMode) {
        set(PREF_NEXT_SONG_MODE, nextSongMode.ordinal());
    }

    public MusicScheduler.NextSongMode getNextSongMode() {
        return MusicScheduler.NextSongMode.values()[get(PREF_NEXT_SONG_MODE, 0)];
    }

    private void set(String key, int value) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    private int get(String key, int defaultValue) {
        return mSharedPrefs.getInt(key, defaultValue);
    }
}
