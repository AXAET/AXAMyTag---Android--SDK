package com.comime.swdevice;

import android.bluetooth.BluetoothDevice;

public interface DeviceListener {
	/**
	 * 
	 * @param tag
	 *            - the SWDevice's tag
	 * @param device
	 *            - the connected BluetoothDevice
	 */
	public void onConnected(String tag, BluetoothDevice device);

	/**
	 * 
	 * @param tag
	 *            - the SWDevice's tag
	 * @param device
	 *            - the disconnected BluetoothDevice
	 */
	public void onDisconnected(String tag, BluetoothDevice device);

	/**
	 * 
	 * @param tag
	 *            - the SWDevice's tag
	 * @param rssi
	 * @param device
	 */
	public void onGetRssi(String tag, int rssi, BluetoothDevice device);

	/**
	 * 
	 * @param tag
	 * @param value
	 *            - the SWDevice's command; see SWDevice's final int Command
	 * @param device
	 */
	public void onGetValue(String tag, byte[] value, BluetoothDevice device);

	/**
	 * 
	 * @param tag
	 * @param value
	 *            - the setting successfully sent
	 * @param device
	 */
	public void onWriteSuccess(String tag, byte[] value, BluetoothDevice device);

}
