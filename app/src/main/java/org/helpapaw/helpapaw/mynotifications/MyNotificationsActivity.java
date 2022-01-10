package org.helpapaw.helpapaw.mynotifications;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityMyNotificationsBinding;

public class MyNotificationsActivity extends AppCompatActivity {

    ActivityMyNotificationsBinding binding;
    MyNotificationsFragment mMyNotificationsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_notifications);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
    }

    private void initialize() {
        if (mMyNotificationsFragment == null) {
            mMyNotificationsFragment = MyNotificationsFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.grp_content_frame, mMyNotificationsFragment);
            transaction.commit();
        }
    }
}
