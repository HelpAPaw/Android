package org.helpapaw.helpapaw.utils;

import static android.content.Context.MODE_PRIVATE;
import static org.helpapaw.helpapaw.base.PawApplication.APP_OPENINGS_TO_ASK_FOR_SHARE;
import static org.helpapaw.helpapaw.base.PawApplication.APP_OPEN_COUNTER;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    public static void shareApp(Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT,
                String.format(context.getString(R.string.string_share_app_text),
                        context.getString(R.string.string_share_link)));
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public static void resetCounter(Context context, int counterResetValue) {
        SharedPreferences prefs = context.getSharedPreferences("HelpAPaw", MODE_PRIVATE);

        prefs.edit().putInt(APP_OPEN_COUNTER, counterResetValue).commit();
    }
}
