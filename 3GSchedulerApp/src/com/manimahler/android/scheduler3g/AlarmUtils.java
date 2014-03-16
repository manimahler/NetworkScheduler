package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

public class AlarmUtils {

	public static void cancelAlarm(Context context, PendingIntent pendingIntent) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		am.cancel(pendingIntent);
	}

	public static void setAlarm(Context context, PendingIntent pendingIntent,
			int secondsFromNow) {

		long wakeTime = DateTimeUtils.getTimeFromNowInMillis(secondsFromNow);

		setAlarm(context, pendingIntent, wakeTime);
	}

	public static void setAlarm(Context context, PendingIntent pendingIntent,
			long wakeTime) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(wakeTime);

		Log.d("AlarmHandler", "Setting alarm with intent " + pendingIntent.toString()
				+ " to go off at " + calendar.getTime().toString());

		am.set(AlarmManager.RTC_WAKEUP, wakeTime, pendingIntent);
	}
	
	public static void setInexactRepeatingAlarm(Context context, PendingIntent pendingIntent,
			int intervalSeconds)
	{
		AlarmManager am = (AlarmManager) context
		.getSystemService(android.content.Context.ALARM_SERVICE);
		
		long firstTimeMillis = DateTimeUtils.getTimeFromNowInMillis(intervalSeconds);
				
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(firstTimeMillis);

		Log.d("AlarmHandler", "Setting inexact repeating alarm with intent " + pendingIntent.toString()
				+ " to first go off at " + calendar.getTime().toString() + " and then every " + intervalSeconds + " sec.");
		
		int intervalMillis = intervalSeconds * 1000;
		
		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstTimeMillis, intervalMillis, pendingIntent);
	}
	
}
