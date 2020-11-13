# paypal_sdk_flutter


[![pub package](https://img.shields.io/pub/v/battery.svg)](https://pub.dev/packages/paypal_sdk_flutter)

Please note that this is not official PayPal's plugin. But
this uses Android and IOS native official libraries (so, 
nothing to worry).

    try {
      PaypalSdkFlutter sdk = PaypalSdkFlutter(
        environment: Environment.sandbox,
        merchantName: "ecommerce",
        clientId: "AQlOU6BX5Nm7G95hWzp5du2OEJlb7p3-2HM9qQH49p_uw1-fw6P96B9dmBZZSz9bg4d6ko-hVfet6zvE",
      );
      var result = await sdk.payWithPayPal(amount: amount, description: description);
      print(result);
    } on PlatformException {
      result = 'Failed to get payPal result.';
    }

See example page for more info.

Implementation and available parameters for now:

      /// [environment] can be Environment.noNetwork, Environment.sandbox, and Environment.production
      final Environment environment;
      final String merchantName;
      /// [urlForPrivacyWebPage] is required if you have privacy, policy to provide
      final String urlForPrivacyWebPage;
      /// [urlForLegalWebPage] is required if you have privacy, policy to provide
      final String urlForLegalWebPage;
      final String clientId;
      /// For to buy something, secret is not must
      final String secret;

      //...

      /// [currency] default to USD
      /// [description] default to empty string ""
      Future<String> payWithPayPal({@required double amount, @required String description, String currency}) async {//..
      
You can get the client id from your paypal developer account dashboard.