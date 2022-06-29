package org.helpapaw.helpapaw.vetclinics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.VetClinic;
import org.helpapaw.helpapaw.databinding.FragmentVetClinicDetailsBinding;

import java.util.HashMap;
import java.util.List;


public class VetClinicDetailsFragment extends BaseFragment
        implements VetClinicDetailsContract.View, VetClinicDetailsAsyncResponse {

    private final static String VET_CLINIC_DETAILS = "vetClinicDetails";

    VetClinicDetailsPresenter vetClinicDetailsPresenter;
    VetClinicDetailsContract.UserActionsListener actionsListener;

    FragmentVetClinicDetailsBinding binding;

    private VetClinic mVetClinic;

    public VetClinicDetailsFragment() {
        // Required empty public constructor
    }

    public static VetClinicDetailsFragment newInstance(VetClinic vetClinic) {
        VetClinicDetailsFragment fragment = new VetClinicDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(VET_CLINIC_DETAILS, vetClinic);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_vet_clinic_details, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            vetClinicDetailsPresenter = new VetClinicDetailsPresenter(this);
        } else {
            vetClinicDetailsPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            vetClinicDetailsPresenter.setView(this);
        }

        actionsListener = vetClinicDetailsPresenter;

        setHasOptionsMenu(true);
        mVetClinic = null;
        if (getArguments() != null) {
            mVetClinic = getArguments().getParcelable(VET_CLINIC_DETAILS);
        }

        actionsListener.onInitDetailsScreen(mVetClinic);

        binding.btnNavigateVet.setOnClickListener(getOnNavigateButtonClickListener());
        binding.btnCallVet.setOnClickListener(getOnCallButtonClickListener());

        return binding.getRoot();
    }

    @Override
    public void showMessage(String message) {
        super.showMessage(message);
    }

    @Override
    protected Presenter getPresenter() {
        return vetClinicDetailsPresenter;
    }

    @Override
    public void showVetClinicDetails(VetClinic vetClinic) {
        binding.txtVetClinicNameDetails.setText(vetClinic.getName());

        String vetClinicPhone = vetClinic.getPhoneNumber();
        if (vetClinicPhone == null || vetClinicPhone.trim().isEmpty()) {
            binding.btnCallVet.setVisibility(View.GONE);
        } else {
            binding.btnCallVet.setText(vetClinicPhone);
            binding.btnCallVet.setVisibility(View.VISIBLE);
        }

        StringBuilder vetClinicDetailsRequest =
                new StringBuilder(createVetClinicDetailsRequest(vetClinic.getId()));

        VetClinicDetailsTask vetClinicDetailsTask = new VetClinicDetailsTask();
        vetClinicDetailsTask.delegate = this;
        vetClinicDetailsTask.execute(vetClinicDetailsRequest.toString());
    }

    @Override
    public void openNavigation(double latitude, double longitude) {
        //https://developer.android.com/guide/components/intents-common#Maps
        //Unfortunately the below doesn't work in Waze (runs a search with the signal title)
        //final String geoIntentData = "geo:0,0?q=" + mSignal.getTitle() + "@" + latitude + "," + longitude;

        //https://stackoverflow.com/questions/25662853/android-intent-for-opening-both-waze-and-google-maps/71251419#71251419
        final String geoIntentData = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoIntentData));
        try {
            startActivity(intent);
        }
        catch (Exception ex) {
            showMessage(getString(R.string.txt_no_navigation_app));
        }
    }

    @Override
    public void openNumberDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void vetClinicDetailsLoaded(HashMap<String, String> result) {
        String phoneNumber = result.get("international_phone_number");

        if (!phoneNumber.isEmpty()) {
            mVetClinic.setPhoneNumber(phoneNumber);
            binding.btnCallVet.setText(phoneNumber);
            binding.btnCallVet.setVisibility(View.VISIBLE);
        }
    }

    public View.OnClickListener getOnNavigateButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onNavigateButtonClicked();
            }
        };
    }

    public View.OnClickListener getOnCallButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onCallButtonClicked();
            }
        };
    }

    private StringBuilder createVetClinicDetailsRequest(String id) {
        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        sb.append("place_id=" + id);
        sb.append("&key=" + getString(R.string.google_android_map_api_key_test)); // TODO - we need to change this

        return sb;
    }
}
