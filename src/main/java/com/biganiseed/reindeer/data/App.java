package com.biganiseed.reindeer.data;

import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONException;
import org.json.JSONObject;

import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.Tools;


import android.R.integer;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;


public class App {
	// identity
	public static final String PACKAGE = "package";
	public static final String SIGNATURE = "signature";
	// local property
	public static final String VERSION_CODE = "version_code";
	public static final String VERSION_NAME = "version_name";
	public static final String NAME = "name";
	public static final String PATH = "path";
	public static final String ICON = "icon";
	public static final String BANNER = "banner";

	public static final String DEV = "dev";
	public static final String UPDATED_AT = "updated_at";
	public static final String ENABLED = "enabled";
	public static final String DESCRIPTION = "description";
	public static final String CONTACT = "contact";

	public static final String IS_SYSTEM = "is_system";
	public static final String IS_SHAREABLE = "is_shareable";
	// temperary status
	public static final String STATUS = "status";
	

	private JSONObject mJson = null;
	
	public boolean isLocalNew(Context context){
		return (getLocalVersionCode(context) > getVersionCode());
	}

	public int getLocalVersionCode(Context context){
		int ret = -1;
	   	PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(getPackage(), PackageManager.GET_META_DATA);
			ret = pi.versionCode;
		} catch (NameNotFoundException e) {
//			e.printStackTrace();
		}
		return ret; 
	}

	public boolean isSystem(){
		return mJson.optBoolean(IS_SYSTEM);
	}

	public String getName(){
		return mJson.optString(NAME, null);
	}
	public JSONObject getDev(){
		return mJson.optJSONObject(DEV);
	}
	public String getPackage(){
		return mJson.optString(PACKAGE, null);
	}
	public int getVersionCode(){
		return mJson.optInt(VERSION_CODE, -1);
	}
	public String getVersionName(){
		return mJson.optString(VERSION_NAME, null);
	}
	public double getUpdatedAt(){
		return mJson.optDouble(UPDATED_AT);
	}
	public String getDescription(){
		return mJson.optString(DESCRIPTION, null);
	}
	public String getContact(){
		return mJson.optString(CONTACT, null);
	}
	public boolean getEnabled(){
		return mJson.optBoolean(ENABLED, true);
	}
	public String getApkPath(){
		return mJson.optString(PATH, null);
	}
	public String getIconPath(){
		return Const.TMP_FOLDER+"/"+getIcon();
	}
	public String getIcon(){
		return mJson.optString(ICON, null);
	}
	public String getBanner(){
		return mJson.optString(BANNER, null);
	}
	public String getSignature(){
		return mJson.optString(SIGNATURE, null);
	}
	public int getStatus(){
		return mJson.optInt(STATUS);
	}
	public long getSize(){
		long ret = 0;
		if(getApkPath() != null){
			File f = new File(getApkPath());
			ret = f.length(); 
		}
		return ret;
	}

	public App(){
	}
	public App(JSONObject data){
		mJson = data;
	}
	public App(PackageManager pm, String packageName) throws NameNotFoundException{
		PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA | PackageManager.GET_SIGNATURES);
		setBy(pm, info);
	}
	public App(PackageManager pm, PackageInfo info) {
		setBy(pm, info);
	}

	public JSONObject getJSON(){
		return mJson;
	}
	
	public void setBy(PackageManager pm, PackageInfo info){
		try {
			if(mJson == null) mJson = new JSONObject();
			mJson.put(PACKAGE, info.packageName);
			mJson.put(VERSION_CODE, info.versionCode);
			mJson.put(VERSION_NAME, info.versionName);
			mJson.put(PATH, info.applicationInfo.sourceDir);
			mJson.put(NAME, info.applicationInfo.loadLabel(pm));
		    mJson.put(ICON, saveIcon(pm, info));
		    if((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (info.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0)
		    	mJson.put(IS_SYSTEM, true);
		    if((info.applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0)
		    	mJson.put(IS_SHAREABLE, true);
		    	
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static public String saveIcon(PackageManager pm, PackageInfo info){
		String ret = null;
		try {
			Drawable bd = info.applicationInfo.loadIcon(pm);
			if(bd != null){
				Bitmap bm = ((BitmapDrawable)bd).getBitmap();
				String pathName = Tools.getImageFileName(info.packageName);
//				File f = new File(Const.TMP_FOLDER+"/"+getIconFileName(info));
				File f = new File(pathName);
		        FileOutputStream out = new FileOutputStream(f);   
		        bm.compress(Bitmap.CompressFormat.PNG, 100, out);
		        ret = f.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static String getIconFileName(PackageInfo info){
		return info.packageName + ".png";
	}

	public static boolean isSelected(Context context, String packageName){
		boolean ret = false;
		String appsString = Tools.getPrefString(context, "bypass_apps", "");
		String[] apps = appsString.split("\n");
		for(int i=0; i<apps.length; i++){
			if(apps[i].equalsIgnoreCase(packageName)){
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	public static void saveSelectedApp(Context context, String packageName, boolean isSelected){
		String appsString = Tools.getPrefString(context, "bypass_apps", "");
		String[] apps = appsString.split("\n");
		// remove the app
		StringBuffer buf = new StringBuffer();
		for(int i=0; i<apps.length; i++){
			if(!apps[i].equalsIgnoreCase(packageName)){
				buf.append(apps[i]);
				buf.append("\n");
			}
		}
		
		if(isSelected) buf.append(packageName);
		Tools.setPrefString(context, "bypass_apps", buf.toString().trim());
	}
	
	public void saveAsSelected(Context context, boolean isSelected){
		App.saveSelectedApp(context, this.getPackage(), isSelected);
	}

	public boolean isSelected(Context context){
		return App.isSelected(context, this.getPackage());
	}
	
	static public int getSelectedAppsCount(Context context){
		String appsString = Tools.getPrefString(context, "bypass_apps", "");
		if(appsString.isEmpty()) return 0;
		
		String[] apps = appsString.split("\n");
		return apps.length;
	}

}
