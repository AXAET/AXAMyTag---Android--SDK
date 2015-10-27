package com.comime.swdevice;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

public class SWDevice {
	/**
	 * the device has been disconnected
	 */
	public final static int DEVICE_STATUS_DISCONNECTED = 0;
	/**
	 * the device has been connected
	 */
	public final static int DEVICE_STATUS_CONNECTED = 1;

	private String tag;// Tagging Bluetooth device

	/**
	 * Bluetooth device connection state,
	 * 
	 * 0 Not connected state ,1 Connected state
	 */
	private int deviceStatus;

	private BluetoothDevice bluetoothDevice;
	private BluetoothGatt mBluetoothGatt;
	private DeviceListener deviceListener;
	private Context context;

	public SWDevice(Context context, String tag) {
		super();
		this.context = context;
		this.tag = tag;

	}

	public SWDevice(Context context, String tag, BluetoothDevice device,
			DeviceListener deviceListener) {
		this.bluetoothDevice = device;
		this.deviceListener = deviceListener;
		this.deviceStatus = DEVICE_STATUS_DISCONNECTED;
		this.tag = tag;
		this.context = context;
	}

	public void setDeviceListener(DeviceListener deviceListener) {
		this.deviceListener = deviceListener;
	}

	/**
	 * set the device tag. you can distinguish between different devices by the
	 * tag
	 * 
	 * @param tag
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	/**
	 * get the device tag
	 * 
	 * @return the device tag
	 */
	public String getTag() {
		return this.tag;
	}

	/**
	 * 
	 * @return the BluetoothDevice connected
	 */
	public BluetoothDevice getBluetoothDevice() {
		return bluetoothDevice;
	}

	/**
	 * 
	 * @param bluetoothDevice
	 */
	public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
		this.bluetoothDevice = bluetoothDevice;
	}

	/**
	 * start connect the device connectGatt and mBluetoothGatt.connect();
	 */
	public void connect() {
		if (bluetoothDevice != null) {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt.disconnect();
				mBluetoothGatt = null;
			}
			mBluetoothGatt = bluetoothDevice.connectGatt(this.context, false,
					gattCallback);
			mBluetoothGatt.connect();
		} else {
		}
	}

	/**
	 * connectGatt do not have mBluetoothGatt.connect();
	 */
	public void connectGatt() {

		if (bluetoothDevice != null) {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt.disconnect();

				mBluetoothGatt = null;
			}
			mBluetoothGatt = bluetoothDevice.connectGatt(this.context, false,
					gattCallback);
		}
	}

	public void discoveryService() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.discoverServices();
		}
	}

	/**
	 * cancel the connection with the device
	 */
	public void disconnect() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	/**
	 * remove the connection with the device
	 */
	public void remove() {
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt.disconnect();
			mBluetoothGatt = null;
		}
		deviceStatus = DEVICE_STATUS_DISCONNECTED;
	}

	/**
	 * Descriptor value to read from the remote device
	 * 
	 * @return true, if the read operation was initiated successfully
	 */
	public boolean readRssi() {
		if (mBluetoothGatt != null && isConnected()) {
			return mBluetoothGatt.readRemoteRssi();
		}
		return false;
	}

	/**
	 * get the device is connected or not
	 * 
	 * @return true is connected,false not
	 */
	public boolean isConnected() {
		if (deviceStatus == 1) {
			return true;
		}
		return false;
	}

	private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			if (UUIDUtils.UUID_LOST_ENABLE.equals(characteristic.getUuid())) {
				byte[] value = characteristic.getValue();
				deviceListener.onGetValue(tag, value, bluetoothDevice);
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			byte[] value = characteristic.getValue();
			deviceListener.onWriteSuccess(tag, value, bluetoothDevice);
		}

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			if (newState == BluetoothGatt.STATE_CONNECTED) {
				gatt.discoverServices();// Search service
			} else if (newState == BluetoothGatt.STATE_DISCONNECTED) {// Disconnect
				deviceStatus = DEVICE_STATUS_DISCONNECTED;
				deviceListener.onDisconnected(tag, bluetoothDevice);
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt,
				BluetoothGattDescriptor descriptor, int status) {
			super.onDescriptorWrite(gatt, descriptor, status);
			byte[] value = descriptor.getValue();
			deviceListener.onWriteSuccess(tag, value, bluetoothDevice);
		}

		@Override
		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			super.onReadRemoteRssi(gatt, rssi, status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				deviceListener.onGetRssi(tag, rssi, bluetoothDevice);
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			deviceStatus = DEVICE_STATUS_CONNECTED;
			deviceListener.onConnected(tag, bluetoothDevice);
			enableNotification();// Enable notification
		}
	};

	/**
	 * Enable notification
	 */
	private void enableNotification() {
		BluetoothGattService nableService = this.mBluetoothGatt
				.getService(UUIDUtils.UUID_LOST_SERVICE);
		if (nableService == null) {
			return;
		}
		BluetoothGattCharacteristic TxPowerLevel = nableService
				.getCharacteristic(UUIDUtils.UUID_LOST_ENABLE);
		if (TxPowerLevel == null) {
			return;
		}
		setCharacteristicNotification(TxPowerLevel, true);
	}

	private void setCharacteristicNotification(
			BluetoothGattCharacteristic characteristic, boolean enabled) {
		if (this.mBluetoothGatt == null) {
			return;
		}
		this.mBluetoothGatt.setCharacteristicNotification(characteristic,
				enabled);
		if (UUIDUtils.UUID_LOST_ENABLE.equals(characteristic.getUuid())) {
			BluetoothGattDescriptor descriptor = characteristic
					.getDescriptor(UUIDUtils.CLIENT_CHARACTERISTIC_CONFIG);
			descriptor
					.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
			this.mBluetoothGatt.writeDescriptor(descriptor);
		}
	}

	/**
	 * Write data to the Bluetooth device and Data length can not be more than
	 * 20
	 * 
	 * @param data
	 */
	private void WriteData(byte[] data) {
		BluetoothGattService alertService = this.mBluetoothGatt
				.getService(UUIDUtils.UUID_LOST_SERVICE);
		if (alertService == null) {
			return;
		}
		BluetoothGattCharacteristic alertLevel = alertService
				.getCharacteristic(UUIDUtils.UUID_LOST_WRITE);
		if (alertLevel == null) {
			return;
		}
		alertLevel.setValue(data);
		alertLevel.setWriteType(1);
		this.mBluetoothGatt.writeCharacteristic(alertLevel);
	}

	/**
	 * start on air download
	 * 
	 * @param imageInfo
	 *            the information you want to update to
	 */
	public List<BluetoothGattService> getServices() {
		if (mBluetoothGatt != null) {
			return mBluetoothGatt.getServices();
		}
		return null;

	}

	/**
	 * close the remote Bluetooth device
	 */
	public void closeBluetoothDevice() {
		byte[] data = new byte[3];
		data[0] = -86;
		data[1] = 7;
		data[2] = ((byte) (data[0] + data[1]));
		WriteData(data);
	}

	/**
	 * Bluetooth devices start alarm
	 */
	public void startRing() {
		byte[] data = new byte[3];
		data[0] = -86;
		data[1] = 5;
		data[2] = ((byte) (data[0] + data[1]));
		WriteData(data);
	}

	/**
	 * Bluetooth devices stop alarm
	 */
	public void stopRing() {
		byte[] data = new byte[3];
		data[0] = -86;
		data[1] = 6;
		data[2] = ((byte) (data[0] + data[1]));
		WriteData(data);
	}
}
