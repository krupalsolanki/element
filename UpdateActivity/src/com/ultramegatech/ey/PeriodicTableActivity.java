
package com.ultramegatech.ey;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.ultramegatech.ey.provider.Elements;
import com.ultramegatech.ey.util.ElementUtils;
import com.ultramegatech.widget.PeriodicTableBlock;
import com.ultramegatech.widget.PeriodicTableView;

public class PeriodicTableActivity extends FragmentActivity implements
		LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener {
	/* Fields to read from the database */
	private final String[] mProjection = new String[] { Elements.NUMBER,
			Elements.SYMBOL, Elements.WEIGHT, Elements.GROUP, Elements.PERIOD,
			Elements.CATEGORY, Elements.UNSTABLE };

	/* The main view */
	private PeriodicTableView mPeriodicTableView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.periodic_table);

		mPeriodicTableView = (PeriodicTableView) findViewById(R.id.ptview);

		mPeriodicTableView
				.setOnItemClickListener(new PeriodicTableView.OnItemClickListener() {
					public void onItemClick(PeriodicTableBlock item) {
						final Intent intent = new Intent(
								getApplicationContext(),
								ElementDetailsActivity.class);
						intent.putExtra(
								ElementDetailsActivity.EXTRA_ATOMIC_NUMBER,
								item.number);
						startActivity(intent);
					}
				});

		loadPreferences();

		getSupportLoaderManager().initLoader(0, null, this).forceLoad();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.periodic_table, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final int id = item.getItemId();
		switch (id) {
		case android.R.id.home:
			finish();
			break;

		// Handle action buttons
		case R.id.menu_list:
			Intent intent = new Intent(this, ElementListActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_settings:
			Intent setting = new Intent(this, SettingsActivity.class);
			startActivity(setting);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Load relevant shared preferences.
	 */
	private void loadPreferences() {
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		final String colorKey = prefs.getString("elementColors", "category");
		if (colorKey.equals("block")) {
			mProjection[5] = Elements.BLOCK;
		} else {
			mProjection[5] = Elements.CATEGORY;
		}
	}

	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this, Elements.CONTENT_URI, mProjection, null,
				null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor d) {
		mPeriodicTableView.getLegend().setColorMap(
				ElementUtils.getColorMap(this));

		final ArrayList<PeriodicTableBlock> periodicTableBlocks = new ArrayList<PeriodicTableBlock>();
		PeriodicTableBlock block;

		while (d.moveToNext()) {
			block = new PeriodicTableBlock();
			block.number = d.getInt(0);
			block.symbol = d.getString(1);
			block.subtext = d.getString(2);
			block.group = d.getInt(3);
			block.period = d.getInt(4);
			block.category = d.getString(5);

			if (d.getInt(6) == 1) {
				block.subtext = "[" + Integer.parseInt(block.subtext) + "]";
			}

			periodicTableBlocks.add(block);
		}

		mPeriodicTableView.setBlocks(periodicTableBlocks);
	}

	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("elementColors")) {
			loadPreferences();
			getSupportLoaderManager().restartLoader(0, null, this).forceLoad();
		}
	}
}