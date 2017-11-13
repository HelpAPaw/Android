package org.helpapaw.helpapaw.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity;

import static org.helpapaw.helpapaw.data.models.Signal.SOMEBODY_ON_THE_WAY;

/**
 * Created by milen on 04/11/17.
 * Centralized place to deal with notifications management
 */

public class NotificationUtils {

    private static final String channel_id_help_needed = "channel_id_help_needed";
    private static final String channel_id_somebody_on_the_way = "channel_id_somebody_on_the_way";

    public static void registerNotificationChannels(Context context) {

        // Notification channels are supported from Android 8.0 on
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager == null) return;

        // Channel 'Help needed'
        CharSequence name = context.getString(R.string.txt_help_needed);
        String description = context.getString(R.string.txt_channel_description_help_needed);
        int importance  = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(channel_id_help_needed, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.enableVibration(true);
        mNotificationManager.createNotificationChannel(mChannel);

        // Channel 'Somebody on the way'
        name = context.getString(R.string.txt_somebody_on_the_way);
        description = context.getString(R.string.txt_channel_description_somebody_on_the_way);
        importance  = NotificationManager.IMPORTANCE_DEFAULT;
        mChannel = new NotificationChannel(channel_id_somebody_on_the_way, name, importance);
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        mChannel.enableVibration(false);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    public static void showNotificationForSignal(Signal signal, Context context) {

        Integer signalCode = signal.getId().hashCode();

        String status = "Status: ";
        String channel_id;
        if (signal.getStatus() == SOMEBODY_ON_THE_WAY) {
            status += context.getString(R.string.txt_somebody_is_on_the_way);
            channel_id = channel_id_somebody_on_the_way;
        }
        else {
            status += context.getString(R.string.txt_you_help_is_needed);
            channel_id = channel_id_help_needed;
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channel_id);
        mBuilder.setSmallIcon(R.drawable.ic_paw_notif);
        mBuilder.setTicker(context.getString(R.string.txt_new_signal));
        mBuilder.setContentTitle(signal.getTitle());
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setWhen(signal.getDateSubmitted().getTime());
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);

        Bitmap pin       = BitmapFactory.decodeResource(context.getResources(), StatusUtils.getPinResourceForCode(signal.getStatus()));
        Bitmap largeIcon = scaleBitmapForLargeIcon(pin, context);

        mBuilder.setContentText(status);
        mBuilder.setLargeIcon(largeIcon);

        Intent resultIntent = new Intent(context, SignalsMapActivity.class);
        resultIntent.putExtra(Signal.KEY_FOCUSED_SIGNAL_ID, signal.getId());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(signalCode, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (mNotificationManager != null) {
            mNotificationManager.notify(signalCode, mBuilder.build());
        }
    }

    private static Bitmap scaleBitmapForLargeIcon(Bitmap bmp, Context context) {
        Resources res    = context.getResources();
        double    ratio  = (double)bmp.getHeight() / (double)bmp.getWidth();
        int       height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int       width  = (int) (height / ratio);

        return addTransparentSideBorder(bmp, height - width);
    }

    //https://stackoverflow.com/a/15525394/2781218
    private static Bitmap addTransparentSideBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize, bmp.getHeight(), bmp.getConfig());
        Canvas canvas        = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(bmp, borderSize / 2, 0, null);
        return bmpWithBorder;
    }
}
