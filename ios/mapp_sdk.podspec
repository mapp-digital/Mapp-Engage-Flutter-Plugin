#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint mapp_sdk.podspec` to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'mapp_sdk'
  s.version          = '0.0.1'
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
  s.dependency 'plugin_mappsdk, '6.0.5'
  s.platform = :ios, '10.0'


  # Flutter.framework does not contain a i386 slice.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64 i386', 'VALID_ARCHS[sdk=iphoneos*]' => 'arm64' }
end
