package org.helpapaw.helpapaw.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;

public class SignalTypeCustomAdapter extends BaseAdapter {

    private final Context context;
    private final String[] signalType;

    private boolean[] currentSignalTypeSelection;
    private boolean[] signalTypeSelection;

    public SignalTypeCustomAdapter(Context context, String[] signalType, boolean[] signalTypeSelection) {
        this.context = context;
        this.signalType = signalType;
        this.signalTypeSelection = signalTypeSelection;

        this.currentSignalTypeSelection = new boolean[signalTypeSelection.length];
        for (int i = 0; i < signalTypeSelection.length; i++) {
            currentSignalTypeSelection[i] = Boolean.valueOf(signalTypeSelection[i]);
        }
    }

    @Override
    public int getViewTypeCount() {
        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return signalType.length;
    }

    @Override
    public Object getItem(int position) {
        return signalType[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.signal_type_item, null, true);
            holder.checkBox = convertView.findViewById(R.id.type_checkbox);
            holder.textView = convertView.findViewById(R.id.type_item);
            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }
        holder.textView.setText(signalType[position]);
        holder.checkBox.setChecked(currentSignalTypeSelection[position]);
        holder.checkBox.setTag(position);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer pos = (Integer)  holder.checkBox.getTag();
                currentSignalTypeSelection[pos] = !currentSignalTypeSelection[pos];
            }
        });
        return convertView;
    }

    public boolean[] getSignalTypeSelection() {
        return this.signalTypeSelection;
    }

    public void setSignalTypeSelectionToCurrent() {
        for (int i = 0; i < signalTypeSelection.length; i++) {
            signalTypeSelection[i] = Boolean.valueOf(currentSignalTypeSelection[i]);
        }
    }

    public void setCurrentSignalTypeSelectionToSelected() {
        for (int i = 0; i < signalTypeSelection.length; i++) {
            currentSignalTypeSelection[i] = Boolean.valueOf(signalTypeSelection[i]);
        }
    }

    public void refreshView(boolean[] selection) {
        for (int i = 0; i < currentSignalTypeSelection.length; i++) {
            currentSignalTypeSelection[i] = Boolean.valueOf(selection[i]);
        }

        notifyDataSetChanged();
    }

    private class ViewHolder {
        protected CheckBox checkBox;
        private TextView textView;
    }
}