
package com.ultramegatech.ey;

import android.app.SearchManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.ultramegatech.ey.provider.Elements;
import com.ultramegatech.ey.util.ElementUtils;
import com.ultramegatech.util.ActionBarWrapper;
import com.ultramegatech.util.UnitUtils;

public class ElementDetailsActivity extends FragmentActivity implements
        LoaderCallbacks<Cursor>, OnSharedPreferenceChangeListener, OnQueryTextListener {
    /* Intent extras */
	String mFilter;

    public static final String EXTRA_ELEMENT_ID = "element_id";
    public static final String EXTRA_ATOMIC_NUMBER = "atomic_number";
    
    /* Units of measurement */
    private static enum Units {
        KELVIN, CELCIUS, FARENHEIT
    }
    
    /* Values from the database row */
    private final ContentValues mData = new ContentValues();
    
    /* Units to use for temperature values */
    private Units mTemperatureUnits;
    
    /* Field used for coloring the element block */
    private String mColorKey;
    
    /* Header text */
    private TextView mTxtHeader;
    
    /* Element block views */
    private RelativeLayout mElementBlock;
    private TextView mTxtElementSymbol;
    private TextView mTxtElementNumber;
    private TextView mTxtElementWeight;
    private TextView mTxtElementElectrons;
    
    /* Table views */
    private TextView mTxtNumber;
    private TextView mTxtSymbol;
    private TextView mTxtName;
    private TextView mTxtWeight;
    private TextView mTxtConfiguration;
    private TextView mTxtElectrons;
    private TextView mTxtCategory;
    private TextView mTxtGPB;
    private TextView mTxtDensity;
    private TextView mTxtMelt;
    private TextView mTxtBoil;
    private TextView mTxtHeat;
    private TextView mTxtNagativity;
    private TextView mTxtAbundance;
    
    /* Buttons */
    private ImageButton mBtnVideo;
    private ImageButton mBtnWiki;
    
    /* Value to return for unknown values */
    private String mStringUnknown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        ActionBarWrapper.getInstance(this).setDisplayHomeAsUpEnabled(true);
        
        mStringUnknown = getString(R.string.unknown);
        
        setContentView(R.layout.element_details);
        findViews();
        
        loadPreferences();
        
        getSupportLoaderManager().initLoader(0, null, this).forceLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView mSearchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
		// Assumes current activity is the searchable activity
		mSearchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		mSearchView.setIconifiedByDefault(true); // Do not iconify the widget;
													// expand it by default
		mSearchView.setOnQueryTextListener(this);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	final int id = item.getItemId();
        switch(id) {
            case android.R.id.home:
                finish();
                break;
           
		// Handle action buttons
		
		case R.id.action_settings:
		 Intent intent = new Intent(this, SettingsActivity.class);
		 startActivity(intent);
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
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        final String tempUnit = prefs.getString("tempUnit", "K");
        if("K".equals(tempUnit)) {
            mTemperatureUnits = Units.KELVIN;
        } else if("C".equals(tempUnit)) {
            mTemperatureUnits = Units.CELCIUS;
        } else {
            mTemperatureUnits = Units.FARENHEIT;
        }
        
        final String colorKey = prefs.getString("elementColors", "category");
        if(colorKey.equals("block")) {
            mColorKey = Elements.BLOCK;
        } else {
            mColorKey = Elements.CATEGORY;
        }
    }
    
    /**
     * Load references to all views.
     */
    private void findViews() {
        mTxtHeader = (TextView)findViewById(R.id.header);
        
        mElementBlock = (RelativeLayout)findViewById(R.id.elementBlock);
        mTxtElementSymbol = (TextView)findViewById(R.id.elementSymbol);
        mTxtElementNumber = (TextView)findViewById(R.id.elementNumber);
        mTxtElementWeight = (TextView)findViewById(R.id.elementWeight);
        mTxtElementElectrons = (TextView)findViewById(R.id.elementElectrons);
        
        mTxtNumber = (TextView)findViewById(R.id.number);
        mTxtSymbol = (TextView)findViewById(R.id.symbol);
        mTxtName = (TextView)findViewById(R.id.name);
        mTxtWeight = (TextView)findViewById(R.id.weight);
        mTxtConfiguration = (TextView)findViewById(R.id.config);
        mTxtElectrons = (TextView)findViewById(R.id.electrons);
        mTxtCategory = (TextView)findViewById(R.id.category);
        mTxtGPB = (TextView)findViewById(R.id.gpb);
        mTxtDensity = (TextView)findViewById(R.id.density);
        mTxtMelt = (TextView)findViewById(R.id.melt);
        mTxtBoil = (TextView)findViewById(R.id.boil);
        mTxtHeat = (TextView)findViewById(R.id.heat);
        mTxtNagativity = (TextView)findViewById(R.id.negativity);
        mTxtAbundance = (TextView)findViewById(R.id.abundance);
        
        mBtnVideo = (ImageButton)findViewById(R.id.videoButton);
        mBtnVideo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showVideo();
            }
        });
        
        mBtnWiki = (ImageButton)findViewById(R.id.wikiButton);
        mBtnWiki.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showWikipedia();
            }
        });
    }
    
    /**
     * Fill views with data loaded from the database.
     */
    private void populateViews() {
        setTitle(getString(R.string.titleElementDetails, mData.getAsString(Elements.NAME)));
        
        mTxtHeader.setText(mData.getAsString(Elements.NAME));
        
        mTxtElementSymbol.setText(mData.getAsString(Elements.SYMBOL));
        mTxtElementNumber.setText(mData.getAsString(Elements.NUMBER));
        mTxtElementWeight.setText(getWeight());
        populateBlockElectrons();
        setBlockBackground();
        
        mTxtNumber.setText(mData.getAsString(Elements.NUMBER));
        mTxtSymbol.setText(mData.getAsString(Elements.SYMBOL));
        mTxtName.setText(mData.getAsString(Elements.NAME));
        mTxtWeight.setText(getWeight());
        mTxtConfiguration.setText(getElectronConfiguration());
        mTxtElectrons.setText(getElectrons());
        mTxtCategory.setText(mData.getAsString(Elements.CATEGORY));
        mTxtGPB.setText(getGPB());
        mTxtDensity.setText(getDensity());
        mTxtMelt.setText(getTemperature(Elements.MELT));
        mTxtBoil.setText(getTemperature(Elements.BOIL));
        mTxtHeat.setText(getHeat());
        mTxtNagativity.setText(getNegativity());
        mTxtAbundance.setText(getAbundance());
    }
    
    /**
     * Set the block color based on element properties.
     */
    private void setBlockBackground() {
        final String key = mData.getAsString(mColorKey);
        if(key == null) {
            return;
        }
        final int background = new ElementUtils(this).getElementColor(key);
        mElementBlock.setBackgroundColor(background);
    }
    
    /**
     * Fill the column of electrons in the block.
     */
    private void populateBlockElectrons() {
        final String electrons = mData.getAsString(Elements.ELECTRONS);
        if(electrons != null) {
            mTxtElementElectrons.setText(electrons.replace(',', '\n'));
        }
    }
    
    /**
     * Get a value as a temperature string.
     * 
     * @param key The value to read
     * @return The converted temperature string
     */
    private String getTemperature(String key) {
        final Double kelvin = mData.getAsDouble(key);
        if(kelvin != null) {
            switch(mTemperatureUnits) {
                case CELCIUS:
                    return String.format("%.2f Â°C", UnitUtils.KtoC(kelvin));
                case FARENHEIT:
                    return String.format("%.2f Â°F", UnitUtils.KtoF(kelvin));
                default:
                    return String.format("%.2f K", kelvin);
            }
        }
        
        return mStringUnknown;
    }
    
    /**
     * Get the atomic weight. For unstable elements, the value of the most stable isotope is
     * returned surrounded by brackets.
     * 
     * @return The atomic weight
     */
    private String getWeight() {
        final Double value = mData.getAsDouble(Elements.WEIGHT);
        if(value != null) {
            final Boolean unstable = mData.getAsBoolean(Elements.UNSTABLE);
            if(unstable != null && unstable) {
                return "[" + value.intValue() + "]";
            }
            return value.toString();
        }
        return null;
    }
    
    /**
     * Get the electron configuration.
     * 
     * @return The formatted electron configuration
     */
    private Spanned getElectronConfiguration() {
        String value = mData.getAsString(Elements.CONFIGURATION);
        if(value != null) {
            value = value.replaceAll("([spdf])([0-9]+)", "$1<sup><small>$2</small></sup>");
            return Html.fromHtml(value);
        }
        return null;
    }
    
    /**
     * Get the electrons per shell.
     * 
     * @return List of electrons separated by commas
     */
    private String getElectrons() {
        final String value = mData.getAsString(Elements.ELECTRONS);
        if(value != null) {
            return value.replace(",", ", ");
        }
        return null;
    }
    
    /**
     * Get the group, period, and block.
     * 
     * @return Group, period, block
     */
    private String getGPB() {
        String group = mData.getAsString(Elements.GROUP);
        String period = mData.getAsString(Elements.PERIOD);
        String block = mData.getAsString(Elements.BLOCK);
        if(group == null || group.equals("0")) {
            group = "n/a";
        }
        return group + ", " + period + ", " + block;
    }
    
    /**
     * Get the density value with unit.
     * 
     * @return The density
     */
    private String getDensity() {
        final String value = mData.getAsString(Elements.DENSITY);
        if(value != null) {
            return value + " g/cmÂ³";
        }
        return mStringUnknown;
    }
    
    /**
     * Get the heat value with unit.
     * 
     * @return The heat
     */
    private String getHeat() {
        final String value = mData.getAsString(Elements.HEAT);
        if(value != null) {
            return value + " J/gÂ·K";
        }
        return mStringUnknown;
    }
    
    /**
     * Get the electronegativity value with unit.
     * 
     * @return The electronegativity
     */
    private String getNegativity() {
        final String value = mData.getAsString(Elements.NEGATIVITY);
        if(value != null) {
            return value + " V";
        }
        return mStringUnknown;
    }
    
    /**
     * Get the abundance value with unit.
     * 
     * @return The abundance
     */
    private String getAbundance() {
        String value = mData.getAsString(Elements.ABUNDANCE);
        if(value != null) {
            if(value.equals("0")) {
                value = "<0.001";
            }
            return value + " mg/kg";
        }
        return mStringUnknown;
    }
    
    /**
     * Launch YouTube video intent.
     */
    private void showVideo() {
        final String videoId = mData.getAsString(Elements.VIDEO);
        if(videoId == null) {
            return;
        }
        final Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + videoId));
        startActivity(intent);
    }
    
    /**
     * Launch view intent for the Wikipedia page.
     */
    private void showWikipedia() {
        String pageId = mData.getAsString(Elements.WIKIPEDIA);
        if(pageId == null) {
            pageId = mData.getAsString(Elements.NAME);
        }
        if(pageId == null) {
            return;
        }
        final Uri uri = Uri.parse("http://m.wikipedia.org/wiki/" + pageId);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
    
    /**
     * Get the content Uri based on an intent extra.
     * @return 
     */
    private Uri getUri() {
        final Intent intent = getIntent();
        
        long id = intent.getLongExtra(EXTRA_ELEMENT_ID, 0);
        if(id != 0) {
            return ContentUris.withAppendedId(Elements.CONTENT_URI_ID, id);
        } else {
            id = intent.getIntExtra(EXTRA_ATOMIC_NUMBER, 0);
            return ContentUris.withAppendedId(Elements.CONTENT_URI_NUMBER, id);
        }
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        setProgressBarIndeterminateVisibility(true);
        
        return new CursorLoader(this, getUri(), null, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor d) {
        if(d.moveToFirst()) {
            final String[] columns = d.getColumnNames();
            for(int i = 0; i < columns.length; i++) {
                if(d.isNull(i)) {
                    continue;
                }
                
                switch(Elements.getColumnType(columns[i])) {
                    case INTEGER:
                        mData.put(columns[i], d.getInt(i));
                        break;
                    case REAL:
                        mData.put(columns[i], d.getDouble(i));
                        break;
                    case BOOLEAN:
                        mData.put(columns[i], d.getInt(i) == 1);
                        break;
                    case TEXT:
                        mData.put(columns[i], d.getString(i));
                        break;
                }
            }
            
            populateViews();
            
            setProgressBarIndeterminateVisibility(false);
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) { }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("tempUnit")) {
            loadPreferences();
            mTxtMelt.setText(getTemperature(Elements.MELT));
            mTxtBoil.setText(getTemperature(Elements.BOIL));
        } else if(key.equals("elementColors")) {
            loadPreferences();
            setBlockBackground();
        }
    }

	@Override
	public boolean onQueryTextChange(String newText) {

		return false;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		Intent intent = new Intent(getApplicationContext(), ElementListActivity.class);
	     intent.putExtra("query", query);
	     startActivity(intent);
	 return true;
	}
}