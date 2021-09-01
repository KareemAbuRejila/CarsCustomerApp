package com.codeshot.carscustomerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{
    private View view;

    public CustomInfoWindow(Context context){
        view= LayoutInflater.from(context).inflate(R.layout.custom_rider_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView tvPickUpTitle=view.findViewById(R.id.tvPickUpInto);
        tvPickUpTitle.setText(marker.getTitle());

        TextView tvPickUpSnippet=view.findViewById(R.id.tvPickUpSnippet);
        tvPickUpSnippet.setText(marker.getSnippet());

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
