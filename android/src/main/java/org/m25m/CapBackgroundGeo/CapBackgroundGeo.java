package org.m25m.CapBackgroundGeo;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

@NativePlugin(
        permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        }
)
public class CapBackgroundGeo extends Plugin {
    public static CapBackgroundGeo instance;
    public static final String TAG = "CapBackgroundGeo";

    private PluginCall currentCall;

    @Override
    public void load() {
        super.load();
        instance = this;
        Log.i(TAG, "LOADed");
    }

    @PluginMethod()
    public void echo(PluginCall call) {

        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", value);
        Log.i(TAG, "MEssage");
        call.success(ret);
    }

    @PluginMethod()
    public void start(PluginCall call) {
        this.currentCall = call;

        if (super.hasRequiredPermissions()) {
            this.startService();
        } else {
            Log.i(TAG, "requesting necessary permissions");
            super.pluginRequestAllPermissions();
        }
    }

    private void startService() {
        Log.i(TAG, "STARTING SERVICE");

        Intent serviceIntent = new Intent(super.getContext(), BackgroundService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            Log.i(TAG, "Hit foreground start");
            super.getContext().startForegroundService(serviceIntent);
        } else {
            Log.i(TAG, "Hit regular start");
            super.getContext().startService(serviceIntent);
        }

        if (this.currentCall != null) {
            this.currentCall.success();
            this.currentCall = null;
        }
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e(TAG, "got permission result");
        if (this.currentCall == null) {
            return;
        }

        for(int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                this.currentCall.error("User denied location permission");
                this.currentCall = null;
                Log.e(TAG, "User denied permission to location");
                return;
            }
        }

        this.startService();
    }

    @PluginMethod()
    public void stop(PluginCall call) {
        Log.i(TAG, "stopping service");

        Intent serviceIntent = new Intent(getContext(), BackgroundService.class);
        getContext().stopService(serviceIntent);

        call.success();
    }

    void processLocation(Location loc) {
        Log.e(TAG, loc.toString());
        super.notifyListeners("geo-update", this.getJSObjectForLocation(loc));
    }

    private JSObject getJSObjectForLocation(Location location) {
        JSObject ret = new JSObject();
        JSObject coords = new JSObject();
        ret.put("coords", coords);
        ret.put("timestamp", location.getTime());
        JSObject geometry = new JSObject(); //THIS is only being returned for now
        geometry.put("lat", location.getLatitude());
        geometry.put("lng", location.getLongitude());
        coords.put("geometry", geometry);
        coords.put("accuracy", location.getAccuracy());
        coords.put("altitude", location.getAltitude());
        if (Build.VERSION.SDK_INT >= 26) {
            coords.put("altitudeAccuracy", location.getVerticalAccuracyMeters());
        }
        coords.put("speed", location.getSpeed());
        coords.put("heading", location.getBearing());
        return geometry;
    }

    public static class BackgroundService extends Service {
        private LocationManager locationManager;
        private LocationListener locationListener;
        private boolean isGettingUpdates = false;

        public static final String CHANNEL_ID = "CapLocationServiceChannel";

        public BackgroundService() {
            super();
        }

        @Override
        public void onCreate() {
            super.onCreate();

            this.locationManager = (LocationManager) instance.getContext().getSystemService(Context.LOCATION_SERVICE);

            this.locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    instance.processLocation(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.i(TAG, "Service started");
            this.getLocationUpdates();
            return START_NOT_STICKY;
        }

        public void getLocationUpdates() {
            if (this.isGettingUpdates) {
                Log.i(TAG, "Attempted starting location updates more than once");
                return;
            }

            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel serviceChannel = new NotificationChannel(
                        CHANNEL_ID,
                        "Background geo Service Channel",
                        NotificationManager.IMPORTANCE_DEFAULT
                );

                NotificationManager manager = getSystemService(NotificationManager.class);
                manager.createNotificationChannel(serviceChannel);
            }
            Intent notificationIntent = new Intent(this, BackgroundService.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification notification =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setContentTitle("Sending location updates")
                            .setContentText("Donation dandy is getting your location")
                            .setContentIntent(pendingIntent)
                            .setTicker("Ticker")
                            .build();

            startForeground(1, notification);

            Criteria c = new Criteria();
            c.setAccuracy(Criteria.ACCURACY_FINE);
            c.setAltitudeRequired(false);
            c.setBearingRequired(false);
            c.setSpeedRequired(false);

            String provider = this.locationManager.getBestProvider(c, true);
            try {
                this.locationManager.requestLocationUpdates(provider, 5000, 10, this.locationListener);
                this.isGettingUpdates = true;
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
