package org.helpapaw.helpapaw.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import com.backendless.BackendlessUser;
import com.google.android.material.snackbar.Snackbar;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentUserProfileBinding;
import org.helpapaw.helpapaw.signaldetails.DeleteSignalDialog;


public class UserProfileFragment extends BaseFragment implements UserProfileContract.View {

    UserProfilePresenter userProfilePresenter;
    UserProfileContract.UserActionsListener actionsListener;

    FragmentUserProfileBinding binding;


    public UserProfileFragment() {
        // Required empty public constructor
    }

    public static UserProfileFragment newInstance() {
        UserProfileFragment fragment = new UserProfileFragment();
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
        userProfilePresenter.onInitUserProfileScreen();

        setHasOptionsMenu(true);

        binding.btnDeleteUser.setOnClickListener(getOnDeleteUserProfileClickListener());
        binding.btnLogout.setOnClickListener(getOnLogoutClickListener());

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
    public void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showPasswordDoesNotMatchMessage() {
        binding.newPasswordConfirm.setError(getString(R.string.txt_invalid_password_confirmation));
    }

    @Override
    public void showPasswordErrorMessage() {
        binding.newPassword.setError(getString(R.string.txt_invalid_password));
    }

    @Override
    public void showUserProfile(BackendlessUser currentUser) {
        binding.userEmail.setText(currentUser.getEmail());
        binding.userName.setText(currentUser.getProperty("name").toString());

        String phoneNumber = currentUser.getProperty("phoneNumber").toString();
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            binding.userPhone.setText(phoneNumber);
        }

        binding.userName.clearFocus();
        binding.userPhone.clearFocus();
        binding.newPassword.clearFocus();
        binding.newPassword.setText("");
        binding.newPasswordConfirm.clearFocus();
        binding.newPasswordConfirm.setText("");
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void deleteUserProfile() {
        FragmentManager fm = getChildFragmentManager();

        DeleteUserProfileDialog deleteSignalDialog = DeleteUserProfileDialog.newInstance(this.userProfilePresenter);
        deleteSignalDialog.show(fm, DeleteSignalDialog.DELETE_SIGNAL_TAG);
    }

    @Override
    public void setProgressVisibility(int visibility) {
        binding.progressBar.setVisibility(visibility);
    }

    public void onFinishActivity() {
        this.getActivity().finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_saveEditUser) {
            hideKeyboard();

            String userName = binding.userName.getText().toString();
            String phone = binding.userPhone.getText().toString();
            String password = binding.newPassword.getText().toString();
            String passwordConfirm = binding.newPasswordConfirm.getText().toString();

            actionsListener.onUpdateUser(userName, phone, password, passwordConfirm);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_profile, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    public View.OnClickListener getOnDeleteUserProfileClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsListener.onDeleteUserProfileClicked();
            }
        };
    }

    public View.OnClickListener getOnLogoutClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsListener.onLogOut();
            }
        };
    }
}
