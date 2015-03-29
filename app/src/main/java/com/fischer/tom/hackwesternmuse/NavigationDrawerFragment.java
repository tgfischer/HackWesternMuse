package com.fischer.tom.hackwesternmuse;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.GridLayout;
import android.widget.GridLayout.Spec;
import android.widget.RelativeLayout;
import android.widget.Button;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements View.OnClickListener {

    private DBAdapter dBAdapter;
    private int numOfContacts = 0;
    private int count = 0;

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (LinearLayout) inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        Button addContact = (Button)mDrawerListView.findViewById(R.id.addContactButton);
        addContact.setOnClickListener(this);

        EditText contactNameInput = (EditText)mDrawerListView.findViewById(R.id.contactNameInput);
        contactNameInput.setHintTextColor(Color.parseColor("#ffc8c8c8"));
        EditText contactPhoneInput = (EditText)mDrawerListView.findViewById(R.id.contactPhoneInput);
        contactPhoneInput.setHintTextColor(Color.parseColor("#ffc8c8c8"));

        dBAdapter = new DBAdapter(getActivity());
        dBAdapter.open();
        populateContactList();

        return mDrawerListView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dBAdapter.close();
    }

    @Override
    public void onClick(View v) {
        //do what you want to do when button is clicked
        if (v.getId() == R.id.addContactButton) {
            //addContact();
            //addNewContact();

            String name = ((EditText)getView().findViewById(R.id.contactNameInput)).getText().toString();
            String phoneString = ((EditText)getView().findViewById(R.id.contactPhoneInput)).getText().toString();

            try
            {
                dBAdapter.insertRow(name, Long.parseLong(phoneString));
                populateContactList();
            }
            catch (NumberFormatException e)
            {
                new AlertDialog.Builder(this.getActivity())
                        .setTitle("Invalid Entry")
                        .setMessage(e.getMessage())
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue on OK
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        } else {
            //delete contact from database based on ID = v.getId()... (0, 1, 2...) numOfContacts

            //GridLayout contactLayout = (GridLayout) getView().findViewById(R.id.grid);
            //contactLayout.removeView((View) v.getParent());
        }
    }

    public void populateContactList() {
        Cursor cursor = dBAdapter.getAllRows();
        //startManagingCursor(cursor);

        String[] fromFieldNames = new String[]{DBAdapter.KEY_NAME, DBAdapter.KEY_PHONE};
        int[] toViewIDs = new int[]{R.id.contactName, R.id.contactPhone};

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(), R.layout.contacts, cursor, fromFieldNames, toViewIDs);

        ListView listView = (ListView)mDrawerListView.findViewById(R.id.contactsListView);
        listView.setAdapter(cursorAdapter);

        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ///parent.getItemAtPosition(position);

                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Contact")
                        .setMessage("Would you like to delete this contact?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dBAdapter.deleteRow(which);

                                populateContactList();
                                Toast.makeText(getActivity(), "Emergency Contact Deleted", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });
    }

    public void removeItem(View v) {

    }

    public void addContact(){ //pass in object from database?

        //RelativeLayout relativeLayout = new RelativeLayout(this.getActivity());
        //relativeLayout.setId(numOfContacts);
        TextView tv1 = new TextView(this.getActivity());
        TextView tv2 = new TextView(this.getActivity());
        Button button = new Button(this.getActivity());
        button.setText("X");

        tv1.setText(((EditText)getView().findViewById(R.id.contactNameInput)).getText());
        tv1.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_Large);
        tv1.setTextColor(Color.parseColor("#FFFFFF"));

        tv2.setText(((EditText)getView().findViewById(R.id.contactPhoneInput)).getText());
        tv2.setTextAppearance(this.getActivity(), android.R.style.TextAppearance_Medium);
        tv2.setTextColor(Color.parseColor("#FFC8C8C8"));

        Spec row10 = GridLayout.spec(count);
        Spec row11 = GridLayout.spec(count,2);

        Spec col1 = GridLayout.spec(0);
        Spec col2 = GridLayout.spec(1);

        GridLayout.LayoutParams first = new GridLayout.LayoutParams(row10, col1);
        GridLayout.LayoutParams second = new GridLayout.LayoutParams(row11, col2);


        if(((EditText)getView().findViewById(R.id.contactNameInput)).getText().toString().matches("") ||
                ((EditText)getView().findViewById(R.id.contactPhoneInput)).getText().toString().matches("")){
            invalidInput();
            return;
        }
        else if(((EditText)getView().findViewById(R.id.contactPhoneInput)).getText().toString().length() < 10 ){
            //|| ((EditText)findViewById(R.id.contactPhoneInput)).getText().toString().length() > 8
            invalidInput();
            return;
        }

        //clear text fields
        ((EditText) getView().findViewById(R.id.contactNameInput)).setText("");
        ((EditText) getView().findViewById(R.id.contactPhoneInput)).setText("");

        // Defining the layout parameters of the TextView
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);

//        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT,
//                RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        float x = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, dm);

        RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp3.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        //lp3.addRule();

        // Setting the parameters on the TextView
        //tv1.setLayoutParams(lp);
        //tv2.setLayoutParams(lp);
        //button.setLayoutParams(lp3);
        //button.setGravity(Gravity.RIGHT);
        button.setId(numOfContacts);
        button.setOnClickListener(this);

        // Setting the RelativeLayout as our content view
        //GridLayout contactLayout = (GridLayout)getView().findViewById(R.id.grid);
        //contactLayout.addView(tv1, first);
        //contactLayout.addView(button, second);
        //contactLayout.addView(tv2);

        numOfContacts++; //for delete button ID
        count += 2;
    }

    public void invalidInput(){

        new AlertDialog.Builder(this.getActivity())
                .setTitle("Invalid Entry")
                .setMessage("Your contact will not be saved.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue on OK
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void clearLayout(){

        //GridLayout lay = (GridLayout)getView().findViewById(R.id.grid);
        //lay.removeAllViews();
        numOfContacts = 0;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            //mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        /*if (item.getItemId() == R.id.action_example) {
            Toast.makeText(getActivity(), "Example action.", Toast.LENGTH_SHORT).show();
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
