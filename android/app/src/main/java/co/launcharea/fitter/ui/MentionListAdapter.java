package co.launcharea.fitter.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import co.launcharea.fitter.R;

/**
 * Created by hyungchulkim on 8/14/15.
 */
public class MentionListAdapter extends BaseAdapter implements Filterable {
    private Context mContext = null;
    private List<ParseUser> mUserList = null;
    private List<ParseUser> mFilteredList = null;

    public MentionListAdapter(Context context) {
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    public List<ParseUser> getUserList() {
        return mUserList;
    }

    public void setUserList(List<ParseUser> list) {
        mUserList = list;
        mFilteredList = list;
    }

    @Override
    public int getCount() {
        if (mFilteredList == null) {
            return 0;
        }
        return mFilteredList.size();
    }

    @Override
    public ParseUser getItem(int position) {
        if (mFilteredList == null) {
            return null;
        }

        return mFilteredList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_list_friend, null);
        }

        ParseImageView profile = (ParseImageView) convertView.findViewById(R.id.profile_picture);
        TextView name = (TextView) convertView.findViewById(R.id.name);

        ParseUser user = getItem(position);
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
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredList = (List<ParseUser>)results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ParseUser> filteredResults = getFilteredResults(constraint);
                FilterResults results = new FilterResults();
                results.values = filteredResults;
                return results;
            }

            private List<ParseUser> getFilteredResults(CharSequence constraint) {
                if (constraint == null || constraint.length() == 0) {
                    return MentionListAdapter.this.getUserList();
                }

                List<ParseUser> results = new ArrayList<ParseUser>();

                List<ParseUser> userList = MentionListAdapter.this.getUserList();
                constraint = constraint.toString().toLowerCase();
                for (int i=0; i<userList.size(); i++) {
                    ParseUser user = userList.get(i);
                    String username = user.getUsername() + user.get("name");
                    if (username.toLowerCase().contains(constraint.toString())) {
                        results.add(user);
                    }
                }

                return results;
            }
        };
    }
}
