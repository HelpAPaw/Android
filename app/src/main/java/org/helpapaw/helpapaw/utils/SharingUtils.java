package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import org.apache.commons.io.FileUtils;
import org.helpapaw.helpapaw.R;

import java.io.File;

/**
 * Created by Emil Ivanov on 10/15/2016.
 */

public class SharingUtils {
    public static final String PACKAGE_FACEBOOK = "com.facebook.katana";

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

}
