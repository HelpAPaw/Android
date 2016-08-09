package org.helpapaw.helpapaw.authentication.register;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import org.helpapaw.helpapaw.R;

/**
 * Created by iliyan on 7/27/16
 */
public class WhyPhoneDialogFragment extends DialogFragment {

    public static WhyPhoneDialogFragment newInstance() {
        return new WhyPhoneDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.txt_why_want_phone))
                .setMessage(getString(R.string.txt_why_phone_description))
                .setPositiveButton(R.string.txt_i_see,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        }
                ).create();
    }
}
