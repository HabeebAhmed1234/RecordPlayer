package com.cromiumapps.musicplayer.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.utils.MusicUtils;
import com.squareup.picasso.Picasso;

/**
 * Created by Habeeb Ahmed on 7/22/2015.
 */
public class MusicItemViewHolder extends RecyclerView.ViewHolder {
    public View mContainer;
    public ImageView mThumbnail;
    public TextView mTitle;
    public TextView mArtist;
    public TextView mDuration;
    public Song mItem;

    //private PaletteView mContainerPaletteView;
    private OnSongItemClickedListener mOnSongClickedListener;

    public MusicItemViewHolder(View v, OnSongItemClickedListener onClickListener) {
        super(v);
        mContainer = (View) v.findViewById(R.id.container);
        mThumbnail = (ImageView) v.findViewById(R.id.thumbnail);
        mTitle  = (TextView) v.findViewById(R.id.title);
        mArtist = (TextView) v.findViewById(R.id.artist);
        mDuration = (TextView) v.findViewById(R.id.duration);
        mOnSongClickedListener = onClickListener;
    }

    public void bind(Song song){
        mItem = song;
        Context context = mContainer.getContext();
        // set text and image
        if (song.albumArtUri != null) {
            Picasso.with(context)
                    .load("file://" + song.albumArtUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.album_placeholder)
                    .into(mThumbnail);
        } else {
            mThumbnail.setImageResource(R.drawable.album_placeholder);
        }
        mTitle.setText(song.title);
        mArtist.setText(song.artist);
        if (song.durationSeconds > 0) {
            mDuration.setText(MusicUtils.makeTimeString(context, song.durationSeconds));
        } else {
            mDuration.setText("--:--");
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnSongClickedListener.onSongClicked(mItem);
            }
        });
    }
}
