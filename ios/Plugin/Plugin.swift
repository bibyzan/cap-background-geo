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
    private var currentCall: CAPPluginCall? = nil
    private var isAuthorized = false
    
    override public func load() {
        self.manager.delegate = self
        self.manager.pausesLocationUpdatesAutomatically = false
        self.manager.allowsBackgroundLocationUpdates = true
    }
    
    @objc func echo(_ call: CAPPluginCall) {
        print("PLSSSSS PRINTTTTT")
        let value = call.getString("value") ?? ""
        call.success([
            "value": value
        ])
    }
    
    @objc func start(_ call: CAPPluginCall) {
        if !self.isAuthorized {
            self.currentCall = call
            self.manager.requestWhenInUseAuthorization()
            print("Requesting when in use")
        } else {
            self.manager.startUpdatingLocation()
            print("started updates!")
            call.success()
        }
    }
    
    @objc func stop(_ call: CAPPluginCall) {
        self.manager.stopUpdatingLocation()
        print("stopped location updates")
        call.success()
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        //for loc in locations {
            // print(loc)
        //}
        if let first = locations.first {
            self.notifyListeners("geo-update", data: [
                "lat": first.coordinate.latitude,
                "lng": first.coordinate.longitude])
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
        print(error)
    }
    
    public func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        print("changed auth status")
        print(status)
        self.isAuthorized = status == .authorizedWhenInUse
        if status == .authorizedWhenInUse,
            let call = self.currentCall {
            self.manager.startUpdatingLocation()
            print("starting location updates")
            call.success()
            self.currentCall = nil
        }
    }
}
