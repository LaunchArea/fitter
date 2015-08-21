package co.launcharea.fitter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Relation;

public class ProfileViewActivity extends BaseActionBarActivity implements View.OnClickListener {

    public static final String EXTRA_USER_ID = "extra_user_id";
    private ParseUser mUser;
    private Button mBtnFollow;
    private TextView mName;
    private ParseImageView mPicture;
    private TextView mBodyStatus;
    private ParseObject mRelationObject;
    private View mHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile_view);

        mHeader = getLayoutInflater().inflate(R.layout.header_profile_view, null);

        mName = (TextView) mHeader.findViewById(R.id.name);
        mPicture = (ParseImageView) mHeader.findViewById(R.id.profile_picture);
        mBodyStatus = (TextView) mHeader.findViewById(R.id.body_status);
        mBtnFollow = (Button) mHeader.findViewById(R.id.btn_follow);

        Intent intent = getIntent();
        String id = intent.getStringExtra(EXTRA_USER_ID);

        if (id.equalsIgnoreCase(ParseUser.getCurrentUser().getObjectId())) {
            finish();
            startActivity(new Intent(this, ProfileEditActivity.class));
            return;
        }

        showProgress(true);
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("objectId", id);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> list, ParseException e) {
                if (e != null || list.size() == 0) {
                    Toast.makeText(ProfileViewActivity.this, "Can't find the user", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mUser = list.get(0);
                try {
                    mUser.fetchIfNeeded();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }

                FragmentManager fragmentManager = getSupportFragmentManager();
                PostListFragment fragment = PostListFragment.newInstance(PostListFragment.PostScope.USER, mUser.getObjectId());
                fragmentManager.beginTransaction()
                        .replace(R.id.post_list_container, fragment)
                        .commit();
            }
        });
    }

    private void refreshBodyStatus() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Profile");
        query.whereEqualTo("user", mUser);
        query.orderByDescending("createdAt");
        query.setLimit(1);
        try {
            List<ParseObject> list = query.find();
            if (list.size() > 0) {
                ParseObject obj = list.get(0);
                Number mHeight = obj.getNumber("height");
                Number mWeight = obj.getNumber("weight");
                Number mFat = obj.getNumber("bodyFat");

                mBodyStatus.setText(getString(R.string.label_height) + ": " + (mHeight == null ? "-" : mHeight) +
                                "\n" + getString(R.string.label_weight) + ": " + (mWeight == null ? "-" : mWeight) +
                                "\n" + getString(R.string.label_body_fat) + ": " + (mFat == null ? "-" : mFat)
                );
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void refreshUI() {
        showProgress(true);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Relation");
        query.whereEqualTo("from", ParseUser.getCurrentUser());
        query.whereEqualTo("to", mUser);
        query.whereEqualTo("type", "follow");
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                mRelationObject = parseObject;
                if (e == null) {
                    mBtnFollow.setText(getString(R.string.action_unfollow));
                } else {
                    mBtnFollow.setText(getString(R.string.action_follow));
                }
                mBtnFollow.setEnabled(true);
                showProgress(false);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_follow) {
            showProgress(true);
            if (mRelationObject == null) {
                Relation relation = new Relation();
                relation.put("from", ParseUser.getCurrentUser());
                relation.put("to", mUser);
                relation.put("type", "follow");

                relation.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            refreshUI();
                        } else {
                            Toast.makeText(ProfileViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        showProgress(false);
                    }
                });
            } else {
                mRelationObject.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            mRelationObject = null;
                            refreshUI();
                        } else {
                            Toast.makeText(ProfileViewActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        showProgress(false);
                    }
                });
            }
        }
    }

    @Override
    public void onViewCreated(BaseFragment fragment) {
        super.onViewCreated(fragment);
        if (fragment instanceof PostListFragment) {
            ListView listView = ((PostListFragment) fragment).getListView();
            ListAdapter adapter = listView.getAdapter();
            listView.setAdapter(null);
            listView.addHeaderView(mHeader);
            listView.setAdapter(adapter);

            mName.setText(mUser.getString("name"));

            ParseFile file = mUser.getParseFile("picture");
            if (file != null) {
                ImageLoader imageLoader = ImageLoader.getInstance();
                imageLoader.displayImage(file.getUrl(), mPicture);
            }

            getSupportActionBar().setTitle(mUser.getUsername() + getString(R.string.profile_title_postfix));

            mBtnFollow.setOnClickListener(this);
            refreshBodyStatus();
            refreshUI();
        }
    }
}
