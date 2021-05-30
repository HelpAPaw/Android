package org.helpapaw.helpapaw.authentication.register;


import android.app.DialogFragment;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentRegisterBinding;
import org.helpapaw.helpapaw.reusable.AlertDialogFragment;

public class RegisterFragment extends AuthenticationFragment implements RegisterContract.View {

    RegisterPresenter registerPresenter;
    RegisterContract.UserActionsListener actionsListener;

    FragmentRegisterBinding binding;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            registerPresenter = new RegisterPresenter(this);
        } else {
            registerPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            registerPresenter.setView(this);
        }

        actionsListener = registerPresenter;
        ppResponseListener = registerPresenter;

        binding.btnSignup.setOnClickListener(getBtnSignUpListener());
        binding.btnShowLogin.setOnClickListener(getBtnShowLoginClickListener());
        binding.txtWhyPhone.setOnClickListener(getTxtWhyPhoneClickListener());

        actionsListener.onInitRegisterScreen();

        return binding.getRoot();
    }

    @Override
    public void showErrorMessage(String message) {
        AlertDialogFragment.showAlert(getString(R.string.txt_error), message, true, this.getFragmentManager());
    }

    @Override
    public void showEmailErrorMessage() {
        binding.editEmail.setError(getString(R.string.txt_invalid_email));
    }

    @Override
    public void showPasswordErrorMessage() {
        binding.editPassword.setError(getString(R.string.txt_invalid_password));
    }

    @Override
    public void showPasswordConfirmationErrorMessage() {
        binding.editPasswordConfirmation.setError(getString(R.string.txt_invalid_password_confirmation));
    }

    @Override
    public void showNameErrorMessage() {
        binding.editName.setError(getString(R.string.txt_name_required));
    }

    @Override
    public void showWhyPhoneDialog() {
        DialogFragment whyPhoneDialogFragment = WhyPhoneDialogFragment.newInstance();
        whyPhoneDialogFragment.show(getActivity().getFragmentManager(), whyPhoneDialogFragment.getTag());
    }

    @Override
    public void clearErrorMessages() {
        binding.editEmail.setError(null);
        binding.editPassword.setError(null);
    }

    @Override
    public void showRegistrationSuccessfulMessage() {
        AlertDialogFragment.showAlert(getString(R.string.txt_success), getString(R.string.txt_registration_successful), false, this.getFragmentManager());
    }

    @Override
    public void closeRegistrationScreen() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void showNoInternetMessage() {
        showErrorMessage(getString(R.string.txt_no_internet));
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void hideKeyboard() {
        super.hideKeyboard();
    }

    @Override
    public void setProgressIndicator(boolean active) {
        binding.progressRegister.setVisibility(active ? View.VISIBLE : View.GONE);
        binding.grpRegister.setVisibility(active ? View.GONE : View.VISIBLE);
    }

    @Override
    protected Presenter getPresenter() {
        return registerPresenter;
    }

    /* OnClick Listeners */

    public View.OnClickListener getBtnShowLoginClickListener() {
        return v -> actionsListener.onLoginButtonClicked();
    }

    public View.OnClickListener getBtnSignUpListener() {
        return v -> {
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString();
            String passwordConfirmation = binding.editPasswordConfirmation.getText().toString();
            String name = binding.editName.getText().toString();
            String phoneNumber = binding.editPhone.getText().toString();

            actionsListener.onRegisterButtonClicked(email, password, passwordConfirmation, name, phoneNumber);
        };
    }

    public View.OnClickListener getTxtWhyPhoneClickListener() {
        return v -> actionsListener.onWhyPhoneButtonClicked();
    }
}
