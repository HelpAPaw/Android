package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by iliyan on 7/29/16
 */
public class GlideImageLoader implements ImageLoader{
    @Override
    public void load(Context context, String url, ImageView imageView) {
        Glide.with(context).load(url).into(imageView);
    }
}
