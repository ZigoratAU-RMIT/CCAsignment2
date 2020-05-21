package com.example.uberrequest;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MapsActivityAnalyze extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Error";
    private GoogleMap mMap;
        private FirebaseFirestore db;
        private List<LatLng> analyseList = new ArrayList<>();
        private  int locCounter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_analyze);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        LatLng loc = new LatLng(40.730610,-73.935242);////-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(loc)
                .title("Marker in New York"));
        mMap.addCircle(new CircleOptions()
                .center(loc)
                .radius(900.0)
                .strokeWidth(3f)
                .strokeColor(Color.BLUE)
                .fillColor(Color.argb(70,255,125,65)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,11.0f));;
        showAnalyse();
        showArea();

        LatLng MELBOURNE = new LatLng(-37.813, 144.962);
        Marker melbourne = mMap.addMarker(new MarkerOptions()
                .position(MELBOURNE)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
    }

    private void showArea() {
        locCounter = 1;
        db = FirebaseFirestore.getInstance();
        db.collection("locations").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> list = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        LatLng item = new LatLng(Double.parseDouble(document.get("lang").toString()),
                                Double.parseDouble(document.get("lat").toString()));
                        analyseList.add(item);
                        switch (locCounter){
                            case 1:
                                mMap.addMarker(new MarkerOptions().position(item)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                                mMap.addCircle(new CircleOptions()
                                        .center(item)
                                        .radius(800.0)
                                        .strokeWidth(3f)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.argb(70,0,127,255)));
                                break;
                            case 2:
                                mMap.addMarker(new MarkerOptions().position(item)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                mMap.addCircle(new CircleOptions()
                                        .center(item)
                                        .radius(800.0)
                                        .strokeWidth(3f)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.argb(70,50,50,250)));
                                break;
                            case 3:
                                mMap.addMarker(new MarkerOptions().position(item)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                                mMap.addCircle(new CircleOptions()
                                        .center(item)
                                        .radius(800.0)
                                        .strokeWidth(3f)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.argb(70,0,255,255)));
                                break;
                            case 4:
                                mMap.addMarker(new MarkerOptions().position(item)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                mMap.addCircle(new CircleOptions()
                                        .center(item)
                                        .radius(800.0)
                                        .strokeWidth(3f)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.argb(70,50,150,50)));
                                break;
                            case 5:
                                mMap.addMarker(new MarkerOptions().position(item)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
                                mMap.addCircle(new CircleOptions()
                                        .center(item)
                                        .radius(800.0)
                                        .strokeWidth(3f)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.argb(70,255,0,255)));
                                break;
                            case 6:
                                mMap.addMarker(new MarkerOptions().position(item)
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                                mMap.addCircle(new CircleOptions()
                                        .center(item)
                                        .radius(800.0)
                                        .strokeWidth(3f)
                                        .strokeColor(Color.BLUE)
                                        .fillColor(Color.argb(70,255,69,0)));
                                break;
                        }
                        locCounter++;
                    }
                    showPolygonArea();
                } else {
                    Toast.makeText(MapsActivityAnalyze.this, "Error getting documents", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showPolygonArea(){
        PolylineOptions polylineOptions = new PolylineOptions().addAll(analyseList)
                .width(5)
                .color(Color.BLUE)
                .geodesic(true);
        mMap.addPolyline(polylineOptions);

        PolygonOptions polygonOptions = new PolygonOptions().addAll(analyseList)
                .clickable(true);
        Polygon polygon = mMap.addPolygon(polygonOptions);
        polygon.setFillColor(0x33FF0000);
        polygon.setStrokeColor(Color.RED);
        polygon.setStrokeWidth(3);
        polygon.setFillColor(Color.argb(20,50,0,255));
    }

    private void showAnalyse(){
        List<LatLng> analyseList = new ArrayList<>();
        analyseList.add(new LatLng(-34,152));
        analyseList.add(new LatLng(-33,153));
        analyseList.add(new LatLng(-32,154));
        analyseList.add(new LatLng(-31,155));
        analyseList.add(new LatLng(-30,156));
        analyseList.add(new LatLng(-35,150));
        analyseList.add(new LatLng(-36,149));
        analyseList.add(new LatLng(-37,148));
        analyseList.add(new LatLng(-38,147));
        analyseList.add(new LatLng(-39,146));
        analyseList.add(new LatLng(-40,145));


        
        for( LatLng points : analyseList )
        {
            //mMap.addMarker(new MarkerOptions().position(points));// address.getPostalCode()));

        }
    }

    public void onZoom(View view)
    {
        if(view.getId() == R.id.ic_zoom_in){
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
        if(view.getId() == R.id.ic_zoom_out){
            mMap.animateCamera(CameraUpdateFactory.zoomOut());
        }
    }
}