#import "AppDelegate.h"
#import "GeneratedPluginRegistrant.h"
#import <mapp_sdk/PushMessageDelegate.h>

@implementation AppDelegate

- (BOOL)application:(UIApplication *)application
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  [GeneratedPluginRegistrant registerWithRegistry:self];
  // Override point for customization after application launch.
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

- (void)applicationDidBecomeActive:(UIApplication *)application {
    signal(SIGPIPE, SIG_IGN);
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    signal(SIGPIPE, SIG_IGN);
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler {
    NSLog(@"%@", userInfo);
    // Forward silent/background push to the plugin pipeline
    [[NSNotificationCenter defaultCenter] postNotificationName:@"handledPushSilent"
                                                        object:nil
                                                       userInfo:userInfo];
    [super application:application didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
}

@end
