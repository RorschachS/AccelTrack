package com.example.healthtrack;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseDML {
	public static final String KEY_TIMESTAMP="timestamp";
	public static final String KEY_XDATA="xdata";
	public static final String KEY_YDATA="ydata";
	public static final String KEY_ZDATA="zdata";
	
	private static final String DATABASE_NAME="AccelDB";
	private static final String DATABASE_TABLE="Name_Id_Age_Sex";
	private static final int DATABASE_VERSION=1;
	
	private DBhelper ourHelper;
	private final Context ourContext;
	private SQLiteDatabase ourDatabase;
	
	private static class DBhelper extends SQLiteOpenHelper{

		public DBhelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE "+ DATABASE_TABLE +"("
			+KEY_TIMESTAMP+" INTEGER PRIMARY KEY, "
			+KEY_XDATA+" REAL, "
			+KEY_YDATA+" REAL, "
			+KEY_ZDATA+" REAL );"
			);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXIST "+DATABASE_TABLE );
			onCreate(db);
		}
		
	}
	
	public DataBaseDML(Context c){
		ourContext=c;
	}
	
	public DataBaseDML open() throws SQLException{
		ourHelper=new DBhelper(ourContext);
		ourDatabase=ourHelper.getWritableDatabase();
		return this;
	}
	
	
	public void close(){
		ourHelper.close();
	}

	public void createEntry(long timestamp, float xvalue, float yvalue, float zvalue) {
		ContentValues cv=new ContentValues();
		cv.put(KEY_TIMESTAMP, timestamp);
		cv.put(KEY_XDATA, xvalue);
		cv.put(KEY_YDATA, yvalue);
		cv.put(KEY_ZDATA, zvalue);
		ourDatabase.insert(DATABASE_TABLE, null, cv);
	}

	public ArrayList<Name_ID_Age_Sex> getData() {
		ArrayList<Name_ID_Age_Sex> list=new ArrayList<Name_ID_Age_Sex>();
		String []columns= new String[]{KEY_TIMESTAMP,KEY_XDATA,KEY_YDATA,KEY_ZDATA};
		Cursor c=ourDatabase.query(DATABASE_TABLE, columns, null, null, null, null, null);
		int timestamp=c.getColumnIndex("timestamp");
		int xVal=c.getColumnIndex("x_data");
		int yVal=c.getColumnIndex("y_data");
		int zVal=c.getColumnIndex("z_data");
		for(c.moveToFirst(); !c.isAfterLast();c.moveToNext()){
			long iTimeStamp=c.getLong(timestamp);
			float iXVal=c.getFloat(xVal);
			float iYVal=c.getFloat(yVal);
			float iZVal=c.getFloat(zVal);
			Name_ID_Age_Sex nm=new Name_ID_Age_Sex(iTimeStamp,iXVal,iYVal,iZVal);
			list.add(nm);
		}
		return list;
	}

}
