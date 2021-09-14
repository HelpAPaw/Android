package org.helpapaw.helpapaw.share;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.utils.SharingUtils;

/**
 * Created by Niya on 15/09/2021.
 */
public class ShareAppDialog extends DialogFragment {

    public static final String SHARE_APP_TAG = "shareApp";

    private Context context;

    public static ShareAppDialog newInstance(Context context) {
        ShareAppDialog shareAppDialog = new ShareAppDialog();
        shareAppDialog.context = context;
        return shareAppDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.string_share_app_reminder_title);
        dialog.setMessage(R.string.string_share_app_reminder_message);

        dialog.setPositiveButton(getString(R.string.text_share), (dialog1, which) -> {
            SharingUtils.shareSupport(context);
            dismiss();
        });

        dialog.setNegativeButton(getString(R.string.txt_cancel), (dialog1, which) -> {
            dismiss();
        });

        return dialog.create();
    }

    @Override
    public void show(@NonNull FragmentManager manager, String tag) {
        if (tag != null && tag.equals(SHARE_APP_TAG)) {
            // we do not show it twice
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        } else {
            super.show(manager, tag);
        }
    }
}


