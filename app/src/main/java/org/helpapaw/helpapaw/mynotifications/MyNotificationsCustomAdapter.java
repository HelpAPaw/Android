package org.helpapaw.helpapaw.mynotifications;

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
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.signaldetails.SignalDetailsActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.Map;

public class MyNotificationsCustomAdapter extends BaseAdapter {

    private final Context context;
    private final Notification[] notifications;
    private final Map<String, Signal> mapSignalsToIds;

    public MyNotificationsCustomAdapter(Context context, Notification[] notifications, Map<String, Signal> mapSignalsToIds) {
        this.context = context;
        this.notifications = notifications;
        this.mapSignalsToIds = mapSignalsToIds;
    }

    @Override
    public int getCount() {
        return notifications.length;
    }

    @Override
    public Object getItem(int position) {
        return notifications[position];
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

        Notification currentNotification = notifications[position];
        String currentNotificationSignalId = currentNotification.getSignalId();
        Signal currentNotificationSignal = mapSignalsToIds.get(currentNotificationSignalId);

        if (currentNotificationSignal != null) {
            Injection.getImageLoader().loadWithRoundedCorners(
                    this.context, currentNotificationSignal.getPhotoUrl(), holder.imageView, R.drawable.ic_paw);
        }

        holder.textViewTitle.setText(currentNotification.getText());

        String formattedDate = Utils.getFormattedDate(currentNotification.getDateReceived());
        holder.textViewDate.setText(formattedDate);

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SignalDetailsActivity.class);
            intent.putExtra(SignalDetailsActivity.SIGNAL_KEY, currentNotificationSignal);
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
