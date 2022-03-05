package org.helpapaw.helpapaw.userprofile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.R;

public class DeleteUserProfileDialog extends DialogFragment {

    public static final String DELETE_USER_PROFILE = "deleteUserProfile";

    private UserProfilePresenter presenter;

    public static DeleteUserProfileDialog newInstance(UserProfilePresenter presenter) {
        DeleteUserProfileDialog deleteSignalDialog = new DeleteUserProfileDialog();
        deleteSignalDialog.presenter = presenter;
        return deleteSignalDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.txt_delete_account_dialog);

        dialog.setPositiveButton("Delete", (dialog1, which) -> {
            presenter.onDeleteUserProfile();

            dismiss();
        });

        dialog.setNegativeButton("Cancel", (dialog1, which) -> {
            dismiss();
        });

        return dialog.create();
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        if (tag != null && tag.equals(DELETE_USER_PROFILE)) {
            // we do not show it twice
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        } else {
            super.show(manager, tag);
        }
    }
}


