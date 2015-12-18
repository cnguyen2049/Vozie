package com.example.android.vozie;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.location.Location;
import android.location.Geocoder;
import android.location.Address;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

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
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener, ErrorAlert.NoticeDialogListener {

    private GoogleMap mainMap;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLoc, lastLocation;
    private LocationRequest mLocationRequest;
    private LocationManager lm;
    private Marker locMarker;
    private EditText searchText;
    private boolean service_enabled = false;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private boolean ErrorAlertPopped = false;
    private Thread checkThread;
    private String currentText;

    /*-------------------------------*/
    /* MapsActivity Callback Methods */
    /*-------------------------------*/
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

    @Override
    public void onConnected(Bundle connectionHint) {
        service_enabled = true;
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            connectErrorAlert("Network Connection Lost");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            connectErrorAlert("Play Service Disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        connectErrorAlert(result.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        if (currentLoc == null) {
            currentLoc = location;
            moveToLocationAnimated(currentLoc);
        } else {
            currentLoc = location;
            moveToLocation(currentLoc);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        ErrorAlertPopped = false;
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
    }

    /*--------------------------*/
    /* Initialization Functions */
    /*--------------------------*/
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mainMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mainMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mainMap.setMyLocationEnabled(false);
            // Check if we were successful in obtaining the map.
            if (mainMap != null) {
                // Create GoogleApiClient and connect
                buildGoogleApiClient();
                mGoogleApiClient.connect();

                initializeSearchBox();
                initializeMap();
            }
        }
    }

    public void initializeSearchBox() {
        searchText = (EditText) findViewById(R.id.search_text);
        Drawable search = ContextCompat.getDrawable(this, R.drawable.search);

        search.setBounds(0, 0, 100, 100);
        searchText.setCompoundDrawables(search, null, null, null);
        searchText.setCursorVisible(false);
        searchText.setFocusable(true);
        searchText.setClickable(true);

        searchText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    searchText.setCursorVisible(true);
                    searchText.setHint("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchText, InputMethodManager.SHOW_IMPLICIT);
                    return true;
                }
                return false;
            }
        });

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
                /* Sets global currentText and attempts to create adapter before next character
                   is entered. */
                currentText = charsequence.toString();
                threadSetAdapter();
            }

            @Override
            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void threadSetAdapter() {
        final Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.search_text);
                ArrayAdapter sendAdapter = (ArrayAdapter) msg.obj;

                if (sendAdapter != null)
                    textView.setAdapter(sendAdapter);
                else
                    textView.setAdapter(null);
            }
        };

        Thread adapterThread = new Thread((new Runnable() {
            public void run() {
                ArrayAdapter<String> adapter;
                List<Address> addresses = resultListFromUserInput(currentText);
                String tText = currentText;

                if (addresses != null) {
                    String[] array = new String[addresses.size()];
                    for (int l = 0; l < addresses.size(); l++) {
                        Address indAddress = addresses.get(l);
                        array[l] = indAddress.getAddressLine(0) + " "
                                + indAddress.getAddressLine(1) + " "
                                + indAddress.getAddressLine(2);
                    }
                    adapter = new ArrayAdapter<String>(MapsActivity.this,
                            android.R.layout.simple_list_item_1, array);

                    /* Before setting adapter, make sure that the text hasn't changed in separate
                    thread */
                    if (tText.equals(currentText)) {
                        Message msg = new Message();
                        msg.obj = adapter;
                        mHandler.sendMessage(msg);
                        return;
                    }

                } else {
                    if (tText.equals(currentText)) {
                        Message msg = new Message();
                        msg.obj = null;
                        mHandler.sendMessage(msg);
                        return;
                    }
                }
            }
        }));
        adapterThread.start();
    }

    public void initializeMap() {
        mainMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng l) {
                searchText.setCursorVisible(false);
                searchText.setHint(R.string.search_hint);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
            }
        });
    }

    /* Build GoogleApiClient and call createLocationRequest */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    /* Fills mLocationRequest with proper request data */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);
    }

    /* Begins updating location if google play service, gps, and network are enabled. */
    private void startLocationUpdates() {
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {}

        initializeConnectionChecking();

        if (service_enabled && gps_enabled && network_enabled)
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                    mLocationRequest, this);
        else
            connectErrorAlert("Error Connecting.");
    }

    private void initializeConnectionChecking() {
        checkThread = new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ex) {}

                    try {
                        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception ex) {}

                    if (!service_enabled || !gps_enabled || !network_enabled)
                        if (!ErrorAlertPopped)
                            connectErrorAlert("Error Connecting.");

                    SystemClock.sleep(5000);
                }
            }
        });
        checkThread.start();
    }


    /*----------------------*/
    /* Map Helper Functions */
    /*----------------------*/
    /* Animated movement to desired address */
    public void moveToLocationAnimated(Location input) {
        try {
            Double lat = (double) (input.getLatitude());
            Double lon = (double) (input.getLongitude());

            final LatLng location = new LatLng(lat, lon);

            setLocMarker(location);

            mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 2));
            mainMap.animateCamera(CameraUpdateFactory.zoomTo(16), 4000, null);
        } catch (NullPointerException e) {
            connectErrorAlert("Connection Error");
        }
    }

    /* Instant movement to desired address */
    public void moveToLocation(Location input) {
        Double lat = (double) (input.getLatitude());
        Double lon = (double) (input.getLongitude());

        final LatLng location = new LatLng(lat, lon);

        setLocMarker(location);

        mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    /* Resets locMarker to specified LatLng */
    public void setLocMarker(LatLng input) {
        String title = getAddressFromLocation(input).getAddressLine(0);
        title += ", " + getAddressFromLocation(input).getAddressLine(1);
        title += ", " + getAddressFromLocation(input).getAddressLine(2);

        if (locMarker != null)
            locMarker.remove();

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

    /* Returns most probable address from LatLng coordinates */
    public Address getAddressFromLocation (LatLng input) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;

        double lat = input.latitude;
        double lon = input.longitude;

        try {
            addresses = geocoder.getFromLocation(lat, lon, 1);
        } catch (java.io.IOException e) { addresses = null; }

        if (addresses != null)
            return addresses.get(0);
        else
            return null;
    }

    /* Get list of address candidates given user string input.
    *  Note: getFromLocationName function bounds the search within the contiguous 48 states. This
     * Will be altered when we launch worldwide! */
    public List<Address> resultListFromUserInput(String input) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addressList = geoCoder.getFromLocationName(input, 5, 25, -125, 50, -63);

            if (addressList.size() > 0)
                return addressList;
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void connectErrorAlert(String error) {
        DialogFragment errorDialog = new ErrorAlert();
        Bundle args = new Bundle();
        args.putString("errorStr", error);
        errorDialog.setArguments(args);
        errorDialog.show(getSupportFragmentManager(), "tag");
        ErrorAlertPopped = true;
    }
}

