#import <Cordova/CDV.h>

@interface ScioCordova : CDVPlugin

- (void)initializePlugin;
- (void)echo:(CDVInvokedUrlCommand*)command;
- (void)connect;
- (void)scanble;
- (void)scan;
- (void)calibrate;
- (void)login;
- (void)analyze;

@end