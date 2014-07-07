package com.manimahler.android.scheduler3g;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class AutostartNotifyReceiver extends BroadcastReceiver {

	private static final String TAG = AutostartNotifyReceiver.class
			.getSimpleName();

	private final String BOOT_COMPLETED_ACTION = "android.intent.action.BOOT_COMPLETED";

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			if (intent.getAction().equals(BOOT_COMPLETED_ACTION)) {

				RestartAlarmAfterBoot(context);

				Log.i(TAG,
						"All time periods were re-scheduled after device was booted");

				UserLog.log(context,
						"Device was booted - time periods re-scheduled");
			}
		} catch (Exception e) {
			Log.e(TAG,
					"Error starting Network Scheduler after device was booted",
					e);

			UserLog.log(context,
					"Error re-scheduling time periods after re-boot.", e);
		}
	}

	public void RestartAlarmAfterBoot(Context context) {
		NetworkScheduler networkScheduler = new NetworkScheduler();

		SharedPreferences prefs = PersistenceUtils
				.getSchedulesPreferences(context);

		ArrayList<ScheduledPeriod> enabledPeriods = PersistenceUtils
				.readFromPreferences(prefs);

		SchedulerSettings settings = PersistenceUtils.readSettings(context);

		networkScheduler.setAlarms(context, enabledPeriods, settings);
	}
}
