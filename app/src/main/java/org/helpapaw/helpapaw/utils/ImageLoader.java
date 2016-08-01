package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

/**
 * Created by iliyan on 7/29/16
 */
public interface ImageLoader {
    void load(Context context, String url, ImageView imageView);

    void load(Context context, Uri uri, ImageView imageView);
}
