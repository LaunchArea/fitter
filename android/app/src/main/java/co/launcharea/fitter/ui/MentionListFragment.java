package co.launcharea.fitter.ui;


import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import co.launcharea.fitter.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MentionListFragment extends Fragment {

    private ListView mListView;
    private MentionListAdapter mAdapter;
    private View mRoot;
    private MentionListener mMentionListener;
    private AdapterView.OnItemClickListener mOnItemClickListener;

    public MentionListFragment() {

    }

    public static MentionListFragment newInstance() {
        MentionListFragment fragment = new MentionListFragment();
        return fragment;
    }

    public void setMentionListener(MentionListener mentionListener) {
        this.mMentionListener = mentionListener;
    }

    public void visibleMentionList(CharSequence s) {
        if (s == null) {
            this.mListView.setVisibility(View.INVISIBLE);
            this.mRoot.setVisibility(View.INVISIBLE);
        } else {
            this.mListView.setVisibility(View.VISIBLE);
            this.mRoot.setVisibility(View.VISIBLE);
            this.mAdapter.getFilter().filter(s);
        }

        this.mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MentionListAdapter(getActivity());

        // Warning: TODO: 멘션 가능한 사용자 쿼리문 개선이 필요하다. (현재는 전체 유저를 가져옴)
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                mAdapter.setUserList(list);
                if (mListView != null) {
                    mListView.setAdapter(mAdapter);
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_mention_list, container, false);

        mRoot = v.findViewById(R.id.mention_list_root);
        mRoot.setVisibility(View.INVISIBLE);

        mListView = (ListView) v.findViewById(R.id.mention_list_view);
        mListView.setAdapter(mAdapter);
        mListView.setVisibility(View.INVISIBLE);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseUser user = mAdapter.getItem(position);
                if (mMentionListener != null) {
                    mMentionListener.onClickUser(user);
                }
            }
        });
        mListView.setBackgroundColor(Color.WHITE);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public interface MentionListener {
        void onClickUser(ParseUser user);
    }
}