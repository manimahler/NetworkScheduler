package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class ScheduledPeriod {

	private static final String TAG = ScheduledPeriod.class.getSimpleName();

	private static final String PERIOD_ID = "ID";
	private static final String NAME = "NAME";
	private static final String END_TIME = "EndTime";
	private static final String START_TIME = "StartTime";
	private static final String SCHEDULING_ENABLED = "SchedulingEnabled";
	private static final String SCHEDULE_START = "ScheduleStart";
	private static final String SCHEDULE_STOP = "ScheduleStop";
	private static final String ENABLE_RADIOS = "EnableRadios";

	private static final String BLUETOOTH = "BLUETOOTH";
	private static final String WIFI = "WIFI";
	private static final String MOBILE_DATA = "MOBILE_DATA";
	private static final String VOLUME = "VOLUME";

	private static final String WEEK_DAYS = "WeekDays";

	private static final String INTERVAL_CONNECT_WIFI = "IntervalConnectWifi";
	private static final String INTERVAL_CONNECT_MOB = "IntervalConnectMob";
	private static final String INTERVAL_CONNECT_BT = "IntervalConnectBt";
	private static final String VIBRATE_WHEN_SILENT = "VibrateWhenSilent";

	private static final String ACTIVE = "Active";

	private static final String SKIPPED = "Skipped";
	private static final String OVERRIDE_WIFI = "OverrideIntervalWifi";
	private static final String OVERRIDE_MOB = "OverrideIntervalMobData";

	private int _id;
	private String _name;
	private boolean _schedulingEnabled;
	private long _startTimeMillis;
	private long _endTimeMillis;
	private boolean _scheduleStart;
	private boolean _scheduleStop;
	private boolean _enableRadios;

	private boolean _mobileData;
	private boolean _wifi;
	private boolean _bluetooth;
	private boolean _volume;

	private boolean _intervalConnectWifi;
	private boolean _intervalConnectMobData;
	private boolean _intervalConnectBluetooth;
	private boolean _vibrateWhenSilent;

	private boolean[] _weekDays;

	private boolean _active;

	private boolean _skipped;
	private boolean _overrideIntervalWifi;
	private boolean _overrideIntervalMob;

	public ScheduledPeriod(Bundle bundle) {

		_id = bundle.getInt(PERIOD_ID);

		_name = bundle.getString(NAME);

		_schedulingEnabled = bundle.getBoolean(SCHEDULING_ENABLED, true);

		_scheduleStart = bundle.getBoolean(SCHEDULE_START, true);
		_scheduleStop = bundle.getBoolean(SCHEDULE_STOP, true);
		_startTimeMillis = bundle.getLong(START_TIME, 0);
		_endTimeMillis = bundle.getLong(END_TIME, 0);

		_enableRadios = bundle.getBoolean(ENABLE_RADIOS, true);

		_mobileData = bundle.getBoolean(MOBILE_DATA, true);
		_wifi = bundle.getBoolean(WIFI, false);
		_bluetooth = bundle.getBoolean(BLUETOOTH, false);
		_volume = bundle.getBoolean(VOLUME, false);

		_weekDays = bundle.getBooleanArray(WEEK_DAYS);

		if (_weekDays == null) {
			_weekDays = new boolean[7];
		}

		_intervalConnectWifi = bundle.getBoolean(INTERVAL_CONNECT_WIFI, false);
		_intervalConnectMobData = bundle
				.getBoolean(INTERVAL_CONNECT_MOB, false);
		_intervalConnectBluetooth = bundle.getBoolean(INTERVAL_CONNECT_BT,
				false);
		_vibrateWhenSilent = bundle.getBoolean(VIBRATE_WHEN_SILENT, false);

		_active = bundle.getBoolean(ACTIVE, false);

		_skipped = bundle.getBoolean(SKIPPED, false);
		_overrideIntervalWifi = bundle.getBoolean(OVERRIDE_WIFI, false);
		_overrideIntervalMob = bundle.getBoolean(OVERRIDE_MOB, false);
	}

	public ScheduledPeriod(SharedPreferences preferences, String qualifier, int persistenceVersion) {
		_id = preferences.getInt(PERIOD_ID + qualifier, -1);

		_name = preferences.getString(NAME + qualifier, "");

		_schedulingEnabled = preferences.getBoolean(SCHEDULING_ENABLED
				+ qualifier, true);
		_scheduleStart = preferences.getBoolean(SCHEDULE_START + qualifier,
				true);
		_scheduleStop = preferences.getBoolean(SCHEDULE_STOP + qualifier, true);

		_startTimeMillis = preferences.getLong(START_TIME + qualifier, 0);
		_endTimeMillis = preferences.getLong(END_TIME + qualifier, 0);

		_enableRadios = preferences.getBoolean(ENABLE_RADIOS + qualifier, true);

		_mobileData = preferences.getBoolean(MOBILE_DATA + qualifier, true);
		_wifi = preferences.getBoolean(WIFI + qualifier, false);
		_bluetooth = preferences.getBoolean(BLUETOOTH + qualifier, false);
		_volume = preferences.getBoolean(VOLUME + qualifier, false);

		_weekDays = new boolean[7];

		for (int i = 0; i < 7; i++) {
			_weekDays[i] = preferences.getBoolean(WEEK_DAYS + qualifier + "_"
					+ i, false);
		}

		if (persistenceVersion == 0) {
			// Starting with app version 19, we start the boolean array on SUNDAY
			// Correct legacy storage using first day of week
			int firstDay = Calendar.getInstance().getFirstDayOfWeek();

			int firstDayOffset = firstDay - 1;

			boolean[] correctedWeekDays = new boolean[7];
			for (int i = 0; i < 7; i++) {

				int correctedIndex = (i + firstDayOffset) % 7;
				correctedWeekDays[correctedIndex] = _weekDays[i];
			}

			_weekDays = correctedWeekDays;
		}

		_intervalConnectWifi = preferences.getBoolean(INTERVAL_CONNECT_WIFI
				+ qualifier, false);
		_intervalConnectMobData = preferences.getBoolean(INTERVAL_CONNECT_MOB
				+ qualifier, false);
		_intervalConnectBluetooth = preferences.getBoolean(INTERVAL_CONNECT_BT
				+ qualifier, false);
		_vibrateWhenSilent = preferences.getBoolean(VIBRATE_WHEN_SILENT
				+ qualifier, false);

		_active = preferences.getBoolean(ACTIVE + qualifier, false);

		_skipped = preferences.getBoolean(SKIPPED + qualifier, false);
		_overrideIntervalWifi = preferences.getBoolean(OVERRIDE_WIFI
				+ qualifier, false);
		_overrideIntervalMob = preferences.getBoolean(OVERRIDE_MOB + qualifier,
				false);
	}

	public ScheduledPeriod(boolean schedulingEnabled, long startTimeMillis,
			long endTimeMillis, boolean[] weekDays) {
		_id = -1;

		_name = "";

		_scheduleStart = true;
		_scheduleStop = true;

		_schedulingEnabled = schedulingEnabled;
		_startTimeMillis = startTimeMillis;
		_endTimeMillis = endTimeMillis;

		_enableRadios = true;

		_weekDays = weekDays;

		_mobileData = true;
		_wifi = true;
		_bluetooth = false;
		_volume = false;

		_intervalConnectWifi = false;
		_intervalConnectMobData = false;
		_intervalConnectBluetooth = false;

		// or calculate right here if within period?
		try {
			_active = isActiveNow();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void set_id(int id) {
		this._id = id;
	}

	public int get_id() {
		return _id;
	}

	public String get_name() {
		return _name;
	}

	public void set_name(String _name) {
		this._name = _name;
	}

	public boolean is_scheduleStart() {
		return _scheduleStart;
	}

	public void set_scheduleStart(boolean _scheduleStart) {
		this._scheduleStart = _scheduleStart;
	}

	public boolean is_scheduleStop() {
		return _scheduleStop;
	}

	public void set_scheduleStop(boolean _scheduleStop) {
		this._scheduleStop = _scheduleStop;
	}

	public boolean is_schedulingEnabled() {
		return _schedulingEnabled;
	}

	public void set_schedulingEnabled(boolean schedulingEnabled) {
		this._schedulingEnabled = schedulingEnabled;
	}

	public long get_startTimeMillis() {
		return _startTimeMillis;
	}

	public void set_startTimeMillis(long startTimeMillis) {
		this._startTimeMillis = startTimeMillis;
	}

	public long get_endTimeMillis() {
		return _endTimeMillis;
	}

	public void set_endTimeMillis(long endTimeMillis) {
		this._endTimeMillis = endTimeMillis;
	}

	public boolean is_enableRadios() {
		return _enableRadios;
	}

	public void set_enableRadios(boolean _enableRadios) {
		this._enableRadios = _enableRadios;
	}

	public boolean[] get_weekDays() {
		return _weekDays;
	}

	public void set_weekDays(boolean[] weekDays) {
		this._weekDays = weekDays;
	}

	public boolean is_mobileData() {
		return _mobileData;
	}

	public void set_mobileData(boolean _mobileData) {
		this._mobileData = _mobileData;
	}

	public boolean is_wifi() {
		return _wifi;
	}

	public void set_wifi(boolean _wifi) {
		this._wifi = _wifi;
	}

	public boolean is_bluetooth() {
		return _bluetooth;
	}

	public void set_bluetooth(boolean _bluetooth) {
		this._bluetooth = _bluetooth;
	}

	public boolean is_volume() {
		return _volume;
	}

	public void set_volume(boolean _volume) {
		this._volume = _volume;
	}

	public boolean is_intervalConnectWifi() {
		return _intervalConnectWifi && is_enableRadios();
	}

	public void set_intervalConnectWifi(boolean _intervalConnectWifi) {
		this._intervalConnectWifi = _intervalConnectWifi;
	}

	public boolean is_intervalConnectMobData() {
		return _intervalConnectMobData && is_enableRadios();
	}

	public void set_intervalConnectMobData(boolean intervalConnectMobData) {
		this._intervalConnectMobData = intervalConnectMobData;
	}

	public boolean is_intervalConnectBluetooth() {
		return _intervalConnectBluetooth && is_enableRadios();
	}

	public void set_intervalConnectBluetooth(boolean intervalConnect) {
		this._intervalConnectBluetooth = intervalConnect;
	}

	public boolean is_vibrateWhenSilent() {
		return _vibrateWhenSilent;
	}

	public void set_vibrateWhenSilent(boolean _vibrateWhenSilent) {
		this._vibrateWhenSilent = _vibrateWhenSilent;
	}

	public boolean is_active() {
		return _active;
	}

	public void set_active(boolean _active) {
		this._active = _active;
	}

	public boolean is_skipped() {
		return _skipped;
	}

	public void set_skipped(boolean _skipped) {
		this._skipped = _skipped;
	}

	public boolean is_overrideIntervalWifi() {
		return _overrideIntervalWifi;
	}

	public void set_overrideIntervalWifi(boolean _userOverride) {
		this._overrideIntervalWifi = _userOverride;
	}

	public boolean is_overrideIntervalMob() {
		return _overrideIntervalMob;
	}

	public void set_overrideIntervalMob(boolean _overrideIntervalMob) {
		this._overrideIntervalMob = _overrideIntervalMob;
	}

	public long getActivationTimeMillis() {
		if (is_enableRadios()) {
			return _startTimeMillis;
		} else {
			return _endTimeMillis;
		}

	}

	public boolean useIntervalConnect() {
		return isIntervalConnectingWifi() || isIntervalConnectingMobileData() ||
				isIntervalConnectingBluetooth();
	}

	public boolean isIntervalConnectingWifi() {
		return (is_wifi() && is_intervalConnectWifi() && is_active() && is_schedulingEnabled());
	}

	public boolean isIntervalConnectingMobileData() {
		return (is_mobileData() && is_intervalConnectMobData() && is_active() && is_schedulingEnabled());
	}
	
	public boolean isIntervalConnectingBluetooth() {
		return (is_bluetooth() && is_intervalConnectBluetooth() && is_active() && is_schedulingEnabled());
	}

	public void saveToPreferences(SharedPreferences.Editor editor,
			String qualifier) {
		editor.putInt(PERIOD_ID + qualifier, _id);
		editor.putString(NAME + qualifier, _name);

		editor.putBoolean(SCHEDULE_START + qualifier, _scheduleStart);
		editor.putBoolean(SCHEDULE_STOP + qualifier, _scheduleStop);

		editor.putBoolean(SCHEDULING_ENABLED + qualifier, _schedulingEnabled);
		editor.putLong(START_TIME + qualifier, _startTimeMillis);
		editor.putLong(END_TIME + qualifier, _endTimeMillis);

		editor.putBoolean(ENABLE_RADIOS + qualifier, _enableRadios);

		Log.d(TAG, "EnabledPeriod: Saving network settings...");

		editor.putBoolean(MOBILE_DATA + qualifier, _mobileData);
		editor.putBoolean(WIFI + qualifier, _wifi);
		editor.putBoolean(BLUETOOTH + qualifier, _bluetooth);
		editor.putBoolean(VOLUME + qualifier, _volume);

		Log.d(TAG, "EnabledPeriod: Saving week days...");
		for (int i = 0; i < 7; i++) {
			editor.putBoolean(WEEK_DAYS + qualifier + "_" + i, _weekDays[i]);
		}

		editor.putBoolean(INTERVAL_CONNECT_WIFI + qualifier,
				_intervalConnectWifi);
		editor.putBoolean(INTERVAL_CONNECT_MOB + qualifier,
				_intervalConnectMobData);
		editor.putBoolean(INTERVAL_CONNECT_BT + qualifier,
				_intervalConnectBluetooth);
		editor.putBoolean(VIBRATE_WHEN_SILENT + qualifier, _vibrateWhenSilent);

		editor.putBoolean(ACTIVE + qualifier, _active);

		editor.putBoolean(SKIPPED + qualifier, _skipped);
		editor.putBoolean(OVERRIDE_WIFI + qualifier, _overrideIntervalWifi);
		editor.putBoolean(OVERRIDE_MOB + qualifier, _overrideIntervalMob);
	}

	public void saveToBundle(Bundle bundle) {
		bundle.putInt(PERIOD_ID, _id);
		bundle.putString(NAME, _name);

		bundle.putBoolean(SCHEDULE_START, _scheduleStart);
		bundle.putBoolean(SCHEDULE_STOP, _scheduleStop);

		bundle.putLong(START_TIME, _startTimeMillis);
		bundle.putLong(END_TIME, _endTimeMillis);
		bundle.putBoolean(SCHEDULING_ENABLED, _schedulingEnabled);
		bundle.putLong(START_TIME, _startTimeMillis);
		bundle.putLong(END_TIME, _endTimeMillis);

		bundle.putBoolean(ENABLE_RADIOS, _enableRadios);

		bundle.putBoolean(MOBILE_DATA, _mobileData);
		bundle.putBoolean(WIFI, _wifi);
		bundle.putBoolean(BLUETOOTH, _bluetooth);
		bundle.putBoolean(VOLUME, _volume);

		bundle.putBooleanArray(WEEK_DAYS, _weekDays);

		bundle.putBoolean(INTERVAL_CONNECT_WIFI, _intervalConnectWifi);
		bundle.putBoolean(INTERVAL_CONNECT_MOB, _intervalConnectMobData);
		bundle.putBoolean(INTERVAL_CONNECT_BT, _intervalConnectBluetooth);
		bundle.putBoolean(VIBRATE_WHEN_SILENT, _vibrateWhenSilent);

		bundle.putBoolean(ACTIVE, _active);

		bundle.putBoolean(SKIPPED, _skipped);
		bundle.putBoolean(OVERRIDE_WIFI, _overrideIntervalWifi);
		bundle.putBoolean(OVERRIDE_MOB, _overrideIntervalMob);
	}

	public boolean isActiveNow() throws Exception {

		return isActiveAt(System.currentTimeMillis());
	}

	public boolean isActiveAt(long timeMillis) throws Exception {
		Calendar checkTime = Calendar.getInstance();
		checkTime.setTimeInMillis(timeMillis);

		Calendar lastActivation = getPreviousActivation(timeMillis);

		if (lastActivation == null) {
			return false; // never activated
		}

		// check if the previous start was applicable on the actual day it would
		// have happened
		if (!isOnActiveWeekday(lastActivation.getTimeInMillis())) {
			return false;
		}

		// now the last start happened, check if it hasn't already stopped
		Calendar lastDeactivation;
		if (is_enableRadios()) {
			lastDeactivation = getPreviousHourMinuteInMillis(timeMillis,
					get_endTimeMillis());
		} else if (!is_scheduleStart()) {
			// only ever de-activating
			return false;
		} else {
			lastDeactivation = getPreviousHourMinuteInMillis(timeMillis,
					get_startTimeMillis());
		}

		// case 1: last stop is after last start (but of course before the check
		// time)
		if (lastDeactivation.after(lastActivation)) {
			// it has already stopped: false (if applicable)
			if (deactivationOnNextDay()) {
				// the relevant active week day is the day before
				DateTimeUtils.addDays(lastDeactivation, -1);
			}

			long relevantMillis = lastDeactivation.getTimeInMillis();

			return !isOnActiveWeekday(relevantMillis);
		}

		// case 2: last stop is before the last start, i.e. after check time
		// today
		// -> still running
		return true;
	}

	public boolean deactivationOnNextDay() {
		if (is_enableRadios()) {
			return !startIsBeforeStop();
		} else {
			return startIsBeforeStop();
		}
	}

	public Calendar getLastScheduledActivationTime() {

		long now = System.currentTimeMillis();

		Calendar lastActivation = getPreviousActivation(now);

		return lastActivation;
	}

	public boolean startIsBeforeStop() {
		boolean result;

		if (is_scheduleStart() && is_scheduleStop()) {
			result = DateTimeUtils.isEarlierInTheDay(_startTimeMillis,
					_endTimeMillis);
		} else if (is_scheduleStart() || is_scheduleStop()) {
			result = is_scheduleStart();
		} else {
			// no start, no stop
			result = true;
		}

		return result;
	}

	public boolean appliesToday(boolean enable, long considerNowWithinMillis)
			throws Exception {

		// do not use today's time but the official end time because the
		// broadcast might arrive late (esp. with inexact repeating on kitkat)

		long alarmTime;

		Log.d(TAG, "enable: " + enable);

		boolean isPeriodActivation;
		if (is_enableRadios()) {
			isPeriodActivation = enable;
		} else {
			isPeriodActivation = !enable;
		}

		if (enable) {
			alarmTime = get_startTimeMillis();
		} else {
			alarmTime = get_endTimeMillis();
		}

		// TODO: contains fuzziness regarding midnight - remove duplication with
		// isOnActiveWeekday
		long actualAlarmTime = DateTimeUtils
				.getCurrentOrPreviousTimeIn24hInMillis(alarmTime,
						considerNowWithinMillis);

		Log.d(TAG, "actualAlarmTime: " + actualAlarmTime);

		if (!isPeriodActivation && deactivationOnNextDay()) {
			// subtract a day, as the relevant day was yesterday

			actualAlarmTime = DateTimeUtils.addDays(actualAlarmTime, -1);
			Log.d(TAG, "subtracted 1 day: " + actualAlarmTime);
		}

		int weekdayIndex = DateTimeUtils.getWeekdayIndex(actualAlarmTime);

		Log.d(TAG, "weekdayIndex: " + weekdayIndex);

		return (get_weekDays()[weekdayIndex]);
	}

	public boolean hasStartAndStopTime() {
		return _scheduleStart && _scheduleStop;
	}

	public String toString(Context context) {
		String name;
		if (_name == null) {
			name = "<no name>";
		} else {
			name = _name;
		}
		String start;
		if (is_scheduleStart()) {
			start = DateTimeUtils.getHourMinuteText(context,
					get_startTimeMillis());
		} else {
			start = "<no start>";
		}

		String end;

		if (is_scheduleStop()) {
			end = DateTimeUtils.getHourMinuteText(context, get_endTimeMillis());
		} else {
			end = "<no stop>";
		}

		if (is_enableRadios()) {
			return String.format("%s enabled between %s and %s", name, start,
					end);
		} else {
			return String.format(
					"%s between %s (stopping) and %s (re-starting)", name, end,
					start);
		}

	}

	public String toString() {
		String name;
		if (_name == null) {
			name = "<no name>";
		} else {
			name = _name;
		}

		return name + " - ID: " + _id;
	}

	private Calendar getPreviousActivation(long beforeTimeMillis) {
		Calendar lastActivation;
		if (is_enableRadios()) {

			if (!is_scheduleStart()) {
				lastActivation = null;
			} else {
				lastActivation = getPreviousHourMinuteInMillis(
						beforeTimeMillis, get_startTimeMillis());
			}
		} else {

			if (!is_scheduleStop()) {
				lastActivation = null;
			} else {
				lastActivation = getPreviousHourMinuteInMillis(
						beforeTimeMillis, get_endTimeMillis());
			}
		}

		return lastActivation;
	}

	private Calendar getPreviousHourMinuteInMillis(long timeMillis,
			long hourMinuteMillis) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(hourMinuteMillis);

		int startHour = calendar.get(Calendar.HOUR_OF_DAY);
		int startMinute = calendar.get(Calendar.MINUTE);

		Calendar calendarAt = Calendar.getInstance();
		calendarAt.setTimeInMillis(timeMillis);

		calendarAt.set(Calendar.HOUR_OF_DAY, startHour);
		calendarAt.set(Calendar.MINUTE, startMinute);
		calendarAt.set(Calendar.SECOND, 0);

		if (calendarAt.getTimeInMillis() > timeMillis) {
			// the previous occurrence is the day before
			DateTimeUtils.addDays(calendarAt, -1);
		}

		return calendarAt;
	}

	private boolean isOnActiveWeekday(long timeInMillis) throws Exception {

		int weekdayIndex = DateTimeUtils.getWeekdayIndex(timeInMillis);

		return (get_weekDays()[weekdayIndex]);
	}

}
