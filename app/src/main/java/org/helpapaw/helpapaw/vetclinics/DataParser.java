package org.helpapaw.helpapaw.vetclinics;

import org.helpapaw.helpapaw.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {
    public List<HashMap<String, String>> parse(String jsonData) {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for (int i = 0; i < placesCount; i++) {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));

//                String request = "https://maps.googleapis.com/maps/api/place/detail/json?placeid" +
//                        ((JSONObject) jsonArray.get(i)).getString("place_id") +
//                        "&key=" +  R.string.google_android_map_api_key_test; // TODO - we need to change this
//
//
//                VetClinicsTask vetClinicsTask = new VetClinicsTask();
//                vetClinicsTask.execute(request);

                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson) {
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";
//        String phoneNumber = "";

        try {
            if (!googlePlaceJson.isNull("name")) {
                placeName = googlePlaceJson.getString("name");
            }
            if (!googlePlaceJson.isNull("vicinity")) {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJson.getString("reference");
            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }
}
