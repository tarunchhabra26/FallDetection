package com.example.tarunchhabra.unirest.maps;

import android.util.Log;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by tarunchhabra on 9/11/16.
 */

/**
 * This class defines a method to get results using Google Maps API distance
 * matrix search using RESTful calls
 *
 * @author tarunchhabra
 */
public class DistanceMatrixSearch {
    private final String HTTP_BASE_URL = "https://maps.googleapis.com/maps/api/distancematrix/";
    private final String API_KEY = "AIzaSyBlPLa-A2MXB9H5RgG34nL9EXp0kpED2Rs";
    public final static String JSON = "json";
    public final static String XML = "xml";

    /**
     * Method to get results from Google Maps API
     *
     * @param input      - A Map of key, values which are to be given as input
     * @param outputType - A JSON array of results
     * @return
     */
    public JSONArray getDistanceBetweenPlaces(Map<String, Object> input, String outputType) {
        Set<String> keySet = null;
        JSONArray results = null;

        outputType = JSON;

        if (input == null) {
            return results;
        }

        keySet = input.keySet();
        if (!keySet.contains("origins") && input.get("origins") == null && !keySet.contains("destinations")
                && input.get("destinations") == null) {
            return results;
        }
        try {
            HttpResponse<JsonNode> request = Unirest.get(HTTP_BASE_URL + outputType).queryString(input)
                    .queryString("key", API_KEY).asJson();
            JSONObject myObj = request.getBody().getObject();
            if (myObj != null)
                results = myObj.getJSONArray("rows");

        } catch (UnirestException e) {
            Log.e("LOG_TAG", "UnirestException occurred : ");
        } catch (JSONException e) {
            Log.e("LOG_TAG", "JSONException occurred : ");
        } finally {
            try {
                Unirest.shutdown();
            } catch (IOException e) {
                Log.e("LOG_TAG", "IOException occurred : ");
            }
        }
        return results;
    }

}

