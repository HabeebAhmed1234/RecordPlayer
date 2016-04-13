package com.cromiumapps.musicplayer.fragments;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.listeners.MusicPlayerListener;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainFragmentAdapter extends FragmentPagerAdapter {
    private static final String TAG = "MainFragmentAdapter";
    //reference to all fragments that are not the settings fragment
    private ArrayList<BaseFragment> mMusicFragments = new ArrayList<BaseFragment>();

    //all drawer fragments
    private ArrayList<Fragment> mPagerFragments = new ArrayList<Fragment>();

    // root fragments
    private AllMusicFragment mAllMusicFragment;
    private AlbumsFragment mAlbumsFragment;
    private ArtistsFragment mArtistsFragment;
    private PlayListsFragment mPlayListsFragment;
    private SettingsFragment mSettingsFragment;

    private Context mContext;
    private final FragmentManager mFragmentManager;

    public MainFragmentAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        mFragmentManager = fragmentManager;
        mContext = context;

        mAllMusicFragment = AllMusicFragment.newInstance();
        mAlbumsFragment = AlbumsFragment.newInstance();
        mArtistsFragment = ArtistsFragment.newInstance();
        mPlayListsFragment = PlayListsFragment.newInstance();
        mSettingsFragment = SettingsFragment.newInstance();

        mPagerFragments.add(mAllMusicFragment);
        mPagerFragments.add(mAlbumsFragment);
        mPagerFragments.add(mArtistsFragment);
        mPagerFragments.add(mPlayListsFragment);
        mPagerFragments.add(mSettingsFragment);

        mMusicFragments.add(mAllMusicFragment);
        mMusicFragments.add(mAlbumsFragment);
        mMusicFragments.add(mArtistsFragment);
        mMusicFragments.add(mPlayListsFragment);
    }

    @Override
    public Fragment getItem(int position) {
        return mPagerFragments.get(position);
    }

    @Override
    public int getCount() {
        return mPagerFragments.size();
    }

    public int getSettingsFragmentIndex() {
        return mPagerFragments.indexOf(mSettingsFragment);
    }

    public boolean onBackPressed(int currentFragment) {
        try {
            return ((BaseFragment)getItem(currentFragment)).onBackPressed();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        BaseFragment fragment = null;
        try {
            fragment = (BaseFragment) mPagerFragments.get(position);
        } catch (ClassCastException e) {
            Log.e(TAG, e.getMessage());
        }
        if (fragment != null) {
            return fragment.getTitle(mContext);
        } else if (mPagerFragments.get(position) instanceof SettingsFragment) {
            return mContext.getString(R.string.title_settings);
        }
        return "";
    }
}
