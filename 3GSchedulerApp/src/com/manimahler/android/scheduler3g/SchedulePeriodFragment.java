package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.manimahler.android.scheduler3g.FlowLayout.LayoutParams;

public class SchedulePeriodFragment extends DialogFragment {

	private static final String TAG = SchedulePeriodFragment.class.getSimpleName();
	
	// Container Activity must implement this interface
	public interface OnPeriodUpdatedListener {
		public void onPeriodUpdated(ScheduledPeriod period);
	}

	ScheduledPeriod _enabledPeriod;
	OnPeriodUpdatedListener _listener;

	View _view;

	// Factory method
	public static SchedulePeriodFragment newInstance(
			ScheduledPeriod enabledPeriod) {

		SchedulePeriodFragment f = new SchedulePeriodFragment();

		// Supply input as argument.
		Bundle args = new Bundle();

		enabledPeriod.saveToBundle(args);

		f.setArguments(args);

		return f;
	}

	public SchedulePeriodFragment() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflator = getActivity().getLayoutInflater();
		View view = inflator.inflate(R.layout.fragment_schedule_period, null);

		builder.setView(view);

		builder.setNegativeButton(android.R.string.no, null);
		builder.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d(TAG, "clicked ok...");
						
						_listener.onPeriodUpdated(_enabledPeriod);
					}
				});

		Bundle savedData;
		if (savedInstanceState != null) {
			savedData = savedInstanceState;
		} else {
			savedData = getArguments();
		}

		_enabledPeriod = new ScheduledPeriod(savedData);

		// name
		EditText editTextName = (EditText) view.findViewById(R.id.editTextName);
		editTextName.setText(_enabledPeriod.get_name());
		editTextName.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				_enabledPeriod.set_name(s.toString());

			}
		});

		// check boxes to schedule start / stop
		CheckBox checkBoxScheduleStart = (CheckBox) view
				.findViewById(R.id.checkBoxScheduleStart);
		CheckBox checkBoxScheduleStop = (CheckBox) view
				.findViewById(R.id.checkBoxScheduleStop);

		checkBoxScheduleStart.setChecked(_enabledPeriod.is_scheduleStart());
		checkBoxScheduleStart
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						_enabledPeriod.set_scheduleStart(isChecked);
					}
				});

		checkBoxScheduleStop.setChecked(_enabledPeriod.is_scheduleStop());
		checkBoxScheduleStop
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						_enabledPeriod.set_scheduleStop(isChecked);
					}
				});

		// start time
		Button timeStart = (Button) view.findViewById(R.id.buttonTimeStart);
		setButtonTime(_enabledPeriod.get_startTimeMillis(), timeStart);

		timeStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				buttonStartClicked(v);
			}
		});

		// end time
		Button timeStop = (Button) view.findViewById(R.id.buttonTimeStop);
		setButtonTime(_enabledPeriod.get_endTimeMillis(), timeStop);

		timeStop.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				buttonStopClicked(v);
			}
		});

		// week days
		FlowLayout flowlayout = (FlowLayout) view
				.findViewById(R.id.flowlayout_weekdays);
		inflateWeekdays(inflator, null, flowlayout);

		// TODO: make custom check box with the desired behavior
		// network sensors
		CheckBox toggleMobileData = (CheckBox) view
				.findViewById(R.id.checkBoxMobileData);
		toggleMobileData.setChecked(_enabledPeriod.is_mobileData());
		toggleMobileData.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onToggleMobileData(v);
			}
		});
		updateCheckboxAppearance(toggleMobileData,
				R.drawable.ic_action_mobile_data);

		CheckBox toggleWifi = (CheckBox) view.findViewById(R.id.checkBoxWifi);
		toggleWifi.setChecked(_enabledPeriod.is_wifi());
		toggleWifi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onToggleWifi(v);
			}
		});
		updateCheckboxAppearance(toggleWifi, R.drawable.ic_action_wifi);

		// bt
		CheckBox toggleBluetooth = (CheckBox) view
				.findViewById(R.id.checkBoxBluetooth);
		toggleBluetooth.setChecked(_enabledPeriod.is_bluetooth());
		toggleBluetooth
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						boolean isChecked1 = ((CheckBox) buttonView)
								.isChecked();
						_enabledPeriod.set_bluetooth(isChecked1);
						updateCheckboxAppearance((CheckBox) buttonView,
								R.drawable.ic_action_bluetooth1);
					}
				});

		updateCheckboxAppearance(toggleBluetooth,
				R.drawable.ic_action_bluetooth1);

		// vol
		CheckBox toggleVolume = (CheckBox) view
				.findViewById(R.id.checkBoxVolume);
		toggleVolume.setChecked(_enabledPeriod.is_volume());
		toggleVolume.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				onToggleVolume(buttonView, isChecked);
			}
		});

		updateCheckboxAppearance(toggleVolume, R.drawable.ic_action_volume_up);

		// check box interval connect wifi
		CheckBox checkBoxIntervalConnectWifi = (CheckBox) view
				.findViewById(R.id.checkBoxScheduleIntervalWifi);
		checkBoxIntervalConnectWifi.setChecked(_enabledPeriod
				.is_intervalConnectWifi());
		checkBoxIntervalConnectWifi
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						onToggleWifiInterval((CheckBox)buttonView, isChecked);
					}
				});
		
		updateCheckBoxIntervalConnectWifi(checkBoxIntervalConnectWifi);
		
		// check box interval connect mob data
		CheckBox checkBoxIntervalConnectMobData = (CheckBox) view
				.findViewById(R.id.checkBoxScheduleIntervalMob);
		checkBoxIntervalConnectMobData.setChecked(_enabledPeriod
				.is_intervalConnectMobData());
		checkBoxIntervalConnectMobData
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						onToggleMobileDataInterval((CheckBox)buttonView, isChecked);
					}
				});
		
		updateCheckBoxIntervalConnectMobData(checkBoxIntervalConnectMobData);
		
		
		// check box vibrate when silent
		CheckBox checkBoxVibrate = (CheckBox) view
				.findViewById(R.id.checkBoxVolumeVibrate);
		checkBoxVibrate.setChecked(_enabledPeriod
				.is_vibrateWhenSilent());
		checkBoxVibrate
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						onToggleVibrateWhenSilent((CheckBox)buttonView, isChecked);
					}
				});
		
		updateCheckBoxVibrateWhenSilent(checkBoxVibrate);
		
		
		AlertDialog dialog = builder.create();

		_view = view;

		return dialog;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		Button okButton = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_POSITIVE);
		Button cancelButton = ((AlertDialog) getDialog())
				.getButton(DialogInterface.BUTTON_NEGATIVE);

		if (cancelButton != null) {
			cancelButton.setBackgroundResource(R.drawable.dialog_button);
		}

		if (okButton != null) {
			// okButton.setBackgroundColor(getResources().getColor(R.color.button_blue));
			okButton.setBackgroundResource(R.drawable.dialog_button);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			_listener = (OnPeriodUpdatedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnPeriodUpdatedListener");
		}
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

		_enabledPeriod.saveToBundle(savedInstanceState);
	}

	public void inflateWeekdays(LayoutInflater inflater, ViewGroup container,
			FlowLayout flowlayout) {

		String[] weekdays = DateTimeUtils.getShortWeekdays(getActivity());

		for (int i = 0; i < weekdays.length; i++) {
			String day = weekdays[i];

			ToggleButton button = (ToggleButton) inflater.inflate(
					R.layout.toggle_button_weekday, null);

			button.setTag(i);

			button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					buttonDayToggleClicked(buttonView, isChecked);
				}
			});

			button.setText(day);
			button.setTextOff(day);
			button.setTextOn(day);
			button.setChecked(_enabledPeriod.get_weekDays()[i]);

			// this is critical: otherwise android tries to set the
			// check boxes on its own but fails to do it properly:
			// http://stackoverflow.com/questions/2512010/android-checkbox-restoring-state-after-screen-rotation
			button.setSaveEnabled(false);

			LayoutParams flowLP = new LayoutParams(5, 5);
			flowlayout.addView(button, flowLP);
		}
	}

	public void buttonDayToggleClicked(View v, boolean isChecked) {

		int dayIndex = (Integer) v.getTag();

		_enabledPeriod.get_weekDays()[dayIndex] = isChecked;
	}

	public void onToggleMobileData(View v) {

		// to avoid the scroll view from jumping back up 
		v.requestFocusFromTouch();
		
		boolean isChecked = ((CheckBox) v).isChecked();

		_enabledPeriod.set_mobileData(isChecked);

		updateCheckboxAppearance((CheckBox) v, R.drawable.ic_action_mobile_data);
		
		Dialog dialog = getDialog();
		
		if (dialog != null) {
			CheckBox checkBoxIntervalConnectMob = (CheckBox) dialog
					.findViewById(R.id.checkBoxScheduleIntervalMob);
			
			updateCheckBoxIntervalConnectMobData(checkBoxIntervalConnectMob);
		}
	}
	
	private void onToggleWifi(View v) {

		// to avoid the scroll view from jumping back up 
		v.requestFocusFromTouch();
		
		boolean isChecked = ((CheckBox) v).isChecked();

		_enabledPeriod.set_wifi(isChecked);

		updateCheckboxAppearance((CheckBox) v, R.drawable.ic_action_wifi);

		Dialog dialog = getDialog();

		if (dialog != null) {
			CheckBox checkBoxIntervalConnectWifi = (CheckBox) dialog
					.findViewById(R.id.checkBoxScheduleIntervalWifi);

			updateCheckBoxIntervalConnectWifi(checkBoxIntervalConnectWifi);
		}
	}
	
	private void onToggleMobileDataInterval(
			CheckBox checkBox, boolean isChecked) {
		
		// to avoid the scroll view from jumping back up 
		checkBox.requestFocusFromTouch();
		
		if (!_enabledPeriod.is_mobileData() ||
				!_enabledPeriod.activeIsEnabled()) {
			// disabled, ignore
			makeIntervalConnectNotSupportedToast();
			checkBox.setChecked(false);
			return;
		} else {
			_enabledPeriod.set_intervalConnectMobData(isChecked);
		}
		
		updateCheckBoxIntervalConnectMobData(checkBox);
	}
	
	private void onToggleWifiInterval(
			CheckBox checkBox, boolean isChecked) {
		
		// to avoid the scroll view from jumping back up 
		checkBox.requestFocusFromTouch();
		
		if (!_enabledPeriod.is_wifi() ||
				!_enabledPeriod.activeIsEnabled()) {
			// disabled, ignore
			makeIntervalConnectNotSupportedToast();
			checkBox.setChecked(false);
			return;
		} else {
			_enabledPeriod.set_intervalConnectWifi(isChecked);
		}
		
		updateCheckBoxIntervalConnectWifi(checkBox);
	}
	
	private void onToggleVolume(CompoundButton checkBox,
			boolean isChecked) {

		// to avoid the scroll view from jumping back up 
		checkBox.requestFocusFromTouch();
		
		_enabledPeriod.set_volume(isChecked);
		updateCheckboxAppearance((CheckBox) checkBox,
				R.drawable.ic_action_volume_up);
		
		Dialog dialog = getDialog();
		
		if (dialog != null) {
			CheckBox checkBoxVibrate = (CheckBox) dialog
					.findViewById(R.id.checkBoxVolumeVibrate);
			
			updateCheckBoxVibrateWhenSilent(checkBoxVibrate);
		}
	}
	
	private void onToggleVibrateWhenSilent(CheckBox checkBox,
			boolean isChecked) {
		
		// to avoid the scroll view from jumping back up 
		checkBox.requestFocusFromTouch();
		
		if (!_enabledPeriod.is_volume()) {
			checkBox.setChecked(false);
			return;
		} else {
			_enabledPeriod.set_vibrateWhenSilent(isChecked);
		}
		
		updateCheckBoxVibrateWhenSilent(checkBox);
	}

	private void updateCheckBoxVibrateWhenSilent(CheckBox checkBox) {
		boolean checkBoxEnabled = _enabledPeriod.is_volume();
		
		boolean strikeThrough = false;
		
		updateCheckboxAppearance(checkBox,
				R.drawable.ic_action_vibrate, // TODO
				checkBoxEnabled, strikeThrough);
	}

	private void buttonStartClicked(View v) {

		FragmentManager fm = getActivity().getSupportFragmentManager();

		DialogFragment timePicker = new TimePickerFragment(
				_enabledPeriod.get_startTimeMillis()) {
			@Override
			public void onTimeSetAndDone(int hourOfDay, int minute) {
				Log.d(TAG, "chosen new start hour: "
						+ hourOfDay);
				startTimePicked(hourOfDay, minute);
			}
		};

		timePicker.show(fm, "timePickerStart");
	}

	private void updateCheckBoxIntervalConnectMobData(CheckBox checkBox) {
		
		updateCheckBoxIntervalConnect(checkBox, _enabledPeriod.is_mobileData());
	}
	
	private void updateCheckBoxIntervalConnectWifi(CheckBox checkBox) {
		
		updateCheckBoxIntervalConnect(checkBox, _enabledPeriod.is_wifi());
	}
	
	private void updateCheckBoxIntervalConnect(CheckBox checkBox, boolean intervalConnectActive) {
		
		boolean checkBoxEnabled = _enabledPeriod.activeIsEnabled() && intervalConnectActive;
		
		boolean strikeThrough = false;
		
		updateCheckboxAppearance(checkBox,
				R.drawable.ic_action_interval,
				checkBoxEnabled, strikeThrough);
	}

	private void updateCheckboxAppearance(CheckBox v, int iconResourceId) {
		boolean checkBoxEnabled = true;
		boolean strikeThrough = !_enabledPeriod.activeIsEnabled();
		updateCheckboxAppearance(v, iconResourceId, checkBoxEnabled,
				strikeThrough);
	}

	private void updateCheckboxAppearance(CheckBox v, int iconResourceId,
			boolean checkBoxEnabled, boolean strikeThrough) {

		int tintColorId;

		boolean isChecked = v.isChecked();

		if (!checkBoxEnabled && isChecked) {
			tintColorId = R.color.weak_grey_transparent;
		} else {
			tintColorId = R.color.button_unchecked;
		}

		Drawable icon = ViewUtils.getTintedIcon(getActivity(), !checkBoxEnabled
				|| !isChecked, tintColorId, iconResourceId);

		if (strikeThrough) {
			Drawable strike = ViewUtils.getTintedIcon(getActivity(),
					!checkBoxEnabled || !isChecked, tintColorId,
					R.drawable.ic_strikethrough);

			Drawable[] layers = new Drawable[2];
			layers[0] = icon;
			layers[1] = strike;

			icon = new LayerDrawable(layers);
		}

		v.setButtonDrawable(icon);

		if (!checkBoxEnabled) {
			v.setTextColor(getResources().getColor(
					R.color.weak_grey_transparent));
		} else if (isChecked) {
			v.setTextColor(getResources().getColor(android.R.color.white));
		} else {
			v.setTextColor(getResources().getColor(
					R.color.toggle_button_unchecked));
		}
	}

	private void makeIntervalConnectNotSupportedToast() {
		
		Context context = getActivity();
		
		if (context == null) {
			// it's not worth it
			return;
		}
		
		Toast.makeText(context, R.string.interval_connect_disabling_period, Toast.LENGTH_LONG).show();
	}

	private void startTimePicked(int hourOfDay, int minute) {

		Log.d(TAG, "Picked start time " + hourOfDay + ":"
				+ minute);

		long nextStartTimeInMillis = DateTimeUtils.getNextTimeIn24hInMillis(
				hourOfDay, minute);

		boolean activeIsEnabledBefore = DateTimeUtils.isEarlierInTheDay(
				_enabledPeriod.get_startTimeMillis(),
				_enabledPeriod.get_endTimeMillis());

		boolean activeIsEnabledAfter = DateTimeUtils.isEarlierInTheDay(
				nextStartTimeInMillis, _enabledPeriod.get_endTimeMillis());

		_enabledPeriod.set_startTimeMillis(nextStartTimeInMillis);

		setButtonTime(nextStartTimeInMillis, R.id.buttonTimeStart);

		updateCheckBoxesOnTimeChange(activeIsEnabledBefore,
				activeIsEnabledAfter);
	}

	private void updateCheckBoxesOnTimeChange(boolean activeIsEnabledBefore,
			boolean activeIsEnabledAfter) {

		if (activeIsEnabledBefore != activeIsEnabledAfter) {
			String message;
			if (activeIsEnabledAfter) {
				message = "Start is before stop. Scheduled period starts networks";
			} else {
				message = "Stop is before start. Scheduled period disables networks";
			}

			Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

			CheckBox wifi = (CheckBox) _view.findViewById(R.id.checkBoxWifi);
			updateCheckboxAppearance(wifi, R.drawable.ic_action_wifi);

			CheckBox mob = (CheckBox) _view
					.findViewById(R.id.checkBoxMobileData);
			updateCheckboxAppearance(mob, R.drawable.ic_action_mobile_data);

			CheckBox bt = (CheckBox) _view.findViewById(R.id.checkBoxBluetooth);
			updateCheckboxAppearance(bt, R.drawable.ic_action_bluetooth1);

			CheckBox vol = (CheckBox) _view.findViewById(R.id.checkBoxVolume);
			updateCheckboxAppearance(vol, R.drawable.ic_action_volume_up);

			CheckBox intervalConnectWifi = (CheckBox) _view
					.findViewById(R.id.checkBoxScheduleIntervalWifi);
			updateCheckboxAppearance(intervalConnectWifi,
					R.drawable.ic_action_interval, activeIsEnabledAfter, false);

			CheckBox intervalConnectMob = (CheckBox) _view
					.findViewById(R.id.checkBoxScheduleIntervalMob);
			updateCheckboxAppearance(intervalConnectMob,
					R.drawable.ic_action_interval, activeIsEnabledAfter, false);
		}
	}

	public void buttonStopClicked(View v) {

		FragmentManager fm = getActivity().getSupportFragmentManager();

		DialogFragment timePicker = new TimePickerFragment(
				_enabledPeriod.get_endTimeMillis()) {
			@Override
			public void onTimeSetAndDone(int hourOfDay, int minute) {
				// Do something with the time chosen by the user
				endTimePicked(hourOfDay, minute);
			}
		};

		timePicker.show(fm, "timePickerStop");
	}

	private void endTimePicked(int hourOfDay, int minute) {

		Log.d(TAG, "Picked end time " + hourOfDay + ":"
				+ minute);

		long nextEndTimeInMillis = DateTimeUtils.getNextTimeIn24hInMillis(
				hourOfDay, minute);

		boolean activeIsEnabledBefore = DateTimeUtils.isEarlierInTheDay(
				_enabledPeriod.get_startTimeMillis(),
				_enabledPeriod.get_endTimeMillis());

		boolean activeIsEnabledAfter = DateTimeUtils.isEarlierInTheDay(
				_enabledPeriod.get_startTimeMillis(), nextEndTimeInMillis);

		_enabledPeriod.set_endTimeMillis(nextEndTimeInMillis);

		setButtonTime(nextEndTimeInMillis, R.id.buttonTimeStop);

		updateCheckBoxesOnTimeChange(activeIsEnabledBefore,
				activeIsEnabledAfter);
	}

	private void setButtonTime(long timeInMillis, int buttonId) {

		Dialog dialog = getDialog();

		Button button = (Button) dialog.findViewById(buttonId);

		setButtonTime(timeInMillis, button);
	}

	private void setButtonTime(long timeInMillis, Button button) {

		Calendar calendar = Calendar.getInstance();

		if (timeInMillis <= 0) {
			calendar.setTimeInMillis(System.currentTimeMillis());
		} else {
			calendar.setTimeInMillis(timeInMillis);
		}

		String text = DateTimeUtils.getHourMinuteText(getActivity(), calendar);

		button.setText(text);
	}

}
