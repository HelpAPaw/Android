package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.helpapaw.helpapaw.R;

public class SharingUtils {

    public static void contactSupport(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", context.getString(R.string.string_support_email), null));
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        Intent chooserIntent = Intent.createChooser(emailIntent, context.getString(R.string.string_support_email));
        context.startActivity(chooserIntent);
    }
}
