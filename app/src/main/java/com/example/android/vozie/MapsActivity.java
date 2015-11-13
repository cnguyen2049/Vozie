package com.example.android.vozie;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mainMap;
    private Location currentLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mainMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mainMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mainMap.setMyLocationEnabled(true);
            // Check if we were successful in obtaining the map.
            if (mainMap != null) {
                // Set marker to current location
                mainMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

                    @Override
                    public void onMyLocationChange(Location arg0) {
                        currentLoc = new Location(arg0);

                        mainMap.addMarker(new MarkerOptions()
                                .position(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()))
                                .title("I am here!"));
                    }
                });
            }
        }
    }

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
    public void moveToAddress(Address input) {
        Double lat = (double) (input.getLatitude());
        Double lon = (double) (input.getLongitude());

        final LatLng location = new LatLng(lat, lon);

        Marker locMarker = mainMap.addMarker(new MarkerOptions()
                .position(location)
                .title(input.toString()));

        mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));

        mainMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    /* Opens default navigation to navigate to specified coordinates  */
    public void navigateToCoordinates(double lat, double lon) {
        String format = "geo:0,0?q=" + lat + "," + lon + "( Location title)";

        Uri uri = Uri.parse(format);


        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
