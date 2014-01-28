package com.manimahler.android.scheduler3g;

import java.util.ArrayList;
import java.util.Calendar;

import com.manimahler.android.scheduler3g.SchedulePeriodFragment.OnPeriodUpdatedListener;
import com.manimahler.android.scheduler3g.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends FragmentActivity implements
		OnPeriodUpdatedListener {

	ScheduleSettings _settings;

	ArrayList<EnabledPeriod> _enabledPeriods;

	private PeriodListAdapter adapter;

	final static boolean DEBUG = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// getWindow().setFormat(PixelFormat.RGBA_8888);

		setContentView(R.layout.activity_main);

		SharedPreferences preferences = GetPreferences();

		_enabledPeriods = PersistenceUtils.readFromPreferences(preferences);

		final ListView listview = (ListView) findViewById(R.id.listview);

		this.registerForContextMenu(listview);

		//listview.setClickable(true);
		//listview.setLongClickable(true);

		// EnabledPeriod[] values = new EnabledPeriod[2];
		//
		// values[0] = new EnabledPeriod(preferences);
		// values[1] = new EnabledPeriod(preferences);

		// final ArrayList<EnabledPeriod> list = new ArrayList<EnabledPeriod>();
		// for (int i = 0; i < values.length; ++i) {
		//
		// EnabledPeriod period = values[i];
		// period.set_id(i);
		// list.add(period);
		// }

		adapter = new PeriodListAdapter(MainActivity.this, _enabledPeriods);

		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				
				Log.d("MainActivity.AdapterView.OnItemClickListener", "Item clicked at " + position);

				EnabledPeriod item = adapter.getItem(position);

				showPeriodDetails(item);

				// ((Button)view.findViewById(android.R.id.button1)).setBackgroundResource(R.drawable.time_button);

				// final String item = (String)
				// parent.getItemAtPosition(position);
				// view.animate().setDuration(2000).alpha(0)
				// .withEndAction(new Runnable() {
				// @Override
				// public void run() {
				// list.remove(item);
				// adapter.notifyDataSetChanged();
				// //view.setAlpha(1);
				// }
				// });
			}
		});

		listview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {

				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.context_menu, menu);

			}
		});
		//
		// listview.setOnItemLongClickListener(new
		// AdapterView.OnItemLongClickListener() {
		//
		// @Override
		// public boolean onItemLongClick(AdapterView<?> parent, View view,
		// int position, long id) {
		//
		// // TODO Auto-generated method stub
		// AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
		// adb.setTitle("Are you sure want to delete this item");
		//
		// // adb.setPositiveButton("Yes",
		// // new DialogInterface.OnClickListener() {
		// //
		// // @Override
		// // public void onClick(DialogInterface dialog,
		// // int which) {
		// // // TODO Auto-generated method stub
		// // itemArrey.remove(position);
		// // itemAdapter.notifyDataSetChanged();
		// //
		// // }
		// // });
		// // adb.setNegativeButton("NO",
		// // new DialogInterface.OnClickListener() {
		// //
		// // @Override
		// // public void onClick(DialogInterface dialog,
		// // int which) {
		// // // TODO Auto-generated method stub
		// // dialog.dismiss();
		// //
		// // }
		// // });
		// adb.show();
		//
		// return false;
		//
		// }
		//
		// });

//		listview.setOnTouchListener(new OnSwipeTouchListener() {
//
//			public void onSwipeRight() {
//				Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT)
//						.show();
//
//			}
//
//			public void onSwipeLeft() {
//				Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT)
//						.show();
//			}
//
//			public void onSwipeBottom() {
//				Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT)
//						.show();
//			}
//		});

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

		// settingsChanged(false);

		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		// ToggleButton button = (ToggleButton) findViewById(R.id.dummy_button);
		//
		// button.setChecked(_settings.is_schedulingEnabled());

		View addBtn = findViewById(R.id.buttonAdd);

		Animation myFadeInAnimation = AnimationUtils.loadAnimation(
				MainActivity.this, R.anim.fadein);

		addBtn.startAnimation(myFadeInAnimation);

		// TransitionDrawable trans = (TransitionDrawable)
		// layout.getBackground();
		// trans.startTransition(2000);
	}

	@Override
	public void onPeriodUpdated(EnabledPeriod period) {
		// TODO Auto-generated method stub

		adapter.updateItem(period);

		saveSettings();
		
		Toast.makeText(MainActivity.this, "Updating time period",
				Toast.LENGTH_SHORT).show();

	}

	// @Override
	// public void onAttachedToWindow() {
	// // trial to mitigate gradient banding on tablets
	// super.onAttachedToWindow();
	// Window window = getWindow();
	// window.setFormat(PixelFormat.RGBA_8888);
	// }

	public void onAddClicked(View view) {
		long start = DateTimeUtils.getNextTimeIn24hInMillis(6, 30);
		long end = DateTimeUtils.getNextTimeIn24hInMillis(23, 30);

		EnabledPeriod newPeriod = new EnabledPeriod(true, start, end,
				new boolean[7]);

		showPeriodDetails(newPeriod);

		// adapter.addItem();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete:
			Log.d("MainActivity.onContextItemSelected", "Delete pressed");
			
			adapter.removeAt(info.position);
			
			saveSettings();
			return true;
		case R.id.modify:
			
			Log.d("MainActivity.onContextItemSelected", "Edit pressed");
			
			EnabledPeriod period = adapter.getItem(info.position);

			showPeriodDetails(period);
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void onToggleClicked(View view) {
		// loseTimePickersFocus();

		// Is the toggle on?
		_settings.set_schedulingEnabled(((ToggleButton) view).isChecked());

		setAlarm();
	}

	//
	// public void buttonStartClicked(View v) {
	//
	// FragmentManager fm = getSupportFragmentManager();
	// SchedulePeriodFragment editNameDialog = new SchedulePeriodFragment();
	// editNameDialog.show(fm, "fragment_schedule_period");
	//
	//
	// DialogFragment newFragment = new TimePickerFragment(
	// _settings.get_startTimeMillis()) {
	// @Override
	// public void onTimeSetAndDone(int hourOfDay, int minute) {
	//
	// startTimePicked(hourOfDay, minute);
	// }
	// };
	//
	// newFragment.show(getSupportFragmentManager(), "timePickerStart");
	// }

	//
	// public void buttonStopClicked(View v) {
	// DialogFragment newFragment = new TimePickerFragment(
	// _settings.get_endTimeMillis()) {
	// @Override
	// public void onTimeSetAndDone(int hourOfDay, int minute) {
	// // Do something with the time chosen by the user
	// endTimePicked(hourOfDay, minute);
	// }
	// };
	//
	// newFragment.show(getSupportFragmentManager(), "timePickerStart");
	// }

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

		String text = DateTimeUtils.getHourMinuteText(this, calendar);

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

		//saveSettings();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// onPause is called e.g. when the back button is pressed
		// loseTimePickersFocus();

		//saveSettings();

	}

	private void saveSettings() {
		SharedPreferences preferences = GetPreferences();

		Log.d("saveSettings", "Saving to preferences...");
		PersistenceUtils.saveToPreferences(preferences, _enabledPeriods);
		
		setAlarm();
		// _settings.saveToPreferences(preferences);
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

		NetworkScheduler scheduler = new NetworkScheduler();

		for (EnabledPeriod period : _enabledPeriods) {
			scheduler.setAlarm(MainActivity.this, period);
		}
		
//		
//		alarmHandler.setAlarm(MainActivity.this, _settings);
//
//		Log.d("MainActivity", "Alarm set: " + _settings.is_schedulingEnabled());
//
//		if (_settings.is_schedulingEnabled()) {
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTimeInMillis(_settings.get_startTimeMillis());
//
//			Log.d("MainActivity", "start Time: " + calendar.toString());
//
//			calendar.setTimeInMillis(_settings.get_endTimeMillis());
//
//			Log.d("MainActivity", "stop Time: " + calendar.toString());
//		}
	}

	private SharedPreferences GetPreferences() {
		NetworkScheduler alarmHandler = new NetworkScheduler();

		return alarmHandler.GetPreferences(MainActivity.this);
	}

	//
	// private void startTimePicked(int hourOfDay, int minute) {
	//
	// long nextStartTimeInMillis =
	// DateTimeUtils.getNextTimeIn24hInMillis(hourOfDay, minute);
	//
	// _settings.set_startTimeMillis(nextStartTimeInMillis);
	//
	// settingsChanged(true);
	// }
	//
	// private void endTimePicked(int hourOfDay, int minute) {
	//
	// long nextEndTimeInMillis =
	// DateTimeUtils.getNextTimeIn24hInMillis(hourOfDay, minute);
	//
	// _settings.set_endTimeMillis(nextEndTimeInMillis);
	//
	// settingsChanged(true);
	// }
	//
	// private void settingsChanged(boolean setAlarm) {
	// Button startButton = (Button) findViewById(R.id.buttonTimeStart);
	// setButtonTime(_settings.get_startTimeMillis(), (Button) startButton);
	//
	// Button stopButton = (Button) findViewById(R.id.buttonTimeStop);
	// setButtonTime(_settings.get_endTimeMillis(), (Button) stopButton);
	//
	// saveSettings();
	//
	// if (setAlarm) {
	// setAlarm();
	// }
	// }

	public void showPeriodDetails(EnabledPeriod item) {
		FragmentManager fm = getSupportFragmentManager();
		SchedulePeriodFragment schedulePeriodFragment = SchedulePeriodFragment
				.newInstance(item);

		//
		// Bundle bundle = new Bundle();
		// item.saveToBundle(bundle);
		// schedulePeriodFragment.setArguments(bundle);
		schedulePeriodFragment.show(fm, "fragment_schedule_period");
	}

}
