package com.biganiseed.reindeer;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import static android.provider.BaseColumns._ID;

public class CacheHelper  {
	// define table and column name for compilation checking.
	public static final String TABLE_NAME = "cache";
	public static final String ID = _ID;
	public static final String DATA = "data";
	public static final String CACHED_AT = "cached_at";
	
	
	protected Context mContext;
	
	public CacheHelper(Context context) {
		mContext = context;
	}

	BaseDbHelper getHelper(){
		return BaseDbHelper.getHelper(mContext);
	}
	public long addCache(String data, String cacheId){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		long ret = addCache(db, data, cacheId);
		return ret;
	}
	public long addCache(SQLiteDatabase db, String data, String cacheId){
		ContentValues values = new ContentValues();
		values.put(ID, cacheId);
		values.put(DATA, data);
		values.put(CACHED_AT, System.currentTimeMillis());
		long ret = db.insertOrThrow(TABLE_NAME, null, values);
		return ret;
	}

	public void clearAll(){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
	}

	public void clearCache(String cacheId){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		db.delete(TABLE_NAME, ID + "='" + cacheId + "'", null);
	}

	public void clearCacheBefore(long days){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		int ret = db.delete(TABLE_NAME, CACHED_AT + " < " + (System.currentTimeMillis()-days*24*3600*1000), null);
		if(ret > 0)	Log.v(Const.APP_NAME, Const.APP_NAME + " CacheHelper cache cleared: "+ret);
	}

	public void cacheData(String data, String cacheId){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		cacheData(db, data, cacheId);
	}

	public void cacheData(SQLiteDatabase db, String data, String cacheId){
		ContentValues values = new ContentValues();
		values.put(DATA, data);
		values.put(CACHED_AT, System.currentTimeMillis());
		int ret = db.update(TABLE_NAME, values, ID + "='" + cacheId + "'", null);
		if(ret == 0) addCache(db, data, cacheId);
	}

	public String loadCache(String cacheId){
		SQLiteDatabase db = getHelper().getReadableDatabase();
		String ret = loadCache(db, cacheId);
		return ret;
	}

	public String loadCache(SQLiteDatabase db, String cacheId){
		String ret = null;
		Cursor cursor = db.query(TABLE_NAME, null, ID + "='" + cacheId + "'", null, null, null, null, "1");
		if (cursor.moveToNext()){
			ret = cursor.getString(cursor.getColumnIndexOrThrow(DATA));
		}
		cursor.close();
		return ret;
	}

	public long getCacheAt(String cacheId) {
		SQLiteDatabase db = getHelper().getReadableDatabase();
		long ret = getCacheAt(db, cacheId);
		return ret;
	}

	public long getCacheAt(SQLiteDatabase db, String cacheId){
		long ret = 0;
		Cursor cursor = db.query(TABLE_NAME, null, ID + "='" + cacheId + "'", null, null, null, null, "1");
		if (cursor.moveToNext()){
			ret = cursor.getLong(cursor.getColumnIndexOrThrow(CACHED_AT));
		}
		cursor.close();
		return ret;
	}

}
