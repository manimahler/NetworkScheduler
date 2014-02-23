package com.manimahler.android.scheduler3g;

import java.util.ArrayList;
import java.util.Arrays;

import com.manimahler.android.scheduler3g.SchedulePeriodFragment.OnPeriodUpdatedListener;
import com.manimahler.android.scheduler3g.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;

/**
 *  FAQ:
 *  When does the warning come auto-delay take place (screen on, bluetooth connected (TODO!))
 *  Why does my wifi/mobile data/bluetooth not start stop?
 *  - weekday? -> Midnight issue?! Which day is 12.00pm? 0.00 am?
 *  - already off? already on?
 *  - skipped? (auto-)delayed?
 *  - interval connect: why does it not start right away after a device reboot?
 */


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class MainActivity extends FragmentActivity implements
		OnPeriodUpdatedListener {

	SchedulerSettings _settings;

	ArrayList<EnabledPeriod> _enabledPeriods;

	private PeriodListAdapter adapter;

	final static boolean DEBUG = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		SharedPreferences schedulesPreferences = GetPreferences();

		_enabledPeriods = PersistenceUtils.readFromPreferences(schedulesPreferences);
		_settings = PersistenceUtils.readSettings(this);

		final ListView listview = (ListView) findViewById(R.id.listview);

		this.registerForContextMenu(listview);

		adapter = new PeriodListAdapter(MainActivity.this, _enabledPeriods);

		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {

				Log.d("MainActivity.AdapterView.OnItemClickListener",
						"Item clicked at " + position);

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

			// TODO: use contextual action bar once 2.x support is dropped
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {

				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.context_menu, menu);

				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

				if (info.position > 0) {
					menu.add(0, R.integer.context_menu_id_up, 6,
							R.string.move_up); //setIcon(android.R.drawable.arrow_up_float);
				}

				if (info.position < adapter.getCount() - 1) {
					menu.add(0, R.integer.context_menu_id_down, 10,
							R.string.move_down); //setIcon(android.R.drawable.arrow_down_float);
				}
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

		// listview.setOnTouchListener(new OnSwipeTouchListener() {
		//
		// public void onSwipeRight() {
		// Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT)
		// .show();
		//
		// }
		//
		// public void onSwipeLeft() {
		// Toast.makeText(MainActivity.this, "left", Toast.LENGTH_SHORT)
		// .show();
		// }
		//
		// public void onSwipeBottom() {
		// Toast.makeText(MainActivity.this, "bottom", Toast.LENGTH_SHORT)
		// .show();
		// }
		// });

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
	public void onStart() {
		super.onStart();

		final ListView listview = (ListView) findViewById(R.id.listview);

		int width = getAvailableScreenWitdh(this);

		Log.d("MainActivity", "width: " + width);

		int maxWidth = 1000;

		int paddingTop = listview.getPaddingTop();
		int paddingBottom = listview.getPaddingBottom();
		int paddingLeftRight;

		if (width > maxWidth) {
			paddingLeftRight = (width - 100 - maxWidth) / 2;
		} else {
			paddingLeftRight = 0;
		}

		Log.d("MainActivity", "Setting padding: " + paddingLeftRight);
		listview.setPadding(paddingLeftRight, paddingTop, paddingLeftRight,
				paddingBottom);
	}

	@Override
	public void onPeriodUpdated(EnabledPeriod period) {

		adapter.updateItem(period);

		saveSettings();

//		View addBtn = findViewById(R.id.buttonSkipToday);
//
//		Animation myFadeInAnimation = AnimationUtils.loadAnimation(
//				MainActivity.this, R.anim.fadein);
//
//		addBtn.startAnimation(myFadeInAnimation);

		// Toast.makeText(MainActivity.this, "Updating time period",
		// Toast.LENGTH_SHORT).show();

	}

	public void onAddClicked(View view) {
		long start = DateTimeUtils.getNextTimeIn24hInMillis(6, 30);
		long end = DateTimeUtils.getNextTimeIn24hInMillis(23, 30);

		boolean[] weekDays = new boolean[7];
		Arrays.fill(weekDays, true);

		EnabledPeriod newPeriod = new EnabledPeriod(true, start, end, weekDays);

		showPeriodDetails(newPeriod);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected (MenuItem item){
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case R.id.settings:
			showSettingsScreen();
			return true;
		}
		
		return false;
		
	}

	private void showSettingsScreen() {

		
		//FragmentManager fm = getSupportFragmentManager();
		
		//SettingsFragment settingsFragment = new SettingsFragment();

		
		Intent intent = new Intent(this, SettingsActivity.class);
		
		startActivity(intent);

		
//		fm.
//		getFragmentManager().beginTransaction()
//        .add(android.R.id.content, settingsFragment)
//        .commit();

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		EnabledPeriod selectedPeriod = adapter.getItem(info.position);

		switch (item.getItemId()) {
		case R.id.delete:
			Log.d("MainActivity.onContextItemSelected", "Delete pressed");

			// cancel the alarm
			selectedPeriod.set_schedulingEnabled(false);
			NetworkScheduler scheduler = new NetworkScheduler();
			scheduler.setAlarm(MainActivity.this, selectedPeriod);

			adapter.removeAt(info.position);
			saveSettings();

			return true;
		case R.id.modify:

			Log.d("MainActivity.onContextItemSelected", "Edit pressed");

			EnabledPeriod period = adapter.getItem(info.position);

			showPeriodDetails(period);
			return true;
		case R.id.activate_now:
			toggleNetworkState(selectedPeriod, true);
			return true;
		case R.id.deactivate_now:
			toggleNetworkState(selectedPeriod, false);
			return true;
		case R.integer.context_menu_id_up:
			adapter.moveUp(info.position);
			return true;
		case R.integer.context_menu_id_down:
			adapter.moveDown(info.position);
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void toggleNetworkState(EnabledPeriod selectedPeriod, boolean enable) {
		try {
			
			NetworkScheduler scheduler = new NetworkScheduler();

			if (! enable)
			{
				scheduler.switchOffNow(this, selectedPeriod.get_id());
			}
			else
			{
				scheduler.switchOnNow(this, selectedPeriod, _settings);
			}
			
		} catch (Exception e) {
			Toast.makeText(this, "Error (de-)activating selected profile",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	private int getAvailableScreenWitdh(Activity activity) {

		WindowManager w = activity.getWindowManager();
		Display d = w.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		d.getMetrics(metrics);
		// since SDK_INT = 1;
		int widthPixels = metrics.widthPixels;
		//int heightPixels = metrics.heightPixels;
		// includes window decorations (statusbar bar/menu bar)
		if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
			try {
				widthPixels = (Integer) Display.class.getMethod("getRawWidth")
						.invoke(d);
				//heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
			} catch (Exception ignored) {
			}
		// includes window decorations (statusbar bar/menu bar)
		if (Build.VERSION.SDK_INT >= 17)
			try {
				Point realSize = new Point();
				Display.class.getMethod("getRealSize", Point.class).invoke(d,
						realSize);
				widthPixels = realSize.x;
				//heightPixels = realSize.y;
			} catch (Exception ignored) {
			}

		return widthPixels;
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

		// saveSettings();
	}

	@Override
	protected void onPause() {
		super.onPause();

		// onPause is called e.g. when the back button is pressed
		// loseTimePickersFocus();

		// saveSettings();

	}

	private void saveSettings() {
		SharedPreferences preferences = GetPreferences();

		Log.d("saveSettings", "Saving to preferences...");
		PersistenceUtils.saveToPreferences(preferences, _enabledPeriods);

		setAlarm();
		// _settings.saveToPreferences(preferences);
	}


	private void setAlarm() {

		NetworkScheduler scheduler = new NetworkScheduler();

		for (EnabledPeriod period : _enabledPeriods) {
			scheduler.setAlarm(MainActivity.this, period);
		}

	}

	private SharedPreferences GetPreferences() {
		NetworkScheduler alarmHandler = new NetworkScheduler();

		return alarmHandler.getSchedulesPreferences(MainActivity.this);
	}

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
