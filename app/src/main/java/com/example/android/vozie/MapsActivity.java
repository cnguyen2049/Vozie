package com.example.android.vozie;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;

import java.io.IOException;
import java.util.Locale;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleMap mainMap;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLoc, lastLocation;
    private LocationRequest mLocationRequest;
    private Marker locMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setUpMapIfNeeded();
    }

    protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mainMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mainMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mainMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mainMap != null) {
                // Create GoogleApiClient and connect
                buildGoogleApiClient();
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentLoc == null) {
            currentLoc = location;
            moveToLocationAnimated(currentLoc);
        } else {
            currentLoc = location;
        }
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /* Get list of address candidates given user string input */
    public List<Address> resultListFromUserInput(String input) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addressList = geoCoder.getFromLocationName(input, 10);
            if (addressList.size() > 0)
                return addressList;
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /* Animated movement to desired address */
    public void moveToLocationAnimated(Location input) {
        Double lat = (double) (input.getLatitude());
        Double lon = (double) (input.getLongitude());

        final LatLng location = new LatLng(lat, lon);

        setCurLocMarker(location);

        mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        mainMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
    }

    /* Simple movement to desired address */
    public void moveToLocation(Location input) {
        Double lat = (double) (input.getLatitude());
        Double lon = (double) (input.getLongitude());

        final LatLng location = new LatLng(lat, lon);

        setCurLocMarker(location);

        mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    public void setCurLocMarker(LatLng input) {
        String title = getAddressFromLocation(input).getAddressLine(0).toString();
        title += ", " + getAddressFromLocation(input).getAddressLine(1).toString();
        title += ", " + getAddressFromLocation(input).getAddressLine(2).toString();

        if (locMarker != null) {
            locMarker.remove();
        }

        locMarker = mainMap.addMarker(new MarkerOptions()
                .position(input)
                .title(title));
        locMarker.showInfoWindow();
    }

    /* Opens default navigation to navigate to specified coordinates  */
    public void navigateToCoordinates(double lat, double lon) {
        String format = "geo:0,0?q=" + lat + "," + lon + "( Location title)";

        Uri uri = Uri.parse(format);


        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public Address getAddressFromLocation(LatLng input) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        double lat = input.latitude;
        double lon = input.longitude;

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (java.io.IOException e) {
            addresses = null;
        }

        if (addresses != null)
            return addresses.get(0);
        else
            return null;
    }
}