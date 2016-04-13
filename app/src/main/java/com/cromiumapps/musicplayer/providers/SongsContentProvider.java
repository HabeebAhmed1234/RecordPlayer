package com.cromiumapps.musicplayer.providers;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

//import android.util.Log;

public class SongsContentProvider extends ContentProvider {
    private static final String TAG = "SongsContentProvider";

    private MusicPlayerDatabase mDB;

    // Constants'
    public static final String AUTHORITY = "com.cromiumapps.musicplayer.SongsContentProvider";
    public static final String MUSIC_TABLE_PATH = "music";
    public static final Uri TABLE_MUSIC_CONTENT_URI =  Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + MUSIC_TABLE_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Declare URI match id here
    public static final int MUSIC_TABLE_CODE = 100;

    // Register URIs from modules
    static {
        SongsContentProvider.registerURIs(sURIMatcher);
    }

    // Declare URIs here
    public static void registerURIs(UriMatcher matcher) {
        matcher.addURI(AUTHORITY, MUSIC_TABLE_PATH, MUSIC_TABLE_CODE);
    }

    @Override
    public boolean onCreate() {
        mDB = new MusicPlayerDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        int uriCode = sURIMatcher.match(uri);
        if (uriCode == MUSIC_TABLE_CODE) {
            try {
                cursor = mDB.getReadableDatabase().query(MusicPlayerDatabase.TABLE_SONGS
                        , projection
                        , selection
                        , selectionArgs
                        , null, null
                        , sortOrder);
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return cursor;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;
        int uriCode = sURIMatcher.match(uri);
        if (uriCode == MUSIC_TABLE_CODE) {
            try {
                rowsDeleted = mDB.getReadableDatabase().delete(MusicPlayerDatabase.TABLE_SONGS
                        , selection
                        , selectionArgs);
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = 0;
        int uriCode = sURIMatcher.match(uri);
        if (uriCode == MUSIC_TABLE_CODE) {
            try {
                id = mDB.getReadableDatabase().insert(MusicPlayerDatabase.TABLE_SONGS
                        , null
                        , values);
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(MUSIC_TABLE_PATH + "/" + id);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;
        int uriCode = sURIMatcher.match(uri);
        if (uriCode == MUSIC_TABLE_CODE) {
            try {
                rowsUpdated = mDB.getWritableDatabase().update(MusicPlayerDatabase.TABLE_SONGS
                                                           , values
                                                           , selection
                                                           , selectionArgs);
            } catch (SQLiteException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
