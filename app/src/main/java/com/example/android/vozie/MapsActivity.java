package com.example.android.vozie;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.graphics.Point;
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
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.List;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
        ConnectionErrorAlert.NoticeDialogListener, ServiceErrorAlert.NoticeDialogListener{

    private final int MAX_RESULTS = 50;
    private final int SEARCH_RADIUS = 30;  // Radius in miles
    private final String[] drawerItems = {"Account", "Payment", "History", "Notifications",
            "Settings", "About"};

    private GoogleMap mainMap;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;
    private LocationRequest mLocationRequest;
    private LocationManager lm;
    private Marker locMarker;
    private CustomMarkerInfoAdapter infoAdapter;
    private Button yesButton, noButton, toButton, fromButton, rideButton, setAsFromButton, setAsToButton;
    private ImageView menuImage;
    private LinearLayout mainLayout, searchLayout, setButtonLayout;
    private RelativeLayout searchRelativeLayout;
    private EditText searchModeSearchText;
    private AutoCompleteTextView searchText;
    private ListView resultList, drawerList;
    private TextView topRect, bottomRect, leftRect, rightRect, mapModeAddressText, mapModeDistanceText;
    private DialogFragment connectionErrorDialog, serviceErrorDialog;
    private SupportMapFragment mapFragment;
    private DrawerLayout drawerLayout;

    private Address currentSearchResults[];
    private Address fromLoc, toLoc, selectedLoc;
    private String currentText, searchModeCurrentText;

    private Thread checkConnectionThread, adapterThread;

    private boolean service_enabled, gps_enabled, network_enabled, search_mode, map_mode;

    /*-------------------------------*/
    /* MapsActivity Callback Methods */
    /*-------------------------------*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initializeErrorAlertDialogs();
        search_mode = false;
        map_mode = false;
        currentSearchResults = new Address[MAX_RESULTS];
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mainMap == null) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            try {
                googleMap.setMyLocationEnabled(false);
            } catch (SecurityException e) {};

            mainMap = googleMap;

            if (mainMap != null) {
                buildGoogleApiClient();
                mGoogleApiClient.connect();

                initializeInterface();
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        // Google Service has connected
        service_enabled = true;

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {}

        initializeConnectionChecking();

        if (service_enabled && (gps_enabled || network_enabled)) {
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } catch (SecurityException e) { }
        }
        else {
            if (!service_enabled)
                serviceError();
            if (!gps_enabled || !network_enabled)
                connectionError();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_SERVICE_DISCONNECTED)
            serviceError();
        else if (cause == CAUSE_NETWORK_LOST)
            connectionError();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (fromLoc == null && !map_mode) {
            if (currentLocation == null)
                moveToLocation(location, true);
            else
                moveToLocation(location, false);

            currentLocation = location;
        }
    }

    @Override
    public void connectionRetryClick(DialogFragment dialog) {
        connectionErrorDialog.dismiss();
    }

    @Override
    public void connectionExitClick(DialogFragment dialog) {
        connectionErrorDialog.dismiss();
        exitApplication();
    }

    @Override
    public void serviceRetryClick(DialogFragment dialog) {
        serviceErrorDialog.dismiss();
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public void serviceExitClick(DialogFragment dialog) {
        serviceErrorDialog.dismiss();
        exitApplication();
    }

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK) {
            if (map_mode)
                deactivateMapMode();
            else if (search_mode)
                deactivateSearchMode();
            else
                return super.onKeyDown(keycode, event);
        }

        return false;
    }

    /*--------------------------*/
    /* Initialization Functions */
    /*--------------------------*/
    public void initializeInterface() {
        initializeMap();
        initializeInfoWindow();
        initializeSearchBox();
        initializeFromToButtons();
        initializeSetFromToButtons();
        initializeRideButton();
        initializeBorderRects();
        initializeSearchModeSearchBox();
        initializeDrawer();
        initializeMenuBar();

        mainLayout = (LinearLayout) findViewById(R.id.main_linear_layout);
        searchLayout = (LinearLayout) findViewById(R.id.search_layout);
        setButtonLayout = (LinearLayout) findViewById(R.id.set_button_layout);
        searchRelativeLayout = (RelativeLayout) findViewById(R.id.search_relative_layout);

        mapModeAddressText = (TextView) findViewById(R.id.map_mode_address_text);
        mapModeDistanceText = (TextView) findViewById(R.id.map_mode_distance_text);
    }

    public void initializeDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerItems));
    }

    public void initializeMenuBar() {
        menuImage = (ImageView) findViewById(R.id.menu_button);

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(drawerList);
            }
        });
    }

    public void initializeErrorAlertDialogs() {
        connectionErrorDialog = new ConnectionErrorAlert();
        serviceErrorDialog = new ServiceErrorAlert();
    }

    public void initializeBorderRects() {
        topRect = (TextView) findViewById(R.id.top_rect);
        bottomRect = (TextView) findViewById(R.id.bot_rect);
        leftRect = (TextView) findViewById(R.id.left_rect);
        rightRect = (TextView) findViewById(R.id.right_rect);
    }

    public void initializeMarkerMovementListener() {
        new Thread(new Runnable() {
            public void run() {
                View v = findViewById(R.id.map);

                while(true) {
                    v.post(new Runnable() {
                        public void run() {
                            Point p = getMarkerScreenCoordinates();
                            setYesNoCoordinates(p);
                        }
                    });
                    try {
                        Thread.currentThread().sleep(1);
                    } catch (InterruptedException e) { }
                }
            }
        }).start();
    }

    public void initializeMap() {
        mainMap.setOnMapClickListener(
                new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng l) {
                        searchText.setCursorVisible(false);
                        searchText.setHint(R.string.search_hint);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
                    }
                }
        );
    }

    public void initializeInfoWindow() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        infoAdapter = new CustomMarkerInfoAdapter(inflater, this);
        mainMap.setInfoWindowAdapter(infoAdapter);

        initializeMarkerMovementListener();
    }

    public void initializeSearchModeSearchBox() {
        resultList = (ListView) findViewById(R.id.search_list);
        searchModeSearchText = (EditText) findViewById(R.id.search_text_main);
        Drawable search = ContextCompat.getDrawable(this, R.drawable.search);

        search.setBounds(0, 0, 100, 100);
        searchModeSearchText.setCompoundDrawables(search, null, null, null);
        searchModeSearchText.setCursorVisible(false);
        searchModeSearchText.setFocusable(true);
        searchModeSearchText.setClickable(true);
        searchModeSearchText.setEnabled(true);

        resultList.setOnItemClickListener (
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
                        selectedLoc = currentSearchResults[position];
                        activateMapMode();
                    }
                }
        );

        searchModeSearchText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    searchModeSearchText.setCursorVisible(true);
                    searchModeSearchText.setHint("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchModeSearchText, InputMethodManager.SHOW_IMPLICIT);
                    return true;
                }
                return false;
            }
        });

        searchModeSearchText.addTextChangedListener(new TextWatcher() {
            Thread resultAdapterThread = null;

            @Override
            public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
                searchModeCurrentText = charsequence.toString();

                if (resultAdapterThread != null)
                    if (resultAdapterThread.isAlive())
                        resultAdapterThread.interrupt();

                resultAdapterThread = new Thread((new Runnable() {
                    String[] strArr;

                    @Override
                    public void run() {
                        if (map_mode)
                            deactivateMapMode();
                        List<Address> addresses = resultListFromUserInput(searchModeCurrentText);

                        if (addresses != null) {
                            strArr = new String[addresses.size()];
                            for (int l = 0; l < addresses.size(); l++) {
                                Address indAddress = addresses.get(l);
                                currentSearchResults[l] = indAddress;

                                strArr[l] = indAddress.getAddressLine(0) + " "
                                        + indAddress.getAddressLine(1) + " "
                                        + indAddress.getAddressLine(2);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (strArr != null) {
                                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this,
                                            android.R.layout.simple_list_item_1, strArr);
                                    resultList.setAdapter(adapter);
                                }
                            }
                        });
                    }
                }));

                resultAdapterThread.start();
            }

            @Override
            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void initializeSearchBox() {
        searchText = (AutoCompleteTextView) findViewById(R.id.search_text);
        Drawable search = ContextCompat.getDrawable(this, R.drawable.search);

        search.setBounds(0, 0, 100, 100);
        searchText.setCompoundDrawables(search, null, null, null);
        searchText.setCursorVisible(false);
        searchText.setFocusable(true);
        searchText.setClickable(true);

        searchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                selectedLoc = currentSearchResults[position];
                activateSearchMode();
                activateMapMode();
            }
        });

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

                adapterThread = new Thread((new Runnable() {
                    public void run() {
                        ArrayAdapter<String> adapter;
                        List<Address> addresses = resultListFromUserInput(currentText);
                        String tText = currentText;

                        if (addresses != null) {
                            String[] array = new String[addresses.size()];
                            for (int l = 0; l < addresses.size(); l++) {
                                Address indAddress = addresses.get(l);
                                currentSearchResults[l] = indAddress;
                                array[l] = indAddress.getAddressLine(0) + " "
                                        + indAddress.getAddressLine(1) + " "
                                        + indAddress.getAddressLine(2);
                            }

                            adapter = new ArrayAdapter<String>(MapsActivity.this,
                                    android.R.layout.simple_list_item_1, array);

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

            @Override
            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void initializeRideButton() {
        rideButton = (Button) findViewById(R.id.ride_button);
    }

    public void initializeYesNoButtons() {
        yesButton = (Button) findViewById(R.id.yes_button);
        noButton = (Button) findViewById(R.id.no_button);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yesClick();
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noClick();
            }
        });
    }

    public void initializeFromToButtons() {
        fromButton = (Button) findViewById(R.id.from_button);
        toButton = (Button) findViewById(R.id.to_button);

        fromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromClick();
            }
        });

        toButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toClick();
            }
        });
    }

    public void initializeSetFromToButtons() {
        setAsFromButton = (Button) findViewById(R.id.set_as_from_search);
        setAsToButton = (Button) findViewById(R.id.set_as_to_search);

        setAsFromButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map_mode)
                    setAsFromClick();
            }
        });

        setAsToButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map_mode)
                    setAsToClick();
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

    private void initializeConnectionChecking() {
        checkConnectionThread = new Thread(new Runnable() {
            public void run() {
                while (connectionErrorDialog.isHidden() && serviceErrorDialog.isHidden()) {
                    try {
                        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    } catch (Exception ex) {}

                    try {
                        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                    } catch (Exception ex) {}

                    if (!service_enabled)
                        serviceError();
                    else if (!gps_enabled || !network_enabled)
                        connectionError();

                    try {
                        Thread.currentThread().sleep(5000);
                    } catch (InterruptedException e) { }
                }
            }
        });

        checkConnectionThread.start();
    }


    /*----------------------*/
    /* Map Helper Functions */
    /*----------------------*/

    /* Movement to desired address */
    public void moveToLocation(Location input, boolean animated) {
        Double lat = (double) (input.getLatitude());
        Double lon = (double) (input.getLongitude());

        final LatLng location = new LatLng(lat, lon);

        setLocMarker(location);

        mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));

        if (animated)
            mainMap.animateCamera(CameraUpdateFactory.zoomTo(16), 4000, null);
    }

    /* Movement to desired address with Lat+Long data */
    public void moveToLocation(LatLng loc, boolean animated) {
        setLocMarker(loc);

        mainMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 16));

        if (animated)
            mainMap.animateCamera(CameraUpdateFactory.zoomTo(16), 4000, null);
    }


    /* Resets locMarker to specified LatLng */
    public void setLocMarker(LatLng input) {
        String title, snippet;

        if (map_mode || search_mode) {
            title = "Location Selected";
            snippet = getAddressFromLocation(input).getAddressLine(0);
            snippet += ", " + getAddressFromLocation(input).getAddressLine(1);
        }
        else {
            title = "Set Pickup Location?";
            snippet = getAddressFromLocation(input).getAddressLine(0);
            snippet += ", " + getAddressFromLocation(input).getAddressLine(1);
            snippet += ", " + getAddressFromLocation(input).getAddressLine(2);
        }

        if (locMarker != null)
            locMarker.remove();

        locMarker = mainMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                .position(input)
                .title(title)
                .snippet(snippet));

        if (mainMap != null) {
            // Create custom marker from custom_marker.xml
            LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            infoAdapter = new CustomMarkerInfoAdapter(inflater, this);
            mainMap.setInfoWindowAdapter(infoAdapter);
        }

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
            double lowerLeftLat = currentLocation.getLatitude()
                    - getRadialDegreeDistance("Lat");
            double lowerLeftLong = currentLocation.getLongitude()
                    - getRadialDegreeDistance("Long");
            double upperRightLat = currentLocation.getLatitude()
                    + getRadialDegreeDistance("Lat");
            double upperRightLong = currentLocation.getLongitude()
                    + getRadialDegreeDistance("Long");

            List<Address> addressList = geoCoder.getFromLocationName(input, MAX_RESULTS,
                    lowerLeftLat, lowerLeftLong, upperRightLat, upperRightLong);

            if (addressList.size() > 0) {
                if (addressList.size() > 1)
                    addressList = sortAddressesByDistance(addressList);
                return addressList;
            }
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getRadialDegreeDistance(String param) {
        switch (param) {
            case "Lat":
                return (1 / 110.54) * SEARCH_RADIUS * 0.621371;
            case "Long":
                return (1 / (111.320 * Math.cos(currentLocation.getLatitude())))
                        * SEARCH_RADIUS * 0.621371;
            default:
                return 0;
        }
    }

    public List<Address> sortAddressesByDistance(List<Address> input) {
        int array[] = new int[input.size()];
        List<Address> returnList;
        Location addrLoc = new Location("i-addr");

        // Fill distance array with data
        for (int i = 0; i < input.size(); i++) {
            addrLoc.setLatitude(input.get(i).getLatitude());
            addrLoc.setLongitude(input.get(i).getLongitude());
            array[i] = Math.round(currentLocation.distanceTo(addrLoc));
        }

        Quicksort sorter = new Quicksort();
        return sorter.sort(array, input);
    }



    public void setFromLocation(Address inAddress, LatLng loc) {
        fromLoc = inAddress;
        fromButton.setText(inAddress.getAddressLine(0));
        fromButton.setBackgroundColor(Color.parseColor("#70000080"));

        moveToLocation(loc, false);

        locMarker.setTitle("Pickup Location Set!");
        locMarker.setSnippet(fromLoc.getAddressLine(0) + " " + fromLoc.getAddressLine(1));
        locMarker.hideInfoWindow();
        locMarker.showInfoWindow();
    }

    public void setToLocation(Address inAddress, LatLng loc) {
        toLoc = inAddress;
        toButton.setText(inAddress.getAddressLine(0));
        toButton.setBackgroundColor(Color.parseColor("#70006400"));

        moveToLocation(loc, false);
    }

    public void serviceError() {
        service_enabled = false;
        serviceErrorDialog.show(getSupportFragmentManager(), "service_error");
    }

    public void connectionError() {
        service_enabled = false;
        serviceErrorDialog.show(getSupportFragmentManager(), "service_error");
    }

    public void exitApplication() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        System.exit(0);
    }

    public void setYesNoCoordinates(Point p) {
        if (p != null && locMarker != null) {
            if (fromLoc == null) {
                if (yesButton != null && noButton != null) {
                    int width = infoAdapter.getWidth();

                    int leftYes = p.x - width / 2;
                    int topYes = p.y - 140;
                    int leftNo = p.x + 70;
                    int topNo = p.y - 140;

                    FrameLayout.LayoutParams yesParams = (FrameLayout.LayoutParams) yesButton.getLayoutParams();
                    yesParams.width = (width / 2) - 70;
                    yesParams.height = 140;
                    yesParams.setMargins(leftYes, topYes, 0, 0);
                    yesButton.setLayoutParams(yesParams);

                    FrameLayout.LayoutParams noParams = (FrameLayout.LayoutParams) noButton.getLayoutParams();
                    noParams.width = (width / 2) - 70;
                    noParams.height = 140;
                    noParams.setMargins(leftNo, topNo, 0, 0);
                    noButton.setLayoutParams(noParams);

                    if (fromLoc == null && !search_mode) {
                        yesButton.setVisibility(View.VISIBLE);
                        noButton.setVisibility(View.VISIBLE);

                    }

                    if (search_mode) {
                        yesButton.setVisibility(View.INVISIBLE);
                        noButton.setVisibility(View.INVISIBLE);
                    }
                }
                else
                    initializeYesNoButtons();
            }
            else {
                yesButton.setVisibility(View.INVISIBLE);
                noButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    public Point getMarkerScreenCoordinates() {
        if (mainMap != null && locMarker != null) {
            Projection projection = mainMap.getProjection();
            return projection.toScreenLocation(locMarker.getPosition());
        }
        else
            return null;
    }

    public void activateSearchMode() {
        search_mode = true;
        mainMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mainLayout.setVisibility(View.INVISIBLE);
        searchLayout.setVisibility(View.VISIBLE);
        setButtonLayout.setVisibility(View.VISIBLE);
        searchRelativeLayout.setVisibility(View.VISIBLE);

        UiSettings mySettings = mainMap.getUiSettings();
        mySettings.setAllGesturesEnabled(false);
    }

    public void deactivateSearchMode() {
        search_mode = false;
        mainMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mainLayout.setVisibility(View.VISIBLE);
        searchLayout.setVisibility(View.INVISIBLE);
        setButtonLayout.setVisibility(View.INVISIBLE);
        searchRelativeLayout.setVisibility(View.INVISIBLE);

        UiSettings mySettings = mainMap.getUiSettings();
        mySettings.setAllGesturesEnabled(true);
    }

    public void activateMapMode() {
        LatLng selectedLocLatLng = new LatLng(selectedLoc.getLatitude(), selectedLoc.getLongitude());

        Location selectedLocation = new Location("Selected");
        selectedLocation.setLatitude(selectedLoc.getLatitude());
        selectedLocation.setLongitude(selectedLoc.getLongitude());

        String mapModeAddressString = selectedLoc.getAddressLine(0) + " "
                + selectedLoc.getAddressLine(1);
        String mapModeDistanceString = Math.round(currentLocation.distanceTo(selectedLocation) * 0.000621371F)
                + " miles from current location";

        map_mode = true;
        mainMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        moveToLocation(selectedLocLatLng, true);

        topRect.setVisibility(View.VISIBLE);
        bottomRect.setVisibility(View.VISIBLE);
        leftRect.setVisibility(View.VISIBLE);
        rightRect.setVisibility(View.VISIBLE);
        mapModeAddressText.setVisibility(View.VISIBLE);
        mapModeDistanceText.setVisibility(View.VISIBLE);

        mapModeAddressText.setText(mapModeAddressString);
        mapModeDistanceText.setText(mapModeDistanceString);

        searchRelativeLayout.setBackgroundColor(Color.TRANSPARENT);
        resultList.setVisibility(View.INVISIBLE);

        UiSettings mySettings = mainMap.getUiSettings();
        mySettings.setAllGesturesEnabled(false);

        setAsFromButton.setBackground(getResources().getDrawable(R.drawable.ride_button_bg_handler));
        setAsToButton.setBackground(getResources().getDrawable(R.drawable.ride_button_bg_handler));
    }

    public void deactivateMapMode() {
        map_mode = false;
        mainMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        topRect.setVisibility(View.INVISIBLE);
        bottomRect.setVisibility(View.INVISIBLE);
        leftRect.setVisibility(View.INVISIBLE);
        rightRect.setVisibility(View.INVISIBLE);
        mapModeAddressText.setVisibility(View.INVISIBLE);
        mapModeDistanceText.setVisibility(View.INVISIBLE);

        searchRelativeLayout.setBackgroundColor(Color.WHITE);
        resultList.setVisibility(View.VISIBLE);

        moveToLocation(currentLocation, false);

        UiSettings mySettings = mainMap.getUiSettings();
        mySettings.setAllGesturesEnabled(true);

        setAsFromButton.setBackground(getResources().getDrawable(R.drawable.ride_button_disabled));
        setAsToButton.setBackground(getResources().getDrawable(R.drawable.ride_button_disabled));
    }
    /*---------------------------*/
    /* Button Handling Functions */
    /*---------------------------*/
    public void yesClick() {
        LatLng current = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        setFromLocation(getAddressFromLocation(current), current);
    }

    public void noClick() {
        activateSearchMode();
    }

    public void fromClick() {
        activateSearchMode();
    }

    public void toClick() { activateSearchMode(); }

    public void setAsFromClick() {
        LatLng current = new LatLng(selectedLoc.getLatitude(), selectedLoc.getLongitude());
        setFromLocation(getAddressFromLocation(current), current);
        deactivateMapMode();
        deactivateSearchMode();
        searchModeSearchText.setText("");
    }

    public void setAsToClick() {
        LatLng current = new LatLng(selectedLoc.getLatitude(), selectedLoc.getLongitude());
        setToLocation(getAddressFromLocation(current), current);
        deactivateMapMode();
        deactivateSearchMode();
        searchModeSearchText.setText("");
    }

    public class Quicksort  {
        private int[] numbers;
        private int number;
        private List<Address> list;

        public List<Address> sort(int[] values, List<Address> addrList) {
            if (values == null || values.length == 0){
                return null;
            }

            this.list = addrList;
            this.numbers = values;
            number = values.length;
            quicksort(0, number - 1);

            return list;
        }

        private void quicksort(int low, int high) {
            int i = low, j = high;

            int pivot = numbers[low + (high-low)/2];

            while (i <= j) {
                while (numbers[i] < pivot) {
                    i++;
                }
                while (numbers[j] > pivot) {
                    j--;
                }

                if (i <= j) {
                    exchange(i, j);
                    i++;
                    j--;
                }
            }

            if (low < j)
                quicksort(low, j);
            if (i < high)
                quicksort(i, high);
        }

        private void exchange(int i, int j) {
            Address tAddr = list.get(i);
            int temp = numbers[i];

            list.set(i, list.get(j));
            numbers[i] = numbers[j];

            list.set(j, tAddr);
            numbers[j] = temp;
        }
    }
}

