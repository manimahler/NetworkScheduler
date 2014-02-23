package com.manimahler.android.scheduler3g;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

public class NetworkScheduler {

	public SharedPreferences getSchedulesPreferences(Context context) {
		return context.getSharedPreferences("SCHEDULER_PREFS",
				Context.MODE_PRIVATE);
	}

	public void setAlarm(Context context, EnabledPeriod period) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		long interval24h = 24 * 60 * 60 * 1000;

		PendingIntent pendingIntentOn = getPendingIntent(context, period, true);
		PendingIntent pendingIntentOff = getPendingIntent(context, period,
				false);

		if (!period.is_schedulingEnabled()) {
			// cancel both
			am.cancel(pendingIntentOn);
			am.cancel(pendingIntentOff);

			return;
		}

		if (period.is_scheduleStart()) {
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_startTimeMillis());

			am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis, interval24h,
					pendingIntentOn);
		} else {
			am.cancel(pendingIntentOn);
		}

		if (period.is_scheduleStop()) {
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_endTimeMillis());
			am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h,
					pendingIntentOff);
		} else {
			am.cancel(pendingIntentOff);
		}
	}

	public boolean isSwitchOffRequired(Context context, EnabledPeriod period) {
		if (period.is_mobileData() && ConnectionUtils.isMobileDataOn(context)) {
			return true;
		}

		if (period.is_wifi() && ConnectionUtils.isWifiOn(context)) {
			return true;
		}

		if (period.is_bluetooth() && ConnectionUtils.isBluetoothOn()) {
			return true;
		}

		return false;
	}

	public void makeAutoDelayNotification(Context context,
			EnabledPeriod period, SchedulerSettings settings) {

		ArrayList<String> sensorsToSwitchOff = getConnectionSwitchOffList(
				period, context);

		if (sensorsToSwitchOff.isEmpty()) {
			Log.d("NetworkScheduler", "No action needed");
			return;
		}

		Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_launcher);

		// to allow 1h delay by user clicking:
		Intent deactivateNowIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		// delayIntent.putExtra(context.getString(R.string.period_id),
		// periodId);
		deactivateNowIntent.setAction("DEACTIVATE");
		deactivateNowIntent.putExtra(context.getString(R.string.period_id),
				period.get_id());

		PendingIntent deactivateNowIntentPending = PendingIntent.getBroadcast(
				context, 0, deactivateNowIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		Intent skipIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		skipIntent.setAction("SKIP");
		skipIntent.putExtra(context.getString(R.string.period_id),
				period.get_id());

		PendingIntent skipIntentPending = PendingIntent.getBroadcast(context,
				0, skipIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		int delayTimeMin = settings.get_delay();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, delayTimeMin);

		String timeFormat = DateFormat.getTimeFormat(context).format(
				new Date(cal.getTimeInMillis()));
		String text = String.format(
				context.getString(R.string.switch_off_auto_delayed_until),
				timeFormat);

		String title = context.getString(R.string.switch_off_auto_delayed);

		if (period.get_name() != null && !period.get_name().isEmpty()) {
			text += String.format(": %s", period.get_name());
		}

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.clock_notification)
				.setLargeIcon(bm)
				.setContentTitle(title)
				.setContentText(text)
				.setTicker(text)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.addAction(R.drawable.clock_notification,
						context.getString(R.string.switch_off_now),
						deactivateNowIntentPending)
				.addAction(R.drawable.clock_notification,
						context.getString(R.string.not_today),
						skipIntentPending).setAutoCancel(true);

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

	public void makeDisableNotification(Context context, EnabledPeriod period,
			SchedulerSettings settings) {

		String tickerText = getTickerText(period, context);

		if (tickerText == null) {
			Log.d("NetworkScheduler", "No action needed");
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

		Intent skipIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		// delayIntent.putExtra(context.getString(R.string.period_id),
		// periodId);
		skipIntent.setAction("SKIP");
		skipIntent.putExtra(context.getString(R.string.period_id),
				period.get_id());

		PendingIntent skipIntentPending = PendingIntent.getBroadcast(context,
				0, skipIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		// TODO: settings / preferences for vibration, sound, toast

		int delayTimeMin = settings.get_delay();

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, delayTimeMin);

		CharSequence delayText = DateUtils.getRelativeTimeSpanString(
				cal.getTimeInMillis(), System.currentTimeMillis(),
				DateUtils.MINUTE_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.clock_notification)
				.setLargeIcon(bm)
				.setContentTitle("Switching off network")
				.setContentText(tickerText)
				.setTicker(tickerText)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.addAction(R.drawable.clock_notification, delayText,
						delayIntentPending)
				.addAction(R.drawable.clock_notification,
						context.getString(R.string.not_today),
						skipIntentPending).setAutoCancel(true);

		// NOTE: if watching full screen video, the ticker is not shown!
		if (settings.is_vibrate()) {
			builder.setVibrate(new long[] { -1, 800, 1000 });
		}

		if (settings.is_playSound()) {
			Uri soundUri = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			builder.setSound(soundUri);
		}

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

	private ArrayList<String> getConnectionSwitchOffList(EnabledPeriod period,
			Context context) {
		ArrayList<String> result = new ArrayList<String>(3);

		if (period.is_mobileData() && ConnectionUtils.isMobileDataOn(context)) {
			result.add(context.getString(R.string.mobile_data));
		}

		if (period.is_wifi() && ConnectionUtils.isWifiOn(context)) {
			result.add(context.getString(R.string.wifi));
		}

		if (period.is_bluetooth() && ConnectionUtils.isBluetoothOn()) {
			result.add(context.getString(R.string.bluetooth));
		}

		return result;
	}

	public static String join(ArrayList<String> list, String delimiter) {

		StringBuilder sb = new StringBuilder();

		String loopDelim = "";

		for (String s : list) {

			sb.append(loopDelim);
			sb.append(s);

			loopDelim = delimiter;
		}

		return sb.toString();
	}

	private String getTickerText(EnabledPeriod period, Context context) {

		ArrayList<String> sensorsToSwitchOff = getConnectionSwitchOffList(
				period, context);

		if (sensorsToSwitchOff.isEmpty()) {
			return null;
		}

		String tickerText = context.getString(R.string.switch_off_shortly)
				+ join(sensorsToSwitchOff, ", ");

		return tickerText;
	}
	
	public void cancelSwitchOff(Context context, int periodId)
	{

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		
		notificationManager.cancel(periodId);
		
		cancelSwitchOff(context, periodId, "OFF");
		cancelSwitchOff(context, periodId, "OFF_DELAYED");
	}

	public void cancelSwitchOff(Context context, int periodId,
			String actionName) {
		try {

			PendingIntent pendingIntentOff = getSwitchOffIntent(context,
					periodId, 0, actionName);

			AlarmUtils.cancelAlarm(context, pendingIntentOff);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cancelIntervalConnect(Context context, int periodId) {
		PendingIntent intervalIntent = getIntervalOnIntent(context, periodId);
		AlarmUtils.cancelAlarm(context, intervalIntent);

		PendingIntent intervalOffIntent = getIntervalOffIntent(context,
				periodId);
		AlarmUtils.cancelAlarm(context, intervalOffIntent);
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, int periodId) {

		SharedPreferences sharedPrefs = getSchedulesPreferences(context);

		EnabledPeriod period = PersistenceUtils
				.getPeriod(sharedPrefs, periodId);

		if (period == null) {
			Log.d("NetworkScheduler",
					"scheduleSwitchOff: Period is null. Assuming deleted");
		} else {
			scheduleSwitchOff(context, seconds, actionName, period);
		}
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, EnabledPeriod period) {

		// AlarmManager am = (AlarmManager) context
		// .getSystemService(android.content.Context.ALARM_SERVICE);

		PendingIntent pendingIntentOff = getSwitchOffIntent(context, period,
				actionName);

		AlarmUtils.setAlarm(context, pendingIntentOff, seconds);

		// long wakeTime = DateTimeUtils
		// .getTimeFromNowInMillis(seconds);
		//
		// Calendar calendar = Calendar.getInstance();
		// calendar.setTimeInMillis(wakeTime);
		//
		// Log.d("AlarmHandler", "Setting warning alarm with intent " +
		// actionName
		// + " for " + calendar.getTime().toString());
		//
		// am.set(AlarmManager.RTC_WAKEUP, wakeTime, pendingIntentOff);
	}

	public void scheduleIntervalConnect(Context context, int periodId,
			SchedulerSettings settings) {

		int intervalSeconds = settings.get_connectInterval() * 60;

		PendingIntent intervalIntent = getIntervalOnIntent(context, periodId);

		AlarmUtils.setInexactRepeatingAlarm(context, intervalIntent,
				intervalSeconds);
		//
		// AlarmManager am = (AlarmManager) context
		// .getSystemService(android.content.Context.ALARM_SERVICE);
		//
		// Log.d("NetworkScheduler", "setting inexact alarm for interval on");
		//
		// int intervalMillis = intervalSeconds * 1000;
		//
		// am.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstOn,
		// intervalMillis, intervalIntent);
	}

	public void scheduleIntervalSwitchOff(Context context, int connectTimeSec,
			int periodId) {

		// long nextOff = DateTimeUtils.getTimeFromNowInMillis(connectTimeSec);

		PendingIntent intervalOffIntent = getIntervalOffIntent(context,
				periodId);

		AlarmUtils.setAlarm(context, intervalOffIntent, connectTimeSec);

		// AlarmManager am = (AlarmManager) context
		// .getSystemService(android.content.Context.ALARM_SERVICE);
		//
		// Log.d("NetworkScheduler", "setting interval off alarm");
		// am.set(AlarmManager.RTC_WAKEUP, nextOff, intervalOffIntent);
	}

	public void switchOffNow(Context context, int periodId)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		SharedPreferences sharedPrefs = getSchedulesPreferences(context);

		EnabledPeriod period = PersistenceUtils
				.getPeriod(sharedPrefs, periodId);

		cancelIntervalConnect(context, periodId);

		if (period == null) {
			Log.d("NetworkScheduler",
					"switchOffNow: Period is null. Assuming deleted");
		} else {

			ConnectionUtils.toggleNetworkState(context, period, false);
		}
	}

	public void switchOnNow(Context context, EnabledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		if (period.is_intervalConnect()) {
			startIntervalConnect(context, period, settings);
		} else {
			ConnectionUtils.toggleNetworkState(context, period, true);
		}
	}

	private void startIntervalConnect(Context context, EnabledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		scheduleIntervalConnect(context, period.get_id(), settings);

		// do the first switch-on right now
		intervalSwitchOn(context, period, settings);
	}

	public void intervalSwitchOn(Context context, EnabledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		if (! period.is_intervalConnect()) {
			
			Log.d("NetworkScheduler", "intervalSwitchOn: Interval connect is off or not relevant. Cancelling");
			
			// interval connect was switched off, cancel the interval alarm
			cancelIntervalConnect(context, period.get_id());

			// and make sure the relevant sensors are ON
			ConnectionUtils.toggleNetworkState(context, period, true);

			return;
		}

		Log.d("NetworkScheduler", "intervalSwitchOn: Interval connect is ON.");
		int connectTimeSec = 120;
		
		scheduleIntervalSwitchOff(context, connectTimeSec, period.get_id());
		
		if (period.is_mobileData()) {
			ConnectionUtils.toggleMobileData(context, true);
		}

		if (period.is_wifi()) {
			ConnectionUtils.toggleWifi(context, true);
		}
	}

	public void intervalSwitchOff(Context context, EnabledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		if (!period.is_intervalConnect()) {
			// double-check, might have been switched off
			
			Log.d("NetworkScheduler", "intervalSwitchOff: Interval connect is OFF, not switching off...");
			return;
		}

		if (period.is_mobileData()) {
			ConnectionUtils.toggleMobileData(context, false);
		}
		
		if (period.is_wifi()) {
			ConnectionUtils.toggleWifi(context, false);
		}
	}

	private PendingIntent getPendingIntent(Context context,
			EnabledPeriod period, boolean start) {

		String action;

		if (period.is_schedulingEnabled()) {
			action = context.getString(R.string.action_enable);
		} else {
			action = context.getString(R.string.action_disable);
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
				requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return pendingIntent;
	}

	private PendingIntent getSwitchOffIntent(Context context,
			EnabledPeriod period, String actionName) {

		return getSwitchOffIntent(context, period.get_id(),
				period.get_endTimeMillis(), actionName);
	}

	private PendingIntent getSwitchOffIntent(Context context, int periodId,
			long endTimeMillis, String actionName) {

		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		intent.setAction(actionName);

		Bundle bundle = new Bundle();
		bundle.putBoolean(context.getString(R.string.action_3g_on), false);

		if (endTimeMillis > 0) {
			bundle.putLong("StopAt", endTimeMillis);
		}

		bundle.putInt(context.getString(R.string.period_id), periodId);

		intent.putExtras(bundle);

		// magic number should not collide with any period id
		// PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
		// Integer.MIN_VALUE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		// use periodId as request code to differentiate between different
		// switch-offs
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				periodId, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return pendingIntent;
	}

	private PendingIntent getIntervalOnIntent(Context context, int periodId) {

		String actionName = "INTERVAL_ON";

		return getIntervalIntent(context, actionName, periodId);
	}

	private PendingIntent getIntervalOffIntent(Context context, int periodId) {
		String actionName = "INTERVAL_OFF";

		return getIntervalIntent(context, actionName, periodId);
	}

	private PendingIntent getIntervalIntent(Context context, String actionName,
			int periodId) {
		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		intent.setAction(actionName);

		Bundle bundle = new Bundle();

		bundle.putInt(context.getString(R.string.period_id), periodId);

		intent.putExtras(bundle);

		// use periodId as request code to differentiate between different
		// switch-offs

		// use same request code everywhere to ensure no parallel interval
		// connects happen
		// by various enabled periods being on at the same time
		int requestCode = 0;

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return pendingIntent;
	}

}
