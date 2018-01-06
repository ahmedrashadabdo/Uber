package com.example.uber.uber;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class UserMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener
{

    private static final String TAG = "UserMapsActivity";
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button btn_Logout, btn_Request;

    private LatLng pickupLocation;
    private Marker pickupMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btn_Logout = (Button) findViewById(R.id.logout);
        btn_Request = (Button) findViewById(R.id.request);
        /*btn_Setting = (Button) findViewById(R.id.setting);
        btn_History = (Button) findViewById(R.id.history);*/


        btn_Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*isLoggingOut = true;

                disconnectDriver();*/

                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserMapsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        btn_Request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference refrequest = FirebaseDatabase.getInstance().getReference("UserRequest");
                GeoFire geoFire = new GeoFire(refrequest);
                geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));/*.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup))*/

                btn_Request.setText("Getting your Driver....");

//                getClosestDriver();
            }
        });

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

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        buildGoogleApiClient(); //Builder to configure a GoogleApiClient.
        mMap.setMyLocationEnabled(true); // Enable to get my current location


        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    protected synchronized void buildGoogleApiClient(){

        mGoogleApiClient = new GoogleApiClient.Builder(this)                      //Builder to configure a GoogleApiClient.
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)                             //Provides callbacks for scenarios that result in a failed attempt to connect the client to the service.
                .addApi(LocationServices.API)                                   //Provides callbacks that are called when the client is connected or disconnected from the service.
                .build();
        mGoogleApiClient.connect();                                            //Connects the client to Google Play services.
    }



    @Override
    public void onLocationChanged(Location location) {

        if (getApplicationContext()!=null) {

            mLastLocation = location;

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }

    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();                              //LocationRequest objects are used to request a quality of service for location updates
        mLocationRequest.setInterval(1000);                                   //Set the desired interval for active location updates, in milliseconds.
        mLocationRequest.setFastestInterval(1000);                           // location is available sooner you can get it (i.e. another app is using the location services).
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//This would be appropriate for mapping applications that are showing your location in real-time.

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }


    @Override
    public void onConnectionSuspended(int i) {

    }


    @Override
    protected void onStop() {
        super.onStop();

    }
}
