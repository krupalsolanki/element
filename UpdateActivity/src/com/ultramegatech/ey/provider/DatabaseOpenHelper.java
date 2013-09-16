
package com.ultramegatech.ey.provider;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    /* Schema version */
    public static final int VERSION = 1;
    /* Database file name */
    private static final String DB_PATH = "data/data/com.ultramegatech.ey/databases/";
	private static final String DB_NAME = "elements.db";

	private SQLiteDatabase myDataBase;
	private final Context myContext;
    /* Schema for 'elements' table */

    public DatabaseOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        this.myContext = context;
    }
    public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {
			Log.i("db", DB_PATH);
			this.getReadableDatabase(); // create empty database

			try {
				copyDataBase();
			} catch (IOException e) {
				throw new Error("Error copying database");
			}
		}
	}

	private boolean checkDataBase() {
		SQLiteDatabase checkDB = null;
		try {
			String myPath = DB_PATH + DB_NAME;
			checkDB = SQLiteDatabase.openDatabase(myPath, null,
					SQLiteDatabase.OPEN_READONLY);
		} catch (SQLiteException e) {
			Log.i("SQLError", e.getMessage()); // database does't exist yet.
		}
		if (checkDB != null) {
			checkDB.close();
		}
		return checkDB != null ? true : false;
	}

	private void copyDataBase() throws IOException {
		// Open your local db as the input stream
		InputStream myInput = myContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	@Override
	public synchronized void close() {
		if (myDataBase != null)
			myDataBase.close();
		super.close();
	}

	public void checkValue() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("select * from elements where num=2", null);
		if (c.moveToFirst()) {
			do {
				String record = Integer.toString(c.getColumnIndex("_id")) + " "
						+ c.getString(c.getColumnIndex("name"));
				Log.i("print sample", record);
			} while (c.moveToNext());
		}
	}

    @Override
    public void onCreate(SQLiteDatabase db) {
       // db.execSQL(SCHEMA_ELEMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      //  db.execSQL("DROP TABLE " + Elements.TABLE_NAME + ";");
      //  onCreate(db);
    }
}