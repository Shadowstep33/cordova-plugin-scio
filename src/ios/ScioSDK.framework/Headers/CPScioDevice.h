//
//  CPScioDevice.h
//  ConsumerPhysics
//
//  Created by Roee Kremer on 6/23/15.
//  Copyright (c) 2015 ConsumerPhysics. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString *const CPScioDeviceErrorDomain;

typedef NS_ENUM(NSUInteger, CPScioDeviceState) {
    CPScioDeviceStateDisconnected,
    CPScioDeviceStateConnecting,
    CPScioDeviceStateConnected
};

@interface CPScioDeviceInfo : NSObject
- (NSString*)name;
- (NSString*)identifier;
- (CPScioDeviceState)state;
- (BOOL)isConnectable;
- (NSNumber *)RSSI;
@end


@class CPScioReading;

/**
 'CPScioDevice' is your connection to ConsumerPhysics SCiO device. it is a singleton so use [CPScioDevice sharedInstance] for each call.
 The first call will ask you to power on your Bluetooth Radio if it is powered off.
 after that it will connect the last connected device.
 if the device is powered off, or not in range. it will be connected automatically when the conditions will be met. 
 if you want to connect other devices. you should use [CPScioDevice sharedInstance] browseWithRefreshBlock:].
*/
@interface CPScioDevice : NSObject
/**
 Initialize/Return a CPScioDevice singleton object.
 Power on Bluetooth radio.
 @return a CPScioDevice instance
*/
+ (id)sharedInstance;
@property (assign, nonatomic) BOOL enableLogs;


/**
 set handler to notify device state changes
 
 @param stateChangedBlock Notify asynchronously any change in device connectivity.
 */
- (void)setStateChangedBlock:(void (^)(CPScioDeviceState state))stateChangedBlock;

/**
 ask a for a list of new/previously connected devices.
 @param refreshBlock Notify asynchronously an update of available devices list (array of CPScioDeviceInfo).
*/
- (void)browseWithRefreshBlock:(void (^)(NSArray *devices))refreshBlock;

/**
 stop devices browsing.
*/
- (void)cancelBrowsing;

/**
 connect to a devices. a connection call, will stop devices browsing.
 @param device is the device to connect info.
 @param failureBlock Notify asynchronously a failed connection.
*/
- (void)connectDevice:(CPScioDeviceInfo *)device failure:(void (^)(NSError *error))failureBlock;

- (void)connectDevice:(CPScioDeviceInfo *)device success:(void (^)(void))successBlock failure:(void (^)(NSError *error))failureBlock;

/**
 disconnect current device
 */
- (void)disconnentDevice;

/**
 check if there is a connected device which is ready to scan.
*/
- (BOOL)isReady;

/**
 set block to handle a device's button press.
 @param buttonBlock a press handler
 */
- (void)setDeviceButtonBlock:(void (^)(void)) buttonBlock;

/**
 make a calibration scan
*/ 
- (void)calibrateWithCompletion:(void (^)(BOOL success))completion;
- (void)isCalibrationValid:(void (^)(BOOL success))completion;

/**
 ask a connected SCiO device for a scan.
 @param completion Notify asynchronously the success of the operation, retrieve the scan's reading or an error (if failed).
*/
- (void)scanWithCompletion:(void (^)(BOOL success,CPScioReading *reading,NSError *error))completion;


/**
 change SCiO device name
 @param name is the new name. Must contain up to 16 chars
 @param completion Notify asynchronously the success of the operation, retrieve an error (if failed).
 */
- (void)renameDeviceWithName:(NSString *)name completion:(void (^)(BOOL success, NSError *error))completion;

/**
 get battery status
 @param completion returns asynchronously the percentage value and isCharging or retrieve an error (if failed).
 */
- (void)getBatteryStatusWithCompletion:(void (^)(double percentage, BOOL isCharging, NSError *error))completion;

/**
 get connected SCiO device ID.
 */
- (NSString *)getDeviceID;

/**
 get connected SCiO device Name.
 */
- (NSString *)deviceName;

@end
