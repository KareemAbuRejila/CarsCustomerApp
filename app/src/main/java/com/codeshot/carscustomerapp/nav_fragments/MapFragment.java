package com.codeshot.carscustomerapp.nav_fragments;


import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codeshot.carscustomerapp.BottomSheetRiderFragment;
import com.codeshot.carscustomerapp.Common.Common;
import com.codeshot.carscustomerapp.CustomInfoWindow;
import com.codeshot.carscustomerapp.Interfaces.IOnBackPressed;
import com.codeshot.carscustomerapp.Models.DataMessage;
import com.codeshot.carscustomerapp.Models.Driver;
import com.codeshot.carscustomerapp.Models.FCMResponse;
import com.codeshot.carscustomerapp.Models.MyPlaces;
import com.codeshot.carscustomerapp.Models.Request;
import com.codeshot.carscustomerapp.Models.Token;
import com.codeshot.carscustomerapp.R;
import com.codeshot.carscustomerapp.Remote.IFCMService;
import com.codeshot.carscustomerapp.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.SphericalUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, IOnBackPressed,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    //View
    private Context context;
    private SwitchCompat location_switch;
    private SupportMapFragment mapFragment;
    private ProgressBar pBMap;
    private FloatingActionButton btnMapClear;
    //FireBase
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference rootRef, ridersRef, myRef;
    private GeoFire geoFire;
    //Map
    private GoogleMap mMap;
    private Boolean backStep = false;
    //PlayService
    private int My_PERMISSION_REQUEST_CODE = 7000;
    private int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private int UPDATE_INTERVAL = 5000;
    private int FASTEST_INTERVAL = 3000;
    private int DISPLACEMENT = 10;

    private Marker mUserMarker, markerDestination;

    //Car Animation
    private List<LatLng> polyLineList;
    private Marker carMarker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition, currentPosition, destinationPosition;
    private int index, next;
    private String startLocation, destination;
    private Place startPlace;
    private PolylineOptions polylineOptions, blackPolyLineOpetions;
    private Polyline blackPolyLine, grayPolyLine;
    private IGoogleAPI mServices;
    private boolean isDriverFound = false;
    String driverID = "";
    private int radius = 1;//1 Km
    private int distance = 1; //3 Km
    private static final int LIMIT = 10;
    //Sender Alert
    IFCMService ifcmService;
    private AutocompleteSupportFragment placeAutocompleteFragmentLocation, destinationPlacesLaceAutocompleteFragment;
    TypeFilter typeFilter;
    private JSONArray routes;

    private ImageView imgExpandable;
    private BottomSheetRiderFragment bottomSheetRiderFragment;
    private TextView btnPickUpRequest;

    private GeoLocation driverLocation;


    private View view;

    public MapFragment(Context context) {
        this.context = context;
    }

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view = inflater.inflate(R.layout.fragment_map, container, false);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
        }
        mapFragment.getMapAsync(this);
//        location_switch=(SwitchCompat) view.findViewById(R.id.location_switchF);
        imgExpandable = view.findViewById(R.id.imgExpandable);
        btnPickUpRequest = view.findViewById(R.id.btnPickUpRequest);
        pBMap = view.findViewById(R.id.pBMap);
        btnMapClear = view.findViewById(R.id.btnMapClear);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Obtain the SupportMapFragment and get notified when the ic_map is ready to be used.
        initializations();
        pBMap.setVisibility(View.VISIBLE);
        btnMapClear.hide();
        polyLineList = new ArrayList<>();
        mServices = Common.getGoogleAPI();

        placeAutocompleteFragmentLocation.setHint("From....");
        destinationPlacesLaceAutocompleteFragment.setHint("To....");
        placeAutocompleteFragmentLocation.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                startPlace = place;
                startLocation = place.getName().toString();
                startLocation = startLocation.replace(" ", "+");
                Log.i("start Location : ", startLocation);
                startPosition = place.getLatLng();

            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(context, status.getStatus().toString(), Toast.LENGTH_SHORT).show();

            }
        });
        placeAutocompleteFragmentLocation.getView().findViewById(R.id.places_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        destinationPlacesLaceAutocompleteFragment.setText("");
                        v.setVisibility(View.GONE);
                    }
                });
        destinationPlacesLaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                destination = place.getName().toString();
                destination = destination.replace(" ", "+");
                Log.i("Destination Location : ", destination);
                destinationPosition = place.getLatLng();
//                getDirections(String.valueOf(destinationPosition.latitude),String.valueOf(destinationPosition.latitude));
                if (startLocation != null) {
                    mMap.clear();
                    getDirections(startPlace.getLatLng(), place.getLatLng());

                    bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance(startPlace.getName(), String.valueOf(startPlace.getLatLng().latitude) + "," + String.valueOf(startPlace.getLatLng().longitude),
                            place.getName(), String.valueOf(place.getLatLng().latitude) + "," + String.valueOf(place.getLatLng().longitude), false,routes);
                    bottomSheetRiderFragment.show(getParentFragmentManager(), bottomSheetRiderFragment.getTag());
//                    mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
//                    mMap.addMarker(new MarkerOptions().position(startPlace.getLatLng()));
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(startPlace.getLatLng(), 20.0f));
                } else {
                    getDirections(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), place.getLatLng());

                    bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance("Your Position", String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()),
                            place.getName(), String.valueOf(place.getLatLng().latitude) + "," + String.valueOf(place.getLatLng().longitude), false,routes);
                    bottomSheetRiderFragment.show(getParentFragmentManager(), bottomSheetRiderFragment.getTag());
                }


            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(context, status.getStatus().toString(), Toast.LENGTH_SHORT).show();

            }
        });
        destinationPlacesLaceAutocompleteFragment.getView().findViewById(R.id.places_autocomplete_clear_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        destinationPlacesLaceAutocompleteFragment.setText("");
                        v.setVisibility(View.GONE);
                        mMap.clear();
                        displayLocation();
                        btnMapClear.hide();
                    }
                });

        setUpLocation();
//        imgExpandable.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()));
//                bottomSheetRiderFragment.show(getSupportFragmentManager(), bottomSheetRiderFragment.getTag());
//            }
//        });
        btnPickUpRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (driverLocation == null)
                    requestPickUpHere(currentUserID);
//                else getDirections(driverLocation);
                else sendRequestToDriver(driverID);
            }
        });
        updateTokenToServer();

        btnMapClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (backStep) {
                    mMap.clear();
                    driverID = "";
                    isDriverFound = false;
                    driverLocation = null;
                    displayLocation();
                    btnMapClear.hide();
                    if (geoDriver!=null){
                        geoDriver.removeAllListeners();
                    }
                    btnPickUpRequest.setText("Pickup request");
                }
            }
        });


    }

    private void updateTokenToServer() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference().child(Common.token_tbl);
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s ->
        {
            Token token = new Token(s);
            if (FirebaseAuth.getInstance().getCurrentUser() != null)//if already login, must update Token
            {
                tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("DEVICE TOKEN", "is updated to server");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("ERROR TOKEN", e.getMessage());
                    }
                });
            }
        } );


    }

    private void updateTokenToServer2() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference().child(Common.token_tbl);
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.codeshot.carscustomerapp", Context.MODE_PRIVATE);
        String newToken = sharedPreferences.getString("token", "");
        Token token = new Token(newToken);
        if (FirebaseAuth.getInstance().getCurrentUser() != null)//if already login, must update Token
        {
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i("Saved Token", "Yesssssssssssssssssssssss");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.i("ERROR TOKEN", e.getMessage());
                }
            });
        }

    }

    DatabaseReference RequestsRef;

    private void sendRequestToDriver(final String driverID) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        RequestsRef = rootRef.child(Common.requests_tbl);
        requestKey = RequestsRef.push().getKey();
        tokens.orderByKey().equalTo(driverID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                                Token token = postSnapShot.getValue(Token.class);//Get Token object from db with key
                                //Make raw payload - convert latlng to json
                                String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                FirebaseMessaging.getInstance().getToken().addOnSuccessListener(s -> {
                                    String riderToken = s;
//                                Notification data=new Notification(riderToken,json_lat_lng);//send it to Driver app and i will deserialize it again
//                                Sender sender=new Sender(token.getToken(),data);//send this data to token

                                    Map<String, String> content = new HashMap<>();
                                    content.put("title", riderToken);
//                                content.put("message",json_lat_lng);
                                    content.put("lat", String.valueOf(mLastLocation.getLatitude()));
                                    content.put("lng", String.valueOf(mLastLocation.getLongitude()));
                                    content.put("requestId", requestKey);
                                    content.put("customerId", currentUserID);
                                    DataMessage dataMessage = new DataMessage(token.getToken(), content);

                                    ifcmService.sendMessage(dataMessage)
                                            .enqueue(new Callback<FCMResponse>() {
                                                @Override
                                                public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                    if (response.body().success == 1) {
                                                        saveRequestToServier();
                                                    } else {
                                                        Toast.makeText(context, "Request Failed sent!", Toast.LENGTH_SHORT).show();

                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<FCMResponse> call, Throwable t) {
                                                    Log.e("ERROR REQUEST SENT ", t.getMessage());
                                                    Toast.makeText(context, "ERROR REQUEST SENT! " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                });

                            }

                        } else {
                            Log.e("ERROR DATASHOT", "Not Exists");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("DATABASE ERROR", databaseError.getMessage());
                    }
                });
    }

    private String requestKey = "";

    private void saveRequestToServier() {
        //Save Request

        DatabaseReference requestRef = RequestsRef.child(currentUserID);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        SimpleDateFormat timeformate = new SimpleDateFormat("hh:mm a");
        String currentDate = dateFormat.format(Calendar.getInstance().getTime());
        String currentTime = timeformate.format(Calendar.getInstance().getTime());
        final Request request = new Request(currentUserID, driverID, currentTime, currentDate, getString(R.string.waitingStatus));
        request.setCustomerId(currentUserID);
        request.setId(requestKey);
        final GeoFire geoFireRequestLocation = new GeoFire(requestRef.child(requestKey).child("geoFireRequestLocation"));
        final GeoFire geoFireDriverLocation = new GeoFire(requestRef.child(requestKey).child("geoFireDriverLocation"));

        requestRef.child(requestKey).setValue(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        geoFireRequestLocation.setLocation(requestKey, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error == null) {
                                    geoFireDriverLocation.setLocation(requestKey, new GeoLocation(driverLocation.latitude, driverLocation.longitude), new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {
                                            if (error == null) {
                                                if (mMap.isMyLocationEnabled())
//                                                    mUserMarker.remove();
//                                                mUserMarker = mMap.addMarker(new MarkerOptions()
//                                                        .title("PickUp Here")
//                                                        .snippet("snippet of PickUp")
//                                                        .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
//                                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
//                                                mUserMarker.showInfoWindow();

                                                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                                    @Override
                                                    public boolean onMarkerClick(Marker marker) {
                                                        marker.showInfoWindow();
                                                        return true;
                                                    }
                                                });
                                                btnPickUpRequest.setText("Getting your PickUp");
                                                findDriver();
                                                Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show();
                                                //saveRequestIdToRiderTbl
                                                Common.lastRequestKey = requestKey;
                                            } else Log.e("GeoFire ERROR", error.getMessage());
                                        }
                                    });
                                } else Log.e("GeoFire ERROR", error.getMessage());
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }

    private void requestPickUpHere(String currentUserID) {
        if (!isDriverFound) {
            DatabaseReference RequestsRef = rootRef.child(Common.pickUpRequest_tbl);
            DatabaseReference requestRef = RequestsRef.child(currentUserID);
            String requestKey = requestRef.push().getKey();
            GeoFire geoFire = new GeoFire(requestRef.child(requestKey));
            geoFire.setLocation(currentUserID, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {
                    if (error == null) {
                        if (mMap.isMyLocationEnabled())
//                            mUserMarker.remove();
                            mMap.setMyLocationEnabled(false);
                        mUserMarker = mMap.addMarker(new MarkerOptions()
                                .title("PickUp Here")
                                .snippet("snippet of PickUp")
                                .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        mUserMarker.showInfoWindow();
                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                marker.showInfoWindow();
                                return true;
                            }
                        });
                        btnPickUpRequest.setText("Getting your PickUp");
                        backStep = true;
                        btnMapClear.show();
                        findDriver();
                    } else Log.e("GeoFire ERROR", error.getMessage());
                }
            });

        }

    }
    GeoQuery geoDriver;
    private void findDriver() {
        DatabaseReference driversLRef = rootRef.child(Common.driversAvailable_tbl);
        GeoFire gfDrivers = new GeoFire(driversLRef);
        geoDriver = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude())
                , radius);
        geoDriver.removeAllListeners();
        geoDriver.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //if found
                if (!isDriverFound) {
                    isDriverFound = true;
                    driverID = key;
                    btnPickUpRequest.setText("CALL DRIVER");
                    Toast.makeText(context, "Driver is founded" + key, Toast.LENGTH_SHORT).show();
                    driverLocation = location;
                    btnMapClear.show();
                }
            }

            @Override
            public void onKeyExited(String key) {
                if (driverID.equals(key)) {
                    btnPickUpRequest.setText("PICKUP REQUEST");
                    btnPickUpRequest.setEnabled(true);
                    isDriverFound = false;
                    driverLocation=null;
                    driverID = "";
                    Toast.makeText(context, "Driver back to offline", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                driverLocation = location;
            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver, increase distance
                if (!isDriverFound && radius < LIMIT) {
                    radius++;
                    findDriver();
                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(context, "onGeoQueryError! " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDirections(final LatLng startPlace, final LatLng endPlace) {

        currentPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

        String requestAPI = null;
        try {
            requestAPI = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "mode=driving&" +
                    "transit_routing_preference=less_driving&" +
                    "origin=" +
                    +startPlace.latitude + "," + startPlace.longitude + "&" +
                    "destination=" + endPlace.latitude + "," + endPlace.longitude + "&" +
                    "key=" + getResources().getString(R.string.googleDirectionKey);
            Log.i("Direction Request API", requestAPI);
            mServices.getPaths(requestAPI)
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            try {
                                JSONObject jsonObject=new JSONObject(response.body().toString());
                                JSONArray jsonArray=jsonObject.getJSONArray("routes");
                                bottomSheetRiderFragment.setRoutes(jsonArray);
                                for (int i=0;i<jsonArray.length();i++){
                                    JSONObject route=jsonArray.getJSONObject(i);
                                    JSONObject poly=route.getJSONObject("overview_polyline");
                                    String polyline=poly.getString("points");
                                    polyLineList=decodePoly(polyline);
                                }
//                                Adjusting bounds
                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                for (LatLng latLng : polyLineList) {
                                    builder.include(latLng);
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                                    mMap.animateCamera(cameraUpdate);

                                    polylineOptions = new PolylineOptions();
                                    polylineOptions.color(Color.RED);
                                    polylineOptions.width(5);
                                    polylineOptions.startCap(new SquareCap());
                                    polylineOptions.endCap(new SquareCap());
                                    polylineOptions.jointType(JointType.ROUND);
                                    polylineOptions.addAll(polyLineList);
                                    grayPolyLine = mMap.addPolyline(polylineOptions);

//                                    blackPolyLineOpetions = new PolylineOptions();
//                                    blackPolyLineOpetions.color(Color.BLACK);
//                                    blackPolyLineOpetions.width(5);
//                                    blackPolyLineOpetions.startCap(new SquareCap());
//                                    blackPolyLineOpetions.endCap(new SquareCap());
//                                    blackPolyLineOpetions.jointType(JointType.ROUND);
//                                    blackPolyLineOpetions.addAll(polyLineList);
//                                    blackPolyLine = mMap.addPolyline(blackPolyLineOpetions);

//                                    mMap.addMarker(new MarkerOptions()
//                                            .position(polyLineList.get(polyLineList.size() - 1))
//                                            .title("PickUp Location"));
                                    if (markerDestination != null) markerDestination.remove();
                                    markerDestination = mMap.addMarker(new MarkerOptions().position(endPlace).
                                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).
                                            title("DESTINATION HERE"));


                                    //Animations
                                    ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                                    polyLineAnimator.setDuration(2000);
                                    polyLineAnimator.setInterpolator(new LinearInterpolator());
                                    polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator animation) {
                                            List<LatLng> points = grayPolyLine.getPoints();
                                            int percentValue = (int) animation.getAnimatedValue();
                                            int size = points.size();
                                            int newPoint = (int) (size * (percentValue / 100.0f));
                                            List<LatLng> p = points.subList(0, newPoint);
//                                            blackPolyLine.setPoints(p);
                                        }
                                    });
                                    polyLineAnimator.start();

//                                    carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
//                                            .flat(true)
//                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
//
//                                    handler = new Handler();
//                                    index = -1;
//                                    next = 1;
//                                    handler.postDelayed(getDrawPathRunnable(), 3000);
                                    pBMap.setVisibility(View.GONE);
                                    backStep = true;
                                    btnMapClear.show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            Toast.makeText(requireContext(), t.getMessage(),Toast.LENGTH_LONG).show();

                        }
                    });
//            mServices.getPath(requestAPI)
//                    .enqueue(new Callback<String>() {
//                        @Override
//                        public void onResponse(Call<String> call, Response<String> response) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(response.body().toString());
//                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
//                                routes=jsonArray;
//                                bottomSheetRiderFragment.setRoutes(routes);
//                                for (int i = 0; i < jsonArray.length(); i++) {
//                                    JSONObject route = jsonArray.getJSONObject(i);
//                                    JSONObject poly = route.getJSONObject("overview_polyline");
//                                    String polyline = poly.getString("points");
//                                    polyLineList = decodePoly(polyline);
//                                    mMap.clear();
//
//                                    mUserMarker = mMap.addMarker(new MarkerOptions().position(startPlace).
//                                            icon(BitmapDescriptorFactory.defaultMarker()).
//                                            title("PICKUP HERE"));
//                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPlace, 20.0f));
//
//
//                                }
//                                //Adjusting bounds
//                                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                                for (LatLng latLng : polyLineList) {
//                                    builder.include(latLng);
//                                    LatLngBounds bounds = builder.build();
//                                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
//                                    mMap.animateCamera(cameraUpdate);
//
//                                    polylineOptions = new PolylineOptions();
//                                    polylineOptions.color(Color.RED);
//                                    polylineOptions.width(5);
//                                    polylineOptions.startCap(new SquareCap());
//                                    polylineOptions.endCap(new SquareCap());
//                                    polylineOptions.jointType(JointType.ROUND);
//                                    polylineOptions.addAll(polyLineList);
//                                    grayPolyLine = mMap.addPolyline(polylineOptions);
//
//                                    blackPolyLineOpetions = new PolylineOptions();
//                                    blackPolyLineOpetions.color(Color.BLACK);
//                                    blackPolyLineOpetions.width(5);
//                                    blackPolyLineOpetions.startCap(new SquareCap());
//                                    blackPolyLineOpetions.endCap(new SquareCap());
//                                    blackPolyLineOpetions.jointType(JointType.ROUND);
//                                    blackPolyLineOpetions.addAll(polyLineList);
//                                    blackPolyLine = mMap.addPolyline(blackPolyLineOpetions);
//
////                                    mMap.addMarker(new MarkerOptions()
////                                            .position(polyLineList.get(polyLineList.size() - 1))
////                                            .title("PickUp Location"));
//                                    if (markerDestination != null) markerDestination.remove();
//                                    markerDestination = mMap.addMarker(new MarkerOptions().position(endPlace).
//                                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)).
//                                            title("DESTINATION HERE"));
//
//
//                                    //Animations
//                                    ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
//                                    polyLineAnimator.setDuration(2000);
//                                    polyLineAnimator.setInterpolator(new LinearInterpolator());
//                                    polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                        @Override
//                                        public void onAnimationUpdate(ValueAnimator animation) {
//                                            List<LatLng> points = grayPolyLine.getPoints();
//                                            int percentValue = (int) animation.getAnimatedValue();
//                                            int size = points.size();
//                                            int newPoint = (int) (size * (percentValue / 100.0f));
//                                            List<LatLng> p = points.subList(0, newPoint);
//                                            blackPolyLine.setPoints(p);
//                                        }
//                                    });
//                                    polyLineAnimator.start();
//
////                                    carMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
////                                            .flat(true)
////                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
////
////                                    handler = new Handler();
////                                    index = -1;
////                                    next = 1;
////                                    handler.postDelayed(getDrawPathRunnable(), 3000);
//                                    pBMap.setVisibility(View.GONE);
//                                    backStep = true;
//                                    btnMapClear.show();
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onFailure(Call<String> call, Throwable t) {
//                            Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
//
//                        }
//                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String getPlacesRequestUri(Location locationRequest,String type){
        String output = "json";
        String baseUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/";
        String location="location="+locationRequest.getLatitude()+","+locationRequest.getLongitude();
        String radius="radius=10000";
        String marker="type=$type";
        String senxor="senxor=ture";
        String key="key="+getResources().getString(R.string.google_maps_key);
        String url=baseUrl+output+"?"+location+"&"+radius+"&"+type+"&"+senxor+"&"+key;
        Log.d("PlacesURL",url);
        return url;
    }

    private List decodePoly(String encoded) {
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);

        }

        return poly;
    }

    private Runnable getDrawPathRunnable() {
        Runnable drawPathRunnable = new Runnable() {
            @Override
            public void run() {
                if (index < polyLineList.size() - 1) {
                    index++;
                    next = index + 1;
                }
                if (index < polyLineList.size() - 1) {
                    startPosition = polyLineList.get(index);
                    endPosition = polyLineList.get(next);
                }
                final ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                valueAnimator.setDuration(3000);
                valueAnimator.setInterpolator(new LinearInterpolator());
                valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        v = valueAnimator.getAnimatedFraction();
                        lng = v * endPosition.longitude + (1 - v) * startPosition.longitude;
                        lat = v * endPosition.latitude + (1 - v) * startPosition.latitude;
                        LatLng newPos = new LatLng(lat, lng);
                        carMarker.setPosition(newPos);
                        carMarker.setAnchor(0.5f, 0.5f);
                        carMarker.setRotation(getBearing(startPosition, newPos));
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(newPos)
                                        .zoom(15.5f)
                                        .build()
                        ));
                    }
                });
                valueAnimator.start();
                handler.postDelayed(this, 3000);

            }
        };
        return drawPathRunnable;
    }

    private float getBearing(LatLng startPosition, LatLng endPosition) {
        double lat = Math.abs(startPosition.latitude - endPosition.latitude);
        double lng = Math.abs(startPosition.longitude - endPosition.longitude);

        if (startPosition.latitude < endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude < endPosition.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (startPosition.latitude >= endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (startPosition.latitude < endPosition.latitude && startPosition.longitude >= endPosition.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Request runtime permission
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, My_PERMISSION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }
        }

    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //create LatLng from mLastLocation and this is center point
            LatLng center = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            //distance in metters
            //heading 0 is northside, 90 is east, 180 is south and 270 is west
            //base on compact :)
            LatLng northSide = SphericalUtil.computeOffset(center, 100000, 0);
            LatLng southSide = SphericalUtil.computeOffset(center, 100000, 180);
            LatLngBounds bounds = LatLngBounds.builder()
                    .include(northSide)
                    .include(southSide)
                    .build();
//            RectangularBounds rectangularBounds=RectangularBounds.newInstance(northSide,southSide);
//            placeAutocompleteFragmentLocation.setLocationBias(rectangularBounds);
            placeAutocompleteFragmentLocation.setTypeFilter(typeFilter);
            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();

            DatabaseReference availableDrivers = FirebaseDatabase.getInstance().getReference(Common.drivers_tbl);
            availableDrivers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //if have anu change from Drivers tl, will reload all drivers available
                    loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("Display Location", databaseError.getMessage());
                }
            });
//            loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            pBMap.setVisibility(View.GONE);
            Log.d("Kareem", String.format("Your Location was changed : %f / %f", latitude, longitude));
        } else
            Log.d("LAST LOCATION", "Can not get your Location");

    }

    private void loadAllAvailableDriver(final LatLng location) {

//        //first, delete all markers on map(include my location marker and available drivers)
//        mMap.clear();
//        //just add location again
//        mMap.addMarker(new MarkerOptions().position(location)
//        .title("Me"));
        //Add Marker
//        if (mUserMarker != null) mUserMarker.remove();//Remove already mUserMarker
//        mUserMarker = mMap.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory.defaultMarker())
//                .position(location)
//                .title("You"));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));


        //Load all available Driver in distance 3 km;
        DatabaseReference driversLRef = rootRef.child(Common.driversAvailable_tbl);
        GeoFire gfDrivers = new GeoFire(driversLRef);

        GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(location.latitude, location.longitude)
                , distance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            Marker dMarker = null;
            DatabaseReference driversRef = rootRef.child(Common.drivers_tbl);

            @Override
            public void onKeyEntered(String key, final GeoLocation location) {

                //Use key to get email from table Drivers
                //Table Driver is table when driver register account and update info
                //Just open your to check this table name
                driversRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Because Rider and Driver model is same properties
                        Driver driver = dataSnapshot.getValue(Driver.class);

                        //Add Driver to Map
                        dMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude, location.longitude))
                                .title(driver.getUserName())
                                .snippet("Phone: " + driver.getPhoneNumber())
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onKeyExited(String key) {
                dMarker.remove();
            }

            @Override
            public void onKeyMoved(String key, final GeoLocation location) {
                if (dMarker != null) dMarker.remove();
                driversRef.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        //Because Rider and Driver model is same properties
                        Driver driver = dataSnapshot.getValue(Driver.class);

                        //Add Driver to Map
                        dMarker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude, location.longitude))
                                .title(driver.getUserName())
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
                        );
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onGeoQueryReady() {
                //if still not found driver, increase distance
                if (distance <= LIMIT) {
                    //distance for just for 3 km
                    distance++;
                    loadAllAvailableDriver(location);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Toast.makeText(context, error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildGoogleApiClient() {
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
//        GoogleApiClient mGoogleApiClient = mGoogleSignInClient.asGoogleApiClient();

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .enableAutoManage(getActivity(), this)
                .addConnectionCallbacks(MapFragment.this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(), PLAY_SERVICE_RES_REQUEST).show();
            } else {
                Toast.makeText(context, "This Device is not supported", Toast.LENGTH_LONG).show();
            }
            return false;
        }
        return true;
    }

    private void rotateMarker(final Marker marker, final int i, GoogleMap mMap) {
        final Handler handler = new Handler();
        long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis();
                float t = interpolator.getInterpolation((float) elapsed / duration);
                float rot = t * i * (1 - t) * startRotation;
                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
    }

    private void stopLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
                if (location_switch.isChecked()) {
//                    displayLocation();
                }
            }
        }
    }

    private void initializations() {
        //Map Activity
        if (!Places.isInitialized()) {
            Places.initialize(context, getResources().getString(R.string.googleDirectionKey));
        }
        placeAutocompleteFragmentLocation = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.placeAutocompleteFragment);
        placeAutocompleteFragmentLocation.setPlaceFields(Arrays.asList(
                Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS,
                Place.Field.WEBSITE_URI));
        destinationPlacesLaceAutocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.destinationPlacesLaceAutocompleteFragment);
        destinationPlacesLaceAutocompleteFragment.setPlaceFields(Arrays.asList(
                Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.WEBSITE_URI));
//        typeFilter=new AutocompleteFilter.Builder()
//                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
//                .setTypeFilter(3)
//                .build();
//        typeFilter=new TypeFilter().
        rootRef = FirebaseDatabase.getInstance().getReference();
        ridersRef = rootRef.child(Common.riders_tbl);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null)
            currentUserID = mAuth.getCurrentUser().getUid();
        myRef = ridersRef.child(currentUserID);
        geoFire = new GeoFire(myRef);
        ifcmService = Common.getFCMService();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        try {
            boolean isSuccessMap = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.uber_style_map)
            );
            if (!isSuccessMap) Log.e("ERROR MAP", "Map style load failed !!!");
        } catch (Resources.NotFoundException ex) {
            ex.printStackTrace();
        }

        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);


        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(context));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (markerDestination != null) markerDestination.remove();
                markerDestination = mMap.addMarker(new MarkerOptions().position(latLng).
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)).
                        title("DESTINATION HERE"));
                getDirections(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),latLng);
//                getDirections();
                bottomSheetRiderFragment = BottomSheetRiderFragment.newInstance("Your Position", String.valueOf(mLastLocation.getLatitude()) + "," + String.valueOf(mLastLocation.getLongitude()),
                        latLng.toString(), String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude), false,routes);
                bottomSheetRiderFragment.show(getParentFragmentManager(), bottomSheetRiderFragment.getTag());
                backStep = true;
                btnMapClear.show();


            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(context, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
        pBMap.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

        try {
            mServices.getPlaces(getPlacesRequestUri(location, "market"))
                    .enqueue(new Callback<MyPlaces>() {
                        @Override
                        public void onResponse(@NotNull Call<MyPlaces> call, @NotNull Response<MyPlaces> response) {
                            if (response.isSuccessful()){
                                assert response.body() != null;
                                Log.d("MyPlaces", response.body().toString());
                            }
                        }

                        @Override
                        public void onFailure(Call<MyPlaces> call, Throwable t) {
                            Log.d("MyPlaces", "onFailure: "+t.getMessage());
                        }
                    });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (backStep) {
            mMap.clear();
            displayLocation();
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
        super.onDestroy();
    }
}

