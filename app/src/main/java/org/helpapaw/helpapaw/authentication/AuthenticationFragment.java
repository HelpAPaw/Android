package org.helpapaw.helpapaw.authentication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;

public abstract class AuthenticationFragment extends BaseFragment {
    protected PrivacyPolicyConfirmationContract.UserResponse ppResponseListener;

    public void showPrivacyPolicyDialog(String privacyPolicy) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(Html.fromHtml(privacyPolicy))
                .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ppResponseListener.onUserAcceptedPrivacyPolicy();
                    }
                })
                .setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, final int i) {
                        ppResponseListener.onUserDeclinedPrivacyPolicy();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
