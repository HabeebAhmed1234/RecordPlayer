package com.cromiumapps.musicplayer.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.viewholders.MusicItemViewHolder;
import com.squareup.picasso.Picasso;

/**
 * Created by habeebahmed on 3/27/16.
 */
public abstract class SublistRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int TYPE_SUBLIST_ITEM = 1;
    private int TYPE_PARENT_LIST_ITEM = 2;

    private boolean mIsShowingSublist = false;
    private int mVisibleGroupIndex = 0;

    @Override
    public int getItemViewType(int position) {
        return mIsShowingSublist ? TYPE_SUBLIST_ITEM : TYPE_PARENT_LIST_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        if(viewType == TYPE_SUBLIST_ITEM) {
            View expandableCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.song_list_item, parent, false);
            holder = new MusicItemViewHolder(expandableCard, getSongClickedListener());
        } else if (viewType == TYPE_PARENT_LIST_ITEM){
            View expandableCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.parent_card_view, parent, false);
            holder = new RecyclerView.ViewHolder(expandableCard) {};
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (mIsShowingSublist) {
            ((MusicItemViewHolder) holder).bind(getGroupItem(mVisibleGroupIndex, position));
        } else {
            Picasso.with(holder.itemView.getContext())
                    .load("file://" + getGroupThumbnail(position))
                    .error(R.drawable.album_placeholder)
                    .placeholder(R.drawable.album_placeholder)
                    .into((ImageView) holder.itemView.findViewById(R.id.thumbnail));

            ((TextView) holder.itemView.findViewById(R.id.title)).setText(getGroupTitle(position));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mIsShowingSublist = true;
                    mVisibleGroupIndex = position;
                    notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mIsShowingSublist ? getGroupItemsCount(mVisibleGroupIndex) : getGroupCount();
    }

    public boolean onBackPressed() {
        if(mIsShowingSublist) {
            mIsShowingSublist = false;
            notifyDataSetChanged();
            return true;
        } else {
            return false;
        }
    }

    protected abstract String getGroupThumbnail(int position);

    protected abstract String getGroupTitle(int position);

    protected abstract Song getGroupItem(int groupIndex, int position);

    protected abstract int getGroupItemsCount(int groupIndex);

    protected abstract int getGroupCount();

    protected abstract OnSongItemClickedListener getSongClickedListener();
}
