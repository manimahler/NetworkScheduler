package com.manimahler.android.scheduler3g;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class StartStopBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = StartStopBroadcastReceiver.class
			.getSimpleName();
	
	private static final long NOW_FUZZINESS_MILLIS = 120000;

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			String action = intent.getAction();

			Bundle bundle = intent.getExtras();
			long stopTime = bundle.getLong("StopAt", 0);

			// TODO: magic number for default
			int periodId = bundle.getInt(
					NetworkScheduler.INTENT_EXTRA_PERIOD_ID, -2);

			// cancel existing notifications
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			// before switching off, remove notification
			notificationManager.cancel(periodId);

			NetworkScheduler scheduler = new NetworkScheduler();
			SharedPreferences sharedPrefs = PersistenceUtils
					.getSchedulesPreferences(context);

			Log.d(TAG, "Received broadcast action " + action
					+ " for period id " + periodId);

			if (periodId >= 0) {
				UserLog.log(context, String.format(
						"Action %s for period id %s", action, periodId));
			}

			SchedulerSettings settings = PersistenceUtils.readSettings(context);

			// interval-off is now handled by runnable.
			if (action.equals(NetworkScheduler.ACTION_INTERVAL_ON)) {
				scheduler.intervalSwitchOn(context, settings);
			} else if (action.equals(NetworkScheduler.ACTION_OFF)) {
				trySwitchOffConnections(context, periodId, stopTime, false);
			} else if (action.equals(NetworkScheduler.ACTION_OFF_DELAYED)) {
				trySwitchOffConnections(context, periodId, stopTime, true);
			} else {

				boolean on;

				if (action.equals(NetworkScheduler.ACTION_START)) {
					on = true;
				} else if (action.equals(NetworkScheduler.ACTION_STOP)) {
					on = false;
				} else {
					Log.e(TAG, "Unknown action " + action);
					return;
				}

				// normal schedule: test weekday
				ScheduledPeriod period = PersistenceUtils.getPeriod(
						sharedPrefs, periodId);

				if (period == null) {
					// assuming deleted -> no action, no re-scheduling
					Log.w(TAG,
							"Scheduled period not found, assuming deleted. No action.");
					UserLog.log(context,
							"Scheduled time period not found any more. Was it deleted?");
					return;
				}

				// re-start 'repeating' alarm (not using repeating because it
				// has become inexact on kitkat)
				// TODO: on gingerbread, sometimes the current time is not after
				// the stop time!
				// -> alarm is re-set for right now and appears to be received
				// twice!
				if (on) {
					scheduler.setNextAlarmStart(context, period, settings, NOW_FUZZINESS_MILLIS);
				} else {
					scheduler.setNextAlarmStop(context, period, NOW_FUZZINESS_MILLIS);
				}

				if (!period.appliesToday(on, NOW_FUZZINESS_MILLIS)) {
					String actionText = "Scheduled action " + action
							+ " for scheduled period "
							+ period.toString(context)
							+ " does not apply today ";
					Log.i(TAG, actionText);
					UserLog.log(context, actionText);
					return;
				}

				if (!on) {

					scheduler.stopApproved(context, period, settings);
				} else {
					boolean manualActivation = false;
					scheduler.start(period, context, settings, manualActivation);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			UserLog.log(context, "Error starting / stopping radio / volume.", e);
		}
	}

	private void trySwitchOffConnections(Context context, int periodId,
			long expectedStopTime, boolean reWarn) {

		NetworkScheduler scheduler = new NetworkScheduler();
		SharedPreferences sharedPrefs = PersistenceUtils
				.getSchedulesPreferences(context);

		ScheduledPeriod referencedPeriod = PersistenceUtils.getPeriod(
				sharedPrefs, periodId);

		SchedulerSettings settings = PersistenceUtils.readSettings(context);

		if (referencedPeriod == null) {
			// it might have been deleted? Test!
			Log.d(TAG, "Referenced period not found. Not stopping.");

			return;
		}

		if (referencedPeriod.get_endTimeMillis() != expectedStopTime) {
			Log.d(TAG, "Expected stop time has changed. Not stopping.");

			return;
		}

		if (!referencedPeriod.is_schedulingEnabled()) {
			Log.d(TAG, "Scheduling was disabled. Not stopping");

			return;
		}

		try {
			if (reWarn) {
				// TODO: in a delayed switch-off there should be a check if a
				// sensor was
				// not already 'switched on' again by another period and we
				// should drop the switch-off

				// add notification
				scheduler.stopApproved(context, referencedPeriod, settings);
			} else {
				
				// cancel interval connect
				scheduler.stop(referencedPeriod, context);
			}
		} catch (Exception e) {
			e.printStackTrace();

			UserLog.log(context,
					"Error stopping period: " + e.getMessage());
		}
	}
}
