package com.manimahler.android.scheduler3g.test;

import java.util.Calendar;

import com.manimahler.android.scheduler3g.DateTimeUtils;
import com.manimahler.android.scheduler3g.ScheduledPeriod;

import junit.framework.TestCase;

public class EnabledPeriodTest extends TestCase {

	public final void testIsCurrentlyActiveOnActiveDay() throws Exception {

		Calendar calendar = Calendar.getInstance();

		// Tuesday, 25. Feb. 2014
		int feb = 1; // MONTH is 0-based!
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, feb);
		calendar.set(Calendar.DAY_OF_MONTH, 25);
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		long start = calendar.getTimeInMillis();
		
		calendar.set(Calendar.HOUR_OF_DAY, 16);
		long stop = calendar.getTimeInMillis();
		
		boolean[] days = new boolean[7];
		days[1] = true;
		
		ScheduledPeriod period = new ScheduledPeriod(true, start, stop, days);
		
		// current time: before start:
		calendar.set(Calendar.HOUR_OF_DAY, 4);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		assertTrue(period.isActiveAt(calendar.getTimeInMillis()));
		
		calendar.set(Calendar.HOUR_OF_DAY, 17);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		// same thing, one week later:
		int mar = 2; // MONTH is 0-based!
		calendar.set(Calendar.MONTH, mar);
		calendar.set(Calendar.DAY_OF_MONTH, 4);
		calendar.set(Calendar.HOUR_OF_DAY, 4);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		assertTrue(period.isActiveAt(calendar.getTimeInMillis()));
		
		calendar.set(Calendar.HOUR_OF_DAY, 17);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		
		// on a different day (monday)
		// same thing, one week later:
		calendar.set(Calendar.DAY_OF_MONTH, 10);
		calendar.set(Calendar.HOUR_OF_DAY, 4);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		calendar.set(Calendar.HOUR_OF_DAY, 17);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
	}
	
	public final void testIsCurrentlyActiveMidnightCrossingPeriod() throws Exception {

		Calendar calendar = Calendar.getInstance();

		// Tuesday, 25. Feb. 2014
		int feb = 1; // MONTH is 0-based!
		calendar.set(Calendar.YEAR, 2014);
		calendar.set(Calendar.MONTH, feb);
		calendar.set(Calendar.DAY_OF_MONTH, 25);
		calendar.set(Calendar.HOUR_OF_DAY, 22);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		
		long start = calendar.getTimeInMillis();
		
		calendar.set(Calendar.DAY_OF_MONTH, 26);
		calendar.set(Calendar.HOUR_OF_DAY, 7);
		long stop = calendar.getTimeInMillis();
		
		boolean[] days = new boolean[7];
		days[1] = true;
		
		ScheduledPeriod period = new ScheduledPeriod(true, start, stop, days);
		
		// current time: after start, 26.th:
		calendar.set(Calendar.HOUR_OF_DAY, 4);
		assertTrue(period.isActiveAt(calendar.getTimeInMillis()));
		
		// no switch-off on wednesday morning
		calendar.set(Calendar.HOUR_OF_DAY, 8);
		assertTrue(period.isActiveAt(calendar.getTimeInMillis()));
		
		// add wednesday
		days[2] = true;
		// switch-off already happened
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
		
		// current time: after start, 25.th:
		calendar.set(Calendar.DAY_OF_MONTH, 25);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		assertTrue(period.isActiveAt(calendar.getTimeInMillis()));
		
		// before start, 25th
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		assertFalse(period.isActiveAt(calendar.getTimeInMillis()));
	}

}
