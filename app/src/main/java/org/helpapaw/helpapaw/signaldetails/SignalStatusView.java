package org.helpapaw.helpapaw.signaldetails;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.helpapaw.helpapaw.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iliyan on 0/29/16
 */
public class SignalStatusView extends FrameLayout implements SignalStatusViewContract{

    FrameLayout grpStatusContainer;

    LinearLayout grpSignalNeedHelp;
    LinearLayout grpSignalOnWay;
    LinearLayout grpSignalSolved;
    ProgressBar  grpProgressBar;

    AppCompatImageView imgNeedHelpSelected;
    AppCompatImageView imgOnWaySelected;
    AppCompatImageView imgSolvedSelected;

    private int selectedStatus;
    private boolean isExpanded = false;
    private StatusCallback callback;
    private List<LinearLayout> statusList = new ArrayList<>();
    private List<AppCompatImageView> selectedImages = new ArrayList<>();


    public SignalStatusView(Context context) {
        super(context);
        initViews(context);
    }

    public SignalStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public SignalStatusView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_signal_status, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUi();
        initData();
        setStatusClickListeners(statusList);
    }

    private void initUi() {
        grpStatusContainer = (FrameLayout) this.findViewById(R.id.grp_status_container);

        grpSignalNeedHelp = (LinearLayout) this.findViewById(R.id.grp_signal_need_help);
        grpSignalOnWay = (LinearLayout) this.findViewById(R.id.grp_signal_on_way);
        grpSignalSolved = (LinearLayout) this.findViewById(R.id.grp_signal_solved);

        grpProgressBar = (ProgressBar) this.findViewById(R.id.grp_progress_bar);

        imgNeedHelpSelected = (AppCompatImageView) grpSignalNeedHelp.findViewById(R.id.img_signal_need_help_selected);
        imgOnWaySelected = (AppCompatImageView) grpSignalOnWay.findViewById(R.id.img_signal_on_way_selected);
        imgSolvedSelected = (AppCompatImageView) grpSignalSolved.findViewById(R.id.img_signal_solved_selected);
    }

    private void initData() {
        statusList.add(grpSignalNeedHelp);
        statusList.add(grpSignalOnWay);
        statusList.add(grpSignalSolved);

        selectedImages.add(imgNeedHelpSelected);
        selectedImages.add(imgOnWaySelected);
        selectedImages.add(imgSolvedSelected);
    }


    private void expandStatusView(int status) {
        for (int i = 0; i < statusList.size(); i++) {
            statusList.get(i).setClickable(false);
            statusList.get(i).setVisibility(VISIBLE);
            final int lastStatusId = statusList.size() - 1;
            if (i != lastStatusId) {
                statusList.get(i).animate().translationYBy(getPixels(TypedValue.COMPLEX_UNIT_DIP, i * 80)).alpha(1);
            } else {
                statusList.get(i).animate().translationYBy(getPixels(TypedValue.COMPLEX_UNIT_DIP, i * 80)).alpha(1).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < statusList.size(); i++) {
                            statusList.get(i).setClickable(true);
                        }
                    }
                });
            }

            if (i != status) {
                selectedImages.get(i).setImageResource(0);
            }
        }

        animateStatusViewHeight(getPixels(TypedValue.COMPLEX_UNIT_DIP, 3 * 80));
        selectedImages.get(status).setImageResource(R.drawable.ic_done);
    }

    private void collapseStatusView(final int status) {

        for (int i = 0; i < statusList.size(); i++) {
            if (i != status) {
                statusList.get(i).animate().alpha(0);
            }
        }

        grpSignalOnWay.animate().translationYBy(-getPixels(TypedValue.COMPLEX_UNIT_DIP, 80));
        grpSignalSolved.animate().translationYBy(-getPixels(TypedValue.COMPLEX_UNIT_DIP, 2 * 80)).withEndAction(new Runnable() {
            @Override
            public void run() {
                updateStatus(status);
            }
        });
        animateStatusViewHeight(getPixels(TypedValue.COMPLEX_UNIT_DIP, 80));
    }

    public void updateStatus(int status) {
        for (int i = 0; i < statusList.size(); i++) {
            if (i != status) {
                statusList.get(i).setVisibility(GONE);
            } else {
                statusList.get(i).setVisibility(VISIBLE);
            }
        }
        this.selectedStatus = status;
        selectedImages.get(status).setImageResource(R.drawable.ic_dropdown);
    }

    private void animateStatusViewHeight(int newHeight) {
        ResizeAnimation resizeAnimation = new ResizeAnimation(
                grpStatusContainer,
                grpStatusContainer.getHeight(),
                newHeight
        );
        resizeAnimation.setDuration(300);
        grpStatusContainer.startAnimation(resizeAnimation);
    }

    private boolean isExpanded() {
        return !(grpSignalSolved.getVisibility() != View.VISIBLE ||
                grpSignalOnWay.getVisibility() != VISIBLE ||
                grpSignalNeedHelp.getVisibility() != VISIBLE);
    }

    public int getPixels(int unit, float size) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return (int) TypedValue.applyDimension(unit, size, metrics);
    }

    public void setStatusCallback(StatusCallback callback) {
        this.callback = callback;
    }

    public int getSelectedStatus() {
        return this.selectedStatus;
    }

    public void setStatusClickListeners(List<LinearLayout> statusList) {
        for (int i = 0; i < statusList.size(); i++) {
            final int selectedStatusL = i;
            statusList.get(i).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (isExpanded()) {
                        if (getSelectedStatus() != selectedStatusL) {
                            if (callback != null) {
                                grpProgressBar.setVisibility(VISIBLE);

                                callback.onRequestStatusChange(selectedStatusL);
                            }
                        }
                    }
                    else {
                        expandStatusView(selectedStatusL);
                    }

                }
            });
        }
    }

    @Override
    public void onStatusChangeRequestFinished(boolean success, int newStatus) {

        grpProgressBar.setVisibility(GONE);

        if (success) {
            if (isExpanded()) {
                collapseStatusView(newStatus);
            }
            else {
                expandStatusView(newStatus);
            }
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState savedState = new SavedState(superState);

        savedState.selectedStatus = this.selectedStatus;
        savedState.isExpanded = isExpanded();

        return savedState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        this.selectedStatus = savedState.selectedStatus;
        this.isExpanded = savedState.isExpanded;

        restoreState(selectedStatus, isExpanded);
    }

    private void restoreState(int selectedStatus, boolean isExpanded) {
        if (isExpanded) {
            expandStatusView(selectedStatus);
        } else {
            updateStatus(selectedStatus);
        }
    }

    static class SavedState extends BaseSavedState {
        int selectedStatus;

        boolean isExpanded;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.selectedStatus = in.readInt();
            this.isExpanded = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.selectedStatus);
            out.writeByte((byte) (isExpanded ? 1 : 0));
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

    }
}

class ResizeAnimation extends Animation {
    private View view;
    private float toHeight;
    private float fromHeight;

    ResizeAnimation(View v, float fromHeight, float toHeight) {
        this.toHeight = toHeight;
        this.fromHeight = fromHeight;
        view = v;
        setDuration(300);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        float height =
                (toHeight - fromHeight) * interpolatedTime + fromHeight;
        ViewGroup.LayoutParams p = view.getLayoutParams();
        p.height = (int) height;
        view.requestLayout();
    }
}

interface StatusCallback {
    void onRequestStatusChange(int status);
}

interface SignalStatusViewContract {
    void onStatusChangeRequestFinished(boolean success, int newStatus);
}

