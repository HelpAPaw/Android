package org.helpapaw.helpapaw.sendsignal;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.IntDef;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import android.view.View;
import android.widget.LinearLayout;

import org.helpapaw.helpapaw.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by iliyan on 7/31/16
 */
public class SendPhotoBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetBehavior behavior;

    public final static String TAG = SendPhotoBottomSheet.class.getSimpleName();

    @IntDef({PhotoType.CAMERA, PhotoType.GALLERY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PhotoType {
        int CAMERA = 1;
        int GALLERY = 2;
    }

    PhotoTypeSelectListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getContext(), R.layout.bottom_sheet_send_photo, null);

        LinearLayout grpCameraOption = (LinearLayout) view.findViewById(R.id.grp_camera_option);
        LinearLayout grpGalleryOption = (LinearLayout) view.findViewById(R.id.grp_gallery_option);

        dialog.setContentView(view);
        behavior = BottomSheetBehavior.from((View) view.getParent());

        if (listener != null) {
            grpCameraOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    listener.onPhotoTypeSelected(PhotoType.CAMERA);
                }
            });

            grpGalleryOption.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    listener.onPhotoTypeSelected(PhotoType.GALLERY);
                }
            });
        }

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void setListener(PhotoTypeSelectListener listener) {
        this.listener = listener;
    }

    public interface PhotoTypeSelectListener {
        void onPhotoTypeSelected(@PhotoType int photoType);
    }
}
