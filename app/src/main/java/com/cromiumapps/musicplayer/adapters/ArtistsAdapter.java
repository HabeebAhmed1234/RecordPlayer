package com.cromiumapps.musicplayer.adapters;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;

import java.util.List;

public class ArtistsAdapter extends SublistRecyclerViewAdapter {
    private static final String TAG = "ArtistsAdapter";

    private SongList mData = new SongList();
    private final OnSongItemClickedListener mSongClickedListener;

    public ArtistsAdapter(SongList songList, OnSongItemClickedListener onSongClickedListener) {
        mData = songList;
        mSongClickedListener = onSongClickedListener;
    }

    public void setData(SongList songList){
        mData = songList;
        notifyDataSetChanged();
    }

    @Override
    protected String getGroupThumbnail(int position) {
        return mData.getArtistByIndex(position).thumbnailUri;
    }

    @Override
    protected String getGroupTitle(int position) {
        return mData.getArtistByIndex(position).name;
    }

    @Override
    protected Song getGroupItem(int groupIndex, int position) {
        return mData.getArtistByIndex(groupIndex).getSongs().get(position);
    }

    @Override
    protected int getGroupItemsCount(int groupIndex) {
        return mData.getArtistByIndex(groupIndex).getSongListSize();
    }

    @Override
    protected int getGroupCount() {
        return mData.getArtistsCount();
    }

    @Override
    protected OnSongItemClickedListener getSongClickedListener() {
        return mSongClickedListener;
    }
}
