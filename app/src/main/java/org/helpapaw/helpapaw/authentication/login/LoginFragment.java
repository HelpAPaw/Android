package org.helpapaw.helpapaw.authentication.login;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.authentication.register.RegisterFragment;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentLoginBinding;
import org.helpapaw.helpapaw.reusable.AlertDialogFragment;
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity;

import java.util.Arrays;

public class LoginFragment extends BaseFragment implements LoginContract.View {

    LoginPresenter loginPresenter;
    LoginContract.UserActionsListener actionsListener;

    FragmentLoginBinding binding;

    public LoginFragment() {
        // Required empty public constructor
    }

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            loginPresenter = new LoginPresenter(this);
        } else {
            loginPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            loginPresenter.setView(this);
        }

        actionsListener = loginPresenter;

        binding.btnLogin.setOnClickListener(getBtnLoginClickListener());
        binding.btnShowRegister.setOnClickListener(getBtnShowRegisterClickListener());

        binding.btnLoginFb.setReadPermissions(Arrays.asList("email"));
        // Callback registration
        AuthenticationActivity activity = (AuthenticationActivity)getActivity();
        binding.btnLoginFb.registerCallback(activity.callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                actionsListener.onLoginFbSuccess(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                // Do nothing
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                showMessage(exception.getMessage());
            }
        });

        actionsListener.onInitLoginScreen();

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
    public void clearErrorMessages() {
        binding.editEmail.setError(null);
        binding.editPassword.setError(null);
    }

    @Override
    public void openRegisterScreen() {
        RegisterFragment registerFragment = RegisterFragment.newInstance();
        openFragment(registerFragment, true, true, true);
    }

    @Override
    public void setProgressIndicator(boolean active) {
        binding.progressLogin.setVisibility(active ? View.VISIBLE : View.GONE);
        binding.grpLogin.setVisibility(active ? View.GONE : View.VISIBLE);
    }

    @Override
    protected Presenter getPresenter() {
        return loginPresenter;
    }

    @Override
    public void hideKeyboard() {
        super.hideKeyboard();
    }

    @Override
    public void closeLoginScreen() {
        if(getActivity()!=null) {
            Intent intent = new Intent(getContext(), SignalsMapActivity.class);
            startActivity(intent);
            getActivity().finish();
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

    /* OnClick Listeners */

    public View.OnClickListener getBtnLoginClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editEmail.getText().toString().trim();
                String password = binding.editPassword.getText().toString();
                actionsListener.onLoginButtonClicked(email, password);
            }
        };
    }

    public View.OnClickListener getBtnShowRegisterClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onRegisterButtonClicked();
            }
        };
    }
}
