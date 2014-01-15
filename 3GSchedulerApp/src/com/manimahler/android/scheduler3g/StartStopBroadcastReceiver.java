package com.manimahler.android.scheduler3g;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class StartStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			String action = intent.getAction();

			Log.d("StartStopBroadcastReceiver", "Received broad cast action "
					+ action);

			Bundle bundle = intent.getExtras();

			// no == for string comparison in Java!
			if (action.equals("OFF")) {
				trySwitchOffMobileData(context, bundle.getLong("StopAt"), false);
			} else if (action.equals("OFF_DELAYED")) {
				trySwitchOffMobileData(context, bundle.getLong("StopAt"), true);
			} else {
				
				boolean on = bundle.getBoolean("Action3gOn");

				if (!on) {

					scheduleNotifiedSwitchOff(context, 30);
				} else {
					toggleMobileData(context, on);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Toast.makeText(context, "Error changing 3g setting",
					Toast.LENGTH_SHORT).show();
		}
	}

	private void scheduleNotifiedSwitchOff(Context context, int seconds) {

		AlarmHandler ah = new AlarmHandler();

		ah.makeDataDisableNotification(context, false);

		ah.scheduleSwitchOff(context, seconds, "OFF");
	}

	private void trySwitchOffMobileData(Context context, long expectedStopTime,
			boolean reWarn) {
		AlarmHandler ah = new AlarmHandler();
		SharedPreferences sharedPrefs = ah.GetPreferences(context);

		ScheduleSettings currentSettings = new ScheduleSettings(sharedPrefs);
		
		if (currentSettings.get_endTimeMillis() != expectedStopTime) {
			Log.d("SwitchOff", "Expected stop time has changed. Not stopping.");

			return;
		}

		if (!currentSettings.is_schedulingEnabled()) {
			Log.d("SwitchOff", "Scheduling was disabled. Not stopping");

			return;
		}

		try {
			if (reWarn) {
				scheduleNotifiedSwitchOff(context, 30);
			} else {
				toggleMobileData(context, false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Toast.makeText(context, "Error changing 3g setting",
					Toast.LENGTH_SHORT).show();
		}

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.cancel(17);
	}

	// / found on the Internet and adapted slightly
	private void toggleMobileData(Context context, boolean enable)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		
		final ConnectivityManager conman = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final Class<?> conmanClass = Class.forName(conman.getClass().getName());
		final Field iConnectivityManagerField = conmanClass
				.getDeclaredField("mService");
		iConnectivityManagerField.setAccessible(true);
		final Object iConnectivityManager = iConnectivityManagerField
				.get(conman);
		final Class<?> iConnectivityManagerClass = Class
				.forName(iConnectivityManager.getClass().getName());
		final Method setMobileDataEnabledMethod = iConnectivityManagerClass
				.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);

		Log.d("StartStopBroadcastReceiver",
				"Switching mobile data ON status to " + enable);

		setMobileDataEnabledMethod.setAccessible(true);
		setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
	}

	private void Stop3g(long expectedStartMillis, Context context) {
		// re-check if there was a change
		AlarmHandler ah = new AlarmHandler();
		SharedPreferences sharedPrefs = ah.GetPreferences(context);

		ScheduleSettings currentSettings = new ScheduleSettings(sharedPrefs);
	}

	private void makeDataEnableToast(Context context, boolean enable,
			TelephonyManager telephonyManager) {
		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
			if (enable) {
				Toast.makeText(
						context,
						"3G Mobile Data Scheduler: Data access over mobile network is alredy enabled",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(
						context,
						"3G Mobile Data Scheduler: Switching off data access over mobile network",
						Toast.LENGTH_LONG).show();
			}
		} else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
			if (enable) {
				Toast.makeText(
						context,
						"3G Mobile Data Scheduler: Switching on data access over mobile network",
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(
						context,
						"3G Mobile Data Scheduler: Data access over mobile network is alredy disabled",
						Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(
					context,
					"3G Mobile Data Scheduler: Unexpected state of mobile data. Please report.",
					Toast.LENGTH_LONG).show();
		}
	}

}
