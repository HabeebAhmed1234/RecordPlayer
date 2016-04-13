package com.cromiumapps.musicplayer.model;

import android.text.TextUtils;
import android.util.Log;

import com.cromiumapps.musicplayer.MusicService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Habeeb Ahmed on 7/28/2015.
 */
public class SongList {
    public static final String TAG = "SongList";

    private List<Song> mSongList = new ArrayList<Song>();
    private List<Album> mAlbumsList = new ArrayList<Album>();
    private List<Artist> mArtistsList = new ArrayList<Artist>();
    private List<PlayList> mPlayLists = new ArrayList<PlayList>();

    public SongList(){}

    public void addSong(Song song){
        // add song to song list
        mSongList.add(song);
        // add song to album
        addSongToAlbum(song);
        // add song to artist
        addSongToArtist(song);
        // add song to playlist
        addSongToPlayList(song);
    }

    private void addSongToAlbum(Song song){
        boolean added = false;
        for (Album album : mAlbumsList) {
            if (album.id == song.albumId) {
                album.addSong(song);
                added = true;
            }
        }
        if (!added) {
            Album newAlbum = new Album(song.albumId);
            newAlbum.addSong(song);
            mAlbumsList.add(newAlbum);
        }
    }

    private int mUniqueArtistID = 0;

    private void addSongToArtist(Song song){
        boolean added = false;
        for (Artist artist : mArtistsList) {
            if (artist.name.equals(song.artist)) {
                artist.addSong(song);
                added = true;
            }
        }
        if (!added && !TextUtils.isEmpty(song.artist)) {
            Artist newArtist = new Artist(mUniqueArtistID, song.artist);
            mUniqueArtistID ++;
            newArtist.addSong(song);
            mArtistsList.add(newArtist);
        }
    }

    private int mUniquePlayListId = 0;

    private void addSongToPlayList(Song song) {
        boolean added = false;
        for (PlayList playList : mPlayLists) {
            if (playList.name.equals(song.playList)) {
                playList.addSong(song);
                added = true;
            }
        }
        if (!added && !TextUtils.isEmpty(song.playList)) {
            PlayList newPlayList = new PlayList(mUniquePlayListId, song.playList);
            mUniquePlayListId ++;
            newPlayList.addSong(song);
            mPlayLists.add(newPlayList);
        }

    }

    public int getAlbumsCount(){
        return mAlbumsList.size();
    }

    public int getArtistsCount(){
        return mArtistsList.size();
    }

    public int getPlayListsCount(){
        return mPlayLists.size();
    }

    public int getAlbumIdFromIndex(int index){
        return mAlbumsList.get(index).id;
    }

    public Song getSong(int index){
        return mSongList.get(index);
    }

    public Song getSongById(int id) {
        for (Song song: mSongList) {
            if(song.id == id) {
                return song;
            }
        }
        return null;
    }

    public Album getAlbumByIndex(int index){
        return mAlbumsList.get(index);
    }

    public List<Album> getAlbums() {
        return mAlbumsList;
    }

    public Artist getArtistByIndex(int index){
        return mArtistsList.get(index);
    }

    public List<Artist> getArtistsList() {
        return mArtistsList;
    }

    public PlayList getPlaylistByIndex(int index) {
        return mPlayLists.get(index);
    }

    public List<PlayList> getPlaylists() {
        return mPlayLists;
    }

    public List<Song> getAllSongs() {
        return mSongList;
    }

    public int size(){
        return mSongList.size();
    }

    public boolean isEmpty(){
        return size() == 0;
    }
}
