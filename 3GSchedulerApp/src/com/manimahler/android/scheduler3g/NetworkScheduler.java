package com.manimahler.android.scheduler3g;

import java.text.MessageFormat;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkScheduler {

	public SharedPreferences GetPreferences(Context context) {
		return context.getSharedPreferences("SCHEDULER_PREFS",
				Context.MODE_PRIVATE);
	}

	public void setAlarm(Context context, EnabledPeriod period) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent pendingIntentOn = getPendingIntent(context, period, true);
		PendingIntent pendingIntentOff = getPendingIntent(context, period,
				false);

		if (period.is_schedulingEnabled()) {

			long interval24h = 24 * 60 * 60 * 1000;
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_startTimeMillis());
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_endTimeMillis());
			// long window = 1 * 60 * 1000;

			// am.setWindow(AlarmManager.RTC_WAKEUP, startMillis, window,
			// pendingIntentOn);
			// am.setWindow(AlarmManager.RTC_WAKEUP, stopMillis, window,
			// pendingIntentOff);
			am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis, interval24h,
					pendingIntentOn);
			am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h,
					pendingIntentOff);
		} else {
			// cancel
			am.cancel(pendingIntentOn);
			am.cancel(pendingIntentOff);
		}

	}

	public void setAlarm(Context context, ScheduleSettings settings) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent pendingIntentOn = getPendingIntent(context, true);
		PendingIntent pendingIntentOff = getPendingIntent(context, false);

		if (settings.is_schedulingEnabled()) {

			long interval24h = 24 * 60 * 60 * 1000;
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(settings
					.get_startTimeMillis());
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(settings
					.get_endTimeMillis());
			// long window = 1 * 60 * 1000;

			// am.setWindow(AlarmManager.RTC_WAKEUP, startMillis, window,
			// pendingIntentOn);
			// am.setWindow(AlarmManager.RTC_WAKEUP, stopMillis, window,
			// pendingIntentOff);
			am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis, interval24h,
					pendingIntentOn);
			am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h,
					pendingIntentOff);
		} else {
			// cancel
			am.cancel(pendingIntentOn);
			am.cancel(pendingIntentOff);
		}
	}

	public void makeDisableNotification(Context context, EnabledPeriod period) {
		//
		//
		// boolean enable = false;

		// if (!needsAction(enable, telephonyManager)) {
		// Log.d("AlarmHandler", "No action needed");
		// // return;
		// }

		String tickerText = getTickerText(period, context);

		if (tickerText == null) {
			Log.d("AlarmHandler", "No action needed");
			return;
		}

		Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_launcher);

		// to allow 1h delay by user clicking:
		Intent delayIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		// delayIntent.putExtra(context.getString(R.string.period_id),
		// periodId);
		delayIntent.setAction("DELAY");
		delayIntent.putExtra(context.getString(R.string.period_id),
				period.get_id());

		PendingIntent delayIntentPending = PendingIntent.getBroadcast(context,
				0, delayIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.clock_notification)
				.setLargeIcon(bm)
				.setContentTitle("Switching off network")
				.setContentText(tickerText + " in a few moments")
				.setTicker(tickerText)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.addAction(R.drawable.clock_notification, "Delay by 1 hour",
						delayIntentPending)
				.setVibrate(new long[] { -1, 800, 1000 }) // if watching full
															// screen video, the
															// ticker is not
															// shown!
				.setAutoCancel(true);

		Toast.makeText(context,
				"Switching off network connection in a few moments...",
				Toast.LENGTH_LONG).show();

		// Creates an Intent for the Activity
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent notifyIntent = PendingIntent.getActivity(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Puts the PendingIntent into the notification builder
		builder.setContentIntent(notifyIntent);

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// periodId allows updating / cancelling the notification later on
		notificationManager.notify(period.get_id(), builder.build());
	}
//
//	private String getTickerText(boolean enable3g,
//			TelephonyManager telephonyManager) {
//		String tickerText;
//
//		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
//			if (enable3g) {
//				tickerText = "3G/4G: Data access alredy enabled";
//			} else {
//				tickerText = "3G/4G: Data access OFF in 30s";
//			}
//		} else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
//			if (enable3g) {
//				tickerText = "3G/4G: Data access ON in 30s";
//			} else {
//				tickerText = "3G/4G: Data access alredy disabled";
//			}
//		} else {
//			tickerText = "";
//		}
//		return tickerText;
//	}

	private String getTickerText(EnabledPeriod period, Context context) {

		boolean mobileData = false;
		boolean wifi = false;
		boolean bluetooth = false;

		//String sensors = "";

		if (period.is_mobileData()) {

			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
				mobileData = true;
				//sensors = context.getString(R.string.mobile_data);
			}
		}

		if (period.is_wifi()) {

			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			boolean wifiEnabled = wifiManager.isWifiEnabled();

			if (wifiEnabled) {

				wifi = true;

//				if (sensors.length() > 0) {
//					sensors += ", ";
//				}
//
//				sensors += context.getString(R.string.wifi);
			}
		}

		if (period.is_bluetooth()) {
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

			if (adapter != null) {
				if (adapter.getState() != BluetoothAdapter.STATE_ON) {

					bluetooth = true;
					// TODO: differentiate between STATE_ON and STATE_CONNECTED,
					// etc. -> settings to force off despite connected
//					if (sensors.length() > 0) {
//						sensors += ", ";
//					}
//
//					sensors += context.getString(R.string.bluetooth);
				}
			}
		}

		String tickerText = null;

		if (wifi && mobileData && bluetooth) {
			tickerText = context.getString(R.string.switch_off_all);
		} else if (wifi && mobileData) {
			tickerText = context.getString(R.string.switch_off_wifi_mobile);
		} else if (wifi && bluetooth) {
			tickerText = context.getString(R.string.switch_off_wifi_bluetooth);
		} else if (mobileData && bluetooth) {
			tickerText = context
					.getString(R.string.switch_off_mobile_bluetooth);
		} else if (wifi) {
			tickerText = context.getString(R.string.switch_off_wifi);
		} else if (mobileData) {
			tickerText = context.getString(R.string.switch_off_mobile);
		} else if (bluetooth) {
			tickerText = context.getString(R.string.switch_off_bluetooth);
		}

		return tickerText;

		// if (sensors.length() > 0) {
		// MessageFormat mf = new MessageFormat(
		// MyResources.rb.getString("DeleteBefore"));
		// myCheckbox = new Checkbox(mf.format(new Object[] { someDate }));
		// return "Switching off in a few moments: " + sensors;
		// } else {
		// return null;
		// }
	}
//
//	private boolean needsAction(boolean enable3g,
//			TelephonyManager telephonyManager) {
//
//		boolean result;
//
//		if (telephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED) {
//			if (enable3g) {
//				result = false;
//			} else {
//				result = true;
//			}
//		} else if (telephonyManager.getDataState() == TelephonyManager.DATA_DISCONNECTED) {
//			if (enable3g) {
//				result = true;
//			} else {
//				result = false;
//			}
//		} else {
//			result = true;
//		}
//		return result;
//	}

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
			String actionName, int periodId) {

		SharedPreferences sharedPrefs = GetPreferences(context);

		EnabledPeriod period = PersistenceUtils
				.getPeriod(sharedPrefs, periodId);

		scheduleSwitchOff(context, seconds, actionName, period);
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, EnabledPeriod period) {

		setAlarmWarningPeriod(context, period, seconds, actionName);
	}

	public void setAlarmWarningPeriod(Context context, EnabledPeriod period,
			int warningPeriodSeconds, String actionName) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		PendingIntent pendingIntentOff = getSwitchOffIntent(context, period,
				actionName);

		int delayMillis = warningPeriodSeconds * 1000;

		long wakeTime = SystemClock.elapsedRealtime() + delayMillis;

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(wakeTime);

		Log.d("AlarmHandler", "Setting warning alarm with intent " + actionName
				+ " for " + calendar.getTime().toString());

		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeTime, pendingIntentOff);
	}

	//
	// public void setAlarmWarningPeriod(Context context, boolean on,
	// ScheduleSettings currentSettings, int warningPeriodSeconds,
	// String actionName) {
	// AlarmManager am = (AlarmManager) context
	// .getSystemService(android.content.Context.ALARM_SERVICE);
	//
	// PendingIntent pendingIntentOff = getSwitchOffIntent(context,
	// currentSettings, actionName);
	//
	// int delayMillis = warningPeriodSeconds * 1000;
	//
	// long wakeTime = SystemClock.elapsedRealtime() + delayMillis;
	//
	// Calendar calendar = Calendar.getInstance();
	// calendar.setTimeInMillis(wakeTime);
	//
	// Log.d("AlarmHandler", "Setting warning alarm with intent " + actionName
	// + " for " + calendar.getTime().toString());
	//
	// am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, wakeTime, pendingIntentOff);
	// }

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
		bundle.putBoolean(context.getString(R.string.action_3g_on), enable3g);

		intent.putExtras(bundle);

		// using service because getActivity needs API level 16
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
				intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		return pendingIntent;
	}

	private PendingIntent getPendingIntent(Context context,
			EnabledPeriod period, boolean start) {

		String action;

		if (period.is_schedulingEnabled()) {
			action = "Enabling 3G";
		} else {
			action = "Disabling 3G";
		}

		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		// to differentiate the intents, otherwise they update each other! NOTE:
		// Extras are not enough!
		intent.setAction(action);

		Bundle bundle = new Bundle();
		bundle.putBoolean(context.getString(R.string.action_3g_on), start);
		bundle.putInt(context.getString(R.string.period_id), period.get_id());

		intent.putExtras(bundle);

		// NOTE: the requestCode must be unique, otherwise they overwrite each
		// other
		int requestCode;
		if (start) {
			requestCode = period.get_id();
		} else {
			requestCode = period.get_id() * -1;
		}

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		return pendingIntent;
	}

	private PendingIntent getSwitchOffIntent(Context context,
			EnabledPeriod period, String actionName) {

		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		intent.setAction(actionName);

		Bundle bundle = new Bundle();
		bundle.putBoolean(context.getString(R.string.action_3g_on), false);

		if (period != null) {
			bundle.putLong("StopAt", period.get_endTimeMillis());
			bundle.putInt(context.getString(R.string.period_id),
					period.get_id());
		}

		intent.putExtras(bundle);

		// magic number should not collide with any period id
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				Integer.MIN_VALUE, intent, Intent.FLAG_ACTIVITY_NEW_TASK);

		return pendingIntent;
	}
	//
	// private PendingIntent getSwitchOffIntent(Context context,
	// ScheduleSettings currentSettings, String actionName) {
	//
	// Intent intent = new Intent(context, StartStopBroadcastReceiver.class);
	//
	// intent.setAction(actionName);
	//
	// Bundle bundle = new Bundle();
	// bundle.putBoolean(context.getString(R.string.action_3g_on), false);
	//
	// if (currentSettings != null) {
	// bundle.putLong("StopAt", currentSettings.get_endTimeMillis());
	// }
	//
	// intent.putExtras(bundle);
	//
	// PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1,
	// intent, Intent.FLAG_ACTIVITY_NEW_TASK);
	//
	// return pendingIntent;
	// }
}
