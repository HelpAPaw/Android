package org.helpapaw.helpapaw.mysignals;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;


public class MySignalsCustomAdapter extends BaseAdapter {

    private final Context context;
    private final Signal[] signals;

    public MySignalsCustomAdapter(Context context, Signal[] signals) {
        this.context = context;
        this.signals = signals;
    }

    @Override
    public int getCount() {
        return signals.length;
    }

    @Override
    public Object getItem(int position) {
        return signals[position];
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
            convertView = inflater.inflate(R.layout.signal_item, null, true);
            holder.imageView = convertView.findViewById(R.id.signal_item_img);
            holder.textViewTitle = convertView.findViewById(R.id.signal_item_title);
            holder.textViewDate = convertView.findViewById(R.id.signal_item_date);
            convertView.setTag(holder);
        } else {
            // the getTag returns the viewHolder object set as a tag to the view
            holder = (ViewHolder)convertView.getTag();
        }

        Signal currentSignal = signals[position];

        Injection.getImageLoader().loadWithRoundedCorners(
                this.context, currentSignal.getPhotoUrl(), holder.imageView, R.drawable.ic_paw);

        holder.textViewTitle.setText(currentSignal.getTitle());

        String formattedDate = Utils.getInstance().getFormattedDate(currentSignal.getDateSubmitted());
        holder.textViewDate.setText(formattedDate);

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SignalDetailsActivity.class);
            intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, currentSignal);
            startActivity(context, intent, null);
        });

        return convertView;
    }

    private static class ViewHolder {
        protected ImageView imageView;
        private TextView textViewTitle;
        private TextView textViewDate;
    }
}