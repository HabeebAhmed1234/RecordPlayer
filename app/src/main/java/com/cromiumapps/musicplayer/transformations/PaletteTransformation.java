package com.cromiumapps.musicplayer.transformations;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import com.cromiumapps.musicplayer.palette.PaletteColorManager;
import com.squareup.picasso.Transformation;

/**
 * Created by habeebahmed on 2/27/16.
 */
public class PaletteTransformation implements Transformation {
    public interface PaletteLoadedListener {
        void onPaletteLoaded(Palette palette);
    }

    private PaletteLoadedListener mListener;

    public PaletteTransformation(PaletteLoadedListener listener) {
        mListener = listener;
    }

    @Override
    public Bitmap transform(Bitmap bitmap) {
        Palette palette = PaletteColorManager.getPaletteSync(bitmap);
        mListener.onPaletteLoaded(palette);
        return bitmap;
    }

    @Override
    public String key() {
        return "PaletteTransformation";
    }
}
