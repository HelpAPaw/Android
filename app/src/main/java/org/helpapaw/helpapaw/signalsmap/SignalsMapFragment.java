package org.helpapaw.helpapaw.signalsmap;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding;
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet;
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity;
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

    private static final String DATE_TIME_FORMAT = "yyyyMMdd_HHmmss";
    private static final String PHOTO_PREFIX = "JPEG_";
    private static final String PHOTO_EXTENSION = ".jpg";

    private static final int LOCATION_PERMISSIONS_REQUEST = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_GALLERY = 3;
    private static final int READ_EXTERNAL_STORAGE_FOR_CAMERA = 4;
    private static final int READ_EXTERNAL_STORAGE_FOR_GALLERY = 5;

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
        } else {
            signalsMapPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalsMapPresenter.setView(this);
        }
        actionsListener = signalsMapPresenter;

        initLocationApi();

        binding.fabAddSignal.setOnClickListener(getFabAddSignalClickListener());
        binding.viewSendSignal.setOnSignalSendClickListener(getOnSignalSendClickListener());
        binding.viewSendSignal.setOnSignalPhotoClickListener(getOnSignalPhotoClickListener());
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

    /* Google Maps */

    private OnMapReadyCallback getMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                signalsGoogleMap = googleMap;
                actionsListener.onInitSignalsMap();
            }
        };
    }

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
        signalsGoogleMap.clear();

        Signal signal;
        Marker marker = null;

        final Map<String, Signal> signalMarkers = new HashMap<>();
        if (signalsGoogleMap != null) {
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
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showPermissionDialog(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION,
                    LOCATION_PERMISSIONS_REQUEST);
        } else {
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

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();

        actionsListener.onLocationChanged(currentLatitude, currentLongitude);
    }


    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    public View.OnClickListener getFabAddSignalClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visibility = binding.viewSendSignal.getVisibility() == View.VISIBLE;
                actionsListener.onAddSignalClicked(visibility);
            }
        };
    }

    @Override
    public void setAddSignalViewVisibility(boolean visibility) {
        if (visibility) {
            showAddSignalView();
            binding.fabAddSignal.setImageResource(R.drawable.ic_close);
        } else {
            hideAddSignalView();
            binding.fabAddSignal.setImageResource(R.drawable.fab_add);
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
        startActivity(intent);
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
    public boolean isActive() {
        return isAdded();
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
}
