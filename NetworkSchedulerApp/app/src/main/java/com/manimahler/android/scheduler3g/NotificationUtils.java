package com.manimahler.android.scheduler3g;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class NotificationUtils {
    private static final String TAG = NotificationUtils.class.getSimpleName();

    private static final String CHANNEL_ID_SWITCH_OFF = "CHANNEL_SWITCH_OFF_REMINDER";
    private static final String CHANNEL_ID_AUTO_DELAY = "CHANNEL_AUTO_DELAY_NOTIFICATION";

    public static String createNotificationChannelSwitchOff(
            Context context) {

        Log.d(TAG, "createNotificationChannelSwitchOff: Creating channel switch-off");

        // NotificationChannels are required for Notifications on O (API 26) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // The id of the channel.
            String channelId = CHANNEL_ID_SWITCH_OFF;

            // The user-visible name of the channel.
            CharSequence channelName = context.getResources().getString(
                    R.string.notification_channel_name_switchoff);

            int channelImportance = NotificationManager.IMPORTANCE_HIGH;

            // Initializes NotificationChannel.
            InitializeChannel(context, channelId, channelName, null,
                    channelImportance, true);

            return channelId;
        } else {
            // Returns null for pre-O (26) devices.
            return null;
        }
    }

    public static String createNotificationChannelAutoDelay(
            Context context) {

        Log.d(TAG, "createNotificationChannelSwitchOff: Creating channel auto-delay");

        // NotificationChannels are required for Notifications on O (API 26) and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // The id of the channel.
            String channelId = CHANNEL_ID_AUTO_DELAY;

            // The user-visible name of the channel.
            CharSequence channelName = context.getResources().getString(
                    R.string.notification_channel_name_autodelay);

            int channelImportance = NotificationManager.IMPORTANCE_DEFAULT;

            // Initializes NotificationChannel.
            InitializeChannel(context, channelId, channelName, null,
                    channelImportance, false);

            return channelId;
        } else {
            // Returns null for pre-O (26) devices.
            return null;
        }
    }

    private static void InitializeChannel(Context context,
                                          String channelId,
                                          CharSequence channelName,
                                          String channelDescription,
                                          int channelImportance,
                                          boolean channelEnableVibrate) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationChannel notificationChannel =
                new NotificationChannel(channelId, channelName, channelImportance);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableVibration(channelEnableVibrate);

        // Adds NotificationChannel to system. Attempting to create an existing notification
        // channel with its original values performs no operation, so it's safe to perform the
        // below sequence.
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
    }
}
