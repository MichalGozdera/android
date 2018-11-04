package com.example.cokeman.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;

import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;


public class MainActivity extends AppCompatActivity implements LocationListener {
    Location gpsLocation;
    final GeoPoint misio = new GeoPoint(0, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button findDorotka = (Button) findViewById(R.id.getGps);
        final Button gps = (Button) findViewById(R.id.gps);
        final MapView osm = (MapView) findViewById(R.id.osm);
        final MapController mc = (MapController) osm.getController();
        final TextView textView = (TextView) findViewById(R.id.coordinates);

        final LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);


        final float scale = this.getResources().getDisplayMetrics().density;
        int height_ = (int) (300 * scale + 0.5f);
        int width_ = (int) (300 * scale + 0.5f);
        ViewGroup.LayoutParams params = osm.getLayoutParams();
        params.height = height_;
        params.width = width_;


        osm.setLayoutParams(params);
        osm.setTileSource(TileSourceFactory.MAPNIK);
        // osm.setVisibility(View.INVISIBLE);
        osm.setBuiltInZoomControls(true);
        osm.setMultiTouchControls(true);


        final GeoPoint dorotka = new GeoPoint(51.0005, 21.7860);
        findDorotka.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                osm.setVisibility(View.VISIBLE);
                textView.setText("Dorotka jest tutaj:");
                mc.animateTo(dorotka);
                mc.setZoom(16);
                osm.setTilesScaledToDpi(true);
            }
        });


        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, this);
        gps.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                osm.setVisibility(View.VISIBLE);
                textView.setText("Misio jest tutaj:");
                misio.setLatitude(gpsLocation.getLatitude());
                misio.setLongitude(gpsLocation.getLongitude());
                mc.animateTo(misio);
                mc.setZoom(16);
                osm.setTilesScaledToDpi(true);
            }
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        String provider = location.getProvider();
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        float accuracy = location.getAccuracy();
        long time = location.getTime();
        misio.setLatitude(lat);
        misio.setLongitude(lng);
        MapView osm = (MapView) findViewById(R.id.osm);
        MapController mc = (MapController) osm.getController();
        mc.animateTo(misio);

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
}
