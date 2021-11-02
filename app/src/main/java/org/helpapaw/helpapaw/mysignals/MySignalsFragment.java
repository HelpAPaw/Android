package org.helpapaw.helpapaw.mysignals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;

import org.helpapaw.helpapaw.R;
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
        actionsListener.onLoadMySignals();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        actionsListener.onLoadMySignals();
    }

    @Override
    public void displaySubmittedSignals(List<Signal> signals) {
        displaySignals(signals, binding.submittedSignalsListView);
    }

    @Override
    public void displayCommentedSignals(List<Signal> signals) {
        displaySignals(signals, binding.commentedSignalsListView);
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

    private void displaySignals(List<Signal> signals, ListView listView) {
        Signal[] signalsArray = new Signal[signals.size()];
        for (int i = 0; i < signals.size(); i++) {
            signalsArray[i] = signals.get(i);
        }

        customAdapter = new MySignalsCustomAdapter(getContext(), signalsArray);
        listView.setAdapter(customAdapter);
        setDynamicHeight(listView);

        binding.notifyChange();
    }

    public static void setDynamicHeight(ListView mListView) {
        ListAdapter mListAdapter = mListView.getAdapter();
        if (mListAdapter == null) {
            // when adapter is null
            return;
        }
        int height = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(mListView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        for (int i = 0; i < mListAdapter.getCount(); i++) {
            View listItem = mListAdapter.getView(i, null, mListView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            height += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = mListView.getLayoutParams();
        params.height = height + (mListView.getDividerHeight() * (mListAdapter.getCount() - 1));
        mListView.setLayoutParams(params);
        mListView.requestLayout();
    }
}
