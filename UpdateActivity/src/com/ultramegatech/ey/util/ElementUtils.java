
package com.ultramegatech.ey.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import com.ultramegatech.ey.R;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class ElementUtils {
    private Context mContext;
    
    /* Map of values to colors */
    private HashMap<Object, Integer> mColorMap;
    
    /**
     * Constructor
     * 
     * @param context
     */
    public ElementUtils(Context context) {
        mContext = context;
    }
    
    /**
     * Get the element color based on the category name.
     * 
     * @param context
     * @param key The category name
     * @return Color hex value
     */
    public int getElementColor(String key) {
        if(mColorMap == null) {
            mColorMap = getColorMap(mContext);
        }
        
        return mColorMap.get(key);
    }
    
    /**
     * Load the element color map from array resources.
     * 
     * @param context
     * @return 
     */
    public static HashMap<Object, Integer> getColorMap(Context context) {
        final HashMap<Object, Integer> colorMap = new LinkedHashMap<Object, Integer>();
        
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String colorKey = prefs.getString("elementColors", "category");
        
        final int keyArray;
        final int colorArray;
        if(colorKey.equals("block")) {
            keyArray = R.array.ptBlocks;
            colorArray = R.array.ptBlockColors;
        } else {
            keyArray = R.array.ptCategories;
            colorArray = R.array.ptCategoryColors;
        }

        final Resources res = context.getResources();
        final String[] colorKeys = res.getStringArray(keyArray);
        final int[] colorValues = res.getIntArray(colorArray);

        if(colorKeys != null && colorValues != null && colorValues.length >= colorKeys.length) {
            for(int i = 0; i < colorKeys.length; i++) {
                colorMap.put(colorKeys[i], colorValues[i]);
            }
        }
        
        return colorMap;
    }
}