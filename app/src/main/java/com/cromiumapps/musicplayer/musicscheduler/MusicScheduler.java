package com.cromiumapps.musicplayer.musicscheduler;

import android.content.Context;
import android.util.Log;

import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.Preferences;
import com.cromiumapps.musicplayer.model.Album;
import com.cromiumapps.musicplayer.model.Artist;
import com.cromiumapps.musicplayer.model.PlayList;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * decides what song should play next
 */
public class MusicScheduler {

    private static final String TAG = "MusicScheduler";

    public enum NextSongMode {SEQUENTIAL, SHUFFLE, REPEAT_CURRENT_SONG}

    private final static int NO_SONG_INDEX = -1;

    private ArrayList<Song> mPlayHistory = new ArrayList<Song>();
    private int mCurrentSongIndex = NO_SONG_INDEX;

    private SongList mSongList;
    private Preferences mPrefs;

    public MusicScheduler(Context context, SongList songList) {
        mSongList = songList;
        mPrefs = new Preferences(context);
    }

    public void addSongToPlayHistory(Song song){
        mPlayHistory.add(song);
    }

    public void clearPlayHistory(){
        mPlayHistory.clear();
    }

    private List<Song> getSearchList(Song currentSong, MusicService.PlayMode playMode, List<Song> songList){
        List <Song> searchList = songList;
        switch (playMode) {
            case ALBUM:
                for (Album album : mSongList.getAlbums()) {
                    if (album.contains(currentSong)) {
                        searchList = album.getSongs();
                    }
                }
                break;
            case ARTIST:
                for (Artist artist : mSongList.getArtistsList()) {
                    if (artist.contains(currentSong)) {
                        searchList = artist.getSongs();
                    }
                }
                break;
            case PLAYLIST:
                for (PlayList playList : mSongList.getPlaylists()) {
                    if (playList.contains(currentSong)) {
                        searchList = playList.getSongs();
                    }
                }
                break;
            default:
                break;
        }
        return searchList;
    }

    // TODO: finding the current song index everytime we play is expensive. Find a way to not have to do this
    public Song getNextSong(Song currentSong, boolean isUserRequest, MusicService.PlayMode playMode){
        List<Song> searchList = getSearchList(currentSong, playMode, mSongList.getAllSongs());
        mCurrentSongIndex = findSongIndex(searchList, currentSong);
        if (mCurrentSongIndex != NO_SONG_INDEX) {
            switch (mPrefs.getNextSongMode()) {
                case SEQUENTIAL:
                    mCurrentSongIndex = (mCurrentSongIndex + 1) % searchList.size();
                    break;
                case SHUFFLE:
                    mCurrentSongIndex = randInt(0, searchList.size() - 1);
                    break;
                case REPEAT_CURRENT_SONG:
                    // if the user has pressed the next button then we should proceed like we are in squential more
                    if (isUserRequest) {
                        mCurrentSongIndex = (mCurrentSongIndex + 1) % searchList.size();
                    }
                    break;
            }
        }

        return searchList.get(mCurrentSongIndex);
    }

    public Song getPreviousSong(Song currentSong, MusicService.PlayMode playMode){
        Log.d(TAG, "getPreviousSong. currentSongTitle = " + currentSong.title + " playmode = " + playMode);
        List<Song> searchList = getSearchList(currentSong, playMode, mSongList.getAllSongs());
        mCurrentSongIndex = findSongIndex(searchList, currentSong);
        Log.d(TAG, "current song index = " + mCurrentSongIndex);
        if (mCurrentSongIndex != NO_SONG_INDEX) {
            switch (mPrefs.getNextSongMode()) {
                case SHUFFLE:
                    Log.d(TAG, "playback mode is shuffle, fetching song from play history");
                    if (mPlayHistory.size() > 0) {
                        mCurrentSongIndex = findSongIndex(searchList, mPlayHistory.get(mPlayHistory.size() - 1));
                        mPlayHistory.remove(mPlayHistory.size() - 1);
                        Log.d(TAG, "there is a play history");
                        break;
                    }
                    Log.d(TAG, "there is no play history. using sequential ordering");
                case REPEAT_CURRENT_SONG:
                    // for repeat current song if the user presses back then we should honor the request and proceed as though we are in sequential mode
                case SEQUENTIAL:
                    mCurrentSongIndex -= 1;
                    if (mCurrentSongIndex < 0) mCurrentSongIndex = mSongList.size() - 1;
                    break;
            }
        }
        Log.d(TAG, "previous song = " + mSongList.getSong(mCurrentSongIndex).title);
        return mSongList.getSong(mCurrentSongIndex);
    }

    private int findSongIndex(List<Song> songList, Song song){
        for(int i = 0 ; i < songList.size() ; i++ ) {
            if(songList.get(i).id == song.id) {
                return i;
            }
        }
        return NO_SONG_INDEX;
    }

    private Random mRandom;
    public int randInt(int min, int max) {
        mRandom = new Random();
        return mRandom.nextInt((max - min) + 1) + min;
    }
}
