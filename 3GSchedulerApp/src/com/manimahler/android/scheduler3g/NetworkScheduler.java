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
		PendingIntent pendingIntentOff = getPendingIntent(context, period, false);
		
		if (! period.is_schedulingEnabled()) {
			// cancel both
			am.cancel(pendingIntentOn);
			am.cancel(pendingIntentOff);
			
			return;
		}

		
		if (period.is_scheduleStart())
		{
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_startTimeMillis());
			
			am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis, interval24h,
					pendingIntentOn);
		}
		else
		{
			am.cancel(pendingIntentOn);
		}
		
		if (period.is_scheduleStop())
		{
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_endTimeMillis());
			am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h,
					pendingIntentOff);
		}
		else
		{
			am.cancel(pendingIntentOff);
		}
	}
	
	public boolean isSwitchOffRequired(Context context, EnabledPeriod period)
	{
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
	
	public void makeAutoDelayNotification(Context context, EnabledPeriod period, 
			SchedulerSettings settings) {

		ArrayList<String> sensorsToSwitchOff = getConnectionSwitchOffList(period, context);
		
		if (sensorsToSwitchOff.isEmpty())
		{
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

		PendingIntent deactivateNowIntentPending = PendingIntent.getBroadcast(context,
				0, deactivateNowIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		
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
				
		String timeFormat = DateFormat.getTimeFormat(context).format(new Date(cal.getTimeInMillis()));
		String text = String.format(context.getString(R.string.switch_off_auto_delayed_until), timeFormat);
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.clock_notification)
				.setLargeIcon(bm)
				.setContentTitle("Network switch-off delayed")
				.setContentText(text)
				.setTicker(text)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.addAction(R.drawable.clock_notification, context.getString(R.string.switch_off_now),
						deactivateNowIntentPending)
				.addAction(R.drawable.clock_notification, context.getString(R.string.not_today), skipIntentPending)
				.setAutoCancel(true);
		
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
				cal.getTimeInMillis(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS,  DateUtils.FORMAT_ABBREV_RELATIVE);
		
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
				.addAction(R.drawable.clock_notification, context.getString(R.string.not_today), skipIntentPending)
				.setAutoCancel(true);

		// NOTE: if watching full screen video, the ticker is not shown!
		if (settings.is_vibrate())
		{
			builder.setVibrate(new long[] { -1, 800, 1000 });
		}
		
		if (settings.is_playSound())
		{
			Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
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

	private ArrayList<String> getConnectionSwitchOffList(EnabledPeriod period, Context context)
	{
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
	
	private String getTickerText(EnabledPeriod period, Context context) {

		boolean mobileData = false;
		boolean wifi = false;
		boolean bluetooth = false;

		// String sensors = "";

		if (period.is_mobileData()) {

			mobileData = ConnectionUtils.isMobileDataOn(context);
		}
		
		if (period.is_wifi()) {
			
			wifi = ConnectionUtils.isWifiOn(context);
		}

		if (period.is_bluetooth()) {
			bluetooth = ConnectionUtils.isBluetoothOn();
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
			String actionName, int periodId) {

		SharedPreferences sharedPrefs = getSchedulesPreferences(context);

		EnabledPeriod period = PersistenceUtils
				.getPeriod(sharedPrefs, periodId);
		
		if (period == null)
		{
			Log.d("NetworkScheduler", "scheduleSwitchOff: Period is null. Assuming deleted");
		}
		else
		{
			scheduleSwitchOff(context, seconds, actionName, period);
		}
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, EnabledPeriod period) {

		setAlarmWarningPeriod(context, period, seconds, actionName);
	}
	
	public void switchOffNow(Context context, int periodId) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		
		SharedPreferences sharedPrefs = getSchedulesPreferences(context);

		EnabledPeriod period = PersistenceUtils.getPeriod(sharedPrefs, periodId);
		
		if (period == null)
		{
			Log.d("NetworkScheduler", "switchOffNow: Period is null. Assuming deleted");
		}
		else
		{
			ConnectionUtils.toggleNetworkState(context, period, false);
		}
	}

	private void setAlarmWarningPeriod(Context context, EnabledPeriod period,
			int warningPeriodSeconds, String actionName) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		PendingIntent pendingIntentOff = getSwitchOffIntent(context, period,
				actionName);

		long wakeTime = DateTimeUtils.getTimeFromNowInMillis(warningPeriodSeconds);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(wakeTime);

		Log.d("AlarmHandler", "Setting warning alarm with intent " + actionName
				+ " for " + calendar.getTime().toString());
		
		am.set(AlarmManager.RTC_WAKEUP, wakeTime, pendingIntentOff);
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
				Integer.MIN_VALUE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return pendingIntent;
	}
}
