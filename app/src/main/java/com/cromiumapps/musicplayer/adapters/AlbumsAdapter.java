package com.cromiumapps.musicplayer.adapters;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;

public class AlbumsAdapter extends SublistRecyclerViewAdapter {
    private static final String TAG = "AlbumsAdapter";

    private SongList mData = new SongList();
    private final OnSongItemClickedListener mSongClickedListener;

    public AlbumsAdapter(SongList songList, OnSongItemClickedListener onSongClickedListener) {
        mData = songList;
        mSongClickedListener = onSongClickedListener;
    }

    public void setData(SongList songList){
        mData = songList;
        notifyDataSetChanged();
    }

    @Override
    protected String getGroupThumbnail(int position) {
        return mData.getAlbumByIndex(position).thumbnailUri;
    }

    @Override
    protected String getGroupTitle(int position) {
        return mData.getAlbumByIndex(position).title;
    }

    @Override
    protected Song getGroupItem(int groupIndex, int position) {
        return mData.getAlbumByIndex(groupIndex).getSongs().get(position);
    }

    @Override
    protected int getGroupItemsCount(int groupIndex) {
        return mData.getAlbumByIndex(groupIndex).getSongListSize();
    }

    @Override
    protected int getGroupCount() {
        return mData.getAlbumsCount();
    }

    @Override
    protected OnSongItemClickedListener getSongClickedListener() {
        return mSongClickedListener;
    }
}
