package org.helpapaw.helpapaw.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentUserProfileBinding;


public class UserProfileFragment extends BaseFragment implements UserProfileContract.View {

//    private final static String SIGNAL_DETAILS = "signalDetails";

    UserProfilePresenter userProfilePresenter;
    UserProfileContract.UserActionsListener actionsListener;

    FragmentUserProfileBinding binding;


    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(SIGNAL_DETAILS, signal);
//        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_profile, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            userProfilePresenter = new UserProfilePresenter(this);
        } else {
            userProfilePresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            userProfilePresenter.setView(this);
        }

        actionsListener = userProfilePresenter;

        setHasOptionsMenu(true);

        binding.userEmail.setText("niya@fjdj.bg");
        binding.userName.setText("niya");
        binding.userPhone.setText("00000");

//        binding.changePassword.setOnClickListener();
//        binding.btnDeleteAccount.setOnClickListener();
//        binding.btnSaveEditUser.setOnClickListener();

        return binding.getRoot();
    }

    @Override
    protected Presenter getPresenter() {
        return userProfilePresenter;
    }

    @Override
    public void hideKeyboard() {
        super.hideKeyboard();
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    public void onBackPressed() {
//        actionsListener.onSignalDetailsClosing();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_profile, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
