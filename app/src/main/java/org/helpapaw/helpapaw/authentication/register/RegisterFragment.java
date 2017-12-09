package org.helpapaw.helpapaw.authentication.register;


import android.app.DialogFragment;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentRegisterBinding;
import org.helpapaw.helpapaw.reusable.AlertDialogFragment;

public class RegisterFragment extends BaseFragment implements RegisterContract.View {

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

        binding.btnSignup.setOnClickListener(getBtnSignUpListener());
        binding.txtLogin.setOnClickListener(getTxtLoginClickListener());
        binding.txtWhyPhone.setOnClickListener(getTxtWhyPhoneClickListener());

        actionsListener.onInitRegisterScreen();

        return binding.getRoot();
    }

    @Override
    public void showMessage(String message) {
        AlertDialogFragment.showAlert("Error", message, true, this.getFragmentManager());
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
    public void closeRegistrationScreen() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet));
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

    public View.OnClickListener getTxtLoginClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onLoginButtonClicked();
            }
        };
    }

    public View.OnClickListener getBtnSignUpListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editEmail.getText().toString().trim();
                String password = binding.editPassword.getText().toString();
                String name = binding.editName.getText().toString();
                String phoneNumber = binding.editPhone.getText().toString();

                actionsListener.onRegisterButtonClicked(email, password, name, phoneNumber);
            }
        };
    }

    public View.OnClickListener getTxtWhyPhoneClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onWhyPhoneButtonClicked();
            }
        };
    }
}
