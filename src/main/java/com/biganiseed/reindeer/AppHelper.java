package com.biganiseed.reindeer;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.biganiseed.reindeer.data.App;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import static android.provider.BaseColumns._ID;

public class AppHelper {
	// define table and column name for compilation checking.
	public static final String TABLE_NAME = "apps";
	public static final String ID = _ID;
	public static final String PACKAGE = "package";
	public static final String NAME = "name";
	public static final String DETAILS = "details";
	//public static final String IS_CLOUDED = "is_clouded";
	public static final String IS_SYSTEM = "is_system";
	
	
	protected Context mContext;
	
	public AppHelper(Context context) {
		mContext = context;
	}

	public BaseDbHelper getHelper(){
		return BaseDbHelper.getHelper(mContext);
	}

	public long addApp(App app){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		long ret = addApp(db, app);
		return ret;
	}
	public long addApp(SQLiteDatabase db, App app){
		ContentValues values = new ContentValues();
		values.put(PACKAGE, app.getPackage());
		values.put(NAME, app.getName());
		values.put(DETAILS, app.getJSON().toString());
		values.put(IS_SYSTEM, app.isSystem());
		long ret = db.insertOrThrow(TABLE_NAME, null, values);
		return ret;
	}

	public Cursor getApps(SQLiteDatabase db, boolean isSystem){
		Cursor ret = db.query(TABLE_NAME, null, IS_SYSTEM + "=" + (isSystem ? 1 : 0), null, null, null, NAME + "," + PACKAGE);
		return ret;
	}

	public JSONArray getApps(boolean isSystem){
		JSONArray ret = new JSONArray();
		SQLiteDatabase db = getHelper().getReadableDatabase();
		Cursor cursor = getApps(db, isSystem);
		while (cursor.moveToNext()){
			try {
				JSONObject json = new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(DETAILS)));
				ret.put(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		return ret;
	}

	public int getAppCount(){
		SQLiteDatabase db = getHelper().getReadableDatabase();
		Cursor c = db.query(TABLE_NAME, new String[]{"count(*)"}, null, null, null, null, null);
		c.moveToFirst();
		int ret = c.getInt(0);
		c.close();
		return ret;
	}

	public void clearAll(){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		db.delete(TABLE_NAME, null, null);
	}

	public App deleteApp(String packageName){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		App app = getApp(db, packageName);
		db.delete(TABLE_NAME, PACKAGE + "='" + packageName + "'", null);
		return app;
	}

	public void updateOrAddApp(App app){
		SQLiteDatabase db = getHelper().getWritableDatabase();
		updateOrAddApp(db, app);
	}

	public void updateOrAddApp(SQLiteDatabase db, App app){
		ContentValues values = new ContentValues();
		values.put(PACKAGE, app.getPackage());
		values.put(NAME, app.getName());
		values.put(DETAILS, app.getJSON().toString());
		values.put(IS_SYSTEM, app.isSystem());
		int ret = db.update(TABLE_NAME, values, PACKAGE + "='" + app.getPackage() + "'", null);
		if(ret == 0) addApp(db, app);
	}

	public App getApp(String packageName){
		SQLiteDatabase db = getHelper().getReadableDatabase();
		App ret = getApp(db, packageName);
		return ret;
	}

	public App getApp(SQLiteDatabase db, String packageName){
		Cursor cursor = db.query(TABLE_NAME, null, PACKAGE + "='" + packageName + "'", null, null, null, null, "1");
		App ret = null;
		if (cursor.moveToNext()){
			JSONObject json;
			try {
				json = new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(DETAILS)));
				ret = new App(json);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		cursor.close();
		return ret;
	}

}
