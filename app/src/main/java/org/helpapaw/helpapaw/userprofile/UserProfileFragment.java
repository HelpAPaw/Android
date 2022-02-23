package org.helpapaw.helpapaw.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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

        binding.btnSaveEditUser.setOnClickListener(getOnSaveEditUserProfileClickListener());

//        binding.changePassword.setOnClickListener();

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
    public void showUserProfile(BackendlessUser currentUser) {
        binding.userEmail.setText(currentUser.getEmail());
        binding.userName.setText(currentUser.getProperty("name").toString());
        binding.userPhone.setText(currentUser.getProperty("phoneNumber").toString());
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void editUserProfile() {
        EditText txtUserName = binding.userName;
        txtUserName.setEnabled(true);
        txtUserName.requestFocus();

        EditText txtUserPhone = binding.userPhone;
        txtUserPhone.setEnabled(true);
        txtUserPhone.requestFocus();

        // place the cursor at the end of the string
        txtUserName.setSelection(txtUserName.getText().length());
        txtUserPhone.setSelection(txtUserPhone.getText().length());

        binding.btnSaveEditUser.setVisibility(View.VISIBLE);
    }

    @Override
    public void saveEditUserProfile() {
        userProfilePresenter.onUpdateUser(
                binding.userName.getText().toString(),
                binding.userPhone.getText().toString());

        endEditSignalTitleMode();
    }

    @Override
    public void deleteUserProfile() {
        FragmentManager fm = getChildFragmentManager();

        DeleteUserProfileDialog deleteSignalDialog = DeleteUserProfileDialog.newInstance(this.userProfilePresenter);
        deleteSignalDialog.show(fm, DeleteSignalDialog.DELETE_SIGNAL_TAG);
    }

    public void onUserProfileDeleted() {
        this.getActivity().finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_deleteUserProfile) {
            actionsListener.onDeleteUserProfileClicked();
            return true;
        }
        else if (item.getItemId() == R.id.btn_editUserProfile) {
            actionsListener.onEditUserProfileClicked();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_user_profile, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void endEditSignalTitleMode() {
        binding.userName.setEnabled(false);
        binding.userPhone.setEnabled(false);

        hideKeyboard();
        binding.btnSaveEditUser.setVisibility(View.GONE);
    }

    public View.OnClickListener getOnSaveEditUserProfileClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsListener.onSaveEditUserClicked();
            }
        };
    }
}
