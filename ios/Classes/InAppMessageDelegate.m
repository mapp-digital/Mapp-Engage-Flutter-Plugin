//  Created by Mihajlo Jezdic on 9.12.21..
//

#import "InAppMessageDelegate.h"
#import "AppoxeeInappSDK.h"

@interface InAppMessageDelegate() <AppoxeeInappDelegate>

@property FlutterMethodChannel* channel;

@end

@implementation InAppMessageDelegate

+ (InAppMessageDelegate *)sharedObject {
    static InAppMessageDelegate *sharedClass = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedClass = [[self alloc] init];
    });
    return sharedClass;
}

- (void)initWith:(FlutterMethodChannel *)channel {
    self.channel = channel;
    NSLog(@"init inapp listers object!");
}

- (void)addNotificationListeners {
    //add notifications listeners
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"didReceiveDeepLinkWithIdentifier" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(didReceiveDeepLinkWithIdentifier:) name:@"didReceiveDeepLinkWithIdentifier" object:nil];

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"didReceiveInappMessageWithIdentifier" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(didReceiveInappMessageWithIdentifier:) name:@"didReceiveInappMessageWithIdentifier" object:nil];

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"didReceiveCustomLinkWithIdentifier" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(didReceiveCustomLinkWithIdentifierHandler:) name:@"didReceiveCustomLinkWithIdentifier" object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"didReceiveInBoxMessages" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(didReceiveInBoxMessagesHandler:) name:@"didReceiveInBoxMessages" object:nil];

    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"didReceiveInBoxMessage" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(didReceiveInBoxMessageHandler:) name:@"didReceiveInBoxMessage" object:nil];
      
    [[NSNotificationCenter defaultCenter] removeObserver:self name:@"didReceiveCustomLinkWithIdentifier" object:nil];
    [NSNotificationCenter.defaultCenter addObserver:self selector:@selector(didReceiveCustomLinkWithIdentifierHandler:) name:@"didReceiveCustomLinkWithIdentifier" object:nil];\
    NSLog(@"notification listeners for inapp added!");
}

//selectors to forward data to flutter level
- (void)didReceiveDeepLinkWithIdentifier:(NSNotification *)notification {
    NSLog(@"notification reveived %@!", notification);
    [self.channel invokeMethod:@"didReceiveDeepLinkWithIdentifier" arguments:notification.userInfo];
}

- (void)didReceiveInappMessageWithIdentifier:(NSNotification *)notification {
    NSLog(@"notification reveived in didReceiveInappMessage %@!", notification);
    [self.channel invokeMethod:@"didReceiveInappMessageWithIdentifier" arguments:notification.userInfo];
}

- (void)didReceiveCustomLinkWithIdentifierHandler:(NSNotification *)notification {
    NSLog(@"notification reveived in didReceiveCustomLinkWith %@!", notification);
    [self.channel invokeMethod:@"didReceiveCustomLinkWithIdentifier" arguments:notification.userInfo];
}

- (void)didReceiveInBoxMessagesHandler:(NSNotification *)notification {
    NSLog(@"notification reveived in didReceiveInBoxMessages %@!", notification);
    [self.channel invokeMethod:@"didReceiveInBoxMessages" arguments:notification.userInfo];
}

- (void)inAppCallFailedWithResponseHandler:(NSNotification *)notification {
    NSLog(@"notification reveived in inAppCallFailedWithResponse %@!", notification);
    [self.channel invokeMethod:@"inAppCallFailedWithResponse" arguments:notification.userInfo];
}

- (void)didReceiveInBoxMessageHandler:(NSNotification *)notification {
    NSLog(@"notification reveived in didReceiveInBoxMessage %@!", notification);
    [self.channel invokeMethod:@"didReceiveInBoxMessage" arguments:notification.userInfo];
}

//delegate method
-(void)didReceiveDeepLinkWithIdentifier:(NSNumber *)identifier withMessageString:(NSString *)message andTriggerEvent:(NSString *)triggerEvent {
    NSLog(@"kavabunga %@, %@, %@", identifier, message, triggerEvent);
    if (identifier && message && triggerEvent) {
      NSLog(@"notification: %@", @{@"action":[identifier stringValue], @"url": message, @"event_trigger": triggerEvent });
        [[NSNotificationCenter defaultCenter] postNotificationName:
     @"didReceiveDeepLinkWithIdentifier" object:nil userInfo:@{@"action":[identifier stringValue], @"url": message, @"event_trigger": triggerEvent }];
     NSLog(@"notification sent!");
    }
}


- (void)appoxeeInapp:(nonnull AppoxeeInapp *)appoxeeInapp didReceiveInappMessageWithIdentifier:
(nonnull NSNumber *)identifier andMessageExtraData:(nullable NSDictionary <NSString *, id> *)
messageExtraData{
  NSDictionary* dict = [[NSDictionary alloc] init];
        if (messageExtraData) {
            dict = @{@"id": [identifier stringValue], @"extraData": messageExtraData};
        } else {
            dict = @{@"id": [identifier stringValue]};
        }
        [[NSNotificationCenter defaultCenter] postNotificationName:
        @"didReceiveInappMessageWithIdentifier" object:nil userInfo:dict];
        NSLog(@"notification sent!");
}

- (void)didReceiveCustomLinkWithIdentifier:(nonnull NSNumber *)identifier withMessageString:(nonnull NSString *)message{
  if (identifier && message) {
    [[NSNotificationCenter defaultCenter] postNotificationName:
        @"didReceiveInappMessageWithIdentifier" object:nil userInfo:@{@"id":[identifier stringValue], @"message": message}];
        NSLog(@"notification sent!");
        NSLog(@"didReceive Custom Link With Identifier: %@!", @{@"id":[identifier stringValue], @"message": message});
          }
}

- (void)didReceiveInBoxMessages:(NSArray *_Nullable)messages{
  NSMutableArray *dicts = [[NSMutableArray alloc] init];
  NSLog(@"inbox messages: %@", messages);
    for(APXInBoxMessage *message in messages) {
        [dicts addObject:[message getDictionary]];
    }
    if (dicts) {
        [[NSNotificationCenter defaultCenter] postNotificationName:
       @"didReceiveInBoxMessages" object:nil userInfo:dicts];
        NSLog(@"Receive inbox Messages");
    }

}

- (void)inAppCallFailedWithResponse:(NSString *_Nullable)response andError:(NSError *_Nullable)error{
   NSError* newError = [NSError errorWithDomain:@"com.mapp.flutterError" code:200 userInfo:@{@"Error reason": @"There is not data"}];
        NSString* newResponse = @"There is not data!";
        if (error)
            newError = error;
        if (response)
            newResponse = response;
        [[NSNotificationCenter defaultCenter] postNotificationName:
       @"inAppCallFailedWithResponse" object:nil userInfo:@{@"error": newError, @"response": newResponse}];
        NSLog(@"inApp Call Failed With Response: %@!", @{@"error": newError, @"response": newResponse});
}

- (void)didReceiveInBoxMessage:(APXInBoxMessage *_Nullable)message{
    if ([message getDictionary]) {
      [[NSNotificationCenter defaultCenter] postNotificationName:
       @"didReceiveInBoxMessage" object:nil userInfo:[message getDictionary]];
        NSLog(@"did Receive InBox Message: %@!", [message getDictionary]);
    }
}

@end
