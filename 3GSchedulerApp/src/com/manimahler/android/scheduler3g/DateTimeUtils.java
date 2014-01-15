package com.manimahler.android.scheduler3g;

import java.util.Calendar;

public class DateTimeUtils {
	
	public static long getNextTimeIn24hInMillis(int hourOfDay, int minute) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Calendar calendarNow = (Calendar) calendar.clone();

		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		if (!calendar.after(calendarNow)) {
			// add 24 hours
			calendar.add(Calendar.HOUR, 24);
		}

		return calendar.getTimeInMillis();
	}

	public static long getNextTimeIn24hInMillis(long milliseconds)
	{
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(milliseconds);
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		return getNextTimeIn24hInMillis(hour, minute);
	}
}
