package com.manimahler.android.scheduler3g;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothActionBroadCastReceiver extends BroadcastReceiver {

	private static final String TAG = BluetoothActionBroadCastReceiver.class
			.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(TAG, "Received bluetooth action " + intent.getAction());

		
		if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
			PersistenceUtils.saveBluetoothState(context, true);
		}
		else if (intent.getAction().equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
			PersistenceUtils.saveBluetoothState(context, false);
		}
		
	}

}
