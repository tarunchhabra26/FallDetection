package com.example.tarunchhabra.unirest.maps;

import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

/**
 * This class defines a method to get results using Google Maps API text search
 * using RESTful calls
 *
 * @author tarunchhabra
 */
public class TextSearch {
    private final String HTTP_BASE_URL = "https://maps.googleapis.com/maps/api/place/textsearch/";
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
    public JSONArray getSuggestedPlaces(Map<String, Object> input, String outputType) {
        Set<String> keySet = null;
        JSONArray results = null;

        outputType = JSON;

        if (input == null) {
            return results;
        }

        keySet = input.keySet();
        if (keySet.contains("query") && input.get("query") != null) {
            try {
                HttpResponse<JsonNode> request = Unirest.get(HTTP_BASE_URL + outputType).queryString(input)
                        .queryString("key", API_KEY).asJson();
                JSONObject myObj = request.getBody().getObject();
                results = myObj.getJSONArray("results");

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
        } else {
            throw new IllegalArgumentException("Required argument `query` is missing or is null !");
        }
        return results;
    }


}

