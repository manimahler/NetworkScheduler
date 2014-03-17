package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff.Mode;
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
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.manimahler.android.scheduler3g.FlowLayout.LayoutParams;

public class SchedulePeriodFragment extends DialogFragment {

	// Container Activity must implement this interface
	public interface OnPeriodUpdatedListener {
		public void onPeriodUpdated(EnabledPeriod period);
	}

	EnabledPeriod _enabledPeriod;
	OnPeriodUpdatedListener _listener;

	View _view;

	// Factory method
	public static SchedulePeriodFragment newInstance(EnabledPeriod enabledPeriod) {

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
						Log.d("SchedulePeriodFragment", "clicked ok...");
						_listener.onPeriodUpdated(_enabledPeriod);
					}
				});

		Bundle savedData;
		if (savedInstanceState != null) {
			savedData = savedInstanceState;
		} else {
			savedData = getArguments();
		}

		_enabledPeriod = new EnabledPeriod(savedData);

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
				onToggleMobileDataClicked(v);
			}
		});
		updateCheckboxAppearance(toggleMobileData,
				R.drawable.ic_action_mobile_data);

		CheckBox toggleWifi = (CheckBox) view.findViewById(R.id.checkBoxWifi);
		toggleWifi.setChecked(_enabledPeriod.is_wifi());
		toggleWifi.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onToggleWifiClicked(v);
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
						boolean isChecked1 = ((CheckBox) buttonView).isChecked();
						_enabledPeriod.set_bluetooth(isChecked1);
						updateCheckboxAppearance((CheckBox) buttonView, R.drawable.ic_action_bluetooth1);
					}
				});

		updateCheckboxAppearance(toggleBluetooth, R.drawable.ic_action_bluetooth1);

		// vol
		CheckBox toggleVolume = (CheckBox) view
				.findViewById(R.id.checkBoxVolume);
		toggleVolume.setChecked(_enabledPeriod.is_volume());
		toggleVolume.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// boolean isChecked1 = ((CheckBox) buttonView).isChecked();
				_enabledPeriod.set_volume(isChecked);
				updateCheckboxAppearance((CheckBox) buttonView,
						R.drawable.ic_action_volume_up);
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
						
						if (! _enabledPeriod.is_wifi())
						{
							// disabled, ignore
							return;
						}
						
						_enabledPeriod.set_intervalConnectWifi(isChecked);
						updateCheckboxAppearance((CheckBox) buttonView,
								R.drawable.ic_action_interval,
								_enabledPeriod.is_wifi());
					}
				});
		updateCheckboxAppearance(checkBoxIntervalConnectWifi,
				R.drawable.ic_action_interval);

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
						
						if (! _enabledPeriod.is_mobileData())
						{
							// disabled, ignore
							return;
						}
						
						_enabledPeriod.set_intervalConnectMobData(isChecked);
						updateCheckboxAppearance((CheckBox) buttonView,
								R.drawable.ic_action_interval,
								_enabledPeriod.is_mobileData());
					}
				});
		updateCheckboxAppearance(checkBoxIntervalConnectMobData,
				R.drawable.ic_action_interval);

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

	// @Override
	// public View onCreateView(LayoutInflater inflater, ViewGroup container,
	// Bundle savedInstanceState) {
	//
	// // View view = inflater.inflate(R.layout.fragment_schedule_period,
	// container);
	// //
	// // getDialog().setTitle("Set Time Period");
	//
	// Bundle savedData;
	// if (savedInstanceState != null)
	// {
	// savedData = savedInstanceState;
	// }
	// else
	// {
	// savedData = getArguments();
	// }
	//
	// _enabledPeriod = new EnabledPeriod(savedData);
	//
	// // start time
	// Button timeStart = (Button)view.findViewById(R.id.buttonTimeStart);
	// setButtonTime(_enabledPeriod.get_startTimeMillis(), timeStart);
	//
	// timeStart.setOnClickListener(new View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// buttonStartClicked(v);
	// }
	// });
	//
	// // end time
	// Button timeStop = (Button)view.findViewById(R.id.buttonTimeStop);
	// setButtonTime(_enabledPeriod.get_endTimeMillis(), timeStop);
	//
	// timeStop.setOnClickListener(new View.OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// buttonStopClicked(v);
	// }
	// });
	//
	// // week days
	// FlowLayout flowlayout =
	// (FlowLayout)view.findViewById(R.id.flowlayout_weekdays);
	// inflateWeekdays(inflater, container, flowlayout);
	//
	// return view;
	// }

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

	// @Override
	// public void show(FragmentManager fm, String tag) {
	//
	// super.show(fm, tag);
	//
	// //
	// ((Button)getDialog().findViewById(android.R.id.button1)).setBackgroundResource(R.drawable.time_button);
	//
	// }

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

	public void onToggleMobileDataClicked(View v) {
		
		boolean isChecked = ((CheckBox) v).isChecked();
		
		_enabledPeriod.set_mobileData(isChecked);
		
		updateCheckboxAppearance((CheckBox) v, R.drawable.ic_action_mobile_data);
		
		Dialog dialog = getDialog();

		if (dialog != null) {
			CheckBox checkBoxIntervalConnectMob = (CheckBox) dialog
					.findViewById(R.id.checkBoxScheduleIntervalMob);
			
			updateCheckboxAppearance(checkBoxIntervalConnectMob,
					R.drawable.ic_action_interval, isChecked);
		}
	}

	public void onToggleWifiClicked(View v) {
		
		boolean isChecked = ((CheckBox) v).isChecked();
		
		_enabledPeriod.set_wifi(isChecked);
		
		updateCheckboxAppearance((CheckBox) v, R.drawable.ic_action_wifi);
		
		Dialog dialog = getDialog();

		if (dialog != null) {
			CheckBox checkBoxIntervalConnectWifi = (CheckBox) dialog
					.findViewById(R.id.checkBoxScheduleIntervalWifi);
			
			updateCheckboxAppearance(checkBoxIntervalConnectWifi,
					R.drawable.ic_action_interval, isChecked);
		}
	}

	public void buttonStartClicked(View v) {

		FragmentManager fm = getActivity().getSupportFragmentManager();

		DialogFragment timePicker = new TimePickerFragment(
				_enabledPeriod.get_startTimeMillis()) {
			@Override
			public void onTimeSetAndDone(int hourOfDay, int minute) {
				Log.d("SchedulePeriodFragment", "chosen new start hour: "
						+ hourOfDay);
				startTimePicked(hourOfDay, minute);
			}
		};

		timePicker.show(fm, "timePickerStart");
	}

	private void updateCheckboxAppearance(CheckBox v, int iconResourceId) {
		boolean checkBoxEnabled = true;
		updateCheckboxAppearance(v, iconResourceId, checkBoxEnabled);
	}

	private void updateCheckboxAppearance(CheckBox v, int iconResourceId,
			boolean checkBoxEnabled) {

		int tintColorId;

		boolean isChecked = v.isChecked();
		
		if (!checkBoxEnabled && isChecked) {
			tintColorId = R.color.weak_grey_transparent;
		} else {
			tintColorId = R.color.button_unchecked;
		}
		
		Drawable icon = ViewUtils.getTintedIcon(getActivity(), 
				!checkBoxEnabled || !isChecked, tintColorId, iconResourceId);
		
//		
//
//		int tint = getResources().getColor(tintColorId);
//
//		Drawable icon = getActivity().getResources()
//				.getDrawable(iconResourceId);
//		if (!checkBoxEnabled || !isChecked) {
//			icon.mutate().setColorFilter(tint, Mode.MULTIPLY);
//		} else {
//			icon.mutate().clearColorFilter();
//		}

		v.setButtonDrawable(icon);
		
		if (!checkBoxEnabled)
		{
			v.setTextColor(getResources().getColor(R.color.weak_grey_transparent));
		}
		else if (isChecked)
		{
			v.setTextColor(getResources().getColor(android.R.color.white));
		}
		else
		{
			v.setTextColor(getResources().getColor(R.color.toggle_button_unchecked));
		}
	}

	private void startTimePicked(int hourOfDay, int minute) {

		Log.d("SchedulePeriodFragment", "Picked end time " + hourOfDay + ":"
				+ minute);

		long nextStartTimeInMillis = DateTimeUtils.getNextTimeIn24hInMillis(
				hourOfDay, minute);

		_enabledPeriod.set_startTimeMillis(nextStartTimeInMillis);

		setButtonTime(nextStartTimeInMillis, R.id.buttonTimeStart);
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

		Log.d("SchedulePeriodFragment", "Picked end time " + hourOfDay + ":"
				+ minute);

		long nextEndTimeInMillis = DateTimeUtils.getNextTimeIn24hInMillis(
				hourOfDay, minute);

		_enabledPeriod.set_endTimeMillis(nextEndTimeInMillis);

		setButtonTime(nextEndTimeInMillis, R.id.buttonTimeStop);
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
