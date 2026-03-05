//
//  InBoxMessage.h
//  AppoxeeInapp
//
//  Created by Neelesh on 29/03/18.
//  Copyright © 2018 Teradata. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <WebKit/WebKit.h>

@interface APXInBoxMessage : NSObject

/*!
 * The template id of message
 */
@property (nonatomic, strong, readonly, nonnull) NSString        *    messageId;

/*!
 * HTML content of the message received
 */
@property (nonatomic, strong, readonly, nullable) NSString        *   content;

/*!
 * Subject of the message
 */
@property (nonatomic, strong, readonly, nullable) NSString        *   subject;

/*!
 * Status of message (READ, UNREAD & DELETED)
 */
@property (nonatomic, strong, readonly, nullable) NSString        *   status;

/*!
 * Summary of message
 */
@property (nonatomic, strong, readonly, nullable) NSString        *   summary;

/*!
 * URL of the icon related to message
 */
@property (nonatomic, strong, readonly, nullable) NSString        *   iconUrl;

/*!
 * Message sent time stamp UTC
 */
@property (nonatomic, strong, readonly, nullable) NSDate          *   sentDate;

/*!
 * Message expiry time stamp UTC
 */
@property (nonatomic, strong, readonly, nullable) NSDate          *   expireDate;

/*!
 * Extra parameters
 */
@property (nonatomic, strong, readonly, nullable) NSDictionary    *   extras;

/*!
 * whole dictionary
 */
@property (nonatomic, strong, readonly, nullable) NSDictionary    *   allDict;

/*!
 * Method used to create message from server response
 */
+ (nullable instancetype)messageFromKeyedValues:(nullable NSDictionary *)keyedValues withEventId:(NSString *_Nonnull)eventId;

/*!
 Return whole dictionary to addopt cordova plugin
 */
- (NSDictionary *)getDictionary;

/*!
 * Handles all actions based on the request received
 */
- (BOOL)handleAPXActionsForRequest:(NSURLRequest *_Nullable)request navigationType:(WKNavigationType)navigationType;

/*!
 * Method used to change message status to read
 */
- (void)markAsRead;

/*!
 * Method used to change message status to unread
 */
- (void)markAsUnread;

/*!
 * Method used to create message status to delete
 */
- (void)markAsDeleted;


/*!
 * Not to be used by developer. It is for internal mapping only
 */
- (NSString *_Nonnull)getMessageIdentifier;

@end
