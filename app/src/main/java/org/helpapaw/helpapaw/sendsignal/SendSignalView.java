package org.helpapaw.helpapaw.sendsignal;

import android.content.Context;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;

/**
 * Created by iliyan on 0/29/16
 */
public class SendSignalView extends CardView {

    ImageView imgSignalPhoto;
    EditText editSignalDescription;
    EditText editAuthorPhone;
    TextView txtSignalSend;
    ProgressBar progressSendSignal;

    public SendSignalView(Context context) {
        super(context);
        initViews(context);
    }

    public SendSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public SendSignalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_send_signal, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        imgSignalPhoto = this.findViewById(R.id.img_signal_photo);
        editSignalDescription = this.findViewById(R.id.edit_signal_description);
        editAuthorPhone = this.findViewById(R.id.edit_author_phone);
        txtSignalSend = this.findViewById(R.id.txt_signal_send);
        progressSendSignal = this.findViewById(R.id.progress_send_signal);
    }

    public void setOnSignalSendClickListener(OnClickListener clickListener) {
        txtSignalSend.setOnClickListener(clickListener);
    }

    public void setOnSignalPhotoClickListener(OnClickListener clickListener) {
        imgSignalPhoto.setOnClickListener(clickListener);
    }

    public void setSignalPhoto(RoundedBitmapDrawable bitmap) {
        imgSignalPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        imgSignalPhoto.setImageDrawable(bitmap);
    }

    public String getSignalDescription() {
        return editSignalDescription.getText().toString().trim();
    }

    public String getAuthorPhone() {
        return editAuthorPhone.getText().toString().trim();
    }

    public void setAuthorPhone(String authorPhone) {
        editAuthorPhone.setText(authorPhone);
    }

    public void clearData() {
        imgSignalPhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgSignalPhoto.setImageResource(R.drawable.ic_camera);
        editSignalDescription.setText(null);
        editAuthorPhone.setText(null);
        setProgressVisibility(false);
    }

    public void setProgressVisibility(boolean visibility) {
        if (visibility) {
            progressSendSignal.setVisibility(VISIBLE);
            txtSignalSend.setVisibility(GONE);
        } else {
            txtSignalSend.setVisibility(VISIBLE);
            progressSendSignal.setVisibility(GONE);
        }
    }
}
