package org.helpapaw.helpapaw.mysignals;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentMySignalsBinding;

import java.util.List;


public class MySignalsFragment extends BaseFragment implements MySignalsContract.View {

    private FragmentMySignalsBinding binding;
    private MySignalsPresenter mySignalsPresenter;
    private MySignalsContract.UserActionsListener actionsListener;
    private MySignalsCustomAdapter customAdapter;

    public static MySignalsFragment newInstance() {
        return new MySignalsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Presenter getPresenter() {
        return mySignalsPresenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_signals, container, false);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(binding.toolbar);
            ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(true);
                supportActionBar.setDisplayShowTitleEnabled(false);
                binding.toolbarTitle.setText(getString(R.string.text_my_signals));
            }
        }

        mySignalsPresenter = new MySignalsPresenter(this);
        mySignalsPresenter.setView(this);
        actionsListener = mySignalsPresenter;
        actionsListener.onOpenMySignalsScreen();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void showRegistrationRequiredAlert() {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity)
                .setTitle(R.string.txt_registration_required)
                .setMessage(R.string.txt_only_registered_users_can_see_my_signals)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openLoginScreen();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        alertBuilder.create().show();
    }

    @Override
    public void openLoginScreen() {
        Intent intent = new Intent(getContext(), AuthenticationActivity.class);
        startActivity(intent);
    }

    @Override
    public void displaySignals(List<Signal> signals) {

        Signal[] signalsArray = new Signal[signals.size()];
        for (int i = 0; i < signals.size(); i++) {
            signalsArray[i] = signals.get(i);
        }

        ListView mySignalsListView = binding.mySignalsListView;
        customAdapter = new MySignalsCustomAdapter(getContext(), signalsArray);
        mySignalsListView.setAdapter(customAdapter);
        binding.notifyChange();
    }
}
