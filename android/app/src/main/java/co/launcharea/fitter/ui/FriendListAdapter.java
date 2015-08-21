package co.launcharea.fitter.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import java.util.List;

import co.launcharea.fitter.R;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class FriendListAdapter extends BaseAdapter implements StickyListHeadersAdapter {

    private List<ParseUser> mFollowings;
    private List<ParseUser> mFollowers;
    private Context mContext;

    public FriendListAdapter(Context context, List<ParseUser> followings, List<ParseUser> followers) {
        mContext = context;
        mFollowings = followings;
        mFollowers = followers;
    }

    @Override
    public int getCount() {
        return mFollowers.size() + mFollowings.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mFollowings.size()) {
            return mFollowings.get(position);
        } else {
            return mFollowers.get(position - mFollowings.size());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mFollowings.size()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_list_friend, null);
        }

        ParseUser user = (ParseUser) getItem(position);

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
        return convertView;
    }

    @Override
    public View getHeaderView(int i, View v, ViewGroup viewGroup) {
        if (v == null) {
            v = View.inflate(mContext, R.layout.section_list_friend, null);
        }

        TextView tv = (TextView) v.findViewById(R.id.section);
        tv.setText(i == 0 ? mContext.getString(R.string.section_followings) : mContext.getString(R.string.section_followers));
        return v;
    }

    @Override
    public long getHeaderId(int i) {
        if (i < mFollowings.size()) {
            return 0;
        } else {
            return 1;
        }
    }
}
