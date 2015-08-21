package co.launcharea.fitter.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import co.launcharea.fitter.R;

public class PostDetailFragment extends Fragment {
    private static String POST = "POST";

    private PostDetailAdapter mPostDetailAdapter;
    private String mPostId;
    private ListView mListView;

    private View mFooterView;

    public PostDetailFragment() {
    }

    public static PostDetailFragment newInstance(String postId) {
        PostDetailFragment fragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putString(POST, postId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPostId = getArguments().getString(POST);
        mFooterView = getActivity().getLayoutInflater().inflate(R.layout.item_post_detail_footer, null, false);

        this.mPostDetailAdapter = new PostDetailAdapter(getActivity(), mPostId);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_post_detail, container, false);
        mListView = (ListView) v.findViewById(R.id.listView);
        mListView.addFooterView(mFooterView);
        mListView.setAdapter(this.mPostDetailAdapter);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPostDetailAdapter.loadObjects();
    }

    public void refresh() {
        if (mPostDetailAdapter != null) {
            mPostDetailAdapter.loadObjects();
        }
    }
}