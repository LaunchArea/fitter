package co.launcharea.fitter.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Notification;
import co.launcharea.fitter.util.FitterUIUtil;

/**
 * Created by hyungchulkim on 8/6/15.
 */
public class NotificationListAdapter extends ParseQueryAdapter<Notification> {
    View.OnClickListener profileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getContext();
            Intent intent = FitterUIUtil.getProfileViewActivityIntent(context, (String) v.getTag());
            context.startActivity(intent);
        }
    };

    public NotificationListAdapter(Context context, ParseQueryAdapter.QueryFactory<Notification> factory) {
        super(context, factory);
    }

    @Override
    public View getItemView(Notification notification, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_list_notification, null);
        }

        if (notification.getRead()) {
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            convertView.setBackgroundResource(R.color.accent_unread_notification);
        }

        String userId = notification.getUserId();
        ParseUser user = ParseUser.createWithoutData(ParseUser.class, userId);

        try {
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ParseFile file = user.getParseFile("picture");

        ParseImageView profile = (ParseImageView) convertView.findViewById(R.id.profile_picture);
        if (file != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(file.getUrl(), profile);
            profile.setTag(user.getObjectId());
            profile.setOnClickListener(profileOnClickListener);
        } else {
            profile.setImageResource(R.drawable.ic_bg_person);
        }

        TextView contentTextView = (TextView) convertView.findViewById(R.id.content);
        contentTextView.setText(notification.getRichContent());

        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(DateUtils.getRelativeTimeSpanString(notification.getCreatedAt().getTime(), System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));

        return convertView;
    }
}