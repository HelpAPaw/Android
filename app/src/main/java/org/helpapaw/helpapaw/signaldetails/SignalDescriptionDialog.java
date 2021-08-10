package org.helpapaw.helpapaw.signaldetails;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;

public class SignalDescriptionDialog extends DialogFragment {

    public static final String EDIT_SIGNAL_DESCRIPTION_TAG = "editSignalDescription";
    public static final String SIGNAL_TITLE = "signalTitle";

    private Signal signal;
    private String signalTitle;
    private SignalDetailsPresenter presenter;

    public static SignalDescriptionDialog newInstance(Signal signal, SignalDetailsPresenter presenter) {
        SignalDescriptionDialog signalDescriptionDialog = new SignalDescriptionDialog();
        signalDescriptionDialog.signal = signal;
        signalDescriptionDialog.presenter = presenter;
        return signalDescriptionDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // disable screen rotation while the dialog is open
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.txt_edit_title_dialog);

        View view = getActivity().getLayoutInflater().inflate(R.layout.view_edit_signal_description, null);

        EditText editText = view.findViewById(R.id.txt_edit_signal_title);
        editText.setText(signal.getTitle());

        dialog.setView(view);

        dialog.setPositiveButton("Save", (dialog1, which) -> {
            String newTitle = editText.getText().toString();
            presenter.onUpdateTitle(newTitle);

            dismiss();
        });

        dialog.setNegativeButton("Cancel", (dialog1, which) -> {
            dismiss();
        });

        return dialog.create();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // reset the screen orientation
        this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        if (tag != null && tag.equals(EDIT_SIGNAL_DESCRIPTION_TAG)) {
            // we do not show it twice
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        } else {
            super.show(manager, tag);
        }
    }
}


