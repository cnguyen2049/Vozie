package com.example.android.vozie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class CustomMarkerInfoAdapter implements GoogleMap.InfoWindowAdapter, View.OnClickListener {
    MapsActivity mainActivity;
    LayoutInflater layoutInflater;

    public CustomMarkerInfoAdapter(LayoutInflater inputInflater, MapsActivity inActivity) {
        mainActivity = inActivity;
        layoutInflater = inputInflater;
    }

    @Override
    public View getInfoWindow(Marker arg0) {
        return null;
    }

    @Override
    public View getInfoContents(Marker arg0) {
        View v = layoutInflater.inflate(R.layout.custom_marker, null);

        Button yesButton = (Button) v.findViewById(R.id.yes_button);
        yesButton.setOnClickListener(this);

        TextView titleView = (TextView) v.findViewById(R.id.title);
        TextView snippetView = (TextView) v.findViewById(R.id.snippet);

        if (arg0.getTitle() != null && arg0.getSnippet() != null) {
            titleView.setText(arg0.getTitle());
            snippetView.setText(arg0.getSnippet());
        }

        return v;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.yes_button:
                mainActivity.yesClick();
                break;
        }
    }


}
