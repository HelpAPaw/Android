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
    private final String[] languages;

    private int currentLanguageSelection;

    public LanguageCustomAdapter(Context context, String[] languages, int languageSelection) {
        this.context = context;
        this.languages = languages;
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
        return languages.length;
    }

    @Override
    public Object getItem(int position) {
        return languages[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.language_item, null, true);
            holder.radioButton = convertView.findViewById(R.id.language_radiobutton);
            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder) convertView.getTag();
        }
        holder.radioButton.setText(languages[position]);
        holder.radioButton.setChecked(position == currentLanguageSelection);
        holder.radioButton.setTag(position);

        holder.radioButton.setOnClickListener(v -> {
            currentLanguageSelection = (Integer) holder.radioButton.getTag();
            notifyDataSetChanged();
        });
        return convertView;
    }

    public int getCurrentLanguageSelection() {
        return this.currentLanguageSelection;
    }

    private static class ViewHolder {
        protected RadioButton radioButton;
    }
}