/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.bluetooth.le;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.axaet.bluetooth.BluetoothService;
import com.example.bluetooth.le.R;

/**
 * For a given BLE device, this Activity provides the user interface to connect,
 * display data, and display GATT services and characteristics supported by the
 * device. The Activity communicates with {@code BluetoothLeService}, which in
 * turn interacts with the Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements OnClickListener {
	private final static String TAG = "DeviceControlActivity";

	public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
	public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

	private TextView mDataField;
	private TextView electricity;
	private TextView humidity;
	private TextView temperature;
	private TextView connection_state;
	private String mDeviceName;
	private String mDeviceAddress;
	private BluetoothService mBluetoothLeService;
	private Button btn_alarm, btn_mute, btn_close;
	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

	// Handles various events fired by the Service.
	// ACTION_GATT_CONNECTED: connected to a GATT server.
	// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
	// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
	// ACTION_DATA_AVAILABLE: received data from the device. This can be a
	// result of read
	// or notification operations.
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i("test",
					"Address="
							+ intent.getStringExtra(BluetoothService.DEVICEADDRESS));
			if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) {
				Log.e(TAG, "ACTION_GATT_CONNECTED");
				connection_state.setText("connected");
			} else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) {
				Log.e(TAG, "ACTION_GATT_DISCONNECTED");
				connection_state.setText("disconnected");
			} else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				Log.i(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
			} else if (BluetoothService.ACTION_DATA_ELECTRICITY.equals(action)) {
				Log.i("test",
						"ELECTRICITY="
								+ intent.getIntExtra(
										BluetoothService.EXTRA_DATA, 0));
				electricity.setText(intent.getIntExtra(
						BluetoothService.EXTRA_DATA, 0) + "");
			} else if (BluetoothService.ACTION_DATA_CLICK.equals(action)) {
				// CLICK
				Log.i("test",
						"CLICK="
								+ intent.getIntExtra(
										BluetoothService.EXTRA_DATA, 0));
				mDataField.setText("click="
						+ intent.getIntExtra(BluetoothService.EXTRA_DATA, 0));
			} else if (BluetoothService.ACTION_DATA_DOUBLECLICK.equals(action)) {
				// DOUBLECLICK
				Log.i("test",
						"DOUBLECLICK="
								+ intent.getIntExtra(
										BluetoothService.EXTRA_DATA, 0));
				mDataField.setText("doubleclick="
						+ intent.getIntExtra(BluetoothService.EXTRA_DATA, 0));
			} else if (BluetoothService.ACTION_DATA_HUMIDITY.equals(action)) {
				// HUMIDITY
				Log.i("test",
						"HUMIDITY="
								+ intent.getDoubleExtra(
										BluetoothService.EXTRA_DATA, 0));
				humidity.setText(intent.getDoubleExtra(
						BluetoothService.EXTRA_DATA, 0) + "%");
			} else if (BluetoothService.ACTION_DATA_LONG_PRESS.equals(action)) {
				// LONG_PRESS
				Log.i("test",
						"LONG_PRESS="
								+ intent.getIntExtra(
										BluetoothService.EXTRA_DATA, 0));
				mDataField.setText("long_press="
						+ intent.getIntExtra(BluetoothService.EXTRA_DATA, 0));
			} else if (BluetoothService.ACTION_DATA_SECONDARY_ALARM
					.equals(action)) {
				// SECONDARY_ALARM
				Log.i("test",
						"SECONDARY_ALARM="
								+ intent.getIntExtra(
										BluetoothService.EXTRA_DATA, 0));
				mDataField.setText("secondary_alarm="
						+ intent.getIntExtra(BluetoothService.EXTRA_DATA, 0));
			} else if (BluetoothService.ACTION_DATA_TEMPERATURE.equals(action)) {
				// TEMPERATURE
				Log.i("test",
						"TEMPERATURE="
								+ intent.getDoubleExtra(
										BluetoothService.EXTRA_DATA, 0));

				temperature.setText(intent.getDoubleExtra(
						BluetoothService.EXTRA_DATA, 0) + "��");
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gatt_services_characteristics);
		final Intent intent = getIntent();
		mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
		mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
		// Sets up UI references.
		((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
		mDataField = (TextView) findViewById(R.id.data_value);
		electricity = (TextView) findViewById(R.id.electricity);
		humidity = (TextView) findViewById(R.id.humidity);
		temperature = (TextView) findViewById(R.id.temperature);
		connection_state = (TextView) findViewById(R.id.connection_state);
		btn_alarm = (Button) findViewById(R.id.alarm);
		btn_mute = (Button) findViewById(R.id.mute);
		btn_close = (Button) findViewById(R.id.close);
		btn_alarm.setOnClickListener(this);
		btn_mute.setOnClickListener(this);
		btn_close.setOnClickListener(this);
		mBluetoothLeService = new BluetoothService(this);
		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver,
				BluetoothService.makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null) {
			boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mGattUpdateReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.alarm:
			mBluetoothLeService.alarmBLE();
			break;
		case R.id.mute:
			mBluetoothLeService.stopAlarm();
			break;
		case R.id.close:
			mBluetoothLeService.close();
			btn_alarm.setEnabled(false);
			btn_mute.setEnabled(false);
			btn_close.setEnabled(false);
			break;
		}

	}
}
