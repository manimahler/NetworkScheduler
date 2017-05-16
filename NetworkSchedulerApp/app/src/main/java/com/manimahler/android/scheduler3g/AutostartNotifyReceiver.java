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
				
				// just to be sure to be persistent - who knows if we got the disconnection on shutdown
				PersistenceUtils.saveBluetoothState(context, false);
				
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
	

	public boolean restartAlarmAfterBoot(Context context, SchedulerSettings settings) throws Exception {
		
		NetworkScheduler networkScheduler = new NetworkScheduler();
		
		SharedPreferences prefs = PersistenceUtils
				.getSchedulesPreferences(context);

		ArrayList<ScheduledPeriod> enabledPeriods = PersistenceUtils
				.readFromPreferences(prefs);
		
		// update the active property on the periods
		for (ScheduledPeriod period : enabledPeriods) {
			if (period.is_scheduleStart() && period.is_scheduleStop() 
					&& period.is_schedulingEnabled()
					&& period.isActiveNow()) {
				
				networkScheduler.toggleActivation(context, period, true, settings, false);
			}
			else
			{
				// might have been active on shutdown:
				period.set_active(false);
			}
		}

		PersistenceUtils.saveToPreferences(prefs, enabledPeriods);
		
		networkScheduler.setAlarms(context, enabledPeriods, settings);
		
		return true;
	}
}
