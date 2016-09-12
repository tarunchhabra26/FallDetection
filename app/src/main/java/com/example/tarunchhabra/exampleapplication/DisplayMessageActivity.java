package com.example.tarunchhabra.exampleapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class DisplayMessageActivity extends AppCompatActivity {
    private float pressureValue;
    private int lightValue;
    private String address;
    private String location;
    private String destPlcId;
    private String destAddress;
    private String destName;
    private String destTimeText;
    private Long destTimeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        pressureValue = intent.getFloatExtra(MainActivity.PRESSURE_VALUE,0.0f);
        lightValue = intent.getIntExtra(MainActivity.LIGHT_VALUE,0);
        address = intent.getStringExtra(MainActivity.ADDRESS);
        location = intent.getStringExtra(MainActivity.LOCATION);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);



        TextView fallDetect = (TextView) findViewById(R.id.extraMessage);
        fallDetect.setText(message);

        Log.i("LOG_FD","Pressure Value found" + pressureValue);
        TextView pressure = (TextView) findViewById(R.id.pressureValue);
        pressure.setText("Pressure : " + pressureValue+" inHg");

        Log.i("LOG_FD","Light Value found" + lightValue);
        TextView light = (TextView) findViewById(R.id.lightValue);
        light.setText("Illuminance : " + lightValue + " lx");

        Log.i("LOG_FD","Origin address" + address);
        TextView origin = (TextView)findViewById(R.id.originValue);
        origin.setText("Origin : " + address);
    }

    @Override
    protected void onStart(){
        super.onStart();
        // Text search
        try {
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?rankby=distance&opennow=true&sensor=true&query=general+hospital&key=AIzaSyBlPLa-A2MXB9H5RgG34nL9EXp0kpED2Rs&location="+location;
            JSONParser parser = new JSONParser();
            JSONObject object = new JSONObject(parser.doInBackground(url));
            Log.i("LOG_FD", "response : " + object.toString());
            JSONArray results = object.getJSONArray("results");
            if (results.length() > 0) {
                JSONObject topResult  = (JSONObject) results.get(0);
                destPlcId = topResult.getString("place_id");
                destAddress = topResult.getString("formatted_address");
                destName = topResult.getString("name");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView destination = (TextView)findViewById(R.id.destinationValue);
        destination.setText("Destination : " + destName);

        try {
            String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + URLEncoder.encode(address, "utf-8") + "&destinations="+URLEncoder.encode(destAddress,"utf-8")+"&departure_time="+System.currentTimeMillis()+"&traffic_model=best_guess&key=AIzaSyBlPLa-A2MXB9H5RgG34nL9EXp0kpED2Rs";
            JSONParser parser = new JSONParser();
            JSONObject object = new JSONObject(parser.doInBackground(url));
            Log.i("LOG_FD", "response : " + object.toString());
            JSONArray results = object.getJSONArray("rows");
            if (results.length() > 0) {
                destTimeText = results.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getString("text");
                destTimeValue = results.getJSONObject(0).getJSONArray("elements").getJSONObject(0).getJSONObject("duration").getLong("value");
            }
        }  catch (JSONException | UnsupportedEncodingException e) {
             e.printStackTrace();
        }

        TextView dest = (TextView)findViewById(R.id.ttValue);
        dest.setText("Travel Time : " + destTimeText);

        boolean is_emergency = isEmergency(pressureValue,lightValue,destTimeValue);

        if (!is_emergency){
            TextView t1 = (TextView)findViewById(R.id.emergency);
            t1.setText("Not an emergency");
            TextView t2 = (TextView)findViewById(R.id.name);
            t2.setText("");
            TextView t3 = (TextView)findViewById(R.id.insurance);
            t3.setText("");
            TextView t4 = (TextView)findViewById(R.id.age);
            t4.setText("");
            TextView t5 = (TextView)findViewById(R.id.bloodgroup);
            t5.setText("");
            TextView t6 = (TextView)findViewById(R.id.allergy);
            t6.setText("");
            TextView t7 = (TextView)findViewById(R.id.eContact);
            t7.setText("");
            TextView t8 = (TextView)findViewById(R.id.ecContact);
            t8.setText("");
            TextView t9 = (TextView)findViewById(R.id.ecName);
            t9.setText("");
        }


    }

    private boolean isEmergency(float pressure, int lux, long travelTime){
        boolean result = false;
        if (pressure < 29.5f && lux < 400 && travelTime > 300L)
            result = true;
        return  result;
    }

    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}
