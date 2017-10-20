package org.helpapaw.helpapaw.authentication;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

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
        super.onActivityResult( requestCode, resultCode, data );
        callbackManager.onActivityResult( requestCode, resultCode, data );
    }
}
