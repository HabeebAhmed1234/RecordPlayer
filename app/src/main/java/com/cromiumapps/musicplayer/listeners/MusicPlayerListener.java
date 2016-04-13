package com.cromiumapps.musicplayer.listeners;

import com.cromiumapps.musicplayer.model.Song;

/**
 * Created by Habeeb Ahmed on 9/3/2015.
 */
public interface MusicPlayerListener {
    void onSongPlay(Song song);
    void onSongPause(Song song);
    void onSongResume();
    void onSongEnd(Song song);
    void onSongProgress(int progress);
    void onMusicPlayerError(String what);
}
