package com.cromiumapps.musicplayer.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.musicscheduler.MusicScheduler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Habeeb Ahmed on 9/9/2015.
 */
public class NextSongModeButton extends ImageView {
    public interface ModeListener {
        void onNewMode(MusicScheduler.NextSongMode mode);
    }

    private MusicScheduler.NextSongMode mCurrentMode = MusicScheduler.NextSongMode.SEQUENTIAL;
    private boolean mIsInit = false;
    private Map<MusicScheduler.NextSongMode, Integer> mDrawablesMap = new HashMap<MusicScheduler.NextSongMode, Integer>();
    private Map<MusicScheduler.NextSongMode, String> mModeSetMessageMap = new HashMap<MusicScheduler.NextSongMode, String>();
    private ModeListener mListener;


    public NextSongModeButton(Context context) {
        super(context);
        init();
    }

    public NextSongModeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NextSongModeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        if (!mIsInit) {
            //init the drawables map
            mDrawablesMap.put(MusicScheduler.NextSongMode.SEQUENTIAL, R.drawable.ic_repeat);
            mDrawablesMap.put(MusicScheduler.NextSongMode.SHUFFLE, R.drawable.ic_shuffle);
            mDrawablesMap.put(MusicScheduler.NextSongMode.REPEAT_CURRENT_SONG, R.drawable.ic_loop);

            //init the messages map
            mModeSetMessageMap.put(MusicScheduler.NextSongMode.SEQUENTIAL, getContext().getResources().getString(R.string.sequential_msg));
            mModeSetMessageMap.put(MusicScheduler.NextSongMode.SHUFFLE, getContext().getResources().getString(R.string.shuffle_msg));
            mModeSetMessageMap.put(MusicScheduler.NextSongMode.REPEAT_CURRENT_SONG, getContext().getResources().getString(R.string.repeat_current_msg));

            updateImageDrawable();

            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    MusicScheduler.NextSongMode [] modes = MusicScheduler.NextSongMode.values();
                    setMode(modes[(mCurrentMode.ordinal() + 1) % modes.length]);
                }
            });
            mIsInit = true;
        }
    }

    public void setMode(MusicScheduler.NextSongMode mode){
        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            updateImageDrawable();
            showMessage();
            if (mListener != null) mListener.onNewMode(mCurrentMode);
        }
    }

    private void updateImageDrawable(){
        setImageResource(mDrawablesMap.get(mCurrentMode));
    }

    private void showMessage(){
        Toast.makeText(getContext(), mModeSetMessageMap.get(mCurrentMode),
                Toast.LENGTH_SHORT).show();
    }

    public void setModeListener(ModeListener listener){
        mListener = listener;
    }

}
