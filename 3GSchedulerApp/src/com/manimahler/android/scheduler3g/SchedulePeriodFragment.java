package com.manimahler.android.scheduler3g;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

import com.manimahler.android.scheduler3g.FlowLayout.LayoutParams;

public class SchedulePeriodFragment extends DialogFragment {

	// Container Activity must implement this interface
	public interface OnPeriodUpdatedListener {
		public void onPeriodUpdated(EnabledPeriod period);
	}

	EnabledPeriod _enabledPeriod;
	OnPeriodUpdatedListener _listener;

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
		builder.setTitle("Set Time Period");
		builder.setIcon(R.drawable.ic_launcher);

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

		CheckBox toggleWifi = (CheckBox) view.findViewById(R.id.checkBoxWifi);
		toggleWifi.setChecked(_enabledPeriod.is_wifi());
		toggleWifi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onToggleWifiClicked(v);
			}
		});

		CheckBox toggleBluetooth = (CheckBox) view
				.findViewById(R.id.checkBoxBluetooth);
		toggleBluetooth.setChecked(_enabledPeriod.is_bluetooth());
		toggleBluetooth.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				onToggleBluetoothClicked(buttonView);				
			}
		});

		AlertDialog dialog = builder.create();

		return dialog;
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

	@Override
	public void show(FragmentManager fm, String tag) {

		super.show(fm, tag);

		// ((Button)getDialog().findViewById(android.R.id.button1)).setBackgroundResource(R.drawable.time_button);

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

		String[] weekdays = DateTimeUtils.getShortWeekdays();

		for (int i = 0; i < weekdays.length; i++) {
			String day = weekdays[i];

			ToggleButton button = (ToggleButton) inflater.inflate(
					R.layout.toggle_button_weekday, container);
			button.setTag(i);

			button.setText(day.toUpperCase());
			button.setTextOff(day.toUpperCase());
			button.setTextOn(day.toUpperCase());
			button.setChecked(_enabledPeriod.get_weekDays()[i]);

			button.setTextColor(getResources().getColorStateList(
					R.drawable.toggle_button_textcolor));

			button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					buttonDayToggleClicked(buttonView, isChecked);
				}
			});

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
	}

	public void onToggleWifiClicked(View v) {
		boolean isChecked = ((CheckBox) v).isChecked();
		_enabledPeriod.set_wifi(isChecked);
	}

	public void onToggleBluetoothClicked(View v) {
		boolean isChecked = ((CheckBox) v).isChecked();
		_enabledPeriod.set_bluetooth(isChecked);
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
