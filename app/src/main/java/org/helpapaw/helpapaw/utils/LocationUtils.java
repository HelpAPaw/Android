package org.helpapaw.helpapaw.utils;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by Emil Ivanov on 10/16/2016.
 */

public class LocationUtils {


    /**
     * Time difference threshold set for one minute.
     */
    static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;


    public static Location getLastKnownLocation(String provider, LocationManager locationManager) {

        // Returns last known location, this is the fastest way to get a location fix.
        Location fastLocation = locationManager.getLastKnownLocation(provider);

        return fastLocation;
    }

    public static void removeLocationUpdates(LocationManager locationManager, LocationListener locationListener) {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Decide if new location is better than older by following some basic criteria.
     * This algorithm can be as simple or complicated as your needs dictate it.
     * Try experimenting and get your best location strategy algorithm.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    public static boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, of course the new location is better.
        if (oldLocation == null) {
            return true;
        }

        // Check if new location is newer in time.
        boolean isNewer = newLocation.getTime() > oldLocation.getTime();

        // Check if new location more accurate. Accuracy is radius in meters, so less is better.
        boolean isMoreAccurate = newLocation.getAccuracy() < oldLocation.getAccuracy();
        if (isMoreAccurate && isNewer) {
            // More accurate and newer is always better.
            return true;
        } else if (isMoreAccurate && !isNewer) {
            // More accurate but not newer can lead to bad fix because of user movement.
            // Let us set a threshold for the maximum tolerance of time difference.
            long timeDifference = newLocation.getTime() - oldLocation.getTime();

            // If time difference is not greater then allowed threshold we accept it.
            if (timeDifference > -TIME_DIFFERENCE_THRESHOLD) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldRequestUpdate(Location oldLocation, Location current) {
        boolean shouldRequestUpdate = true;

        float distance = current.distanceTo(oldLocation);

        if (distance > 5) {
            shouldRequestUpdate = true;
        }

        return shouldRequestUpdate;

    }


    public static float getDistance(Location old, Location current){

        return current.distanceTo(old);
    }
}
