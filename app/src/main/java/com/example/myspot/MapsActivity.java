package com.example.myspot;

import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.IDNA;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    String myParent;
    private GoogleMap mMap;
    boolean isVisitor=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        //here we can know from which activity we got here
        Intent intent = getIntent();
        myParent = intent.getStringExtra("parent");

        isVisitor=getIntent().getBooleanExtra("isVisitor",false);

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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng technion = new LatLng(32.777828, 35.023777);
        //mMap.addMarker(new MarkerOptions().position(technion).title("Marker in Sydney")).showInfoWindow();

        //buildings:red
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773736, 35.028497)).title("450")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773790, 35.028993)).title("451")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773497, 35.027971)).title("452")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773421, 35.028325)).title("453")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773405, 35.028690)).title("454")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773207, 35.027685)).title("455")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773125, 35.028085)).title("456")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773125, 35.028462)).title("457")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.772856, 35.027763)).title("458")).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.772785, 35.028173)).title("459")).showInfoWindow();


        //parkingAreas:green
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773963, 35.028355)).title("1").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773731, 35.027791)).title("2").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773604, 35.02851)).title("2Licensed").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.774047, 35.027024)).title("3").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773323, 35.02803)).title("3Licensed").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.773412, 35.02646)).title("4").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.772972, 35.028004)).title("4Licensed").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.774989, 35.028372)).title("Peripheral plot").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mMap.addMarker(new MarkerOptions().position(new LatLng(32.774772, 35.026951)).title("inFrontOfBioInformatics").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();


        //setting up the initial zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(technion,16.0f));
        //set the type of the map
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //mMap.setIndoorEnabled(true);
        // setting bounds to the map
        LatLng north = new LatLng(32.782449, 35.023263);
        LatLng east = new LatLng(32.774511, 35.029008);
        LatLng west = new LatLng(32.778439, 35.015010);
        LatLng south = new LatLng(32.772602, 35.020895);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(north);
        builder.include(east);
        builder.include(west);
        builder.include(south);

       /* LatLngBounds tmpBounds = builder.build();
        LatLng center = tmpBounds.getCenter();
        LatLng northEast = move(center, 0, 0);
        LatLng southWest = move(center, 0, 0);
        builder.include(southWest);
        builder.include(northEast);*/
        //tmpBounds = builder.build();
        LatLngBounds tmpBounds = builder.build();
        mMap.setLatLngBoundsForCameraTarget(tmpBounds);

        // mMap.setLatLngBoundsForCameraTarget(LatLngBounds.builder().build());


        mMap.setBuildingsEnabled(true);

    }
    private static final double EARTHRADIUS = 6366198;
    /**
     * Create a new LatLng which lies toNorth meters north and toEast meters
     * east of startLL
     */

    public void changeMapStyle(View view){
        if(mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else{
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        }
    }

    public void goBack(View view){
        if(myParent.equals("ReachSpot")){
            Intent intent = new Intent(this,ReachSpot.class);
            intent.putExtra("isVisitor",isVisitor);
            startActivity(intent);
        }else if(myParent.equals("InfoOfSpotAc")){
            Intent intent = new Intent(this,InfoOfSpotAc.class);
            intent.putExtra("isVisitor",isVisitor);
            startActivity(intent);
        }else{
            Intent intent = new Intent(this,LabelHomePageActivity.class);
            startActivity(intent);
        }
    }
    public void help(View view){

        Dialog dialog=new Dialog(MapsActivity.this);
        dialog.setContentView(R.layout.activity_map_instructions);
        dialog.setCancelable(true);

        dialog.show();

    }

}
