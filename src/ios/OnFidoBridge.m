#import "OnFidoBridge.h"

#import <Cordova/CDVAvailability.h>

@implementation OnFidoBridge

- (void)pluginInitialize {
}

- (void)echo:(CDVInvokedUrlCommand *)command {
  NSString* phrase = [command.arguments objectAtIndex:0];
  NSLog(@"%@", phrase);
}

- (void)getDate:(CDVInvokedUrlCommand *)command {
  NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
  NSLocale *enUSPOSIXLocale = [NSLocale localeWithLocaleIdentifier:@"en_US_POSIX"];
  [dateFormatter setLocale:enUSPOSIXLocale];
  [dateFormatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ssZZZZZ"];

  NSDate *now = [NSDate date];
  NSString *iso8601String = [dateFormatter stringFromDate:now];

  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:iso8601String];
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (void)scan: (CDVInvokedUrlCommand *)command {
  ONFlowConfigBuilder *configBuilder = [ONFlowConfig builder];

  [configBuilder withToken:@"test_jNkxF8-E4hfeUiHgNx_OZGtdl4F2ntpS"];
  [configBuilder withApplicantId:@"6e9b8817-42a5-4822-9693-79333169bc42"];
  [configBuilder withDocumentStep];
  [configBuilder withFaceStepOfVariant:ONFaceStepVariantPhoto];

  NSError *configError = NULL;
  ONFlowConfig *config = [configBuilder buildAndReturnError:&configError];

  if (configError == NULL) {
      ONFlow *onFlow = [[ONFlow alloc] initWithFlowConfiguration:config];
      [onFlow withResponseHandler:^(ONFlowResponse *response) {
          CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"Hello World!"];
          [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
      }];

      NSError *runError = NULL;
      UIViewController *onfidoController = [onFlow runAndReturnError:&runError];

      if (runError == NULL) {
        [self presentViewController:onfidoController animated:YES completion:NULL];
      }
  }
}

@end
