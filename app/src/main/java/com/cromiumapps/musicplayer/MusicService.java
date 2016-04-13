package com.cromiumapps.musicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.musicscheduler.MusicScheduler;
import com.cromiumapps.musicplayer.notification.MPNotificationManager;
import com.cromiumapps.musicplayer.palette.PaletteColorManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Habeeb Ahmed on 7/30/2015.
 */
public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, MusicFetcher.MusicFetchedListener {

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public interface MusicPlayerListener {
        void onSongPlay(Song song);

        void onSongPause();

        void onSongResume();

        void onSongEnd();

        void onSongProgress(int progress);

        void onMusicPlayerError(String what);
    }

    public enum PlayMode {ALL_MUSIC, ALBUM, ARTIST, PLAYLIST}

    private static final String TAG = "MusicService";
    private static final int PROGRESS_POLL_INTERVAL_MS = 1000;
    private final IBinder mMusicServiceBind = new MusicBinder();
    private MediaPlayer mMediaPlayer = null;
    private MPNotificationManager mMPNotificationManager = null;
    private Set<MusicPlayerListener> mListeners = new HashSet<MusicPlayerListener>();
    private Song mCurrentSong;
    private PlayMode mCurrentPlayMode;
    private MusicScheduler mMusicScheduler;
    private Preferences mPrefs;
    private Handler mMainThreadHandler;
    private Runnable mProgressPollRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.getDuration() > 0) {
                    notifyProgress(mMediaPlayer.getCurrentPosition() / 1000);
                    mMainThreadHandler.postDelayed(this, PROGRESS_POLL_INTERVAL_MS);
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMusicPlayer();
        mMPNotificationManager = new MPNotificationManager(this);
        mPrefs = new Preferences(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    private void initMusicPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void addMusicPlayerListener(MusicPlayerListener listener) {
        if(mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
        mListeners.add(listener);
    }

    public void removeMusicPlayerListener(MusicPlayerListener listener) {
        mListeners.remove(listener);
    }

    public boolean isPlaying() {
        return mMediaPlayer == null ? false : mMediaPlayer.isPlaying();
    }

    public void seekTo(int progress) {
        if (mMediaPlayer != null && mMediaPlayer.getDuration() > 0) {
            int seekToInMilliseconds = progress * 1000;
            mMediaPlayer.seekTo(seekToInMilliseconds);
        }
    }

    public Song getCurrentSong() {
        return mCurrentSong;
    }

    public void setCurrentPlayMode(PlayMode mode) {
        if (mCurrentPlayMode != mode) {
            mMusicScheduler.clearPlayHistory();
            mCurrentPlayMode = mode;
        }
    }

    public void setNextSongMode(MusicScheduler.NextSongMode mode) {
        if (mode == MusicScheduler.NextSongMode.REPEAT_CURRENT_SONG && mMediaPlayer != null) {
            mMediaPlayer.setLooping(true);
        } else {
            mMediaPlayer.setLooping(false);
        }
        mPrefs.setNextSongMode(mode);
    }

    public void playSong(Song song) {
        if (mMediaPlayer == null) {
            Log.e(TAG, "invalid state mMediaPlayer == null");
        } else {
            Log.d(TAG, "play song = " + song);
            mCurrentSong = song;
            mMusicScheduler.addSongToPlayHistory(mCurrentSong);
            mMediaPlayer.reset();
            try {
                Log.d(TAG, "set data source = " + mCurrentSong.playableUri);
                mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(mCurrentSong.playableUri));
                Log.d(TAG, "prepare mediaplayer async");
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                Log.e(TAG, "Error setting data source", e);
                notifyOnError(e.getMessage());
            }
            MusicPlayerApplication.getPaletteColorManager().setPaletteColor(Uri.parse("file://" + mCurrentSong.albumArtUri));
        }
    }

    public void playNextSong(boolean isUserRequest) {
        Log.d(TAG, "play next song. isUserRequest = " + isUserRequest);
        playSong(mMusicScheduler.getNextSong(mCurrentSong, isUserRequest, mCurrentPlayMode));
    }

    public void playPreviousSong() {
        Log.d(TAG, "play previous song");
        playSong(mMusicScheduler.getPreviousSong(mCurrentSong, mCurrentPlayMode));
    }

    public void pauseSong() {
        Log.d(TAG, "pause song");
        mMediaPlayer.pause();
        notifyPause();
    }

    public void resumeSong() {
        if (mCurrentSong != null && !mMediaPlayer.isPlaying()) {
            Log.d(TAG, "resume song");
            mMediaPlayer.start();
            notifyResume();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMusicServiceBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }


    public void onPrepared(MediaPlayer player) {
        Log.d(TAG, "media player prepared. start playing");
        player.start();
        notifyPlayStart(mCurrentSong);
        notifyProgress(0);
        mMainThreadHandler.postDelayed(mProgressPollRunnable, PROGRESS_POLL_INTERVAL_MS);
    }

    public static class NotificationButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "test");
        }

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.d(TAG, "song ended. play next song");
        notifyEnd();
        playNextSong(false);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //check if its a serious error
        String errorString = isMajorError(what);
        if (!TextUtils.isEmpty(errorString)) {
            mMediaPlayer.reset();
            notifyOnError("MusicPlayer error = " + errorString);
        } else {
            Log.e(TAG, "MediaPlayer minor error. what = " + what);
        }
        return false;
    }

    /**
     * checks if it is a major error and returns a readable error string
     *
     * @param what
     * @return
     */
    private String isMajorError(int what) {
        String errorString = null;
        Resources resources = getResources();
        switch (what) {
            case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                errorString = resources.getString(R.string.error_corrupted_data);
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                errorString = resources.getString(R.string.error_unsupported_file_format);
                break;
        }
        return errorString;
    }

    // music fetcher callbacks
    @Override
    public void onMusicFetchStart() {

    }

    @Override
    public void onMusicFetched(SongList songList) {
        mMusicScheduler = new MusicScheduler(this, songList);
    }

    @Override
    public void onNoMusicOnDevice() {

    }

    @Override
    public void onMusicFetchError(String errorMessage) {

    }

    // notifications
    private void notifyPlayStart(Song song) {
        for (MusicPlayerListener listener : mListeners) {
            listener.onSongPlay(song);
        }
    }

    private void notifyPause() {
        for (MusicPlayerListener listener : mListeners) {
            listener.onSongPause();
        }
    }

    private void notifyResume() {
        for (MusicPlayerListener listener : mListeners) {
            listener.onSongResume();
        }
    }

    private void notifyEnd() {
        for (MusicPlayerListener listener : mListeners) {
            listener.onSongEnd();
        }
    }

    private void notifyProgress(int progress) {
        for (MusicPlayerListener listener : mListeners) {
            listener.onSongProgress(progress);
        }
    }

    private void notifyOnError(String what) {
        for (MusicPlayerListener listener : mListeners) {
            listener.onMusicPlayerError(what);
        }
    }
}