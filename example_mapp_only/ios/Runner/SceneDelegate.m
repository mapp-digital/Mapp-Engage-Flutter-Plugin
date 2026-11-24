#import "SceneDelegate.h"
#import <signal.h>

@implementation SceneDelegate

- (void)sceneDidBecomeActive:(UIScene *)scene {
  [super sceneDidBecomeActive:scene];
  signal(SIGPIPE, SIG_IGN);
}

- (void)sceneWillEnterForeground:(UIScene *)scene {
  [super sceneWillEnterForeground:scene];
  signal(SIGPIPE, SIG_IGN);
}

@end
