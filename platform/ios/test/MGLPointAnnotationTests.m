#import <Mapbox/Mapbox.h>
#import <XCTest/XCTest.h>

@interface MGLPointAnnotationTests : XCTestCase

@end

@implementation MGLPointAnnotationTests

- (void)setUp {
    [super setUp];
    // Put setup code here. This method is called before the invocation of each test method in the class.
}

- (void)testCoding {
    MGLPointAnnotation *annotation = [[MGLPointAnnotation alloc] init];
    annotation.coordinate = CLLocationCoordinate2DMake(0.5, 0.5);
    annotation.title = @"title";
    annotation.subtitle = @"subtitle";
    
    NSString *filePath = [NSTemporaryDirectory() stringByAppendingPathComponent:@"pointAnnotation"];
    [NSKeyedArchiver archiveRootObject:annotation toFile:filePath];
    
    MGLPointAnnotation *unarchivedAnnotation = [NSKeyedUnarchiver unarchiveObjectWithFile:filePath];
    
    XCTAssertEqualObjects(annotation, unarchivedAnnotation);
}

@end
