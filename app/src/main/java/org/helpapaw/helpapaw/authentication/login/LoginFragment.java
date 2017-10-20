package org.helpapaw.helpapaw.authentication.login;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.authentication.register.RegisterFragment;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentLoginBinding;

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
        binding.txtSignup.setOnClickListener(getTxtSignUpClickListener());
        binding.btnLoginFb.setOnClickListener(getBtnLoginFbClickListener());
        binding.btnLoginFb.setReadPermissions("email");
        binding.btnLoginFb.setFragment(this);

        actionsListener.onInitLoginScreen();

        return binding.getRoot();
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
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

    public View.OnClickListener getTxtSignUpClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onRegisterButtonClicked();
            }
        };
    }

    public View.OnClickListener getBtnLoginFbClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationActivity activity = (AuthenticationActivity)getActivity();
                actionsListener.onLoginFbButtonClicked(activity, activity.callbackManager);
            }
        };
    }
}
