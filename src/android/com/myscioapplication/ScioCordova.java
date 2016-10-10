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
import android.content.Intent;
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
import com.consumerphysics.android.sdk.callback.cloud.ScioCloudModelsCallback;
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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import consumerphysics.com.myscioapplication.config.Constants;
import consumerphysics.com.myscioapplication.interfaces.IScioDevice;
import consumerphysics.com.myscioapplication.utils.StringUtils;

public class ScioCordova extends CordovaPlugin implements IScioDevice {

    private final static int LOGIN_ACTIVITY_RESULT = 1000;
    // TODO: Put your redirect url here!
    private static final String REDIRECT_URL = "https://www.consumerphysics.com";

    // TODO: Put your app key here!
    private static final String APPLICATION_KEY = "4b5ac28b-28f9-4695-b784-b7665dfe3763";
	
    // Members
    private String deviceName;
    private String deviceAddress;
    private String username;
    private String modelId;
    private String modelName;
	
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
	private Activity mActivity;
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
		mActivity = this.cordova.getActivity();
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

			doScan();
            return true;
        }
		
        if (action.equals("calibrate")) {

			doCalibrate();
            return true;
        }
		
		if(action.equals("login")){
		
			doLogin();
			return true;
		}
		
		if(action.equals("getmodels")){

			getScioCloud().getModels(new ScioCloudModelsCallback() {
				@Override
				public void onSuccess(List<ScioModel> models) {
					storeSelectedModels(models);
					callbackContext.success("Stored models");
				}

				@Override
				public void onError(int code, String msg) {
					Toast.makeText(context, "Error while retrieving models", Toast.LENGTH_SHORT).show();
					callbackContext.error(msg);
				}
			});		
            return true;
		}
		
        return false;
    }

    private void connect(final String deviceAddress) {
	
        scioDevice = new ScioDevice(context, deviceAddress);
        scioCloud = new ScioCloud(context);
		
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
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "SCiO button was pressed", Toast.LENGTH_SHORT).show();

                doScan();
            }
        });
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
	
    public void doScan() {

        modelId = getSharedPrefs().getString(Constants.MODEL_ID, null);
		
		Toast.makeText(context, "Set Model To "+modelId, Toast.LENGTH_SHORT).show();	
        if (!isDeviceConnected()) {
            Toast.makeText(context, "Can not scan. SCiO is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (modelId == null) {
            Toast.makeText(context, Constants.MODEL_ID+" Can not scan. Model was not selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLoggedIn()) {
            Toast.makeText(context, "Can not scan. User is not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

		Toast.makeText(context, "Scanning...", Toast.LENGTH_SHORT).show();				
		
        getScioDevice().scan(new ScioDeviceScanHandler() {
            @Override
            public void onSuccess(final ScioReading reading) {
                // ScioReading object is Serializable and can be saved to be used later for analyzing.
                List<String> modelsToAnalyze = new ArrayList<>();
                modelsToAnalyze.addAll(Arrays.asList(modelId.split(",")));

                getScioCloud().analyze(reading, modelsToAnalyze, new ScioCloudAnalyzeManyCallback() {
                    @Override
                    public void onSuccess(final List<ScioModel> models) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Successful Scan", Toast.LENGTH_SHORT).show();
								callbackContext.success(models.toString());
                            }
                        });
                    }

                    @Override
                    public void onError(final int code, final String msg) {
                        mActivity.runOnUiThread(new Runnable() {
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
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Can not scan. Calibration is needed", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError() {
               mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error while scanning", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onTimeout() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Timeout while scanning", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
	
    private void storeSelectedModels(final List<ScioModel> models) {
        SharedPreferences pref = context.getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();

        String modelIds = "";
        String modelNames = "";
        for (ScioModel scioModel : models) {
            modelNames += scioModel.getName();
            modelNames += ",";
            modelIds += scioModel.getId();
            modelIds += ",";
        }

        modelIds = modelIds.substring(0, modelIds.length() - 1);
        modelNames = modelNames.substring(0, modelNames.length() - 1);

        edit.putString(Constants.MODEL_ID, modelIds);
        edit.putString(Constants.MODEL_NAME, modelNames);

        edit.commit();
    }
	
    private void getScioUser() {
        getScioCloud().getScioUser(new ScioCloudUserCallback() {
            @Override
            public void onSuccess(final ScioUser user) {
                storeUsername(user.getUsername());

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Welcome " + user.getFirstName() + " " + user.getLastName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(final int code, final String message) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error while getting the user info.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
	
    private void storeUsername(final String username) {
        this.username = username;
        getSharedPrefs().edit().putString(Constants.USER_NAME, username).commit();
    }
	
    public void doLogin() {
        if (!isLoggedIn()) {
            final Intent intent = new Intent(context, com.consumerphysics.android.sdk.sciosdk.ScioLoginActivity.class);
            intent.putExtra(ScioLoginActivity.INTENT_REDIRECT_URI, REDIRECT_URL);
            intent.putExtra(ScioLoginActivity.INTENT_APPLICATION_ID, APPLICATION_KEY);

            mActivity.startActivityForResult(intent, LOGIN_ACTIVITY_RESULT);
        }
        else {
			Toast.makeText(context, "Has Access Token", Toast.LENGTH_SHORT).show();

            getScioUser();
        }
    }
	
    public void doCalibrate() {
        if (!isDeviceConnected()) {
            Toast.makeText(context, "Can not calibrate. SCiO is not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        getScioDevice().calibrate(new ScioDeviceCalibrateHandler() {
            @Override
            public void onSuccess() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "SCiO was calibrated successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Error while calibrating", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onTimeout() {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "Timeout while calibrating", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}