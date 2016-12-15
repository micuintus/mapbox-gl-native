#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

#import <mbgl/util/feature.hpp>

@interface NSCoder (MGLAdditions)

- (void)mgl_encodeLocationCoordinates2D:(std::vector<CLLocationCoordinate2D>)coordinates forKey:(NSString *)key;

- (std::vector<CLLocationCoordinate2D>)mgl_decodeLocationCoordinates2DForKey:(NSString *)key;

@end
