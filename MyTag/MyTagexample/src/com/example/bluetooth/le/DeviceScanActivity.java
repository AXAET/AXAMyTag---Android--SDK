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

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.comime.swdevice.DeviceListener;
import com.comime.swdevice.DeviceScanCallBack;
import com.comime.swdevice.SWDevice;
import com.comime.swdevice.SWDeviceScanManager;
import com.example.bluetooth.R;

/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
@SuppressLint("NewApi")
public class DeviceScanActivity extends ListActivity implements
		DeviceScanCallBack, DeviceListener {

	private LeDeviceListAdapter mLeDeviceListAdapter;
	private boolean mScanning;

	List<BluetoothDevice> devicelist = new ArrayList<BluetoothDevice>();// devices
	// been
	// scaned

	Handler mHandler = new Handler();
	private SWDeviceScanManager scanManager;

	private SWDevice swDevice;
	private ProgressDialog dialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setTitle(R.string.title_devices);

		scanManager = new SWDeviceScanManager(this);
		scanManager.setScanCallBack(this);
		scanLeDevice(true);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					scanManager.stopScan();
					invalidateOptionsMenu();
				}
			}, 10000);
			mScanning = true;
			scanManager.startScan();
		} else {
			mScanning = false;
			scanManager.stopScan();
		}
		invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		if (!mScanning) {
			menu.findItem(R.id.menu_stop).setVisible(false);
			menu.findItem(R.id.menu_scan).setVisible(true);
			menu.findItem(R.id.menu_refresh).setActionView(null);
		} else {
			menu.findItem(R.id.menu_stop).setVisible(true);
			menu.findItem(R.id.menu_scan).setVisible(false);
			menu.findItem(R.id.menu_refresh).setActionView(
					R.layout.actionbar_indeterminate_progress);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_scan:
			mLeDeviceListAdapter.clear();
			scanLeDevice(true);
			break;
		case R.id.menu_stop:
			scanLeDevice(false);
			invalidateOptionsMenu();
			break;
		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Initializes list view adapter.
		mLeDeviceListAdapter = new LeDeviceListAdapter();
		setListAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		mLeDeviceListAdapter.clear();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);
		if (device == null)
			return;
		swDevice = new SWDevice(DeviceScanActivity.this, device.getAddress(),
				device, DeviceScanActivity.this);
		swDevice.connectGatt();
		dialog = ProgressDialog.show(DeviceScanActivity.this, "tip",
				"Connecting");
	}

	// Adapter for holding devices found through scanning.
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public LeDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText(R.string.unknown_device);
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

	// Device scan callback.
	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		final BluetoothDevice device2 = device;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mLeDeviceListAdapter.addDevice(device2);
				mLeDeviceListAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onConnected(String tag, BluetoothDevice device) {
		Log.i("axaet", tag + "onConnected");
		if (tag.equals(swDevice.getTag())) {
			MyApplication.hashMap.put(tag, swDevice);
			Intent intent = new Intent(DeviceScanActivity.this,
					DeviceControlActivity.class);
			intent.putExtra("tag", tag);
			startActivity(intent);
		}
		dialog.dismiss();
	}

	@Override
	public void onDisconnected(String tag, BluetoothDevice device) {
		dialog.dismiss();
		Log.i("axaet", tag + "onDisconnected");
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(DeviceScanActivity.this,
						"Connect failed, please re connect", Toast.LENGTH_SHORT)
						.show();
			}
		});

	}

	@Override
	public void onGetRssi(String tag, int rssi, BluetoothDevice device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGetValue(String tag, byte[] value, BluetoothDevice device) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onWriteSuccess(String tag, byte[] value, BluetoothDevice device) {
		// TODO Auto-generated method stub

	}
}