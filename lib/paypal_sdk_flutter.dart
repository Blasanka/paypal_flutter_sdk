import 'dart:async';
import 'dart:developer';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class PaypalSdkFlutter {
  PaypalSdkFlutter({
    @required this.environment,
    @required this.merchantName,
    @required this.clientId,
    this.urlForPrivacyWebPage,
    this.urlForLegalWebPage,
    this.secret,
  });

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

  static const MethodChannel _channel =
      const MethodChannel('paypal_sdk_flutter');

  /// [currency] default to USD
  /// [description] default to empty string ""
  Future<String> payWithPayPal({@required double amount, @required String description, String currency}) async {
    try {
      final String result = await _channel.invokeMethod(
          'payWithPayPal',
          {
            "environment": environment.toValueString(),
            "merchantName": merchantName,
            "urlForPrivacyWebPage": urlForPrivacyWebPage,
            "urlForLegalWebPage": urlForLegalWebPage,
            "clientId": clientId,
            "secret": secret,
            "amount": amount,
            "currency": currency,
            "description": description,
          }
      );
      print("within plugin $result");
      return result;
    } catch (e, t) {
      log(e);
      print(t);
      return "ITs null";
    }
  }
}

extension ParseToString on Environment {
  String toValueString() {
    return this.toString().split('.').last;
  }
}

enum Environment {
  noNetwork,
  sandbox,
  production,
}