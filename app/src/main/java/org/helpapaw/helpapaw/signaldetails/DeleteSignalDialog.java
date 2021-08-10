package org.helpapaw.helpapaw.signaldetails;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;

public class DeleteSignalDialog extends DialogFragment {

    public static final String DELETE_SIGNAL_TAG = "deleteSignal";

    private Signal signal;
    private SignalDetailsPresenter presenter;

    public static DeleteSignalDialog newInstance(Signal signal, SignalDetailsPresenter presenter) {
        DeleteSignalDialog deleteSignalDialog = new DeleteSignalDialog();
        deleteSignalDialog.signal = signal;
        deleteSignalDialog.presenter = presenter;
        return deleteSignalDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.txt_delete_signal_dialog);

        dialog.setPositiveButton("Delete", (dialog1, which) -> {
            presenter.onDeleteSignal();

            dismiss();
        });

        dialog.setNegativeButton("Cancel", (dialog1, which) -> {
            dismiss();
        });

        return dialog.create();
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        if (tag != null && tag.equals(DELETE_SIGNAL_TAG)) {
            // we do not show it twice
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        } else {
            super.show(manager, tag);
        }
    }
}


