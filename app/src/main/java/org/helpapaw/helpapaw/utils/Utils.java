package org.helpapaw.helpapaw.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.helpapaw.helpapaw.base.PawApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

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

    //Validation
    public boolean isEmailValid(String email) {
        Pattern EMAIL_ADDRESS
                = Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        );

        return EMAIL_ADDRESS.matcher(email).matches();
    }

    //Network
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

    public static String getHtml(String url) throws IOException {
        // Build and set timeout values for the request.
        URLConnection connection = (new URL(url)).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        connection.connect();

        // Read and store the result line by line then return the entire string.
        InputStream    in     = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder  html   = new StringBuilder();
        for (String line; (line = reader.readLine()) != null; ) {
            html.append(line);
        }
        in.close();

        return html.toString();
    }

    //Location
    public float getDistanceBetween(double latitudePointOne, double longitudePointOne,
                                    double latitudePointTwo, double longitudePointTwo) {
        Location pointOne = new Location("");
        pointOne.setLatitude(latitudePointOne);
        pointOne.setLongitude(longitudePointOne);

        Location pointTwo = new Location("");
        pointTwo.setLatitude(latitudePointTwo);
        pointTwo.setLongitude(longitudePointTwo);

        return pointOne.distanceTo(pointTwo);
    }

    @SuppressLint("DefaultLocale")
    public static String getWktPoint(double longitude, double latitude) {
        return String.format(Locale.ENGLISH, "Point (%.15f %.15f)", longitude, latitude);
    }

    //Dates
    public String getFormattedDate(Date date) {
        String formattedDate = "";

        try {
            String     DETAILS_DATE_FORMAT = "dd.MM.yyyy, hh:mm a";
            DateFormat targetFormat = new SimpleDateFormat(DETAILS_DATE_FORMAT, Locale.getDefault());
            formattedDate = targetFormat.format(date);
        }
        catch (Exception ex) {
            Log.d(Utils.class.getName(), "Failed to parse date.");
        }

        return formattedDate;
    }

    public static int convertBooleanArrayToInt(boolean[] arr) {
        int n = 0;
        for (int i = arr.length - 1; i >= 0; i--) {
            n = (n << 1) | (arr[i] ? 1 : 0);
        }
        return n;
    }

    public static boolean[] convertIntegerToBooleanArray(int n, int arraySize) {
        boolean[] booleanArr = new boolean[arraySize];

        for (int i = arraySize - 1; i >= 0; i--) {
            if ((n & (1 << i)) > 0) {
                booleanArr[i] = true;
            } else {
                booleanArr[i] = false;
            }
        }

        return booleanArr;
    }

    public static String selectedTypesToString(boolean[] selectedSignalTypes, String[] signalTypes) {
        String selectedTypesToString = "";

        if (allSelected(selectedSignalTypes)) {
            selectedTypesToString = "All signal types";
        } else if (noneSelected(selectedSignalTypes)) {
            selectedTypesToString = "None";
        } else {
            for (int i = 0; i < selectedSignalTypes.length; i++) {
                if (selectedSignalTypes[i]) {
                    selectedTypesToString = selectedTypesToString + signalTypes[i] + ", ";
                }
            }
            selectedTypesToString = selectedTypesToString.substring(0, selectedTypesToString.length() - 2);
        }

        return selectedTypesToString;
    }

    public static boolean allSelected(boolean[] selectedSignalTypes) {
        for (boolean selectedSignalType : selectedSignalTypes) {
            if (!selectedSignalType) {
                return false;
            }
        }
        return true;
    }

    public static boolean noneSelected(boolean[] selectedSignalTypes) {
        for (boolean selectedSignalType : selectedSignalTypes) {
            if (selectedSignalType) {
                return false;
            }
        }
        return true;
    }


    /**
     * @param signalType The signal type for which to check. Should have only a single bit raised
     * @param signalTypeSettings The notification settings against which to check. Should have raised
     *                           bits for each signal type that the user wants to be notified about
     * @return Whether the respective user should be notified about that signal type or not
     */
    public static boolean shouldNotifyAboutSignalType(int signalType, int signalTypeSettings) {
        return ((signalTypeSettings & (1 << signalType)) > 0);
    }
}
