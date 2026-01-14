//
//  PushMessageDelegate.h
//  testXcFrameworkObjc
//
//  Created by Stefan Stevanovic on 9.12.21..
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface PushMessageDelegate : NSObject

-(void)initWith:(FlutterMethodChannel*) channel;
+ (PushMessageDelegate *)sharedObject;
-(void) addNotificationListeners;

@end

NS_ASSUME_NONNULL_END
