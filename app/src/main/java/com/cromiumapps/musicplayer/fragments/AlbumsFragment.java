package com.cromiumapps.musicplayer.fragments;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.cromiumapps.musicplayer.listeners.OnSongItemClickedListener;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.adapters.AlbumsAdapter;
import com.cromiumapps.musicplayer.model.SongList;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public class AlbumsFragment extends ExpandableSectionsFragment implements OnSongItemClickedListener {
    public static AlbumsFragment newInstance() {
        AlbumsFragment fragment = new AlbumsFragment();
        return fragment;
    }

    private AlbumsAdapter mAdapter;

    @Override
    protected RecyclerView.Adapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new AlbumsAdapter(new SongList(), this);
        }
        return mAdapter;
    }

    @Override
    protected MusicService.PlayMode getPlayMode() {
        return MusicService.PlayMode.ALBUM;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_albums);
    }

    @Override
    public void onMusicFetched(SongList songList) {
        ((AlbumsAdapter)getAdapter()).setData(songList);
    }
}
