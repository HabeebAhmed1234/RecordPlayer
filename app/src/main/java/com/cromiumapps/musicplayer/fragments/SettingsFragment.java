package com.cromiumapps.musicplayer.fragments;

import com.cromiumapps.musicplayer.MainActivity;
import com.cromiumapps.musicplayer.MusicPlayerApplication;
import com.cromiumapps.musicplayer.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v4.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.cromiumapps.musicplayer.model.SongList;
import com.cromiumapps.musicplayer.musicfetcher.MusicFetcher;
import com.cromiumapps.musicplayer.palette.PaletteColorManager;
import com.cromiumapps.musicplayer.preferences.UserInfo;

/**
 * Created by Habeeb Ahmed on 7/21/2015.
 */
public class SettingsFragment extends PreferenceFragment {

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    private BaseFragment.Host mHost;
    private MusicFetcher mMusicFetcher;

    private Preference mRescanPreference;
    private Preference mThemeColourQualityPreference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        MusicPlayerApplication.getPaletteColorManager().addView(view);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mHost = (BaseFragment.Host) getActivity();
        mMusicFetcher = mHost.getMusicFetcher();

        mRescanPreference = findPreference("rescan_preference");
        mRescanPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new UserInfo(getActivity()).setIsLocalDBExpired(true);
                mMusicFetcher.fetchMusic();
                return true;
            }
        });

        mThemeColourQualityPreference = findPreference("colours_quality");
        mThemeColourQualityPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                new UserInfo(getActivity()).setColorQuality((String) newValue);
                Intent i = getActivity().getPackageManager().getLaunchIntentForPackage( getActivity().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra(MainActivity.EXTRA_GOTO_SETTINGS, true);
                startActivity(i);
                return true;
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
