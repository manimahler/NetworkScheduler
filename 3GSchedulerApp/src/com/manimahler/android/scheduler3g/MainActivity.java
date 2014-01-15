package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends FragmentActivity {

	ScheduleSettings _settings;
	
	final static boolean DEBUG = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		SharedPreferences preferences = GetPreferences();

		_settings = new ScheduleSettings(preferences);

		if (_settings.get_startTimeMillis() <= 0) {
			_settings.set_startTimeMillis(System.currentTimeMillis());
		}

		if (_settings.get_endTimeMillis() <= 0) {
			_settings.set_endTimeMillis(System.currentTimeMillis());
		}

		// TimePicker startPicker =
		// (TimePicker)findViewById(R.id.timePickerStart);
		// TimePicker endPicker = (TimePicker)findViewById(R.id.timePickerEnd);

		// setPickerTime(_settings.get_startTimeMillis(), startPicker);
		// setPickerTime(_settings.get_endTimeMillis(), endPicker);

		// OnTimeChangedListener onTimeChangedListener = new
		// TimePicker.OnTimeChangedListener() {
		//
		// @Override
		// public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
		// {
		// setAlarm(mSchedulingEnabled);
		// }
		// };

		// startPicker.setOnTimeChangedListener(onTimeChangedListener);
		// endPicker.setOnTimeChangedListener(onTimeChangedListener);

		// Button startButton = (Button)findViewById(R.id.buttonTimeStart);
		// setButtonTime(_settings.get_startTimeMillis(), startButton);
		//
		// Button stopButton = (Button)findViewById(R.id.buttonTimeStop);
		// setButtonTime(_settings.get_endTimeMillis(), stopButton);

		settingsChanged(false);

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		ToggleButton button = (ToggleButton) findViewById(R.id.dummy_button);

		button.setChecked(_settings.is_schedulingEnabled());
		
		View layout = findViewById(R.id.textViewNextTime);
		
		Animation myFadeInAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fadein);
		
		layout.startAnimation(myFadeInAnimation);
		
//		TransitionDrawable trans = (TransitionDrawable) layout.getBackground();
//		trans.startTransition(2000);
	}

	public void onToggleClicked(View view) {
		// loseTimePickersFocus();

		// Is the toggle on?
		_settings.set_schedulingEnabled(((ToggleButton) view).isChecked());

		setAlarm();
	}

	public void buttonStartClicked(View v) {

        FragmentManager fm = getSupportFragmentManager();
        SchedulePeriodFragment editNameDialog = new SchedulePeriodFragment();
        editNameDialog.show(fm, "fragment_schedule_period");

		
		DialogFragment newFragment = new TimePickerFragment(
				_settings.get_startTimeMillis()) {
			@Override
			public void onTimeSetAndDone(int hourOfDay, int minute) {

				startTimePicked(hourOfDay, minute);
			}
		};

		newFragment.show(getSupportFragmentManager(), "timePickerStart");
	}

	public void buttonStopClicked(View v) {
		DialogFragment newFragment = new TimePickerFragment(
				_settings.get_endTimeMillis()) {
			@Override
			public void onTimeSetAndDone(int hourOfDay, int minute) {
				// Do something with the time chosen by the user
				endTimePicked(hourOfDay, minute);
			}
		};

		newFragment.show(getSupportFragmentManager(), "timePickerStart");
	}

	private void setPickerTime(long timeInMillis, TimePicker picker) {

		// set picker format am/pm vs. 24h
		picker.setIs24HourView(DateFormat.is24HourFormat(this));

		if (timeInMillis <= 0) {
			return;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);

		picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	private void setButtonTime(long timeInMillis, Button button) {

		Calendar calendar = Calendar.getInstance();

		if (timeInMillis <= 0) {
			calendar.setTimeInMillis(System.currentTimeMillis());
		} else {
			calendar.setTimeInMillis(timeInMillis);
		}

		String hour;

		if (DateFormat.is24HourFormat(this)) {
			hour = String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY));
		} else {
			hour = String.format("%02d", calendar.get(Calendar.HOUR));
		}

		String min = String.format("%02d", calendar.get(Calendar.MINUTE));

		String text;

		if (DateFormat.is24HourFormat(this)) {
			text = String.format("%1$s : %2$s", hour, min);
		} else {
			String am_pm;

			if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
				am_pm = "AM";
			} else {
				am_pm = "PM";
			}

			text = String.format("%1$s : %2$s %3$s", hour, min, am_pm);
		}

		button.setText(text);

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.

		// onSaveInstanceState is called e.g. if the orientation is changed and
		// the app needs to be restarted

		// loseTimePickersFocus();

		saveSettings();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// onPause is called e.g. when the back button is pressed
		// loseTimePickersFocus();

		saveSettings();
	}

	private void saveSettings() {
		SharedPreferences preferences = GetPreferences();

		_settings.saveToPreferences(preferences);
	}

	// private void loseTimePickersFocus()
	// {
	// //TimePicker startPicker =
	// (TimePicker)findViewById(R.id.timePickerStart);
	// //startPicker.clearFocus();
	//
	// TimePicker endPicker = (TimePicker)findViewById(R.id.timePickerEnd);
	// endPicker.clearFocus();
	// }

	private long getNextTimeMillisFromPicker(TimePicker timePicker) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());

		Calendar calendarNow = (Calendar) calendar.clone();

		calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
		calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
		calendar.set(Calendar.SECOND, 0);

		if (!calendar.after(calendarNow)) {
			// add 24 hours
			calendar.add(Calendar.HOUR, 24);
		}

		return calendar.getTimeInMillis();
	}



	//
	// private void setAlarm(boolean on) {
	//
	// long startTime = 0;
	// //getNextTimeMillisFromPicker((TimePicker)findViewById(R.id.timePickerStart));
	// long stopTime =
	// getNextTimeMillisFromPicker((TimePicker)findViewById(R.id.timePickerEnd));
	//
	// AlarmHandler alarmHandler = new AlarmHandler();
	//
	// alarmHandler.setAlarm(MainActivity.this, on, startTime, stopTime);
	//
	//
	// Log.d("MainActivity", "Alarm set: " + on);
	//
	// if (on)
	// {
	// Calendar calendar = Calendar.getInstance();
	// calendar.setTimeInMillis(startTime);
	//
	// Log.d("MainActivity", "start Time: " + calendar.toString());
	// }
	// }

	private void setAlarm() {

		AlarmHandler alarmHandler = new AlarmHandler();

		alarmHandler.setAlarm(MainActivity.this, _settings);

		Log.d("MainActivity", "Alarm set: " + _settings.is_schedulingEnabled());

		
		if (_settings.is_schedulingEnabled()) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(_settings.get_startTimeMillis());

			Log.d("MainActivity", "start Time: " + calendar.toString());

			calendar.setTimeInMillis(_settings.get_endTimeMillis());

			Log.d("MainActivity", "stop Time: " + calendar.toString());
		}
	}

	private SharedPreferences GetPreferences() {
		AlarmHandler alarmHandler = new AlarmHandler();

		return alarmHandler.GetPreferences(MainActivity.this);
	}

	private void startTimePicked(int hourOfDay, int minute) {

		long nextStartTimeInMillis = DateTimeUtils.getNextTimeIn24hInMillis(hourOfDay, minute);

		_settings.set_startTimeMillis(nextStartTimeInMillis);

		settingsChanged(true);
	}

	private void endTimePicked(int hourOfDay, int minute) {

		long nextEndTimeInMillis = DateTimeUtils.getNextTimeIn24hInMillis(hourOfDay, minute);

		_settings.set_endTimeMillis(nextEndTimeInMillis);

		settingsChanged(true);
	}

	private void settingsChanged(boolean setAlarm) {
		Button startButton = (Button) findViewById(R.id.buttonTimeStart);
		setButtonTime(_settings.get_startTimeMillis(), (Button) startButton);

		Button stopButton = (Button) findViewById(R.id.buttonTimeStop);
		setButtonTime(_settings.get_endTimeMillis(), (Button) stopButton);

		saveSettings();

		if (setAlarm) {
			setAlarm();
		}
	}
}
