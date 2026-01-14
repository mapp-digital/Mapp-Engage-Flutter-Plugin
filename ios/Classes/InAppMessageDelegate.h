//
//  InAppMessageDelegate.h
//  testXcFrameworkObjc
//
//  Created by Mihajlo Jezdic on 9.12.21..
//

#import <Foundation/Foundation.h>
#import <Flutter/Flutter.h>

NS_ASSUME_NONNULL_BEGIN

@interface InAppMessageDelegate : NSObject

-(void)initWith:(FlutterMethodChannel*) channel;
+ (InAppMessageDelegate *)sharedObject;
-(void) addNotificationListeners;

@end

NS_ASSUME_NONNULL_END