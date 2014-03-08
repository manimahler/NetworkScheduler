package com.manimahler.android.scheduler3g;

import java.util.Date;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

public class DelayStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		int periodId = intent.getExtras().getInt(
				context.getString(R.string.period_id), -4);

		String action = intent.getAction();
		Log.d("DelayStopBroadcastReceiver",
				"Received delay broadcast for action " + action
						+ " and period id " + periodId);
		
		NetworkScheduler scheduler = new NetworkScheduler();
		scheduler.cancelSwitchOff(context, periodId);
		
		if (action.equals("SKIP")) {
			Toast.makeText(context,
					context.getString(R.string.switch_off_skipped_today),
					Toast.LENGTH_LONG).show();
			// do nothing
			return;
		}

		if (action.equals("DEACTIVATE")) {
			try {
				scheduler.deactivate(periodId, context);
				showSwitchOffToast(context);
			} catch (Exception e) {
				Log.e("DelayStopBroadcastReceiver",
						"Error delaying switch off", e);
				Toast.makeText(context,
						"Error deactivating network connection",
						Toast.LENGTH_LONG).show();
			}
			return;
		}

		if (action.equals("DELAY")) {
			try {
				SchedulerSettings settings = PersistenceUtils
						.readSettings(context);

				int delayInSec = settings.get_delay() * 60;

				scheduler.scheduleSwitchOff(context, delayInSec, "OFF_DELAYED",
						periodId);

				showDelayToast(context, delayInSec);
			} catch (Exception e) {
				Log.e("DelayStopBroadcastReceiver",
						"Error delaying switch off", e);
			}
		} else {
			Log.e("DelayStopBroadcastReceiver", "Unknown action");
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
