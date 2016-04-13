package com.cromiumapps.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.viewholders.MusicItemViewHolder;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;

/**
 * Created by Habeeb Ahmed on 7/22/2015.
 */
public class AllMusicAdapter extends RecyclerView.Adapter<MusicItemViewHolder> {
    private static final String TAG = "MyDraggableItemAdapter";
    private final OnSongItemClickedListener mSongClickedListener;
    private SongList mData = new SongList();

    public AllMusicAdapter(SongList songList, OnSongItemClickedListener onSongClickedListener) {
        mData = songList;
        mSongClickedListener = onSongClickedListener;
        // DraggableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }

    public void setData(SongList songList) {
        mData = songList;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return mData.getSong(position).id;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public MusicItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.song_list_item, parent, false);
        return new MusicItemViewHolder(v, mSongClickedListener);
    }

    @Override
    public void onBindViewHolder(MusicItemViewHolder holder, int position) {
        final Song item = mData.getSong(position);
       holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
