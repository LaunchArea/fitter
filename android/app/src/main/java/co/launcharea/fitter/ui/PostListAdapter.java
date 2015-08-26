package co.launcharea.fitter.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.LinkedList;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Comment;
import co.launcharea.fitter.model.FitLog;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.util.FitterUIUtil;
import co.launcharea.fitter.widget.FitLogSummaryView;

public class PostListAdapter extends BaseAdapter {

    private final Context mContext;
    View.OnClickListener profileOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getContext();
            Intent intent = FitterUIUtil.getProfileViewActivityIntent(context, (String) v.getTag());
            context.startActivity(intent);
        }
    };
    private List<Post> mPosts = new LinkedList<Post>();

    public PostListAdapter(Context context) {
        mContext = context;
    }

    public View getItemView(Post post, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_list_post, null);
        }

        ParseUser user = post.getUser();
        ParseImageView profile = (ParseImageView) convertView.findViewById(R.id.profile_picture);
        TextView name = (TextView) convertView.findViewById(R.id.name);

        ParseFile file = user.getParseFile("picture");
        if (file != null) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(file.getUrl(), profile);
        } else {
            profile.setImageResource(R.drawable.ic_bg_person);
        }
        name.setText(user.getUsername());

        TextView date = (TextView) convertView.findViewById(R.id.date);
        date.setText(DateUtils.getRelativeTimeSpanString(post.getCreatedAt().getTime(), System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));


        boolean showFitLog = false;
        boolean showContent = false;

        FitLog summary = post.getFitLogSummary();
        FitLogSummaryView fitlogView = (FitLogSummaryView) convertView.findViewById(R.id.fitlog_summary);
        if (summary != null) {
            showFitLog = true;
            fitlogView.loadData(summary);
            fitlogView.setVisibility(View.VISIBLE);
        } else {
            fitlogView.setVisibility(View.GONE);
        }


        View contentLayout = convertView.findViewById(R.id.content_layout);
        if (post.getContent().length() > 0) {
            TextView content = (TextView) convertView.findViewById(R.id.content);
            content.setText(post.getRichContent());
            contentLayout.setVisibility(View.VISIBLE);
            showContent = true;
        } else {
            contentLayout.setVisibility(View.GONE);
        }

        convertView.findViewById(R.id.divider).setVisibility(showFitLog && showContent ? View.VISIBLE : View.GONE);
        convertView.findViewById(R.id.dummy_margin).setVisibility(showFitLog && !showContent ? View.VISIBLE : View.GONE);

        TextView commentCountTextView = (TextView) convertView.findViewById(R.id.comment_count);
        View commentLayout = convertView.findViewById(R.id.last_comment);

        int commentCount = post.getInt("commentCount");

        if (commentCount > 0) {
            commentCountTextView.setVisibility(View.VISIBLE);
            commentCountTextView.setText(String.format(mContext.getString(R.string.formated_comment_count), commentCount));

            Comment lastComment = post.getLastComment();
            if (lastComment != null) {
                commentLayout.setVisibility(View.VISIBLE);
                fillCommentData(commentLayout, lastComment);
            } else {
                commentLayout.setVisibility(View.GONE);
            }
        } else {
            commentCountTextView.setVisibility(View.GONE);
            commentLayout.setVisibility(View.GONE);
            commentLayout.setTag(null);
        }


        SimpleDraweeView photo = (SimpleDraweeView) convertView.findViewById(R.id.photo);

        ParseFile photoFile = post.getParseFile("photo");
        if (photoFile != null) {
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(photoFile.getUrl()))
                    .setAutoRotateEnabled(true)
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(request)
                    .setOldController(photo.getController())
                    .build();
            photo.setAspectRatio(1.778f);
            photo.setController(controller);
            photo.setVisibility(View.VISIBLE);
        } else {
            photo.setVisibility(View.GONE);
        }

        name.setTag(user.getObjectId());
        name.setOnClickListener(profileOnClickListener);
        profile.setTag(user.getObjectId());
        profile.setOnClickListener(profileOnClickListener);
        return convertView;
    }

    /**
     * @param v
     * @param comment
     * @return true if success
     */
    private void fillCommentData(final View v, final Comment comment) {
        v.findViewById(R.id.date).setVisibility(View.GONE);
        v.findViewById(R.id.username).setVisibility(View.INVISIBLE);
        v.findViewById(R.id.profile_picture).setVisibility(View.INVISIBLE);
        v.findViewById(R.id.content).setVisibility(View.INVISIBLE);

        v.setTag(comment);
        if (comment != null) {
            if (comment.isDataAvailable()) {
                fillCommentContent(v, comment);
            } else {
                comment.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject parseObject, ParseException e) {
                        if (v.getTag() == comment) {
                            fillCommentContent(v, (Comment) parseObject);
                        }
                    }
                });
            }
        }
    }

    private void fillCommentContent(View v, Comment comment) {
        TextView content = (TextView) v.findViewById(R.id.content);
        content.setText(comment.getRichContent());
        content.setVisibility(View.VISIBLE);

        TextView date = (TextView) v.findViewById(R.id.date);
        date.setText(DateUtils.getRelativeTimeSpanString(comment.getCreatedAt().getTime(), System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));
        date.setVisibility(View.VISIBLE);

        if (comment.getUser().isDataAvailable()) {
            fillCommentUser(v, comment.getUser());
        } else {
            comment.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                }
            });
        }
    }

    private void fillCommentUser(final View v, ParseUser parseUser) {
        TextView name = (TextView) v.findViewById(R.id.username);

        ParseUser user = parseUser;
        name.setText(user.getUsername());

        name.setTag(user.getObjectId());
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                Intent intent = FitterUIUtil.getProfileViewActivityIntent(context, (String) v.getTag());
                context.startActivity(intent);
            }
        });
        name.setVisibility(View.VISIBLE);

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
        profile.setVisibility(View.VISIBLE);
    }

    public void setItems(List<Post> posts) {
        mPosts = posts;
    }


    @Override
    public int getCount() {
        return mPosts.size();
    }

    @Override
    public Object getItem(int position) {
        return mPosts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItemView((Post) getItem(position), convertView, parent);
    }

    public Context getContext() {
        return mContext;
    }

    public void appendItems(List<Post> list) {
        mPosts.addAll(list);
    }
}
