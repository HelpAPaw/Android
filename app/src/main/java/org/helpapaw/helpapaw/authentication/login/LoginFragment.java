package org.helpapaw.helpapaw.authentication.login;

import android.app.AlertDialog;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.authentication.AuthenticationFragment;
import org.helpapaw.helpapaw.authentication.register.RegisterFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.databinding.FragmentLoginBinding;
import org.helpapaw.helpapaw.reusable.AlertDialogFragment;
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity;

import java.util.Arrays;

public class LoginFragment extends AuthenticationFragment implements LoginContract.View {

    public static int RC_SIGN_IN_GOOGLE = 123;

    LoginPresenter loginPresenter;
    LoginContract.UserActionsListener actionsListener;
    FragmentLoginBinding binding;
    GoogleSignInClient mGoogleSignInClient;

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
        ppResponseListener = loginPresenter;

        binding.btnLogin.setOnClickListener(getBtnLoginClickListener());
        binding.btnShowRegister.setOnClickListener(getBtnShowRegisterClickListener());
        binding.btnForgotPassword.setOnClickListener(getBtnForgotPasswordClickListener());

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
                showErrorMessage(exception.getMessage());
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getResources().getString(R.string.google_oauth_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);
        binding.btnSignInGoogle.setOnClickListener(getBtnSignInWithGoogleClickListener());

        actionsListener.onInitLoginScreen();

        return binding.getRoot();
    }

    @Override
    public void showErrorMessage(String message) {
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
        if (getActivity() != null) {
            Toast.makeText(getActivity(), R.string.txt_login_successful, Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public void showNoInternetMessage() {
        super.showNoInternetMessage();
    }

    @Override
    public void showPasswordResetConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.txt_confirm_password_reset))
                .setPositiveButton(R.string.txt_yes, (dialogInterface, i) -> actionsListener.onPasswordResetRequested(binding.editEmail.getText().toString().trim()))
                .setNegativeButton(R.string.txt_no, (dialogInterface, i) -> {})
                .show();
    }

    @Override
    public void showPasswordResetSuccessfulMessage() {
        AlertDialogFragment.showAlert(getString(R.string.txt_success), getString(R.string.txt_password_reset_successful), false, this.getFragmentManager());
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    /* OnClick Listeners */

    public View.OnClickListener getBtnLoginClickListener() {
        return v -> {
            String email = binding.editEmail.getText().toString().trim();
            String password = binding.editPassword.getText().toString();
            actionsListener.onLoginButtonClicked(email, password);
        };
    }

    public View.OnClickListener getBtnShowRegisterClickListener() {
        return v -> actionsListener.onRegisterButtonClicked();
    }

    public View.OnClickListener getBtnForgotPasswordClickListener() {
        return v -> actionsListener.onForgotPasswordButtonClicked();
    }

    public View.OnClickListener getBtnSignInWithGoogleClickListener() {
        return v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            actionsListener.handleSignInWithGoogleResult(task);
        }
    }
}
