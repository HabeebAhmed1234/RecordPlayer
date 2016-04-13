package com.cromiumapps.musicplayer.fragments;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;

import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;

public class FragmentUiManager {

    private ViewPager mViewPager;
    private MainFragmentAdapter mMainFragmentPagerAdapter;

    private final FragmentManager mFragmentManager;

    public FragmentUiManager(Activity activity, FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mMainFragmentPagerAdapter = new MainFragmentAdapter(activity, fragmentManager);
        mViewPager = (ViewPager) activity.findViewById(R.id.pager);
        mViewPager.setAdapter(mMainFragmentPagerAdapter);
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public void openSettingsFragment() {
        mViewPager.setCurrentItem(mMainFragmentPagerAdapter.getSettingsFragmentIndex());
    }

    public boolean onBackPressed() {
        return mMainFragmentPagerAdapter.onBackPressed(mViewPager.getCurrentItem());
    }

    public void showDialogFragment(DialogFragment dialogFragment){
        dialogFragment.show(mFragmentManager, "dialog_fragment");
    }
}
