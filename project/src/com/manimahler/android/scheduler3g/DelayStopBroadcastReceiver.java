package com.manimahler.android.scheduler3g;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class DelayStopBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = DelayStopBroadcastReceiver.class
			.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {

		int periodId = intent.getExtras().getInt(
				NetworkScheduler.INTENT_EXTRA_PERIOD_ID, -4);

		String action = intent.getAction();
		Log.d(TAG, "Received delay broadcast for action " + action
				+ " and period id " + periodId);

		NetworkScheduler scheduler = new NetworkScheduler();
		scheduler.cancelSwitchOff(context, periodId);

		if (action.equals(NetworkScheduler.ACTION_SWITCHOFF_SKIP)) {
			Toast.makeText(context,
					context.getString(R.string.switch_off_skipped_today),
					Toast.LENGTH_LONG).show();
			UserLog.log(context, "Switch-off was skipped");
			// do nothing
			
			return;
		}

		if (action.equals(NetworkScheduler.ACTION_SWITCHOFF_DEACTIVATE_NOW)) {
			try {
				scheduler.stop(periodId, context);
				showSwitchOffToast(context);
				UserLog.log(context, "Switch-off initiated from notification");
			} catch (Exception e) {
				Log.e(TAG, "Error deactivating connection", e);
				UserLog.log(context, "Error deactivating connection from notification", e);
				
				Toast.makeText(context,
						"Error deactivating network connection",
						Toast.LENGTH_LONG).show();
			}
			return;
		}

		if (action.equals(NetworkScheduler.ACTION_SWITCHOFF_DELAY)) {
			try {
				SchedulerSettings settings = PersistenceUtils
						.readSettings(context);

				int delayInSec = settings.get_delay() * 60;

				scheduler.scheduleSwitchOff(context, delayInSec, NetworkScheduler.ACTION_OFF_DELAYED,
						periodId);

				showDelayToast(context, delayInSec);
				UserLog.log(context, "Switch-off delayed by " + settings.get_delay() + "min");
				
			} catch (Exception e) {
				Log.e(TAG, "Error delaying switch off", e);
				UserLog.log(context, "Error delaying switch off", e);
			}
		} else {
			Log.e(TAG, "Unknown action");
		}
	}

	private void showSwitchOffToast(Context context) {
		String toastText = context.getString(R.string.connections_switched_off);

		Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
	}

	private void showDelayToast(Context context, int delayInSec) {
		long delayTime = DateTimeUtils.getTimeFromNowInMillis(delayInSec);

		String timeFormat = DateFormat.getTimeFormat(context).format(
				new Date(delayTime));
		String toastText = String.format(
				context.getString(R.string.switch_off_delayed_until),
				timeFormat);

		Toast.makeText(context, toastText, Toast.LENGTH_LONG).show();
	}
}
