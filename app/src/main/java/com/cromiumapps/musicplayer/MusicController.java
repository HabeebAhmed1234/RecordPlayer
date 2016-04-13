package com.cromiumapps.musicplayer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.cromiumapps.musicplayer.dialogfragment.AddToPlaylistDialogFragment;
import com.cromiumapps.musicplayer.fragments.BaseFragment;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.musicscheduler.MusicScheduler;
import com.cromiumapps.musicplayer.palette.PaletteColorManager;
import com.cromiumapps.musicplayer.picasso.BlurTransformation;
import com.cromiumapps.musicplayer.utils.MusicUtils;
import com.cromiumapps.musicplayer.views.PlayPauseDrawable;
import com.cromiumapps.musicplayer.widget.NextSongModeButton;
import com.squareup.picasso.Picasso;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public class MusicController implements MainActivity.OnMusicServiceConnectedListener, MusicService.MusicPlayerListener, MusicFetcher.MusicFetchedListener {
    private static final String TAG = "MusicService";
    private Song mCurrentSong;
    private MusicService mMusicService;

    //views
    private View mPlayerRoot;

    private ImageView mPlayPauseButton;
    private SeekBar mSeekBar;
    private PlayPauseDrawable mPlayPauseDrawable;
    private ImageView mAlbumArt;
    private ImageView mNextButton;
    private ImageView mPreviousButton;
    private ImageView mAddToPlayListButton;
    private NextSongModeButton mNextSongModeButton;
    private TextView mTitle;
    private TextView mArtist;
    private TextView mCurrentProgressText;
    private TextView mTotalProgressText;

    private Context mContext;
    private BaseFragment.Host mHost;
    private Preferences mPrefs;

    public MusicController(BaseFragment.Host host, Context context, View view) {
        mHost = host;
        mContext = context;
        mPrefs = new Preferences(mContext);

        mPlayerRoot = view;
        mAlbumArt = (ImageView) mPlayerRoot.findViewById(R.id.album_art);
        mPlayPauseButton = (ImageView) mPlayerRoot.findViewById(R.id.play_pause_btn);
        mNextButton = (ImageView) mPlayerRoot.findViewById(R.id.next_button);
        mPreviousButton = (ImageView) mPlayerRoot.findViewById(R.id.previous_button);
        mAddToPlayListButton = (ImageView) mPlayerRoot.findViewById(R.id.add_to_playlist_btn);
        mNextSongModeButton = (NextSongModeButton) mPlayerRoot.findViewById(R.id.next_song_mode_btn);
        mTitle = (TextView) mPlayerRoot.findViewById(R.id.title);
        mArtist = (TextView) mPlayerRoot.findViewById(R.id.artist);
        mSeekBar = (SeekBar) mPlayerRoot.findViewById(R.id.seek_bar);

        mPlayPauseDrawable = new PlayPauseDrawable(mContext);
        mPlayPauseButton.setImageDrawable(mPlayPauseDrawable);

        mCurrentProgressText = (TextView) mPlayerRoot.findViewById(R.id.current_progress_text);
        mTotalProgressText = (TextView) mPlayerRoot.findViewById(R.id.total_progress_text);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCurrentProgressText.setText(MusicUtils.makeTimeString(mContext, progress));
                if (fromUser && mMusicService != null) {
                    mMusicService.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setOnClickListeners();

        mNextSongModeButton.setModeListener(new NextSongModeButton.ModeListener() {
            @Override
            public void onNewMode(MusicScheduler.NextSongMode mode) {
                if (mMusicService != null) {
                    mMusicService.setNextSongMode(mode);
                }
            }
        });

        mAddToPlayListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHost.showDialogFragment(AddToPlaylistDialogFragment.newInstance(mCurrentSong.id));
            }
        });

        View panel = mPlayerRoot.findViewById(R.id.control_panel);
        PaletteColorManager.setViewConfigAlpha(panel, 0.75f);
        //MusicPlayerApplication.getPaletteColorManager().addView(panel);
    }

    public void setNewSong(Song newSong) {
        if(mMusicService != null && newSong != null) mMusicService.playSong(newSong);
    }

    private void showPlayerView() {
        mNextSongModeButton.setMode(mPrefs.getNextSongMode());
        mPlayerRoot.setVisibility(View.VISIBLE);
    }

    private void hidePlayerView() {
        mPlayerRoot.setVisibility(View.GONE);
    }

    private void populateViewWithSong(Song song, boolean isPlaying) {
        Picasso.with(mContext).load("file://" + song.albumArtUri)
                .placeholder(R.drawable.album_placeholder)
                .error(R.drawable.album_placeholder)
                .transform(new BlurTransformation(mContext, 5))
                .into(mAlbumArt);
        mTitle.setText(song.title);
        mArtist.setText(song.artist);
        mTotalProgressText.setText(MusicUtils.makeTimeString(mContext, song.durationSeconds));
        mSeekBar.setMax(song.durationSeconds);
        mPlayPauseDrawable.setCurrentState(isPlaying ? PlayPauseDrawable.STATE.STATE_PLAYING : PlayPauseDrawable.STATE.STATE_PAUSED);
        showPlayerView();
    }

    private void setOnClickListeners() {
        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicService != null) {
                    mPlayPauseDrawable.toggle();
                    if (mPlayPauseDrawable.getCurrentState() == PlayPauseDrawable.STATE.STATE_PAUSED) {
                        mMusicService.pauseSong();
                    } else {
                        mMusicService.resumeSong();
                    }
                }
            }
        });
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicService != null) {
                    mMusicService.playNextSong(true);
                }
            }
        });
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMusicService != null) {
                    mMusicService.playPreviousSong();
                }
            }
        });
    }

    @Override
    public void onMusicServiceConnected(MusicService musicService) {
        mMusicService = musicService;
        mMusicService.addMusicPlayerListener(this);
        Song musicServiceSong = mMusicService.getCurrentSong();
        if (mCurrentSong != null && (musicServiceSong == null || mCurrentSong.id != musicServiceSong.id)) {
            // user clicked on a song that is different to the currently playing song
            // we ask the music service to play the new song
            mMusicService.playSong(mCurrentSong);
            showPlayerView();
        } else if (musicServiceSong != null) {
            // music service is currently holding a song. We should show that song.
            mCurrentSong = musicServiceSong;
            populateViewWithSong(mCurrentSong, mMusicService.isPlaying());
            showPlayerView();
        } else {
            hidePlayerView();
        }
    }

    @Override
    public void onMusicServiceDisconnected() {
        onMusicPlayerError(mContext.getResources().getString(R.string.service_disconected_error));
    }

    //callbacks from MusicService
    @Override
    public void onSongPlay(Song song) {
        mCurrentSong = song;
        populateViewWithSong(mCurrentSong, true);

        mPlayPauseDrawable.setCurrentState(PlayPauseDrawable.STATE.STATE_PLAYING);
    }

    @Override
    public void onSongPause() {
        mPlayPauseDrawable.setCurrentState(PlayPauseDrawable.STATE.STATE_PAUSED);
    }

    @Override
    public void onSongResume() {
    }

    @Override
    public void onSongEnd() {
        mSeekBar.setProgress(0);
    }

    @Override
    public void onSongProgress(int progress) {
        mSeekBar.setProgress(progress);
    }

    @Override
    public void onMusicPlayerError(String what) {
        //TODO: link this up
        // mHost.onGeneralError(what, true);
    }

    @Override
    public void onMusicFetchStart() {

    }

    @Override
    public void onMusicFetched(SongList songList) {
    }

    @Override
    public void onNoMusicOnDevice() {

    }

    @Override
    public void onMusicFetchError(String errorMessage) {

    }
}
