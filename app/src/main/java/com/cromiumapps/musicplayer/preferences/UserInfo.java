package com.cromiumapps.musicplayer.preferences;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.cromiumapps.musicplayer.gson.UriTypeAdapter;
import com.cromiumapps.musicplayer.model.SongList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UserInfo extends AbstractSharedPreferences {

    public static final String USERINFO_APP_VERSION = "userinfo_app_version";
    public static final String USERINFO_VERSION = "userinfo_version";
    public static final String USERINFO_IS_LOCAL_DB_EXPIRED = "userinfo_is_local_db_expired";
    public static final String USER_INFO_COLOUR_QUALITY = "userinfo_colour_quality";

    public static final String COLOUR_QUALITY_PREF_VALUE_BRIGHT = "bright";
    public static final String COLOUR_QUALITY_PREF_VALUE_DARK = "dark";

    private Gson mGson;

    public UserInfo(Context context) {
        super(context);
        mGson = new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriTypeAdapter())
                .create();
    }

    public void clearUserData() {
        clearChanges();
        commitChanges();
    }

    public String getAppVersion(){
        String version = getStringByKey(USERINFO_APP_VERSION);

        if (version.length() == 0){
            try{
                version = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).versionName;
                setByKey(USERINFO_APP_VERSION, version);
                commitChanges();
            } catch (Exception e) {
                return "";
            }
        }

        return version;
    }

    public int getVersion() {
        return getIntByKey(USERINFO_VERSION);
    }

    public void setVersion(int version) {
        setByKey(USERINFO_VERSION, version);
    }

    public void setIsLocalDBExpired(boolean isLocalDBExpired){
        setByKey(USERINFO_IS_LOCAL_DB_EXPIRED, isLocalDBExpired);
    }

    public boolean isLocalDBExpired(){
        return getBooleanByKey(USERINFO_IS_LOCAL_DB_EXPIRED, true);
    }

    public void setColorQuality(String quality){
        setByKey(USER_INFO_COLOUR_QUALITY, quality);
    }

    public String getColourQuality() {
        return getStringByKey(USER_INFO_COLOUR_QUALITY, COLOUR_QUALITY_PREF_VALUE_DARK);
    }
}
