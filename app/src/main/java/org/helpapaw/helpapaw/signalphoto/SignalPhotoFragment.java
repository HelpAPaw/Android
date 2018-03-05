package org.helpapaw.helpapaw.signalphoto;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentSignalPhotoBinding;
import org.helpapaw.helpapaw.utils.Injection;

/**
 * Created by milen on 05/03/18.
 * Fragment to display a signal's photo
 */

public class SignalPhotoFragment extends BaseFragment implements SignalPhotoContract.View {

    private final static String SIGNAL_DETAILS = "signalDetails";

    SignalPhotoPresenter                    signalPhotoPresenter;
    SignalPhotoContract.UserActionsListener actionsListener;

    FragmentSignalPhotoBinding binding;

    public SignalPhotoFragment() {
        // Required empty public constructor
    }

    public static SignalPhotoFragment newInstance(Signal signal) {
        SignalPhotoFragment fragment = new SignalPhotoFragment();
        Bundle              bundle   = new Bundle();
        bundle.putParcelable(SIGNAL_DETAILS, signal);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_photo, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            signalPhotoPresenter = new SignalPhotoPresenter(this);
        } else {
            signalPhotoPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalPhotoPresenter.setView(this);
        }

        actionsListener = signalPhotoPresenter;
        Signal mSignal = null;
        if (getArguments() != null) {
            mSignal = getArguments().getParcelable(SIGNAL_DETAILS);
        }

        actionsListener.onInitPhotoScreen(mSignal);

        return binding.getRoot();
    }
    @Override
    protected Presenter getPresenter() {
        return signalPhotoPresenter;
    }

    @Override
    public void showSignalPhoto(Signal signal) {

        Injection.getImageLoader().load(getContext(), signal.getPhotoUrl(), binding.imgSignalPhoto, R.drawable.no_image);
    }
    public void onBackPressed() {
        getActivity().finish();
    }
}
