package com.squarevault.cordova.scio;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.consumerphysics.android.sdk.callback.device.ScioDeviceCallback;
import com.consumerphysics.android.sdk.callback.device.ScioDeviceConnectHandler;
import com.consumerphysics.android.sdk.sciosdk.ScioCloud;
import com.consumerphysics.android.sdk.sciosdk.ScioDevice;

import consumerphysics.com.myscioapplication.config.Constants;
import consumerphysics.com.myscioapplication.interfaces.IScioDevice;
import consumerphysics.com.myscioapplication.utils.StringUtils;
/**
 * This class echoes a string called from JavaScript.
 */
public class ScioCordova extends CordovaPlugin implements IScioDevice {

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

    protected SharedPreferences getSharedPreferences() {
        return getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
    }
	
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("connect")) {
			/* Get the Preferred Scio Device and Connect */
			final String deviceAddress = getSharedPreferences().getString(Constants.SCIO_ADDRESS, null);
			
			if (!StringUtils.isEmpty(deviceAddress)) {
				connect(deviceAddress, callbackContext);
			}else{
				callbackContext.error("Device ID Empty");
			}

            return true;
        }
        return false;
    }

    private void connect(final String deviceAddress, CallbackContext callbackContext) {
        scioDevice = new ScioDevice(this, deviceAddress);
        scioDevice.connect(new ScioDeviceConnectHandler() {
            @Override
            public void onConnected() { 
				callbackContext.success("Scio at "+deviceAddress+" connected");
            }

            @Override
            public void onConnectFailed() {
				callbackContext.error("Scio at "+deviceAddress+" failed to connect");
            }

            @Override
            public void onTimeout() {
				callbackContext.error("Scio at "+deviceAddress+" timed out");
            }
        });

        scioDevice.setScioDisconnectCallback(new ScioDeviceCallback() {
            @Override
            public void execute() {
				callbackContext.error("Scio at "+deviceAddress+" disconnected");
            }
        });

        scioDevice.setButtonPressedCallback(new ScioDeviceCallback() {
            @Override
            public void execute() {
                /* Btn Handler */
            }
        });
    }
}