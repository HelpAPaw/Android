package org.helpapaw.helpapaw.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import org.helpapaw.helpapaw.R;

public class SharingUtils {

    public static void contactSupport(Activity activity) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", activity.getString(R.string.string_support_email), null));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Intent chooserIntent = Intent.createChooser(emailIntent, activity.getString(R.string.string_support_email));
        activity.startActivity(chooserIntent);
    }
}
