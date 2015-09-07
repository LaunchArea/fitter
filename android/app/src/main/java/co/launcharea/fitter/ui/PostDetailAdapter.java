package co.launcharea.fitter.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Comment;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.util.FitterUIUtil;
import co.launcharea.fitter.widget.FitLogGroup;

/**
 * Created by hyungchulkim on 8/3/15.
 */
public class PostDetailAdapter extends BaseAdapter implements ListAdapter {
    private Post mPost;
    private PostDetailCommentListAdapter mPostDetailCommentListAdapter;
    private int mCount;
    private Context mContext;
    public PostDetailAdapter(Context context, String postId) {
        mPost = ParseObject.createWithoutData(Post.class, postId);

        ParseQueryAdapter.QueryFactory<Comment> factory = new ParseQueryAdapter.QueryFactory<Comment>() {

            @Override
            public ParseQuery<Comment> create() {
                ParseQuery query = ParseQuery.getQuery("Comment").whereEqualTo("post", mPost);
//                query.whereExists("user");
                query.addDescendingOrder("createdAt");
                return query;
            }
        };



        mContext = context;
        mCount = ITEM_TYPE.size;
        mPostDetailCommentListAdapter = new PostDetailCommentListAdapter(context, factory);

        final PostDetailAdapter self = this;
        mPostDetailCommentListAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<Comment>(){
            @Override
            public void onLoading() {
                Log.d("ADAPTER", "onLoading");
            }

            @Override
            public void onLoaded(List<Comment> list, Exception e) {
                Log.d("ADAPTER", "onLoaded");
                mCount = ITEM_TYPE.size - 1 + mPostDetailCommentListAdapter.getCount();
                self.notifyDataSetChanged();
            }
        });

        mPostDetailCommentListAdapter.loadObjects();
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Object getItem(int position) {
        if (position < ITEM_TYPE.COMMENT) {
            return null;
        }
        return mPostDetailCommentListAdapter.getItem(position - ITEM_TYPE.size + 1);
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case ITEM_TYPE.HEADER:
                return ITEM_TYPE.HEADER;
            default:
                return ITEM_TYPE.COMMENT;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_TYPE.size;
    }

    @Override
    public long getItemId(int position) {
        if (position < ITEM_TYPE.size) {
            return 0;
        }
        return mPostDetailCommentListAdapter.getItemId(position - ITEM_TYPE.size + 1);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("PostDetailAdapter", "position:" + position);

        switch(getItemViewType(position)) {
            case ITEM_TYPE.HEADER:
                if (convertView == null) {
                    convertView = View.inflate(this.mContext, R.layout.item_post_detail_header, null);
                }


                final TextView userNameTextView = (TextView) convertView.findViewById(R.id.post_detail_header_user_name);

                if (mPost.getUser().isDataAvailable()) {
                    userNameTextView.setText(mPost.getUser().getUsername());
                } else {
                    mPost.getUser().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject parseObject, ParseException e) {
                            userNameTextView.setText(mPost.getUser().getUsername());
                        }
                    });
                }

                TextView contentTextView = (TextView) convertView.findViewById(R.id.post_detail_header_post);
                String content = mPost.getContent();
                if (content != null && content.length() > 0) {
                    contentTextView.setText(mPost.getRichContent());
                    FitterUIUtil.linkifyText(contentTextView);
                    contentTextView.setVisibility(View.VISIBLE);
                } else {
                    contentTextView.setVisibility(View.GONE);
                }
                FitLogGroup fitlogView = (FitLogGroup) convertView.findViewById(R.id.post_detail_fitlog_view);
                fitlogView.loadView(mPost.getFitLogId(), false, mPost.getFitLogSummary());

                ParseImageView profile = (ParseImageView) convertView.findViewById(R.id.profile_picture);
                ParseFile file = mPost.getUser().getParseFile("picture");
                if (file != null) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(file.getUrl(), profile);
                } else {
                    profile.setImageResource(R.drawable.ic_bg_person);
                }

                TextView date = (TextView) convertView.findViewById(R.id.date);
                date.setText(DateUtils.getRelativeTimeSpanString(mPost.getCreatedAt().getTime(), System.currentTimeMillis(), 0L, DateUtils.FORMAT_ABBREV_ALL));

                ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
                ParseFile photoFile = mPost.getParseFile("photo");
                if (photoFile != null) {
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(photoFile.getUrl(), photo);
                    photo.setVisibility(View.VISIBLE);
                } else {
                    photo.setVisibility(View.GONE);
                }

                return convertView;
            case ITEM_TYPE.COMMENT:
                Comment comment = mPostDetailCommentListAdapter.getItem(position - ITEM_TYPE.size + 1);
                return mPostDetailCommentListAdapter.getItemView(comment, convertView, parent);
            default:
                return null;
        }
    }

    public void loadObjects() {
        mPostDetailCommentListAdapter.loadObjects();
    }

    private static class ITEM_TYPE {
        public static final int HEADER = 0;
        public static final int COMMENT = 1;

        public static final int size = 2;
    }
}