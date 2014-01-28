package com.manimahler.android.scheduler3g;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.content.Context;
import android.text.format.DateFormat;

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
	
	public static String getHourMinuteText(Context context, long milliseconds) {
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(milliseconds);
		
		return getHourMinuteText(context, calendar);
	}
	
	public static String getHourMinuteText(Context context, Calendar calendar) {
		
		String hour;

		if (DateFormat.is24HourFormat(context)) {
			hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
		} else {
			hour = String.format("%02d", calendar.get(Calendar.HOUR));
		}

		String min = String.format("%02d", calendar.get(Calendar.MINUTE));

		String text;
		
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
		
		String[] ampmStrings = dateFormatSymbols.getAmPmStrings();
		
		if (DateFormat.is24HourFormat(context)) {
			text = String.format("%1$s:%2$s", hour, min);
		} else {
			String am_pm = ampmStrings[calendar.get(Calendar.AM_PM)];

			text = String.format("%1$s:%2$s %3$s", hour, min, am_pm);
		}
		return text;
	}
	
	public static String[] getShortWeekdays()
	{
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
        String[] weekdays = dateFormatSymbols.getShortWeekdays();
        
        int firstdDay = Calendar.getInstance().getFirstDayOfWeek();
        int weekLength = 7;
        
        String[] result = new String[7];
        
        int resultIndex = 0;
        
        // NOTE: this approach has issues when changing the locale (first day)
        // use official index for storing the results?
        
		for (int i = firstdDay; i < firstdDay + weekLength; i++)
        {
			int dayIndex;
			if (i <= 7)
			{
				dayIndex = i;
			}
			else
			{
				dayIndex = i % weekLength;
			}
            
			String day = weekdays[dayIndex];
			
			if (day != null && !day.isEmpty())
			{
				result[resultIndex] = day;
				
				resultIndex++;
			}            
        }
		
		return result;
	}
	
	public static int getWeekdayIndexOfToday() throws Exception
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		
		// quick and dirty:
		int firstDay = Calendar.getInstance().getFirstDayOfWeek();
		
		if (firstDay == 1)
		{
			return weekday - 1;
		}
		
		int weekLength = 7;
		if (firstDay == 2)
		{
			return (weekday + weekLength - 2) % weekLength;
		}
		else
		{
		throw new Exception("Unexpected first day of the week");
		//calendar.get()
		}
		//Calendar.getInstance().get(Calendar.)
	}
	
}
