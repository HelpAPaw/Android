package org.helpapaw.helpapaw.reusable;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import org.helpapaw.helpapaw.utils.SharingUtils;

/**
 * Use throughout the whole app whenever you want to alert the user about something important (e.g. an error)
 */

public class AlertDialogFragment extends DialogFragment {
    public static final String ARG_TITLE = "AlertDialog.Title";
    public static final String ARG_MESSAGE = "AlertDialog.Message";
    public static final String ARG_SHOW_SUPPORT = "AlertDialog.ShowSupport";

    public static void showAlert(String title, String message, boolean showSupportButton, FragmentManager fm) {
        DialogFragment dialog = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        args.putBoolean(ARG_SHOW_SUPPORT, showSupportButton);
        dialog.setArguments(args);
        if (fm != null) {
            dialog.show(fm, "tag");
        }
    }

    public AlertDialogFragment() {}

    @NonNull
    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        String title = args.getString(ARG_TITLE, "");
        String message = args.getString(ARG_MESSAGE, "");
        boolean showSupportButton = args.getBoolean(ARG_SHOW_SUPPORT, false);

        final FragmentActivity activity = getActivity();
        assert activity != null;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);
        if (showSupportButton) {
            alertBuilder.setNeutralButton("Contact support", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharingUtils.contactSupport(activity);
                }
            });
        }

        return alertBuilder.create();
    }
}