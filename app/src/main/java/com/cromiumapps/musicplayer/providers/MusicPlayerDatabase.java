package com.cromiumapps.musicplayer.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

public class MusicPlayerDatabase extends SQLiteOpenHelper {
    private static final String TAG = "MusicPlayerDatabase";

    private Context mContext;
    private static final int    DB_VERSION = 1;
    private static final String DB_NAME = "musicplayer_data.db";

    // Messages table, for storing all messages sent and received
    public static final String TABLE_SONGS                      = "songs";
    public static final String SONGS_SONG_ID                    = "_id";
    public static final String SONGS_COL_SONG_ID                = "song_id";
    public static final String SONGS_COL_SONG_TITLE             = "song_title";
    public static final String SONGS_COL_SONG_ARTIST            = "song_artist";
    public static final String SONGS_COL_SONG_PLAYLIST          = "song_playlist";
    public static final String SONGS_COL_SONG_DURATION          = "song_duration";
    public static final String SONGS_COL_SONG_PLAYABLE_URI      = "song_playable_uri";
    public static final String SONGS_COL_SONG_ALBUM_ID          = "sosng_album_id";
    public static final String SONGS_COL_SONG_ALBUM_NAME        = "song_album_anme";
    public static final String SONGS_COL_ALBUM_ART_URI          = "song_album_art_uri";

    private static final String CREATE_TABLE_MESSAGES =
            "create table " + TABLE_SONGS + " ("
                    + SONGS_SONG_ID                     + " integer primary key autoincrement, "
                    + SONGS_COL_SONG_ID                 + " integer unique, "
                    + SONGS_COL_SONG_TITLE              + " text not null, "
                    + SONGS_COL_SONG_ARTIST             + " text, "
                    + SONGS_COL_SONG_PLAYLIST           + " text, "
                    + SONGS_COL_SONG_DURATION           + " integer, "
                    + SONGS_COL_SONG_PLAYABLE_URI       + " text not null, "
                    + SONGS_COL_SONG_ALBUM_ID           + " integer, "
                    + SONGS_COL_SONG_ALBUM_NAME         + " text, "
                    + SONGS_COL_ALBUM_ART_URI           + " text "
                    + "); ";

    public MusicPlayerDatabase(Context context) {
        //super(context, name, factory, version);
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion){
            if (oldVersion == 1) {
                Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + " => add state column and modify conversation view");
                upgradeFrom1To2(db);
                onUpgrade(db, oldVersion + 1, newVersion);
            }
        }
    }

    private void upgradeFrom1To2(SQLiteDatabase db) {
    }

    public void wipeAndRecreate(SQLiteDatabase db) {
        // Clear user data, since it will be out of sync with the cleared database otherwise
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().clear().commit();

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        onCreate(db);
    }
}

