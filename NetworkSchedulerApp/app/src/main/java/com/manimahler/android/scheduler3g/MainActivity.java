package com.manimahler.android.scheduler3g;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.manimahler.android.scheduler3g.NetworkScheduler.NetworkType;
import com.manimahler.android.scheduler3g.SchedulePeriodFragment.OnPeriodUpdatedListener;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * FAQ: When does the warning come auto-delay take place (screen on, bluetooth
 * connected (TODO!)) Why does my wifi/mobile data/bluetooth not start stop? -
 * weekday? -> Midnight issue?! Which day is 12.00pm? 0.00 am? - already off?
 * already on? - skipped? (auto-)delayed? - interval connect: why does it not
 * start right away after a device reboot?
 */

public class MainActivity extends AppCompatActivity implements
        OnPeriodUpdatedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int REQUEST_DO_NOT_DISTURB_CODE = 1001;

    private SchedulerSettings _settings;
    private PeriodListAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initializePersistedFields();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            android.support.v7.app.ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayShowTitleEnabled(true);
            LinearLayout globalSwitch = (LinearLayout) getLayoutInflater()
                    .inflate(R.layout.actionbar_switch, null);

            android.support.v7.app.ActionBar.LayoutParams lp =
                    new android.support.v7.app.ActionBar.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                            Gravity.RIGHT | Gravity.CENTER_VERTICAL);

            lp.setMargins(0, 0, 30, 0);

            actionBar.setCustomView(globalSwitch, lp);

            actionBar.setDisplayShowCustomEnabled(true);
        }
        // TODO: consider an entry in menu?

        CompoundButton globalSwitch = (CompoundButton) findViewById(R.id.globalSwitch);

        // currently not supported < SDK 14
        if (globalSwitch != null) {
            globalSwitch.setChecked(_settings.is_globalOn());

            globalSwitch
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            onGlobalOnClicked(buttonView, isChecked);
                        }
                    });
        }

        final ListView listview = (ListView) findViewById(R.id.listview);

        this.registerForContextMenu(listview);

        listview.setAdapter(_adapter);

        if (!_settings.is_globalOn()) {
            // contains setAlpha, which is not supported on Gingerbread (always
            // globalOn):
            updateEnabledAppearance(false);
        } else {
            setItemPressListeners(listview, true);
        }

        View addBtn = findViewById(R.id.buttonAdd);

        Animation myFadeInAnimation = AnimationUtils.loadAnimation(
                MainActivity.this, R.anim.fadein);

        addBtn.startAnimation(myFadeInAnimation);
    }

    @Override
    protected void onStart() {
        super.onStart();

        final ListView listview = (ListView) findViewById(R.id.listview);
        int paddingTop = listview.getPaddingTop();
        int paddingBottom = listview.getPaddingBottom();

        int paddingLeftRight = getListViewHorizontalPadding();

        Log.d(TAG, "Setting padding: " + paddingLeftRight);
        listview.setPadding(paddingLeftRight, paddingTop, paddingLeftRight,
                paddingBottom);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // use the opportunity to refresh the (potentially updated _active
        // state) periods
        // otherwise we'd need some kind of polling or auto-refresh.
        initializePersistedFields();
    }

    @Override
    public void onPeriodUpdated(ScheduledPeriod period) {

        try {

            _adapter.updateItem(period);

            // must be saved before activating, otherwise it has ID -1 which is
            // illegal!
            saveSettings();


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

                boolean askForBatteryOptimzierExclusion = !pm.isIgnoringBatteryOptimizations(packageName);

                if (askForBatteryOptimzierExclusion) {
                    View contextView = findViewById(R.id.mainview);

                    final Snackbar snackbar = Snackbar.make(contextView, R.string.snack_whitelist_from_optimizations, Snackbar.LENGTH_INDEFINITE);

                    View snackbarView = snackbar.getView();
                    TextView snackTextView = (TextView) snackbarView.findViewById(R.id.snackbar_text);

                    snackTextView.setMaxLines(7);

                    snackbar.setAction(android.R.string.ok, new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });

                    snackbar.show();
                }

                // These other approaches apparently result in trouble from google:
//				if (!pm.isIgnoringBatteryOptimizations(packageName))
//					intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//				else {
//					intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//					intent.setData(Uri.parse("package:" + packageName));
//				}
//
//				startActivity(intent);
            }

            if (period.is_scheduleStart() && period.is_scheduleStop()
                    && period.is_schedulingEnabled() && period.isActiveNow()) {

                NetworkScheduler networkScheduler = new NetworkScheduler();

                boolean switchOn = period.is_enableRadios();

                // NOTE: switching off is a bit risky, the user might want this
                // to be an exception
                // plus it does not work properly! It is not active despite it
                // should and it
                // starts all the sensors it should stop.
                if (switchOn) {

                    makeActivePeriodCheckingSensorsToast(period);

                    networkScheduler.toggleActivation(this, period, switchOn,
                            _settings, false);
                }
            }

        } catch (Exception e) {
            UserLog.log(this, "Error storing new period", e);
            e.printStackTrace();
        }
    }

    public void onAddClicked(View view) {

        long start = DateTimeUtils.getNextTimeIn24hInMillis(6, 30);
        long end = DateTimeUtils.getNextTimeIn24hInMillis(23, 30);

        boolean[] weekDays = new boolean[7];
        Arrays.fill(weekDays, true);

        ScheduledPeriod newPeriod = new ScheduledPeriod(true, start, end,
                weekDays);

        _adapter.showPeriodDetails(newPeriod);
    }

    public void onGlobalOnClicked(CompoundButton buttonView, boolean isChecked) {

        try {
            boolean isGlobalOn = isChecked;

            if (!isGlobalOn) {
                // log as long as it is still enabled:
                UserLog.log(this,
                        "Network Scheduler completely disabled by user.");
            }

            PersistenceUtils.saveGlobalOnState(this, isGlobalOn);
            // re-read the settings, this also re-initializes the user log
            _settings = PersistenceUtils.readSettings(this);

            String toastText;

            NetworkScheduler scheduler = new NetworkScheduler();

            if (isGlobalOn) {

                UserLog.log(this, "Network Scheduler enabled by user!");

                for (ScheduledPeriod period : _adapter.getPeriods()) {
                    if (period.is_scheduleStart() && period.is_scheduleStop()
                            && period.is_schedulingEnabled()
                            && period.isActiveNow()) {

                        toggleActivation(period, true, false);
                    }
                }

                // save the updated period's active property and also sets the
                // alarms for all periods:
                saveSettings();

                toastText = getResources().getString(
                        R.string.global_switch_on_toast);
            } else {

                for (ScheduledPeriod period : _adapter.getPeriods()) {
                    period.set_active(false);
                }

                // also schedules the alarms -> delete below
                saveSettings();

                scheduler.deleteAlarms(this, _adapter.getPeriods());
                toastText = getResources().getString(
                        R.string.global_switch_off_toast);
            }

            updateEnabledAppearance(isGlobalOn);

            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            UserLog.log(this, "Error toggling global on / off switch", e);
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.settings:
                showSettingsScreen();
                return true;
            case R.id.help:
                String url = getResources().getString(R.string.help_url);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;
        }

        return false;

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
                .getMenuInfo();

        ScheduledPeriod selectedPeriod = _adapter.getItem(info.position);

        String msg;

        try {

            switch (item.getItemId()) {
                case R.id.delete:
                    Log.d(TAG, "Delete pressed");

                    // cancel the alarm;
                    NetworkScheduler scheduler = new NetworkScheduler();
                    scheduler.deleteAlarm(this, selectedPeriod);

                    _adapter.removeAt(info.position);
                    saveSettings();

                    _adapter.notifyDataSetChanged();

                    return true;
                case R.id.modify:

                    Log.d(TAG, "Edit pressed");

                    _adapter.showPeriodDetails(selectedPeriod);
                    return true;
                case R.id.activate_now:
                    UserLog.log(this, "Manual activation for period "
                            + selectedPeriod);
                    toggleActivation(selectedPeriod, true, true);
                    _adapter.notifyDataSetChanged();
                    return true;
                case R.id.deactivate_now:
                    UserLog.log(this, "Manual deactivation for period "
                            + selectedPeriod);
                    toggleActivation(selectedPeriod, false, true);
                    _adapter.notifyDataSetChanged();
                    return true;
                case R.id.skip_next:
                    selectedPeriod.set_skipped(!selectedPeriod.is_skipped());
                    saveSettings();
                    _adapter.notifyDataSetChanged();

                    if (selectedPeriod.is_skipped()) {
                        msg = getResources().getString(R.string.skipped_period);
                    } else {
                        msg = getResources().getString(R.string.unskipped_period);
                    }

                    UserLog.log(this, msg + " Period: " + selectedPeriod);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    return true;
                case R.id.is_enabled:
                    toggleEnabled(selectedPeriod);

                    return true;
                case R.integer.context_menu_id_interval_wifi:
                    toggleCurrentIntervalWifi(selectedPeriod);

                    if (selectedPeriod.is_overrideIntervalWifi()) {
                        msg = getResources().getString(
                                R.string.overridden_wifi_interval);
                    } else {
                        msg = getResources().getString(
                                R.string.unoverridden_wifi_interval);
                    }

                    UserLog.log(this, msg + " Period: " + selectedPeriod);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    return true;
                case R.integer.context_menu_id_interval_mob:
                    toggleCurrentIntervalMobData(selectedPeriod);

                    if (selectedPeriod.is_overrideIntervalMob()) {
                        msg = getResources().getString(
                                R.string.overridden_mob_interval);
                    } else {
                        msg = getResources().getString(
                                R.string.unoverridden_mob_interval);
                    }

                    UserLog.log(this, msg + " Period: " + selectedPeriod);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    return true;
                case R.integer.context_menu_id_up:

                    _adapter.moveUp(info.position);
                    // PeriodListAdapter.moveItemUp(_enabledPeriods, info.position);
                    // and save new list order - TODO: add property listPosition to
                    // be
                    // more explicit
                    saveSettings();
                    return true;
                case R.integer.context_menu_id_down:
                    _adapter.moveDown(info.position);
                    saveSettings();
                    return true;
                default:
                    return super.onContextItemSelected(item);
            }

        } catch (Exception e) {
            UserLog.log(this, "Error in context menu", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();

            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == MainActivity.REQUEST_DO_NOT_DISTURB_CODE) {
            // activity started in SchedulePeriodFragment.onToggleVolume()

            if (Build.VERSION.SDK_INT < 23) {
                return;
            }

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            boolean wasGranted = notificationManager.isNotificationPolicyAccessGranted();
            String toastText = getResources().getString(
                    wasGranted
                            ? R.string.change_ringer_granted
                            : R.string.change_ringer_denied);

            Toast.makeText(this, toastText, Toast.LENGTH_LONG).show();
        }
    }

    private void initializePersistedFields() {
        SharedPreferences schedulesPreferences = PersistenceUtils
                .getSchedulesPreferences(MainActivity.this);

        ArrayList<ScheduledPeriod> periods = PersistenceUtils
                .readFromPreferences(schedulesPreferences);

        _settings = PersistenceUtils.readSettings(this);

        // important to remain in sync - the adapter must reference the list
        // used here
        if (_adapter != null) {
            _adapter.resetPeriods(periods);
        } else {
            _adapter = new PeriodListAdapter(MainActivity.this, periods);
        }
    }

    private void setItemPressListeners(final ListView listview, boolean enabled) {

        if (!enabled) {
            listview.setOnItemClickListener(null);
            listview.setOnCreateContextMenuListener(null);

            return;
        }

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {

                // NOTE: This is not working any more once there are clickable buttons on the view
                //  item.
                Log.d(TAG, "Item clicked at " + position);

                ScheduledPeriod item = _adapter.getItem(position);
                _adapter.showPeriodDetails(item);
            }
        });

        listview.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            // TODO: use contextual action bar once 2.x support is dropped
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenuInfo menuInfo) {

                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);

                // Get the list item position
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

                ScheduledPeriod selectedPeriod = _adapter
                        .getItem(info.position);

                MenuItem skipItem = menu.findItem(R.id.skip_next);
                skipItem.setChecked(selectedPeriod.is_skipped());

                if (!selectedPeriod.is_schedulingEnabled()) {
                    menu.removeItem(R.id.skip_next);
                }

                MenuItem enabledItem = menu.findItem(R.id.is_enabled);
                enabledItem.setChecked(selectedPeriod.is_schedulingEnabled());

                if (!selectedPeriod.is_enableRadios()) {
                    MenuItem activateItem = menu.findItem(R.id.activate_now);
                    activateItem.setTitle(R.string.activate_now_off);

                    MenuItem deactivateItem = menu
                            .findItem(R.id.deactivate_now);
                    deactivateItem.setTitle(R.string.deactivate_now_on);
                }

                // NOTE: listening to the user-made changes in the system only
                // works for WiFi, but
                // not for mobile data -> add specific context menu entries

                if (selectedPeriod.is_active()
                        && selectedPeriod.is_intervalConnectWifi()) {
                    MenuItem wifiItem = menu.add(0,
                            R.integer.context_menu_id_interval_wifi, 3,
                            R.string.context_menu_interval_wifi);
                    wifiItem.setCheckable(true);
                    wifiItem.setChecked(!selectedPeriod
                            .is_overrideIntervalWifi());
                }

                if (selectedPeriod.is_active()
                        && selectedPeriod.is_intervalConnectMobData()) {
                    MenuItem mobItem = menu.add(0,
                            R.integer.context_menu_id_interval_mob, 4,
                            R.string.context_menu_interval_mob);
                    mobItem.setCheckable(true);
                    mobItem.setChecked(!selectedPeriod.is_overrideIntervalMob());
                }

                if (info.position > 0) {
                    menu.add(0, R.integer.context_menu_id_up, 10,
                            R.string.move_up); // setIcon(android.R.drawable.arrow_up_float);
                }

                if (info.position < _adapter.getCount() - 1) {
                    menu.add(0, R.integer.context_menu_id_down, 12,
                            R.string.move_down); // setIcon(android.R.drawable.arrow_down_float);
                }
            }
        });
    }

    private void updateEnabledAppearance(boolean enabled) {
        ListView listview = (ListView) findViewById(R.id.listview);
        setItemPressListeners(listview, enabled);

        RelativeLayout mainView = (RelativeLayout) findViewById(R.id.mainview);
        ViewUtils.setControlsEnabled(this, enabled, mainView, true);

        _adapter.setItemsEnabled(enabled);
        _adapter.notifyDataSetChanged();
    }

    private void showSettingsScreen() {

        // FragmentManager fm = getSupportFragmentManager();
        // SettingsFragment settingsFragment = new SettingsFragment();

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void toggleCurrentIntervalWifi(ScheduledPeriod periodWifi)
            throws Exception {
        periodWifi.set_overrideIntervalWifi(!periodWifi
                .is_overrideIntervalWifi());
        saveSettings();

        NetworkScheduler scheduler = new NetworkScheduler();
        scheduler.setupIntervalConnect(this, _settings);

        _adapter.notifyDataSetChanged();
    }

    private void toggleCurrentIntervalMobData(ScheduledPeriod periodMobData)
            throws Exception {
        periodMobData.set_overrideIntervalMob(!periodMobData
                .is_overrideIntervalMob());

        saveSettings();

        NetworkScheduler scheduler = new NetworkScheduler();
        scheduler.setupIntervalConnect(this, _settings);

        _adapter.notifyDataSetChanged();
    }

    private void toggleEnabled(ScheduledPeriod selectedPeriod) throws Exception {
        selectedPeriod.set_schedulingEnabled(!selectedPeriod
                .is_schedulingEnabled());

        if (!selectedPeriod.is_schedulingEnabled()) {
            UserLog.log(this, "Disabling period " + selectedPeriod);
            selectedPeriod.set_active(false);
        } else {
            if (selectedPeriod.is_scheduleStart()
                    && selectedPeriod.is_scheduleStop()
                    && selectedPeriod.is_schedulingEnabled()
                    && selectedPeriod.isActiveNow()) {

                makeActivePeriodCheckingSensorsToast(selectedPeriod);

                NetworkScheduler networkScheduler = new NetworkScheduler();

                networkScheduler.toggleActivation(this, selectedPeriod, true,
                        _settings, false);
            }
        }

        saveSettings();

        _adapter.notifyDataSetChanged();
    }

    private void makeActivePeriodCheckingSensorsToast(
            ScheduledPeriod selectedPeriod) {

        NetworkScheduler networkScheduler = new NetworkScheduler();

        ArrayList<String> sensorList = networkScheduler
                .getSensorsInPeriodArrayList(selectedPeriod, this);
        String sensorListMsg = networkScheduler.getSensorStringList(sensorList,
                this);

        if (sensorListMsg == null) {
            return;
        }

        String formatString;
        if (selectedPeriod.is_enableRadios()) {
            formatString = getResources().getString(
                    R.string.enabled_period_checking);
        } else {
            formatString = getResources().getString(
                    R.string.disabled_period_checking);
        }

        String msg = String.format(formatString, sensorListMsg);

        UserLog.log(this, msg);

        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private void toggleActivation(ScheduledPeriod selectedPeriod,
                                  boolean activate, boolean ignoreSkip) {
        try {

            NetworkScheduler scheduler = new NetworkScheduler();

            scheduler.toggleActivation(this, selectedPeriod, activate,
                    _settings, ignoreSkip);

        } catch (Exception e) {
            Toast.makeText(this, "Error (de-)activating selected profile",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private int getListViewHorizontalPadding() {

        int width = getScreenWidth(this);

        Log.d(TAG, "width: " + width);

        int maxWidth = 560;

        int paddingLeftRight;

        if (width > maxWidth) {
            paddingLeftRight = (width - maxWidth) / 2;
        } else {
            paddingLeftRight = 0;
        }

        return paddingLeftRight;
    }

    private int getScreenWidth(Activity activity) {

        DisplayMetrics displayMetrics = new DisplayMetrics();

        WindowManager windowManager = activity.getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        Log.d(TAG, "width: " + displayMetrics.widthPixels);
        Log.d(TAG, "density: " + displayMetrics.density);

        int densityIndependentPixels = (int) ((double) displayMetrics.widthPixels / displayMetrics.density);

        Log.d(TAG, "adjusted width: " + densityIndependentPixels);

        return densityIndependentPixels;
    }

    private void saveSettings() throws Exception {
        SharedPreferences preferences = PersistenceUtils
                .getSchedulesPreferences(MainActivity.this);

        Log.d(TAG, "Saving to preferences...");
        PersistenceUtils.saveToPreferences(preferences, _adapter.getPeriods());

        try {
            NetworkScheduler scheduler = new NetworkScheduler();
            scheduler.setAlarms(this, _adapter.getPeriods(), _settings);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void showIntervalConnectExplanation(NetworkType radio) {
        FragmentManager fm = getSupportFragmentManager();

        ExplainIntervalConnectDialog explainDlg = ExplainIntervalConnectDialog
                .newInstance(radio, _settings.get_connectInterval(),
                        _settings.get_connectDuration());

        explainDlg.show(fm, "fragment_explain_intervalconnect");
    }
}
