package org.helpapaw.helpapaw.mysignals;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.databinding.ActivityMySignalsBinding;

public class MySignalsActivity extends AppCompatActivity {

    ActivityMySignalsBinding binding;
    MySignalsFragment mMySignalsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_signals);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialize();
    }

    private void initialize() {
        if (mMySignalsFragment == null) {
            mMySignalsFragment = MySignalsFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.grp_content_frame, mMySignalsFragment);
            transaction.commit();
        }
    }
}
