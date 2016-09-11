package com.example.tarunchhabra.exampleapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.util.HashSet;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener {
    private SensorManager mSensorManager;
    private Sensor mLight;
    private Sensor mPressure;
    private GoogleApiClient mGoogleApiClient;
    private LatLng location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Context context = getApplicationContext();

        createNotification("Fall detected", "Click here for more info.");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            //Success ! there is a pressure sensor
            createToast(context, "Success ! there is a pressure sensor", Toast.LENGTH_SHORT);
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            float max = mPressure.getMaximumRange();
            createToast(context, "Max sensor value(pressure) : " + max, Toast.LENGTH_SHORT);
            mSensorManager.registerListener(PressureSensorListener, mPressure,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            createToast(context, "Failure ! no pressure sensor", Toast.LENGTH_SHORT);
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            //Success ! there is a pressure sensor
            createToast(context, "Success ! there is a light sensor", Toast.LENGTH_SHORT);
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            float max = mLight.getMaximumRange();
            createToast(context, "Max sensor value(light) : " + max, Toast.LENGTH_SHORT);
            mSensorManager.registerListener(LightSensorListener, mLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            createToast(context, "Failure ! no light sensor", Toast.LENGTH_SHORT);
        }
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, new PlaceFilter(false, new HashSet<String>()));
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                CharSequence name = "";
                float maxLikelihood = 0.0f;
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Log.d("LOG_TAG", String.format("Place '%s' has likelihood: %g",
                                placeLikelihood.getPlace().getName(),
                                placeLikelihood.getLikelihood()));
                        if (placeLikelihood.getLikelihood() > maxLikelihood) {
                            name = placeLikelihood.getPlace().getName();
                            maxLikelihood = placeLikelihood.getLikelihood();
                            location = placeLikelihood.getPlace().getLatLng();
                        }

                    }
                    createToast(context,"Nearest possible place is : " + name + " with likelihood of  : " + maxLikelihood, Toast.LENGTH_SHORT);
                    likelyPlaces.release();
                }
            });

            /*
            PendingResult<PlaceBuffer> plcResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, "General Hospital").setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(@NonNull PlaceBuffer places) {
                    if(places.getStatus().isSuccess()){
                        Place place = places.get(0);
                        Log.d("LOG_TAG", "Place found : " + place.getName() + " with address : " + place.getAddress());
                        createToast(context,"Place : " + place.getName() + " address : " + place.getAddress(), Toast.LENGTH_SHORT);
                    }
                    places.release();
                }
            });
            */
            /*
            location = new LatLng(35.772135, 78.689112);
            PendingResult<AutocompletePredictionBuffer> plcPredictions = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient,"General Hospital",toBounds(location,10000.00),null);
            if (plcPredictions.)
            plcPredictions.setResultCallback(new ResultCallback<AutocompletePredictionBuffer>() {
                @Override
                public void onResult(@NonNull AutocompletePredictionBuffer autocompletePredictions) {
                    if (autocompletePredictions.getStatus().isSuccess()){
                        for (AutocompletePrediction prediction : autocompletePredictions){
                            Log.d("LOG_TAG","Place prediction found : " + prediction.getFullText(null).toString() );
                        }
                    }
                }
            });
            */
            /*
            PendingResult<PlaceBuffer> plcResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, "General Hospital");
            plcResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                @Override
                public void onResult(@NonNull PlaceBuffer places) {
                    if(places.getStatus().isSuccess()){
                        for (Place place : places) {
                            Log.d("LOG_TAG", "Place found : " + place.getName() + " with address : " + place.getAddress());
                            createToast(context, "Place : " + place.getName() + " address : " + place.getAddress(), Toast.LENGTH_SHORT);
                        }
                    }
                    places.release();
                }
            });
            */

        }finally{

        }

    }

    private final SensorEventListener PressureSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            Context context = getApplicationContext();
            //createToast(context, "change detected", Toast.LENGTH_SHORT);
            if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
                float currentLux = event.values[0];
                //createToast(context, "Current bar:" + currentLux, Toast.LENGTH_SHORT);
                Log.d("LOG_TAG", "value : " + currentLux);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Context context = getApplicationContext();
            createToast(context, "sensor accuracy changed", Toast.LENGTH_SHORT);
        }
    };

    private final SensorEventListener LightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Context context = getApplicationContext();
            //createToast(context, "change detected", Toast.LENGTH_SHORT);
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                float currentLux = event.values[0];
                //createToast(context, "Current lux:" + currentLux, Toast.LENGTH_SHORT);
                Log.d("LOG_TAG", "value : " + currentLux);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Context context = getApplicationContext();
            createToast(context, "sensor accuracy changed", Toast.LENGTH_SHORT);
        }
    };


    private void createToast(Context context, CharSequence text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void createNotification(String title, String text) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_fd)
                        .setContentTitle(title)
                        .setContentText(text);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(23, mBuilder.build());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private LatLngBounds toBounds(LatLng center, double radius) {
        LatLng southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225);
        LatLng northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45);
        return new LatLngBounds(southwest, northeast);
    }
}
