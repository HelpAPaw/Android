package org.helpapaw.helpapaw.signalsmap;

import android.databinding.DataBindingUtil;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.databinding.InfoWindowSignalBinding;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.images.RoundedTransformation;

import java.util.Map;

/**
 * Created by iliyan on 8/2/16
 */
public class SignalInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Map<String, Signal> signalMarkers;
    private LayoutInflater inflater;
    private Marker lastShownMarker;

    InfoWindowSignalBinding binding;

    public SignalInfoWindowAdapter(Map<String, Signal> signalMarkers, LayoutInflater inflater) {
        this.signalMarkers = signalMarkers;
        this.inflater = inflater;
        this.binding = DataBindingUtil.inflate(inflater, R.layout.info_window_signal, null, false);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        Signal signal = signalMarkers.get(marker.getId());
        if (signal != null) {
            PhotoRepository photoRepository = Injection.getPhotoRepositoryInstance();
            String          photoUrl        = photoRepository.getPhotoUrl(signal.getId());

            if (lastShownMarker == null || !lastShownMarker.getId().equals(marker.getId())) {
                lastShownMarker = marker;

                binding.txtSignalTitle.setText(signal.getTitle());
                binding.txtSignalStatus.setText(getStatusString(signal.getStatus()));

                Picasso.with(inflater.getContext()).load(photoUrl).resize(200, 200)
                        .centerCrop()
                        .noFade()
                        .placeholder(R.drawable.ic_paw)
                        .transform(new RoundedTransformation(16, 0))
                        .into(binding.imgSignalPhoto, new MarkerCallback(marker));

            }
        }

        return binding.getRoot();
    }

    private class MarkerCallback implements Callback {
        Marker marker = null;

        MarkerCallback(Marker marker) {
            this.marker = marker;
        }

        @Override
        public void onError() {
            Log.e(getClass().getSimpleName(), "Error loading thumbnail!");
        }

        @Override
        public void onSuccess() {
            if (marker != null && marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            }
        }
    }

    private String getStatusString(int status) {
        switch (status) {
            case 0:
                return inflater.getContext().getString(R.string.txt_status_help_needed);
            case 1:
                return inflater.getContext().getString(R.string.txt_status_somebody_on_the_way);
            case 2:
                return inflater.getContext().getString(R.string.txt_status_solved);
            default:
                return inflater.getContext().getString(R.string.txt_status_help_needed);
        }
    }
}
