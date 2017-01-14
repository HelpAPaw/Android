package org.helpapaw.helpapaw.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ShareCompat;

/**
 * Created by Emil Ivanov on 10/15/2016.
 */

public class SharingUtils {
    public static final String PACKAGE_FACEBOOK = "com.facebook.katana";


    public static final String EMAIL = "emil.iv.ivanov@gmail.com";
    public static final String SUBJECT ="[Help A Paw] Feedback";
    public static final String CHOOSER_TITLE = "Send Feedback";
    /**
     * Using default sharing implemetation for Facebook
     * @param context
     * @param message
     * @return
     */
    public static Intent shareFacebook(Context context, String message) {


        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.setPackage(PACKAGE_FACEBOOK);

//        File file = FileUtils.createTempImageFile(imagePath);
//        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

//       MessageDialog.show((Activity) context,shareContent);

        return shareIntent;
    }
    public static boolean verifyPackageInstalled(Context context, String appPackage) {

        boolean installed = false;

        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(appPackage, 0);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    public static Intent sendFeedback(){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto",EMAIL, null));
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,SUBJECT );

        return Intent.createChooser(intent, "Send Feedback");
    }

    public static void sendFeedbackUsingCompat(Activity activity){
        ShareCompat.IntentBuilder.from(activity)
                .setType("message/rfc822")
                .addEmailTo(EMAIL)
                .setSubject(SUBJECT)
//                .setText(body)
                //.setHtmlText(body) //If you are using HTML in your body text
                .setChooserTitle(CHOOSER_TITLE)
                .startChooser();

    }
}
