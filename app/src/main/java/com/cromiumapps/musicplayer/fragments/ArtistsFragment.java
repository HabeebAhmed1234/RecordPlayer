package com.cromiumapps.musicplayer.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.adapters.ArtistsAdapter;
import com.cromiumapps.musicplayer.model.SongList;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public class ArtistsFragment extends ExpandableSectionsFragment implements OnSongItemClickedListener {
    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        return fragment;
    }

    private ArtistsAdapter mAdapter;

    @Override
    protected RecyclerView.Adapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new ArtistsAdapter(new SongList(), this);
        }
        return mAdapter;
    }

    @Override
    protected MusicService.PlayMode getPlayMode() {
        return MusicService.PlayMode.ARTIST;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_artists);
    }

    @Override
    public void onMusicFetched(SongList songList) {
        ((ArtistsAdapter)getAdapter()).setData(songList);
    }
}
