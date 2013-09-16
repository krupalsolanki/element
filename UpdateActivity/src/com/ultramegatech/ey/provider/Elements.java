
package com.ultramegatech.ey.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class Elements implements BaseColumns {
    /* Database table name */
    public static final String TABLE_NAME = "elements";
    
    /* Column names */
    public static final String NUMBER = "num";
    public static final String SYMBOL = "sym";
    public static final String NAME = "name";
    public static final String GROUP = "g";
    public static final String PERIOD = "p";
    public static final String BLOCK = "b";
    public static final String WEIGHT = "w";
    public static final String DENSITY = "dens";
    public static final String MELT = "melt";
    public static final String BOIL = "boil";
    public static final String HEAT = "heat";
    public static final String NEGATIVITY = "neg";
    public static final String ABUNDANCE = "ab";
    public static final String CATEGORY = "cat";
    public static final String CONFIGURATION = "ec";
    public static final String ELECTRONS = "eps";
    public static final String UNSTABLE = "uns";
    public static final String VIDEO = "vid";
    public static final String WIKIPEDIA = "wiki";
    
    /* Data types */
    private static final String BASE_TYPE = "com.ultramegatech.ey.element";
    public static final String DATA_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_TYPE;
    public static final String DATA_TYPE_ITEM =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + BASE_TYPE;
    
    /* Content resolver uri bases */
    private static final String SCHEME = "content://";
    public static final Uri CONTENT_URI =
            Uri.parse(SCHEME + ElementsProvider.AUTHORITY + "/elements");
    public static final Uri CONTENT_URI_ID =
            Uri.parse(SCHEME + ElementsProvider.AUTHORITY + "/elements/");
    public static final Uri CONTENT_URI_NUMBER =
            Uri.parse(SCHEME + ElementsProvider.AUTHORITY + "/elements/n/");
    public static final Uri CONTENT_URI_SYMBOL =
            Uri.parse(SCHEME + ElementsProvider.AUTHORITY + "/elements/s/");
    public static final Uri CONTENT_URI_FILTER =
            Uri.parse(SCHEME + ElementsProvider.AUTHORITY + "/elements/filter/");
    
    /* Column types */
    public static enum Type {
        UNDEFINED, TEXT, INTEGER, REAL, BOOLEAN
    }
    
    /**
     * Get the data type of the specified column.
     * 
     * @param c The column name
     * @return 
     */
    public static Type getColumnType(String c) {
        if(c.equals(SYMBOL) || c.equals(NAME) || c.equals(BLOCK) || c.equals(CATEGORY)
                || c.equals(CONFIGURATION) || c.equals(ELECTRONS) || c.equals(VIDEO)
                || c.equals(WIKIPEDIA)) {
            return Type.TEXT;
        }
        if(c.equals(WEIGHT) || c.equals(DENSITY) || c.equals(MELT) || c.equals(BOIL)
                || c.equals(HEAT) || c.equals(NEGATIVITY) || c.equals(ABUNDANCE)) {
            return Type.REAL;
        }
        if(c.equals(NUMBER) || c.equals(GROUP) || c.equals(PERIOD)) {
            return Type.INTEGER;
        }
        if(c.equals(UNSTABLE)) {
            return Type.BOOLEAN;
        }
        
        return Type.UNDEFINED;
    }
    
    /**
     * Constructor
     */
    private Elements() { }
}