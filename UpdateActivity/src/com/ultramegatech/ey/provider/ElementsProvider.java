
package com.ultramegatech.ey.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;


public class ElementsProvider extends ContentProvider {
    /* Provider authority */
    public static final String AUTHORITY = "com.ultramegatech.ey.provider";
    
    /* Uri matcher ids */
    private static final int ELEMENTS = 1;
    private static final int ELEMENTS_ID = 2;
    private static final int ELEMENTS_NUMBER = 3;
    private static final int ELEMENTS_SYMBOL = 4;
    private static final int ELEMENTS_FILTER = 5;
    
    /* Uri matcher */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "elements", ELEMENTS);
        sUriMatcher.addURI(AUTHORITY, "elements/#", ELEMENTS_ID);
        sUriMatcher.addURI(AUTHORITY, "elements/n/#", ELEMENTS_NUMBER);
        sUriMatcher.addURI(AUTHORITY, "elements/s/*", ELEMENTS_SYMBOL);
        sUriMatcher.addURI(AUTHORITY, "elements/filter/*", ELEMENTS_FILTER);
    }
    
    /* SQLiteOpenHelper */
    private DatabaseOpenHelper mDatabaseOpenHelper;
    
    @Override
    public boolean onCreate() {
        mDatabaseOpenHelper = new DatabaseOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch(sUriMatcher.match(uri)) {
            case ELEMENTS:
                qb.setTables(Elements.TABLE_NAME);
                break;
            case ELEMENTS_ID:
                qb.setTables(Elements.TABLE_NAME);
                qb.appendWhere(Elements._ID + " = " + uri.getLastPathSegment());
                break;
            case ELEMENTS_NUMBER:
                qb.setTables(Elements.TABLE_NAME);
                qb.appendWhere(Elements.NUMBER + " = " + uri.getLastPathSegment());
                break;
            case ELEMENTS_SYMBOL:
                qb.setTables(Elements.TABLE_NAME);
                qb.appendWhere(Elements.SYMBOL + " = ");
                qb.appendWhereEscapeString(uri.getLastPathSegment());
                break;
            case ELEMENTS_FILTER:
                qb.setTables(Elements.TABLE_NAME);
                qb.appendWhere(Elements.NAME + " LIKE ");
                qb.appendWhereEscapeString(uri.getLastPathSegment() + "%");
                qb.appendWhere(" OR " + Elements.SYMBOL + " LIKE ");
                qb.appendWhereEscapeString(uri.getLastPathSegment() + "%");
                break;
            default:
                throw new IllegalArgumentException("Invalid URI: " + uri.toString());
        }
        
        final SQLiteDatabase db = mDatabaseOpenHelper.getReadableDatabase();
        final Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null,
                sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case ELEMENTS:
            case ELEMENTS_FILTER:
                return Elements.DATA_TYPE;
            case ELEMENTS_ID:
            case ELEMENTS_NUMBER:
            case ELEMENTS_SYMBOL:
                return Elements.DATA_TYPE_ITEM;
            default:
                return null;
        }
    }

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}