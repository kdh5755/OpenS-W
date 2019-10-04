package com.example.realgps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    private GoogleMap mGoogleMap = null;
    boolean mMoveMapByUser = true;
    private Marker currentMarker = null;
    private static final int REQUEST_LOCATION = 1;
    Button btn;
    Button btnLoad;
    TextView Lat, Lon;
    TextView showLocationTxt;
    double a,b;
    DatabaseReference reff;
    LocationManager locationManager;
    private List<Marker> originMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //'위치 확인' 버튼
        Lat = (TextView) findViewById(R.id.Latitude);
        Lon = (TextView) findViewById(R.id.Longitude);
        btnLoad = (Button) findViewById(R.id.Save);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 reff = FirebaseDatabase.getInstance().getReference().child("User").child("1");
                 reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String latitude = dataSnapshot.child("latitude").getValue().toString();
                        String longitude = dataSnapshot.child("longitude").getValue().toString();
                        Lat.setText(latitude);
                        Lon.setText(longitude);
                        a = Double.parseDouble(dataSnapshot.child("latitude").getValue().toString());
                        b = Double.parseDouble(dataSnapshot.child("longitude").getValue().toString());
                        TrashLocation(a,b);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                Toast.makeText(MainActivity.this, "쓰레기통 위치 확인완료", Toast.LENGTH_SHORT).show();
            }
        });

        //'현재 위치' 버튼
        ActivityCompat.requestPermissions(this, new String[]
                {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        showLocationTxt = findViewById(R.id.location);
        btn = findViewById(R.id.Gpscheck);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                //GPS 활성 여부
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    //GPS 비활성
                    OnGPS();
                }
                else {
                    //GPS 활성
                    getLocation();
                }
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mGoogleMap = googleMap;
        LatLng Init = new LatLng(37.56,126.97);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Init));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(5));
        originMarkers.add(mGoogleMap.addMarker(new MarkerOptions().title("초기 위치").position(Init)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mGoogleMap.setMyLocationEnabled(true);
    }

    public void TrashLocation(double a, double b) {

        mMoveMapByUser = false;
        LatLng Trash_LOCATION = new LatLng(a,b);
        String markerTitle = "쓰레기통";
        String markerSnippet = "대기 중";
        if (currentMarker != null) currentMarker.remove();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Trash_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mGoogleMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(Trash_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }
    public  void CurrentLocation(double c, double d){
        mMoveMapByUser = false;

        //디폴트 위치
        LatLng CURRENT_LOCATION = new LatLng(c,d);
        String markerTitle = "현재위치";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(CURRENT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        currentMarker = mGoogleMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(CURRENT_LOCATION, 15);
        mGoogleMap.moveCamera(cameraUpdate);
    }
    private void getLocation() {

        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (LocationGps != null) {
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();

                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(longi);

                double c = Double.parseDouble(latitude);
                double d = Double.parseDouble(longitude);
                CurrentLocation(c,d);

                showLocationTxt.setText("현재 위치:" + "\n" + "위도(Latitude) = " + latitude + "\n" + "경도(Longitude)= " + longitude);
                Toast.makeText(MainActivity.this, "현재위치 확인 완료", Toast.LENGTH_SHORT).show();
            } else if (LocationNetwork != null) {
                double lat = LocationNetwork.getLatitude();
                double longi = LocationNetwork.getLongitude();

                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(longi);

                showLocationTxt.setText("현재 위치:" + "\n" + "위도(Latitude) = " + latitude + "\n" + "경도(Longitude)= " + longitude);
                Toast.makeText(MainActivity.this, "현재위치 확인 완료", Toast.LENGTH_SHORT).show();
            } else if (LocationPassive != null) {
                double lat = LocationPassive.getLatitude();
                double longi = LocationPassive.getLongitude();

                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(longi);

                showLocationTxt.setText("현재 위치:" + "\n" + "위도(Latitude) = " + latitude + "\n" + "경도(Longitude)= " + longitude);
            } else {
                Toast.makeText(this, "현재 위치를 알 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("GPS가 연결되어 있지않습니다.").setCancelable(false).setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_LOCALE_SETTINGS));
            }
        }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}