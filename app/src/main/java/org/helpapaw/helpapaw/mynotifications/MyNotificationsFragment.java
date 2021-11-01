package org.helpapaw.helpapaw.mynotifications;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentMyNotificationsBinding;

import java.util.List;
import java.util.Map;

public class MyNotificationsFragment extends BaseFragment implements MyNotificationsContract.View {

    private FragmentMyNotificationsBinding binding;
    private MyNotificationsPresenter presenter;
    private MyNotificationsContract.UserActionsListener actionsListener;
    private MyNotificationsCustomAdapter customAdapter;

    public static MyNotificationsFragment newInstance() {
        return new MyNotificationsFragment();
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_notifications, container, false);
        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        if (appCompatActivity != null) {
            appCompatActivity.setSupportActionBar(binding.toolbar);
            ActionBar supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (supportActionBar != null) {
                supportActionBar.setDisplayHomeAsUpEnabled(true);
                supportActionBar.setDisplayShowTitleEnabled(false);
                binding.toolbarTitle.setText(getString(R.string.text_my_notifications));
            }
        }

        presenter = new MyNotificationsPresenter(this);
        presenter.setView(this);

        actionsListener = presenter;
        actionsListener.onOpenMyNotificationsScreen();

        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        actionsListener.onOpenMyNotificationsScreen();
    }

    @Override
    public void displayNotifications(List<Notification> notifications,
                                     Map<String, Signal> mapSignalsToIds) {
        displayNotifications(notifications, binding.myNotificationsListView, mapSignalsToIds);
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
    public void deleteMyNotifications() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.txt_delete_notifications_dialog);

        dialog.setPositiveButton("Delete", (dialog1, which) -> {
            presenter.onDeleteMyNotifications();
        });

        dialog.setNegativeButton("Cancel", (dialog1, which) -> {
        });

        dialog.show();
    }

    @Override
    public void setProgressVisibility(int visibility) {
        binding.progressBar.setVisibility(visibility);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_my_notifications, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btn_deleteMyNotifications) {
            actionsListener.onDeleteMyNotificationsClicked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoNotificationsToBeListed(boolean zeroNotifications) {
        setHasOptionsMenu(!zeroNotifications);
        if (zeroNotifications) {
            binding.noNotificationsMessage.setVisibility(View.VISIBLE);
        }
        else {
            binding.noNotificationsMessage.setVisibility(View.GONE);
        }
    }

    private void displayNotifications(List<Notification> notifications,
                                      ListView listView, Map<String, Signal> mapSignalsToIds) {
        Notification[] notificationsArray = new Notification[notifications.size()];
        for (int i = 0; i < notifications.size(); i++) {
            notificationsArray[i] = notifications.get(i);
        }

        customAdapter = new MyNotificationsCustomAdapter(getContext(), notificationsArray, mapSignalsToIds);
        listView.setAdapter(customAdapter);

        binding.notifyChange();
    }
}
