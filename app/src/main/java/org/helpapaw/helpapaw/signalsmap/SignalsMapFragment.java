package org.helpapaw.helpapaw.signalsmap;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding;
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet;
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SignalsMapFragment extends BaseFragment implements SignalsMapContract.View, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = SignalsMapFragment.class.getSimpleName();
    private static final String MAP_VIEW_STATE = "mapViewSaveState";
    private static final float DEFAULT_MAP_ZOOM = 14.5f;
    private static final String DATE_TIME_FORMAT = "yyyyMMdd_HHmmss";
    private static final String PHOTO_PREFIX = "JPEG_";
    private static final String PHOTO_EXTENSION = ".jpg";

    private static final int LOCATION_PERMISSIONS_REQUEST = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_GALLERY = 3;
    private static final int READ_EXTERNAL_STORAGE_FOR_CAMERA = 4;
    private static final int READ_EXTERNAL_STORAGE_FOR_GALLERY = 5;
    private static final int REQUEST_SIGNAL_DETAILS = 6;
    private static final int REQUEST_CHECK_SETTINGS = 214;
    private static final String VIEW_ADD_SIGNAL = "view_add_signal";
    private static final int PADDING_TOP = 190;
    private static final int PADDING_BOTTOM = 160;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private GoogleMap signalsGoogleMap;

    private double mCurrentLat;
    private double mCurrentLong;

    private SignalsMapPresenter signalsMapPresenter;
    private SignalsMapContract.UserActionsListener actionsListener;

    private boolean mMarkerAdded = false;
    FragmentSignalsMapBinding binding;
    private Menu optionsMenu;

    UserManager userManager;
    private Marker mMarker;
    private LocationRequest mLocationRequest;
    private boolean mVisibilityAddSignal = false;

    public SignalsMapFragment() {
        // Required empty public constructor
    }

    public static SignalsMapFragment newInstance() {
        return new SignalsMapFragment();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signals_map, container, false);
        userManager = Injection.getUserManagerInstance();
        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle(MAP_VIEW_STATE) : null;
        binding.mapSignals.onCreate(mapViewSavedInstanceState);

        mVisibilityAddSignal = savedInstanceState!=null ? savedInstanceState.getBoolean(VIEW_ADD_SIGNAL) : false;
        setAddSignalViewVisibility(mVisibilityAddSignal);
        if (binding.mapSignals != null) {
            binding.mapSignals.getMapAsync(getMapReadyCallback());
        }

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            signalsMapPresenter = new SignalsMapPresenter(this);
        } else {
            signalsMapPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalsMapPresenter.setView(this);
        }
        actionsListener = signalsMapPresenter;
        if(actionsListener!=null){
         //   actionsListener.onCancelSchedulerService(getContext()); //cancel if previous service is running
            actionsListener.onSetupSchedulerService(getContext());
        }
        initLocationApi();

        setHasOptionsMenu(true);

        binding.fabAddSignal.setOnClickListener(getFabAddSignalClickListener());
        binding.viewSendSignal.setOnSignalSendClickListener(getOnSignalSendClickListener());
        binding.viewSendSignal.setOnSignalPhotoClickListener(getOnSignalPhotoClickListener());

        binding.viewFilterSignals.setOnEmergencyClickListener(getOnEmergencyClickListener());
        binding.viewFilterSignals.setOnInProgessClickListener(getOnInProgressClickListener());
        binding.viewFilterSignals.setOnSolvedClickListener(getOnSolvedClickListener());

        return binding.getRoot();
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

    public void onSaveInstanceState(Bundle outState) {
        //This MUST be done before saving any of your own or your base class's variables
        final Bundle mapViewSaveState = new Bundle(outState);
        binding.mapSignals.onSaveInstanceState(mapViewSaveState);
        outState.putBundle(MAP_VIEW_STATE, mapViewSaveState);

        outState.putBoolean(VIEW_ADD_SIGNAL, mVisibilityAddSignal);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapSignals.onLowMemory();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_signals_map, menu);

        this.optionsMenu = menu;


        if(userManager.getUserToken()!=null && !userManager.getUserToken().isEmpty()){


            optionsMenu.findItem(R.id.menu_item_settings).setTitle(getString(R.string.txt_logout));
        }else{
            optionsMenu.findItem(R.id.menu_item_settings).setTitle(getString(R.string.txt_login));
        }
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_refresh) {
            actionsListener.onRefreshButtonClicked();
            return true;
        }
        if(item.getItemId() == R.id.menu_item_settings){
            Log.d(TAG,"logout clicked");
             actionsListener.onAuthenticationAction();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Google Maps */

    private OnMapReadyCallback getMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                signalsGoogleMap = googleMap;
                actionsListener.onInitSignalsMap();
                signalsGoogleMap.setPadding(0,PADDING_TOP,0,PADDING_BOTTOM);
                signalsGoogleMap.setOnMapClickListener(mapClickListener);
                signalsGoogleMap.setOnMarkerDragListener(mapDragListener);

            }
        };
    }


    private GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {

            if( binding.viewSendSignal.getVisibility() == View.VISIBLE && !mMarkerAdded ) {
                signalsGoogleMap.clear();
                signalsGoogleMap.setPadding(0,PADDING_TOP,0,PADDING_BOTTOM);
                mMarker = signalsGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));

                actionsListener.onMarkerMoved(latLng.latitude, latLng.longitude);
                mMarkerAdded = true;
            }
        }
    };

    private  GoogleMap.OnMarkerClickListener mMarkerClick = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }
    };


    private GoogleMap.OnMarkerDragListener mapDragListener = new GoogleMap.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {

        }

        @Override
        public void onMarkerDrag(Marker marker) {

        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
          double latitude =   marker.getPosition().latitude;
          double longitude =  marker.getPosition().longitude;
            if(mMarker!=null){
                mMarker.remove();
            }

            mMarker = signalsGoogleMap.addMarker(new MarkerOptions().position(marker.getPosition()).draggable(true));
            actionsListener.onMarkerMoved(latitude, longitude);
        }
    };


    @Override
    public void updateMapCameraPosition(double latitude, double longitude, float zoom) {
        CameraUpdate center =
                CameraUpdateFactory.newLatLng(new LatLng(latitude, longitude));
        CameraUpdate cameraZoom = CameraUpdateFactory.zoomTo(zoom);

        signalsGoogleMap.moveCamera(center);
        signalsGoogleMap.animateCamera(cameraZoom);
    }

    @Override
    public void displaySignals(List<Signal> signals, boolean showPopup) {

        Signal signal;
        Marker marker = null;

        final Map<String, Signal> signalMarkers = new HashMap<>();
        if (signalsGoogleMap != null) {
            signalsGoogleMap.clear();
            signalsGoogleMap.setPadding(0,PADDING_TOP,0,PADDING_BOTTOM);
            for (int i = 0; i < signals.size(); i++) {
                signal = signals.get(i);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(new LatLng(signal.getLatitude(), signal.getLongitude()))
                        .title(signal.getTitle());

                markerOptions.icon(BitmapDescriptorFactory.fromResource(getDrawableFromStatus(signal.getStatus())));

                marker = signalsGoogleMap.addMarker(markerOptions);
                signalMarkers.put(marker.getId(), signal);
            }
            SignalInfoWindowAdapter infoWindowAdapter = new SignalInfoWindowAdapter(signalMarkers, getActivity().getLayoutInflater());
            signalsGoogleMap.setInfoWindowAdapter(infoWindowAdapter);

            signalsGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    actionsListener.onSignalInfoWindowClicked(signalMarkers.get(marker.getId()));
                }
            });

            if (showPopup && marker != null) {
                marker.showInfoWindow();
            }
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

    /* Location API */

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
    public void onConnected(Bundle bundle) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(new LocationRequest());

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient,
                        builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
                final LocationSettingsStates states= locationSettingsResult.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.

                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                   getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.

                        break;
                }
            }
        });
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_PERMISSIONS_REQUEST);
        } else {
           setAddSignalViewVisibility(mVisibilityAddSignal);
            signalsGoogleMap.setMyLocationEnabled(true);
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            } else {
                handleNewLocation(location);
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
        mCurrentLat = location.getLatitude();
        mCurrentLong = location.getLongitude();

        actionsListener.onLocationChanged(mCurrentLat, mCurrentLong);
    }


    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.fabAddSignal, message, Snackbar.LENGTH_LONG).show();
    }

    public View.OnClickListener getFabAddSignalClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visibility = binding.viewSendSignal.getVisibility() == View.VISIBLE;
                signalsGoogleMap.clear();
//                signalsGoogleMap.animateCamera();
                updateMapCameraPosition(mCurrentLat,mCurrentLong, DEFAULT_MAP_ZOOM);
                actionsListener.onAddSignalClicked(visibility);
              //
            }
        };
    }

    @Override
    public void setAddSignalViewVisibility(boolean visibility) {
        if (visibility) {
            mVisibilityAddSignal=visibility;
            hideFilterSignalsView();
            showAddSignalView();
            binding.fabAddSignal.setImageResource(R.drawable.ic_close);
        } else {
            mVisibilityAddSignal=visibility;
            hideAddSignalView();
            showFilterSignalsView();
            binding.fabAddSignal.setImageResource(R.drawable.fab_add);
        }
    }

    private void showFilterSignalsView(){
        binding.viewFilterSignals.setVisibility(View.VISIBLE);
        binding.viewFilterSignals.setAlpha(0.0f);
        if(binding.viewSendSignal.getHeight()>0) {
            binding.viewFilterSignals
                    .animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(300)
                    .translationY((binding.viewSendSignal.getHeight() * 1.2f))
                    .alpha(1.0f);
            Log.d(TAG, "Height : " + binding.viewSendSignal.getHeight() + "");
        }else{
            binding.viewFilterSignals
                    .animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(300)
                    .translationY((56 * 1.2f))
                    .alpha(1.0f);
            Log.d(TAG, "Height : " + binding.viewSendSignal.getHeight() + "");
        }
    }

    private void hideFilterSignalsView(){

        if(binding.viewSendSignal.getHeight()>0) {
            binding.viewFilterSignals
                    .animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(300)
                    .translationY(-(binding.viewSendSignal.getHeight() * 1.2f)).withEndAction(new Runnable() {
                @Override
                public void run() {
                    binding.viewFilterSignals.setVisibility(View.INVISIBLE);
                    Log.d(TAG, "Height : " + binding.viewSendSignal.getHeight() + "");
                }
            });
        }else{ binding.viewFilterSignals
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(- (200 * 1.2f)).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        binding.viewFilterSignals.setVisibility(View.INVISIBLE);
                    }
                });
            Log.d(TAG,"Height : " + binding.viewSendSignal.getHeight() + "");

        }
    }


    private void showAddSignalView() {
        binding.viewSendSignal.setVisibility(View.VISIBLE);
        binding.viewSendSignal.setAlpha(0.0f);

        binding.viewSendSignal
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY((binding.viewSendSignal.getHeight() * 1.2f))
                .alpha(1.0f);
    }

    private void hideAddSignalView() {

        binding.viewSendSignal
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(300)
                .translationY(-(binding.viewSendSignal.getHeight() * 1.2f)).withEndAction(new Runnable() {
            @Override
            public void run() {
                binding.viewSendSignal.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void hideKeyboard() {
        super.hideKeyboard();
    }

    @Override
    public void showSendPhotoBottomSheet() {
        SendPhotoBottomSheet sendPhotoBottomSheet = new SendPhotoBottomSheet();
        sendPhotoBottomSheet.setListener(new SendPhotoBottomSheet.PhotoTypeSelectListener() {
            @Override
            public void onPhotoTypeSelected(@SendPhotoBottomSheet.PhotoType int photoType) {
                if (photoType == SendPhotoBottomSheet.PhotoType.CAMERA) {
                    actionsListener.onCameraOptionSelected();
                } else if (photoType == SendPhotoBottomSheet.PhotoType.GALLERY) {
                    actionsListener.onGalleryOptionSelected();
                }
            }
        });
        sendPhotoBottomSheet.show(getFragmentManager(), SendPhotoBottomSheet.TAG);
    }

    String imageFileName;

    @Override
    public void openCamera() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE_FOR_CAMERA);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                String timeStamp = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault()).format(new Date());
                imageFileName = PHOTO_PREFIX + timeStamp + PHOTO_EXTENSION;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getInstance().getPhotoFileUri(getContext(), imageFileName));
                startActivityForResult(intent, REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void openGallery() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE_FOR_GALLERY);
        } else {
            Intent intent = new Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_GALLERY);
            }
        }
    }

    @Override
    public void openLoginScreen() {
        Intent intent = new Intent(getContext(), AuthenticationActivity.class);
        startActivity(intent);
    }

    @Override
    protected Presenter getPresenter() {
        return signalsMapPresenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Uri takenPhotoUri = ImageUtils.getInstance().getPhotoFileUri(getContext(), imageFileName);
                actionsListener.onSignalPhotoSelected(takenPhotoUri.getPath());
            }
        }

        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri photoMediaUri = data.getData();
            File photoFile = ImageUtils.getInstance().getFromMediaUri(getContext(), getContext().getContentResolver(), photoMediaUri);
            actionsListener.onSignalPhotoSelected(Uri.fromFile(photoFile).getPath());
        }

        if(requestCode == REQUEST_SIGNAL_DETAILS){
            if(resultCode == Activity.RESULT_OK){
                Signal signal = data.getParcelableExtra("signal");
                actionsListener.onSignalStatusUpdated(signal);
            }
        }
    }

    @Override
    public void setThumbnailImage(String photoUri) {
        Resources res = getResources();
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(res, ImageUtils.getInstance().getRotatedBitmap(new File(photoUri)));
        drawable.setCornerRadius(10);
        binding.viewSendSignal.setSignalPhoto(drawable);
    }

    @Override
    public void clearSignalViewData() {
        binding.viewSendSignal.clearData();
    }

    @Override
    public void setSignalViewProgressVisibility(boolean visibility) {
        binding.viewSendSignal.setProgressVisibility(visibility);
    }

    @Override
    public void openSignalDetailsScreen(Signal signal) {
        Intent intent = new Intent(getContext(), SignalDetailsActivity.class);
        intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, signal);
        startActivityForResult(intent, REQUEST_SIGNAL_DETAILS);
    }

    @Override
    public void closeSignalsMapScreen() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void showDescriptionErrorMessage() {
        showMessage(getString(R.string.txt_description_required));
    }

    @Override
    public void showAddedSignalMessage() {
        showMessage(getString(R.string.txt_signal_added_successfully));
    }

    @Override
    public void showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet));
    }

    @Override
    public void setProgressVisibility(boolean visibility) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu
                    .findItem(R.id.menu_item_refresh);
            if (refreshItem != null) {
                if (visibility) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.toolbar_progress);
                    if (refreshItem.getActionView() != null) {
                        ProgressBar progressBar = (ProgressBar) refreshItem.getActionView().findViewById(R.id.toolbar_progress_bar);
                        if (progressBar != null) {
                            progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                        }
                    }
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                }
            }
        }
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void addMapMarker(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude,longitude);

        if(mMarker==null) {
            mMarker = signalsGoogleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));
            mMarkerAdded = true;
        }

    }

    @Override
    public void clearMapMarker() {
            signalsGoogleMap.clear();
            mMarkerAdded = false;
            actionsListener.onCancelAddSignal();
    }

    @Override
    public void onLogoutSuccess() {
        Snackbar.make(binding.getRoot().findViewById(R.id.fab_add_signal), R.string.txt_logout_successfully, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLogoutFailure(String message) {
        Snackbar.make(binding.getRoot().findViewById(R.id.fab_add_signal), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_FOR_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    actionsListener.onStoragePermissionForCameraGranted();
                } else {
                    // Permission Denied
                    Toast.makeText(getContext(), R.string.txt_storage_permissions_for_camera, Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            case READ_EXTERNAL_STORAGE_FOR_GALLERY:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    actionsListener.onStoragePermissionForGalleryGranted();
                } else {
                    // Permission Denied
                    Toast.makeText(getContext(), R.string.txt_storage_permissions_for_gallery, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void showPermissionDialog(Activity activity, String permission, int permissionCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, permissionCode);
        }
    }

    /* OnClick Listeners */

    public void onBackPressed() {
        actionsListener.onBackButtonPressed();
    }

    public View.OnClickListener getOnSignalSendClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String description = binding.viewSendSignal.getSignalDescription();
                mMarkerAdded = false;

                actionsListener.onSendSignalClicked(description);

            }
        };
    }


    public View.OnClickListener getOnSignalPhotoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onChoosePhotoIconClicked();
            }
        };
    }

    public View.OnClickListener getOnEmergencyClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFilterEmergency();
            }
        };
    }

    public View.OnClickListener getOnInProgressClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFilterInProgress();
            }
        };
    }

    public View.OnClickListener getOnSolvedClickListener(){
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onFilterSolved();
            }
        };
    }





}
