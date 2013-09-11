package com.ultramegatech.ey;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.SectionIndexer;

import com.ultramegatech.ey.provider.Elements;
import com.ultramegatech.ey.util.ElementUtils;

public class ElementListActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener,
		OnQueryTextListener, OnCloseListener {

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mOptions;
	private String mSort;

	/* Keys for saving instance state */

	private static final String KEY_SORT = "key_sort";
	private SearchView mSearchView;
	/* Fields to read from the database */
	private final String[] mListProjection = new String[] { Elements._ID,
			Elements.NUMBER, Elements.SYMBOL, Elements.NAME, Elements.CATEGORY };

	/* Mapping of fields to views */
	private final String[] mListFields = new String[] { Elements.NUMBER,
			Elements.SYMBOL, Elements.NAME, Elements.CATEGORY };
	private final int[] mListViews = new int[] { R.id.number, R.id.symbol,
			R.id.name, R.id.block };

	/* Sort directions */
	private static String SORT_ASC = "ASC";
	/* The list */
	private ListView mListView;

	/* List adapter */
	private SimpleCursorAdapter mAdapter;

	/* Current value to filter results by */
	private String mFilter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		/*
		 * ActionBarWrapper.getInstance(this).setDisplayHomeAsUpEnabled(true);
		 */
		setContentView(R.layout.element_list);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mFilter = getIntent().getStringExtra("query");
			Log.i("query", mFilter);
		}
		mTitle = mDrawerTitle = getTitle();
		mOptions = getResources().getStringArray(R.array.options_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, mOptions));

		Log.e("drawer set ", "drawer");
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			@Override
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final Intent intent = new Intent(getApplicationContext(),
						ElementDetailsActivity.class);
				intent.putExtra(ElementDetailsActivity.EXTRA_ELEMENT_ID, id);
				startActivity(intent);
			}
		});

		loadPreferences();

		if (savedInstanceState != null) {
			mSort = savedInstanceState.getString(KEY_SORT);
		}

		setupAdapter();
		// setupFilter();
		// setupSort();

		getSupportLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_SORT, mSort);
	}

	public static class MySearchView extends SearchView {
		public MySearchView(Context context) {
			super(context);
		}

		// The normal SearchView doesn't clear its search text when
		// collapsed, so we will do this for it.
		@Override
		public void onActionViewCollapsed() {
			setQuery("", false);
			super.onActionViewCollapsed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);

		// Get the SearchView and set the searchable configuration
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView mSearchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
		// Assumes current activity is the searchable activity
		mSearchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		mSearchView.setIconifiedByDefault(true); // Do not iconify the widget;
													// expand it by default
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setOnCloseListener(this);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action buttons
		switch (item.getItemId()) {
		case R.id.action_settings:

			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	/**
	 * Load relevant shared preferences.
	 */
	private void loadPreferences() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		Log.i("List created in fragment", "list created ");
		final String colorKey = prefs.getString("elementColors", "category");
		if (colorKey.equals("block")) {
			mListProjection[4] = Elements.BLOCK;
			mListFields[3] = Elements.BLOCK;
		} else {
			mListProjection[4] = Elements.CATEGORY;
			mListFields[3] = Elements.CATEGORY;
		}
		final String sortOrder = prefs.getString("sortBy", "Atomic Number");
		Log.i("sortBy", sortOrder);
		if (sortOrder.equalsIgnoreCase("atomic number")) {
			mSort = Elements.NUMBER + " " + SORT_ASC;
		} else {
			mSort = Elements.NAME + " " + SORT_ASC;
		}

	}

	/**
	 * Create and configure the list adapter.
	 */
	public class CustomAdapter extends SimpleCursorAdapter implements
			SectionIndexer {
		public CustomAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			final String alphabet = getApplicationContext().getString(
					R.string.alphabet);
			mAlphabetIndexer = new AlphabetIndexer(null, 3, alphabet);
		}

		private AlphabetIndexer mAlphabetIndexer;

		@Override
		public Cursor swapCursor(Cursor newCursor) {
			// Update the AlphabetIndexer with new cursor as well
			mAlphabetIndexer.setCursor(newCursor);
			return super.swapCursor(newCursor);
		}

		@Override
		public int getPositionForSection(int section) {
			if (getCursor() == null) {
				return 0;
			}
			return mAlphabetIndexer.getPositionForSection(section);
		}

		@Override
		public int getSectionForPosition(int position) {
			if (getCursor() == null) {
				return 0;
			}
			return mAlphabetIndexer.getSectionForPosition(position);
		}

		@Override
		public Object[] getSections() {
			return mAlphabetIndexer.getSections();
		}

	}

	private void setupAdapter() {
		mAdapter = new CustomAdapter(this, R.layout.element_list_item, null,
				mListFields, mListViews, 0);

		final ElementUtils elementUtils = new ElementUtils(this);
		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			public boolean setViewValue(View view, Cursor cursor, int i) {
				if (view instanceof RelativeLayout) {
					final int background = elementUtils.getElementColor(cursor
							.getString(i));
					view.setBackgroundColor(background);
					return true;
				}

				return false;
			}
		});

		mListView.setAdapter(mAdapter);
	}

	/**
	 * Restart the Cursor Loader.
	 */
	private void restartLoader() {
		getSupportLoaderManager().restartLoader(0, null, this).forceLoad();
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		setProgressBarIndeterminateVisibility(true);

		final Uri uri;
		if (TextUtils.isEmpty(mFilter)) {
			uri = Elements.CONTENT_URI;
		} else {
			uri = Uri.withAppendedPath(Elements.CONTENT_URI_FILTER, mFilter);
		}

		return new CursorLoader(this, uri, mListProjection, null, null, mSort);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor d) {
		setProgressBarIndeterminateVisibility(false);

		mAdapter.swapCursor(d);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("elementColors") || (key.equals("sortBy"))) {
			getSupportLoaderManager().destroyLoader(0);
			loadPreferences();
			setupAdapter();
			restartLoader();
		}

	}

	@Override
	public boolean onClose() {
		if (!TextUtils.isEmpty(mSearchView.getQuery())) {
			mSearchView.setQuery(null, true);
		}

		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		// Called when the action bar search text has changed. Update
		// the search filter, and restart the loader to do a new query
		// with this filter.
		String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
		// Don't do anything if the filter hasn't actually changed.
		// Prevents restarting the loader when restoring state.
		if (mFilter == null && newFilter == null) {
			return true;
		}
		if (mFilter != null && mFilter.equals(newFilter)) {
			return true;
		}
		mFilter = newFilter;
		restartLoader();
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		// TODO Auto-generated method stub
		return false;
	}

	private void selectItem(int position) {
		// update the main content by replacing fragments
		// update the main content by replacing fragments
		if (position == 1) {
			Fragment fragment = new PeriodicTable();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mOptions[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		if (position == 2) {
			Fragment fragment = new ElementList();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mOptions[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		if (position == 3) {
			Fragment fragment = new HelperActivity();
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mOptions[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		if (position == 4) {
			this.finish();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			int pid = android.os.Process.myPid();
			android.os.Process.killProcess(pid);
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	/**
	 * Fragment that appears in the "content_frame", shows a planet
	 */

	public static class ElementList extends Fragment {
		public ElementList() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.element_list, container,
					false);

			Intent intent = new Intent(getActivity(), ElementListActivity.class);
			getActivity().setTitle(R.string.menuList);
			getActivity().startActivity(intent);
			return rootView;
		}
	}

	public static class PeriodicTable extends Fragment {
		public PeriodicTable() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.periodic_table,
					container, false);
			Intent intent = new Intent(getActivity(),
					PeriodicTableActivity.class);
			getActivity().setTitle(R.string.titlePeriodicTable);
			getActivity().startActivity(intent);
			return rootView;
		}
	}

	public static class HelperActivity extends Fragment {
		public HelperActivity() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.activity_help, container,
					false);

			Intent intent = new Intent(getActivity(), HelpActivity.class);
			getActivity().setTitle(R.string.menuList);
			getActivity().startActivity(intent);
			return rootView;
		}
	}
}