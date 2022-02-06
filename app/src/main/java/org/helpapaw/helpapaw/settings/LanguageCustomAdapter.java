package org.helpapaw.helpapaw.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;

public class LanguageCustomAdapter extends BaseAdapter {

    private final Context context;
    private final String[] language;

    private int currentLanguageSelection;

    public LanguageCustomAdapter(Context context, String[] signalType, int languageSelection) {
        this.context = context;
        this.language = signalType;
        this.currentLanguageSelection = languageSelection ;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return language.length;
    }

    @Override
    public Object getItem(int position) {
        return language[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder(); LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.language_item, null, true);
            holder.radioButton = convertView.findViewById(R.id.language_radiobutton);
            holder.textView = convertView.findViewById(R.id.language_item);
            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }
        holder.textView.setText(language[position]);
        if (position == currentLanguageSelection) {
            holder.radioButton.setChecked(true);
        }
        holder.radioButton.setTag(position);

        holder.radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer pos = (Integer)  holder.radioButton.getTag();
                currentLanguageSelection = position;
            }
        });
        return convertView;
    }

    public int getCurrentLanguageSelection() {
        return this.currentLanguageSelection;
    }

    public void setCurrentLanguageSelection(int currentLanguageSelection) {
        this.currentLanguageSelection = currentLanguageSelection;
    }

    private static class ViewHolder {
        protected RadioButton radioButton;
        private TextView textView;
    }
}