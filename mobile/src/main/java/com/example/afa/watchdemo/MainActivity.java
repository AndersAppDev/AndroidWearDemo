package com.example.afa.watchdemo;

import android.Manifest;
import android.app.Notification;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private final int REQUEST_CONNECTION_FAILURE_RESOLUTION = 9000;

    private static final int UPDATE_INTERVAL_MS = 1000;
    private static final int FASTEST_INTERVAL_MS = 500;

    private static final int REQUEST_CODE = 1337;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest locationRequest;

    private Location lastLocation;

    private ListView locationsListView;
    private TextView tickerTextView;
    private int ticker = 0;

    private PlaceArrayAdapter placeArrayAdapter;
    private ArrayList<PlaceModel> placeModels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_INTERVAL_MS);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastLocation != null) {
                    Snackbar.make(findViewById(R.id.content_main), "Lat: " + lastLocation.getLatitude() + "\n" + "Lng: " + lastLocation.getLongitude(), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        locationsListView = (ListView) findViewById(R.id.locations_listview);
        tickerTextView = (TextView) findViewById(R.id.ticker);

        placeModels.addAll(getMockPlaces());

        placeArrayAdapter = new PlaceArrayAdapter(this, R.layout.place_row, placeModels);
        locationsListView.setAdapter(placeArrayAdapter);
        locationsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sendNotification(placeModels.get(position));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != REQUEST_CODE) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.getStatus().isSuccess()) {
                                Log.d(TAG, "Successfully requested location updates");
                            } else {
                                Log.d(TAG,
                                        "Failed in requesting location updates, "
                                                + "status code: "
                                                + status.getStatusCode()
                                                + ", message: "
                                                + status.getStatusMessage());
                            }
                        }
                    });
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        String[] permissions = new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection to location client suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection to location client failed");

        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_CONNECTION_FAILURE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");
        lastLocation = location;

        ticker++;
        tickerTextView.setText("Updated: " + ticker);

        placeArrayAdapter.notifyDataSetChanged();
    }

    private String getDistanceToText(Location to) {
        return lastLocation != null ? lastLocation.distanceTo(to) + " meters" : "Wait for proper GPS signal";
    }

    private void sendNotification(PlaceModel destination) {
        String contentTitle = lastLocation != null ? "Distance to " + destination.getName() : "Not yet located";
        String contentText = getDistanceToText(destination.getLocation());

        Notification notification = new NotificationCompat.Builder(getApplication())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setLocalOnly(false)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
        int notificationId = 9283;
        notificationManager.notify(notificationId, notification);
    }

    private List<PlaceModel> getMockPlaces() {
        List<PlaceModel> places = new ArrayList<>();
        places.add(new PlaceModel("Random sted 1", 56.274285, 10.305495));
        places.add(new PlaceModel("Random sted 2", 56.274287, 10.305406));
        places.add(new PlaceModel("Random sted 3", 56.274296, 10.305245));
        places.add(new PlaceModel("Random sted 4", 56.274278, 10.305744));

        return places;
    }

    private class PlaceArrayAdapter extends ArrayAdapter<PlaceModel> {

        private Context mContext;
        private int mResource;
        private List<PlaceModel> mPlaceModelList;

        public PlaceArrayAdapter(Context context, int resource, List<PlaceModel> placeModelList) {
            super(context, resource, placeModelList);

            this.mContext = context;
            this.mResource = resource;
            this.mPlaceModelList = placeModelList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);
            }

            PlaceModel place = mPlaceModelList.get(position);

            TextView name = (TextView) convertView.findViewById(R.id.place_name);
            name.setText(place.getName());

            TextView distance = (TextView) convertView.findViewById(R.id.distance);
            distance.setText(getDistanceToText(place.getLocation()));

            return convertView;
        }
    }
}
