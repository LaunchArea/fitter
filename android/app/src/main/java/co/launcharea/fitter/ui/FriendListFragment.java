package co.launcharea.fitter.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Relation;
import co.launcharea.fitter.util.FitterUIUtil;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class FriendListFragment extends Fragment implements ListView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {

    private StickyListHeadersAdapter mFriendListAdapter;
    private StickyListHeadersListView mListView;
    private ParseUser mUser;
    private SwipeRefreshLayout mSwipeLayout;


    public FriendListFragment() {
    }

    public static FriendListFragment newInstance() {
        FriendListFragment fragment = new FriendListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = ParseUser.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friend_list, container, false);
        mListView = (StickyListHeadersListView) v.findViewById(R.id.list);
        mListView.setOnItemClickListener(this);

        mSwipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(R.color.primary);

        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        mSwipeLayout.setRefreshing(true);

        new AsyncTask<Void, Void, ParseException>() {

            List<ParseUser> follower;
            List<ParseUser> following;

            @Override
            protected ParseException doInBackground(Void... params) {
                ParseQuery<Relation> q1 = new ParseQuery("Relation").whereEqualTo("from", mUser);
                ParseQuery<Relation> q2 = new ParseQuery("Relation").whereEqualTo("to", mUser);
                ParseQuery<Relation> query = ParseQuery.or(Arrays.asList(q1, q2));
                query.orderByAscending("name");
                List<Relation> list = null;
                try {
                    list = query.find();
                } catch (ParseException e) {
                    e.printStackTrace();
                    return e;
                }

                ParseUser me = ParseUser.getCurrentUser();
                follower = new LinkedList<ParseUser>();
                following = new LinkedList<ParseUser>();
                for (Relation each : list) {
                    ParseUser from = each.getParseUser("from");
                    ParseUser to = each.getParseUser("to");
                    try {
                        from.fetchIfNeeded();
                        to.fetchIfNeeded();
                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }
                    if (from.getUsername().equalsIgnoreCase(me.getUsername())) {
                        following.add(to);
                    } else {
                        follower.add(from);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(ParseException e) {
                super.onPostExecute(e);
                if (e != null) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                } else {
                    mFriendListAdapter = new FriendListAdapter(getActivity(), following, follower);
                    mListView.setAdapter(mFriendListAdapter);
                }
                mSwipeLayout.setRefreshing(false);
            }
        }.execute();
    }


    @Override
    public void onItemClick(AdapterView<?> l, View v, int pos, long id) {
        ParseUser target = (ParseUser) mFriendListAdapter.getItem(pos);
        Intent intent = FitterUIUtil.getProfileViewActivityIntent(getActivity(), target.getObjectId());
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        refresh();
    }
}
