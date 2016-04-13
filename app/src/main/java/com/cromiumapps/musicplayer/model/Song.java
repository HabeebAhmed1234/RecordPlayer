package com.cromiumapps.musicplayer.model;

/**
 * Created by Habeeb Ahmed on 7/23/2015.
 */
public class Song {
    public final int id;
    public final String title;
    public final String artist;
    public final String playList;
    public final int durationSeconds;
    public final String playableUri;
    public final int albumId;
    public final String albumName;
    public final String albumArtUri;

    public Song(int id, String title, String artist, String playList, int albumId, String albumName, int durationSeconds, String albumArtUri, String playableUri){
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.playList = playList;
        this.albumId = albumId;
        this.albumName = albumName;
        this.durationSeconds = durationSeconds;
        this.albumArtUri = albumArtUri;
        this.playableUri = playableUri;
    }

    @Override
    public boolean equals(Object other) {
        return ((Song)other).id == this.id;
    }
}
