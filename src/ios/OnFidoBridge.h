#import <Cordova/CDVPlugin.h>

@interface OnFidoBridge : CDVPlugin {
}

- (void)scan: (CDVInvokedUrlCommand *)command;

@end
