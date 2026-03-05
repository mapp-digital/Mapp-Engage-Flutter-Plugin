#ifdef __OBJC__
#import <UIKit/UIKit.h>
#else
#ifndef FOUNDATION_EXPORT
#if defined(__cplusplus)
#define FOUNDATION_EXPORT extern "C"
#else
#define FOUNDATION_EXPORT extern
#endif
#endif
#endif

#import "InAppMessageDelegate.h"
#import "MappSdkPlugin.h"
#import "PushMessageDelegate.h"

FOUNDATION_EXPORT double mapp_sdkVersionNumber;
FOUNDATION_EXPORT const unsigned char mapp_sdkVersionString[];

