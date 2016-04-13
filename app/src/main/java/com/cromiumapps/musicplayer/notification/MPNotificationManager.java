package com.cromiumapps.musicplayer.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;

import com.cromiumapps.musicplayer.MainActivity;
import com.cromiumapps.musicplayer.MusicService;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.model.Song;
import com.cromiumapps.musicplayer.utils.MusicUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.security.InvalidParameterException;

/**
 * Created by Habeeb Ahmed on 9/9/2015.
 */
public class MPNotificationManager implements MusicService.MusicPlayerListener {

    public static final int CONTROL_PANEL_NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_VIEWS_CLICK_INTENT_FILTER = "notification_views_click_intent_filter";
    private static final String EXTRA_NOTIFICATION_BUTTON_CLICKED = "extra_notification_button_clicked";
    private static final int NOTIFICATION_PLAY_PAUSE_BUTTON_CLICKED = 1;
    private static final int NOTIFICATION_NEXT_BUTTON_CLICKED = 2;
    private static final int NOTIFICATION_PREVIOUS_BUTTON_CLICKED = 3;
    private static final int NOTIFICATION_X_BUTTON_CLICKED = 4;
    private MusicService mMusicService;

    private Target mNotificationAlbumArtTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
            startForeground(getNotification(mMusicService.getCurrentSong(), bitmap, !mMusicService.isPlaying()));
        }

        @Override
        public void onBitmapFailed(Drawable drawable) {
            startForeground(getNotification(mMusicService.getCurrentSong(), null, !mMusicService.isPlaying()));
        }

        @Override
        public void onPrepareLoad(Drawable drawable) {

        }
    };

    public MPNotificationManager(MusicService musicService){
        mMusicService = musicService;
        init();
    }

    public void init(){
        mMusicService.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    int button = extras.getInt(EXTRA_NOTIFICATION_BUTTON_CLICKED);
                    switch (button) {
                        case NOTIFICATION_PLAY_PAUSE_BUTTON_CLICKED:
                            if (mMusicService.isPlaying()) {
                                mMusicService.pauseSong();
                            } else {
                                mMusicService.resumeSong();
                            }
                            break;
                        case NOTIFICATION_NEXT_BUTTON_CLICKED:
                            mMusicService.playNextSong(true);
                            break;
                        case NOTIFICATION_PREVIOUS_BUTTON_CLICKED:
                            mMusicService.playPreviousSong();
                            break;
                        case NOTIFICATION_X_BUTTON_CLICKED:
                            mMusicService.pauseSong();
                            mMusicService.stopForeground(true);
                            break;
                        default:
                            throw new InvalidParameterException("notification button clicked id not recognized");
                    }
                }
            }
        }, new IntentFilter(NOTIFICATION_VIEWS_CLICK_INTENT_FILTER));
        mMusicService.addMusicPlayerListener(this);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification getNotification(Song song, Bitmap albumArt, boolean showPlay){
        Intent launchIntent = new Intent(mMusicService, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra(MainActivity.EXTRA_NOTIFICATION_BAR_LAUNCH, true);
        PendingIntent pendingLaunchIntent = PendingIntent.getActivity(mMusicService, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mMusicService);

        RemoteViews notificationView = new RemoteViews(mMusicService.getPackageName(), R.layout.notification_bar_layout);
        RemoteViews expandedView = new RemoteViews(mMusicService.getPackageName(),R.layout.notification_bar_layout_expanded);

        Bitmap bigBitmap = albumArt;
        if (bigBitmap == null) {
            bigBitmap = BitmapFactory.decodeResource(mMusicService.getResources(), R.drawable.album_placeholder_black);
        }

        applyIntentsAndContent(notificationView, bigBitmap, song, showPlay);
        applyIntentsAndContent(expandedView, bigBitmap, song, showPlay);

        builder.setContentIntent(pendingLaunchIntent)
                .setSmallIcon(R.drawable.ic_play)
                .setTicker("Now Playing")
                .setOngoing(true)
                .setContent(notificationView)
                .setStyle(new NotificationCompat.BigPictureStyle()
                .bigPicture(bigBitmap));

        Notification notification;

        notification = builder.build();

        if (Build.VERSION.SDK_INT >= 16) {
            notification.bigContentView = expandedView;
        }

        return notification;
    }

    private void applyIntentsAndContent(RemoteViews remoteView, Bitmap albumArt, Song song, boolean showPlay){

        Intent nextButtonClickedIntent = new Intent(NOTIFICATION_VIEWS_CLICK_INTENT_FILTER);
        nextButtonClickedIntent.putExtra(EXTRA_NOTIFICATION_BUTTON_CLICKED, NOTIFICATION_NEXT_BUTTON_CLICKED);
        Intent previousButtonClickedIntent = new Intent(NOTIFICATION_VIEWS_CLICK_INTENT_FILTER);
        previousButtonClickedIntent.putExtra(EXTRA_NOTIFICATION_BUTTON_CLICKED, NOTIFICATION_PREVIOUS_BUTTON_CLICKED);
        Intent playPauseButtonClickedIntent = new Intent(NOTIFICATION_VIEWS_CLICK_INTENT_FILTER);
        playPauseButtonClickedIntent.putExtra(EXTRA_NOTIFICATION_BUTTON_CLICKED, NOTIFICATION_PLAY_PAUSE_BUTTON_CLICKED);
        Intent xButtonClickedIntent = new Intent(NOTIFICATION_VIEWS_CLICK_INTENT_FILTER);
        xButtonClickedIntent.putExtra(EXTRA_NOTIFICATION_BUTTON_CLICKED, NOTIFICATION_X_BUTTON_CLICKED);

        PendingIntent nextButtonPi = PendingIntent.getBroadcast(mMusicService, NOTIFICATION_NEXT_BUTTON_CLICKED, nextButtonClickedIntent, 0);
        PendingIntent previousButtonPi = PendingIntent.getBroadcast(mMusicService, NOTIFICATION_PREVIOUS_BUTTON_CLICKED, previousButtonClickedIntent, 0);
        PendingIntent playPauseButtonPi = PendingIntent.getBroadcast(mMusicService, NOTIFICATION_PLAY_PAUSE_BUTTON_CLICKED, playPauseButtonClickedIntent, 0);
        PendingIntent xButtonPi = PendingIntent.getBroadcast(mMusicService, NOTIFICATION_X_BUTTON_CLICKED, xButtonClickedIntent, 0);


        remoteView.setOnClickPendingIntent(R.id.next_button, nextButtonPi);
        remoteView.setOnClickPendingIntent(R.id.previous_button, previousButtonPi);
        remoteView.setOnClickPendingIntent(R.id.play_pause_btn, playPauseButtonPi);
        remoteView.setOnClickPendingIntent(R.id.x_button, xButtonPi);

        if (albumArt == null) {
            remoteView.setImageViewResource(R.id.album_art, R.drawable.album_placeholder);
        } else {
            remoteView.setImageViewBitmap(R.id.album_art, albumArt);
        }
        remoteView.setTextViewText(R.id.title, song.title);
        remoteView.setTextViewText(R.id.artist, song.artist);
        remoteView.setImageViewResource(R.id.play_pause_btn, showPlay ? R.drawable.ic_play : R.drawable.ic_pause);
    }

    private void startForeground(Notification notification) {
        mMusicService.startForeground(MPNotificationManager.CONTROL_PANEL_NOTIFICATION_ID, notification);
    }

    private void refreshNotificationStatus(Song song){
        if (!TextUtils.isEmpty(song.albumArtUri)) {
            Picasso.with(mMusicService).load("file://" + song.albumArtUri)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(mNotificationAlbumArtTarget);
        } else {
            startForeground(getNotification(song, null, !mMusicService.isPlaying()));
        }
    }

    @Override
    public void onSongPlay(Song song) {
        refreshNotificationStatus(song);
    }

    @Override
    public void onSongPause() {
        refreshNotificationStatus(mMusicService.getCurrentSong());
    }

    @Override
    public void onSongResume() {
        refreshNotificationStatus(mMusicService.getCurrentSong());
    }

    @Override
    public void onSongEnd() {

    }

    @Override
    public void onSongProgress(int progress) {

    }

    @Override
    public void onMusicPlayerError(String what) {

    }
}
