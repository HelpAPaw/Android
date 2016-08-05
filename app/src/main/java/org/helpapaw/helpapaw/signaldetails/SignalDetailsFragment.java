package org.helpapaw.helpapaw.signaldetails;


import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.BaseFragment;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.base.PresenterManager;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.databinding.FragmentSignalDetailsBinding;
import org.helpapaw.helpapaw.utils.Injection;

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

        Signal signal = null;
        if (getArguments() != null) {
            signal = getArguments().getParcelable(SIGNAL_DETAILS);
        }

        actionsListener.onInitDetailsScreen(signal);

        return binding.getRoot();
    }

    @Override
    public void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
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
        binding.txtSignalAuthor.setText(String.format(getString(R.string.txt_signal_from), signal.getAuthorName()));
        binding.txtSubmittedDate.setText(String.format(getString(R.string.txt_submitted_on), signal.getDateSubmitted()));
        Injection.getImageLoader().loadWithRoundedCorners(getContext(), signal.getPhotoUrl(), binding.imgSignalPhoto, R.drawable.ic_paw);
    }

    @Override
    public void displayComments(List<Comment> comments) {

        for (int i = 0; i < comments.size(); i++) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View inflatedCommentView = inflater.inflate(R.layout.view_comment, binding.grpComments, false);
            TextView txtCommentText = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_text);
            TextView txtCommentAuthor = (TextView) inflatedCommentView.findViewById(R.id.txt_comment_author);
            Comment comment = comments.get(i);
            txtCommentText.setText(comment.getText());
            txtCommentAuthor.setText(comment.getOwnerName());
            binding.grpComments.addView(inflatedCommentView);
        }

    }

    /* OnClick Listeners */
}
