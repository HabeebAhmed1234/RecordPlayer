package com.cromiumapps.musicplayer.palette;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.cromiumapps.musicplayer.MusicPlayerApplication;
import com.cromiumapps.musicplayer.R;
import com.cromiumapps.musicplayer.preferences.UserInfo;
import com.cromiumapps.musicplayer.utils.ColorUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by hahmed on 8/13/2015.
 */
public class PaletteColorManager {
    public interface PaletteColorChangedListener {
        public void onPaletteColorChanged(Palette newPalette);
    }

    public interface ColorTransitionAnimationListener {
        public void onColorAnimate(int color);
    }

    private static final int COLOR_FADE_ANIMATION_DURATION_MS = 150;

    private Context mContext = null;
    private Map<Uri, Palette> mPaletteCache = new HashMap<Uri, Palette>();
    private Palette mCurrentPalette;
    private ArrayList<PaletteColorChangedListener> mListeners = new ArrayList<PaletteColorChangedListener>();
    private ArrayList<WeakReference<View>> mWeakReferenceViews = new ArrayList<WeakReference<View>>();
    private AsyncTask<Uri, Void, Palette> mFetchNewPaletteTask;

    public PaletteColorManager(Context context) {
        mContext = context;
    }

    public Palette getCurrentPalette() {
        return mCurrentPalette;
    }

    public void addListener(PaletteColorChangedListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<PaletteColorChangedListener>();
        }
        mListeners.add(listener);

        if (mCurrentPalette != null) {
            listener.onPaletteColorChanged(mCurrentPalette);
        }
    }

    public void removeListener(PaletteColorChangedListener listener) {
        if (mListeners != null) {
            mListeners.remove(listener);
        }
    }

    public void addView(View view) {
        if (mWeakReferenceViews == null) {
            mWeakReferenceViews = new ArrayList<WeakReference<View>>();
        }
        mWeakReferenceViews.add(new WeakReference<View>(view));

        if (mCurrentPalette != null) {
            setNewBackgroundColor(view, mCurrentPalette, true);
        }
    }

    public void addView(TextView textView) {

    }

    public void removeView(View view) {
        if (mWeakReferenceViews != null) {
            Iterator<WeakReference<View>> iterator = mWeakReferenceViews.iterator();
            while (iterator.hasNext()) {
                WeakReference<View> weakReference = iterator.next();
                View strongView = weakReference.get();
                if (strongView == view) {
                    iterator.remove();
                }
            }
        }
    }

    public void setPaletteColor(final Uri imageUri) {
        if (mFetchNewPaletteTask != null) {
            mFetchNewPaletteTask.cancel(true);
        }

        Palette cachedPalette = mPaletteCache.get(imageUri);
        if (cachedPalette != null) {
            onNewPalette(cachedPalette);
        } else {
            mFetchNewPaletteTask = new FetchNewPaletteTask();
            mFetchNewPaletteTask.execute(imageUri);
        }
    }

    private void notifyNewPalette(Palette newPalette) {
        if (mListeners != null) {
            for (PaletteColorChangedListener listener : mListeners) {
                listener.onPaletteColorChanged(newPalette);
            }
        }
    }

    public void setNewColor(final ColorTransitionAnimationListener callback, int fromColor, int toColor) {
        if (fromColor == toColor) return;
        ValueAnimator colorFade = ValueAnimator.ofObject(new ColorUtils.UnsaturatedRgbInHslEvaluator(), fromColor, toColor);
        colorFade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                callback.onColorAnimate((int) animation.getAnimatedValue());
            }
        });
        colorFade.setInterpolator(new AccelerateInterpolator());
        colorFade.setDuration(COLOR_FADE_ANIMATION_DURATION_MS);
        colorFade.start();
    }

    private void onNewPalette(Palette palette) {
        mCurrentPalette = palette;
        notifyNewPalette(mCurrentPalette);

        if (mWeakReferenceViews != null) {
            Iterator<WeakReference<View>> iterator = mWeakReferenceViews.iterator();
            while (iterator.hasNext()) {
                WeakReference<View> weakReference = iterator.next();
                View paletteView = weakReference.get();
                if (paletteView == null) {
                    iterator.remove();
                } else {
                    setNewBackgroundColor(paletteView, mCurrentPalette, true);
                }
            }
        }
    }

    private class TaskInfo {
        public Uri uri;

        public TaskInfo(Uri uri) {
            this.uri = uri;
        }
    }

    private class FetchNewPaletteTask extends AsyncTask<Uri, Void, Palette> {
        private Uri mUri;

        @Override
        protected Palette doInBackground(Uri... params) {
            mUri = params[0];
            return getPaletteSync(mContext, mUri);
        }

        @Override
        protected void onPostExecute(Palette palette) {
            super.onPostExecute(palette);
            onNewPalette(palette);
        }
    }

    public static Palette getPaletteSync(Context context, Uri uri) {
        Palette palette = null;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            if (bitmap != null) {
                palette = getPaletteSync(bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return palette;
    }

    public static Palette getPaletteSync(Bitmap bitmap) {
        return Palette.generate(bitmap);
    }

    public static void setNewBackgroundColor(final View view, final Palette toPalette, final boolean animate) {
        final int toColor = applyViewConfig(extractPaletteColour(view.getContext(), toPalette), view);
        int fromColor = ColorUtils.getBackgroundColor(view);
        if (fromColor == toColor) return;
        if (animate) {
            ValueAnimator colorFade = ValueAnimator.ofObject(new ColorUtils.UnsaturatedRgbInHslEvaluator(), fromColor, toColor);
            colorFade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    view.setBackgroundColor((int) animation.getAnimatedValue());
                }
            });
            colorFade.setInterpolator(new AccelerateInterpolator());
            colorFade.setDuration(COLOR_FADE_ANIMATION_DURATION_MS);
            colorFade.start();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    view.setBackgroundColor(toColor);
                }
            });
        }
    }

    /**
     * Takes the base color and applies and configs stored in the view's tag
     * @param baseColor
     * @param view
     * @return
     */
    private static int applyViewConfig(int baseColor, View view) {
        Object alphaConfig = view.getTag(R.id.VIEW_CONFIG_ALPHA);
        if(alphaConfig != null) {
            return adjustAlpha(baseColor, (float) alphaConfig);
        }

        return baseColor;
    }

    private static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * use this as a universal shade extractor
     * @param context
     * @param palette
     * @return
     */
    public static int extractPaletteColour(Context context, Palette palette) {
        if (palette == null) {
            return context.getResources().getColor(R.color.app_quiet_color);
        }
        String colourQuality = new UserInfo(context).getColourQuality();
        if(colourQuality.equals(UserInfo.COLOUR_QUALITY_PREF_VALUE_BRIGHT)) {
            return palette.getVibrantColor(context.getResources().getColor(R.color.app_quiet_color));
        }
        return palette.getMutedColor(context.getResources().getColor(R.color.app_quiet_color));
    }

    public static void setViewConfigAlpha(View view, float alpha) {
        view.setTag(R.id.VIEW_CONFIG_ALPHA, alpha);
    }
}
