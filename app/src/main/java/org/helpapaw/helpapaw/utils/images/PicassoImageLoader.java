package org.helpapaw.helpapaw.utils.images;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by iliyan on 8/2/16
 */
public class PicassoImageLoader implements ImageLoader {
    @Override
    public void load(Context context, String url, ImageView imageView, @DrawableRes int placeholder) {
        Picasso.with(context)
                .load(url)
                .placeholder(placeholder)
                .fit()
                .centerCrop()
                .into(imageView);
    }

}
