package org.helpapaw.helpapaw.authentication;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.login.LoginFragment;
import org.helpapaw.helpapaw.databinding.ActivityAuthenticationBinding;

public class AuthenticationActivity extends AppCompatActivity {
    ActivityAuthenticationBinding binding;
    public CallbackManager        callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_authentication);

        callbackManager = CallbackManager.Factory.create();

        if (null == savedInstanceState) {
            initFragment(LoginFragment.newInstance());
        }
    }

    private void initFragment(Fragment loginFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.grp_content_frame, loginFragment);
        transaction.commit();
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
