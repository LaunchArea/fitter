package co.launcharea.fitter.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Comment;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.util.EndlessScrollListener;
import co.launcharea.fitter.util.FitterUIUtil;

public class PostListFragment extends BaseFragment implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final int OBJECT_PER_PAGE = 20;
    private static final int LOAD_MORE_THRESHOLD = 5;
    static String ARG_USER_ID = "arg_user_id";
    static String ARG_POST_KIND = "arg_post_kind";
    private PostListAdapter mPostListAdapter;
    private PostScope mPostScope = PostScope.ALL;
    private ListView mListView;
    private SwipeRefreshLayout mSwipeLayout;
    private String mUserId;
    private ParseObject mUserObject;

    public PostListFragment() {
    }

    public static PostListFragment newInstance(PostScope kind) {
        return newInstance(kind, null);
    }

    public static PostListFragment newInstance(PostScope kind, String userId) {
        PostListFragment fragment = new PostListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_POST_KIND, kind);
        if (userId != null) {
            args.putSerializable(ARG_USER_ID, userId);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setPostScope(PostScope scope, String userId) {
        mPostScope = scope;
        if (mPostScope == PostScope.USER) {
            mUserId = userId;
            mUserObject = ParseObject.createWithoutData("_User", mUserId);
        }

        mPostListAdapter = new PostListAdapter(getActivity());

        if (mListView != null) {
            mListView.setAdapter(mPostListAdapter);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PostScope scope = PostScope.ALL;
        String userId = null;
        if (getArguments() != null) {
            scope = (PostScope) getArguments().getSerializable(ARG_POST_KIND);
            if (scope == PostScope.USER) {
                userId = (String) getArguments().getSerializable(ARG_USER_ID);
            }
        }
        setPostScope(scope, userId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post_list, container, false);
        mListView = (ListView) v.findViewById(R.id.list);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mPostListAdapter);

        mSwipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.primary);

        return v;

    }

    private ParseQuery getQuery() {
        switch (mPostScope) {
            case ALL: {
                ParseQuery query = new ParseQuery("Post");
                query.whereExists("user");
                query.addDescendingOrder("createdAt");
                return query;
            }
            case FOLLOWING: {
                ParseQuery queryFollowing = ParseQuery.getQuery("Relation").whereEqualTo("from", ParseUser.getCurrentUser());
                ParseQuery queryPostOfFollowing = ParseQuery.getQuery("Post").whereMatchesKeyInQuery("user", "to", queryFollowing);
                queryPostOfFollowing.addDescendingOrder("createdAt");
                return queryPostOfFollowing;
            }
            case USER: {
                ParseQuery<Post> query = ParseQuery.getQuery("Post");
                mUserObject = ParseObject.createWithoutData("_User", mUserId);
                query.whereEqualTo("user", mUserObject);
                query.addDescendingOrder("createdAt");
                return query;
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        mSwipeLayout.setRefreshing(true);
        LoadTask task = new LoadTask(0) {
            @Override
            protected void onPostExecute(List<Post> list) {
                super.onPostExecute(list);
                mSwipeLayout.setRefreshing(false);
                mListView.setOnScrollListener(new EndlessScrollListener(LOAD_MORE_THRESHOLD) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount) {
//                mPostListAdapter.loadNextPage();
                        Log.d("yns", "" + page + " " + totalItemsCount);
                        loadMore(totalItemsCount);
                    }
                });
            }
        };
        task.execute();
    }

    private void loadMore(int start) {
        LoadTask task = new LoadTask(start);
        task.execute();
    }

    @Override
    public void onItemClick(AdapterView<?> l, View v, int pos, long id) {
        int position = pos - mListView.getHeaderViewsCount();
        if (position < 0) {
            return;
        }

        Post post = (Post) mPostListAdapter.getItem(position);
        ParseUser user = post.getUser();

        Intent intent = FitterUIUtil.getPostDetailActivityIntent(getActivity().getApplicationContext(), post);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    public ListView getListView() {
        return mListView;
    }

    public enum PostScope {
        ALL, FOLLOWING, USER
    }

    class LoadTask extends AsyncTask<Void, Void, List<Post>> {

        private final int mStart;

        public LoadTask(int start) {
            mStart = start;
        }

        @Override
        protected List<Post> doInBackground(Void... params) {
            ParseQuery<Post> query = getQuery();
            query.setLimit(OBJECT_PER_PAGE);
            query.setSkip(mStart);
            try {
                List<Post> list = query.find();
                for (Post post : list) {
                    post.fetchIfNeeded();
                    post.getUser().fetchIfNeeded();
                    if (post.getInt("commentCount") > 0) {
                        final Comment comment = post.getLastComment();
                        if (comment != null) {
                            comment.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    comment.getUser().fetchIfNeededInBackground();
                                }
                            });
                        }
                    }
                }
                return list;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Post> list) {
            super.onPostExecute(list);
            if (list != null) {
                if (mStart == 0) {
                    mPostListAdapter.setItems(list);
                } else {
                    mPostListAdapter.appendItems(list);
                }
                mPostListAdapter.notifyDataSetChanged();
            }
            mSwipeLayout.setRefreshing(false);
        }
    }
}
