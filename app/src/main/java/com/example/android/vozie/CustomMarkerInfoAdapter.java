package com.example.android.vozie;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class CustomMarkerInfoAdapter implements GoogleMap.InfoWindowAdapter {
    MapsActivity mainActivity;
    LayoutInflater layoutInflater;
    View v;

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
        v = layoutInflater.inflate(R.layout.custom_marker, null);

        TextView titleView = (TextView) v.findViewById(R.id.title);
        TextView snippetView = (TextView) v.findViewById(R.id.snippet);

        if (arg0.getTitle() != null && arg0.getSnippet() != null) {
            titleView.setText(arg0.getTitle());
            snippetView.setText(arg0.getSnippet());
        }

        return v;
    }

    public int getWidth() {
        if (v != null)
            return v.getWidth();
        else
            return 0;
    }
}
