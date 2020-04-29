package org.helpapaw.helpapaw.base;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.helpapaw.helpapaw.R;

import java.util.UUID;

/**
 * Created by iliyan on 6/22/16
 */
public abstract class BaseFragment extends Fragment {

    public static final String SCREEN_ID = "screenId";

    protected String screenId;
    private boolean onSaveInstanceCalled = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            screenId = UUID.randomUUID().toString();
        } else {
            screenId = savedInstanceState.getString(SCREEN_ID);
            onSaveInstanceCalled = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SCREEN_ID, screenId);
        if (getPresenter() != null) {
            onSaveInstanceCalled = true;
            PresenterManager.getInstance().putPresenter(screenId, getPresenter());
        }
    }

    @Override
    public void onDestroy() {
        if (getPresenter() == null) {
            //no viewModel for this fragment
            super.onDestroy();
            return;
        }
        if (getActivity() != null && getActivity().isFinishing()) {
            getPresenter().clearView();
            PresenterManager.getInstance().remove(screenId);
        } else if (this.isRemoving() && !onSaveInstanceCalled) {
            // The fragment can be still in back stack even if isRemoving() is true.
            // We check onSaveInstanceCalled - if this was not called then the fragment is totally removed.
            getPresenter().clearView();
            PresenterManager.getInstance().remove(screenId);
        }
        super.onDestroy();
    }

    public String getScreenId() {
        return screenId;
    }

    protected abstract Presenter getPresenter();

    protected void openFragment(Fragment fragmentToOpen, boolean addToBackStack, boolean shouldAnimate, boolean animateBothDirections) {
        if (getActivity() != null) {
            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            if (toolbar != null) {
                toolbar.getMenu().clear();
            }
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            if (shouldAnimate) {
                if (animateBothDirections) {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left,
                            R.anim.slide_in_right, R.anim.slide_out_right);
                } else {
                    ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);
                }
            }
            ft.replace(R.id.grp_content_frame, fragmentToOpen);
            if (addToBackStack) {
                ft.addToBackStack(null);
            }

            ft.commit();
        }
    }

    protected void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
