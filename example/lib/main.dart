import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:paypal_sdk_flutter/paypal_sdk_flutter.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _paymentStatus = 'Unknown';

  Future<void> doPayment({double amount, String description}) async {
    String result;
    try {
      PaypalSdkFlutter sdk = PaypalSdkFlutter(
        environment: Environment.noNetwork,
        merchantName: "your merchant name",
        clientId: "your client id",
      );
      var result = await sdk.payWithPayPal(amount: amount, description: description);
      print("Withing example $result");

      if (!mounted) return;

      setState(() {
        _paymentStatus = result;
      });
    } on PlatformException {
      result = 'Failed to get payPal result.';

      if (!mounted) return;

      setState(() {
        _paymentStatus = result;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('PayPal Plugin example app'),
        ),
        body: Center(
          child: Text('Result: $_paymentStatus\n'),
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            await doPayment(amount: 10.0, description: "Apple");
          },
          child: Icon(Icons.payment),
        ),
      ),
    );
  }
}
