package com.comime.swdevice;

import android.bluetooth.BluetoothDevice;

public interface DeviceScanCallBack {
	/**
	 * scan result; see SWDeviceScanManager.startScan()
	 * 
	 * @param device the device been scanned
	 * @param rssi
	 * @param scanRecord
	 */
	public void onLeScan(BluetoothDevice device, final int rssi,
			byte[] scanRecord);
}
