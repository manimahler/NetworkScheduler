package com.manimahler.android.scheduler3g;

import java.lang.reflect.InvocationTargetException;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

public class StartStopBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		try {
			String action = intent.getAction();

			Bundle bundle = intent.getExtras();
			long stopTime = bundle.getLong("StopAt");

			// TODO: magic number for default
			int periodId = bundle.getInt(context.getString(R.string.period_id),
					-2);
			
			Log.d("StartStopBroadcastReceiver", "Received broadcast action "
					+ action + " for period id " + periodId);
			
			// cancel existing notifications
			NotificationManager notificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			// before switching off, remove notification
			notificationManager.cancel(periodId);

			// do not use == for string comparison in Java!
			if (action.equals("OFF")) {
				trySwitchOffConnections(context, periodId, stopTime, false);
			} else if (action.equals("OFF_DELAYED")) {
				trySwitchOffConnections(context, periodId, stopTime, true);
			} else {

				// normal schedule: test weekday
				NetworkScheduler scheduler = new NetworkScheduler();
				SharedPreferences sharedPrefs = scheduler.getSchedulesPreferences(context);

				EnabledPeriod referencedPeriod = PersistenceUtils.getPeriod(
						sharedPrefs, periodId);
				
				SchedulerSettings settings = PersistenceUtils.readSettings(context);
				
				boolean on = bundle.getBoolean(context
						.getString(R.string.action_3g_on));
				
				if (! appliesToday(referencedPeriod, on))
				{
					Log.d("StartStopBroadcastReceiver", "action does not apply today ");
					return;
				}
				
				if (!on && settings.is_warnOnDeactivation()) {
					
					switchOff(context, referencedPeriod, settings);
				} else {
					ConnectionUtils.toggleNetworkState(context, referencedPeriod, on);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Toast.makeText(context, "Error changing 3g setting",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private boolean appliesToday(EnabledPeriod referencedPeriod, boolean enable) throws Exception {
		
		// do not use todays time but the official end time because the
		// broadcast might arrive late (esp. with inexact repeating on kitkat)
		
		long alarmTime;
		
		Log.d("StartStopReceiver", "enable: " + enable);
		
		if (enable)
		{
			alarmTime = referencedPeriod.get_startTimeMillis();
		}
		else
		{
			alarmTime = referencedPeriod.get_endTimeMillis();
		}
		
		long actualAlarmTime = DateTimeUtils.getPreviousTimeIn24hInMillis(alarmTime);
		
		Log.d("StartStopReceiver", "actualAlarmTime: " + actualAlarmTime);
		
		int weekdayIndex = DateTimeUtils.getWeekdayIndex(actualAlarmTime);
		
		Log.d("StartStopReceiver", "weekdayIndex: " + weekdayIndex);
		
		return (referencedPeriod.get_weekDays()[weekdayIndex]);
	}
	
	private void switchOff(Context context, EnabledPeriod period, SchedulerSettings settings) throws ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		
		
		NetworkScheduler scheduler = new NetworkScheduler();
		
		if (! scheduler.isSwitchOffRequired(context, period))
		{
			Log.d("StartStopReceiver", "No action required.");
			
			return;
		}
		
		boolean enableFalse = false;
		if (! settings.is_warnOnDeactivation())
		{
			ConnectionUtils.toggleNetworkState(context, period, enableFalse);
		}
		
		PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        
		if (! isScreenOn && settings.is_warnOnlyWhenScreenOn())
		{
			ConnectionUtils.toggleNetworkState(context, period, enableFalse);
		}
		else
		{
			if (settings.is_autoDelay())
			{
				Log.d("StartStopReceiver", "Screen is on: auto-delay.");
				
				int delayInSec = settings.get_delay() * 60;
				
				scheduler.makeAutoDelayNotification(context, period, settings);
				scheduler.scheduleSwitchOff(context, delayInSec, "OFF_DELAYED", period);
			}
			else
			{
				Log.d("StartStopReceiver", "Screen is on: notification.");
				scheduler.makeDisableNotification(context, period, settings);
				
				int fewMomentsInSec = 45;
				scheduler.scheduleSwitchOff(context, fewMomentsInSec, "OFF", period);
			}
		}
	}
	
	private void trySwitchOffConnections(Context context, int periodId,
			long expectedStopTime, boolean reWarn) {

		NetworkScheduler scheduler = new NetworkScheduler();
		SharedPreferences sharedPrefs = scheduler.getSchedulesPreferences(context);

		EnabledPeriod referencedPeriod = PersistenceUtils.getPeriod(
				sharedPrefs, periodId);
		
		SchedulerSettings settings = PersistenceUtils.readSettings(context);

		if (referencedPeriod == null) {
			// it might have been deleted? Test!
			Log.d("SwitchOff", "Referenced period not found. Not stopping.");

			return;
		}

		if (referencedPeriod.get_endTimeMillis() != expectedStopTime) {
			Log.d("SwitchOff", "Expected stop time has changed. Not stopping.");

			return;
		}

		if (!referencedPeriod.is_schedulingEnabled()) {
			Log.d("SwitchOff", "Scheduling was disabled. Not stopping");
			
			return;
		}
		
		try {
			if (reWarn) {
				// TODO: in a delayed switch-off there should be a check if a sensor was
				//       not already 'switched on' again by another period and we should drop the switch-off
				
				// add notification
				switchOff(context, referencedPeriod, settings);
			} else {
				
				ConnectionUtils.toggleNetworkState(context, referencedPeriod, false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Toast.makeText(context, "Error changing 3g setting",
					Toast.LENGTH_SHORT).show();
		}
	}
}
