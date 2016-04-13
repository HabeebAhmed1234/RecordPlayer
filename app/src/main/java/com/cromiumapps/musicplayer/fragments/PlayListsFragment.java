package com.cromiumapps.musicplayer.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.adapters.PlayListsAdapter;
import com.cromiumapps.musicplayer.model.SongList;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public class PlayListsFragment extends ExpandableSectionsFragment implements OnSongItemClickedListener {
    public static PlayListsFragment newInstance() {
        PlayListsFragment fragment = new PlayListsFragment();
        return fragment;
    }

    private PlayListsAdapter mAdapter;

    @Override
    protected RecyclerView.Adapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new PlayListsAdapter(new SongList(), this);
        }
        return mAdapter;
    }

    @Override
    protected MusicService.PlayMode getPlayMode() {
        return MusicService.PlayMode.PLAYLIST;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_playlists);
    }

    @Override
    public void onMusicFetched(SongList songList) {
        ((PlayListsAdapter)getAdapter()).setData(songList);
    }
}
