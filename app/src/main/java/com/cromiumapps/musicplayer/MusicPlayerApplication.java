package com.cromiumapps.musicplayer;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.support.v7.graphics.Palette;

import com.cromiumapps.musicplayer.palette.PaletteColorManager;
public class MusicPlayerApplication extends Application {
    private static final String TAG = "MusicPlayerApplication";
    private static PaletteColorManager PALETTE_COLOR_MANAGER;

    public static PaletteColorManager getPaletteColorManager() {
        return PALETTE_COLOR_MANAGER;
    }

    public void onCreate() {
        super.onCreate();
        // only initialize for the main process
        if (getPackageName().equals(getProcessName())) {
            PALETTE_COLOR_MANAGER = new PaletteColorManager(this);

        }
    }

    private String getProcessName() {
        String processName = null;
        final int pid = Process.myPid();
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                processName = processInfo.processName;
                break;
            }
        }
        return processName;
    }
}
