package co.launcharea.fitter.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import co.launcharea.fitter.R;
import co.launcharea.fitter.model.FitLog;

public class WorkoutSelectorActivity extends BaseActionBarActivity implements ActionBar.TabListener {

    private final static int TAB_TYPE_MINE = 0;
    private final static int TAB_TYPE_FOLLOWEES = 1;
    private final static int TAB_TYPE_FOLLOWERS = 2;
    private final static int TAB_TYPE_ALL = 3;
    private final static int TAB_COUNT = 4;
    private final static String BUNDLE_KEY_SEARCH_EXPANDED = "searchExpanded";
    private final static String BUNDLE_KEY_QUERY_TEXT = "queryText";
    MenuItem mSearchMenu;
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
    private boolean mSearchExpanded = false;
    private String mQueryText;
    private Drawable mIconOpenSearch;
    private Drawable mIconCloseSearch;
    private EditText mEditText;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_selector);

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
        String[] tabNames = getResources().getStringArray(R.array.workout_selector_tabs);
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(tabNames[i])
                            .setTabListener(this));
        }

        //mViewPager.setOffscreenPageLimit(2);

        mIconOpenSearch = getResources()
                .getDrawable(R.drawable.ic_action_search);
        mIconCloseSearch = getResources()
                .getDrawable(R.drawable.ic_action_clear);

        if (savedInstanceState == null) {
            mQueryText = "";
        } else {
            mSearchExpanded = savedInstanceState.getBoolean(BUNDLE_KEY_SEARCH_EXPANDED);
            mQueryText = savedInstanceState.getString(BUNDLE_KEY_QUERY_TEXT);
        }

        findViewById(R.id.newWorkOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(WorkoutSelectorActivity.this);
                LinearLayout dialogLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_workout_selector, null);
                builder.setView(dialogLayout);
                dialogLayout.findViewById(R.id.workoutUsing).setVisibility(View.GONE);
                dialogLayout.findViewById(R.id.workoutScroll).setVisibility(View.GONE);

                Calendar cal = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.format_date));
                ((EditText) dialogLayout.findViewById(R.id.workoutName)).setText(getString(R.string.label_workout) + " " + dateFormat.format(cal.getTime()));

                builder.setPositiveButton(getString(R.string.action_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });
                builder.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                });

                Intent intent = new Intent();
                mDialog = builder.create();
                mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                mDialog.show();

                // override onClickListener not to be dismissed right after OK clicked.
                // to get button from dialog, dialog must be shown first.
                // if not working, dialog.setOnshowListener should be used.
                Button positiveButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setTag(intent);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = (Intent) v.getTag();
                        String workoutName = ((EditText) mDialog.findViewById(R.id.workoutName)).getText().toString();
                        if (!workoutName.isEmpty()) {
                            intent.putExtra(PostingActivity.EXTRA_WORKOUT_NAME, workoutName);
                            WorkoutSelectorActivity.this.setResult(RESULT_OK, intent);
                            WorkoutSelectorActivity.this.finish();
                            mDialog.dismiss();
                        }
                    }

                });
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(BUNDLE_KEY_SEARCH_EXPANDED, mSearchExpanded);
        //mQueryText = mEditText.getText().toString();
        outState.putString(BUNDLE_KEY_QUERY_TEXT, mQueryText);
    }

    /*
    TODO : https://github.com/LaunchArea/fitter/issues/89
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_workout_selector, menu);
        mSearchMenu = menu.findItem(R.id.workoutSearch);
        if (mSearchExpanded) {
            openSearchBar(mQueryText);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.workoutSearch) {
            if (mSearchExpanded) {
                closeSearchBar();
            } else {
                openSearchBar(mQueryText);
            }
            return true;
        }
        // TODO

        return super.onOptionsItemSelected(item);
    }
*/
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    private void openSearchBar(String queryText) {

        // Set custom view on action bar.
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_search);

        // Search edit text field setup.
        mEditText = (EditText) actionBar.getCustomView().findViewById(R.id.searchText);
        //mSearchEt.addTextChangedListener(new SearchWatcher());
        mEditText.setText(queryText);
        mEditText.requestFocus();

        // Change search icon accordingly.

        mSearchMenu.setIcon(mIconCloseSearch);
        mSearchExpanded = true;
    }

    private void closeSearchBar() {
        // Remove custom view.
        getSupportActionBar().setDisplayShowCustomEnabled(false);

        // Change search icon accordingly.
        mSearchMenu.setIcon(mIconOpenSearch);
        mSearchExpanded = false;
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private ListView mListView;
        private WorkoutListAdapter mListAdapter;
        private int mSectionNumber;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_workout_selector, container, false);
            mListView = (ListView) rootView.findViewById(R.id.workoutListView);
            mListAdapter = new WorkoutListAdapter(getActivity(), false);
            mListView.setAdapter(mListAdapter);
            mListView.setOnItemClickListener(mListAdapter);
            new FetchTask(getArguments().getInt(ARG_SECTION_NUMBER)).execute();
            return rootView;
        }

        class FetchTask extends AsyncTask<Void, Void, ParseException> {

            private final int mIndex;
            private List<FitLog> mList;

            protected FetchTask(int index) {
                mIndex = index;
            }

            @Override
            protected ParseException doInBackground(Void... params) {
                ParseQuery<FitLog> query = new ParseQuery("FitLog");
                query.whereEqualTo("index", 0);
                query.addDescendingOrder("createdAt");
                switch (mIndex) {
                    case 0:
                        query.whereEqualTo("user", ParseUser.getCurrentUser());
                        break;
                    case 1:
                        ParseQuery queryFollowee = ParseQuery.getQuery("Relation").whereEqualTo("from", ParseUser.getCurrentUser());
                        query.whereMatchesKeyInQuery("user", "to", queryFollowee);
                        break;
                    case 2:
                        ParseQuery queryFollower = ParseQuery.getQuery("Relation").whereEqualTo("to", ParseUser.getCurrentUser());
                        query.whereMatchesKeyInQuery("user", "from", queryFollower);
                        break;
                    case 3:
                        break;
                }
                try {
                    // TODO
                    mList = query.find();
                    Iterator<FitLog> it = mList.iterator();
                    while (it.hasNext()) {
                        FitLog fitLog = it.next();
                        fitLog.fetchIfNeeded();
                        fitLog.getParseUser("user").fetchIfNeeded();
                    }

                } catch (ParseException e) {
                    e.printStackTrace();
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(ParseException e) {
                super.onPostExecute(e);
                if (e != null) {
                    e.printStackTrace();
                } else {
                    mListAdapter.setList(mList);
                }
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Workouts";
        }
    }

}
