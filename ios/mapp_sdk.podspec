#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint mapp_sdk.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'mapp_sdk'
  s.version          = '0.0.12'
  s.summary          = 'A new flutter plugin project.'
  s.description      = <<-DESC
A new flutter plugin project.
                       DESC
  s.homepage         = 'http://mapp.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Mapp Digital' => 'stefan.stevanovic@mapp.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.vendored_frameworks = [
    'Frameworks/AppoxeeSDK.xcframework',
    'Frameworks/AppoxeeLocationServices.xcframework',
    'Frameworks/AppoxeeInapp.xcframework'
  ]

  s.resources = [
    'Frameworks/AppoxeeSDKResources.bundle',
    'Frameworks/AppoxeeInappResources.bundle'
  ]
  s.requires_arc = true
  s.frameworks = "WebKit"
  s.library = 'sqlite3'
  s.platform = :ios, '10.0'
  s.static_framework = true
  s.swift_version = '5.0'


  # Flutter.framework does not contain a i386 slice.
  # Add both the Pods-installed path and the Flutter plugin symlink path
  # so headers/frameworks are found whether CocoaPods places files in
  # Pods/mapp_sdk/Frameworks or the Flutter .symlinks location.
  s.pod_target_xcconfig = {
    'HEADER_SEARCH_PATHS' => '$(inherited) "$(PODS_ROOT)/mapp_sdk/Frameworks/**" "$(PODS_ROOT)/../.symlinks/plugins/mapp_sdk/ios/Frameworks/**"',
    'FRAMEWORK_SEARCH_PATHS' => '$(inherited) "$(PODS_ROOT)/mapp_sdk/Frameworks" "$(PODS_ROOT)/../.symlinks/plugins/mapp_sdk/ios/Frameworks"',
    'DEFINES_MODULE' => 'YES',
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }
end
