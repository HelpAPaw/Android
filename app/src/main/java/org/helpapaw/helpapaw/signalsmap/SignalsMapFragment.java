package org.helpapaw.helpapaw.signalsmap;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.databinding.FragmentSignalsMapBinding;

public class SignalsMapFragment extends BaseFragment {

    FragmentSignalsMapBinding binding;

    private GoogleMap signalsGoogleMap;

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
        return binding.getRoot();
    }

    private OnMapReadyCallback getMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                signalsGoogleMap = googleMap;


                //test
                CameraUpdate center =
                        CameraUpdateFactory.newLatLng(new LatLng(42.697145, 23.309241));
                CameraUpdate zoom = CameraUpdateFactory.zoomTo(13);

                signalsGoogleMap.moveCamera(center);
                signalsGoogleMap.animateCamera(zoom);
            }
        };
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
    protected Presenter getPresenter() {
        return null;
    }
}
