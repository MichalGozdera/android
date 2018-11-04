package com.example.cokeman.myapplication;

import android.location.LocationManager;
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


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Button gpsButton = (Button) findViewById(R.id.getGps);
        final TextView textView = (TextView) findViewById(R.id.coordinates);
        final MapView osm = (MapView) findViewById(R.id.osm);


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

        final MapController mc = (MapController) osm.getController();
        final GeoPoint dorotka = new GeoPoint(51.0005, 21.7860);
        gpsButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                osm.setVisibility(View.VISIBLE);
                textView.setText("Dorotka jest tutaj:");
                mc.animateTo(dorotka);
                mc.setZoom(16);
                osm.setTilesScaledToDpi(true);
            }
        });

        // google.setVisibility(View.INVISIBLE);


    }
}
