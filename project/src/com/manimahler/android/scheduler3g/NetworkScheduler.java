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

	private static final String TAG = NetworkScheduler.class.getSimpleName();

	public static final String ACTION_SWITCHOFF_DELAY = "DELAY";
	public static final String ACTION_SWITCHOFF_SKIP = "SKIP";
	public static final String ACTION_SWITCHOFF_DEACTIVATE_NOW = "DEACTIVATE";

	public static final String ACTION_START = "START";
	public static final String ACTION_STOP = "STOP";
	public static final String ACTION_INTERVAL_ON = "INTERVAL_ON";
	public static final String ACTION_INTERVAL_OFF = "INTERVAL_OFF";
	public static final String ACTION_OFF = "OFF";
	public static final String ACTION_OFF_DELAYED = "OFF_DELAYED";

	public static final String INTENT_EXTRA_PERIOD_ID = "PERIOD_ID";

	private static final String INTERVAL_MOBILEDATA = "MOBILEDATA";
	private static final String INTERVAL_WIFI = "WIFI";

	private enum NetworkType {
		WiFi, MobileData, Bluetooth, Volume
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

		Log.d(TAG, "Deleting alarms for period " + period.get_id());

		PendingIntent pendingIntentOn = getPendingIntent(context, period, true);
		PendingIntent pendingIntentOff = getPendingIntent(context, period,
				false);

		// cancel both
		am.cancel(pendingIntentOn);
		am.cancel(pendingIntentOff);
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
			am.cancel(pendingIntentOn);

			return;
		}

		if (period.is_scheduleStart()) {
			long startMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_startTimeMillis());

			AlarmUtils.setAlarm(context, pendingIntentOn, startMillis);

		} else {
			Log.d(TAG, "Cancelling start alarm for period " + period.get_id());
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
			am.cancel(pendingIntentOff);

			return;
		}

		if (period.is_scheduleStop()) {
			long stopMillis = DateTimeUtils.getNextTimeIn24hInMillis(period
					.get_endTimeMillis());

			AlarmUtils.setAlarm(context, pendingIntentOff, stopMillis);
		} else {
			am.cancel(pendingIntentOff);
		}
	}

	public void start(ScheduledPeriod period, Context context,
			SchedulerSettings settings, boolean manualActivation)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		boolean skip = period.is_skipped();

		boolean isPeriodActivation = period.is_enableRadios();

		boolean activate = isPeriodActivation && (!skip || manualActivation);
		period.set_active(activate);

		// reset transient properties at the end of the period (or at the start,
		// if there is no end)
		if (!isPeriodActivation || !period.is_scheduleStop()) {
			period.set_skipped(false);
			period.set_overrideIntervalMob(false);
			period.set_overrideIntervalWifi(false);
		}

		// must be saved straight away because re-read in startIntervalConnect
		PersistenceUtils.saveToPreferences(
				PersistenceUtils.getSchedulesPreferences(context), period);

		if (!skip || manualActivation) {
			// when activating (enabling) && start is earlier than end, we
			// really want this period to win:
			// but when activating and start is later (active == disabled) we
			// want the previous period to have a say:
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

			deactivateAffectedStartOnlyPeriods(context, period, settings);

		} else {
			String name = period.get_name();
			if (name == null)
				name = "<no name>";
			Log.i(TAG,
					"Period is skipped, not starting sensors for "
							+ period.get_name());
			UserLog.log(context,
					"Scheduled period is skipped. Radios not started for "
							+ period.toString(context));
		}
	}

	public void stopApproved(Context context, ScheduledPeriod period,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		if (!isChangeRequired(context, period)) {
			Log.d(TAG, "No action required.");
			UserLog.log(context, "No enabling/disabling required because all radios and volume are in the desired state");

			// still cancel interval connect
			stop(period, context);
		} else if (!settings.is_warnOnDeactivation()) {
			stop(period, context);
		} else if (period.is_skipped()) {
			// no need for warning, stop will honour / reset skip flag
			stop(period, context);
		} else {
			PowerManager powerManager = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			boolean isScreenOn = powerManager.isScreenOn();

			if (!isScreenOn && settings.is_warnOnlyWhenScreenOn()) {
				// cancel interval connect
				stop(period, context);
			} else {
				if (settings.is_autoDelay()) {
					Log.i(TAG,
							"Auto-delaying scheduled period "
									+ period.toString()
									+ "  because screen is on");

					int delayInSec = settings.get_delay() * 60;

					makeAutoDelayNotification(context, period, settings);
					scheduleSwitchOff(context, delayInSec, ACTION_OFF_DELAYED,
							period);
					
					UserLog.log(context, "Switch-off automatically delayed by " + settings.get_delay() + "min");
				} else {
					Log.d(TAG, "Screen is on: notification.");
					makeDisableNotification(context, period, settings);

					int fewMomentsInSec = 45;
					scheduleSwitchOff(context, fewMomentsInSec, ACTION_OFF,
							period);
				}
			}
		}
	}

	public void stop(int periodId, Context context)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		SharedPreferences sharedPrefs = PersistenceUtils
				.getSchedulesPreferences(context);

		ScheduledPeriod period = PersistenceUtils.getPeriod(sharedPrefs,
				periodId);

		stop(period, context);
	}

	public void stop(ScheduledPeriod period, Context context)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {
		stop(period, context, false);
	}

	public void stop(ScheduledPeriod period, Context context, boolean manualStop)
			throws ClassNotFoundException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException {

		if (period == null) {
			Log.d(TAG, "switchOffNow: Period is null. Assuming deleted");
			return;
		}

		boolean isPeriodActivation = !period.is_enableRadios();

		// only activate the period if it will be ever de-activated by
		// re-starting
		period.set_active(isPeriodActivation && period.is_scheduleStart());

		period.set_overrideIntervalWifi(false);
		period.set_overrideIntervalMob(false);

		boolean skip = period.is_skipped();
		if (!isPeriodActivation || !period.is_scheduleStart()) {
			// reset at the end of the period
			period.set_skipped(false);
			period.set_overrideIntervalMob(false);
			period.set_overrideIntervalWifi(false);
		}

		PersistenceUtils.saveToPreferences(
				PersistenceUtils.getSchedulesPreferences(context), period);

		if (!skip || manualStop) {
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

			deactivateAffectedStartOnlyPeriods(context, period, settings);
		}
	}

	public void cancelSwitchOff(Context context, int periodId) {

		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationManager.cancel(periodId);

		cancelSwitchOff(context, periodId, ACTION_OFF);
		cancelSwitchOff(context, periodId, ACTION_OFF_DELAYED);
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

	public void scheduleSwitchOff(Context context, int seconds,
			String actionName, int periodId) {

		SharedPreferences sharedPrefs = PersistenceUtils
				.getSchedulesPreferences(context);

		ScheduledPeriod period = PersistenceUtils.getPeriod(sharedPrefs,
				periodId);

		if (period == null) {
			Log.d(TAG, "scheduleSwitchOff: Period is null. Assuming deleted");
		} else {
			scheduleSwitchOff(context, seconds, actionName, period);
		}
	}

	public void setupIntervalConnect(Context context, SchedulerSettings settings) {
		try {
			startIntervalConnect(context, settings);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
				.readFromPreferences(PersistenceUtils
						.getSchedulesPreferences(context));

		ScheduledPeriod lastActivatedWifiPeriod = getLastActivatedActivePeriod(
				allPeriods, NetworkType.WiFi);
		ScheduledPeriod lastActivatedMobDataPeriod = getLastActivatedActivePeriod(
				allPeriods, NetworkType.MobileData);

		boolean intervalWifi = lastActivatedWifiPeriod != null
				&& lastActivatedWifiPeriod.is_enableRadios()
				&& lastActivatedWifiPeriod.is_intervalConnectWifi()
				&& !lastActivatedWifiPeriod.is_overrideIntervalWifi();

		boolean intervalMobData = lastActivatedMobDataPeriod != null
				&& lastActivatedMobDataPeriod.is_enableRadios()
				&& lastActivatedMobDataPeriod.is_intervalConnectMobData()
				&& !lastActivatedMobDataPeriod.is_overrideIntervalMob();

		if (!intervalWifi && !intervalMobData) {
			// no relevant active interval-connect period at all
			cancelIntervalConnect(context);
			return;
		}
		
		UserLog.log(TAG, context,
				"Interval switch-on - Wi-Fi: " + intervalWifi + " - Mobile Data: " + intervalMobData);

		Log.d(TAG, "intervalSwitchOn: Interval connect is ON.");
		int connectTimeSec = 60 * settings.get_connectDuration();

		scheduleIntervalSwitchOff(context, connectTimeSec,
				createIntervalSwitchOffExtras(intervalWifi, intervalMobData));

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
			UserLog.log(TAG, context,
					"Interval switch-off skipped (screen is ON and device is unlocked)");

			scheduleIntervalSwitchOff(context, reTestIntervalSec, bundle);
			return;
		}

		if (!isDeviceLocked) {
			// NOTE: if keyguard is not locked and the user switches the screen
			// back on
			// NO user_present broadcast is received! Therefore only switch off
			// if locked.
			UserLog.log(TAG, context,
					"Interval switch-off skipped (keyguard is not yet locked)");
			
			scheduleIntervalSwitchOff(context, reTestIntervalSec, bundle);
			return;
		}

		if (bundle == null) {
			Log.d(TAG, "No bundle");
		}

		boolean intervalWifi = bundle == null
				|| bundle.getBoolean(INTERVAL_WIFI);
		boolean intervalMobData = bundle == null
				|| bundle.getBoolean(INTERVAL_MOBILEDATA);

		if (intervalMobData) {
			ConnectionUtils.toggleMobileData(context, false);
		}

		if (intervalWifi) {
			if (settings.is_keepWifiConnected()
					&& ConnectionUtils.isWifiConnected(context)) {
				UserLog.log(TAG, context,
						"Interval switch-off skipped for Wi-Fi (due to option 'Keep Wi-Fi connected')");
			} else {
				ConnectionUtils.toggleWifi(context, false);
			}
		}
	}

	private void scheduleSwitchOff(Context context, int seconds,
			String actionName, ScheduledPeriod period) {

		PendingIntent pendingIntentOff = getSwitchOffIntent(context, period,
				actionName);

		AlarmUtils.setAlarm(context, pendingIntentOff, seconds);
	}

	private void scheduleIntervalConnect(Context context,
			SchedulerSettings settings) {

		int intervalSeconds = settings.get_connectInterval() * 60;

		PendingIntent intervalOnIntent = getIntervalIntent(context,
				ACTION_INTERVAL_ON, null);

		AlarmUtils.setInexactRepeatingAlarm(context, intervalOnIntent,
				intervalSeconds);
	}

	private void scheduleIntervalSwitchOff(Context context, int connectTimeSec,
			Bundle extras) {

		PendingIntent intervalOffIntent = getIntervalIntent(context,
				ACTION_INTERVAL_OFF, extras);

		AlarmUtils.setAlarm(context, intervalOffIntent, connectTimeSec);
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
				.readFromPreferences(PersistenceUtils
						.getSchedulesPreferences(context));

		boolean intervalConnectRequired = false;
		if (period.is_wifi()) {
			ScheduledPeriod previousActiveWifi = getLastActivatedActivePeriod(
					allPeriods, NetworkType.WiFi);

			boolean enableWifi;
			if (enable) {
				enableWifi = previousActiveWifi == null
						|| previousActiveWifi.is_enableRadios();
			} else {
				enableWifi = previousActiveWifi != null
						&& previousActiveWifi.is_enableRadios();
			}
			ConnectionUtils.toggleWifi(context, enableWifi);

			if (enableWifi && previousActiveWifi != null
					&& previousActiveWifi.is_enableRadios()
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
						|| previousActiveMobData.is_enableRadios();
			} else {
				enableMobData = previousActiveMobData != null
						&& previousActiveMobData.is_enableRadios();
			}

			ConnectionUtils.toggleMobileData(context, enableMobData);

			if (enableMobData && previousActiveMobData != null
					&& previousActiveMobData.is_enableRadios()
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
						|| previousActiveBluetooth.is_enableRadios();
			} else {
				enableBluetooth = previousActiveBluetooth != null
						&& previousActiveBluetooth.is_enableRadios();
			}

			ConnectionUtils.toggleBluetooth(context, enableBluetooth);
		}

		if (period.is_volume()) {
			ScheduledPeriod previousActiveVolume = getLastActivatedActivePeriod(
					allPeriods, NetworkType.Volume);

			boolean enableVolume;
			if (enable) {
				enableVolume = previousActiveVolume == null
						|| previousActiveVolume.is_enableRadios();
			} else {
				enableVolume = previousActiveVolume != null
						&& previousActiveVolume.is_enableRadios();
			}

			ConnectionUtils.toggleVolume(context, enableVolume,
					period.is_vibrateWhenSilent());
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

	private void setAlarm(Context context, ScheduledPeriod period,
			SchedulerSettings settings) throws Exception {

		setNextAlarmStart(context, period, settings);
		setNextAlarmStop(context, period);
	}

	private boolean isChangeRequired(Context context, ScheduledPeriod period) {
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

	private void makeAutoDelayNotification(Context context,
			ScheduledPeriod period, SchedulerSettings settings) {

		ArrayList<String> sensorsToSwitchOff = getConnectionSwitchOffList(
				period, context);

		if (sensorsToSwitchOff.isEmpty()) {
			Log.d(TAG, "No action needed");
			return;
		}

		Bitmap bm = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_launcher);

		// to allow 1h delay by user clicking:
		Intent deactivateNowIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		deactivateNowIntent.setAction(ACTION_SWITCHOFF_DEACTIVATE_NOW);
		deactivateNowIntent.putExtra(INTENT_EXTRA_PERIOD_ID, period.get_id());

		PendingIntent deactivateNowIntentPending = PendingIntent.getBroadcast(
				context, 0, deactivateNowIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		Intent skipIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		skipIntent.setAction(ACTION_SWITCHOFF_SKIP);
		skipIntent.putExtra(INTENT_EXTRA_PERIOD_ID, period.get_id());

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

	private void makeDisableNotification(Context context,
			ScheduledPeriod period, SchedulerSettings settings) {

		String tickerText = getTickerText(period, context);

		if (tickerText == null) {
			Log.d(TAG, "No action needed");
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
		delayIntent.setAction(ACTION_SWITCHOFF_DELAY);
		delayIntent.putExtra(INTENT_EXTRA_PERIOD_ID, period.get_id());

		PendingIntent delayIntentPending = PendingIntent.getBroadcast(context,
				0, delayIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Intent skipIntent = new Intent(context,
				DelayStopBroadcastReceiver.class);

		// delayIntent.putExtra(context.getString(R.string.period_id),
		// periodId);
		skipIntent.setAction(ACTION_SWITCHOFF_SKIP);
		skipIntent.putExtra(INTENT_EXTRA_PERIOD_ID, period.get_id());

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
				.setContentTitle(
						context.getString(R.string.switch_off_notification_title))
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

	private void startIntervalConnect(Context context,
			SchedulerSettings settings) throws ClassNotFoundException,
			NoSuchFieldException, IllegalArgumentException,
			IllegalAccessException, NoSuchMethodException,
			InvocationTargetException {

		scheduleIntervalConnect(context, settings);

		// do the first switch-on right now
		intervalSwitchOn(context, settings);
	}

	private void cancelIntervalConnect(Context context) {

		Log.d(TAG, "cancelIntervalConnect: Cancelling interval connect");

		PendingIntent intervalOnIntent = getIntervalIntent(context,
				ACTION_INTERVAL_ON, null);
		AlarmUtils.cancelAlarm(context, intervalOnIntent);

		PendingIntent intervalOffIntent = getIntervalIntent(context,
				ACTION_INTERVAL_OFF, null);
		AlarmUtils.cancelAlarm(context, intervalOffIntent);
	}

	private void deactivateAffectedStartOnlyPeriods(Context context,
			ScheduledPeriod currentPeriod, SchedulerSettings settings) {

		ArrayList<ScheduledPeriod> allPeriods = PersistenceUtils
				.readFromPreferences(PersistenceUtils
						.getSchedulesPreferences(context));

		if (currentPeriod.is_wifi()) {
			ArrayList<ScheduledPeriod> startedWifiPeriods = getAffectedActiveStartOnlyPeriods(
					allPeriods, NetworkType.WiFi, currentPeriod);

			for (ScheduledPeriod wifiPeriod : startedWifiPeriods) {
				wifiPeriod.set_active(false);
			}
		}

		if (currentPeriod.is_mobileData()) {
			ArrayList<ScheduledPeriod> startedMobPeriods = getAffectedActiveStartOnlyPeriods(
					allPeriods, NetworkType.MobileData, currentPeriod);

			for (ScheduledPeriod mobPeriod : startedMobPeriods) {
				mobPeriod.set_active(false);
			}
		}

		if (currentPeriod.is_bluetooth()) {
			ArrayList<ScheduledPeriod> startedBtPeriods = getAffectedActiveStartOnlyPeriods(
					allPeriods, NetworkType.Bluetooth, currentPeriod);

			for (ScheduledPeriod btPeriod : startedBtPeriods) {
				btPeriod.set_active(false);
			}
		}

		if (currentPeriod.is_volume()) {
			ArrayList<ScheduledPeriod> startedVolPeriods = getAffectedActiveStartOnlyPeriods(
					allPeriods, NetworkType.Volume, currentPeriod);

			for (ScheduledPeriod volPeriod : startedVolPeriods) {
				volPeriod.set_active(false);
			}
		}

		PersistenceUtils.saveToPreferences(
				PersistenceUtils.getSchedulesPreferences(context), allPeriods);
	}

	private ArrayList<ScheduledPeriod> getAffectedActiveStartOnlyPeriods(
			ArrayList<ScheduledPeriod> allPeriods, NetworkType networkType,
			ScheduledPeriod except) {

		ArrayList<ScheduledPeriod> result = new ArrayList<ScheduledPeriod>(
				allPeriods.size());

		for (ScheduledPeriod period : allPeriods) {
			if (!period.is_active()) {
				continue;
			}

			if (period.is_scheduleStop()) {
				continue;
			}

			if (applies(networkType, period)) {
				result.add(period);
			}
		}

		return result;
	}

	private boolean applies(NetworkType networkType, ScheduledPeriod forPeriod) {
		if (networkType == NetworkType.WiFi && !forPeriod.is_wifi()) {
			return false;
		}

		if (networkType == NetworkType.MobileData && !forPeriod.is_mobileData()) {
			return false;
		}

		if (networkType == NetworkType.Bluetooth && !forPeriod.is_bluetooth()) {
			return false;
		}

		if (networkType == NetworkType.Volume && !forPeriod.is_volume()) {
			return false;
		}

		return true;
	}

	private ScheduledPeriod getLastActivatedActivePeriod(
			ArrayList<ScheduledPeriod> allPeriods, NetworkType networkType) {
		return getLastActivatedActivePeriod(allPeriods, networkType, null);
	}

	private ScheduledPeriod getLastActivatedActivePeriod(
			ArrayList<ScheduledPeriod> allPeriods, NetworkType networkType,
			ScheduledPeriod except) {
		ScheduledPeriod result = null;

		for (ScheduledPeriod period : allPeriods) {

			if (!period.is_active()) {
				continue;
			}

			if (!period.is_scheduleStop()) {
				// it's no proper period -> don't use to determine activity
				// forever
				continue;
			}

			if (except != null && except.get_id() == period.get_id()) {
				continue;
			}

			if (!applies(networkType, period)) {
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

	private PendingIntent getPendingIntent(Context context,
			ScheduledPeriod period, boolean start) {

		String action;

		if (start) {
			action = ACTION_START;
		} else {
			action = ACTION_STOP;
		}

		Intent intent = new Intent(context, StartStopBroadcastReceiver.class);

		// to differentiate the intents, otherwise they update each other! NOTE:
		// Extras are not enough!
		intent.setAction(action);

		Bundle bundle = new Bundle();
		bundle.putInt(INTENT_EXTRA_PERIOD_ID, period.get_id());

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

		if (endTimeMillis > 0) {
			bundle.putLong("StopAt", endTimeMillis);
		}

		bundle.putInt(INTENT_EXTRA_PERIOD_ID, periodId);

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

	private Bundle createIntervalSwitchOffExtras(boolean wifi, boolean mobData) {
		Bundle bundle = new Bundle();
		bundle.putBoolean(INTERVAL_WIFI, wifi);
		bundle.putBoolean(INTERVAL_MOBILEDATA, mobData);
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

	private static String join(ArrayList<String> list, String delimiter) {

		StringBuilder sb = new StringBuilder();

		String loopDelim = "";

		for (String s : list) {

			sb.append(loopDelim);
			sb.append(s);

			loopDelim = delimiter;
		}

		return sb.toString();
	}
}
