package org.helpapaw.helpapaw.signalsmap;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding;

import java.util.List;

public class SignalsMapFragment extends BaseFragment implements SignalsMapContract.View, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = SignalsMapFragment.class.getSimpleName();
    private static final int LOCATION_PERMISSIONS_REQUEST = 1;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

    private GoogleMap signalsGoogleMap;

    private SignalsMapPresenter signalsMapPresenter;
    private SignalsMapContract.UserActionsListener actionsListener;

    FragmentSignalsMapBinding binding;

    public SignalsMapFragment() {
        // Required empty public constructor
    }

    public static SignalsMapFragment newInstance() {
        return new SignalsMapFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signals_map, container, false);

        binding.mapSignals.onCreate(savedInstanceState);

        if (binding.mapSignals != null) {
            binding.mapSignals.getMapAsync(getMapReadyCallback());
        }

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            signalsMapPresenter = new SignalsMapPresenter(this);
            actionsListener = signalsMapPresenter;
        } else {
            signalsMapPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalsMapPresenter.setView(this);
            actionsListener = signalsMapPresenter;
        }

        initLocationApi();

        binding.fabAddSignal.setOnClickListener(getFabAddSignalClickListener());

        return binding.getRoot();
    }

    private OnMapReadyCallback getMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                signalsGoogleMap = googleMap;
            }
        };
    }

    private void initLocationApi() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(30 * 1000)        // 30 seconds, in milliseconds
                .setFastestInterval(10 * 1000); // 10 seconds, in milliseconds
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapSignals.onResume();
        googleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapSignals.onPause();

        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        binding.mapSignals.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapSignals.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapSignals.onLowMemory();
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void displaySignals(List<Signal> signals) {
        Signal signal;
        for (int i = 0; i < signals.size(); i++) {
            signal = signals.get(i);
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(signal.getLatitude(), signal.getLongitude()))
                    .title(signal.getTitle());

            markerOptions.icon(BitmapDescriptorFactory.fromResource(getDrawableFromStatus(signal.getStatus())));

            signalsGoogleMap.addMarker(markerOptions);
        }
    }

    private int getDrawableFromStatus(int status) {
        switch (status) {
            case 0:
                return R.drawable.pin_red;
            case 1:
                return R.drawable.pin_orange;
            case 2:
                return R.drawable.pin_green;
            default:
                return R.drawable.pin_red;
        }
    }

    @Override
    public void updateMapCameraPosition(double latitude, double longitude, int zoom) {
        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
        CameraUpdate cameraZoom = CameraUpdateFactory.zoomTo(zoom);

        signalsGoogleMap.moveCamera(center);
        signalsGoogleMap.animateCamera(cameraZoom);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_PERMISSIONS_REQUEST);
        } else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                handleNewLocation(location);
            }
        }
    }

    public static void showPermissionDialog(Activity activity, String permission, int permissionCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, R.string.txt_location_permission_not_granted, Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission},
                        permissionCode);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed with error code: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        actionsListener.onLocationChanged(currentLatitude, currentLongitude);
    }

    public View.OnClickListener getFabAddSignalClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onAddSignalClicked();
            }
        };
    }

    @Override
    public void showAddSignalView() {
        //TODO:
        Toast.makeText(getContext(), "Fab clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Presenter getPresenter() {
        return signalsMapPresenter;
    }
}
