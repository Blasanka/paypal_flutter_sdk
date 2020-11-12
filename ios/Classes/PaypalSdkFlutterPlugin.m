#import "PaypalSdkFlutterPlugin.h"
#if __has_include(<paypal_sdk_flutter/paypal_sdk_flutter-Swift.h>)
#import <paypal_sdk_flutter/paypal_sdk_flutter-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "paypal_sdk_flutter-Swift.h"
#endif

@implementation PaypalSdkFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftPaypalSdkFlutterPlugin registerWithRegistrar:registrar];
}
@end
