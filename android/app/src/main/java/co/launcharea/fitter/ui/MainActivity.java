package co.launcharea.fitter.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.Notification;
import co.launcharea.fitter.util.FitterParseUtil;

public class MainActivity extends BaseActionBarActivity implements ActionBar.TabListener {

    public final static int TAB_TYPE_POST_LIST = 0;
    public final static int TAB_TYPE_FRIEND_LIST = 1;
    public final static int TAB_TYPE_NOTIFICATION_LIST = 2;
    public final static int TAB_COUNT = 3;
    public final static String NOTIFICATION_BROADCAST = "co.launcharea.fitter.NOTIFICATION_BROADCAST";
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private FloatingActionButton mFAB;
    private BroadcastReceiver mNotificationReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setIcon(mSectionsPagerAdapter.getIcon(i))
                            .setCustomView(mSectionsPagerAdapter.getView(i))
                            .setTabListener(this));
            mSectionsPagerAdapter.setNotificationBadge(i, 0);
        }

        updateNotificationBadge();

        mFAB = (FloatingActionButton) findViewById(R.id.fab);
        mFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, PostingActivity.class));
            }
        });

        Log.i("HC", "" + getApplicationContext());

        mNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateNotificationBadge();
            }
        };
    }

    private void updateNotificationBadge () {
        FitterParseUtil.getNotificationBadge(new FindCallback<Notification>() {
            @Override
            public void done(List<Notification> list, ParseException e) {
                if (list == null) {
                    return;
                }
                mSectionsPagerAdapter.setNotificationBadge(TAB_TYPE_NOTIFICATION_LIST, list.size());
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNotificationReceiver, new IntentFilter(NOTIFICATION_BROADCAST));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mNotificationReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileEditActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());

        if (mSectionsPagerAdapter.hasNotification(tab.getPosition())) {
            mSectionsPagerAdapter.setNotificationBadge(tab.getPosition(), 0);

            if (tab.getPosition() == TAB_TYPE_NOTIFICATION_LIST) {
                Date currentDate = new Date();
                ParseUser.getCurrentUser().put("lastSeenNotification", currentDate);
                ParseUser.getCurrentUser().saveEventually();
            }
        }
        setActionBarTitle(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onViewCreated(BaseFragment fragment) {
        super.onViewCreated(fragment);

        if (fragment instanceof PostListFragment) {
            final PostListFragment postListFragment = ((PostListFragment) fragment);
            ListView listView = postListFragment.getListView();
            if (listView.getHeaderViewsCount() == 0) {
                View header = getLayoutInflater().inflate(R.layout.header_main_feed, null);
                ListAdapter adapter = listView.getAdapter();
                listView.setAdapter(null);
                listView.addHeaderView(header);
                listView.setAdapter(adapter);
                Spinner spinner = (Spinner) header.findViewById(R.id.post_scope_spinner);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        PostListFragment.PostScope scope = PostListFragment.PostScope.USER;
                        switch (position) {
                            case 0:
                                scope = PostListFragment.PostScope.ALL;
                                break;
                            case 1:
                                scope = PostListFragment.PostScope.FOLLOWING;
                                break;
                        }
                        postListFragment.setPostScope(scope, ParseUser.getCurrentUser().getObjectId());
                        postListFragment.refresh();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        }
    }

    private void setActionBarTitle(int position) {
        int titleId = R.string.app_name;
        switch (position) {
            case 0:
                titleId = R.string.tab_post_list;
                break;
            case 1:
                titleId = R.string.tab_friend_list;
                break;
            case 2:
                titleId = R.string.tab_notice;
                break;
        }
        getSupportActionBar().setTitle(titleId);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        View[] mActionBarTabViews;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

            mActionBarTabViews = new View[TAB_COUNT];
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case TAB_TYPE_POST_LIST:
                    return PostListFragment.newInstance(PostListFragment.PostScope.ALL);
                case TAB_TYPE_FRIEND_LIST:
                    return FriendListFragment.newInstance();
                case TAB_TYPE_NOTIFICATION_LIST:
                    return NotificationListFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case TAB_TYPE_POST_LIST:
                    return getString(R.string.tab_post_list).toUpperCase();
                case TAB_TYPE_FRIEND_LIST:
                    return getString(R.string.tab_friend_list).toUpperCase();
                case TAB_TYPE_NOTIFICATION_LIST:
                    return getString(R.string.tab_notice).toUpperCase();
            }
            return null;
        }

        public int getIcon(int position) {
            switch (position) {
                case TAB_TYPE_POST_LIST:
                    return R.drawable.ic_action_forum;
                case TAB_TYPE_FRIEND_LIST:
                    return R.drawable.ic_action_people;
                case TAB_TYPE_NOTIFICATION_LIST:
                    return R.drawable.ic_action_public;
            }
            return 0;
        }

        public View getView(int position) {
            View view = mActionBarTabViews[position];
            if (view == null) {
                view = View.inflate(getApplicationContext(), R.layout.action_bar_tab_custom_view, null);

                CharSequence title = getPageTitle(position);

                int icon = getIcon(position);
                ImageView imageView = (ImageView) view.findViewById(R.id.icon_image_view);
                imageView.setImageResource(icon);

                mActionBarTabViews[position] = view;
            }

            return view;
        }

        public boolean hasNotification(int position) {
            View view = getView(position);
            TextView badgeView = (TextView) view.findViewById(R.id.notification_badge);
            return badgeView.getVisibility() != View.INVISIBLE;

        }

        public void setNotificationBadge(int position, int count) {
            View view = getView(position);
            TextView badgeView = (TextView)view.findViewById(R.id.notification_badge);
            badgeView.setText("" + count);
            if (count == 0) {
                badgeView.setVisibility(View.INVISIBLE);
            } else {
                badgeView.setVisibility(View.VISIBLE);
            }
        }
    }
}
