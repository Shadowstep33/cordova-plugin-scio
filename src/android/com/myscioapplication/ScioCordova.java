package com.squarevault.cordova.scio;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.consumerphysics.android.scioconnection.services.SCiOBLeService;
import com.consumerphysics.android.scioconnection.utils.BLEUtils;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallback;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceConnectHandler;
import com.consumerphysics.android.sdk.sciosdk.ScioCloud;
import com.consumerphysics.android.sdk.sciosdk.ScioDevice;
import com.consumerphysics.android.sdk.callback.cloud.ScioCloudAnalyzeManyCallback;
import com.consumerphysics.android.sdk.callback.cloud.ScioCloudSCiOVersionCallback;
import com.consumerphysics.android.sdk.callback.cloud.ScioCloudUserCallback;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceBatteryHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCalibrateHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallbackHandler;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceScanHandler;
import com.consumerphysics.android.sdk.model.ScioBattery;
import com.consumerphysics.android.sdk.model.ScioModel;
import com.consumerphysics.android.sdk.model.ScioReading;
import com.consumerphysics.android.sdk.model.ScioUser;
import com.consumerphysics.android.sdk.sciosdk.ScioLoginActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import consumerphysics.com.myscioapplication.config.Constants;
import consumerphysics.com.myscioapplication.interfaces.IScioDevice;
import consumerphysics.com.myscioapplication.utils.StringUtils;

public class ScioCordova extends CordovaPlugin implements IScioDevice {

    private Map<String, String> devices;
    private DevicesAdapter devicesAdapter;
    private BluetoothAdapter bluetoothAdapter;

    // BlueTooth scan callback
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String deviceName = device.getName();

            // Show only SCiO devices
            if (deviceName != null && deviceName.startsWith("SCiO")) {
                deviceName = deviceName.substring(4);
                String scio = devices.get(device.getAddress());
				
                final Device dev = new Device(device.getAddress(), deviceName);
                storeDevice(dev);
                Toast.makeText(context, dev.getName() + " was selected", Toast.LENGTH_SHORT).show();
				// Stop Scan
				bluetoothAdapter.stopLeScan(leScanCallback);
				callbackContext.success("Scio "+deviceName+ " found with BLE");
                // if (scio == null) {
                    // addDevice(deviceName, device.getAddress());
                // }
            }
        }
    };
	
	private Context context;
	private CallbackContext callbackContext;
    private ScioCloud scioCloud;
    private ScioDevice scioDevice;

    protected ScioCloud getScioCloud() {
        return scioCloud;
    }

    protected ScioDevice getScioDevice() {
        return scioDevice;
    }
	
    protected boolean isDeviceConnected() {
        return scioDevice != null && scioDevice.isConnected();
    }

    protected boolean isLoggedIn() {
        return scioCloud != null && scioCloud.hasAccessToken();
    }

    protected SharedPreferences getSharedPrefs() {
        return context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
    }
	
    private final class Device {
        private String address;
        private String name;

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public Device(final String address, final String name) {
            this.name = name;
            this.address = address;
        }
    }
	
    public class DevicesAdapter extends ArrayAdapter<Device> {
        public DevicesAdapter(final Context context, final List<Device> devices) {
            super(context, 0, devices);
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext cbCtx) throws JSONException {
		context = this.cordova.getActivity().getApplicationContext();
		callbackContext = cbCtx;
		
        if (action.equals("connect")) {
			/* Get the Preferred Scio Device and Connect */
			final String deviceAddress = getSharedPrefs().getString(Constants.SCIO_ADDRESS, null);
			
			if (!StringUtils.isEmpty(deviceAddress)) {
				connect(deviceAddress);
			}else{
				callbackContext.error("Device ID Empty");
			}

            return true;
        }
		
        if (action.equals("scanble")) {

			devices = new LinkedHashMap<>();

			final List<Device> arrayOfDevices = new ArrayList<>();
			devicesAdapter = new DevicesAdapter(context, arrayOfDevices);

			bluetoothAdapter = BLEUtils.getBluetoothAdapter(context);

			// Start Scan
			bluetoothAdapter.startLeScan(leScanCallback);
            return true;
        }
		
        if (action.equals("scan")) {

			devices = new LinkedHashMap<>();

			final List<Device> arrayOfDevices = new ArrayList<>();
			devicesAdapter = new DevicesAdapter(context, arrayOfDevices);

			bluetoothAdapter = BLEUtils.getBluetoothAdapter(context);

			// Start Scan
			bluetoothAdapter.startLeScan(leScanCallback);
            return true;
        }
		
        return false;
    }

    private void connect(final String deviceAddress) {
	
        scioDevice = new ScioDevice(context, deviceAddress);
		
        scioDevice.connect(new ScioDeviceConnectHandler() {		
            @Override
            public void onConnected() { 
				onScioConnected();
            }

            @Override
            public void onConnectFailed() {
				onScioConnectionFailed();
            }

            @Override
            public void onTimeout() {
				onScioDisconnected();
            }
        });

        scioDevice.setScioDisconnectCallback(new ScioDeviceCallback() {
            @Override
            public void execute() {
				onScioDisconnected();
            }
        });

        scioDevice.setButtonPressedCallback(new ScioDeviceCallback() {
            @Override
            public void execute() {
                /* Btn Handler */
            }
        });
    }
	
    @Override
    public void onScioButtonClicked() {
    }

    @Override
    public void onScioConnected() {
		callbackContext.success("Scio at connected");
    }

    @Override
    public void onScioConnectionFailed() {
		callbackContext.error("Scio at failed to connect");
    }

    @Override
    public void onScioDisconnected() {
		callbackContext.error("Scio at disconnected");
    }
	
    private void storeDevice(final Device device) {
        final SharedPreferences pref = context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = pref.edit();

        edit.putString(Constants.SCIO_ADDRESS, device.getAddress());
        edit.putString(Constants.SCIO_NAME, device.getName());

        edit.commit();
    }

    private void addDevice(final String name, final String address) {
        devices.put(address, name);

        final Device dev = new Device(address, name);
        devicesAdapter.add(dev);
    }
	
    public void doScan(final View view) {

        if (!isDeviceConnected()) {
            Toast.makeText(context, "Can not scan. SCiO is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (modelId == null) {
            Toast.makeText(context, "Can not scan. Model was not selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLoggedIn()) {
            Toast.makeText(context, "Can not scan. User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = ProgressDialog.show(this, "Please Wait", "Analyzing...", false);

        getScioDevice().scan(new ScioDeviceScanHandler() {
            @Override
            public void onSuccess(final ScioReading reading) {
                // ScioReading object is Serializable and can be saved to be used later for analyzing.
                List<String> modelsToAnalyze = new ArrayList<>();
                modelsToAnalyze.addAll(Arrays.asList(modelId.split(",")));

                getScioCloud().analyze(reading, modelsToAnalyze, new ScioCloudAnalyzeManyCallback() {
                    @Override
                    public void onSuccess(final List<ScioModel> models) {
                        Log.d(TAG, "analyze onSuccess");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Successful Scan", Toast.LENGTH_SHORT).show();
								callbackContext.success(models);
                            }
                        });
                    }

                    @Override
                    public void onError(final int code, final String msg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Error while analyzing: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }

            @Override
            public void onNeedCalibrate() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Can not scan. Calibration is needed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error while scanning", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onTimeout() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Timeout while scanning", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}