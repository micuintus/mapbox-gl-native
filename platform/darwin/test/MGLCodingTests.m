#import <Mapbox/Mapbox.h>
#import <XCTest/XCTest.h>


@interface MGLCodingTests : XCTestCase
@end

@implementation MGLCodingTests

- (NSString *)temporaryFilePathForClass:(Class)clazz {
    return [NSTemporaryDirectory() stringByAppendingPathComponent:NSStringFromClass(clazz)];
}

- (void)testPointAnnotation {
    MGLPointAnnotation *annotation = [[MGLPointAnnotation alloc] init];
    annotation.coordinate = CLLocationCoordinate2DMake(0.5, 0.5);
    annotation.title = @"title";
    annotation.subtitle = @"subtitle";
    
    NSString *filePath = [self temporaryFilePathForClass:MGLPointAnnotation.class];
    [NSKeyedArchiver archiveRootObject:annotation toFile:filePath];
    MGLPointAnnotation *unarchivedAnnotation = [NSKeyedUnarchiver unarchiveObjectWithFile:filePath];
    
    XCTAssertEqualObjects(annotation, unarchivedAnnotation);
}

- (void)testPointFeature {
    MGLPointFeature *pointFeature = [[MGLPointFeature alloc] init];
    pointFeature.title = @"title";
    pointFeature.subtitle = @"subtitle";
    pointFeature.identifier = @(123);
    pointFeature.attributes = @{@"bbox": @[@1, @2, @3, @4]};
    
    NSString *filePath = [self temporaryFilePathForClass:MGLPointFeature.class];
    [NSKeyedArchiver archiveRootObject:pointFeature toFile:filePath];
    MGLPointFeature *unarchivedPointFeature = [NSKeyedUnarchiver unarchiveObjectWithFile:filePath];
    
    XCTAssertEqualObjects(pointFeature, unarchivedPointFeature);
}

@end
