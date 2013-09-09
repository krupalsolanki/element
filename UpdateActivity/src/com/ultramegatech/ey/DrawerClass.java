package com.ultramegatech.ey;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.ultramegatech.ey.ElementListActivity;
import com.ultramegatech.ey.PeriodicTableActivity;
import com.ultramegatech.ey.R;
import com.ultramegatech.ey.SettingsActivity;

public class DrawerClass extends FragmentActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	public ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mOptions;

	public DrawerClass(Context context) {
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

	}

	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		}
	}

	public void selectItem(int position) {
		// update the main content by replacing fragments
		if (position == 3) {
			Fragment fragment = new PeriodicTable();
			/*
			 * Bundle args = new Bundle();
			 * args.putInt(ChemicalEquation.ARG_PLANET_NUMBER, position);
			 * fragment.setArguments(args);
			 */

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mOptions[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}

		if (position == 1) {

			/*
			 * getSupportFragmentManager().beginTransaction().replace(R.id.
			 * content_frame, new ElementListFragment()).commit();
			 */
			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mOptions[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		if (position == 2) {
			Fragment fragment = new ElementList();
			/*
			 * Bundle args = new Bundle();
			 * args.putInt(ChemicalEquation.ARG_PLANET_NUMBER, position);
			 * fragment.setArguments(args);
			 */

			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.content_frame, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			setTitle(mOptions[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		}
		if (position == 4) {
			finish();
		}

	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	public void hideDrawer(Menu menu) {
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
	}

	public boolean handleSelect(Context context, MenuItem menuItem) {
		if (mDrawerToggle.onOptionsItemSelected(menuItem)) {
			return true;
		}
		int id = menuItem.getItemId();
		switch (id) {
		case R.id.action_settings:
			launchOptionsActivity(context);
			break;
		default:
			return super.onOptionsItemSelected(menuItem);
		}
		
		return super.onOptionsItemSelected(menuItem);
	}

	private void launchOptionsActivity(Context context) {
		context.startActivity(new Intent(context, SettingsActivity.class));
	}
	public static class ElementList extends Fragment {
		public ElementList() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.element_list,
					container, false);
			
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
			Intent intent = new Intent(getActivity(), PeriodicTableActivity.class);
			getActivity().setTitle(R.string.titlePeriodicTable);
			getActivity().startActivity(intent);
			return rootView;
		}
	}
}

