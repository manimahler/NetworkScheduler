package com.manimahler.android.scheduler3g;

import java.io.File;
import java.text.DecimalFormat;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

// TODO: turn into a PreferenceFragment once gingerbread support is dropped...
public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
			initSummary(getPreferenceScreen().getPreference(i));
		}

		Preference buttonShowLog = findPreference(this
				.getString(R.string.pref_key_logging_show));
		buttonShowLog
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						openLogFile(arg0.getContext());
						return true;
					}
				});

		Preference buttonDeleteLog = findPreference(this
				.getString(R.string.pref_key_logging_delete));
		buttonDeleteLog
				.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
					@Override
					public boolean onPreferenceClick(Preference arg0) {
						deleteLogFile(arg0.getContext());
						updatePrefAppearance(arg0);
						return true;
					}
				});
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		super.onPreferenceTreeClick(preferenceScreen, preference);

		// If the user has clicked on a preference screen, set up the action bar
		// to have the up-navigation caret visible:
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
				&& preference instanceof PreferenceScreen) {
			initializeSubScreen((PreferenceScreen) preference);
		}

		return false;
	}

	/**
	 * Sets up the action bar for a {@link PreferenceScreen} This enables the
	 * caret (up-navigation on the preference's sub-screens) Taken from
	 * http://stackoverflow
	 * .com/questions/18155036/add-up-button-to-preferencescreen and adapted
	 * with additional onDismiss listener
	 * */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initializeSubScreen(PreferenceScreen preferenceScreen) {

		Log.d(TAG, "Initializing action bar..");
		final Dialog dialog = preferenceScreen.getDialog();

		if (dialog != null) {

			final boolean updateUnlockPolicy = preferenceScreen
					.getKey()
					.equals(this
							.getString(R.string.pref_key_unlock_policy_preferences));

			final boolean updateIntervalConnect = preferenceScreen
					.getKey()
					.equals(this
							.getString(R.string.pref_key_interval_connect_preferences));

			OnDismissListener dismissListener = new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog) {

					if (updateUnlockPolicy) {
						setGroupPreferenceSummaryUnlockPolicy();
					}

					if (updateIntervalConnect) {
						setGroupPreferenceSummaryIntervalConnect();
					}
				}
			};

			// first listen to onDismiss to update the summary of the
			// unlock-policy in the main screen
			dialog.setOnDismissListener(dismissListener);

			// Inialize the action bar
			dialog.getActionBar().setDisplayHomeAsUpEnabled(true);

			// Apply custom home button area click listener to close the
			// PreferenceScreen because PreferenceScreens are dialogs which
			// swallow
			// events instead of passing to the activity
			// Related Issue:
			// https://code.google.com/p/android/issues/detail?id=4611
			View homeBtn = dialog.findViewById(android.R.id.home);

			if (homeBtn != null) {
				OnClickListener dismissDialogClickListener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				};

				// Prepare yourselves for some hacky programming
				ViewParent homeBtnContainer = homeBtn.getParent();

				// The home button is an ImageView inside a FrameLayout
				if (homeBtnContainer instanceof FrameLayout) {
					ViewGroup containerParent = (ViewGroup) homeBtnContainer
							.getParent();

					if (containerParent instanceof LinearLayout) {
						// This view also contains the title text, set the whole
						// view as clickable
						((LinearLayout) containerParent)
								.setOnClickListener(dismissDialogClickListener);
					} else {
						// Just set it on the home button
						((FrameLayout) homeBtnContainer)
								.setOnClickListener(dismissDialogClickListener);
					}
				} else {
					// The 'If all else fails' default case
					homeBtn.setOnClickListener(dismissDialogClickListener);
				}
			}
		}
	}

	private void openLogFile(Context context) {
		Intent intent = new Intent();

		intent.setAction(android.content.Intent.ACTION_VIEW);

		File file = UserLog.getLogFile(context);

		intent.setDataAndType(Uri.fromFile(file), "text/*");

		startActivity(intent);

	}

	private void deleteLogFile(Context context) {

		File file = UserLog.getLogFile(context);

		try {
			if (file.exists()) {
				file.delete();

				Toast.makeText(context,
						context.getString(R.string.log_file_deleted),
						Toast.LENGTH_SHORT).show();
			}

		} catch (Exception e) {
			// Unable to create file, likely because external storage is
			// not currently mounted.
			Log.w(TAG, "Error deleting log file", e);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Set up a listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		Preference preference = findPreference(key);

		if (preference == null) {
			Log.e(TAG,
					String.format("Preference with key %s does not exist", key));
			return;
		}

		updatePrefAppearance(preference);

		// if connect interval has changed -> re-schedule alarm:
		if (preference.getKey().equals(
				this.getString(R.string.pref_key_connect_interval))) {

			// if no active interval-connecting period: the alarm will be
			// cancelled on the first switch-on
			NetworkScheduler scheduler = new NetworkScheduler();
			scheduler.setupIntervalConnect(this,
					PersistenceUtils.readSettings(this));
		}
	}

	private void initSummary(Preference p) {
		if (p instanceof PreferenceGroup) {
			PreferenceGroup prefGroup = (PreferenceGroup) p;
			for (int i = 0; i < prefGroup.getPreferenceCount(); i++) {
				initSummary(prefGroup.getPreference(i));
			}
			// the sub-screen could also have a summary:
			updatePrefAppearance(p);
		} else {
			updatePrefAppearance(p);
		}
	}

	private void updatePrefAppearance(Preference p) {
		// The idea to set the summary was taken from
		// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
		// However this does not work if the summary in the strings resource
		// contains a format (%s)
		// which is replaced in here. Because the next time p.getSummary won't
		// contain the %s parameter
		// and the current value does not get inserted! There does not seem to
		// be a simple solution, so resort to hack:

		if (p.getKey() == null) {
			return;
		}

		if (p.getKey().equals(
				this.getString(R.string.pref_key_unlock_policy_preferences))) {
			setGroupPreferenceSummaryUnlockPolicy(p);
		}

		if (p.getKey().equals(
				this.getString(R.string.pref_key_interval_connect_preferences))) {

			setGroupPreferenceSummaryIntervalConnect(p);
		}

		if (p.getKey().equals(
				this.getString(R.string.pref_key_connect_interval))) {
			ensurePreferenceLarger0(p);

			updatePreferenceTitle(p, R.string.pref_title_connect_interval);
			updatePreferenceSummary(p, R.string.pref_summary_connect_interval);
		}

		if (p.getKey().equals(
				this.getString(R.string.pref_key_connect_duration))) {

			int duration = ensurePreferenceLarger0(p);

			updatePreferenceTitle(p, R.string.pref_title_connect_duration);

			if (duration == 1) {
				updatePreferenceSummary(p,
						R.string.pref_summary_connect_duration_singular);
			} else {
				updatePreferenceSummary(p,
						R.string.pref_summary_connect_duration);
			}
		}

		if (p.getKey().equals(this.getString(R.string.pref_key_delay_min))) {
			ensurePreferenceLarger0(p);

			updatePreferenceTitle(p, R.string.pref_title_delay_min);
			updatePreferenceSummary(p, R.string.pref_summary_delay_min);
		}

		if (p.getKey().equals(this.getString(R.string.pref_key_logging_show))) {

			File logFile = UserLog.getLogFile(p.getContext());

			p.setEnabled(logFile.exists());
		}

		if (p.getKey().equals(this.getString(R.string.pref_key_logging_delete))) {

			File logFile = UserLog.getLogFile(p.getContext());

			double kilobytes = 0;
			if (logFile.exists()) {
				double bytes = logFile.length();
				kilobytes = (bytes / 1024);
			} else {
				p.setEnabled(false);
			}

			String summaryFormat = this
					.getString(R.string.pref_summary_logging_delete);

			DecimalFormat df = new DecimalFormat("#0.0");
			p.setSummary(String.format(summaryFormat, df.format(kilobytes)));
		}
	}

	private int tryParsePositiveInt(String stringValue) {

		int result = -1;
		try {
			result = Integer.parseInt(stringValue);
		} catch (Exception ex) {
			// caught intentionally, the stored value is no integer
		}

		return result;
	}

	private int ensurePreferenceLarger0(Preference p) {
		EditTextPreference editTextPref = (EditTextPreference) p;
		String currentText = editTextPref.getText();
		int currentValue = 0;
		try {
			currentValue = Integer.parseInt(currentText);
		} catch (Exception e) {
			// caught intentionally
		}

		if (currentValue == 0) {
			// minimum value:
			editTextPref.setText("1");
		}

		return currentValue;
	}

	private void updatePreferenceSummary(Preference p, int summaryResId) {

		EditTextPreference editTextPref = (EditTextPreference) p;

		String textValue = editTextPref.getText();

		updatePreferenceSummary(p, summaryResId, textValue);
	}

	private void updatePreferenceSummary(Preference p, int summaryResId,
			String value) {

		String summaryFormat = this.getString(summaryResId);

		p.setSummary(String.format(summaryFormat, value));
	}

	private void updatePreferenceTitle(Preference p, int titleResId) {
		EditTextPreference editTextPref = (EditTextPreference) p;
		String textValue = editTextPref.getText();

		String titleFormat = this.getString(titleResId);

		p.setTitle(String.format(titleFormat, textValue));
	}

	private void setGroupPreferenceSummaryUnlockPolicy() {
		Preference prefUnlockGroup = findPreference(this
				.getString(R.string.pref_key_unlock_policy_preferences));

		setGroupPreferenceSummaryUnlockPolicy(prefUnlockGroup);

	}

	private void setGroupPreferenceSummaryUnlockPolicy(
			Preference unlockGroupPref) {
		int prefWifiIndex = 1;
		int prefMobiIndex = 2;
		Preference wifiPref = ((PreferenceGroup) unlockGroupPref)
				.getPreference(prefWifiIndex);
		Preference mobiPref = ((PreferenceGroup) unlockGroupPref)
				.getPreference(prefMobiIndex);

		String summaryFormat = "%s: %s";
		String wifiSummary = String.format(summaryFormat,
				this.getString(R.string.wifi), wifiPref.getSummary());
		String mobSummary = String.format(summaryFormat,
				this.getString(R.string.mobile_data), mobiPref.getSummary());

		String summary = wifiSummary + "\n" + mobSummary;

		unlockGroupPref.setSummary(summary);

		// otherwise the screen is not refreshed!
		onContentChanged();
	}

	private void setGroupPreferenceSummaryIntervalConnect(
			Preference intervalGroupPref) {

		int prefIdxConnectInterval = 1;
		int prefIdxConnectDuration = 2;
		EditTextPreference prefConnectInterval = (EditTextPreference) ((PreferenceGroup) intervalGroupPref)
				.getPreference(prefIdxConnectInterval);
		EditTextPreference prefConnectDuration = (EditTextPreference) ((PreferenceGroup) intervalGroupPref)
				.getPreference(prefIdxConnectDuration);

		String interval = prefConnectInterval.getText();
		String duration = prefConnectDuration.getText();

		String summaryFormat;
		if (tryParsePositiveInt(duration) == 1) {
			summaryFormat = this
					.getString(R.string.pref_summary_intervalconnect_group_singular);
		} else {
			summaryFormat = this
					.getString(R.string.pref_summary_intervalconnect_group);

		}

		intervalGroupPref.setSummary(String.format(summaryFormat, interval,
				duration));

		onContentChanged();
	}

	private void setGroupPreferenceSummaryIntervalConnect() {
		Preference prefUnlockGroup = findPreference(this
				.getString(R.string.pref_key_interval_connect_preferences));

		setGroupPreferenceSummaryIntervalConnect(prefUnlockGroup);

	}
}