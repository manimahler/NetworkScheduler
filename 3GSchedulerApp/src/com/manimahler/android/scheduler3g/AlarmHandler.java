package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

public class AlarmHandler {

	public SharedPreferences GetPreferences(Context context) {
		return context.getSharedPreferences("SCHEDULER_PREFS", Context.MODE_PRIVATE);
	}
	
	public void setAlarm(Context context, ScheduleSettings settings) {
		
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		
		PendingIntent pendingIntentOn = getPendingIntent(context, true);
		PendingIntent pendingIntentOff = getPendingIntent(context, false);
		
		if (settings.is_schedulingEnabled()) {
			
			long interval24h = 24 * 60 * 60 * 1000;
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(settings.get_startTimeMillis());
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(settings.get_endTimeMillis());
			//long window = 1 * 60 * 1000;
			
			//am.setWindow(AlarmManager.RTC_WAKEUP, startMillis, window, pendingIntentOn);
			//am.setWindow(AlarmManager.RTC_WAKEUP, stopMillis, window, pendingIntentOff);
			am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis, interval24h, pendingIntentOn);
			am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h, pendingIntentOff);
		} else {
			// cancel
			am.cancel(pendingIntentOn);
			am.cancel(pendingIntentOff);
		}
	}
	
	public void makeDataDisableNotification(Context context, boolean enable) {

		TelephonyManager telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (!needsAction(enable, telephonyManager)) {
			Log.d("AlarmHandler", "No action needed");
			// return;
		}

		String tickerText = getTickerText(enable, telephonyManager);

		Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_launcher);

		Intent delayIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		delayIntent.setAction("DELAY");

		PendingIntent pendingIntentDelay = PendingIntent.getBroadcast(context,
				0, delayIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.clock_notification)
				.setLargeIcon(bm)
				.setContentTitle("Switching OFF 3G")
				.setContentText(
						"3G mobile data will be switched off in a few seconds")
				.setTicker(tickerText)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.addAction(R.drawable.clock_notification, "Delay by 1 hour",
						pendingIntentDelay)
				.setVibrate(new long[] { -1, 800, 1000 }) // if watching full
															// screen video, the
															// ticker is not
															// shown!
				.setAutoCancel(true);

		// Creates an Intent for the Activity

		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent notifyIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Puts the PendingIntent into the notification builder
		builder.setContentIntent(notifyIntent);

		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(17, builder.build());
	}

	private String getTickerText(boolean enable3g,
			TelephonyManager telephonyManager) {
		String tickerText;

		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
			if (enable3g) {
				tickerText = "3G/4G: Data access alredy enabled";
			} else {
				tickerText = "3G/4G: Data access OFF in 30s";
			}
		} else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
			if (enable3g) {
				tickerText = "3G/4G: Data access ON in 30s";
			} else {
				tickerText = "3G/4G: Data access alredy disabled";
			}
		} else {
			tickerText = "";
		}
		return tickerText;
	}

	private boolean needsAction(boolean enable3g,
			TelephonyManager telephonyManager) {

		boolean result;

		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
			if (enable3g) {
				result = false;
			} else {
				result = true;
			}
		} else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
			if (enable3g) {
				result = true;
			} else {
				result = false;
			}
		} else {
			result = true;
		}
		return result;
	}

	public void cancelSwitchOff(Context context, String actionName) {
		try {
			AlarmManager am = (AlarmManager) context
					.getSystemService(android.content.Context.ALARM_SERVICE);

			PendingIntent pendingIntentOff = getSwitchOffIntent(context, null,
					actionName);

			am.cancel(pendingIntentOff);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName) {

		SharedPreferences sharedPrefs = GetPreferences(context);

		ScheduleSettings currentSettings = new ScheduleSettings(sharedPrefs);

		setAlarmWarningPeriod(context, false, currentSettings, seconds,
				actionName);
	}

	public void setAlarmWarningPeriod(Context context, boolean on,
			ScheduleSettings currentSettings, int warningPeriodSeconds,
			String actionName) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		PendingIntent pendingIntentOff = getSwitchOffIntent(context,
				currentSettings, actionName);

		int delayMillis = warningPeriodSeconds * 1000;

		long wakeTime = SystemClock.elapsedRealtime() + delayMillis;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(wakeTime);

		Log.d("AlarmHandler", "Setting warning alarm with intent " + actionName
				+ " for " + calendar.getTime().toString());

		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeTime, pendingIntentOff);
	}

	private PendingIntent getPendingIntent(Context context, boolean enable3g) {

		String action;

		if (enable3g) {
			action = "Enabling 3G";
		} else {
			action = "Disabling 3G";
		}

		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		// to differentiate the intents, otherwise they update each other! NOTE:
		// Extras are not enough!
		intent.setAction(action);

		Bundle bundle = new Bundle();
		bundle.putBoolean("Action3gOn", enable3g);

		intent.putExtras(bundle);

		// using service because getActivity needs API level 16
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		return pendingIntent;
	}

	private PendingIntent getSwitchOffIntent(Context context,
			ScheduleSettings currentSettings, String actionName) {

		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		intent.setAction(actionName);

		Bundle bundle = new Bundle();
		bundle.putBoolean("Action3gOn", false);

		if (currentSettings != null) {
			bundle.putLong("StopAt", currentSettings.get_endTimeMillis());
		}

		intent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,
				intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		return pendingIntent;
	}
}
