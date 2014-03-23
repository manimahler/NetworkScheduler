package com.manimahler.android.scheduler3g;

import java.util.ArrayList;
import java.util.Arrays;

import com.manimahler.android.scheduler3g.SchedulePeriodFragment.OnPeriodUpdatedListener;
import com.manimahler.android.scheduler3g.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 *  FAQ:
 *  When does the warning come auto-delay take place (screen on, bluetooth connected (TODO!))
 *  Why does my wifi/mobile data/bluetooth not start stop?
 *  - weekday? -> Midnight issue?! Which day is 12.00pm? 0.00 am?
 *  - already off? already on?
 *  - skipped? (auto-)delayed?
 *  - interval connect: why does it not start right away after a device reboot?
 */


public class MainActivity extends FragmentActivity implements
		OnPeriodUpdatedListener {

	private SchedulerSettings _settings;

	private ArrayList<ScheduledPeriod> _enabledPeriods;
	
	private PeriodListAdapter _adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		SharedPreferences schedulesPreferences = GetPreferences();

		_enabledPeriods = PersistenceUtils.readFromPreferences(schedulesPreferences);
		_settings = PersistenceUtils.readSettings(this);
		
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayShowTitleEnabled(true);
			LinearLayout globalSwitch = (LinearLayout) getLayoutInflater().inflate(R.layout.actionbar_switch, null);

			ActionBar.LayoutParams lp = new ActionBar.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
			
			actionBar.setCustomView(globalSwitch, lp);
			
			actionBar.setDisplayShowCustomEnabled(true);
		}
//		else
//		{
//			// add the switch (toggle button) somewhere else
//			RelativeLayout bottomBar = (RelativeLayout) findViewById(R.id.bottom_bar);
//			RelativeLayout globalSwitch = (RelativeLayout) getLayoutInflater().inflate(R.layout.actionbar_switch, null);
//			
//			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//					RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//			
//			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//						
//			globalSwitch.setLayoutParams(params);
//			globalSwitch.setEnabled(false);
//			
//			bottomBar.addView(globalSwitch);
//		}

		CompoundButton globalSwitch = (CompoundButton)findViewById(R.id.globalSwitch);
		
		// currently not supported < SDK 14
		if (globalSwitch != null)
		{
		globalSwitch.setChecked(_settings.is_globalOn());
		
		globalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onGlobalOnClicked(buttonView);
			}
		});
		}
		
		final ListView listview = (ListView) findViewById(R.id.listview);

		this.registerForContextMenu(listview);

		_adapter = new PeriodListAdapter(MainActivity.this, _enabledPeriods);

		listview.setAdapter(_adapter);

		if (! _settings.is_globalOn())
		{
			// contains setAlpha, which is not supported on Gingerbread (always globalOn):
			updateEnabledAppearance(false);
		}
		else
		{
			setItemPressListeners(listview, true);
		}
		
		
//		LinearLayout mainView = (LinearLayout) findViewById(R.id.mainview);
//		disableEnableControls(_settings.is_globalOn(), mainView);

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

	private void setItemPressListeners(final ListView listview, boolean enabled) {
		
		
		if (! enabled)
		{
			listview.setOnItemClickListener(null);
			listview.setOnCreateContextMenuListener(null);
			
			return;
		}
		
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {

				Log.d("MainActivity.AdapterView.OnItemClickListener",
						"Item clicked at " + position);

				ScheduledPeriod item = _adapter.getItem(position);

				showPeriodDetails(item);
			}
		});

		listview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

			// TODO: use contextual action bar once 2.x support is dropped
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {

				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.context_menu, menu);

				// Get the list
			    ListView list = (ListView)v;

			    // Get the list item position
			    AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			    
			    ScheduledPeriod selectedPeriod = _adapter.getItem(info.position);
				
			    MenuItem skipItem = menu.findItem(R.id.skip_next);
			    skipItem.setChecked(selectedPeriod.is_skipped());
			    
			    // NOTE: listening to the user-made changes in the system only works for WiFi, but
			    // not for mobile data -> add specific context menu entries
			    
			    if (selectedPeriod.is_active() && selectedPeriod.is_intervalConnectWifi())
			    {
			    	MenuItem wifiItem = menu.add(0, R.integer.context_menu_id_interval_wifi, 4, R.string.context_menu_interval_wifi);
			    	wifiItem.setCheckable(true);
			    	wifiItem.setChecked(! selectedPeriod.is_overrideIntervalWifi());
			    }
			    
			    if (selectedPeriod.is_active() && selectedPeriod.is_intervalConnectMobData())
			    {
			    	MenuItem mobItem = menu.add(0, R.integer.context_menu_id_interval_mob, 5, R.string.context_menu_interval_mob);
			    	mobItem.setCheckable(true);
			    	mobItem.setChecked(! selectedPeriod.is_overrideIntervalMob());
			    }
			    
				if (info.position > 0) {
					menu.add(0, R.integer.context_menu_id_up, 8,
							R.string.move_up); //setIcon(android.R.drawable.arrow_up_float);
				}

				if (info.position < _adapter.getCount() - 1) {
					menu.add(0, R.integer.context_menu_id_down, 12,
							R.string.move_down); //setIcon(android.R.drawable.arrow_down_float);
				}
			}
		});
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
	public void onPeriodUpdated(ScheduledPeriod period) {

		_adapter.updateItem(period);

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

		ScheduledPeriod newPeriod = new ScheduledPeriod(true, start, end, weekDays);

		showPeriodDetails(newPeriod);
	}
	
	public void onGlobalOnClicked(CompoundButton buttonView){
		
		PersistenceUtils.saveGlobalOnState(this, buttonView.isChecked());
		
		String toastText;
		
		NetworkScheduler scheduler = new NetworkScheduler();
		
		
		if (buttonView.isChecked())
		{
			scheduler.setAlarms(this, _enabledPeriods, _settings);
			toastText = "Network Scheduler is enabled";
		}
		else
		{
			scheduler.deleteAlarms(this, _enabledPeriods);
			toastText = "Network Scheduler completely disabled";
		}
		
		updateEnabledAppearance(buttonView.isChecked());
		
		Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
	}
	
	
	private void updateEnabledAppearance(boolean enabled)
	{
		ListView listview = (ListView) findViewById(R.id.listview);
		setItemPressListeners(listview, enabled);
		
		RelativeLayout mainView = (RelativeLayout) findViewById(R.id.mainview);
		ViewUtils.setControlsEnabled(enabled, mainView);
		
		_adapter.setItemsEnabled(enabled);
		_adapter.notifyDataSetChanged();
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
		case R.id.help:
			String url = "https://sites.google.com/site/networkscheduler/home";
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			return true;
		}
		
		return false;
		
	}

	private void showSettingsScreen() {

		
		//FragmentManager fm = getSupportFragmentManager();
		
		//SettingsFragment settingsFragment = new SettingsFragment();

		
		Intent intent = new Intent(this, SettingsActivity.class);
		
		startActivity(intent);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		ScheduledPeriod selectedPeriod = _adapter.getItem(info.position);

		switch (item.getItemId()) {
		case R.id.delete:
			Log.d("MainActivity.onContextItemSelected", "Delete pressed");

			// cancel the alarm;
			NetworkScheduler scheduler = new NetworkScheduler();
			try {
				scheduler.deleteAlarm(this, selectedPeriod);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			_adapter.removeAt(info.position);
			saveSettings();
			
			_adapter.notifyDataSetChanged();

			return true;
		case R.id.modify:

			Log.d("MainActivity.onContextItemSelected", "Edit pressed");

			ScheduledPeriod periodToEdit = _adapter.getItem(info.position);

			showPeriodDetails(periodToEdit);
			return true;
		case R.id.activate_now:
			toggleNetworkState(selectedPeriod, true);
			_adapter.notifyDataSetChanged();
			return true;
		case R.id.deactivate_now:
			toggleNetworkState(selectedPeriod, false);
			_adapter.notifyDataSetChanged();
			return true;
		case R.id.skip_next:
			ScheduledPeriod periodToSkip = _adapter.getItem(info.position);
			periodToSkip.set_skipped(! periodToSkip.is_skipped());
			onPeriodUpdated(periodToSkip);
			return true;
		case R.integer.context_menu_id_interval_wifi:
			toggleCurrentIntervalWifi(_adapter.getItem(info.position));
			return true;
		case R.integer.context_menu_id_interval_mob:
			toggleCurrentIntervalMobData(_adapter.getItem(info.position));
			return true;
		case R.integer.context_menu_id_up:
			_adapter.moveUp(info.position);
			_adapter.notifyDataSetChanged();
			return true;
		case R.integer.context_menu_id_down:
			_adapter.moveDown(info.position);
			_adapter.notifyDataSetChanged();
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void toggleCurrentIntervalWifi(ScheduledPeriod periodWifi) {
		periodWifi.set_overrideIntervalWifi(!periodWifi.is_overrideIntervalWifi());
		saveSettings();
		
		NetworkScheduler scheduler = new NetworkScheduler();
		scheduler.setupIntervalConnect(this, _settings);
		
		_adapter.notifyDataSetChanged();
	}
	
	private void toggleCurrentIntervalMobData(ScheduledPeriod periodMobData) {
		periodMobData.set_overrideIntervalMob(!periodMobData.is_overrideIntervalMob());
		//onPeriodUpdated(periodMobData);
		
		saveSettings();
		
		NetworkScheduler scheduler = new NetworkScheduler();
		scheduler.setupIntervalConnect(this, _settings);
		
		_adapter.notifyDataSetChanged();
	}
	

	private void toggleNetworkState(ScheduledPeriod selectedPeriod, boolean enable) {
		try {
			
			NetworkScheduler scheduler = new NetworkScheduler();

			if (! enable)
			{
				scheduler.stop(selectedPeriod, this);
			}
			else
			{
				scheduler.start(selectedPeriod, this, _settings);
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
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// use the opportunity to refresh the (potentially updated _active state) periods
		// otherwise we'd need some kind of polling or auto-refresh.
		_adapter.notifyDataSetChanged();
	}

	private void saveSettings() {
		SharedPreferences preferences = GetPreferences();

		Log.d("saveSettings", "Saving to preferences...");
		PersistenceUtils.saveToPreferences(preferences, _enabledPeriods);

		try {
			NetworkScheduler scheduler = new NetworkScheduler();
			scheduler.setAlarms(this, _enabledPeriods, _settings);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private SharedPreferences GetPreferences() {
		NetworkScheduler alarmHandler = new NetworkScheduler();

		return alarmHandler.getSchedulesPreferences(MainActivity.this);
	}

	public void showPeriodDetails(ScheduledPeriod item) {
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
