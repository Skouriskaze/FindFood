package caden.foodapp;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.result.DataSourceStatsResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Map extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMarkerClickListener,
        LocationListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 6;

    private GoogleMap mMap;

    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager m_LocationManager;
    private Location m_Location;
    private LocationRequest mLocationRequest;

    private boolean isStartMarked;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser = null;

    private Campus mCampus;
    private LatLng mUni;
    private Marker mCamMarker;

    private FloatingActionButton fab;
    private boolean isStateDragging;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private DataSnapshot mDataSnapshot;

    private List<Pin> mPins;
    private List<String> mTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mContext = this;
        isStartMarked = false;
        isStateDragging = false;

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mTypes = new ArrayList<>();
        updateTypes();

        mCampus = new Campus(getIntent().getStringExtra("Name"), getIntent().getStringExtra("Address"));
        if (!mCampus.getName().equals("") && mCampus.getName() != null) {
            Geocoder geocoder = new Geocoder(this);
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocationName(mCampus.getName(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Address address = addressList.get(0);
            if (address != null)
                mUni = new LatLng(address.getLatitude(), address.getLongitude());
        }

        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Pins").child(mCampus.getName());
        mPins = new ArrayList<>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //
                mDataSnapshot = dataSnapshot;
                onPinsChanged(dataSnapshot);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database", "Cancelled???");
            }
        });

        if (mUser != null) {
            Log.d("FIREBASE", mUser.getEmail());
        }

        fab = (FloatingActionButton) findViewById(R.id.fab);
        RadioButton rbAllDay = (RadioButton) findViewById(R.id.rbAllDay);
        rbAllDay.setChecked(true);

        if (checkPlayServices()) {
            // If this check succeeds, proceed with normal processing.
            // Otherwise, prompt user to get valid Play Services APK.
            if (!AppUtils.isLocationEnabled(mContext)) {
                // notify user
                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
                dialog.setMessage("Location not enabled!");
                dialog.setPositiveButton("Open location settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        // TODO Auto-generated method stub

                    }
                });
                dialog.show();
            }
            buildGoogleApiClient();
        } else {
            Toast.makeText(mContext, "Location not supported in this device", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateTypes() {
        updateTypes(false);
    }
    private void updateTypes(boolean ignore) {
        mTypes.clear();
        LinearLayout llFilter = (LinearLayout) findViewById(R.id.llFilters);
        for (int i = 0; i < llFilter.getChildCount(); i++) {
            View view = llFilter.getChildAt(i);
            if (view instanceof CheckBox) {
                CheckBox cb = (CheckBox) view;
                if (!ignore && cb.isChecked()) {
                    mTypes.add(cb.getTag().toString());
                } else if (ignore) {
                    mTypes.add(cb.getTag().toString());
                }
            }
        }
    }

    private void onPinsChanged(DataSnapshot dataSnapshot) {
        mPins.clear();
        killExpiredPins(dataSnapshot);
        for (DataSnapshot d : dataSnapshot.getChildren()) {
            Pin p = d.getValue(Pin.class);
            mPins.add(p);
        }
        drawAllMarkers();
    }

    private void killExpiredPins() {
        killExpiredPins(mDataSnapshot);
    }
    private void killExpiredPins(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null) {
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                Pin p = d.getValue(Pin.class);
                if (p.getExpiration() < System.currentTimeMillis()) {
                    ref.child(d.getKey()).removeValue();
                }
            }
        }
    }

    private void drawAllMarkers() {
        mMap.clear();
        for (Pin p : mPins) {
            if (mTypes.contains(p.getType()))
                drawMarker(p);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
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
        mMap.setOnMarkerClickListener(this);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(37, -96)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mUni, 15));
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                killExpiredPins();
                if (isStateDragging) {
                    if (mCamMarker != null) {
                        mCamMarker.setPosition(mMap.getCameraPosition().target);
                    }
                }
            }
        });

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                //finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdate();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                    mMap.setMyLocationEnabled(true);
                } else {
                    Log.e("Map", "Permission Denied");
                }
            }
        }
    }

    protected void startLocationUpdate() {
        mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10000).setFastestInterval(2000);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        m_Location = location;
        killExpiredPins();
        if (!isStartMarked) {
            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
            //mMap.addMarker(new MarkerOptions().position(loc).title("Your Location"));
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
            isStartMarked = true;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    public void addDialog(View view) {
        Log.d("Action", "Marker to be added");
        isStateDragging = !isStateDragging;

        updateTypes(isStateDragging);
        killExpiredPins();
        drawAllMarkers();

        if (isStateDragging) {
            mCamMarker = mMap.addMarker(new MarkerOptions().title("Nothing").snippet("Snippet").position(mMap.getCameraPosition().target));
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#03A9F4")));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
        } else {
            fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E91E63")));
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_add));
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final LayoutInflater layoutInflater = this.getLayoutInflater();
            final View dialogView = layoutInflater.inflate(R.layout.dialog_marker, null, false);
            final LatLng camPos = mMap.getCameraPosition().target;
            final NumberPicker np = (NumberPicker) dialogView.findViewById(R.id.npDuration);
            final NumberPicker npH = (NumberPicker) dialogView.findViewById(R.id.npDurationHours);
            np.setMinValue(0);
            np.setMaxValue(60);
            np.setValue(0);
            npH.setMinValue(0);
            npH.setMaxValue(6);
            npH.setValue(1);
            builder.setView(dialogView)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String desc = ((EditText) dialogView.findViewById(R.id.desc)).getText().toString();
                            String type = ((RadioButton) dialogView.findViewById(((RadioGroup) dialogView.findViewById(R.id.rgType)).getCheckedRadioButtonId())).getText().toString();
                            int nMinutes= np.getValue();
                            int nHours = npH.getValue();
                            int nDuration = 60 * nHours + nMinutes;
                            LinearLayout llTags = (LinearLayout) dialogView.findViewById(R.id.llTags);
                            List<String> tags = new ArrayList<>();
                            String snip = "";
                            for (int i = 0; i < llTags.getChildCount(); i++) {
                                View cb = llTags.getChildAt(i);
                                if (cb instanceof CheckBox && ((CheckBox) cb).isChecked()) {
                                    snip += (snip.equals("") ? "" : ", ") + ((CheckBox) cb).getText();
                                    tags.add(((CheckBox) cb).getText().toString());
                                }
                            }
                            //String snip = ((EditText) dialogView.findViewById(R.id.snippet)).getText().toString();
                            if (desc.trim().equals("")) {
                                desc = "Food";
                            }

                            Pin pin = new Pin(mCampus, camPos, desc, type, tags, System.currentTimeMillis() + 1000 * 60 * nDuration);

                            Marker mark = drawMarker(pin);
                            mark.setTag(pin);

                            mPins.add(pin);
                            ref.push().setValue(pin);

                            mark.showInfoWindow();
                            mCamMarker.remove();
//                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mCamMarker.remove();
                            dialog.cancel();
                        }
                    });

            builder.show();
        }

    }

    private Marker drawMarker(Pin p) {
        LatLng camPos = new LatLng(p.getLatitute(), p.getLongitude());
        String desc = p.getDescription();
        String type = p.getType();
        List<String> tags = p.getTags();
        String snip = "";
        if (null != tags) {
            for (String s : tags) {
                snip += (snip.equals("") ? "" : ", ") + (s);
            }
        }

        Marker mark = mMap.addMarker(new MarkerOptions()
                .position(camPos).title(desc));

        //.icon(BitmapDescriptorFactory.fromResource(R.drawable.cake)));
        switch (type) {
            case "Desserts":
                mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.cakes));
                break;
            case "Snacks":
                mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.chipss));
                break;
            case "Meals":
                mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.pizzas));
                break;
            case "Other":
                mark.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.asterisks));
                break;
            default:
                break;
        }
        if (!snip.trim().equals("")) {
            mark.setSnippet(snip);
        }

        return mark;
    }

    public void onBeginClick(View view) {
        final EditText etBegin = (EditText) findViewById(R.id.etBeginTime);
        final RadioButton rbFrom = (RadioButton) findViewById(R.id.rbFrom);
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                rbFrom.setChecked(true);
                StringBuilder sb = new StringBuilder();
                sb.append(hourOfDay % 12 == 0 ? 12 : hourOfDay % 12);
                sb.append(":");
                sb.append(String.format("%1$02d", minute));
                sb.append(hourOfDay > 11 ? " PM" : " AM");
                etBegin.setText(sb.toString());
            }
        }, 0, 0, false);
        tpd.show();
    }

    public void onEndClick(View view) {

        final EditText etEnd = (EditText) findViewById(R.id.etEndTime);
        final RadioButton rbFrom = (RadioButton) findViewById(R.id.rbFrom);
        TimePickerDialog tpd = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                rbFrom.setChecked(true);
                StringBuilder sb = new StringBuilder();
                sb.append(hourOfDay % 12 == 0 ? 12 : hourOfDay % 12);
                sb.append(":");
                sb.append(String.format("%1$02d", minute));
                sb.append(hourOfDay > 11 ? " PM" : " AM");
                etEnd.setText(sb.toString());
            }
        }, 0, 0, false);
        tpd.show();
    }

    public void onChecked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.cbCake:
                if (checked) {
                    ((CheckBox) view).setButtonDrawable(R.drawable.cakep);
                } else {
                    ((CheckBox) view).setButtonDrawable(R.drawable.cake);
                }
                break;
            case R.id.cbChips:
                if (checked) {
                    ((CheckBox) view).setButtonDrawable(R.drawable.chipsp);
                } else {
                    ((CheckBox) view).setButtonDrawable(R.drawable.chips);
                }
                break;
            case R.id.cbPizza:
                if (checked) {
                    ((CheckBox) view).setButtonDrawable(R.drawable.pizzap);
                } else {
                    ((CheckBox) view).setButtonDrawable(R.drawable.pizza);
                }
                break;
            case R.id.cbAsterisk:
                if (checked) {
                    ((CheckBox) view).setButtonDrawable(R.drawable.asteriskp);
                } else {
                    ((CheckBox) view).setButtonDrawable(R.drawable.asterisk);
                }
                break;
        }

        updateTypes();
        killExpiredPins();
        drawAllMarkers();
    }
}
