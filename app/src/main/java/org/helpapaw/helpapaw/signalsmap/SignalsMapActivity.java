package org.helpapaw.helpapaw.signalsmap;

import android.content.Intent;
import android.os.Bundle;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseActivity;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

public class SignalsMapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserManager userManager = Injection.getUserManagerInstance();
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                //TODO: Load Signals Map Fragment
            }

            @Override
            public void onLoginFailure(String message) {
                Intent intent = new Intent(SignalsMapActivity.this, AuthenticationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_base;
    }


}
