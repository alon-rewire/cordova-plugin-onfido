/**
 */
package com.plugin.onfido;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.onfido.android.sdk.capture.DocumentType;
import com.onfido.android.sdk.capture.ExitCode;
import com.onfido.android.sdk.capture.Onfido;
import com.onfido.android.sdk.capture.OnfidoConfig;
import com.onfido.android.sdk.capture.OnfidoFactory;
import com.onfido.android.sdk.capture.errors.OnfidoException;
import com.onfido.android.sdk.capture.ui.options.CaptureScreenStep;
import com.onfido.android.sdk.capture.ui.options.FlowStep;
import com.onfido.android.sdk.capture.upload.Captures;
import com.onfido.android.sdk.capture.upload.DocumentSide;
import com.onfido.android.sdk.capture.utils.CountryCode;
import com.onfido.api.client.data.Applicant;

import android.content.Intent;
import android.util.Log;
import android.app.Activity;

public class OnFidoBridge extends CordovaPlugin {
  private static final String TAG = "OnFidoBridge";
  private Onfido client;
  private CallbackContext currentCallbackContext = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing OnFido");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if(action.equals("scan")) {
      Activity context=this.cordova.getActivity();
      client = OnfidoFactory.create(context).getClient();

      final FlowStep[] defaultStepsWithWelcomeScreen = new FlowStep[]{
        FlowStep.WELCOME,
        new CaptureScreenStep(DocumentType.DRIVING_LICENCE, CountryCode.US),
        FlowStep.CAPTURE_FACE,
        FlowStep.FINAL
      };

      OnFidoBridge.this.currentCallbackContext = callbackContext;

      final String applicantId;
      try
      {
        applicantId = args.getString(0);
      } catch(JSONException e) {
        callbackContext.error("missing argument: \"applicantId\"");
        Log.d(TAG, "execute: argument \"applicantId\" was not passed");
        return false;
      }

      this.cordova.getThreadPool().execute(new Runnable() {
        @Override
        public void run() {

          OnfidoConfig onfidoConfig = getOnfidoConfigBuilder(applicantId)
            .withCustomFlow(defaultStepsWithWelcomeScreen)
            .build();
          Intent onFidoIntent = client.createIntent(onfidoConfig);
          cordova.startActivityForResult(OnFidoBridge.this, onFidoIntent, 1);
        }
      });
    }
    return true;
  }

  private OnfidoConfig.Builder getOnfidoConfigBuilder(String applicantId) {
    String token = OnFidoBridge.this.preferences.getString("onfido-mobile-sdk-token", null);
    if(token == null) {
      String msg = "Failed to get onfido-mobile-sdk-token";
      Log.e(TAG, msg);
      OnFidoBridge.this.currentCallbackContext.error(msg);
    }

    return OnfidoConfig.builder()
      .withToken(token)
      .withApplicant(applicantId);
  }

  protected JSONObject buildCaptureJsonObject(Captures captures) throws JSONException{
    JSONObject captureJson = new JSONObject();
    if(captures.getDocument() == null) {
      captureJson.put("document", null);
    }

    JSONObject docJson = new JSONObject();

    DocumentSide frontSide = captures.getDocument().getFront();
    if(frontSide != null) {
      JSONObject docSideJson = new JSONObject();
      docSideJson.put("id", frontSide.getId());
      docSideJson.put("side", frontSide.getSide());
      docSideJson.put("type", frontSide.getType());

      docJson.put("front", docSideJson);
    }

    DocumentSide backSide = captures.getDocument().getBack();
    if(backSide != null) {
      JSONObject docSideJson = new JSONObject();
      docSideJson.put("id", backSide.getId());
      docSideJson.put("side", backSide.getSide());
      docSideJson.put("type", backSide.getType());

      docJson.put("back", docSideJson);
    }

    captureJson.put("document", docJson);

    return captureJson;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    client.handleActivityResult(resultCode, data, new Onfido.OnfidoResultListener() {
      @Override
      public void userCompleted(Applicant applicant, Captures captures) {
        JSONObject captureJson;
        try {
          captureJson = buildCaptureJsonObject(captures);
        } catch(JSONException e) {
          Log.d(TAG, "userCompleted: failed to build json result");
          return;
        }

        final PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, captureJson.toString());
        OnFidoBridge.this.currentCallbackContext.sendPluginResult(pluginResult);
        Log.d(TAG, "userCompleted: successfully returned data to plugin");
      }

      @Override
      public void userExited(ExitCode exitCode, Applicant applicant) {
        final PluginResult pluginResult = new PluginResult(Status.NO_RESULT, "User did not finished the flow");
        OnFidoBridge.this.currentCallbackContext.sendPluginResult(pluginResult);

        Log.d(TAG, "userExited: YES");
      }

      @Override
      public void onError(OnfidoException e, Applicant applicant) {
        final PluginResult pluginResult = new PluginResult(Status.ERROR, e.getMessage());
        OnFidoBridge.this.currentCallbackContext.sendPluginResult(pluginResult);

        Log.d(TAG, "onError: YES");
        e.printStackTrace();
      }
    });
  }
}
