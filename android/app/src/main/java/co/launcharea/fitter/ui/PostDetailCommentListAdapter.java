package co.launcharea.fitter.ui;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
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
import co.launcharea.fitter.model.Comment;
import co.launcharea.fitter.util.FitterUIUtil;

/**
 * Created by hyungchulkim on 8/3/15.
 */
public class PostDetailCommentListAdapter extends ParseQueryAdapter<Comment> {
    View.OnClickListener profileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getContext();
            Intent intent = FitterUIUtil.getProfileViewActivityIntent(context, (String) v.getTag());
            context.startActivity(intent);
        }
    };

    public PostDetailCommentListAdapter(Context context, ParseQueryAdapter.QueryFactory<Comment> factory) {
        super(context, factory);
    }

    @Override
    public View getItemView(Comment comment, View v, ViewGroup parent) {
        if (v == null) {
            v = View.inflate(getContext(), R.layout.item_post_detail_comment, null);
        }

        if (comment == null) {
            Log.i("COMMENT", "comment is null");
            return v;
        }

        TextView name = (TextView) v.findViewById(R.id.username);
        if (name == null) {
            Log.e("COMMENT", "name view is null");
            return v;
        }

        ParseUser user = comment.getUser();
        try {
            // TODO : handle background processing
            user.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        name.setText(user.getUsername());

        TextView content = (TextView) v.findViewById(R.id.content);
        content.setText(comment.getRichContent());

        Log.i("COMMENT", "name:" + user.getUsername() + ", content:" + comment.getContent());

        ParseImageView profile = (ParseImageView) v.findViewById(R.id.profile_picture);

        ParseFile file = user.getParseFile("picture");
        if (file != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(file.getUrl(), profile);
            profile.setTag(user.getObjectId());
            profile.setOnClickListener(profileOnClickListener);
        } else {
            profile.setImageResource(R.drawable.ic_bg_person);
        }

        TextView date = (TextView) v.findViewById(R.id.date);
        date.setText(DateUtils.getRelativeTimeSpanString(comment.getCreatedAt().getTime(), System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));

        v.setVisibility(View.VISIBLE);
        return v;
    }
}
