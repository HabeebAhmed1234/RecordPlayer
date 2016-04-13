package com.cromiumapps.musicplayer.musicfetcher;

import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.preferences.UserInfo;
import com.cromiumapps.musicplayer.providers.MusicPlayerDatabase;
import com.cromiumapps.musicplayer.providers.SongsContentProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Habeeb Ahmed on 7/28/2015.
 * the music fetcher process is as follows. when fetchMusic is called we first check our local DB.
 * if the local DB is empty or expired we query the android music db.
 * if the android db is empty then we throw an error "no music on device"
 * if the android db is not empty we synchronize our localdb with the android db and re-query the local db.
 * once we've read the music list from the local db we parse it into the mSongList variable and notify all
 * listeners that the music list is available
 */
public class MusicFetcher implements LoaderManager.LoaderCallbacks<Cursor>{
    public interface MusicFetchedListener {
        void onMusicFetchStart();
        void onMusicFetched(SongList songList);
        void onNoMusicOnDevice();
        void onMusicFetchError(String errorMessage);
    }

    private static final String TAG = "MusicFetcher";
    private static final Uri ANDROID_MUSIC_PROVIDER_URI = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private static final int URL_LOADER = 0;
    private static final String[] sLocalDBProjection =
            {
                    MusicPlayerDatabase.SONGS_COL_SONG_ID,
                    MusicPlayerDatabase.SONGS_COL_SONG_TITLE,
                    MusicPlayerDatabase.SONGS_COL_SONG_ARTIST,
                    MusicPlayerDatabase.SONGS_COL_SONG_PLAYLIST,
                    MusicPlayerDatabase.SONGS_COL_SONG_DURATION ,
                    MusicPlayerDatabase.SONGS_COL_SONG_PLAYABLE_URI,
                    MusicPlayerDatabase.SONGS_COL_SONG_ALBUM_ID,
                    MusicPlayerDatabase.SONGS_COL_SONG_ALBUM_NAME,
                    MusicPlayerDatabase.SONGS_COL_ALBUM_ART_URI
            };

    private Context mContext;
    private LoaderManager mLoaderManager;
    private SongList mSongList = null;
    private Set<MusicFetchedListener> mListeners = new HashSet<MusicFetchedListener>();
    private UserInfo mUserInfo;
    private Handler mMainThreadHandler;

    public MusicFetcher(Context context, LoaderManager loaderManager){
        mContext = context;
        mMainThreadHandler = new Handler(Looper.getMainLooper());
        mUserInfo = new UserInfo(mContext);
        mLoaderManager = loaderManager;
        mSongList = new SongList();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                return new CursorLoader(
                        mContext,
                        SongsContentProvider.TABLE_MUSIC_CONTENT_URI,
                        sLocalDBProjection,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() == 0 || mUserInfo.isLocalDBExpired()) {
            // localdb is empty load from android db
            synchronizeLocalDBWithAndroidDB();
        } else {
            parseCursorIntoSongList(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void addMusicFetchedListener(MusicFetchedListener listener){
        if(mListeners.contains(listener)) mListeners.remove(listener);
        mListeners.add(listener);
        if (mSongList != null) {
            listener.onMusicFetched(mSongList);
        }
    }

    public void removeOnMusicFetchedListener(MusicFetchedListener listener){
        mListeners.remove(listener);
    }

    public SongList getAllSongsList(){
        return mSongList;
    }

    public void fetchMusic(){
        notifyFetchStart();
        loadFromDB();
    }

    public void loadFromDB(){
        mLoaderManager.initLoader(URL_LOADER, null, this);
    }

    private void synchronizeLocalDBWithAndroidDB(){
        new AsyncTask<Void, Void, Void> () {
            @Override
            protected Void doInBackground(Void... params) {

                ArrayList<Song> androidDBSongs = getSongs();
                if (androidDBSongs.size() > 0) {
                    ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
                    ContentProviderOperation.Builder insertBuilder;
                    // first delete all existing entries
                    ContentProviderOperation.Builder deleteOperation = ContentProviderOperation.newDelete(SongsContentProvider.TABLE_MUSIC_CONTENT_URI);
                    operations.add(deleteOperation.build());
                    // add in the new songs
                    for (Song song : androidDBSongs) {
                        Log.d(TAG, "+ " + " id = "+song.id + "  title = " + song.title + "  artist = "+song.artist);
                        insertBuilder = ContentProviderOperation.newInsert(SongsContentProvider.TABLE_MUSIC_CONTENT_URI);
                        ContentValues mNewValues = new ContentValues();
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_ID, song.id);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_TITLE, song.title);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_ARTIST, song.artist);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_DURATION, song.durationSeconds);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_PLAYABLE_URI, song.playableUri);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_ALBUM_ID, song.albumId);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_SONG_ALBUM_NAME, song.albumName);
                        mNewValues.put(MusicPlayerDatabase.SONGS_COL_ALBUM_ART_URI, song.albumArtUri);
                        insertBuilder.withValues(mNewValues);
                        operations.add(insertBuilder.build());
                    }
                    try {
                        mContext.getContentResolver().applyBatch(SongsContentProvider.AUTHORITY, operations);
                        purgeStaleEntries();
                        mUserInfo.setIsLocalDBExpired(false);
                        mUserInfo.commitChanges();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        mSongList = new SongList();
                        notifyError(e.getMessage());
                    } catch (OperationApplicationException e) {
                        mSongList = new SongList();
                        notifyError(e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    mSongList = new SongList();
                    notifyNoMusic();
                }

                return null;
            }
        }.execute();
    }

    //TODO: implement a function to remove entries in the local db that are no longer valid
    private void purgeStaleEntries(){

    }

    private void parseCursorIntoSongList(final Cursor data){
        new AsyncTask<Void, Void, SongList> () {

            @Override
            protected SongList doInBackground(Void... params) {
                SongList songList = new SongList();

                if(data != null && data.moveToFirst()){
                    //get columns
                    int idColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_ID);
                    int titleColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_TITLE);
                    int artistColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_ARTIST);
                    int playlistColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_PLAYLIST);
                    int durationColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_DURATION);
                    int playableUriColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_PLAYABLE_URI);
                    int albumIdColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_ALBUM_ID);
                    int albumNameColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_SONG_ALBUM_NAME);
                    int albumArtUriColumn = data.getColumnIndex(MusicPlayerDatabase.SONGS_COL_ALBUM_ART_URI);

                    //add songs to list
                    do {
                        int id = data.getInt(idColumn);
                        String title = data.getString(titleColumn);
                        String artist = data.getString(artistColumn);
                        String playList = data.getString(playlistColumn);
                        int duration = data.getInt(durationColumn);
                        Uri playableUri = Uri.parse(data.getString(playableUriColumn));
                        int albumId = data.getInt(albumIdColumn);
                        String albumName = data.getString(albumNameColumn);
                        String albumArtUri = data.getString(albumArtUriColumn);
                        songList.addSong(new Song(id, title, artist, playList, albumId, albumName, duration, albumArtUri, playableUri.toString()));
                    }
                    while (data.moveToNext());
                }
                return songList;
            }

            @Override
            protected void onPostExecute(SongList songList) {
                super.onPostExecute(songList);
                mSongList = songList;
                notifyFetched();
            }
        }.execute();
    }

    private ArrayList<Song> getSongs() {
        ArrayList<Song> songList = new ArrayList<Song>();
        ContentResolver musicResolver = mContext.getContentResolver();
        String  selection = MediaStore.Audio.Media.IS_MUSIC;
        Cursor musicCursor = musicResolver.query(ANDROID_MUSIC_PROVIDER_URI, null, selection, null, MediaStore.Audio.Media._ID);

        if(musicCursor!=null && musicCursor.moveToFirst()){
            //get columns
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int durationColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int albumIdColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int albumNameColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            //add songs to list
            do {
                int id = musicCursor.getInt(idColumn);
                String title = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                int durationInSeconds = musicCursor.getInt(durationColumn) / 1000;
                int albumId = musicCursor.getInt(albumIdColumn);
                String albumName = musicCursor.getString(albumNameColumn);
                String albumArtUri = getCoverArtUri(Integer.toString(albumId));
                Uri playableUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Integer.toString(id));
                songList.add(new Song(id, title, artist, null, albumId, albumName, durationInSeconds, albumArtUri, playableUri.toString()));
            }
            while (musicCursor.moveToNext());
        }
        return songList;
    }

    private String getCoverArtUri(String albumId) {
        String path = null;
        Cursor c = mContext.getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{albumId},
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                path = c.getString(0);
            }
            c.close();
        }
        return path;
    }

    private void notifyFetchStart(){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (MusicFetchedListener listener : mListeners) {
                    listener.onMusicFetchStart();
                }
            }
        });
    }

    private void notifyFetched(){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (MusicFetchedListener listener : mListeners) {
                    listener.onMusicFetched(mSongList);
                }
            }
        });
    }

    private void notifyNoMusic(){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (MusicFetchedListener listener : mListeners) {
                    listener.onNoMusicOnDevice();
                }
            }
        });
    }

    private void notifyError(final String errorMessage){
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                for (MusicFetchedListener listener : mListeners) {
                    listener.onMusicFetchError(errorMessage);
                }
            }
        });
    }
}
