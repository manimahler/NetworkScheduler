package com.manimahler.android.scheduler3g;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;

public class NetworkScheduler {

	private static final String MOBILEDATA = "MOBILEDATA";
	private static final String WIFI = "WIFI";

	public SharedPreferences getSchedulesPreferences(Context context) {
		return context.getSharedPreferences("SCHEDULER_PREFS",
				Context.MODE_PRIVATE);
	}

	public void deleteAlarms(Context context,
			ArrayList<ScheduledPeriod> enabledPeriods) {
		for (ScheduledPeriod enabledPeriod : enabledPeriods) {
			deleteAlarm(context, enabledPeriod);
		}

		cancelIntervalConnect(context);
	}

	public void deleteAlarm(Context context, ScheduledPeriod period) {

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Log.d("NetworkScheduler",
				"Deleting alarms for period " + period.get_id());

		PendingIntent pendingIntentOn = getPendingIntent(context, period, true);
		PendingIntent pendingIntentOff = getPendingIntent(context, period,
				false);

		// cancel both
		am.cancel(pendingIntentOn);
		am.cancel(pendingIntentOff);

		// if (period.useIntervalConnect())
		// {
		// cancelIntervalConnect(context, period.get_id());
		// }
	}

	public void setAlarms(Context context,
			ArrayList<ScheduledPeriod> enabledPeriods,
			SchedulerSettings settings) {
		// it will be re-set if necessary:
		cancelIntervalConnect(context);

		for (ScheduledPeriod enabledPeriod : enabledPeriods) {
			try {
				setAlarm(context, enabledPeriod, settings);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void setNextAlarmStart(Context context, ScheduledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent pendingIntentOn = getPendingIntent(context, period, true);

		if (!period.is_schedulingEnabled()) {
			// cancel
			am.cancel(pendingIntentOn);

			return;
		}

		if (period.is_scheduleStart()) {
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_startTimeMillis());

			AlarmUtils.setAlarm(context, pendingIntentOn, startMillis);

			// am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis,
			// interval24h,
			// pendingIntentOn);
		} else {
			am.cancel(pendingIntentOn);
		}

		if (period.is_active() && period.useIntervalConnect()) {
			startIntervalConnect(context, settings);
		}
	}

	public void setNextAlarmStop(Context context, ScheduledPeriod period)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		PendingIntent pendingIntentOff = getPendingIntent(context, period,
				false);

		if (!period.is_schedulingEnabled()) {
			// cancel
			am.cancel(pendingIntentOff);

			return;
		}

		if (period.is_scheduleStop()) {
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_endTimeMillis());

			AlarmUtils.setAlarm(context, pendingIntentOff, stopMillis);
			// am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h,
			// pendingIntentOff);
		} else {
			am.cancel(pendingIntentOff);
		}
	}

	public void setAlarm(Context context, ScheduledPeriod period,
			SchedulerSettings settings) throws Exception {

		// TRIAL:
		setNextAlarmStart(context, period, settings);
		setNextAlarmStop(context, period);
		//
		// AlarmManager am = (AlarmManager) context
		// .getSystemService(Context.ALARM_SERVICE);
		//
		// long interval24h = 24 * 60 * 60 * 1000;
		//
		// PendingIntent pendingIntentOn = getPendingIntent(context, period,
		// true);
		// PendingIntent pendingIntentOff = getPendingIntent(context, period,
		// false);
		//
		// if (!period.is_schedulingEnabled()) {
		// // cancel both
		// am.cancel(pendingIntentOn);
		// am.cancel(pendingIntentOff);
		//
		// return;
		// }
		//
		// if (period.is_scheduleStart()) {
		// long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
		// .get_startTimeMillis());
		//
		// am.setRepeating(AlarmManager.RTC_WAKEUP, startMillis, interval24h,
		// pendingIntentOn);
		// } else {
		// am.cancel(pendingIntentOn);
		// }
		//
		// if (period.is_scheduleStop()) {
		// long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
		// .get_endTimeMillis());
		// am.setRepeating(AlarmManager.RTC_WAKEUP, stopMillis, interval24h,
		// pendingIntentOff);
		// } else {
		// am.cancel(pendingIntentOff);
		// }
		//
		// if (period.is_active() && period.useIntervalConnect()) {
		// startIntervalConnect(context, period, settings);
		// }
	}

	public boolean isChangeRequired(Context context, ScheduledPeriod period) {
		if (period.is_mobileData() && ConnectionUtils.isMobileDataOn(context)) {
			return true;
		}

		if (period.is_wifi() && ConnectionUtils.isWifiOn(context)) {
			return true;
		}

		if (period.is_bluetooth() && ConnectionUtils.isBluetoothOn()) {
			return true;
		}

		if (period.is_volume()) {
			return true;
		}

		return false;
	}

	public void makeAutoDelayNotification(Context context,
			ScheduledPeriod period, SchedulerSettings settings) {

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
				.setPriority(NotificationCompat.PRIORITY_MAX)
				// otherwise the buttons are not shown
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

	public void makeDisableNotification(Context context,
			ScheduledPeriod period, SchedulerSettings settings) {

		String tickerText = getTickerText(period, context);

		if (tickerText == null) {
			Log.d("NetworkScheduler", "No action needed");
			return;
		}

		// TODO: should be b/w icon according to design guidelines
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
				.setPriority(NotificationCompat.PRIORITY_MAX)
				// increases the chance to see the buttons
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

	private ArrayList<String> getConnectionSwitchOffList(
			ScheduledPeriod period, Context context) {
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

		if (period.is_volume() && ConnectionUtils.isVolumeOn(context)) {
			result.add(context.getString(R.string.volume_on));
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

	private String getTickerText(ScheduledPeriod period, Context context) {

		ArrayList<String> sensorsToSwitchOff = getConnectionSwitchOffList(
				period, context);

		if (sensorsToSwitchOff.isEmpty()) {
			return null;
		}

		String tickerText = context.getString(R.string.switch_off_shortly)
				+ join(sensorsToSwitchOff, ", ");

		return tickerText;
	}

	public void start(ScheduledPeriod period, Context context,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		boolean isPeriodActivation = period.activeIsEnabled();
		period.set_active(isPeriodActivation);

		boolean skip = period.is_skipped();
		if (skip) {
			if (!isPeriodActivation) {
				// reset at the end of the period
				period.set_skipped(false);
			}
		}

		// must be saved straight away because re-read in startIntervalConnect
		PersistenceUtils.saveToPreferences(getSchedulesPreferences(context),
				period);

		if (!skip) {
			// when activating (enabling) && start is earlier than end, we
			// really
			// want this period to win:
			// but when activating and start is later (active == disabled) we
			// wan
			// the previous period
			// to have a say:
			boolean enable = true;
			if (isPeriodActivation) {
				ConnectionUtils.toggleNetworkState(context, period, enable);

				if (period.useIntervalConnect()) {
					startIntervalConnect(context, settings);
				} else {
					cancelIntervalConnect(context);
				}

			} else {
				endToggleSensors(context, period, settings, enable);
			}
		} else {
			String name = period.get_name();
			if (name == null)
				name = "<no name>";
			Log.i("NetworkScheduler",
					"Period is skipped, not starting sensors for "
							+ period.get_name());
		}

	}

	private void endToggleSensors(Context context, ScheduledPeriod period,
			SchedulerSettings settings, boolean enable)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		// if previously started, still active period has interval connect
		// and this period has constant-connect: cancel interval connect
		// and this period does not specify the sensor: keep interval connect
		// -> if in doubt (active period requires interval connect) start it
		// and decide in the broadcast receiver what to do

		// get previously activated period to find out whether to actually start
		// the sensors or stop them
		ArrayList<ScheduledPeriod> allPeriods = PersistenceUtils
				.readFromPreferences(getSchedulesPreferences(context));

		boolean intervalConnectRequired = false;
		if (period.is_wifi()) {
			ScheduledPeriod previousActiveWifi = getLastActivatedActivePeriod(
					allPeriods, NetworkType.WiFi);

			boolean enableWifi;
			if (enable) {
				enableWifi = previousActiveWifi == null
						|| previousActiveWifi.activeIsEnabled();
			} else {
				enableWifi = previousActiveWifi != null
						&& previousActiveWifi.activeIsEnabled();
			}
			ConnectionUtils.toggleWifi(context, enableWifi);

			if (enableWifi && previousActiveWifi != null
					&& previousActiveWifi.activeIsEnabled()
					&& previousActiveWifi.is_intervalConnectWifi()) {
				intervalConnectRequired = true;
			}
		}

		if (period.is_mobileData()) {
			ScheduledPeriod previousActiveMobData = getLastActivatedActivePeriod(
					allPeriods, NetworkType.MobileData);

			boolean enableMobData;
			if (enable) {
				enableMobData = previousActiveMobData == null
						|| previousActiveMobData.activeIsEnabled();
			} else {
				enableMobData = previousActiveMobData != null
						&& previousActiveMobData.activeIsEnabled();
			}

			ConnectionUtils.toggleMobileData(context, enableMobData);

			if (enableMobData && previousActiveMobData != null
					&& previousActiveMobData.activeIsEnabled()
					&& previousActiveMobData.is_intervalConnectMobData()) {
				intervalConnectRequired = true;
			}
		}

		if (period.is_bluetooth()) {
			ScheduledPeriod previousActiveBluetooth = getLastActivatedActivePeriod(
					allPeriods, NetworkType.Bluetooth);

			boolean enableBluetooth;
			if (enable) {
				enableBluetooth = previousActiveBluetooth == null
						|| previousActiveBluetooth.activeIsEnabled();
			} else {
				enableBluetooth = previousActiveBluetooth != null
						&& previousActiveBluetooth.activeIsEnabled();
			}

			ConnectionUtils.toggleBluetooth(context, enableBluetooth);
		}

		if (period.is_volume()) {
			ScheduledPeriod previousActiveVolume = getLastActivatedActivePeriod(
					allPeriods, NetworkType.Volume);

			boolean enableVolume;
			if (enable) {
				enableVolume = previousActiveVolume == null
						|| previousActiveVolume.activeIsEnabled();
			} else {
				enableVolume = previousActiveVolume != null
						&& previousActiveVolume.activeIsEnabled();
			}

			ConnectionUtils.toggleVolume(context, enableVolume);
		}

		if (intervalConnectRequired) {
			startIntervalConnect(context, settings);
		} else {
			cancelIntervalConnect(context);
		}
		//
		// // for the sensors which have not interval-connect activated
		// ConnectionUtils.toggleNetworkState(context, period, true);
	}

	public void stopApproved(Context context, ScheduledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		if (!isChangeRequired(context, period)) {
			Log.d("StartStopReceiver", "No action required.");

			// still cancel interval connect
			stop(period, context);
		}

		if (!settings.is_warnOnDeactivation()) {
			stop(period, context);
		}

		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		boolean isScreenOn = powerManager.isScreenOn();

		if (!isScreenOn && settings.is_warnOnlyWhenScreenOn()) {
			// cancel interval connect
			stop(period, context);
		} else {
			if (settings.is_autoDelay()) {
				Log.d("StartStopReceiver", "Screen is on: auto-delay.");

				int delayInSec = settings.get_delay() * 60;

				makeAutoDelayNotification(context, period, settings);
				scheduleSwitchOff(context, delayInSec, "OFF_DELAYED", period);
			} else {
				Log.d("StartStopReceiver", "Screen is on: notification.");
				makeDisableNotification(context, period, settings);

				int fewMomentsInSec = 45;
				scheduleSwitchOff(context, fewMomentsInSec, "OFF", period);
			}
		}
	}

	public void stop(int periodId, Context context)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		SharedPreferences sharedPrefs = getSchedulesPreferences(context);

		ScheduledPeriod period = PersistenceUtils.getPeriod(sharedPrefs,
				periodId);

		stop(period, context);
	}

	public void stop(ScheduledPeriod period, Context context)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		if (period == null) {
			Log.d("NetworkScheduler",
					"switchOffNow: Period is null. Assuming deleted");
			return;
		}

		boolean isPeriodActivation = !period.activeIsEnabled();
		period.set_active(isPeriodActivation);

		period.set_overrideIntervalWifi(false);
		period.set_overrideIntervalMob(false);
		
		boolean skip = period.is_skipped();
		if (!isPeriodActivation && skip) {
			// reset at the end of the period
			period.set_skipped(false);
		}

		PersistenceUtils.saveToPreferences(getSchedulesPreferences(context),
				period);

		if (!skip) {
			SchedulerSettings settings = PersistenceUtils.readSettings(context);

			if (isPeriodActivation) {
				// it's the stop, but it happens before the start, so the period
				// becomes active
				ConnectionUtils.toggleNetworkState(context, period, false);
				// stop interval connect if not necessary, or re-start it with
				// the
				// relevant sensors (from other periods)
				startIntervalConnect(context, settings);
			} else {
				endToggleSensors(context, period, settings, false);
			}
		}
	}

	public void cancelSwitchOff(Context context, int periodId) {

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.cancel(periodId);

		cancelSwitchOff(context, periodId, "OFF");
		cancelSwitchOff(context, periodId, "OFF_DELAYED");
	}

	public void cancelSwitchOff(Context context, int periodId, String actionName) {
		try {

			PendingIntent pendingIntentOff = getSwitchOffIntent(context,
					periodId, 0, actionName);

			AlarmUtils.cancelAlarm(context, pendingIntentOff);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void cancelIntervalConnect(Context context) {

		Log.d("NetworkScheduler",
				"cancelIntervalConnect: Cancelling interval connect");

		PendingIntent intervalIntent = getIntervalOnIntent(context);
		AlarmUtils.cancelAlarm(context, intervalIntent);

		PendingIntent intervalOffIntent = getIntervalOffIntent(context, null);
		AlarmUtils.cancelAlarm(context, intervalOffIntent);
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, int periodId) {

		SharedPreferences sharedPrefs = getSchedulesPreferences(context);

		ScheduledPeriod period = PersistenceUtils.getPeriod(sharedPrefs,
				periodId);

		if (period == null) {
			Log.d("NetworkScheduler",
					"scheduleSwitchOff: Period is null. Assuming deleted");
		} else {
			scheduleSwitchOff(context, seconds, actionName, period);
		}
	}

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, ScheduledPeriod period) {

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

	public void scheduleIntervalConnect(Context context,
			SchedulerSettings settings) {

		int intervalSeconds = settings.get_connectInterval() * 60;

		PendingIntent intervalIntent = getIntervalOnIntent(context);

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
			Bundle extras) {

		// long nextOff = DateTimeUtils.getTimeFromNowInMillis(connectTimeSec);

		PendingIntent intervalOffIntent = getIntervalOffIntent(context, extras);

		AlarmUtils.setAlarm(context, intervalOffIntent, connectTimeSec);

		// AlarmManager am = (AlarmManager) context
		// .getSystemService(android.content.Context.ALARM_SERVICE);
		//
		// Log.d("NetworkScheduler", "setting interval off alarm");
		// am.set(AlarmManager.RTC_WAKEUP, nextOff, intervalOffIntent);
	}

	// public void switchOffNow(Context context, int periodId)
	// throws ClassNotFoundException, NoSuchFieldException,
	// IllegalArgumentException, IllegalAccessException,
	// NoSuchMethodException, InvocationTargetException {
	//
	// SharedPreferences sharedPrefs = getSchedulesPreferences(context);
	//
	// EnabledPeriod period = PersistenceUtils
	// .getPeriod(sharedPrefs, periodId);
	//
	// cancelIntervalConnect(context, periodId);
	//
	// if (period == null) {
	// Log.d("NetworkScheduler",
	// "switchOffNow: Period is null. Assuming deleted");
	// } else {
	//
	// ConnectionUtils.toggleNetworkState(context, period, false);
	// }
	// }

	// public void switchOnNow(Context context, EnabledPeriod period,
	// SchedulerSettings settings) throws ClassNotFoundException,
	// NoSuchFieldException, IllegalArgumentException,
	// IllegalAccessException, NoSuchMethodException,
	// InvocationTargetException {
	//
	// if (period.useIntervalConnect()) {
	// startIntervalConnect(context, period, settings);
	// } else {
	// ConnectionUtils.toggleNetworkState(context, period, true);
	// }
	// }

	public void setupIntervalConnect(Context context, SchedulerSettings settings) {
		try {
			startIntervalConnect(context, settings);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void startIntervalConnect(Context context,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		scheduleIntervalConnect(context, settings);

		// do the first switch-on right now
		intervalSwitchOn(context, settings);

		// registerScreenOnBroadcastReceiver(context, period, settings);
	}

	//
	// private BroadcastReceiver mPowerKeyReceiver = null;
	//
	// private void registerScreenOnBroadcastReceiver(Context context,
	// EnabledPeriod period, SchedulerSettings settings) {
	// final IntentFilter theFilter = new IntentFilter();
	// /** System Defined Broadcast */
	// theFilter.addAction(Intent.ACTION_SCREEN_ON);
	// theFilter.addAction(Intent.ACTION_SCREEN_OFF);
	//
	// mPowerKeyReceiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// String strAction = intent.getAction();
	//
	// Log.d("NetworkScheduler", "Receiving screen action ON/OFF");
	//
	// if (strAction.equals(Intent.ACTION_SCREEN_ON)) {
	//
	// try{
	// SchedulerSettings settings = PersistenceUtils.readSettings(context);
	//
	// SharedPreferences sharedPrefs = getSchedulesPreferences(context);
	//
	// int mobIntervalPeriod =
	// intent.getIntExtra(context.getString(R.string.period_id) + "MOB", -1);
	//
	// int wifiIntervalPeriod = 3;
	// //intent.getIntExtra(context.getString(R.string.period_id) + "WIFI", -1);
	//
	// if (mobIntervalPeriod >= 0)
	// {
	// Log.d("NetworkScheduler", "Receiving screen ON for mobile data");
	//
	// EnabledPeriod period = PersistenceUtils
	// .getPeriod(sharedPrefs, mobIntervalPeriod);
	//
	// intervalSwitchOn(context, period, settings);
	// }
	//
	// if (wifiIntervalPeriod >= 0)
	// {
	// Log.d("NetworkScheduler", "Receiving screen ON for wifi");
	//
	// EnabledPeriod period = PersistenceUtils
	// .getPeriod(sharedPrefs, wifiIntervalPeriod);
	//
	// intervalSwitchOn(context, period, settings);
	// }
	// }
	// catch (Exception ex)
	// {
	// Log.e("NeworkScheduler", "Error in screen on handler: " +
	// ex.getMessage());
	// }
	// }
	// }
	// };
	//
	// Bundle bundle = new Bundle();
	//
	// if (period.useIntervalConnectMobileData(settings))
	// {
	// bundle.putInt(context.getString(R.string.period_id) + "MOB",
	// period.get_id());
	// }
	//
	// if (period.useIntervalConnectWifi(settings))
	// {
	// bundle.putInt(context.getString(R.string.period_id) + "WIFI",
	// period.get_id());
	// }
	//
	// //mPowerKeyReceiver.setResultExtras(bundle);
	//
	// context.getApplicationContext().registerReceiver(mPowerKeyReceiver,
	// theFilter);
	// }
	//
	// private void unregisterScreenOnReceiver(Context context) {
	//
	//
	// try {
	// context.unregisterReceiver(mPowerKeyReceiver);
	// }
	// catch (IllegalArgumentException e) {
	// mPowerKeyReceiver = null;
	// }
	// }

	private boolean anyPeriodUsesIntervalConnect(Context context) {
		ArrayList<ScheduledPeriod> allPeriods = PersistenceUtils
				.readFromPreferences(getSchedulesPreferences(context));

		for (ScheduledPeriod enabledPeriod : allPeriods) {
			if (enabledPeriod.useIntervalConnect()) {
				return true;
			}
		}

		return false;
	}

	private enum NetworkType {
		WiFi, MobileData, Bluetooth, Volume
	}

	private ScheduledPeriod getLastActivatedActivePeriod(
			ArrayList<ScheduledPeriod> allPeriods, NetworkType networkType) {
		return getLastActivatedActivePeriod(allPeriods, networkType, null);
	}

	private ScheduledPeriod getLastActivatedActivePeriod(
			ArrayList<ScheduledPeriod> allPeriods, NetworkType networkType,
			ScheduledPeriod except) {
		ScheduledPeriod result = null;
		// ScheduledPeriod lastActivatedMobDataPeriod = null;
		for (ScheduledPeriod period : allPeriods) {

			if (!period.is_active()) {
				continue;
			}

			if (except != null && except.get_id() == period.get_id()) {
				continue;
			}

			if (networkType == NetworkType.WiFi && !period.is_wifi()) {
				continue;
			}

			if (networkType == NetworkType.MobileData
					&& !period.is_mobileData()) {
				continue;
			}

			if (networkType == NetworkType.Bluetooth && !period.is_bluetooth()) {
				continue;
			}

			if (networkType == NetworkType.Volume && !period.is_volume()) {
				continue;
			}

			Calendar lastActivated = period.getLastScheduledActivationTime();
			if (lastActivated == null) {
				continue;
			}

			if (result == null) {
				result = period;
				continue;
			}

			if (networkType == NetworkType.WiFi
					&& period.is_wifi()
					&& lastActivated.after(result
							.getLastScheduledActivationTime())) {
				result = period;
			}

			if (networkType == NetworkType.MobileData
					&& period.is_mobileData()
					&& lastActivated.after(result
							.getLastScheduledActivationTime())) {
				result = period;
			}

			if (networkType == NetworkType.Bluetooth
					&& period.is_bluetooth()
					&& lastActivated.after(result
							.getLastScheduledActivationTime())) {
				result = period;
			}

			if (networkType == NetworkType.Volume
					&& period.is_volume()
					&& lastActivated.after(result
							.getLastScheduledActivationTime())) {
				result = period;
			}
		}
		return result;
	}

	public void intervalSwitchOn(Context context, SchedulerSettings settings)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		// any period could be active and require interval connect
		// if 2 periods are active at the time, the later started period
		// wins (consider storing activation time for manual activation)
		// both if the activation of the later means switching off or on

		// TODO: whenever the user has enabled / disabled a network by other
		// means
		// respect that setting (see WifiEnabler in android source)

		ArrayList<ScheduledPeriod> allPeriods = PersistenceUtils
				.readFromPreferences(getSchedulesPreferences(context));

		ScheduledPeriod lastActivatedWifiPeriod = getLastActivatedActivePeriod(
				allPeriods, NetworkType.WiFi);
		ScheduledPeriod lastActivatedMobDataPeriod = getLastActivatedActivePeriod(
				allPeriods, NetworkType.MobileData);

		boolean intervalWifi = lastActivatedWifiPeriod != null
				&& lastActivatedWifiPeriod.activeIsEnabled()
				&& lastActivatedWifiPeriod.is_intervalConnectWifi()
				&& !lastActivatedWifiPeriod.is_overrideIntervalWifi();

		boolean intervalMobData = lastActivatedMobDataPeriod != null
				&& lastActivatedMobDataPeriod.activeIsEnabled()
				&& lastActivatedMobDataPeriod.is_intervalConnectMobData()
				&& !lastActivatedMobDataPeriod.is_overrideIntervalMob();

		if (!intervalWifi && !intervalMobData) {
			// no relevant active interval-connect period at all
			cancelIntervalConnect(context);
			return;
		}

		Log.d("NetworkScheduler", "intervalSwitchOn: Interval connect is ON.");
		int connectTimeSec = 60;

		scheduleIntervalSwitchOff(context, connectTimeSec,
				createSwitchOffExtras(intervalWifi, intervalMobData));

		// first toggle-on Wi-Fi, it takes slightly longer to start
		if (intervalWifi) {
			ConnectionUtils.toggleWifi(context, true);
		}

		if (intervalMobData) {
			ConnectionUtils.toggleMobileData(context, true);
		}
	}

	public void intervalSwitchOff(Context context, SchedulerSettings settings,
			Bundle bundle) throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		// TODO: also here, loop through all active periods!
		// schedule another 2-minute period if screen is on or keyguard not
		// locked:
		int reTestIntervalSec = 120;

		KeyguardManager kgMgr = (KeyguardManager) context
				.getSystemService(Context.KEYGUARD_SERVICE);

		boolean isDeviceLocked = kgMgr.inKeyguardRestrictedInputMode();

		if (isScreenOn(context) && !isDeviceLocked) {
			Log.d("NetworkScheduler",
					"intervalSwitchOff: Screen is ON and device is unlocked, not switching off...");

			scheduleIntervalSwitchOff(context, reTestIntervalSec, bundle);
			return;
		}

		if (!isDeviceLocked) {
			// NOTE: if keyguard is not locked and the user switches the screen
			// back on
			// NO user_present broadcast is received! Therefore only switch off
			// if locked.
			Log.d("NetworkScheduler",
					"intervalSwitchOff: Keyguard is not locked, not switching off...");

			scheduleIntervalSwitchOff(context, reTestIntervalSec, bundle);
			return;
		}

		if (bundle == null) {
			Log.d("NetworkScheduler", "No bundle");
		}

		boolean intervalWifi = bundle == null || bundle.getBoolean(WIFI);
		boolean intervalMobData = bundle == null
				|| bundle.getBoolean(MOBILEDATA);

		if (intervalMobData) {
			ConnectionUtils.toggleMobileData(context, false);
		}

		if (intervalWifi) {
			if (settings.is_keepWifiConnected()
					&& ConnectionUtils.isWifiConnected(context)) {
				Log.d("NetworkScheduler",
						"intervalSwitchOff: WIFI is connected, not switching off...");
			} else {
				ConnectionUtils.toggleWifi(context, false);
			}
		}
	}

	private PendingIntent getPendingIntent(Context context,
			ScheduledPeriod period, boolean start) {

		String action;

		if (start) {
			action = context.getString(R.string.action_start);
		} else {
			action = context.getString(R.string.action_stop);
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
			ScheduledPeriod period, String actionName) {

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

	private PendingIntent getIntervalOnIntent(Context context) {

		String actionName = "INTERVAL_ON";

		return getIntervalIntent(context, actionName, null);
	}

	private PendingIntent getIntervalOffIntent(Context context, Bundle extras) {
		String actionName = "INTERVAL_OFF";

		return getIntervalIntent(context, actionName, extras);
	}

	private Bundle createSwitchOffExtras(boolean wifi, boolean mobData) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(WIFI, wifi);
		bundle.putBoolean(MOBILEDATA, mobData);
		return bundle;
	}

	private PendingIntent getIntervalIntent(Context context, String actionName,
			Bundle bundle) {
		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		intent.setAction(actionName);

		if (bundle != null) {
			intent.putExtras(bundle);
		}

		// use same request code everywhere to ensure no parallel interval
		// connects happen
		// by various enabled periods being on at the same time
		int requestCode = 0;

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT);

		return pendingIntent;
	}

	private boolean isScreenOn(Context context) {
		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		return powerManager.isScreenOn();
	}

}
