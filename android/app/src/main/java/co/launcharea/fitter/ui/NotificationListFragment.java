package co.launcharea.fitter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Notification;
import co.launcharea.fitter.model.Post;
import co.launcharea.fitter.util.FitterUIUtil;

public class NotificationListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, ParseQueryAdapter.OnQueryLoadListener<Notification> {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView mListView;
    private NotificationListAdapter mAdapter;

    public NotificationListFragment() {
        // Required empty public constructor
    }

    public static NotificationListFragment newInstance() {
        NotificationListFragment fragment = new NotificationListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ParseQueryAdapter.QueryFactory<Notification> factory = new ParseQueryAdapter.QueryFactory<Notification>() {

            @Override
            public ParseQuery<Notification> create() {
                long DAY_IN_MS = 1000 * 60 * 60 * 24;
                Date date = new Date(System.currentTimeMillis() - (90 * DAY_IN_MS));

                ParseQuery query = new ParseQuery("Notification");
                query.whereGreaterThan("createdAt", date);
                query.addDescendingOrder("createdAt");
                return query;
            }
        };

        mAdapter = new NotificationListAdapter(getActivity(), factory);
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_list, container, false);
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.primary);

        mListView = (ListView)view.findViewById(R.id.notification_list_view);
        mAdapter.addOnQueryLoadListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Notification notification = mAdapter.getItem(position);

                Intent intent = null;
                Post post = null;
                ParseUser user = null;

                String postId = notification.getPostId();
                if (postId != null) {
                    post = ParseObject.createWithoutData(Post.class, postId);
                    try {
                        post.fetchIfNeeded();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                String userId = notification.getUserId();
                if (userId != null) {
                    user = ParseObject.createWithoutData(ParseUser.class, userId);
                    try {
                        user.fetchIfNeeded();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                switch (notification.getType()) {
                    case Notification.TYPE_FOLLOWING:
                        intent = FitterUIUtil.getProfileViewActivityIntent(getActivity().getApplicationContext(), userId);
                        break;
                    case Notification.TYPE_LIKE_ON_MY_POST:
                        break;
                    case Notification.TYPE_COMMENT_ON_POST:
                        intent = FitterUIUtil.getPostDetailActivityIntent(getActivity().getApplicationContext(), post);
                        break;
                    case Notification.TYPE_COMMENT_ON_MY_COMMENT:
                        break;
                    case Notification.TYPE_MENTION_ON_A_POST:
                        intent = FitterUIUtil.getPostDetailActivityIntent(getActivity().getApplicationContext(), post);
                        break;
                    case Notification.TYPE_MENTION_ON_A_COMMENT:
                        intent = FitterUIUtil.getPostDetailActivityIntent(getActivity().getApplicationContext(), post);
                        break;
                    default:
                        break;
                }

                if (intent == null) {
                    return;
                }

                startActivity(intent);
                notification.setRead();
                notification.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Log.d("NOTIFICATION", "set read success: " + notification.getObjectId());
                        } else {
                            e.printStackTrace();
                        }
                    }
                });

                Log.d("NOTIFICATION", notification.toString());
            }
        });

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onRefresh() {
        mAdapter.loadObjects();
        mSwipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onLoading() {
    }

    @Override
    public void onLoaded(List<Notification> list, Exception e) {
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
}
