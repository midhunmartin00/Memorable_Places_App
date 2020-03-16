package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng[] latLngs;
    String[] names;
    Geocoder geocoder;

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 5, locationListener);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public void markLocation(Location location,String title){
        LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(sydney).title(title)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,12));
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        mMap.clear();
        mMap.setOnMapLongClickListener(this);
        Intent intent=getIntent();
        int position=intent.getIntExtra("position",0);
        geocoder=new Geocoder(getApplicationContext(),Locale.getDefault());
        locationManager=(LocationManager) this.getSystemService(LOCATION_SERVICE);
        if(position==0) {
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i("location", location.toString());
                    markLocation(location, "Your Location");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            else {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 5, locationListener);
                Location previousLocation =locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                markLocation(previousLocation,"Your location");
            }
        }
        else{
            Location location1=new Location(LocationManager.NETWORK_PROVIDER);
            location1.setLatitude(MainActivity.locations.get(position).latitude);
            location1.setLongitude(MainActivity.locations.get(position).longitude);
            Log.i("new", location1.toString());
            try {
                Log.i("new", geocoder.getFromLocation(location1.getLatitude(),location1.getLongitude(),1).get(0).getFeatureName());
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("error", "onMapReady: ");
            }
            markLocation(location1,MainActivity.places.get(position));
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions markerOptions=new MarkerOptions();
                markerOptions.position(latLng);
                try {
                    List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                    Log.i("location", addressList.toString());
                    String add="";
                    if(addressList!=null && addressList.size()>0){
                        if(addressList.get(0).getThoroughfare()!=null){
                            if(addressList.get(0).getSubThoroughfare()!=null)
                                add+=addressList.get(0).getSubThoroughfare()+" ";
                            add+=addressList.get(0).getThoroughfare();
                        }
                    }
//        Log.i("location", addressList.toString());
                    if(add.equals("")){
                        SimpleDateFormat sdf=new SimpleDateFormat("HH:mm yyyy-MM-dd");
                        add+=sdf.format(new Date());
                    }
                    markerOptions.title(add);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.addMarker(markerOptions).setTag(0);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker.getTag().toString().equals("0")) {
                    final EditText editText = new EditText(MapsActivity.this);
                    FrameLayout frameLayout = new FrameLayout(MapsActivity.this);
                    FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                    lp.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                    lp.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
                    editText.setLayoutParams(lp);
                    editText.setText(marker.getTitle().toString());
                    frameLayout.addView(editText);
                    new AlertDialog.Builder(MapsActivity.this)
                            .setIcon(android.R.drawable.sym_def_app_icon)
                            .setTitle("Are you sure")
                            .setMessage("Do you want to add this item to your Memorable Places\n\nEnter a name for the memorable place")
                            .setView(frameLayout)
                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MainActivity.locations.add(marker.getPosition());
                                    MainActivity.places.add(editText.getText().toString());
                                    Toast.makeText(MapsActivity.this, "Location Saved", Toast.LENGTH_SHORT).show();
                                    MainActivity.adapter.notifyDataSetChanged();

                                    SharedPreferences sharedPreferences = MapsActivity.this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
                                    try {
                                        ArrayList<String> latitudes = new ArrayList<String>();
                                        ArrayList<String> longitudes = new ArrayList<String>();
                                        for (LatLng i : MainActivity.locations) {
                                            latitudes.add(Double.toString(i.latitude));
                                            longitudes.add(Double.toString(i.longitude));
                                        }
                                        sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
                                        sharedPreferences.edit().putString("lats", ObjectSerializer.serialize(latitudes)).apply();
                                        sharedPreferences.edit().putString("longs", ObjectSerializer.serialize(longitudes)).apply();
                                        marker.setTag(1);
                                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            return true;
        }
    });
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(latLng);
        Log.i("new", "onMapLongClick: "+latLng.toString());
        String add="";
        try {
            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(addressList!=null && addressList.size()>0){
                if(addressList.get(0).getThoroughfare()!=null){
                    if(addressList.get(0).getSubThoroughfare()!=null)
                        add+=addressList.get(0).getSubThoroughfare()+" ";
                    add+=addressList.get(0).getThoroughfare();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Log.i("location", addressList.toString());
        if(add.equals("")){
            SimpleDateFormat sdf=new SimpleDateFormat("HH:mm yyyy-MM-dd");
            add+=sdf.format(new Date());
        }

        markerOptions.title(add);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(markerOptions).setTag(1);
        MainActivity.locations.add(latLng);
        MainActivity.places.add(add);
        Toast.makeText(this, "Location Saved", Toast.LENGTH_SHORT).show();
        MainActivity.adapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitudes=new ArrayList<String>();
            ArrayList<String> longitudes=new ArrayList<String>();
            for(LatLng i:MainActivity.locations){
                latitudes.add(Double.toString(i.latitude));
                longitudes.add(Double.toString(i.longitude));
            }
            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("lats",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longs",ObjectSerializer.serialize(longitudes)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
