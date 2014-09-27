package com.manimahler.android.scheduler3g;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.Log;

public class DateTimeUtils {
	
	private static final String TAG = DateTimeUtils.class.getSimpleName();

	public static long getTimeFromNowInMillis(int secondsFromNow) {
		int delayMillis = secondsFromNow * 1000;

		long result = System.currentTimeMillis() + delayMillis;

		return result;
	}
	
	public static long getNextTimeIn24hInMillis(int hourOfDay, int minute) {
		
		long considerNowWithinMillis = 0;
		
		return getNextTimeIn24hInMillis(hourOfDay, minute, considerNowWithinMillis);
	}

	public static long getNextTimeIn24hInMillis(int hourOfDay, int minute,
			long considerNowWithinMillis) {
		
		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTimeInMillis(System.currentTimeMillis());
		
		Calendar calendar = setTime(hourOfDay, minute);
		boolean now = considerAsNow(calendar, considerNowWithinMillis, calendarNow);
		
		// avoid missing / duplicate events issues for example:
		// - hourOfDay, minute is midnight, current time just before 
		//	 -> last midnight + 24 results in this midnight (which might have passed by then)
		// - hourOfDay, minute is just before current time, by adding another day we miss this event!
		
		// Midnight issue: Early arrival of alarm
		// --------------------------------------
		// - milliseconds : 0:00 -> hourOfDay 0, minute 0
		// - current time 23:59:59.99
		// -> calendar is yesterday at midnight (0:00) -> 24h before now, 48h before desired time!
		//    -> check before first, add 1 day, check now afterwards, add another day if needed
		
		if (!now && calendar.before(calendarNow)) {
			// NOTE: add 1 day rather than 24 hours because of summer time
			// change -> + 1 d is 23 or 25 hours!
			addDays(calendar, 1);
		}
		
		// Midnight issue: Late arrival of alarm
		// -------------------------------------
		// - milliseconds: 23:59:59.99 -> hourOfDay 23, minute 59
		// - current time 0:00 (arriving late) 
		// -> calendar is is already today at 23:59 -> not now, but in ca. 24h -> correct, do nothing
		
		// General issues: slightly early / late arrival: handled by now-fuzziness
		
		if (calendar.before(calendarNow) || now) {
			// NOTE: add 1 day rather than 24 hours because of summer time
			// change -> + 1 d is 23 or 25 hours!
			addDays(calendar, 1);
		}

		return calendar.getTimeInMillis();
	}

	
	public static long getNextTimeIn24hInMillis(long milliseconds, long considerNowWithinMillis) {
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTimeInMillis(milliseconds);
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		
		return getNextTimeIn24hInMillis(hour, minute, considerNowWithinMillis);
	}

	public static long getCurrentOrPreviousTimeIn24hInMillis(int hourOfDay, int minute,
			long considerNowWithinMillis) {

		Calendar calendarNow = Calendar.getInstance();
		calendarNow.setTimeInMillis(System.currentTimeMillis());

		Calendar calendar = setTime(hourOfDay, minute);

		// it seems that starting with 4.3 'now' is sometimes (a couple of
		// millis) earlier than the set time!
		// we just want to make sure a delayed alarm (on kitkat) is not skipped
		// because the wrong day is taken
		
		// NOTE: considerNowWithinMillis must be > 1 minute because the resolution is only minutes!
		// 5 minutes:
		
		boolean considerSameTime = considerAsNow(calendar, considerNowWithinMillis, calendarNow);
		
		// Situation 1:
		// -----------
		// - hourOfDay == 0, minute == 0
		// - current time: 23:59:59.99 (arriving early, just the day before we wanted)
		//   -> considerSameTime == false (calendar is 24h ahead, add 1 day)
		
		// Situation 2:
		// -----------
		// - hourOfDay == 23, minute == 59
		// - current time: 00:00:00 (arriving late, the day after we wanted it)
		//   -> considerSameTime == false (calendar is 24h behind, subtract 1 day)
		if (! considerSameTime) {
			
			if (calendar.before(calendarNow))
			{
				Log.w(TAG, "Provided time is before now. Adding one day. Hour: " + hourOfDay + " Minute: " + minute);
				addDays(calendar, 1);
			}
			else
			{
				Log.w(TAG, "Provided time is after now. Subtracting one day. Hour: " + hourOfDay + " Minute: " + minute);
				// subtract one day
				addDays(calendar, -1);	
			}
		}

		return calendar.getTimeInMillis();
	}

	private static boolean considerAsNow(Calendar calendar, long considerNowWithinMillis,
			Calendar calendarNow) {
		
		long absDifference = Math.abs(calendar.getTimeInMillis()
				- calendarNow.getTimeInMillis());

		boolean considerSameTime = absDifference <= considerNowWithinMillis;
		
		return considerSameTime;
	}

	public static long getCurrentOrPreviousTimeIn24hInMillis(long milliseconds,
			long considerNowWithinMillis) {
		Calendar calendar = Calendar.getInstance();

		calendar.setTimeInMillis(milliseconds);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		return getCurrentOrPreviousTimeIn24hInMillis(hour, minute, considerNowWithinMillis);
	}

	public static boolean isEarlierInTheDay(long millis, long thanMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);

		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		Calendar compareCalendar = Calendar.getInstance();
		compareCalendar.setTimeInMillis(thanMillis);

		int compareHour = compareCalendar.get(Calendar.HOUR_OF_DAY);
		int compareMinute = compareCalendar.get(Calendar.MINUTE);

		if (hour < compareHour) {
			return true;
		}

		if (hour == compareHour) {
			if (minute < compareMinute) {
				return true;
			}
		}

		return false;
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

		for (int i = 0; i < result.length; i++) {
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

	public static String getWeekdaysText(Context context, boolean[] weekDays,
			String everyday, String never) {

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
		for (int d = 0; d < consecutiveDays.length; d++) {
			if (consecutiveDays[d] > 0) {
				add(resultBuilder, d, consecutiveDays[d], shortWeekdays);
			}
		}

		if (resultBuilder.length() == 0) {
			return never;
		}

		return resultBuilder.toString();
	}
	
	public static void addDays(Calendar calendar, int days) {
		// NOTE: 1 day <> 24 hours when changing between summer / winter time
		calendar.add(Calendar.DATE, days);
	}
	
	public static long addDays(long millis, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);
		
		addDays(calendar, days);
		
		return calendar.getTimeInMillis();
	}
	
	private static Calendar setTime(int hourOfDay, int minute) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		calendar.set(Calendar.MINUTE, minute);
		calendar.set(Calendar.SECOND, 0);

		return calendar;
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

		if (count > 1) {
			int endIndex = dayIndex + count - 1;

			add(String.format("%1$s - %2$s", shortWeekdays[dayIndex],
					shortWeekdays[endIndex % weekLength]), toString);
		} else {
			add(String.format("%1$s", shortWeekdays[dayIndex]), toString);

		}
	}

	private static void add(String addition, StringBuilder toString) {
		if (toString.length() > 0) {
			toString.append(", ");
		}

		toString.append(addition);
	}
}
