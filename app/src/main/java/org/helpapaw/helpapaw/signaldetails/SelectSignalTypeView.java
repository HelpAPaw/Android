package org.helpapaw.helpapaw.signaldetails;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.cardview.widget.CardView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.utils.MultiSelectionSpinner;

import java.util.Arrays;
import java.util.List;

public class SelectSignalTypeView extends CardView {

    MultiSelectionSpinner typeSpinner;
    List<String> selectedTypes;

    boolean selectionChanged;

    public SelectSignalTypeView(Context context) {
        super(context);
        initViews(context);
    }

    public SelectSignalTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public SelectSignalTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(context);
    }

    private void initViews(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_select_signal_type, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        typeSpinner = this.findViewById(R.id.multi_spinner_signal_type);
        typeSpinner.setItems(Arrays.asList(getResources().getStringArray(R.array.signal_types_items)));
        this.selectedTypes = typeSpinner.getSelectedItems();
        this.selectionChanged = typeSpinner.isSelectionChanged();
    }

    public boolean isSelectionChanged() {
        return selectionChanged;
    }
}


