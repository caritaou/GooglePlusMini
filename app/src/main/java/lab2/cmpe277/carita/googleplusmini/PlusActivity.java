package lab2.cmpe277.carita.googleplusmini;

import java.util.List;
import java.util.Locale;


import android.app.Activity;
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
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.plusDomains.model.Person;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


public class PlusActivity extends ActionBarActivity implements ActionBar.TabListener  {
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
    private static String accessToken;

    private static String displayName;
    private static String organizations;
    private static String aboutMe;
    private static String image_url;
    private static String occupation;

    private static String[] circle_list;
    private static String[][] circle_children_list;
    private static Person[][] circle_children_people;

    private static ImageLoader imageLoader;
    private static DisplayImageOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .discCacheSize(100 * 1024 * 1024).build();
        ImageLoader.getInstance().init(config);

        imageLoader = ImageLoader.getInstance();
        options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(getResources().getDrawable(R.drawable.ic_launcher))
                .showImageOnFail(getResources().getDrawable(R.drawable.ic_launcher))
                .showImageOnLoading(getResources().getDrawable(R.drawable.ic_launcher)).build();

        Intent activity = getIntent();
        accessToken = activity.getExtras().getString("accessToken");

        aboutMe = activity.getExtras().getString("aboutMe");
        occupation = activity.getExtras().getString("occupation");
        organizations = activity.getExtras().getString("organizations");
        displayName = activity.getExtras().getString("displayName");
        image_url = activity.getExtras().getString("image_url");
        image_url = image_url.substring(0,image_url.indexOf("?")) + "?sz=300";

        circle_list = activity.getStringArrayExtra("circle_list");
        circle_children_list = GetInfoTask.getArray();
        circle_children_people = GetInfoTask.getCircle_children_people();

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
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent activity = new Intent(getApplicationContext(), LoginActivity.class);
        activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(activity);
        finish();
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
                default:
                    return null;
            }

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
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
            }
            return null;
        }
    }

    /**
     * Fragment containing the user's profile
     */
    public static class MyProfile extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String TITLE = "Profile";

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.profile_fragment, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.icon);

            //download and display image from url
            imageLoader.displayImage(image_url, imageView, options);

            TextView profile_name = (TextView) rootView.findViewById(R.id.profile_name);
            if (displayName != null) {
                profile_name.setText(displayName);
            }
            else{
                displayName = "User";
                profile_name.setText("User does not have a displayName");
            }

            TextView profile_info = (TextView) rootView.findViewById(R.id.profile_info);
            if (occupation != null) {
                profile_info.setText("Occupation: " + occupation + "\n");
            }
            else{
                profile_info.setText("No occupation set" + "\n");
            }

            if (organizations != null) {
                profile_info.append("Organizations: " + organizations + "\n");
            }
            else{
                profile_info.append("Not in any organizations" + "\n");
            }

            if (aboutMe != null) {
                profile_info.append("About " + displayName + ": " + aboutMe + "\n");
            }
            else{
                profile_info.append(displayName + " does not have an About Me description" + "\n");
            }

            Button email = (Button) rootView.findViewById(R.id.button_email);
            email.setVisibility(View.INVISIBLE);    //Hide button on user's own profile
            return rootView;
        }
    }

    /**
     * Fragment containing the user's circles of friends
     */
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
        public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.circle_main, container, false);
            ExpandableListView list = (ExpandableListView) rootView.findViewById(R.id.listView);
            list.setAdapter(new FriendListAdapter(this.getActivity()));
            list.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                //Set child listener. clicking on friend under a circle will open the friend's profile
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    String friendDisplayName;
                    String friendOrganizations = "";
                    String friendAboutMe;
                    String friendImage_url;
                    String friendOccupation;

                    Intent activity = new Intent(parent.getContext(), FriendProfile.class);
                    activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    Person friend = circle_children_people[groupPosition][childPosition];
                    friendDisplayName = friend.getDisplayName();
                    activity.putExtra("friendDisplayName", friendDisplayName);
                    friendAboutMe = friend.getAboutMe();
                    activity.putExtra("friendAboutMe", friendAboutMe);
                    friendImage_url = friend.getImage().getUrl();
                    activity.putExtra("friendImage_url", friendImage_url);
                    friendOccupation = friend.getOccupation();
                    activity.putExtra("friendOccupation", friendOccupation);

                    if(friend.getOrganizations() != null) {
                        List<Person.Organizations> tmp = friend.getOrganizations();
                        for (Person.Organizations o: tmp){
                            friendOrganizations = organizations + " " + o.getName() + ",";
                        }
                        friendOrganizations = friendOrganizations.substring(0, friendOrganizations.length()-1); //remove the last comma
                        activity.putExtra("friendOrganizations", friendOrganizations);
                    }

                    startActivity(activity);
                    return true;
                }
            });
            return rootView;
        }

        public class FriendListAdapter extends BaseExpandableListAdapter {
            public LayoutInflater inflater;
            public Activity activity;
            private String[] circles = circle_list;
            private String[][] circle_children = (String[][])circle_children_list;

            public FriendListAdapter (Activity activity) {
                this.activity = activity;
                inflater = activity.getLayoutInflater();
            }

            @Override
            public int getGroupCount() {
                return circles.length;
            }

            @Override
            public int getChildrenCount(int groupPosition) {
                System.out.println("getChildrenCount: " + groupPosition);
                return circle_children[groupPosition].length;
            }

            @Override
            public Object getGroup(int groupPosition) {
                return circles[groupPosition];
            }

            @Override
            public Object getChild(int groupPosition, int childPosition) {
                return circle_children[groupPosition][childPosition];
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
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.circle_list, null);
                }

                ((CheckedTextView) convertView).setText(getGroup(groupPosition).toString());
                ((CheckedTextView) convertView).setChecked(isExpanded);
                return convertView;
            }

            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.entry, null);
                }

                convertView.setClickable(false);
                TextView textView = (TextView) convertView.findViewById(R.id.entry);
                textView.setText(getChild(groupPosition, childPosition).toString());

                Person friend = circle_children_people[groupPosition][childPosition];
                ImageView icon = (ImageView) convertView.findViewById(R.id.mini_icon);
                imageLoader.displayImage(friend.getImage().getUrl(), icon, options);
                return convertView;
            }

            @Override
            public boolean isChildSelectable(int groupPosition, int childPosition) {
                return true;
            }
        }
    }
}
