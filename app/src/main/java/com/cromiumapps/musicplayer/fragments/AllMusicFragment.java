package com.cromiumapps.musicplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.adapters.AllMusicAdapter;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public class AllMusicFragment extends BaseFragment implements OnSongItemClickedListener {
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private AllMusicAdapter mAdapter;

    public static AllMusicFragment newInstance() {
        AllMusicFragment fragment = new AllMusicFragment();
        return fragment;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_all_music);
    }

    @Override
    public int getRootViewId() {
        return R.layout.fragment_all_music;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = getAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    protected AllMusicAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new AllMusicAdapter(new SongList(), this);
        }
        return mAdapter;
    }

    @Override
    public void onMusicFetched(SongList songList) {
        getAdapter().setData(songList);
    }

    @Override
    public void onSongClicked(Song song) {
        mHost.setMusicPlayMode(MusicService.PlayMode.ALL_MUSIC);
        mHost.openMusicController(song);
    }
}
