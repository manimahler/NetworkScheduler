package com.manimahler.android.scheduler3g;

import com.manimahler.android.scheduler3g.NetworkScheduler.NetworkType;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ExplainIntervalConnectDialog extends DialogFragment {

	private static final String RADIO = "RADIO";
	private static final String INTERVAL = "CONNECT_INTERVAL";
	private static final String DURATION = "CONNECT_DURATION";

	private NetworkType _radio;
	private int _connectInterval;
	private double _connectDuration;

	// Factory method
	public static ExplainIntervalConnectDialog newInstance(NetworkType radio,
			int connectInterval, double connectDuration) {

		ExplainIntervalConnectDialog f = new ExplainIntervalConnectDialog();

		// Supply input as argument.
		Bundle args = new Bundle();

		saveToBundle(args, radio, connectInterval, connectDuration);

		f.setArguments(args);

		return f;
	}

	public ExplainIntervalConnectDialog() {
		// Empty constructor required for DialogFragment
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Bundle savedData;
		if (savedInstanceState != null) {
			savedData = savedInstanceState;
		} else {
			savedData = getArguments();
		}

		// read from bundle
		_radio = (NetworkType) savedData.getSerializable(RADIO);
		_connectInterval = savedData.getInt(INTERVAL);
		_connectDuration = savedData.getDouble(DURATION);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflator = getActivity().getLayoutInflater();
		View view = inflator.inflate(R.layout.alert_dialog, null);

		TextView introView = (TextView) view.findViewById(R.id.intro);
		TextView messageView = (TextView) view.findViewById(R.id.message);

		Resources resources = getActivity().getResources();

		String radioText;

		if (_radio == NetworkType.WiFi) {
			radioText = resources.getString(R.string.wifi);
		} else if (_radio == NetworkType.MobileData) {
			radioText = resources.getString(R.string.mobile_data);
		} else {
			radioText = resources.getString(R.string.bluetooth);
		}

		String explainIntroFormat = resources
				.getString(R.string.explain_interval_connection_intro);
		introView.setText(String.format(explainIntroFormat, radioText));

		String explainTextFormat;

		if (_radio == NetworkType.Bluetooth) {
			explainTextFormat = resources
					.getString(R.string.explain_interval_connection_bt_text);
		} else if (_radio == NetworkType.WiFi) {
			explainTextFormat = resources
					.getString(R.string.explain_interval_connection_wifi_text);
		} else {
			explainTextFormat = resources
					.getString(R.string.explain_interval_connection_mob_text);
		}

		messageView.setText(String.format(explainTextFormat, radioText,
				_connectInterval, _connectDuration));

		builder.setView(view);

		builder.setNegativeButton(R.string.close_dialog,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});

		// Create the AlertDialog object and return it
		AlertDialog dialog = builder.create();

		dialog.setCanceledOnTouchOutside(true);

		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		AlertDialog dialog = (AlertDialog) getDialog();

		Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

		if (cancelButton != null) {
			cancelButton.setBackgroundResource(R.drawable.dialog_button);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.

		saveToBundle(savedInstanceState, _radio, _connectInterval,
				_connectDuration);
	}

	private static void saveToBundle(Bundle args, NetworkType radio,
			int connectInterval, double connectDuration) {
		args.putSerializable(RADIO, radio);
		args.putInt(INTERVAL, connectInterval);
		args.putDouble(DURATION, connectDuration);
	}
}
