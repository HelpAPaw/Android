package org.helpapaw.helpapaw.signalsmap;

import static androidx.core.content.UnusedAppRestrictionsConstants.API_30;
import static androidx.core.content.UnusedAppRestrictionsConstants.API_30_BACKPORT;
import static androidx.core.content.UnusedAppRestrictionsConstants.API_31;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.IntentCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.databinding.DataBindingUtil;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

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
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.util.concurrent.ListenableFuture;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding;
import org.helpapaw.helpapaw.filtersignal.FilterSignalTypeDialog;
import org.helpapaw.helpapaw.photo.UploadPhotoContract;
import org.helpapaw.helpapaw.reusable.AlertDialogFragment;
import org.helpapaw.helpapaw.share.ShareAppDialog;
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.StatusUtils;
import org.helpapaw.helpapaw.utils.images.ImageUtils;
import org.helpapaw.helpapaw.vetclinics.VetClinicDetailsActivity;
import org.helpapaw.helpapaw.vetclinics.VetClinicsInfoWindowAdapter;

import static org.helpapaw.helpapaw.base.PawApplication.APP_OPEN_COUNTER;
import static org.helpapaw.helpapaw.base.PawApplication.APP_OPENINGS_TO_ASK_FOR_SHARE;
import static org.helpapaw.helpapaw.filtersignal.FilterSignalTypeDialog.EXTRA_SIGNAL_TYPE_SELECTION;
import static org.helpapaw.helpapaw.filtersignal.FilterSignalTypeDialog.FILTER_TAG;
import static org.helpapaw.helpapaw.filtersignal.FilterSignalTypeDialog.REQUEST_UPDATE_SIGNAL_TYPE_SELECTION;

public class SignalsMapFragment extends BaseFragment
        implements SignalsMapContract.View,
        UploadPhotoContract.View,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static final String TAG = SignalsMapFragment.class.getSimpleName();
    private static final String MAP_VIEW_STATE = "mapViewSaveState";

    private static final int READ_EXTERNAL_STORAGE_FOR_CAMERA = 4;
    private static final int REQUEST_SIGNAL_DETAILS = 6;
    private static final int REQUEST_HIBERNATION_EXEMPTION = 7;
    private static final int REQUEST_CHECK_SETTINGS = 214;
    private static final String VIEW_ADD_SIGNAL = "view_add_signal";
    private static final int PADDING_TOP = 190;
    private static final int PADDING_BOTTOM = 160;

    private static final int REQUEST_VET_CLINIC_DETAILS = 8;

    private GoogleApiClient googleApiClient;
    private GoogleMap signalsGoogleMap;
    private ArrayList<Signal> mDisplayedSignals = new ArrayList<>();
    private final ArrayList<Marker> mDisplayedSignalMarkers = new ArrayList<>();
    private final Map<String, Signal> mMarkerIdToSignalMap = new HashMap<>();
    private Signal mCurrentlyShownInfoWindowSignal;

    private final ArrayList<Marker> mDisplayedVetClinicsMarkers = new ArrayList<>();
    private final Map<String, VetClinic> mVetClinicsMarkers = new HashMap<>();

    private final Map<String, GoogleMap.InfoWindowAdapter> adapterMap = new HashMap<>();

    private double mCurrentLat;
    private double mCurrentLong;
    private float mZoom;

    private SignalsMapPresenter signalsMapPresenter;
    private SignalsMapContract.UserActionsListener actionsListener;
    private UploadPhotoContract.UserActionsListener uploadPhotoActionsListener;

    FragmentSignalsMapBinding binding;
    private Menu optionsMenu;

    private boolean mVisibilityAddSignal = false;
    private String mFocusedSignalId;

    private ISettingsRepository settingsRepository;

    ActivityResultLauncher<String[]> mForegroundPermissionsLauncher;
    ActivityResultLauncher<String> mBackgroundPermissionLauncher;

    public SignalsMapFragment() {
        // Required empty public constructor
    }

    public static SignalsMapFragment newInstance() {
        return new SignalsMapFragment();
    }

    public static SignalsMapFragment newInstance(String focusedSignalId) {
        SignalsMapFragment signalsMapFragment = new SignalsMapFragment();
        Bundle args = new Bundle();
        args.putString(Signal.KEY_SIGNAL_ID, focusedSignalId);
        signalsMapFragment.setArguments(args);
        return signalsMapFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        Bundle arguments = getArguments();

        if ((arguments != null) && arguments.containsKey(Signal.KEY_SIGNAL_ID)) {
            mFocusedSignalId = arguments.getString(Signal.KEY_SIGNAL_ID);
            arguments.remove(Signal.KEY_SIGNAL_ID);
        }

        settingsRepository = Injection.getSettingsRepositoryInstance();

        //Initialize location api
        initLocationApi();

        askForPermissionsIfNeeded();
    }

    private void askForPermissionsIfNeeded() {
        // First setup the result handlers
        mForegroundPermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> results) {
                        for (Boolean granted : results.values()) {
                            if (granted) {
                                googleApiClient.disconnect();
                                googleApiClient.connect();
                                return;
                            }
                        }
                        // Permission Denied
                        Toast.makeText(getContext(), R.string.txt_location_permissions_for_map, Toast.LENGTH_SHORT).show();
                    }
                });
        mBackgroundPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                result -> {
                    // Do nothing, be happy
                });

        assert getActivity() != null;
        // Then check if we have permission for foreground location
        if (   (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            && (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)   ) {
            // If we don't and the user hasn't denied the rationale before...
            if (!settingsRepository.getHasDeniedForegroundLocationRationale()) {
                // ...check if the system thinks we should show the rationale or it hasn't been shown so far
                if (   (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION))
                    || (!settingsRepository.getHasShownForegroundLocationRationale())   ) {
                    settingsRepository.setHasShownForegroundLocationRationale(true);
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.txt_permission_needed)
                            .setMessage(R.string.txt_foreground_location_rationale)
                            .setPositiveButton(R.string.txt_i_want_this_feature, (dialogInterface, i) -> askForForegroundLocationPermission())
                            .setNegativeButton(R.string.txt_no_thanks, ((dialog, which) -> settingsRepository.setHasDeniedForegroundLocationRationale(true)));
                    alertBuilder.create().show();
                }
                // If not rationale is needed - ask for foreground location directly
                else {
                    askForForegroundLocationPermission();
                }
            }
        } else {
            // If we have foreground location - check if we need background location
            // It was introduced in API 29 so only bother from there upward
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // If we already have permissions for foreground locations - ask for background
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (!settingsRepository.getHasDeniedBackgroundLocationRationale()) {
                        if (   (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                            || (!settingsRepository.getHasShownBackgroundLocationRationale())   ) {
                            settingsRepository.setHasShownBackgroundLocationRationale(true);
                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity())
                                    .setTitle(R.string.txt_permission_needed)
                                    .setMessage(R.string.txt_background_location_rationale)
                                    .setPositiveButton(R.string.txt_i_want_this_feature, (dialogInterface, i) -> askForBackgroundLocationPermission())
                                    .setNegativeButton(R.string.txt_no_thanks, ((dialog, which) -> settingsRepository.setHasDeniedBackgroundLocationRationale(true)));
                            alertBuilder.create().show();
                        }
                        else {
                            askForBackgroundLocationPermission();
                        }
                    }
                }
                else {
                    askForHibernationExemptionIfNeeded();
                }
            }
        }
    }

    private void askForForegroundLocationPermission() {
        mForegroundPermissionsLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void askForBackgroundLocationPermission() {
        mBackgroundPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    private void askForHibernationExemptionIfNeeded() {
        if (!settingsRepository.getHasShownHibernationExemptionDialog() && (getActivity() != null)) {
            //https://developer.android.com/topic/performance/app-hibernation
            ListenableFuture<Integer> future = PackageManagerCompat.getUnusedAppRestrictionsStatus(getActivity());
            future.addListener(() -> {
                try {
                    switch (future.get()) {
                        case API_30_BACKPORT:
                        case API_30:
                        case API_31:
                        {
                            askForHibernationExemption();
                        }
                    }
                }
                catch (Exception ignored) {}

            }, ContextCompat.getMainExecutor(getActivity()));
        }
    }

    private void askForHibernationExemption() {
        // If your app works primarily in the background, you can ask the user
        // to disable these restrictions. Check if you have already asked the
        // user to disable these restrictions. If not, you can show a message to
        // the user explaining why permission auto-reset or app hibernation should be
        // disabled. Then, redirect the user to the page in system settings where they
        // can disable the feature.
        assert getActivity() != null;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.txt_disable_hibernation)
                .setMessage(R.string.txt_disable_hibernation_explanation)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    // Check if fragment is still added to an activity to avoid a crash
                    if (isAdded()) {
                        Intent intent = IntentCompat.createManageUnusedAppRestrictionsIntent(PawApplication.getContext(), PawApplication.getContext().getPackageName());
                        // You must use startActivityForResult(), not startActivity(), even if you don't use the result code returned in onActivityResult().
                        startActivityForResult(intent, REQUEST_HIBERNATION_EXEMPTION);
                    }
                })
                .setNegativeButton(R.string.txt_cancel, null);
        alertBuilder.create().show();
        settingsRepository.setHasShownHibernationExemptionDialog(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signals_map, container, false);
        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle(MAP_VIEW_STATE) : null;
        binding.mapSignals.onCreate(mapViewSavedInstanceState);

        //noinspection SimplifiableConditionalExpression
        mVisibilityAddSignal = savedInstanceState != null ? savedInstanceState.getBoolean(VIEW_ADD_SIGNAL) : false;

        binding.mapSignals.getMapAsync(getMapReadyCallback());

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            signalsMapPresenter = new SignalsMapPresenter(this);

            // Init signal type selection (for filtering what signals are shown on the map)
            // Default to "all signals shown"
            String[] signalTypes = getResources().getStringArray(R.array.signal_types_items);
            if (SignalsMapPresenter.selectedSignalTypes == null) {
                SignalsMapPresenter.selectedSignalTypes = new boolean[signalTypes.length];
                Arrays.fill(SignalsMapPresenter.selectedSignalTypes, true);
            }
        } else {
            signalsMapPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalsMapPresenter.setView(this);
        }
        actionsListener = signalsMapPresenter;
        uploadPhotoActionsListener = signalsMapPresenter;

        setHasOptionsMenu(true);

        binding.fabAddSignal.setOnClickListener(getFabAddSignalClickListener());
        binding.viewSendSignal.setOnSignalSendClickListener(getOnSignalSendClickListener());
        binding.viewSendSignal.setOnSignalPhotoClickListener(getOnSignalPhotoClickListener());

        showShareAppReminderIfNeeded();
      
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.mapSignals.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapSignals.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapSignals.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        binding.mapSignals.onStop();

        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        binding.mapSignals.onDestroy();
        super.onDestroy();
        settingsRepository.clearLocationData();
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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_signals_map, menu);

        this.optionsMenu = menu;

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_refresh) {
            actionsListener.onRefreshButtonClicked();
            return true;
        }
        else if (item.getItemId() == R.id.menu_item_filter_signals) {
            actionsListener.onFilterSignalsButtonClicked();
            return true;
        }
        else if (item.getItemId() == R.id.menu_item_show_clinics) {
            actionsListener.onShowVetClinicsClicked(mCurrentLat, mCurrentLong, calculateZoomToMeters());

            if (!actionsListener.shouldShowVetClinics()) {
                setClinicsMenuButtonToShow(item);
            } else {
                setClinicsMenuButtonToHide(item);
            }

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setClinicsMenuButtonToShow(MenuItem item) {
        item.getIcon().setAlpha(255);
        item.setTitle(R.string.content_show_clinics);
    }

    private void setClinicsMenuButtonToHide(MenuItem item) {
        item.getIcon().setAlpha(130);
        item.setTitle(R.string.content_hide_clinics);
    }

    private void showShareAppReminderIfNeeded() {
        SharedPreferences prefs = this.getActivity().getSharedPreferences("HelpAPaw", Context.MODE_PRIVATE);
        int counter = prefs.getInt(APP_OPEN_COUNTER, 0);

        if (counter == APP_OPENINGS_TO_ASK_FOR_SHARE) {
            FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();

            ShareAppDialog shareAppDialog = ShareAppDialog.newInstance(this.getContext());
            shareAppDialog.show(fm, ShareAppDialog.SHARE_APP_TAG);
        }
    }
          
    public void setFocusedSignalId(String focusedSignalId) {
        mFocusedSignalId = focusedSignalId;
        actionsListener.onInitSignalsMap(mFocusedSignalId);
    }

    /* Google Maps */

    private OnMapReadyCallback getMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                signalsGoogleMap = googleMap;
                actionsListener.onInitSignalsMap(mFocusedSignalId);
                signalsGoogleMap.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM);
                signalsGoogleMap.setOnMapClickListener(mapClickListener);
                signalsGoogleMap.setOnMarkerClickListener(mapMarkerClickListener);
                signalsGoogleMap.setOnCameraIdleListener(mapCameraIdleListener);
                signalsGoogleMap.setInfoWindowAdapter(new CentralInfoWindowAdapter(adapterMap));
            }
        };
    }

    private final GoogleMap.OnMapClickListener mapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(@NonNull LatLng latLng) {

            // Clicking on the map closes any open info window
            mCurrentlyShownInfoWindowSignal = null;

            mFocusedSignalId = null;
        }
    };

    private final GoogleMap.OnMarkerClickListener mapMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            // Save the signal for the currently shown info window in case it should be reopen
            mCurrentlyShownInfoWindowSignal = mMarkerIdToSignalMap.get(marker.getId());

            mFocusedSignalId = null;
            return false;
        }
    };

    private final GoogleMap.OnCameraIdleListener mapCameraIdleListener = new GoogleMap.OnCameraIdleListener() {
        @Override
        public void onCameraIdle() {
            CameraPosition cameraPosition = signalsGoogleMap.getCameraPosition();
            LatLng cameraTarget = cameraPosition.target;
            mCurrentLong = cameraTarget.longitude;
            mCurrentLat = cameraTarget.latitude;
            mZoom = cameraPosition.zoom;
            int radius = calculateZoomToMeters();
            actionsListener.onLocationChanged(cameraTarget.latitude, cameraTarget.longitude, radius, settingsRepository.getTimeout());

            if (actionsListener.shouldShowVetClinics()) {
                actionsListener.showVetClinics(mCurrentLat, mCurrentLong, radius);
            }
        }
    };

    @Override
    public void updateMapCameraPosition(double latitude, double longitude, Float zoom) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraUpdate cameraUpdate;

        if (zoom != null) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        }
        else {
            cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
        }
        signalsGoogleMap.animateCamera(cameraUpdate);
    }

    @Override
    public void displaySignals(List<Signal> signals, boolean showPopup, String focusedSignalId,  boolean[] selectedTypes) {
        mFocusedSignalId = focusedSignalId;
        displaySignals(signals, showPopup, selectedTypes);
    }

    @Override
    public void displaySignals(List<Signal> signals, boolean showPopup, boolean[] selectedTypes) {

        Marker markerToFocus = null;
        Signal signalToFocus = null;
        Marker markerToReShow = null;

        // Add new signals and replace already present ones with fresher versions
        for (Signal newSignal : signals) {
            Signal alreadyPresent = null;
            for (Signal presentSignal : mDisplayedSignals) {
                if (newSignal.getId().equals(presentSignal.getId())) {
                    alreadyPresent = presentSignal;
                    break;
                }
            }

            if (alreadyPresent != null) {
                mDisplayedSignals.remove(alreadyPresent);
            }
            mDisplayedSignals.add(newSignal);
        }

        // Remove any deleted signals
        ArrayList<Signal> notDeletedSignals = new ArrayList<>();
        for (Signal signal : mDisplayedSignals) {
            if (!signal.getIsDeleted()) {
                notDeletedSignals.add(signal);
            }
        }
        mDisplayedSignals = notDeletedSignals;

        // Remove signals that are not included in current filter
        if (selectedTypes != null) {
            ArrayList<Signal> filteredSignals = new ArrayList<>();
            for (Signal signal : mDisplayedSignals) {
                // Protection for older app versions when new signal types are available in backend
                if (signal.getType() > selectedTypes.length) continue;

                boolean signalTypeIsInFilter = selectedTypes[signal.getType()];
                // Only add signal if its type is in current filter OR if it is focused
                if (signalTypeIsInFilter || (signal.getId().equals(mFocusedSignalId))) {
                    filteredSignals.add(signal);
                }
            }
            mDisplayedSignals = filteredSignals;
        }

        if (signalsGoogleMap != null) {
            // Clear only signal markers
            for (int i = 0; i < mDisplayedSignalMarkers.size(); i++) {
                mDisplayedSignalMarkers.get(i).remove();
            }
            mDisplayedSignalMarkers.clear();

            signalsGoogleMap.setPadding(0, PADDING_TOP, 0, PADDING_BOTTOM);
            for (int i = 0; i < mDisplayedSignals.size(); i++) {
                Signal signal = mDisplayedSignals.get(i);

                Marker marker = addMarkerToMap(signal);
                SignalInfoWindowAdapter signalInfoWindowAdapter = new SignalInfoWindowAdapter(mMarkerIdToSignalMap, getActivity().getLayoutInflater());
                adapterMap.put(marker.getId(), signalInfoWindowAdapter);

                if (mFocusedSignalId != null) {
                    if (signal.getId().equalsIgnoreCase(mFocusedSignalId)) {
                        showPopup = true;
                        markerToFocus = marker;
                        signalToFocus = signal;
                    }
                }
                // If an info window was open before signals refresh - reopen it
                if (mCurrentlyShownInfoWindowSignal != null) {
                    if (signal.getId().equalsIgnoreCase(mCurrentlyShownInfoWindowSignal.getId())) {
                        markerToReShow = marker;
                    }
                }
            }


            signalsGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(@NonNull Marker marker) {
                    String markerId = marker.getId();

                    Signal signal = mMarkerIdToSignalMap.get(markerId);
                    VetClinic vetClinic = mVetClinicsMarkers.get(markerId);
                    if (signal != null) {
                        actionsListener.onSignalInfoWindowClicked(signal);
                    } else if (vetClinic != null) {
                        actionsListener.onVetClinicInfoWindowClicked(vetClinic);
                    }
                }
            });

            if (showPopup && (markerToFocus != null)) {
                markerToFocus.showInfoWindow();
                updateMapCameraPosition(signalToFocus.getLatitude(), signalToFocus.getLongitude(), null);
            } else
                if (markerToReShow != null) {
                markerToReShow.showInfoWindow();
            }
        }
    }

    @Override
    public void showVetClinicsOnMap(List<VetClinic> vetClinics) {
        for (int i = 0; i < vetClinics.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            VetClinic current = vetClinics.get(i);
            double lat = current.getLatitude();
            double lng = current.getLongitude();
            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.title(current.getName());
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_vet_clinic));

            Marker vetMarker = signalsGoogleMap.addMarker(markerOptions);

            mVetClinicsMarkers.put(vetMarker.getId(), current);
            mDisplayedVetClinicsMarkers.add(vetMarker);

            VetClinicsInfoWindowAdapter vetInfoWindowAdapter = new VetClinicsInfoWindowAdapter(mVetClinicsMarkers, getActivity().getLayoutInflater());
            adapterMap.put(vetMarker.getId(), vetInfoWindowAdapter);
        }
    }

    @Override
    public void hideVetClinicsFromMap() {
        for (int i = 0; i < mDisplayedVetClinicsMarkers.size(); i++) {
            mDisplayedVetClinicsMarkers.get(i).remove();
        }
    }

    @NonNull
    private Marker addMarkerToMap(Signal signal) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(signal.getLatitude(), signal.getLongitude()))
                .title(signal.getTitle());

        markerOptions.icon(BitmapDescriptorFactory.fromResource(StatusUtils.getPinResourceForCode(signal.getStatus())));

        Marker marker = signalsGoogleMap.addMarker(markerOptions);
        mMarkerIdToSignalMap.put(marker.getId(), signal);
        mDisplayedSignalMarkers.add(marker);
        return marker;
    }

    /* Location API */

    @Override
    public void onLocationChanged(@NonNull Location location) {
        handleNewLocation(location);
    }

    private void initLocationApi() {
        googleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Create the LocationRequest object
        // 30 seconds, in milliseconds
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(30 * 1000)        // 30 seconds, in milliseconds
                .setFastestInterval(10 * 1000); // 10 seconds, in milliseconds
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                final Status status = locationSettingsResult.getStatus();
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
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (Exception e) {
                            Injection.getCrashLogger().recordException(e);
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
        Context cont = getContext();
        //Protection for the case when activity is destroyed (e.g. when rotating)
        //Probably there is a better fix in the actual workflow but we need a quick fix as users experience a lot of crashes
        if (cont == null) {
            Injection.getCrashLogger().recordException(new Throwable("Context is null, exiting..."));
            return;
        }
        if (   (ContextCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            || (ContextCompat.checkSelfPermission(cont, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)   ) {
            setAddSignalViewVisibility(mVisibilityAddSignal);
            if (signalsGoogleMap != null) {
                signalsGoogleMap.setMyLocationEnabled(true);
            }
            setLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    private void setLastLocation() {
        if (!mVisibilityAddSignal) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (location != null) {
                handleNewLocation(location);
            }
        }
    }

    private void handleNewLocation(Location location) {
        double longitude = settingsRepository.getLastShownLongitude();
        double latitude = settingsRepository.getLastShownLatitude();
        float newZoom = settingsRepository.getLastShownZoom();

        mCurrentLat = latitude == 0 ? location.getLatitude() : latitude;
        mCurrentLong = longitude == 0 ? location.getLongitude() : longitude;
        float zoom = newZoom == 0 ? calculateMetersToZoom() : newZoom;
        updateMapCameraPosition(mCurrentLat, mCurrentLong, zoom);
        actionsListener.onLocationChanged(mCurrentLat, mCurrentLong, settingsRepository.getRadius(), settingsRepository.getTimeout());

        Injection.getPushNotificationsRepositoryInstance().updateDeviceInfoInCloud(
                location,
                settingsRepository.getRadius(),
                settingsRepository.getTimeout(),
                settingsRepository.getSignalTypes());
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

    private int calculateZoomToMeters() {
        VisibleRegion visibleRegion = signalsGoogleMap.getProjection().getVisibleRegion();
        float[] distanceWidth = new float[1];
        float[] distanceHeight = new float[1];

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        //calculate the distance width (left <-> right of map on screen)
        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth);

        //calculate the distance height (top <-> bottom of map on screen)
        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight);

        //visible radius is (smaller distance) / 2:
        float radius = (distanceWidth[0] < distanceHeight[0]) ? distanceWidth[0] / 2 : distanceHeight[0] / 2;
        return ((int) radius);
    }

    private float calculateMetersToZoom() {
        double radius = settingsRepository.getRadius() * 1000;
        double scale = radius / 500;
        float zoomLevel = (float) (16 - Math.log(scale) / Math.log(2));
        return zoomLevel - 0.5f;
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.fabAddSignal, message, Snackbar.LENGTH_LONG).show();
    }

    public View.OnClickListener getFabAddSignalClickListener() {
        return v -> {
            boolean visibility = binding.viewSendSignal.getVisibility() == View.VISIBLE;
            actionsListener.onAddSignalClicked(visibility);
        };
    }

    @Override
    public void setAddSignalViewVisibility(boolean visibility) {

        mVisibilityAddSignal = visibility;

        if (visibility) {
            showAddSignalView();
            showAddSignalPin();

            binding.fabAddSignal.setImageResource(R.drawable.ic_close);
        } else {
            hideAddSignalView();
            hideAddSignalPin();

            binding.fabAddSignal.setImageResource(R.drawable.fab_add);
        }
    }

    @Override
    public void setFilterSignalViewVisibility(boolean visibility) {
        if (visibility) {
            showFilterSignalView();

            hideAddSignalView();
            hideAddSignalPin();
            binding.fabAddSignal.setImageResource(R.drawable.fab_add);
        }
    }

    @Override
    public void setActiveFilterTextVisibility(boolean visibility) {
        if (visibility) {
            showActiveFilterText();
        } else {
            hideActiveFilterText();
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
                .translationY(-(binding.viewSendSignal.getHeight() * 1.2f))
                .withEndAction(() -> binding.viewSendSignal.setVisibility(View.INVISIBLE));
    }

    private void showAddSignalPin() {
        binding.addSignalPin.setVisibility(View.VISIBLE);
        binding.addSignalPin.setAlpha(0.0f);

        binding.addSignalPin
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .translationY(0)
                .alpha(1.0f);
    }

    private void hideAddSignalPin() {

        binding.addSignalPin
                .animate()
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(200)
                .alpha(0.0f)
                .withEndAction(() -> binding.addSignalPin.setVisibility(View.INVISIBLE));
    }

    private void showFilterSignalView() {
        FragmentManager fm = getChildFragmentManager();

        FilterSignalTypeDialog filterSignalTypeDialog = FilterSignalTypeDialog.newInstance(SignalsMapPresenter.selectedSignalTypes);
        filterSignalTypeDialog.show(fm, FILTER_TAG);
    }

    private void showActiveFilterText() {
        binding.txtActiveFilter.setVisibility(View.VISIBLE);
    }

    private void hideActiveFilterText() {
        binding.txtActiveFilter.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideKeyboard() {
        super.hideKeyboard();
    }

    @Override
    public Fragment getFragment() {
        return this;
    }

    @Override
    public void showRegistrationRequiredAlert() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity)
                .setTitle(R.string.txt_registration_required)
                .setMessage(R.string.txt_only_registered_users_can_submit_signals)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> openLoginScreen())
                .setNegativeButton(android.R.string.cancel, null);
        alertBuilder.create().show();
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
                // null because the location where the camera saves the photo is kept in the presenter
                uploadPhotoActionsListener.onSignalPhotoSelected(null);
            }
        }
        else if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                saveImageFromUri(uploadPhotoActionsListener, data.getData());
            }

            else {
                File photoFile = ImageUtils.getInstance().getFileFromMediaUri(getContext(), getContext().getContentResolver(), data.getData());
                if (photoFile != null) {
                    uploadPhotoActionsListener.onSignalPhotoSelected(photoFile);
                }
            }

        }
        else if (requestCode == REQUEST_SIGNAL_DETAILS) {
            if (resultCode == Activity.RESULT_OK) {
                Signal signal = data.getParcelableExtra("signal");
                if (signal != null) {
                    actionsListener.onSignalUpdated(signal);
                }
            }
        }
        else if (requestCode == REQUEST_UPDATE_SIGNAL_TYPE_SELECTION) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    boolean[] signalTypeSelection = data.getBooleanArrayExtra(EXTRA_SIGNAL_TYPE_SELECTION);
                    if (signalTypeSelection != null) {
                        actionsListener.onFilterSignalsClicked(signalTypeSelection);
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @Override
    public void setThumbnailImage(File photoFile) {
        Resources res = getResources();
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(res, ImageUtils.getInstance().getRotatedBitmap(photoFile));
        drawable.setCornerRadius(10);
        binding.viewSendSignal.setSignalPhoto(drawable);
    }

    @Override
    public void setAuthorPhone(String phoneNumber) {
        binding.viewSendSignal.setAuthorPhone(phoneNumber);
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

        settingsRepository.setLastShownLatitude(mCurrentLat);
        settingsRepository.setLastShownLongitude(mCurrentLong);
        settingsRepository.setLastShownZoom(mZoom);
    }

    @Override
    public void openVetClinicDetailsScreen (VetClinic vetClinic) {
        Intent intent = new Intent(getContext(), VetClinicDetailsActivity.class);
        intent.putExtra(VetClinicDetailsActivity.VET_CLINIC_KEY, vetClinic);
        startActivityForResult(intent, REQUEST_VET_CLINIC_DETAILS);

        settingsRepository.setLastShownLatitude(mCurrentLat);
        settingsRepository.setLastShownLongitude(mCurrentLong);
        settingsRepository.setLastShownZoom(mZoom);
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
        super.showNoInternetMessage();
    }

    @Override
    public void setSignalsMenuButtonRefreshingStatus(boolean isRefreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu.findItem(R.id.menu_item_refresh);
            setMenuItemRefreshingStatus(refreshItem, isRefreshing);
        }
    }

    @Override
    public void setClinicsMenuButtonRefreshingStatus(boolean isRefreshing) {
        if (optionsMenu != null) {
            final MenuItem clinicsItem = optionsMenu.findItem(R.id.menu_item_show_clinics);
            setMenuItemRefreshingStatus(clinicsItem, isRefreshing);
        }
    }

    private void setMenuItemRefreshingStatus(MenuItem menuItem, boolean isRefreshing) {
        if (menuItem != null) {
            if (isRefreshing) {
                MenuItemCompat.setActionView(menuItem, R.layout.toolbar_progress);
                if (menuItem.getActionView() != null) {
                    ProgressBar progressBar = menuItem.getActionView().findViewById(R.id.toolbar_progress_bar);
                    if (progressBar != null) {
                        progressBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
                    }
                }
            } else {
                MenuItemCompat.setActionView(menuItem, null);
            }
        }
    }

    @Override
    public void setClinicsMenuButtonToShow() {
        if (optionsMenu != null) {
            final MenuItem clinicsItem = optionsMenu.findItem(R.id.menu_item_show_clinics);
            if (clinicsItem != null) {
                setClinicsMenuButtonToShow(clinicsItem);
            }
        }
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void onLogoutSuccess() {
        Snackbar.make(binding.getRoot().findViewById(R.id.fab_add_signal), R.string.txt_logout_succeeded, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLogoutFailure(String message) {
        AlertDialogFragment.showAlert(getString(R.string.txt_logout_failed), message, true, this.getFragmentManager());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_FOR_CAMERA:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    uploadPhotoActionsListener.onCameraOptionSelected();
                } else {
                    // Permission Denied
                    Toast.makeText(getContext(), R.string.txt_storage_permissions_for_camera, Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            case READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    uploadPhotoActionsListener.onGalleryOptionSelected();
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

    /* OnClick Listeners */

    public void onBackPressed() {
        actionsListener.onBackButtonPressed();
    }

    public View.OnClickListener getOnSignalSendClickListener() {
        return v -> {
            String description = binding.viewSendSignal.getSignalDescription();
            String authorPhone = binding.viewSendSignal.getAuthorPhone();
            int type = binding.viewSendSignal.getSignalType();

            actionsListener.onSendSignalClicked(description, authorPhone, type);
        };
    }

    public View.OnClickListener getOnSignalPhotoClickListener() {
        return v -> actionsListener.onChoosePhotoIconClicked();
    }
}