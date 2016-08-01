package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by iliyan on 7/29/16
 */
public class GlideImageLoader implements ImageLoader {
    @Override
    public void load(Context context, String url, ImageView imageView) {
        Glide.with(context).load(url).into(imageView);
    }

    @Override
    public void load(Context context, Uri uri, ImageView imageView) {
        Glide.with(context)
                .load(new File(uri.getPath())).override(100, 100).centerCrop().into(imageView);
    }
}
