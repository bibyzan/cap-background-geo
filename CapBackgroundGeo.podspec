
  Pod::Spec.new do |s|
    s.name = 'CapBackgroundGeo'
    s.version = '0.0.1'
    s.summary = 'since the rest don't work'
    s.license = 'MIT'
    s.homepage = 'https://github.com/bibyzan/cap-background-geo.git'
    s.author = 'bibyzan'
    s.source = { :git => 'https://github.com/bibyzan/cap-background-geo.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end