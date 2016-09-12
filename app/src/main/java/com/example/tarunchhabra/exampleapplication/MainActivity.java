package com.example.tarunchhabra.exampleapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tarunchhabra.utils.CommonMethods;
import com.example.tarunchhabra.utils.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import java.util.Random;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public final static String EXTRA_MESSAGE = "com.example.tarunchhabra.exampleapplication.MESSAGE";
    public final static String PRESSURE_VALUE = "com.example.tarunchhabra.exampleapplication.PRESSURE";
    public final static String LIGHT_VALUE = "com.example.tarunchhabra.exampleapplication.LIGHT";
    public final static String LOCATION = "com.example.tarunchhabra.exampleapplication.LOCATION";
    public final static String ADDRESS = "com.example.tarunchhabra.exampleapplication.ADDRESS";

    private SensorManager mSensorManager;
    private Sensor mLight;
    private Sensor mPressure;
    private CommonMethods methods;
    private Context current;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private float mPressureValue = 0.0f;
    private int mLightValue = 0;
    private String mAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        current = getApplicationContext();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    protected void startIntentService(){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        if (mResultReceiver == null)
            mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        methods = new CommonMethods();
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            //Success ! there is a pressure sensor
            methods.createToast(current, "There is a pressure sensor available", Toast.LENGTH_SHORT);
            mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            //float max = mPressure.getMaximumRange();
            //methods.createToast(current, "Max sensor value(pressure) : " + max, Toast.LENGTH_SHORT);
            mSensorManager.registerListener(PressureSensorListener, mPressure,
                    SensorManager.SENSOR_DELAY_NORMAL);

        } else {
            methods.createToast(current, "Failure ! no pressure sensor.", Toast.LENGTH_SHORT);
            mPressureValue = 29.4f;
            TextView pressure = (TextView) findViewById(R.id.pressureValue);
            pressure.setText(mPressureValue + " inHg");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            //Success ! there is a pressure sensor
            methods.createToast(current, "There is a light sensor available", Toast.LENGTH_SHORT);
            mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            //float max = mLight.getMaximumRange();
            //methods.createToast(current, "Max sensor value(light) : " + max, Toast.LENGTH_SHORT);
            mSensorManager.registerListener(LightSensorListener, mLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            methods.createToast(current, "Failure ! no light sensor", Toast.LENGTH_SHORT);
            mLightValue = 399;
        }

        mGoogleApiClient.connect();


    }

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(PressureSensorListener);
        mSensorManager.unregisterListener(LightSensorListener);
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    public void sendMessage(View view) {
        Random rand = new Random();
        int side = rand.nextInt(2);
        if (side == 1) {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            String message = "Fall simulation started";
            intent.putExtra(EXTRA_MESSAGE, message);
            intent.putExtra(PRESSURE_VALUE, mPressureValue);
            intent.putExtra(LIGHT_VALUE, mLightValue);
            intent.putExtra(LOCATION, mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
            intent.putExtra(ADDRESS, mAddress);
            startActivity(intent);
        } else {
            methods.createToast(current,"Didn't fall, try clicking again",Toast.LENGTH_SHORT);
        }
    }

    private final SensorEventListener PressureSensorListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            float currentPressure = event.values[0];
            // Convert PinHg
            currentPressure = 0.02952998751f * currentPressure;
            TextView pressure = (TextView) findViewById(R.id.pressureValue);
            mPressureValue = currentPressure;
            pressure.setText(mPressureValue + " inHg");
            Log.i("LOG_FD", "pressure value : " + mPressureValue);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i("LOG_FD", "Pressure sensor accuracy changed");
        }
    };

    private final SensorEventListener LightSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                int currentLux = (int)event.values[0];
                TextView lux = (TextView) findViewById(R.id.lightValue);
                mLightValue = currentLux;
                lux.setText(mLightValue + " lx");
                Log.i("LOG_TAG", "light value : " + mLightValue);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.i("LOG_FD", "Light sensor accuracy changed");
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Location oLocation = null;
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        oLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (oLocation != null) {
            mLastLocation = oLocation;
            startIntentService();

            if (!Geocoder.isPresent()){
                methods.createToast(current,"No geocoder present", Toast.LENGTH_SHORT);
                return;
            }

        }

    }

    @SuppressLint("ParcelCreator")
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        protected void onReceiveResult(int resultCode, Bundle resultData){
            // Display the address string
            // or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            if (resultCode == Constants.SUCCESS_RESULT){
                TextView textView = (TextView) findViewById(R.id.currentAddress);
                mAddress = mAddressOutput;
                textView.setText(mAddress);
            }

        }
    }


    @Override
    public void onConnectionSuspended(int i) {
        methods.createToast(current,"Connection to Google services disrupted",Toast.LENGTH_SHORT);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        methods.createToast(current,"Connection to Google services failed !",Toast.LENGTH_SHORT);
    }
}
