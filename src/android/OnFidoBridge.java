/**
 */
package com.example;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.ionicframework.emptyproj632910.MainActivity;
import com.onfido.android.sdk.capture.ExitCode;
import com.onfido.android.sdk.capture.Onfido;
import com.onfido.android.sdk.capture.OnfidoConfig;
import com.onfido.android.sdk.capture.OnfidoFactory;
import com.onfido.android.sdk.capture.ui.camera.face.FaceCaptureStep;
import com.onfido.android.sdk.capture.ui.camera.face.FaceCaptureVariant;
import com.onfido.android.sdk.capture.ui.options.FlowStep;
import com.onfido.android.sdk.capture.upload.Captures;
import com.onfido.android.sdk.capture.utils.OnfidoApiUtil;
import com.onfido.api.client.OnfidoAPI;
import com.onfido.api.client.data.Address;
import com.onfido.api.client.data.Applicant;
import com.onfido.api.client.data.Check;
import com.onfido.api.client.data.ErrorData;
import com.onfido.api.client.data.Report;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.Activity;
import java.util.Date;

public class OnFidoBridge extends CordovaPlugin {
  private static final String TAG = "OnFidoBridge";
  private Onfido client;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing OnFidoBridge");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("startSdk")) {
      Activity context=this.cordova.getActivity();
      client = OnfidoFactory.create(context).getClient();

      final FlowStep[] defaultStepsWithWelcomeScreen = new FlowStep[]{
        FlowStep.WELCOME,
        FlowStep.CAPTURE_DOCUMENT,
        FlowStep.CAPTURE_FACE,
        FlowStep.FINAL
      };


      Context cntx=this.cordova.getActivity().getApplicationContext();
      Intent intent=new Intent(context, com.example.FakeActivity.class);

      this.cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {
          OnfidoConfig onfidoConfig = getTestOnfidoConfigBuilder()
            .withCustomFlow(defaultStepsWithWelcomeScreen)
            .build();
          Intent onFidoIntent = client.createIntent(onfidoConfig);
          cordova.startActivityForResult((CordovaPlugin)OnFidoBridge.this, onFidoIntent, 1);
        }
      });


      // An example of returning data back to the web layer
      final PluginResult result = new PluginResult(PluginResult.Status.OK, (new Date()).toString());
      callbackContext.sendPluginResult(result);
    }
    return true;
  }

  private OnfidoConfig.Builder getTestOnfidoConfigBuilder() {
      return OnfidoConfig.builder()
        .withToken("")
        .withApplicant("");
  }
}
