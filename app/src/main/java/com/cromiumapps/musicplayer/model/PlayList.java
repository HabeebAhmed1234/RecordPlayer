package com.cromiumapps.musicplayer.model;

import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by Habeeb Ahmed on 7/28/2015.
 */
public class PlayList {
    public int id;
    public String name;
    public String thumbnailUri;
    public Palette mPalette = null;
    private ArrayList<Song> mSongList = new ArrayList<Song>();

    public PlayList(int id, String name){
        this.name = name;
        this.id = id;
    }

    public void addSong(Song song){
        mSongList.add(song);

        if (thumbnailUri == null && !TextUtils.isEmpty(song.albumArtUri)) {
            thumbnailUri = song.albumArtUri;
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
