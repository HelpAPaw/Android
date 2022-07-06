package org.helpapaw.helpapaw.vetclinics;

import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.databinding.InfoWindowVetClinicBinding;

import java.util.Map;

public class VetClinicsInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private Map<String, VetClinic> vetClinicsMarkers;
    private LayoutInflater inflater;

    InfoWindowVetClinicBinding binding;

    public VetClinicsInfoWindowAdapter(Map<String, VetClinic> vetClinicsMarkers, LayoutInflater inflater) {
        this.vetClinicsMarkers = vetClinicsMarkers;
        this.inflater = inflater;
        this.binding = DataBindingUtil.inflate(inflater, R.layout.info_window_vet_clinic, null, false);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        VetClinic vetClinic = vetClinicsMarkers.get(marker.getId());
        if (vetClinic != null) {
            binding.txtVetClinicName.setText(vetClinic.getName());
        }

        return binding.getRoot();
    }
}
