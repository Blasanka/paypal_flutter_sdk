package com.blasanka.paypal_sdk_flutter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.paypal.android.sdk.payments.PayPalAuthorization;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalFuturePaymentActivity;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalProfileSharingActivity;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.app.FlutterActivity;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** PaypalSdkFlutterPlugin */
public class PaypalSdkFlutterPlugin implements FlutterPlugin, ActivityAware, MethodCallHandler,
        PluginRegistry.ActivityResultListener {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;

  private static final String TAG = "PaypalSdkFlutterPlugin";

  private static final int REQUEST_CODE_PAYMENT = 1;
  private static final int REQUEST_CODE_FUTURE_PAYMENT = 2;
  private static final int REQUEST_CODE_PROFILE_SHARING = 3;
  private Result flutterResult;
  private Context context;
  private Activity activity;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "paypal_sdk_flutter");
    channel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(PluginRegistry.Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "paypal_sdk_flutter");
    channel.setMethodCallHandler(new PaypalSdkFlutterPlugin());
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    flutterResult = result;
    if (call.method.equals("payWithPayPal")) {

      payWithPayPal(call);

    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
    channel = null;
    activity.stopService(new Intent(context, PayPalService.class));
  }

//  @Override
//  public void onDestroy() {
//    // Stop service when done
//    super.onDestroy();
//  }

  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PAYMENT) {
      if (resultCode == Activity.RESULT_OK) {
        PaymentConfirmation confirm =
                data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
        if (confirm != null) {
          try {
            Log.i(TAG, confirm.toJSONObject().toString(4));
            Log.i(TAG, confirm.getPayment().toJSONObject().toString(4));

            /**
             *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
             * or consent completion.
             * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
             * for more details.
             *
             * For sample mobile backend interactions, see
             * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
             */
            displayResultText(confirm.getProofOfPayment().getPaymentId());
            //"PaymentConfirmation info received from PayPal");

            return true;
          } catch (JSONException e) {
            Log.e(TAG, "an extremely unlikely failure occurred: ", e);
            flutterResult.error("-1", "An extremely unlikely failure occurred",  null);
          }
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        Log.i(TAG, "The user canceled.");
        flutterResult.error("0", "The user canceled.",  null);
      } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
        Log.i(
                TAG,
                "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
        flutterResult.error("-2", "An invalid Payment.",  null);
      }
    } else if (requestCode == REQUEST_CODE_FUTURE_PAYMENT) {
      if (resultCode == Activity.RESULT_OK) {
        PayPalAuthorization auth =
                data.getParcelableExtra(PayPalFuturePaymentActivity.EXTRA_RESULT_AUTHORIZATION);
        if (auth != null) {
          try {
            Log.i("FuturePaymentExample", auth.toJSONObject().toString(4));

            String authorization_code = auth.getAuthorizationCode();
            Log.i("FuturePaymentExample", authorization_code);

            sendAuthorizationToServer(auth);
            displayResultText("Future Payment code received from PayPal");

          } catch (JSONException e) {
            Log.e("FuturePaymentExample", "an extremely unlikely failure occurred: ", e);
            flutterResult.error("-2", "an extremely unlikely failure occurred.",  null);
          }
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        Log.i("FuturePaymentExample", "The user canceled.");
        flutterResult.error("-2", "The user canceled.",  null);
      } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
        Log.i(
                "FuturePaymentExample",
                "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
        flutterResult.error("-2", "an extremely unlikely failure occurred.",  null);
      }
    } else if (requestCode == REQUEST_CODE_PROFILE_SHARING) {
      if (resultCode == Activity.RESULT_OK) {
        PayPalAuthorization auth =
                data.getParcelableExtra(PayPalProfileSharingActivity.EXTRA_RESULT_AUTHORIZATION);
        if (auth != null) {
          try {
            Log.i("ProfileSharingExample", auth.toJSONObject().toString(4));

            String authorization_code = auth.getAuthorizationCode();
            Log.i("ProfileSharingExample", authorization_code);

            sendAuthorizationToServer(auth);
            displayResultText("Profile Sharing code received from PayPal");

          } catch (JSONException e) {
            Log.e("ProfileSharingExample", "an extremely unlikely failure occurred: ", e);
            flutterResult.error("-2", "an extremely unlikely failure occurred.",  null);
          }
        }
      } else if (resultCode == Activity.RESULT_CANCELED) {
        Log.i("ProfileSharingExample", "The user canceled.");
      } else if (resultCode == PayPalFuturePaymentActivity.RESULT_EXTRAS_INVALID) {
        Log.i(
                "ProfileSharingExample",
                "Probably the attempt to previously start the PayPalService had an invalid PayPalConfiguration. Please see the docs.");
        flutterResult.error("-2", "Probably the attempt to previously.",  null);
      }
    }
    return false;
  }

  private void payWithPayPal(@NonNull MethodCall call) {
    Map<String, Object> arguments = (Map<String, Object>) call.arguments;
    String configEnv;

    switch (Objects.requireNonNull(arguments.get("environment")).toString()) {
      case "production":
        configEnv = PayPalConfiguration.ENVIRONMENT_PRODUCTION;
        break;
      case "sandbox":
        configEnv = PayPalConfiguration.ENVIRONMENT_SANDBOX;
        break;
      default:
        configEnv = PayPalConfiguration.ENVIRONMENT_NO_NETWORK;
        break;
    }

    // note that these credentials will differ between live & sandbox environments.
    final String CONFIG_CLIENT_ID = Objects.requireNonNull(arguments.get("clientId")).toString();

    Uri privacyUrl;
    Uri legalUrl;

    if (arguments.get("urlForPrivacyWebPage") != null)
      privacyUrl = Uri.parse(arguments.get("urlForPrivacyPage").toString());
    else
      privacyUrl = Uri.parse("https://www.example.com/privacy");

    if (arguments.get("urlForLegalWebPage") != null)
      legalUrl = Uri.parse(arguments.get("urlForLegalPage").toString());
    else
      legalUrl = Uri.parse("https://www.example.com/legal");

    PayPalConfiguration config = new PayPalConfiguration()
            .environment(configEnv)
            .clientId(CONFIG_CLIENT_ID)
            // The following are only used in PayPalFuturePaymentActivity.
            .merchantName(Objects.requireNonNull(arguments.get("merchantName")).toString())
            .merchantPrivacyPolicyUri(privacyUrl)
            .merchantUserAgreementUri(legalUrl);

    Intent intent = new Intent(activity, PayPalService.class);
    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
    context.startService(intent);
    /*
     * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
     * Change PAYMENT_INTENT_SALE to
     *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
     *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
     *     later via calls from your server.
     *
     * Also, to include additional payment details and an item list, see getStuffToBuy() below.
     */
    PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE,
            arguments.get("amount"),
            arguments.get("currency"),
            arguments.get("description"));

    /*
     * See getStuffToBuy(..) for examples of some available payment options.
     */

    Intent paymentActivity = new Intent(context, PaymentActivity.class);

    // send the same configuration for restart resiliency
    paymentActivity.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

    paymentActivity.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

    activity.startActivityForResult(paymentActivity, REQUEST_CODE_PAYMENT);
  }

  private PayPalPayment getThingToBuy(String paymentIntent, Object amount, Object currency, Object description) {
    if (currency == null) currency = "USD";
    if (description == null) description = "description";
    return new PayPalPayment(new BigDecimal(amount.toString()), currency.toString(), description.toString(),
            paymentIntent);
  }

  protected void displayResultText(String result) {
    flutterResult.success(result);
//        ((TextView)findViewById(R.id.txtResult)).setText("Result : " + result);
//        Toast.makeText(
//                getApplicationContext(),
//                result, Toast.LENGTH_LONG)
//                .show();

  }

  private void sendAuthorizationToServer(PayPalAuthorization authorization) {

    /**
     * TODO: Send the authorization response to your server, where it can
     * exchange the authorization code for OAuth access and refresh tokens.
     *
     * Your server must then store these tokens, so that your server code
     * can execute payments for this user in the future.
     *
     * A more complete example that includes the required app-server to
     * PayPal-server integration is available from
     * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
     */

  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
    activity = binding.getActivity();
    context = binding.getActivity().getBaseContext();
    binding.addActivityResultListener(this);
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {

  }
}
