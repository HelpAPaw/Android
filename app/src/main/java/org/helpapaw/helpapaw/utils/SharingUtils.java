package org.helpapaw.helpapaw.utils;

import android.app.Activity;
import android.support.v4.app.ShareCompat;

/**
 * Created by Emil Ivanov on 10/15/2016.
 */

public class SharingUtils {

    public static final String EMAIL = "help.a.paw.app@gmail.com";
    public static final String SUBJECT ="[Help A Paw] Feedback";
    public static final String CHOOSER_TITLE = "Send Feedback";

    public static void sendFeedbackUsingCompat(Activity activity) {
        ShareCompat.IntentBuilder.from(activity)
                .setType("message/rfc822")
                .addEmailTo(EMAIL)
                .setSubject(SUBJECT)
                .setChooserTitle(CHOOSER_TITLE)
                .startChooser();
    }
}
