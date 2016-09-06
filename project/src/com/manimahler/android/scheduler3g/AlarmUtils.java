package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class AlarmUtils {

	private static final String TAG = AlarmUtils.class.getSimpleName();
	

	public static void cancelAlarm(Context context, PendingIntent pendingIntent) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		am.cancel(pendingIntent);
	}

	public static void setExactAlarmSeconds(Context context, PendingIntent pendingIntent,
			int secondsFromNow) {

		long wakeTime = DateTimeUtils.getTimeFromNowInMillis(secondsFromNow);

		setExactAlarmMilliseconds(context, pendingIntent, wakeTime);
	}

	public static void setExactAlarmMilliseconds(Context context, PendingIntent pendingIntent,
			long wakeTime) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		logAlarmWakeUpTime(pendingIntent, wakeTime);

		int alarmType = AlarmManager.RTC_WAKEUP;
		
		// setExact was introduced with kitkat
		// setExactAndAllowWhileIdle was introduced with marshmallow
		if (Build.VERSION.SDK_INT >= 23) {
			am.setExactAndAllowWhileIdle(alarmType, wakeTime, pendingIntent);
		}
		else if (Build.VERSION.SDK_INT >= 19) {
			am.setExact(alarmType, wakeTime, pendingIntent);
		}
		else{
			am.set(alarmType, wakeTime, pendingIntent);
		}
	}
	
	public static void setInexactAlarmSeconds(Context context, PendingIntent pendingIntent,
			int secondsFromNow) {

		long wakeTime = DateTimeUtils.getTimeFromNowInMillis(secondsFromNow);

		setInexactAlarmMilliseconds(context, pendingIntent, wakeTime);
	}
	
	public static void setInexactAlarmMilliseconds(Context context, PendingIntent pendingIntent,
			long wakeTime) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		logAlarmWakeUpTime(pendingIntent, wakeTime);
		
		int alarmType = AlarmManager.RTC_WAKEUP;

		// setAndAllowWhileIdle was introduced with marshmallow
		if (Build.VERSION.SDK_INT >= 23) {
			am.setAndAllowWhileIdle(alarmType, wakeTime, pendingIntent);
		}
		else {
			// On newer phones this is several minutes late!
			am.set(alarmType, wakeTime, pendingIntent);
		}
	}

	private static void logAlarmWakeUpTime(PendingIntent pendingIntent,
			long wakeTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(wakeTime);

		Log.d(TAG, "Setting alarm with intent " + pendingIntent.toString()
				+ " to go off at " + calendar.getTime().toString());
	}
	
	public static void setInexactRepeatingAlarm(Context context,
			PendingIntent pendingIntent, int intervalSeconds) {
		AlarmManager am = (AlarmManager) context
				.getSystemService(android.content.Context.ALARM_SERVICE);

		long firstTimeMillis = DateTimeUtils
				.getTimeFromNowInMillis(intervalSeconds);

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(firstTimeMillis);

		Log.d(TAG, "Setting inexact repeating alarm with intent "
				+ pendingIntent.toString() + " to first go off at "
				+ calendar.getTime().toString() + " and then every "
				+ intervalSeconds + " sec.");

		int intervalMillis = intervalSeconds * 1000;

		am.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstTimeMillis,
				intervalMillis, pendingIntent);
	}

}
