package org.helpapaw.helpapaw.data.repositories.vetClinics;

import org.helpapaw.helpapaw.data.models.VetClinic;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VetClinicsParser {

    public static final String NAME = "name";
    public static final String GEOMETRY = "geometry";
    public static final String LOCATION = "location";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String REFERENCE = "reference";
    public static final String FORMATTED_ADDRESS = "formatted_address";
    public static final String INTERNATIONAL_PHONE_NUMBER = "international_phone_number";
    public static final String URL = "url";

    public List<VetClinic> parse(String jsonData) {
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

    public VetClinic parseDetails(String jsonData) {
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) new JSONObject(jsonData).get("result");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaceDetails(jsonObject);
    }

    private List<VetClinic> getPlaces(JSONArray jsonArray) {
        int placesCount = jsonArray.length();
        List<VetClinic> placesList = new ArrayList<>();
        VetClinic vetClinic;

        for (int i = 0; i < placesCount; i++) {
            try {
                vetClinic = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(vetClinic);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    private VetClinic getPlace(JSONObject googlePlaceJson) {
        String placeName = "";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            placeName = googlePlaceJson.getString(NAME);
            latitude = googlePlaceJson.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LAT);
            longitude = googlePlaceJson.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LNG);
            reference = googlePlaceJson.getString(REFERENCE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        VetClinic vetClinic = new VetClinic(reference,
                                            placeName,
                                            Double.parseDouble(latitude),
                                            Double.parseDouble(longitude));
        return vetClinic;
    }

    private VetClinic getPlaceDetails(JSONObject googlePlaceJson) {
        String placeName = "";
        String latitude = "";
        String longitude = "";
        String reference = "";
        String address = "";
        String phone = "";
        String url = "";

        try {
            placeName = googlePlaceJson.getString(NAME);
            latitude = googlePlaceJson.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LAT);
            longitude = googlePlaceJson.getJSONObject(GEOMETRY).getJSONObject(LOCATION).getString(LNG);
            reference = googlePlaceJson.getString(REFERENCE);
            address = googlePlaceJson.getString(FORMATTED_ADDRESS);
            phone = googlePlaceJson.getString(INTERNATIONAL_PHONE_NUMBER);
            url = googlePlaceJson.getString(URL);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        VetClinic vetClinic = new VetClinic(reference, placeName, Double.parseDouble(latitude),
                Double.parseDouble(longitude), phone, address, url);

        return vetClinic;
    }
}

// Example of place details
/*
{
   "html_attributions" : [],
   "result" : {
      "address_components" : [
         {
            "long_name" : "Sofia",
            "short_name" : "Sofia",
            "types" : [ "locality", "political" ]
         },
         {
            "long_name" : "Kambanite",
            "short_name" : "Kambanite",
            "types" : [ "neighborhood", "political" ]
         },
         {
            "long_name" : "Sofia City Province",
            "short_name" : "Sofia City Province",
            "types" : [ "administrative_area_level_1", "political" ]
         },
         {
            "long_name" : "Bulgaria",
            "short_name" : "BG",
            "types" : [ "country", "political" ]
         },
         {
            "long_name" : "1138",
            "short_name" : "1138",
            "types" : [ "postal_code" ]
         }
      ],
      "adr_address" : "Панчарево ул &quot;Черешова градина&quot;1, \u003cspan class=\"postal-code\"\u003e1138\u003c/span\u003e \u003cspan class=\"region\"\u003eKambanite\u003c/span\u003e, \u003cspan class=\"locality\"\u003eSofia\u003c/span\u003e, \u003cspan class=\"country-name\"\u003eBulgaria\u003c/span\u003e",
      "business_status" : "OPERATIONAL",
      "formatted_address" : "Панчарево ул \"Черешова градина\"1, 1138 Kambanite, Sofia, Bulgaria",
      "formatted_phone_number" : "02 979 0935",
      "geometry" : {
         "location" : {
            "lat" : 42.61632629999999,
            "lng" : 23.40097549999999
         },
         "viewport" : {
            "northeast" : {
               "lat" : 42.61771168029149,
               "lng" : 23.4023501802915
            },
            "southwest" : {
               "lat" : 42.61501371970849,
               "lng" : 23.39965221970849
            }
         }
      },
      "icon" : "https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/generic_business-71.png",
      "icon_background_color" : "#7B9EB0",
      "icon_mask_base_uri" : "https://maps.gstatic.com/mapfiles/place_api/icons/v2/generic_pinlet",
      "international_phone_number" : "+359 2 979 0935",
      "name" : "Veterinary clinic \"Blue Cross\"",
      "opening_hours" : {
         "open_now" : true,
         "periods" : [
            {
               "close" : {
                  "day" : 1,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 0,
                  "time" : "0800"
               }
            },
            {
               "close" : {
                  "day" : 2,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 1,
                  "time" : "0800"
               }
            },
            {
               "close" : {
                  "day" : 3,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 2,
                  "time" : "0800"
               }
            },
            {
               "close" : {
                  "day" : 4,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 3,
                  "time" : "0800"
               }
            },
            {
               "close" : {
                  "day" : 5,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 4,
                  "time" : "0800"
               }
            },
            {
               "close" : {
                  "day" : 6,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 5,
                  "time" : "0800"
               }
            },
            {
               "close" : {
                  "day" : 0,
                  "time" : "0100"
               },
               "open" : {
                  "day" : 6,
                  "time" : "0800"
               }
            }
         ],
         "weekday_text" : [
            "Monday: 8:00 am – 1:00 am",
            "Tuesday: 8:00 am – 1:00 am",
            "Wednesday: 8:00 am – 1:00 am",
            "Thursday: 8:00 am – 1:00 am",
            "Friday: 8:00 am – 1:00 am",
            "Saturday: 8:00 am – 1:00 am",
            "Sunday: 8:00 am – 1:00 am"
         ]
      },
      "photos" : [
         {
            "height" : 4032,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/111401547238375130271\"\u003eJoana Leventieva\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEAzQX35jKwIpkvyFx0FmxJVxYsbor6ugy2hDeax-rpUw7E5rVG1Z4bJ6Ot50xfSHCFiKn3W-7ubVqurE0fPk0fuMOU9GIO6P-0t9oaidIt4Do5-yz9tarGg6eRlRkmo6Iym4AE4DTF0ZotxkqBLDk3lJJAd85_SSLHVkoMScbTdLLEv",
            "width" : 3024
         },
         {
            "height" : 2976,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/103176994269494902002\"\u003eIvailo Shopov\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEDCTwl8GYZmeKgmEf5V2FSvc66UamPP31sc7zvFH5qIye0sFrDRtPGrwvh0RCpl_Tmq-A6uPiXen44tSmtfdAemrRLSULvOB8mIvg-eOQ68jbuqgBCO30_2hgHhrUnGiUa3AQQR2RXmydTb7n6V1xtkaRdBAAo06qdO2gi_T7Rnyf3u",
            "width" : 3968
         },
         {
            "height" : 2988,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/116481661593556671787\"\u003eBoris Bachvarov\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEDd0FlEkXsTF0lIfY3dcPM8GKYAwyeMKpTjGDo6H8O-Y2NFrLLEXgs7qtCMHCAPGymWC3xYtAMky4YkKILOZxSMCAZIB8THjddDv-v9u9hJdu1hvRoVLDXjRjmV7fFS4D7fPlcC6H0Mrlgya_UXfS1txA3C7nvIJPhrRIUY6jNH46xm",
            "width" : 5312
         },
         {
            "height" : 2976,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/107073364274435617850\"\u003eVladimir Georgiev\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEDed2EhvQH78Kq009GLFLwLaaXmslK8_pXU_B7iVu7jjTwHvwyisMubBikxY2gE_4dbAjrpkfyYTuOT0rch9BCp9oF6b2CXoPt4J7zgpp8rk6VZ9HV1mUlprwbxRTvBrnRuap4ykFwbcrRTwFjH7D16xD_RjeDyWdsckLxS4l6pl6EJ",
            "width" : 3968
         },
         {
            "height" : 5504,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/117018848329679737628\"\u003eTeodora Yordanova\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uECRH2gLXRcQESvvVRFGYYPptDZpQeuUtSJVrvAsB5lu6g3xALiEcJ_T7MdLboy5Fu6bVGKytyjQjAqGbHI__3Xk5ZpnWya8HvLbQLAYddUA3QwhJivESZcEHVlvb1w2UedD4jqEGJsIyEM9GcMZKWr0E3y5SUsXiLtzjHF4y0TxXZuW",
            "width" : 3096
         },
         {
            "height" : 4618,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/115539423595727262738\"\u003eСимеон Пешев\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEDveZYNV4gBWR9byfbkYv1kxb7vJol94kX-5OW-Kdsjg-ZoeuqnS-N1sqF1Odr_3mA5X3BnL__szzAsIKAOkAWeBLJrBmcv4hzAocUqaaWfMifMicGGya778WAEdTZCC4ljA0nwJFl2RVxW9YbN4CLEl06RQgrLf9KOYTyvwqvUev9P",
            "width" : 3464
         },
         {
            "height" : 1280,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/116371724614161359558\"\u003eДимитринка Замфирова\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEDdrtNPp44eui8eeKwovWyxKDAWelycD9yAhzfeWy8InQsJb9AMx3y4rVE0nL9lq1z27k11lY386n2MC7aBi8zXASEg58JyVY85sWdAL5qRsIRI2q7WObv61OdCeWVihfgx5OoeljBg_L5GcfZLcdxoOKeL5udA4ERJ5JleOhEuroyv",
            "width" : 960
         },
         {
            "height" : 4032,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/109817330982813772083\"\u003eМаргарита Ватева\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEAGNveBIZ-DIcggGPMzL-Gd60gjGOwxllOJ6BnM510qjBA3nrS-HE6e0CaktQC7lxbB0H4zvDpdVNE9alNUw8ALmi0DUZ-Vij_F-Axt3VXtxmenIshxEKQZ2uP80o65U4pzAD0RD9XYB7S3ZcHiXpPIX0aB8WSSxxSh0Y8o8KXJsx22",
            "width" : 1960
         },
         {
            "height" : 4032,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/114206150684712415861\"\u003eGeorgi Dimitrov\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEBUu_Wt8eIY04V0Kz2Vu5nbMhdZV_X5eHj71oej0TBs0TRNjqVq3ilPct3-JOTgME07hVrV6XEi4qCHprK7VHyheua0-krmBhdQzHlseiNy-6Gtqds2F1N-ub_DOIYeXxufkVzB3oET24zSdxPJrI1v0rdxR0F1ZIc0Y2I2EjJ9mrwe",
            "width" : 3024
         },
         {
            "height" : 3491,
            "html_attributions" : [
               "\u003ca href=\"https://maps.google.com/maps/contrib/101898409710247831594\"\u003eVasilena Atanasova\u003c/a\u003e"
            ],
            "photo_reference" : "Aap_uEDXttC4RyqCfX1XpEPLuxdTVQGm5f2bej4cE_GY455hSR4IeicTREr8KufOLXc8qZFmMz3H10kLxH8dqy6WyRC-pRpBZmCPBCOky65AYrKgUqDjle0XJC2ZtMxso5w5LwSwZ4kqgbhkIlkWs4SJvHCbQ2rfQMZPwYsvV8WRz8iZjW-Z",
            "width" : 2639
         }
      ],
      "place_id" : "ChIJ7YIyns2AqkAR4X_GtOVAQXM",
      "plus_code" : {
         "compound_code" : "JC82+G9 Sofia, Bulgaria",
         "global_code" : "8GJ5JC82+G9"
      },
      "rating" : 4.3,
      "reference" : "ChIJ7YIyns2AqkAR4X_GtOVAQXM",
      "reviews" : [
         {
            "author_name" : "Nikolay Gornishki",
            "author_url" : "https://www.google.com/maps/contrib/110824819318777119332/reviews",
            "language" : "en",
            "profile_photo_url" : "https://lh3.googleusercontent.com/a/AATXAJx1BYjnKWJblRM7BHIe6Hbc9t_xP4-V7B96IYFK=s128-c0x00000000-cc-rp-mo",
            "rating" : 1,
            "relative_time_description" : "3 weeks ago",
            "text" : "Don’t leave your pet unattended with these people. They put my cat to sleep without telling me in the middle of the night and lied that they tried calling. This is not a place to help you or your pet but to monetise on suffering.",
            "time" : 1654569740
         },
         {
            "author_name" : "Kiril Dimitrov",
            "author_url" : "https://www.google.com/maps/contrib/115460975399839414650/reviews",
            "language" : "en",
            "profile_photo_url" : "https://lh3.googleusercontent.com/a-/AOh14GjkeTtVjYX4Q-Wtr-0oh49TI42U-asQ52YbT5NCAw=s128-c0x00000000-cc-rp-mo-ba4",
            "rating" : 4,
            "relative_time_description" : "2 weeks ago",
            "text" : "Good service.  Great doctors. Prices are a bit on the high side.\nCan get crowded on weekends",
            "time" : 1654959293
         },
         {
            "author_name" : "Bot",
            "author_url" : "https://www.google.com/maps/contrib/101083226367673821318/reviews",
            "language" : "en",
            "profile_photo_url" : "https://lh3.googleusercontent.com/a/AATXAJwxuBHyqZJu4SFrailJFwlgQKS-RXRSF2LfICP6=s128-c0x00000000-cc-rp-mo-ba4",
            "rating" : 5,
            "relative_time_description" : "11 months ago",
            "text" : "Best vets in Sofia. Very polite, young staff. Highly recommended!",
            "time" : 1625841271
         },
         {
            "author_name" : "Kristian Krastev",
            "author_url" : "https://www.google.com/maps/contrib/106734811426262724889/reviews",
            "language" : "en",
            "profile_photo_url" : "https://lh3.googleusercontent.com/a-/AOh14Giar0JcB9djAx4d3k63ixyrkCNU8ShRW4JCRpkhgA=s128-c0x00000000-cc-rp-mo-ba4",
            "rating" : 4,
            "relative_time_description" : "2 weeks ago",
            "text" : "Good clinic, but a little bit outside the city.",
            "time" : 1654873612
         },
         {
            "author_name" : "Marina Koleva",
            "author_url" : "https://www.google.com/maps/contrib/114657335501943069551/reviews",
            "language" : "en",
            "profile_photo_url" : "https://lh3.googleusercontent.com/a-/AOh14GiEMClLqLWza8Fp1AzISvIDaCboSscSlGyER6Jl3Q=s128-c0x00000000-cc-rp-mo",
            "rating" : 5,
            "relative_time_description" : "8 months ago",
            "text" : "Great clinic. I have taken my dog there before on multiple occasions and last week I called on the phone and a veterinarian gave me advice on the phone, for free, no need to go there. Great people!",
            "time" : 1635138717
         }
      ],
      "types" : [ "veterinary_care", "point_of_interest", "establishment" ],
      "url" : "https://maps.google.com/?cid=8304990543172501473",
      "user_ratings_total" : 1439,
      "utc_offset" : 180,
      "vicinity" : "Панчарево ул \"Черешова градина\"1, Sofia",
      "website" : "http://www.bluecrossbg.com/"
   },
   "status" : "OK"
}
* */
