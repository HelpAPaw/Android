package org.helpapaw.helpapaw.utils.images;

import android.content.Context;
import androidx.annotation.DrawableRes;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by iliyan on 8/2/16
 */
public class PicassoImageLoader implements ImageLoader {
    @Override
    public void load(Context context, String url, ImageView imageView, @DrawableRes int error) {
        Picasso.get()
                .load(url)
                .error(error)
                .fit()
                .centerInside()
                .into(imageView);
    }

    @Override
    public void loadWithRoundedCorners(Context context, String url, ImageView imageView, @DrawableRes int placeholder) {
        Picasso.get()
                .load(url)
                .placeholder(placeholder)
                .transform(new RoundedTransformation(16, 0))
                .fit()
                .centerCrop()
                .into(imageView);
    }

}
