import Foundation
import Capacitor
import CoreLocation

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitor.ionicframework.com/docs/plugins/ios
 */
@objc(CapBackgroundGeo)
public class CapBackgroundGeo: CAPPlugin, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    
    override public func load() {
        self.manager.delegate = self
        self.manager.pausesLocationUpdatesAutomatically = false
        self.manager.allowsBackgroundLocationUpdates = true
        
        self.manager.requestWhenInUseAuthorization()
    }
    
    @objc func echo(_ call: CAPPluginCall) {
        print("PLSSSSS PRINTTTTT")
        let value = call.getString("value") ?? ""
        call.success([
            "value": value
        ])
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        for loc in locations {
            print(loc)
        }
    }
    
    public func locationManagerDidResumeLocationUpdates(_ manager: CLLocationManager) {
        print("resumed")
    }
    
    public func locationManagerDidPauseLocationUpdates(_ manager: CLLocationManager) {
        print("paused")
    }
    
    public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("failed")
        print(error.localizedDescription)
    }
    
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        print("changed auth status")
        print(status)
        if status == .authorizedWhenInUse {
            self.manager.startUpdatingLocation()
        }
    }
}
