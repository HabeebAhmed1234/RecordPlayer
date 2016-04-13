package com.cromiumapps.musicplayer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cromiumapps.musicplayer.fragments.BaseFragment;
import com.cromiumapps.musicplayer.fragments.FragmentUiManager;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.palette.PaletteColorManager;
import com.cromiumapps.musicplayer.views.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class MainActivity extends FragmentActivity implements MusicFetcher.MusicFetchedListener, BaseFragment.Host, MusicService.MusicPlayerListener {

    public interface OnMusicServiceConnectedListener {
        void onMusicServiceConnected(MusicService musicService);
        void onMusicServiceDisconnected();
    }

    private static final String TAG = "MainActivity";
    public static final String EXTRA_NOTIFICATION_BAR_LAUNCH = "extra_notification_bar_launch";
    public static final String EXTRA_GOTO_SETTINGS = "extra_goto_settings";

    private MusicFetcher mMusicFetcher;

    private View mLoadingView;
    private PagerSlidingTabStrip mTabs;
    private FragmentUiManager mFragmentUiManager;
    private MusicController mMusicController;
    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private TextView mToolbarTitle;
    private TextView mToolbarSubTitle;

    //service variables
    private MusicService mMusicService;
    private Set<OnMusicServiceConnectedListener> mMusicServiceConnectedListeners = new HashSet<OnMusicServiceConnectedListener>();
    private boolean mIsMusicServiceBound = false;

    private ServiceConnection mMusicServiceConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            mIsMusicServiceBound = true;
            mMusicService = binder.getService();
            mMusicFetcher.addMusicFetchedListener(mMusicService);
            Song currentSong = mMusicService.getCurrentSong();
            if (currentSong != null) {
                mMusicController.setNewSong(currentSong);
                setToolbarCurrentSong(currentSong);
            }
            for (OnMusicServiceConnectedListener listener : mMusicServiceConnectedListeners) {
                listener.onMusicServiceConnected(mMusicService);
            }
            mMusicService.addMusicPlayerListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMusicService.removeMusicPlayerListener(MainActivity.this);
            mMusicFetcher.removeOnMusicFetchedListener(mMusicService);
            mIsMusicServiceBound = false;
            for (OnMusicServiceConnectedListener listener : mMusicServiceConnectedListeners) {
                listener.onMusicServiceDisconnected();
            }
        }
    };

    //pallete variables
    private PaletteColorManager mPaletteColorManager;

    private void handleLaunchIntent(Intent intent){
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (extras.containsKey(EXTRA_NOTIFICATION_BAR_LAUNCH) && extras.getBoolean(EXTRA_NOTIFICATION_BAR_LAUNCH)) {
                    openMusicController(null);
                    setIntent(null);
                } else if (extras.containsKey(EXTRA_GOTO_SETTINGS) && extras.getBoolean(EXTRA_GOTO_SETTINGS)) {
                    openSettingsFragment();
                }
            }
        }
    }

    private void connectToMusicService(){
        Intent musicServiceStartIntent = new Intent(this, MusicService.class);
        bindService(musicServiceStartIntent, mMusicServiceConnection, Context.BIND_AUTO_CREATE);
        startService(musicServiceStartIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mLoadingView = findViewById(R.id.loading_view);
        mTabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mMusicController = new MusicController(this, this, findViewById(R.id.music_controller_view));
        mAppBar = (AppBarLayout) findViewById(R.id.appbar);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        mToolbarSubTitle = (TextView) findViewById(R.id.toolbar_subtitle);
        mAppBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                float total = appBarLayout.getTotalScrollRange();
                float fraction = total == 0 ? 0 : ((float) Math.abs(verticalOffset)) / total;
                mToolbar.setAlpha(fraction);
            }
        });

        mMusicFetcher = new MusicFetcher(this, getLoaderManager());
        mFragmentUiManager = new FragmentUiManager(this, getSupportFragmentManager());
        mTabs.setViewPager(mFragmentUiManager.getViewPager());
        mTabs.setTextColorResource(R.color.white);
        mMusicFetcher.addMusicFetchedListener(this);
        mMusicFetcher.addMusicFetchedListener(mMusicController);
        mMusicFetcher.fetchMusic();
        connectToMusicService();

        mPaletteColorManager = MusicPlayerApplication.getPaletteColorManager();
        mPaletteColorManager.addView(mTabs);
        mPaletteColorManager.addView(mToolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handleLaunchIntent(getIntent());
        addMusicServiceConnectedListener(mMusicController);
        if(mMusicService != null) {
            mMusicService.addMusicPlayerListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMusicService != null) {
            mMusicService.removeMusicPlayerListener(mMusicController);
            mMusicService.removeMusicPlayerListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMusicFetcher.removeOnMusicFetchedListener(this);
        mMusicServiceConnectedListeners.clear();
    }

    @Override
    public void onBackPressed() {
        if (!mFragmentUiManager.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                openSettingsFragment();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setMusicPlayMode(MusicService.PlayMode mode) {
        if (mMusicService != null) {
            mMusicService.setCurrentPlayMode(mode);
        }
    }

    @Override
    public MusicFetcher getMusicFetcher(){
        return mMusicFetcher;
    }

    @Override
    public void showLoadingScreen() {
        mLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoadingScreen() {
        mLoadingView.setVisibility(View.GONE);
    }

    @Override
    public void openMusicController(Song newSong) {
        mMusicController.setNewSong(newSong);
        mAppBar.setExpanded(true);
    }

    public void openSettingsFragment() {
        mFragmentUiManager.openSettingsFragment();
    }

    @Override
    public void addMusicServiceConnectedListener(OnMusicServiceConnectedListener listener) {
        mMusicServiceConnectedListeners.add(listener);
        if (mIsMusicServiceBound) {
            listener.onMusicServiceConnected(mMusicService);
        }
    }

    @Override
    public void removeMusicServiceConnectedListener(OnMusicServiceConnectedListener listener) {
        mMusicServiceConnectedListeners.remove(listener);
    }

    @Override
    public void onMusicFetchStart() {
        showLoadingScreen();
    }

    @Override
    public void onMusicFetched(SongList songList) {
        hideLoadingScreen();
    }

    @Override
    public void onNoMusicOnDevice() {
        hideLoadingScreen();
    }

    @Override
    public void onMusicFetchError(String errorMessage) {
        hideLoadingScreen();
        showErrorDialog(errorMessage, true);
    }

    @Override
    public void onGeneralError(String what, boolean shouldExitAfter){
        showErrorDialog(what, shouldExitAfter);
    }

    @Override
    public void showDialogFragment(DialogFragment dialogFragment) {
        mFragmentUiManager.showDialogFragment(dialogFragment);
    }

    private void showErrorDialog(String message, final boolean shouldExitAfter){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getResources().getString(R.string.error));
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(getResources().getString(shouldExitAfter ? R.string.exit_app : R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (shouldExitAfter) {
                    finish();
                } else {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void setToolbarCurrentSong(Song song) {
        mToolbarTitle.setText(song.title);
        mToolbarSubTitle.setText(song.artist);
    }

    @Override
    public void onSongPlay(Song song) {
        setToolbarCurrentSong(song);
        mAppBar.setExpanded(true);
    }

    @Override
    public void onSongPause() {

    }

    @Override
    public void onSongResume() {

    }

    @Override
    public void onSongEnd() {

    }

    @Override
    public void onSongProgress(int progress) {

    }

    @Override
    public void onMusicPlayerError(String what) {
        showErrorDialog(what, false);
    }
}
