package com.manimahler.android.scheduler3g;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateFormat;

public class DateTimeUtils {

	public static long getTimeFromNowInMillis(int secondsFromNow)
	{
		int delayMillis = secondsFromNow * 1000;

		long result = System.currentTimeMillis() + delayMillis;
		
		return result;
	}
	
	public static long getNextTimeIn24hInMillis(int hourOfDay, int minute) {

		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTimeInMillis(System.currentTimeMillis());

		Calendar calendar = setTime(hourOfDay, minute);

		if (!calendar.after(calendarNow)) {
			// add 24 hours
			calendar.add(Calendar.HOUR, 24);
		}

		return calendar.getTimeInMillis();
	}

	public static long getNextTimeIn24hInMillis(long milliseconds) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(milliseconds);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		return getNextTimeIn24hInMillis(hour, minute);
	}

	public static long getPreviousTimeIn24hInMillis(int hourOfDay, int minute) {

		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTimeInMillis(System.currentTimeMillis());
		
		Calendar calendar = setTime(hourOfDay, minute);
		
		// it seems that starting with 4.3 'now' is sometimes (a couple of millis) earlier than the set time!
		// we just want to make sure a delayed alarm (on kitkat) is not skipped because the wrong
		// day is taken
		
		// TODO: adapt to inexact repeating
		long maxDifferenceConsideredSameDay = 600000; // 10 min
		
		long absDifference = Math.abs(calendar.getTimeInMillis() - calendarNow.getTimeInMillis());
		
		if (absDifference > maxDifferenceConsideredSameDay)
		{
			// add -24 hours
			calendar.add(Calendar.HOUR, -24);
		}

		return calendar.getTimeInMillis();
	}

	public static long getPreviousTimeIn24hInMillis(long milliseconds) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(milliseconds);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		return getPreviousTimeIn24hInMillis(hour, minute);
	}
	
	public static boolean isEarlierInTheDay(long millis, long thanMillis)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		Calendar compareCalendar = Calendar.getInstance();
		compareCalendar.setTimeInMillis(thanMillis);
		
		int compareHour = compareCalendar.get(Calendar.HOUR_OF_DAY);
		int compareMinute = compareCalendar.get(Calendar.MINUTE);
		
		if (hour < compareHour)
		{
			return true;
		}
		
		if (hour == compareHour) {
			if (minute < compareMinute) {
				return true;
			}
		}
		
		return false;
	}

	private static Calendar setTime(int hourOfDay, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		return calendar;
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

	public static String[] getShortWeekdays(Context context) {
		DateFormatSymbols dateFormatSymbols = new DateFormatSymbols();
		String[] weekdays = dateFormatSymbols.getShortWeekdays();

		int firstdDay = Calendar.getInstance().getFirstDayOfWeek();
		int weekLength = 7;

		String[] result = new String[7];

		int resultIndex = 0;

		// NOTE: this approach has issues when changing the locale (first day)
		// use official index for storing the results?

		for (int i = firstdDay; i < firstdDay + weekLength; i++) {
			int dayIndex;
			if (i <= 7) {
				dayIndex = i;
			} else {
				dayIndex = i % weekLength;
			}

			String day = weekdays[dayIndex];

			if (day != null && !day.isEmpty()) {
				result[resultIndex] = day;

				resultIndex++;
			}
		}
		
		Locale locale = context.getResources().getConfiguration().locale;
		
		for (int i = 0; i < result.length; i++)
		{
			result[i] = result[i].toUpperCase(locale);
		}

		return result;
	}

	public static int getWeekdayIndex(long milliseconds) throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(milliseconds);

		int weekday = calendar.get(Calendar.DAY_OF_WEEK);

		// quick and dirty:
		int firstDay = Calendar.getInstance().getFirstDayOfWeek();

		if (firstDay == 1) {
			return weekday - 1;
		}

		int weekLength = 7;
		if (firstDay == 2) {
			return (weekday + weekLength - 2) % weekLength;
		} else {
			throw new Exception("Unexpected first day of the week");
		}
	}

	public static int getWeekdayIndexOfToday() throws Exception {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		int weekday = calendar.get(Calendar.DAY_OF_WEEK);

		// quick and dirty:
		int firstDay = Calendar.getInstance().getFirstDayOfWeek();

		if (firstDay == 1) {
			return weekday - 1;
		}

		int weekLength = 7;
		if (firstDay == 2) {
			return (weekday + weekLength - 2) % weekLength;
		} else {
			throw new Exception("Unexpected first day of the week");
		}
	}

	public static String getWeekdaysText(Context context, boolean[] weekDays, String everyday,
			String never) {

		StringBuilder resultBuilder = new StringBuilder();
		String[] shortWeekdays = getShortWeekdays(context);

		// get the first false value to start with
		int start = 0;
		while (start < weekDays.length && weekDays[start]) {
			start++;
		}

		if (start == weekDays.length) {
			return everyday;
		}
		
		int[] consecutiveDays = new int[weekDays.length];
		
		int consecutiveCount = 0;
		for (int i = start + 1; i <= start + weekDays.length; i++) {
			int dayIdx = i % weekDays.length;

			if (weekDays[dayIdx]) {
				consecutiveCount++;
			} else if (consecutiveCount > 0) {
				add(consecutiveDays, i - 1, consecutiveCount, shortWeekdays);
				consecutiveCount = 0;
			}
		}
		
		// now we can have an ordered list of short days
		for (int d = 0; d < consecutiveDays.length; d++)
		{
			if (consecutiveDays[d] > 0)
			{
				add(resultBuilder, d, consecutiveDays[d], shortWeekdays);
			}
		}
		
		if (resultBuilder.length() == 0)
		{
			return never;
		}

		return resultBuilder.toString();
	}

	private static void add(int[] dayCount, int dayIndex, int previous,
			String[] shortWeekdays) {

		int minConsecutiveLength = 3;
		int weekLength = 7;

		int startDayIdx = dayIndex - previous + 1;
		
		if (previous >= minConsecutiveLength) {
			dayCount[startDayIdx % weekLength] = previous;
		} else {
			for (int i = startDayIdx; i <= dayIndex; i++) {
				dayCount[i % weekLength] = 1;
			}
		}
	}
	
	private static void add(StringBuilder toString, int dayIndex, int count,
			String[] shortWeekdays) {

		int weekLength = shortWeekdays.length;
		
		if (count > 1)
		{
			int endIndex = dayIndex + count - 1;
			
			add(String.format("%1$s - %2$s", 
					shortWeekdays[dayIndex], 
					shortWeekdays[endIndex % weekLength]),
					toString);
		}
		else
		{
			add(String.format("%1$s", shortWeekdays[dayIndex]), toString);

		}
		
//		
//		int minConsecutiveLength = 3;
//		int weekLength = 7;
//
//		int startDayIdx = dayIndex - previous + 1;
//
//		if (previous >= minConsecutiveLength) {
//			add(String.format("%1$s - %2$s", 
//					shortWeekdays[startDayIdx % weekLength], 
//					shortWeekdays[dayIndex % weekLength]),
//					toString);
//		} else {
//			for (int i = startDayIdx; i <= dayIndex; i++) {
//				add(String.format("%1$s", shortWeekdays[i % weekLength]),
//						toString);
//			}
//		}
	}

	//
	// public static String getWeekdaysText(boolean[] weekDays, String everyday,
	// String never) {
	//
	// StringBuilder resultBuilder = new StringBuilder();
	//
	// String[] shortWeekdays = getShortWeekdays();
	//
	// boolean[] consecutiveDays = getConsecutiveEntries(weekDays, 3);
	//
	// int start = -1;
	//
	// int end = -1;
	//
	// for (int i = 0; i < weekDays.length; i++) {
	// int previous = (i + 6) % weekDays.length;
	// if (consecutiveDays[i] != consecutiveDays[previous]) {
	// if (weekDays[i]) {
	// // starting
	// start = i;
	// } else {
	// // stopping - add to string
	// if (start >= 0) {
	// add(String.format("%1$s - %2$s",
	// shortWeekdays[start], shortWeekdays[previous]),
	// resultBuilder);
	// } else {
	// // except for range across end-start
	// end = i;
	// }
	// }
	// } else if (weekDays[i] && !consecutiveDays[i]) {
	// // single non-consecutive value, add to string
	// add(String.format("%1$s", shortWeekdays[i]), resultBuilder);
	// }
	// }
	//
	// // range across end/start
	// if (start > 0 && end > 0) {
	// add(String.format("%1$s - %2$s, ", shortWeekdays[start],
	// shortWeekdays[end]), resultBuilder);
	// }
	//
	// String result = resultBuilder.toString();
	//
	// if (result.isEmpty() && start < 0 && end < 0) {
	// // no change - always or never
	// if (weekDays[0]) {
	// // result = context.getString(R.string.everyday);
	// result = everyday;
	// } else {
	// result = never;
	// }
	// }
	//
	// return result;
	// }

	private static void add(String addition, StringBuilder toString) {
		if (toString.length() > 0) {
			toString.append(", ");
		}

		toString.append(addition);
	}
//
//	/**
//	 * Gets an array with those entries being true that are consecutive true
//	 * elements in the input list.
//	 * 
//	 * @param list
//	 *            of boolean values
//	 * @return
//	 */
//	/**
//	 * Gets an array with those entries being true that are consecutive true
//	 * elements in the input list.
//	 * 
//	 * @param list
//	 *            of boolean values
//	 * @param minLength
//	 *            of consecutive values to be considered worth grouping
//	 * @return
//	 */
//	private static boolean[] getConsecutiveEntries(boolean[] list, int minLength) {
//		// Otherwise a single value is considered a consecutive group
//		if (minLength < 2) {
//			throw new IllegalArgumentException("minLength must be 2 or greater");
//		}
//
//		// result array is initialized with <false> values
//		boolean[] result = new boolean[list.length];
//
//		boolean consecutive = false;
//		int start = -1;
//
//		for (int i = 1; i < list.length + minLength; i++) {
//
//			// go over the end of the array to finish consecutive entries
//			// started at the end
//			if (i >= list.length && !list[list.length - 1]) {
//				continue;
//			}
//
//			int currentIndex = i % list.length;
//			int previousIndex = (i - 1) % list.length;
//
//			boolean stop = false;
//
//			if (list[previousIndex] && list[currentIndex]) {
//				if (!consecutive) {
//					consecutive = true;
//					start = previousIndex;
//				}
//			} else if (consecutive) {
//				// assuming weekDays[i] is false here!
//				stop = true;
//			}
//
//			// if at the end of the consecutive range or at the end of the loop
//			if (stop || i + 1 == list.length + minLength) {
//
//				// if the consecutive length is enough
//				if (start >= 0 && i - minLength >= start) {
//					// stop the group, assign <true> to all consecutive entries
//					for (int j = start; j < i; j++) {
//						result[j % list.length] = true;
//					}
//				}
//				consecutive = false;
//			}
//
//		}
//
//		return result;
//	}

}
