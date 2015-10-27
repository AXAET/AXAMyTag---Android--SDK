package com.comime.swdevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

public class SWDeviceScanManager {

	private BluetoothAdapter bluetoothAdapter;
	private BluetoothManager bluetoothManager;
	private Context context;
	private DeviceScanCallBack scanCallBack;

	/**
	 * 
	 * @param context
	 */
	public SWDeviceScanManager(Context context) {
		this.context = context;
		init();
	}

	/**
	 * 
	 * @param scanCallBack
	 */
	public void setScanCallBack(DeviceScanCallBack scanCallBack) {
		this.scanCallBack = scanCallBack;
	}

	/**
	 * 
	 * @return the phone is supportBle or not
	 */
	public boolean supportBle() {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			return true;
		}
		return false;
	}

	private void init() {
		bluetoothManager = (BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}
	}

	public boolean bluetoothIsEnable() {
		return bluetoothAdapter.isEnabled();
	}

	/**
	 * start scan devices, the result will be on DeviceScanCallBack.
	 * onLeScan(BluetoothDevice device, final int rssi, byte[] scanRecord);
	 */

	public void startScan() {
		bluetoothAdapter.startLeScan(callback);

	}

	/**
	 * stopScan
	 */
	public void stopScan() {
		bluetoothAdapter.stopLeScan(callback);
	}

	private LeScanCallback callback = new LeScanCallback() {

		@Override
		public void onLeScan(BluetoothDevice device, final int rssi,
				byte[] scanRecord) {
			scanCallBack.onLeScan(device, rssi, scanRecord);
		}
	};
}
