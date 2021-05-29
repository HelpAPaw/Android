package org.helpapaw.helpapaw.signaldetails;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.authentication.AuthenticationActivity;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentSignalDetailsBinding;
import org.helpapaw.helpapaw.signalphoto.SignalPhotoActivity;
import org.helpapaw.helpapaw.signalphoto.SignalPhotoContract;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.StatusUtils;
import org.helpapaw.helpapaw.utils.Utils;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;
import java.util.List;

import static org.helpapaw.helpapaw.data.models.Comment.COMMENT_TYPE_STATUS_CHANGE;

public class SignalDetailsFragment extends BaseFragment implements SignalDetailsContract.View,
        SignalPhotoContract.Upload {

    private final static String SIGNAL_DETAILS = "signalDetails";
    private final static String TAG = SignalDetailsFragment.class.getSimpleName();

    SignalDetailsPresenter signalDetailsPresenter;
    SignalDetailsContract.UserActionsListener actionsListener;

    FragmentSignalDetailsBinding binding;

    private Signal mSignal;

    private String imageFileName;

    public SignalDetailsFragment() {
        // Required empty public constructor
    }

    public static SignalDetailsFragment newInstance(Signal signal) {
        SignalDetailsFragment fragment = new SignalDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(SIGNAL_DETAILS, signal);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_details, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            signalDetailsPresenter = new SignalDetailsPresenter(this, getFragmentManager(),this);
        } else {
            signalDetailsPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalDetailsPresenter.setView(this);
        }

        actionsListener = signalDetailsPresenter;
        setHasOptionsMenu(true);
        mSignal = null;
        if (getArguments() != null) {
            mSignal = getArguments().getParcelable(SIGNAL_DETAILS);
        }

        actionsListener.onInitDetailsScreen(mSignal);

        binding.btnAddComment.setOnClickListener(getOnAddCommentClickListener());
        binding.editComment.setOnFocusChangeListener(getOnCommentEditTextFocusChangeListener());
        binding.imgCall.setOnClickListener(getOnCallButtonClickListener());
        binding.btnCall.setOnClickListener(getOnCallButtonClickListener());
        binding.imgSignalPhoto.setOnClickListener(getOnSignalPhotoClickListener());
        binding.scrollSignalDetails.setOnBottomReachedListener(getOnBottomReachedListener());
        binding.viewSignalStatus.setStatusCallback(getStatusViewCallback());
        binding.btnChangePhoto.setOnClickListener(getOnChangeSignalPhotoClickListener());

        return binding.getRoot();
    }

    @Override
    public void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void setProgressIndicator(boolean active) {
        binding.progressComments.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    @Override
    protected Presenter getPresenter() {
        return signalDetailsPresenter;
    }

    @Override
    public void hideKeyboard() {
        super.hideKeyboard();
    }

    @Override
    public void showSignalDetails(Signal signal) {
        binding.txtSignalTitle.setText(signal.getTitle());
        binding.txtSignalAuthor.setText(signal.getAuthorName());

        String formattedDate = Utils.getInstance().getFormattedDate(signal.getDateSubmitted());
        binding.txtSubmittedDate.setText(formattedDate);
        binding.viewSignalStatus.updateStatus(signal.getStatus());

        String authorPhone = signal.getAuthorPhone();
        if (authorPhone == null || authorPhone.trim().isEmpty()) {
            binding.imgCall.setVisibility(View.GONE);
            binding.btnCall.setVisibility(View.GONE);
        } else {
            binding.imgCall.setVisibility(View.VISIBLE);
            binding.btnCall.setText(authorPhone);
            binding.btnCall.setVisibility(View.VISIBLE);
        }

        // here check if signal author is current user
        boolean anyPhotoUploaded = signalDetailsPresenter.isAnyPhotoUploaded(signal.getId());
        boolean isCurrentUserSignalAuthor = signalDetailsPresenter.getCurrentUserId().equals(signal.getAuthorId());
        if (isCurrentUserSignalAuthor && !anyPhotoUploaded) {
            binding.imgSignalPhoto.setVisibility(View.INVISIBLE);
            binding.btnChangePhoto.setVisibility(View.VISIBLE);
        } else {
            binding.imgSignalPhoto.setVisibility(View.VISIBLE);
            binding.btnChangePhoto.setVisibility(View.INVISIBLE);
        }

        int type = 0;
        try {
             type = signal.getType();
        } catch (Exception e) {
            // ignore
        }

        String[] signalTypes = getResources().getStringArray(R.array.signal_types_items);
        String signalType = "-";
        if (signalTypes.length > signal.getType()) {
            signalType = signalTypes[signal.getType()];
        }
        binding.txtSignalType.setText(String.format(getString(R.string.txt_signal_type), signalType));

        Injection.getImageLoader().loadWithRoundedCorners(getContext(), signal.getPhotoUrl(), binding.imgSignalPhoto, R.drawable.ic_paw);
    }

    @Override
    public void displayComments(List<Comment> comments) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        binding.grpComments.removeAllViews();

        for (int i = 0; i < comments.size(); i++) {
            Comment comment = comments.get(i);

            View inflatedCommentView;
            String commentText;

            if (comment.getType().equals(COMMENT_TYPE_STATUS_CHANGE)) {
                inflatedCommentView = inflater.inflate(R.layout.view_comment_status_change, binding.grpComments, false);

                // First try to get the new status code
                int newStatus = Comment.getNewStatusFromStatusChangeComment(comment);
                // Then get the string for that status
                String statusString = StatusUtils.getStatusStringForCode(newStatus);
                // Finally form the string to be displayed as comment
                commentText = String.format(getString(R.string.txt_user_changed_status_to), comment.getAuthorName(), statusString);

                // Set the icon for the new status
                ImageView imgNewStatusIcon = (ImageView) inflatedCommentView.findViewById((R.id.img_new_status_icon));
                imgNewStatusIcon.setImageResource(StatusUtils.getPinResourceForCode(newStatus));
            }
            else {
                inflatedCommentView = inflater.inflate(R.layout.view_comment, binding.grpComments, false);

                TextView txtCommentAuthor = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_author);
                txtCommentAuthor.setText(comment.getAuthorName());

                commentText = comment.getText();
            }

            // text and date elements are common for both type of comments so they are set in common code
            TextView txtCommentText = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_text);
            TextView txtCommentDate = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_date);

            txtCommentText.setText(commentText);

            String formattedDate = Utils.getInstance().getFormattedDate(comment.getDateCreated());
            txtCommentDate.setText(formattedDate);

            binding.grpComments.addView(inflatedCommentView);
        }
    }

    @Override
    public void showCommentErrorMessage() {
        binding.editComment.setError(getString(R.string.txt_error_empty_comment));
    }

    @Override
    public void clearSendCommentView() {
        binding.editComment.setError(null);
        binding.editComment.setText(null);
    }

    @Override
    public void showRegistrationRequiredAlert(int messageId) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity)
                .setTitle(R.string.txt_registration_required)
                .setMessage(messageId)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        binding.editComment.clearFocus();
                        openLoginScreen();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        hideKeyboard();
                        binding.editComment.clearFocus();
                    }
                });
        alertBuilder.create().show();
    }

    @Override
    public void openLoginScreen() {
        Intent intent = new Intent(getContext(), AuthenticationActivity.class);
        startActivity(intent);
    }

    @Override
    public void setNoCommentsTextVisibility(boolean visibility) {
        if (visibility) {
            binding.txtNoComments.setVisibility(View.VISIBLE);
            setShadowVisibility(false);
        } else {
            binding.txtNoComments.setVisibility(View.GONE);
        }
    }

    @Override
    public void showNoInternetMessage() {
        showMessage(getString(R.string.txt_no_internet));
    }

    @Override
    public void scrollToBottom() {
        binding.scrollSignalDetails.post(new Runnable() {
            @Override
            public void run() {
                binding.scrollSignalDetails.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void showStatusUpdatedMessage() {
        showMessage(getString(R.string.txt_status_updated));
    }

    @Override
    public void openNumberDialer(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_signal_details, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void closeScreenWithResult(Signal signal) {
        Intent data = new Intent();
        data.putExtra("signal", signal);
        getActivity().setResult(Activity.RESULT_OK, data);

        getActivity().finish();
    }

    @Override
    public void setShadowVisibility(boolean visibility) {
        if (visibility) {
            binding.viewShadow.animate().alpha(1);
        } else {
            binding.viewShadow.animate().alpha(0);
        }
    }

    @Override
    public void openSignalPhotoScreen() {
        Intent intent = new Intent(getContext(), SignalPhotoActivity.class);
        intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, mSignal);
        startActivity(intent);
    }

    @Override
    public void onStatusChangeRequestFinished(boolean success, int newStatus) {

        if (success) {
            actionsListener.loadCommentsForSignal(mSignal.getId());
        }

        binding.viewSignalStatus.onStatusChangeRequestFinished(success, newStatus);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                Uri takenPhotoUri = ImageUtils.getInstance().getPhotoFileUri(getContext(), getImageFileName());
                actionsListener.onSignalPhotoSelected(takenPhotoUri.getPath());
            }
        }
        else if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                saveImageFromURI(this, signalDetailsPresenter, data.getData());
            }

            else {
                // DRY!!
                File photoFile = ImageUtils.getInstance().getFromMediaUri(getContext(), getContext().getContentResolver(), data.getData());
                if (photoFile != null) {
                    actionsListener.onSignalPhotoSelected(Uri.fromFile(photoFile).getPath());
                }
            }
        }
    }

    public void onBackPressed() {
        actionsListener.onSignalDetailsClosing();
    }

    public StatusCallback getStatusViewCallback() {
        return new StatusCallback() {
            @Override
            public void onRequestStatusChange(int status) {
                actionsListener.onRequestStatusChange(status);
            }
        };
    }

    public String getImageFileName() {
        return imageFileName;
    }

    /* OnClick Listeners */
    public View.OnClickListener getOnAddCommentClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String commentText = binding.editComment.getText().toString();
                actionsListener.onAddCommentButtonClicked(commentText);
            }
        };
    }

    public View.OnClickListener getOnCallButtonClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.onCallButtonClicked();
            }
        };
    }

    public InteractiveScrollView.OnBottomReachedListener getOnBottomReachedListener() {
        return new InteractiveScrollView.OnBottomReachedListener() {
            @Override
            public void onBottomReached(boolean isBottomReached) {
                actionsListener.onBottomReached(isBottomReached);
            }
        };
    }

    public View.OnClickListener getOnSignalPhotoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsListener.onSignalPhotoClicked();
            }
        };
    }

    public View.OnClickListener getOnChangeSignalPhotoClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actionsListener.onChangeSignalPhotoClicked();
            }
        };
    }

    public View.OnFocusChangeListener getOnCommentEditTextFocusChangeListener() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    actionsListener.onTryToAddComment();
                }
            }
        };
    }
}
