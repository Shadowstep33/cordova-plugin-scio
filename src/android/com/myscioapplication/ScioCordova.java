package com.squarevault.cordova.scio;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallback;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceConnectHandler;
import com.consumerphysics.android.sdk.sciosdk.ScioCloud;
import com.consumerphysics.android.sdk.sciosdk.ScioDevice;

import consumerphysics.com.myscioapplication.config.Constants;
import consumerphysics.com.myscioapplication.interfaces.IScioDevice;
import consumerphysics.com.myscioapplication.utils.StringUtils;


public class ScioCordova extends CordovaPlugin implements IScioDevice {

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
}