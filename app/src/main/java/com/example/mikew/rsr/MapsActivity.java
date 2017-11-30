package com.example.mikew.rsr;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private FusedLocationProviderClient fused_location_client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.maps_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        final Button popup_button = (Button) findViewById(R.id.call_button);
        popup_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(R.id.black_background).setVisibility(View.VISIBLE);
                findViewById(R.id.call_window).setVisibility(View.VISIBLE);
                findViewById(R.id.call_button).setVisibility(View.GONE);
            }
        });

        final Button cancel_button = (Button) findViewById(R.id.popup_cancel_button);
        cancel_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(R.id.black_background).setVisibility(View.GONE);
                findViewById(R.id.call_window).setVisibility(View.GONE);
                findViewById(R.id.call_button).setVisibility(View.VISIBLE);
            }
        });

        final Button call_button = (Button) findViewById(R.id.popup_call_button);
        call_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:123456789"));
                startActivity(callIntent);
            }
        });
        SupportMapFragment map_fragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map_fragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        if (!isLocationEnabled(this)) {
            locationAlert();
        }
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menu_inflater = getMenuInflater();
        menu_inflater.inflate(R.menu.return_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 10: {
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap google_map) {
        map = google_map;
        fused_location_client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
        }
        fused_location_client.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                if (location != null) {
                    // Logic to handle location object
                    map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {

                            View view = getLayoutInflater().inflate(R.layout.cutom_info_box, null);
                            TextView info_box_location_info = (TextView) view.findViewById(R.id.loationinfo);
                            LatLng latitude_longitude = marker.getPosition();
                            Geocoder geo_coder = new Geocoder(MapsActivity.this);
                            List<Address> list = null;
                            try {
                                list = geo_coder.getFromLocation(latitude_longitude.latitude, latitude_longitude.longitude, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Address address = list.get(0);
                            info_box_location_info.setText(address.getAddressLine(0));
                            return view;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            return null;
                        }
                    });
                    findViewById(R.id.waitSign).setVisibility(View.GONE);
                    LatLng current_location = new LatLng(location.getLatitude(), location.getLongitude());
                    MarkerOptions current_location_marker = new MarkerOptions()
                            .position(current_location)
                            .title("Uw locatie:")
                            .snippet("")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker));
                    map.addMarker(current_location_marker).showInfoWindow();
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f));
                }
                }
            });
    }


    public static boolean isLocationEnabled(Context context) {
        int location_mode = 0;
        String location_providers;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                location_mode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return location_mode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            location_providers = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(location_providers);
        }
    }
    public void locationAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setMessage(R.string.dialog_message)
            .setCancelable(false)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        AlertDialog alert = builder.create();
        alert.setTitle(R.string.dialog_title);
        alert.show();
    }
}