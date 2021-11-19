#import "MappSdkPlugin.h"
#import <AppoxeeSDK.h>

@implementation MappSdkPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"mapp_sdk"
            binaryMessenger:[registrar messenger]];
  MappSdkPlugin* instance = [[MappSdkPlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"getPlatformVersion" isEqualToString:call.method]) {
    result([@"iOS " stringByAppendingString:[[UIDevice currentDevice] systemVersion]]);
  } else if ([@"engage" isEqualToString:call.method]){
    NSNumber* severNumber = call.arguments[0];
    [[Appoxee shared] engageAndAutoIntegrateWithLaunchOptions:NULL andDelegate:NULL with:severNumber];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

@end
