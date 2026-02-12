//
//  AppoxeeLocationManager.h
//  AppoxeeLocationServices
//
//  Created by Raz Elkayam on 10/30/14.
//  Copyright (c) 2014 Appoxee. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

@class AppoxeeLocationManager;

@protocol AppoxeeLocationManagerDelegate <NSObject>

@optional

/**
 Notifies on faliure.
 @brief Method for notifiyng when a failure happend.
 */
- (void)locationManager:(nonnull AppoxeeLocationManager *)manager didFailWithError:(nullable NSError *)error;

/**
 Notifies when a device entered into a geographic region.
 @brief Method for notifiyng when geographic region is entered by the device.
 <pre><code>
 - (void)locationManager:(AppoxeeLocationManager *)manager didEnterGeoRegion:(CLCircularRegion *)geoRegion
 {
    // Entered a region.
    // Notice that the .identifier @property of the CLCircularRegion instance is the location ID as created By Teradata.
 
    NSLog(@"Entered location id of: %@, of coordinates: %.2f | %.2f, with radius: %.2f", geoRegion.identifier, geoRegion.center.latitude, geoRegion.center.longitude, geoRegion.radius);
 }
 </code></pre>
 */
- (void)locationManager:(nonnull AppoxeeLocationManager *)manager didEnterGeoRegion:(nonnull CLCircularRegion *)geoRegion;

/**
 Notifies when a device exits from a geographic region.
 @brief Method for notifiyng when geographic region is exited by the device.
 <pre><code>
 - (void)locationManager:(AppoxeeLocationManager *)manager didExitGeoRegion:(CLCircularRegion *)geoRegion
 {
    // Exited a region.
    // Notice that the .identifier @property of the CLCircularRegion instance is the location ID as created By Teradata.
 
    NSLog(@"Entered location id: %@, of coordinates: %.2f | %.2f, with radius: %.2f", geoRegion.identifier, geoRegion.center.latitude, geoRegion.center.longitude, geoRegion.radius);
 }
 </code></pre>
 */
- (void)locationManager:(nonnull AppoxeeLocationManager *)manager didExitGeoRegion:(nonnull CLCircularRegion *)geoRegion;

@end

/*!
 * Appoxee Location manager is the main interface API for developers who implements Appoxee Location Services.
 */
@interface AppoxeeLocationManager : NSObject

/*! 
 * between 1 - 5, where 1 prints all logs and 5 prints critical logs.
 */
@property (nonatomic) NSInteger logLevel;
@property (nonatomic) NSArray<CLLocation*>* _Nullable regions;

/*!
 * The delegate for recieving AppoxeeLocationManagerDelegate events.
 */
@property (nonatomic, weak, nullable) id <AppoxeeLocationManagerDelegate> delegate;

/*!
 * The last known location of the user.
 */
@property (nonatomic, strong, nullable) CLLocation *lastLocation;
@property (nonatomic, nonnull, readonly) NSString *locationServicesVersion;

/**
 AppoxeeLocationManager instance
 @brief Method for getting a singleton instance of AppoxeeLocationManager
 <pre><code>
 AppoxeeLocationManager *locationManager = [AppoxeeLocationManager shared];
 </code></pre>
 @return AppoxeeLocationManager an Instance Type of AppoxeeLocationManager.
 */
+ (nullable instancetype)shared;

/**
 Start monitoring for geo fences. Method will set a UIApplicationBackgroundFetchIntervalMinimum if application supports background fetch.
 @brief Method for starting monitoring for geo fences. Method will set a UIApplicationBackgroundFetchIntervalMinimum if application supports background fetch.
 @attention Authorization status must be kCLAuthorizationStatusAuthorizedAlways or kCLAuthorizationStatusAuthorized, else method will not execute.
 <pre><code>
 - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
 {
    [[Appoxee shared] engageWithLaunchOptions:launchOptions andDelegate:self andSDKID:@"sdk.id" andAppSecret:@"app.secret"];
    // Set an observer, and enable location monitoring when AppoxeeSDK is ready. This can also be achived when listening to @"APX_Ready" notification with NSNotificationCenter
    [[Appoxee shared] addObserver:self forKeyPath:@"isReady" options:(NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld) context:NULL];
 
    return YES;
 }
 - (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
 {
    if ([keyPath isEqualToString:@"isReady"]) {
 
        [[Appoxee shared] removeObserver:self forKeyPath:@"isReady"];
        [[AppoxeeLocationManager shared] enableLocationMonitoring];
    }
 }
 </code></pre>
 */
- (void)enableLocationMonitoring;

/**
 Stop monitoring for geo fences. Method will set a UIApplicationBackgroundFetchIntervalNever if application supports background fetch.
 @brief Method for stoping monitoring for geo fences. Method will set a UIApplicationBackgroundFetchIntervalNever if application supports background fetch.
 <pre><code>
 [[AppoxeeLocationManager shared] disableLocationMonitoring];
 </code></pre>
 */
- (void)disableLocationMonitoring;

@end
