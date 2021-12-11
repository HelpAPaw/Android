package org.helpapaw.helpapaw.mysignals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentMySignalsBinding;

import java.util.List;

public class MySubmittedSignalsFragment extends BaseFragment implements MySignalsContract.View {

    private FragmentMySignalsBinding binding;
    private MySubmittedSignalsPresenter presenter;
    private MySignalsContract.UserActionsListener actionsListener;
    private MySignalsCustomAdapter customAdapter;


    public MySubmittedSignalsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Presenter getPresenter() {
        return presenter;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_signals, container, false);

        presenter = new MySubmittedSignalsPresenter(this);
        presenter.setView(this);

        actionsListener = presenter;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        actionsListener.onViewResume();
    }

    @Override
    public void displaySignals(List<Signal> signals) {
        displaySignals(signals, binding.signalsListView);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet));
    }

    @Override
    public void setProgressVisibility(int visibility) {
        binding.progressBar.setVisibility(visibility);
    }

    @Override
    public void onNoSignalsToBeListed(boolean zeroSignals) {
        setHasOptionsMenu(!zeroSignals);
        if (zeroSignals) {
            binding.noSignalsMessage.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
        else {
            binding.noSignalsMessage.setVisibility(View.GONE);
        }
    }

    private void displaySignals(List<Signal> signals, ListView listView) {
        Signal[] signalsArray = new Signal[signals.size()];
        for (int i = 0; i < signals.size(); i++) {
            signalsArray[i] = signals.get(i);
        }

        customAdapter = new MySignalsCustomAdapter(getContext(), signalsArray);
        listView.setAdapter(customAdapter);

        binding.notifyChange();
    }
}
