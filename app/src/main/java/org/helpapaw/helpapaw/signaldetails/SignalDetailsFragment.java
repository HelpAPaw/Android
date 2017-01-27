package org.helpapaw.helpapaw.signaldetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.SharingUtils;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;

public class SignalDetailsFragment extends BaseFragment implements SignalDetailsContract.View {

    private final static String SIGNAL_DETAILS = "signalDetails";

    SignalDetailsPresenter signalDetailsPresenter;
    SignalDetailsContract.UserActionsListener actionsListener;

    FragmentSignalDetailsBinding binding;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signal_details, container, false);

        if (savedInstanceState == null || PresenterManager.getInstance().getPresenter(getScreenId()) == null) {
            signalDetailsPresenter = new SignalDetailsPresenter(this);
        } else {
            signalDetailsPresenter = PresenterManager.getInstance().getPresenter(getScreenId());
            signalDetailsPresenter.setView(this);
        }

        actionsListener = signalDetailsPresenter;
        setHasOptionsMenu(true);
        Signal signal = null;
        if (getArguments() != null) {
            signal = getArguments().getParcelable(SIGNAL_DETAILS);
        }

        actionsListener.onInitDetailsScreen(signal);

        binding.btnAddComment.setOnClickListener(getOnAddCommentClickListener());
        binding.imgCall.setOnClickListener(getOnCallButtonClickListener());
        binding.scrollSignalDetails.setOnBottomReachedListener(getOnBottomReachedListener());
        binding.viewSignalStatus.setStatusCallback(getStatusViewCallback());

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

        if (signal.getAuthorPhone() == null) {
            binding.imgCall.setVisibility(View.GONE);
        } else {
            binding.imgCall.setVisibility(View.VISIBLE);
        }

        Injection.getImageLoader().loadWithRoundedCorners(getContext(), signal.getPhotoUrl(), binding.imgSignalPhoto, R.drawable.ic_paw);
    }

    @Override
    public void displayComments(List<Comment> comments) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < comments.size(); i++) {
            View inflatedCommentView = inflater.inflate(R.layout.view_comment, binding.grpComments, false);
            TextView txtCommentText = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_text);
            TextView txtCommentAuthor = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_author);
            TextView txtCommentDate = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_date);

            Comment comment = comments.get(i);
            txtCommentText.setText(comment.getText());
            txtCommentAuthor.setText(comment.getOwnerName());

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

        if(item.getItemId()== R.id.menu_item_share){
          if(SharingUtils.verifyPackageInstalled(getActivity(), SharingUtils.PACKAGE_FACEBOOK)) {
              startActivity(SharingUtils.shareFacebook(getActivity(), binding.txtSignalTitle.getText().toString()));
          }else{
              Snackbar.make(getView(), getString(R.string.error_facebook_not_installed), Snackbar.LENGTH_LONG).show();
          }
        }

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

    public void onBackPressed() {
        actionsListener.onSignalDetailsClosing();
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

    public StatusCallback getStatusViewCallback() {
        return new StatusCallback() {
            @Override
            public void onRequestStatusChange(int status) {
                actionsListener.onRequestStatusChange(status);
            }
        };
    }

    @Override
    public void onStatusChangeRequestFinished(boolean success, int newStatus) {
        binding.viewSignalStatus.onStatusChangeRequestFinished(success, newStatus);
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
}
