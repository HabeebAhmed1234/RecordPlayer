package com.cromiumapps.musicplayer.model;

import android.support.v7.graphics.Palette;

import java.util.ArrayList;

/**
 * Created by Habeeb Ahmed on 7/28/2015.
 */
public class Album {
    public int id;
    public String title;
    public String thumbnailUri;
    public Palette mPalette = null;
    private ArrayList<Song> mSongList = new ArrayList<Song>();

    public Album(int id){
        this.id = id;
    }

    public void addSong(Song song){
        mSongList.add(song);
        if (thumbnailUri == null && song.albumArtUri != null && !song.albumArtUri.isEmpty()) {
            thumbnailUri = song.albumArtUri;
        }

        if (title == null && song.albumName != null && !song.albumName.isEmpty()) {
            title = song.albumName;
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
