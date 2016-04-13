package com.cromiumapps.musicplayer.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cromiumapps.musicplayer.MainActivity;
import com.cromiumapps.musicplayer.MusicPlayerApplication;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.model.Song;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public abstract class BaseFragment extends Fragment implements MusicFetcher.MusicFetchedListener {
    public interface Host {
        void setMusicPlayMode(MusicService.PlayMode mode);
        MusicFetcher getMusicFetcher();
        void showLoadingScreen();
        void hideLoadingScreen();
        void openMusicController(Song song);
        void addMusicServiceConnectedListener(MainActivity.OnMusicServiceConnectedListener listener);
        void removeMusicServiceConnectedListener(MainActivity.OnMusicServiceConnectedListener listener);
        void onGeneralError(String what, boolean shouldExitAfter);
        void showDialogFragment(DialogFragment dialogFragment);
    }

    public abstract String getTitle(Context context);
    public abstract int getRootViewId();
    protected Host mHost;

    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mHost = ((Host) activity);
        mHost.getMusicFetcher().addMusicFetchedListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mHost.getMusicFetcher().removeOnMusicFetchedListener(this);
        mHost = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getRootViewId(), container, false);
        MusicPlayerApplication.getPaletteColorManager().addView(view);
        return view;
    }


    @Override
    public void onMusicFetchStart() {}

    @Override
    public void onMusicFetched(SongList songList){}

    @Override
    public void onNoMusicOnDevice() {}

    @Override
    public void onMusicFetchError(String errorMessage) {}
}
