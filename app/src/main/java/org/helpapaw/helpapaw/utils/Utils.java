package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.helpapaw.helpapaw.base.PawApplication;

/**
 * Created by iliyan on 7/25/16
 */
public class Utils {

    private static Utils instance;

    public synchronized static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public boolean hasNetworkConnection() {
        ConnectivityManager connectivity = (ConnectivityManager) PawApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    public float getDistanceBetween(double latitudePointOne, double longitudePointOne,
                                    double latitudePointTwo, double longitudePointTwo){
        Location pointOne = new Location("");
        pointOne.setLatitude(latitudePointOne);
        pointOne.setLongitude(longitudePointOne);

        Location pointTwo = new Location("");
        pointTwo.setLatitude(latitudePointTwo);
        pointTwo.setLongitude(longitudePointTwo);

        return pointOne.distanceTo(pointTwo);
    }
}
