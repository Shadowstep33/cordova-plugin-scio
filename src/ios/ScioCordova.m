//
//  ScioCordova.m
//  ConsumerPhysics
//
//  Created by Daniel David on 06/06/2016.
//  Copyright © 2016 ConsumerPhysics. All rights reserved.
//

#import "ScioCordova.h"
#import <Cordova/CDV.h>
#import <ScioSDK/ScioSDK.h>

@interface ScioCordova () <>
	@property (weak, nonatomic) IBOutlet UILabel *sampleAppVersion;

	@property (weak, nonatomic) IBOutlet UILabel *scioNameLabel;
	@property (weak, nonatomic) IBOutlet UILabel *statusLabel;
	@property (weak, nonatomic) IBOutlet UILabel *userNameLabel;
	@property (weak, nonatomic) IBOutlet UILabel *modelLabel;
	@property (weak, nonatomic) IBOutlet UILabel *sdkVersionLabel;

	@property (weak, nonatomic) IBOutlet UITableView *tableView;

	@property (strong, nonatomic) NSDictionary *items;
	@property (strong, nonatomic) NSArray *sectionsTitles;

	@property (strong, nonatomic) CPScioReading *scanReading;
	@property (strong, nonatomic) CPScioDeviceInfo *currentDevice;
	@property (strong, nonatomic) CPScioModelInfo *currentModel;

	@property (assign, nonatomic) CPScioDeviceState deviceState;

	@property (strong, nonatomic) NSString *deviceName;
@end

@implementation ScioCordova

- (void)initializePlugin {
    
    __weak typeof(&*self)weakSelf = self;
    
    self.deviceState = CPScioDeviceStateDisconnected;
    self.deviceName = [[CPScioDevice sharedInstance] deviceName];
    // Update device status
    [[CPScioDevice sharedInstance] setStateChangedBlock:^(CPScioDeviceState state) {
        NSString *status = @"";
        weakSelf.deviceState = state;
        switch (state) {
            case CPScioDeviceStateDisconnected:
                status = @"Disconnected";
                break;
            case CPScioDeviceStateConnected:
            {
                status = @"Connected";
                self.scioNameLabel.text = self.deviceName ?: [[CPScioDevice sharedInstance] deviceName];
            }
                break;
            case CPScioDeviceStateConnecting:
                status = @"Connecting...";
                break;
            default:
                break;
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            weakSelf.statusLabel.text = status;
        });
        NSLog(@"Device state: %zd",state);
    }];
    
    // This can be use start scanning (calibrateWithCompletion or scanWithCompletion)
    [[CPScioDevice sharedInstance] setDeviceButtonBlock:^{
        [weakSelf toastWithTitle:@"SCiO Device" message:@"Button clicked"];
    }];
    
    if ([[CPScioCloud sharedInstance] isLoggedIn]) {
        [self getUserInfo];
    }
}

- (void)echo:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* echo = [command.arguments objectAtIndex:0];

    if (echo != nil && [echo length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:echo];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

/* Connect Device to Scio */
- (void)connect {
    NSLog(@"connectAPI");
    
    if (!self.currentDevice) {
        [self alertWithTitle:@"No Device" message:@"Select device before connecting"];
        return;
    }
    
    __weak typeof(&*self)weakSelf = self;
    [[CPScioDevice sharedInstance] connectDevice:self.currentDevice success:^{
        [weakSelf toastWithTitle:@"SCiO is connected" message:@"SCiO device connected successfully"];
    } failure:^(NSError *error) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf alertWithTitle:error.userInfo[NSLocalizedDescriptionKey]  message:error.userInfo[NSLocalizedFailureReasonErrorKey]];
        });
    }];

};

/* Find Scio */
- (void)scanble {
    NSLog(@"openSelectDeviceScreen");

    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];

    UINavigationController *nvc = [storyboard instantiateViewControllerWithIdentifier:@"select-device-navigation-controller"];
    SASelectDeviceViewController *vc = ((SASelectDeviceViewController *)nvc.topViewController);
    
    self.deviceState = CPScioDeviceStateDisconnected;
    
    __weak typeof(&*self)weakSelf = self;
    vc.onFinishBlock = ^(CPScioDeviceInfo *selected, UIViewController *presenting) {
        weakSelf.currentDevice = selected;
        weakSelf.deviceName = selected.name;
        if (selected) {
            dispatch_async(dispatch_get_main_queue(), ^{
                weakSelf.scioNameLabel.text = selected.name;
                weakSelf.statusLabel.text = @"Not connected";
            });
        } else {
            dispatch_async(dispatch_get_main_queue(), ^{
                weakSelf.scioNameLabel.text = @"";
            });
        }
        
        [presenting dismissViewControllerAnimated:YES completion:nil];
    };
    
    [self presentViewController:nvc animated:YES completion:nil];
};

/* Scan sample */
- (void)scan:(CDVInvokedUrlCommand*)command
{
    NSLog(@"scanAPI");

    if (![[CPScioDevice sharedInstance] isReady]) {
        [self alertWithTitle:@"No Device" message:@"Please connect a device"];
        return;
    }
    
    if (self.deviceState != CPScioDeviceStateConnected) {
        [self alertWithTitle:@"Device isn't connected" message:@"Make sure your device is on"];
        return;
    }
    
    __weak typeof(&*self)weakSelf = self;
    [[CPScioDevice sharedInstance] isCalibrationValid:^(BOOL success) {
        if (!success) {
            [weakSelf alertWithTitle:@"Calibration is invalid" message:@"Calibrate before scan"];
            return;
        }
        [weakSelf toastWithTitle:@"scanAPI" message:@"Scanning"];
        [[CPScioDevice sharedInstance] scanWithCompletion:^(BOOL success, CPScioReading *reading, NSError *error) {
            NSLog(@"Scan: %i",success);
            if (!success) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self alertWithTitle:error.userInfo[NSLocalizedDescriptionKey] message:error.userInfo[NSLocalizedFailureReasonErrorKey]];
                });
                return;
            }
            weakSelf.scanReading = reading;
            if (![SASampleFileUtils storeToDisk:reading fileName:SALastScanFileName]) {
                [self alertWithTitle:@"Failure" message:@"Failed to save last scan"];
            }
            
            [self toastWithTitle:@"Scan Completed" message:@"You can analyze a model now."];
        }];
    }];
};

/* Connect Device to Scio */
- (void)calibrate {
    NSLog(@"calibrateAPI");

    if (![[CPScioDevice sharedInstance] isReady]) {
        [self alertWithTitle:@"No Device" message:@"Please connect a device"];
        return;
    }
    
    if (self.deviceState != CPScioDeviceStateConnected) {
        [self alertWithTitle:@"Device isn't connected" message:@"Make sure your device is on"];
        return;
    }
    
    [self toastWithTitle:@"CalibrateAPI" message:@"Calibrating"];
    __weak typeof(&*self)weakSelf = self;
    [[CPScioDevice sharedInstance] calibrateWithCompletion:^(BOOL success) {
        NSLog(@"calibration: %i",success);
        
        if (!success) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf alertWithTitle:@"Calibration Failed" message:@"Please try again."];
            });
            return;
        }
        [self toastWithTitle:@"Calibration Completed" message:@"You can scan now"];
    }];
};

/* Login to SCIO cloud */
- (void)login {
    NSLog(@"loginAPI");
    CPScioLoginViewController *vc = [CPScioLoginViewController loginViewController];
    __weak typeof(&*vc)weakVC = vc;
    __weak typeof(&*self)weakSelf = self;
    
    [vc showLoginWithCompletion:^(BOOL success, NSError *error) {
        if (success) {
            [weakSelf getUserInfo];
            [weakVC dismissViewControllerAnimated:YES completion:nil];
        } else {
            [weakVC dismissViewControllerAnimated:YES completion:nil];
            [weakSelf alertWithTitle:@"Failed to login" message:error.userInfo[@"Error"]];
        }
    } inNavigationController:YES presentingViewController:self];
};

/* Analyze scan with model */
- (void)analyze {
    NSLog(@"AnalyzeAPI");
    
    if (!self.modelLabel.text.length) {
        [self alertWithTitle:@"Missing model" message:@"Select a model before analyzing a scan"];
        return;
    }
    
    if (![[CPScioCloud sharedInstance] isLoggedIn]) {
        [self alertWithTitle:@"Login required" message:@"You must login in order to get analyze"];
        return;
    }
    
    CPScioReading *lastScan = [SASampleFileUtils readArchiveWithFileName:SALastScanFileName];

    if (!lastScan) {
        [self alertWithTitle:@"Missing Scan" message:@"Scan before analyzing"];
        return;
    }
    
    [self toastWithTitle:@"Analyzing" message:[NSString stringWithFormat:@"Analyzing last saved scan. Model: %@", self.modelLabel.text]];
     
    __weak typeof(&*self)weakSelf = self;
    [[CPScioCloud sharedInstance] analyzeReading:lastScan modelIdentifier:self.currentModel.identifier completion:^(BOOL success, CPScioModel *model, NSError *error) {
        NSLog(@"analyze succeded: %i",success);
        if (success) {
            NSString *message = [NSString stringWithFormat:@"Type: %@\nValue: %@\n%@", model.modelType == CPScioModelTypeClassification ? @"Classification" : @"Estimation", model.attributeValue, model.modelType == CPScioModelTypeClassification ? [NSString stringWithFormat:@"Confidence: %.2lf\n", model.confidence] : @""];
            if (model.modelType == CPScioModelTypeClassification) {
                message = [message stringByAppendingString:[NSString stringWithFormat:@"Low Confidence: %@", model.lowConfidence ? @"YES" : @"NO"]];
            }
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf alertWithTitle:@"Results" message:message];
            });
        } else {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self alertWithTitle:[error.userInfo objectForKey:NSLocalizedDescriptionKey] message:[error.userInfo objectForKey:NSLocalizedFailureReasonErrorKey]];
            });
        }
    }];
}

/* set selected model 
- (void)setmodel:(NSString*)modelname
{
};*/

/* get available models 
- (void)getmodels {
};*/


@end