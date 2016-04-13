package com.cromiumapps.musicplayer.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.adapters.SublistRecyclerViewAdapter;
import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.model.Song;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public abstract class ExpandableSectionsFragment extends BaseFragment implements OnSongItemClickedListener {

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    protected abstract RecyclerView.Adapter getAdapter();
    protected abstract MusicService.PlayMode getPlayMode();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setAdapter(getAdapter());
    }

    @Override
    public int getRootViewId() {
        return R.layout.fragment_expandable_lists;
    }

    @Override
    public void onSongClicked(Song song) {
        mHost.setMusicPlayMode(getPlayMode());
        mHost.openMusicController(song);
    }

    @Override
    public boolean onBackPressed() {
        SublistRecyclerViewAdapter adapter = (SublistRecyclerViewAdapter) getAdapter();
        return adapter.onBackPressed();
    }
}
