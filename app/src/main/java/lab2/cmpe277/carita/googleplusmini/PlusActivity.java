package lab2.cmpe277.carita.googleplusmini;

import java.util.Locale;


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

//import com.google.android.gms.plus.model.people.Person;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Circle;
import com.google.api.services.plusDomains.model.Person;


public class PlusActivity extends ActionBarActivity implements ActionBar.TabListener {
//    /**
//     * The {@link android.support.v4.view.PagerAdapter} that will provide
//     * fragments for each of the sections. We use a
//     * {@link FragmentPagerAdapter} derivative, which will keep every
//     * loaded fragment in memory. If this becomes too memory intensive, it
//     * may be best to switch to a
//     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
//     */
    SectionsPagerAdapter mSectionsPagerAdapter;
//    /**
//     * The {@link ViewPager} that will host the section contents.
//     */
    ViewPager mViewPager;
    private static PlusDomains plusDomains;
    private static String accountName;
    private static String accessToken;

//    private static ListView_Adapter listViewAdapter;
//    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent activity = getIntent();
        accessToken = activity.getExtras().getString("accessToken");


//        accountName = activity.getStringExtra("accountName");
//        String scopes = activity.getExtras().getString("scopes");
//
//        Bundle appActivities = new Bundle();
//        appActivities.putString(GoogleAuthUtil.KEY_REQUEST_VISIBLE_ACTIVITIES,
//                "http://schemas.google.com/AddActivity");
//
//        String accessToken = null;
//        try {
//            accessToken = GoogleAuthUtil.getToken(
//                    this,                                              // Context context
//                    accountName,  // String accountName
//                    scopes,                                            // String scope
//                    appActivities                                      // Bundle bundle
//            );
//        } catch (IOException transientEx) {
//            // network or server error, the call is expected to succeed if you try again later.
//            // Don't attempt to call again immediately - the request is likely to
//            // fail, you'll hit quotas or back-off.
//            return;
//        } catch (UserRecoverableAuthException e) {
//            // Requesting an authorization code will always throw
//            // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
//            // because the user must consent to offline access to their data.  After
//            // consent is granted control is returned to your activity in onActivityResult
//            // and the second call to GoogleAuthUtil.getToken will succeed.
//            startActivityForResult(e.getIntent(), 0);
//            return;
//        } catch (GoogleAuthException authEx) {
//            // Failure. The call is not expected to ever succeed so it should not be
//            // retried.
//            return;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), credential).build();


        //Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setHomeButtonEnabled(true);

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
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            Intent activity = new Intent(getApplicationContext(), LoginActivity.class);
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activity);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onPrepareOptionsMenu (Menu menu) {
//        if (!loggedIn)
//            menu.getItem(1).setEnabled(false);
//        return true;
//    }

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
            switch (position) {
                case 0:
                    return MyProfile.newInstance(0, "Profile");
                case 1:
                    return MyCircles.newInstance(1, "Circles");
//                case 2:
//                    return Friends.newInstance(2, "Friends");
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
//                case 2:
//                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class MyProfile extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String TITLE = "Profile";
        private Person me;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MyProfile newInstance(int sectionNumber, String title) {
            MyProfile fragment = new MyProfile();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(TITLE, title);
            fragment.setArguments(args);
            return fragment;
        }

//        public Profile() {
//            try {
//                me = plusDomains.people().get("me").execute();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.profile_main, container, false);
            TextView tvLabel = (TextView) rootView.findViewById(R.id.profile_text);
            tvLabel.setText("Profile ");
//            tvLabel.append(accountName);
            if (accessToken != null) {
                tvLabel.append(accessToken);
            }
            return rootView;
        }
    }

    public static class MyCircles extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String TITLE = "Circles";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static MyCircles newInstance(int sectionNumber, String title) {
            MyCircles fragment = new MyCircles();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putString(TITLE, title);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.circle_main, container, false);
//            TextView tvLabel = (TextView) rootView.findViewById(R.id.circle_text);
//            tvLabel.setText("Circle");

            ExpandableListView list = (ExpandableListView) rootView.findViewById(R.id.listView);
            list.setAdapter(new FriendListAdapter());

//            switch(getArguments().getInt(ARG_SECTION_NUMBER)){
//                case 1:
//                    //The constructor ListView_Adapter(MainActivity.DummySectionFragment) is undefined
//                    listViewAdapter = new ListView_Adapter(getActivity().getBaseContext());
//                    ListView listView = (ListView) rootView.findViewById(R.id.listView1);
//                    for (int i=0;i<20;i++)
//                    {
//                        listViewAdapter.add("this Index : "+i);
//                    }
//                    return listView;
//            }

            return rootView;
        }

        public class FriendListAdapter extends BaseExpandableListAdapter {
            private String[] circles = {"Friends", "Family", "Acquaintances", "Following"};
            private Circle[] google_circles;
            private Person[][] google_circle_list;
            private String[][] circle_list = {
                    {"Friend1", "Friend2", "Friend3"},
                    {"Family1", "Family2", "Family3", "Family4"},
                    {"Acquaintance1", "Acquaintance2", "Acquaintance3"} ,
                    {"Following 1"}
            };

            @Override
            public int getGroupCount() {
                return circles.length;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                return circle_list[groupPosition].length;
            }

            @Override
            public Object getGroup(int groupPosition) {
                return circles[groupPosition];
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return circle_list[groupPosition][childPosition];
            }

            @Override
            public long getGroupId(int groupPosition) {
                return groupPosition;
            }

            @Override
            public long getChildId(int groupPosition, int childPosition) {
                return childPosition;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

            @Override
            public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
                TextView textView = new TextView(MyCircles.this.getActivity());
                textView.setText(getGroup(groupPosition).toString());
                return textView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                TextView textView = new TextView(MyCircles.this.getActivity());
                textView.setText(getChild(groupPosition, childPosition).toString());
                return textView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return false;
            }
        }
    }

//    public static class Friends extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//        private static final String TITLE = "Friends";
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static Friends newInstance(int sectionNumber, String title) {
//            Friends fragment = new Friends();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            args.putString(TITLE, title);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
////        public Friends() {
////        }
////
////        @Override
////        public void onCreate(Bundle savedInstanceState) {
////            super.onCreate(savedInstanceState);
////
////        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.friend_main, container, false);
//            TextView tvLabel = (TextView) rootView.findViewById(R.id.friend_text);
//            tvLabel.setText("Friends");
//            return rootView;
//        }
//    }

//    private class MyListViewAdapter extends ArrayAdapter<String> {
//
//        public MyListViewAdapter (Context c) {
//            super(c, R.layout.list_cell);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View row = convertView;
//            ListView_Text holder = null;
//
//            if (row == null)
//            {
//                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//                row = inflater.inflate(R.layout.list_cell, parent, false);
//                holder = new ListView_Text(row);
//                row.setTag(holder);
//            }
//            else
//            {
//                holder = (ListView_Text) row.getTag();
//            }
//
//            holder.populateFrom(getItem(position));
//
//            return row;
//        }
//
//        static class ListView_Text {
//            private TextView cell_name = null;
//
//            ListView_Text(View row) {
//                cell_name = (TextView) row.findViewById(R.id.list_cell_name);
//            }
//
//            void populateFrom(String index) {
//                cell_name.setText(index);
//            }
//
//        }
//    }
}