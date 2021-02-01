package org.helpapaw.helpapaw.signaldetails;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.utils.SignalTypeCustomAdapter;

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

        signalType = getResources().getStringArray(R.array.signal_types_items);
        signalTypeSelection = new boolean[signalType.length];
        setSelection(true);

        customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalType, signalTypeSelection);
        signalTypeListView.setAdapter(customAdapter);

        selectAll.setOnClickListener(v -> {
            setSelection(true);
            customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalType, signalTypeSelection);
            signalTypeListView.setAdapter(customAdapter);
        });

        deselectAll.setOnClickListener(v -> {
            setSelection(false);
            customAdapter = new SignalTypeCustomAdapter(signalTypeListView.getContext(), signalType, signalTypeSelection);
            signalTypeListView.setAdapter(customAdapter);
        });
    }

    public void setOnFilterClickListener(OnClickListener clickListener) {
        filter.setOnClickListener(clickListener);
    }

    public boolean[] getSignalTypeSelection() {
        return signalTypeSelection;
    }

    private void setSelection(boolean isSelect){
        for (int i = 0; i < signalTypeSelection.length; i++) {
            signalTypeSelection[i] = isSelect;
        }
    }
}


