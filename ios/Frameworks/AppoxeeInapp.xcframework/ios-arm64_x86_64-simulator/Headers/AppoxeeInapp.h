//
//  AppoxeeInapp.h
//  AppoxeeInapp
//
//  Created by Raz Elkayam on 4/17/16.
//  Copyright © 2016 Teradata. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "APXInBoxMessage.h"

typedef NS_ENUM(NSInteger, INAPPSERVER) {
    l3,
    eMC,
    eMC_US,
    cROC,
    tEST,
    tEST55,
    tEST61
};

@class AppoxeeInapp;

@protocol AppoxeeInappDelegate <NSObject>

@optional
/**
 Delegate method for informing a received Inapp message.
 @brief Method will be called when an Inapp message is receieved.
 @param appoxeeInapp A singleton instance of AppoxeeInapp.
 @param identifier NSNumber as an identifier for the specific message.
 @param messageExtraData NSDictionary which may contain extra data assigned to the specific message.
 */
- (void)appoxeeInapp:(nonnull AppoxeeInapp *)appoxeeInapp didReceiveInappMessageWithIdentifier:(nonnull NSNumber *)identifier andMessageExtraData:(nullable NSDictionary <NSString *, id> *)messageExtraData;

/**
 Delegate method for informing a received Inapp message.
 @brief Method will be called when a button click event has deep link associated.
 @param identifier NSNumber as an identifier for the specific message.
 @param message NSString which may contain information on page to be loaded in application.
 @param triggerEvent contains information which type of event has been triggered
 */
- (void)didReceiveDeepLinkWithIdentifier:(nonnull NSNumber *)identifier withMessageString:(nonnull NSString *)message andTriggerEvent:(nonnull NSString *)triggerEvent;

/**
 Delegate method for informing a received Inapp message.
 @brief Method will be called when a button click event has deep link associated.
 @param identifier NSNumber as an identifier for the specific message.
 @param message NSString which may contain information on page to be loaded in application.
 */
- (void)didReceiveCustomLinkWithIdentifier:(nonnull NSNumber *)identifier withMessageString:(nonnull NSString *)message;

/**
 Delegate method for informing a received In Box message.
 @param messages contains the list of in box messages as InBoxMessage objects.
 */
- (void)didReceiveInBoxMessages:(NSArray *_Nullable)messages;


- (void)inAppCallFailedWithResponse:(NSString *_Nullable)response andError:(NSError *_Nullable)error;

/**
 Delegate method for informing a received In Box message.
 @param messages contains the list of in box messages as InBoxMessage objects.
 */
- (void)didReceiveInBoxMessage:(APXInBoxMessage *_Nullable)message;


@end

@interface AppoxeeInapp : NSObject

/*!
 * The Appoxee Inapp delegate.
 */
@property (nonatomic, weak, nullable) id <AppoxeeInappDelegate> delegate;
@property (nonatomic, weak, nullable) UIImage* closeButtonImage;
@property (nonatomic, readwrite) enum INAPPSERVER server;

/**
 Appoxee Inapp instance
 @brief Method for getting a singleton instance of AppoxeeInapp
 <pre><code>
 AppoxeeInapp *appoxeeInapp = [AppoxeeInapp shared];
 </pre></code>
 @return AppoxeeInapp an Instance Type of AppoxeeInapp.
 */
+ (nullable instancetype)shared;

/**
 Appoxee Inapp version
 @brief Method for getting the SDK version
 @code
 NSString *version = [AppoxeeInapp inappVersion];
 </pre></code>
 @return NSString representing the SDK version.
 */
+ (nonnull NSString *)inappVersion;

- (enum INAPPSERVER)getServer;

/**
 Method for engaging with AppoxeeInapp SDK.
 @brief Call this method to enable engagement with AppoxeeInapp SDK. You will typically call this method on application launch. 
 @attention Method is considered critical, and must be implemented.
 <pre><code>
 - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
 {
    [[AppoxeeInapp shared] engageWithDelegate:self];
 
    return YES;
 }
 </pre></code>
 @param delegate id which conforms to AppoxeeInappDelegate protocol.
 */
- (void)engageWithDelegate:(nullable id <AppoxeeInappDelegate>)delegate with:(enum INAPPSERVER) server;

- (void)engageWithDelegate:(nullable id <AppoxeeInappDelegate>)delegate andCloseImage:(nullable UIImage*)closeImage with:(enum INAPPSERVER) server;

/**
 Method for reporting an interactionable event, which may trigger a display of an Inapp message, provided by a Real Time Decisioning Engine.
 @brief Call this method to report significant events, which may trigger a display of an Inapp banner message or a full screen Inapp message.
 <pre><code>
 - (void)userDidConfirmPaymentOfAmount:(nonnull NSNumber *)amount
 {
    [[AppoxeeInapp shared] reportInteractionEventWithName:@"payment" andAttributes:@{@"amount" : amount}];
 }
 </pre></code>
 @param name NSString which describes the event name. No spaces, numbers, or symboles are allowed. Preferebly, use camelCase.
 @param attributes NSDictionary which contains Plist objects with any additional data.
 */
- (void)reportInteractionEventWithName:(nonnull NSString *)name andAttributes:(nullable NSDictionary <NSString *, NSObject <NSCoding> *> *)attributes;

/**
 Method for reporting an interactionable event, which may trigger a fetch of all inbox messages, provided by a Real Time Decisioning Engine.
 @brief Call this method to all inbox messages.
 This method sends response of fetched messages in delegate method calls.
 - (void)didReceiveInBoxMessages:(NSArray *_Nullable)messages
  */
- (void)fetchAPXInBoxMessages;

/**
 Method for load next message in queue
 @brief Call this method to all inbox messages.
 This method sends response of fetched messages in delegate method calls.
 - (void)didReceiveInBoxMessages:(NSArray *_Nullable)messages
 */

- (void)fetchInBoxMessageWithMessageId:(NSString *_Nullable)messageId;

- (void)fireInAppEventWithAppId:(NSString *_Nullable)appId tenantId:(NSString *_Nullable)tenantId userId:(NSString *_Nullable)userId deviceId:(NSString *_Nullable)deviceId jamieUrl:(NSString *_Nullable)jamieUrl alias:(NSString *)alias andEventName:(NSString *_Nullable)eventName;


@end
