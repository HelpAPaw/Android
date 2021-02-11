package org.helpapaw.helpapaw.signaldetails;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.filtersignal.SignalTypeCustomAdapter;

public class FilterSignalTypeView extends CardView {

    private ListView signalTypeListView;
    private String[] signalType;

    private boolean[] signalTypeSelection;
    private SignalTypeCustomAdapter customAdapter;

    private TextView filter;

    public FilterSignalTypeView(Context context) {
        super(context);
        initViews(context);
    }

    public FilterSignalTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    public FilterSignalTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
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

        signalTypeListView = findViewById(R.id.signal_type_list_view);
        TextView selectAll = findViewById(R.id.txt_select_all_signal_types);
        TextView deselectAll = findViewById(R.id.txt_deselect_all_signal_types);
        filter = findViewById(R.id.txt_filter_signal_types);
        TextView title = findViewById(R.id.txt_filter_signal_types_description);
        title.setText(R.string.txt_filter_signal_types_description);

        signalType = getResources().getStringArray(R.array.signal_types_items);
        signalTypeSelection = new boolean[signalType.length];
        signalTypeSelection = setSelection(true);

        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalType, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);

        selectAll.setOnClickListener(v -> {
            customAdapter.refreshView(setSelection(true));
        });

        deselectAll.setOnClickListener(v -> {
            customAdapter.refreshView(setSelection(false));
        });
    }

    @Override
    public void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if(visibility == VISIBLE) {
            customAdapter.refreshView(signalTypeSelection);
        }
    }

    public void setOnFilterClickListener(OnClickListener clickListener) {
        filter.setOnClickListener(clickListener);
    }

    public boolean[] getSignalTypeSelection() {
        customAdapter.setSignalTypeSelectionToCurrent();

        signalTypeSelection = customAdapter.getSignalTypeSelection();
        return signalTypeSelection;
    }

    private boolean[] setSelection(boolean isSelect){
        boolean[] selection = new boolean[signalTypeSelection.length];

        for (int i = 0; i < selection.length; i++) {
            selection[i] = isSelect;
        }

        return selection;
    }
}


