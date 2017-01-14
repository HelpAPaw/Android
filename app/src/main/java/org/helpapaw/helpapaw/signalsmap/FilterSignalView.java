package org.helpapaw.helpapaw.signalsmap;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;

import org.helpapaw.helpapaw.R;

/**
 * Created by Emil Ivanov on 9/12/2016.
 */

public class FilterSignalView extends CardView {

    ImageButton mImageViewEmergency;
    ImageButton mImageViewInProgressImage;
    ImageButton mImageViewSolvedImage;

    public FilterSignalView(Context context) {
        super(context);
        initViews(context);
    }

    public FilterSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public FilterSignalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_filter_signals, this);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mImageViewEmergency = (ImageButton) this.findViewById(R.id.iv_signal_emergency);
        mImageViewInProgressImage = (ImageButton) this.findViewById(R.id.iv_signal_in_progress);
        mImageViewSolvedImage = (ImageButton) this.findViewById(R.id.iv_signal_solved);
    }


    public void setOnEmergencyClickListener(OnClickListener clickListener){
        mImageViewEmergency.setOnClickListener(clickListener);
    }
    public void setOnInProgessClickListener(OnClickListener clickListener){
        mImageViewInProgressImage.setOnClickListener(clickListener);
    }
    public void setOnSolvedClickListener(OnClickListener clickListener){
        mImageViewSolvedImage.setOnClickListener(clickListener);
    }


    public void setStateActiveEmergency(){
        mImageViewEmergency.setSelected(true);
        mImageViewSolvedImage.setSelected(false);
        mImageViewInProgressImage.setSelected(false);
    }
    public void setStateActiveInProgess(){
        mImageViewEmergency.setSelected(false);
        mImageViewSolvedImage.setSelected(false);
        mImageViewInProgressImage.setSelected(true);
    }
    public void setStateActiveResolved(){
        mImageViewEmergency.setSelected(false);
        mImageViewSolvedImage.setSelected(true);
        mImageViewInProgressImage.setSelected(false);
    }
}
