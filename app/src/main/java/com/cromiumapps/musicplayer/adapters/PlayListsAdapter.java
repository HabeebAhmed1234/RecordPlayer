package com.cromiumapps.musicplayer.adapters;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;

import java.util.List;

public class PlayListsAdapter extends SublistRecyclerViewAdapter {
    private static final String TAG = "PlayListsAdapter";

    private SongList mData = new SongList();
    private final OnSongItemClickedListener mSongClickedListener;

    public PlayListsAdapter(SongList songList, OnSongItemClickedListener onSongClickedListener) {
        mData = songList;
        mSongClickedListener = onSongClickedListener;
    }

    public void setData(SongList songList){
        mData = songList;
        notifyDataSetChanged();
    }

    @Override
    protected String getGroupThumbnail(int position) {
        return mData.getPlaylistByIndex(position).thumbnailUri;
    }

    @Override
    protected String getGroupTitle(int position) {
        return mData.getPlaylistByIndex(position).name;
    }

    @Override
    protected Song getGroupItem(int groupItem, int position) {
        return mData.getPlaylistByIndex(groupItem).getSongs().get(position);
    }

    @Override
    protected int getGroupItemsCount(int groupIndex) {
        return mData.getPlaylistByIndex(groupIndex).getSongListSize();
    }

    @Override
    protected int getGroupCount() {
        return mData.getPlayListsCount();
    }

    @Override
    protected OnSongItemClickedListener getSongClickedListener() {
        return mSongClickedListener;
    }

}
