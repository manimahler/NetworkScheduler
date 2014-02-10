package com.manimahler.android.scheduler3g;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class StartStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			String action = intent.getAction();

			Bundle bundle = intent.getExtras();
			long stopTime = bundle.getLong("StopAt");

			// TODO: magic number for default
			int periodId = bundle.getInt(context.getString(R.string.period_id),
					-2);

			Log.d("StartStopBroadcastReceiver", "Received broadcast action "
					+ action + " for period id " + periodId);

			// do not use == for string comparison in Java!
			if (action.equals("OFF")) {
				trySwitchOffMobileData(context, periodId, stopTime, false);
			} else if (action.equals("OFF_DELAYED")) {
				trySwitchOffMobileData(context, periodId, stopTime, true);
			} else {

				// normal schedule: test weekday
				NetworkScheduler scheduler = new NetworkScheduler();
				SharedPreferences sharedPrefs = scheduler.GetPreferences(context);

				EnabledPeriod referencedPeriod = PersistenceUtils.getPeriod(
						sharedPrefs, periodId);
				
				boolean on = bundle.getBoolean(context
						.getString(R.string.action_3g_on));
				
				if (! appliesToday(referencedPeriod, on))
				{
					Log.d("StartStopBroadcastReceiver", "action does not apply today ");
					return;
				}
				
				if (!on) {
					scheduleNotifiedSwitchOff(context, 30, referencedPeriod);
				} else {
					ConnectionUtils.toggleNetworkState(context, referencedPeriod, on);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Toast.makeText(context, "Error changing 3g setting",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean appliesToday(EnabledPeriod referencedPeriod, boolean enable) throws Exception {
		
		// do not use todays time but the official end time because the
		// broadcast might arrive late (esp. with inexact repeating on kitkat)
		
		long alarmTime;
		
		Log.d("StartStopReceiver", "enable: " + enable);
		
		if (enable)
		{
			alarmTime = referencedPeriod.get_startTimeMillis();
		}
		else
		{
			alarmTime = referencedPeriod.get_endTimeMillis();
		}
		
		long actualAlarmTime = DateTimeUtils.getPreviousTimeIn24hInMillis(alarmTime);
		
		Log.d("StartStopReceiver", "actualAlarmTime: " + actualAlarmTime);
		
		int weekdayIndex = DateTimeUtils.getWeekdayIndex(actualAlarmTime);
		
		Log.d("StartStopReceiver", "weekdayIndex: " + weekdayIndex);
		
		return (referencedPeriod.get_weekDays()[weekdayIndex]);
	}

	private void scheduleNotifiedSwitchOff(Context context, int seconds,
			EnabledPeriod period) {
		
		NetworkScheduler scheduler = new NetworkScheduler();

		scheduler.makeDisableNotification(context, period);

		scheduler.scheduleSwitchOff(context, seconds, "OFF", period);
	}

	private void trySwitchOffMobileData(Context context, int periodId,
			long expectedStopTime, boolean reWarn) {

		NetworkScheduler scheduler = new NetworkScheduler();
		SharedPreferences sharedPrefs = scheduler.GetPreferences(context);

		EnabledPeriod referencedPeriod = PersistenceUtils.getPeriod(
				sharedPrefs, periodId);

		if (referencedPeriod == null) {
			// it might have been deleted? Test!
			Log.d("SwitchOff", "Referenced period not found. Not stopping.");

			return;
		}

		if (referencedPeriod.get_endTimeMillis() != expectedStopTime) {
			Log.d("SwitchOff", "Expected stop time has changed. Not stopping.");

			return;
		}

		if (!referencedPeriod.is_schedulingEnabled()) {
			Log.d("SwitchOff", "Scheduling was disabled. Not stopping");

			return;
		}

		// TODO: check sensors

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		try {
			if (reWarn) {
				// add notification
				scheduler.makeDisableNotification(context, referencedPeriod);
				scheduler.scheduleSwitchOff(context, 30, "OFF",
						referencedPeriod);
			} else {
				// before switching off, remove notification

				notificationManager.cancel(periodId);
				ConnectionUtils.toggleNetworkState(context, referencedPeriod, false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Toast.makeText(context, "Error changing 3g setting",
					Toast.LENGTH_SHORT).show();
		}
	}

	//
	// private void trySwitchOffMobileData(Context context, long
	// expectedStopTime,
	// boolean reWarn) {
	//
	// NetworkScheduler scheduler = new NetworkScheduler();
	// SharedPreferences sharedPrefs = scheduler.GetPreferences(context);
	//
	// ScheduleSettings currentSettings = new ScheduleSettings(sharedPrefs);
	//
	// if (currentSettings.get_endTimeMillis() != expectedStopTime) {
	// Log.d("SwitchOff", "Expected stop time has changed. Not stopping.");
	//
	// return;
	// }
	//
	// if (!currentSettings.is_schedulingEnabled()) {
	// Log.d("SwitchOff", "Scheduling was disabled. Not stopping");
	//
	// return;
	// }
	//
	// try {
	// if (reWarn) {
	// scheduleNotifiedSwitchOff(context, 30, );
	// } else {
	// toggleMobileData(context, false);
	// }
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	//
	// Toast.makeText(context, "Error changing 3g setting",
	// Toast.LENGTH_SHORT).show();
	// }
	//
	// NotificationManager notificationManager = (NotificationManager) context
	// .getSystemService(Context.NOTIFICATION_SERVICE);
	//
	// notificationManager.cancel(17);
	// }

//	private void toggleNetworkState(Context context, int periodId,
//			boolean enable) throws ClassNotFoundException,
//			NoSuchFieldException, IllegalArgumentException,
//			IllegalAccessException, NoSuchMethodException,
//			InvocationTargetException {
//
//		NetworkScheduler scheduler = new NetworkScheduler();
//		SharedPreferences sharedPrefs = scheduler.GetPreferences(context);
//
//		EnabledPeriod referencedPeriod = PersistenceUtils.getPeriod(
//				sharedPrefs, periodId);
//		toggleNetworkState(context, referencedPeriod, enable);
//	}

//
//	private void Stop3g(long expectedStartMillis, Context context) {
//		// re-check if there was a change
//		NetworkScheduler ah = new NetworkScheduler();
//		SharedPreferences sharedPrefs = ah.GetPreferences(context);
//
//		ScheduleSettings currentSettings = new ScheduleSettings(sharedPrefs);
//	}
//
//	private void makeDataEnableToast(Context context, boolean enable,
//			TelephonyManager telephonyManager) {
//		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
//			if (enable) {
//				Toast.makeText(
//						context,
//						"3G Mobile Data Scheduler: Data access over mobile network is alredy enabled",
//						Toast.LENGTH_LONG).show();
//			} else {
//				Toast.makeText(
//						context,
//						"3G Mobile Data Scheduler: Switching off data access over mobile network",
//						Toast.LENGTH_LONG).show();
//			}
//		} else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
//			if (enable) {
//				Toast.makeText(
//						context,
//						"3G Mobile Data Scheduler: Switching on data access over mobile network",
//						Toast.LENGTH_LONG).show();
//			} else {
//				Toast.makeText(
//						context,
//						"3G Mobile Data Scheduler: Data access over mobile network is alredy disabled",
//						Toast.LENGTH_LONG).show();
//			}
//		} else {
//			Toast.makeText(
//					context,
//					"3G Mobile Data Scheduler: Unexpected state of mobile data. Please report.",
//					Toast.LENGTH_LONG).show();
//		}
//	}

}
