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
				
				SchedulerSettings settings = PersistenceUtils.readSettings(context);
				
				if (! settings.is_globalOn()) {
					// do nothing
					
					UserLog.log(context, "Network Scheduler is off - no time periods scheduled.");
					return;
				}

				restartAlarmAfterBoot(context, settings);

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

	public boolean restartAlarmAfterBoot(Context context, SchedulerSettings settings) {
		
		NetworkScheduler networkScheduler = new NetworkScheduler();
		
		SharedPreferences prefs = PersistenceUtils
				.getSchedulesPreferences(context);

		ArrayList<ScheduledPeriod> enabledPeriods = PersistenceUtils
				.readFromPreferences(prefs);

		networkScheduler.setAlarms(context, enabledPeriods, settings);
		
		return true;
	}
}
