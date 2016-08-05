package org.helpapaw.helpapaw.utils.images;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

/**
 * Created by iliyan on 7/29/16
 */
public interface ImageLoader {

    void load(Context context, String url, ImageView imageView, @DrawableRes int placeholder);

    void loadWithRoundedCorners(Context context, String url, ImageView imageView, @DrawableRes int placeholder);

}
