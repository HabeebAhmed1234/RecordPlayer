package com.cromiumapps.musicplayer.model;

import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Habeeb Ahmed on 7/28/2015.
 */
public class Artist {
    public int id;
    public String name;
    public String thumbnailUri = null;
    public Palette mPalette = null;
    private ArrayList<Song> mSongList = new ArrayList<Song>();

    public Artist(int id, String name){
        this.name = name;
        this.id = id;
    }

    public void addSong(Song song){
        mSongList.add(song);

        Log.d("asdasd", "");
        if (thumbnailUri == null && !TextUtils.isEmpty(song.albumArtUri)) {
            thumbnailUri = song.albumArtUri;
        } else if (thumbnailUri == null) {
            thumbnailUri = song.albumArtUri;
        }

        if (name == null && song.artist != null && !song.artist.isEmpty()) {
            name = song.artist;
        }
    }

    public ArrayList<Song> getSongs(){
        return mSongList;
    }

    public Song getSong(int index){
        return mSongList.get(index);
    }

    public int getSongListSize(){
        return mSongList.size();
    }


    public boolean contains(Song song) {
        return mSongList.contains(song);
    }
}
