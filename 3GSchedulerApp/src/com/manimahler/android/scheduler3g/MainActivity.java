package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends Activity {
	private static final String END_TIME = "EndTime";

	private static final String START_TIME = "StartTime";

	private static final String SCHEDULING_ENABLED = "SchedulingEnabled";

	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = false;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

	private boolean mSchedulingEnabled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		
		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		
		// Set the values of the UI
		mSchedulingEnabled = preferences.getBoolean(SCHEDULING_ENABLED, false);
		
		long startTimeMillis = preferences.getLong(START_TIME, 0);
		long endTimeMillis = preferences.getLong(END_TIME, 0);
		
		TimePicker startPicker = (TimePicker)findViewById(R.id.timePickerStart);
		TimePicker endPicker = (TimePicker)findViewById(R.id.timePickerEnd);
		
		setPickerTime(startTimeMillis, startPicker);
		setPickerTime(endTimeMillis, endPicker);
		
		
		OnTimeChangedListener onTimeChangedListener = new TimePicker.OnTimeChangedListener() {
			
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				setAlarm(mSchedulingEnabled);
			}
		};
		
		startPicker.setOnTimeChangedListener(onTimeChangedListener);
		endPicker.setOnTimeChangedListener(onTimeChangedListener);
		
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		ToggleButton button = (ToggleButton)findViewById(R.id.dummy_button);
		
		button.setChecked(mSchedulingEnabled);
	}
	
	public void onToggleClicked(View view) {
		loseTimePickersFocus();

	    // Is the toggle on?
		mSchedulingEnabled = ((ToggleButton) view).isChecked();
	    
		setAlarm(mSchedulingEnabled);
	}

	private void setPickerTime(long timeInMillis, TimePicker picker) {
		
		// set picker format am/pm vs. 24h
		picker.setIs24HourView(DateFormat.is24HourFormat(this));
		
		if (timeInMillis <= 0)
		{
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timeInMillis);
		
		picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}
	
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  
	  // onSaveInstanceState is called e.g. if the orientation is changed and the app needs to be restarted
	  
	  loseTimePickersFocus();

	  saveState();	  
	}
	
	@Override
	protected void onPause() 
	{
	  super.onPause();
	  
	  // onPause is called e.g. when the back button is pressed
	  loseTimePickersFocus();

	  saveState();
	}

	private void saveState() {

		// Store values between instances here

		SharedPreferences preferences = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();

		long startTime = getNextTimeMillisFromPicker((TimePicker) findViewById(R.id.timePickerStart));
		long stopTime = getNextTimeMillisFromPicker((TimePicker) findViewById(R.id.timePickerEnd));

		editor.putBoolean(SCHEDULING_ENABLED, mSchedulingEnabled);
		editor.putLong(START_TIME, startTime);
		editor.putLong(END_TIME, stopTime);

		// Commit to storage
		editor.commit();
	}

	private void loseTimePickersFocus()
	{
		TimePicker startPicker = (TimePicker)findViewById(R.id.timePickerStart);
		startPicker.clearFocus();
		
		TimePicker endPicker = (TimePicker)findViewById(R.id.timePickerEnd);
		endPicker.clearFocus();
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			//mSystemUiHider.hide();
		}
	};

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	private long getNextTimeMillisFromPicker(TimePicker timePicker) {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		
		Calendar calendarNow = (Calendar)calendar.clone();
		
		calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
		calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
		
		if (!calendar.after(calendarNow))
		{
			// add 24 hours
			calendar.add(Calendar.HOUR, 24);
		}
		
		return calendar.getTimeInMillis();
	}

	private PendingIntent getPendingIntent(boolean enable3g) {
		
		String action;
		
		if (enable3g)
		{
			action = "Enabling 3G";
		}
		else
		{
			action = "Disabling 3G";
		}
		
		Intent intent= new Intent(MainActivity.this, StartStopBroadcastReceiver.class);
		
		// to differentiate the intents, otherwise they update each other! NOTE: Extras are not enough!
		intent.setAction(action);

		Bundle bundle = new Bundle();
		bundle.putBoolean("Action3gOn", enable3g);

		intent.putExtras(bundle);

		// using service because getActivity needs API level 16
		PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, 
				intent, Intent.FLAG_ACTIVITY_NEW_TASK);
		return pendingIntent;
	}

	private void setAlarm(boolean on) {

		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

		PendingIntent pendingIntentOn = getPendingIntent(true);
		PendingIntent pendingIntentOff = getPendingIntent(false);
		
		if (on)
		{		
			long startTime = getNextTimeMillisFromPicker((TimePicker)findViewById(R.id.timePickerStart));
			long stopTime = getNextTimeMillisFromPicker((TimePicker)findViewById(R.id.timePickerEnd));
			
			long interval24h = 24 * 60 * 60 * 1000;
			
			am.setRepeating(AlarmManager.RTC_WAKEUP, startTime, interval24h, pendingIntentOn);
			am.setRepeating(AlarmManager.RTC_WAKEUP, stopTime, interval24h, pendingIntentOff);
		}
		else
		{
			// cancel 
			am.cancel(pendingIntentOn);
			am.cancel(pendingIntentOff);
		}
	}
}
