package com.biganiseed.reindeer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class Const {
	public static boolean IS_RESELLER = false ; // if this build is for reseller

	public static String APP_NAME = "Reindeer";
	public static final int HTTP_TIMEOUT = 10000;
	public static final String PREFS = APP_NAME;
	public static final String MINI_KEY = "20130426113409";
	public static final String ORDER_TIME_FORMAT = "yyyyMMddHHmmss";
	public static final String ORDER_PREFIX = "RE";
//	public static String DNS_URL = "http://www.douban.com/note/317378727/"; // REDNS
//	public static String DNS_URL_DEFAULT = "http://about.me/alex_liao/collections/redns"; // for oversea users
	public static String DNS_URL_DEFAULT = "http://r1.ba3.info/dns";
	public static String DNS_URL_VIP = "http://r1.ba3.info/dnsvip";
	
	public static final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.biganiseed.reindeer";
	public static final String SHARE_URL = "http://xunluvpn.org/?ref=ra";
	public static String TMP_FOLDER = "";
	public static  String BROADCAST_DOWNLOAD_PROGRESS = "com.biganseed.reindeer.DOWNLOAD_PROGRESS";
	public static final int MULITI_DOWNLOADING = 4;
	public static final boolean DEFAULT_SMART_ROUTE = true;
//	public static final String EXEC_PATH = "/data/data/com.biganiseed.reindeer/";
	
	public static  String BROADCAST_REFRESH_APP = "com.biganseed.reindeer.REFRESH_APP";
	public static  String BROADCAST_CACHE_APPS_PROGRESS = "com.biganseed.reindeer.CACHE_APPS_PROGRESS";
	public static final String KEY_PERCENT = "percent";
	public static final String KEY_PACKAGE = "package";
	public static final String KEY_APP = "app";
	public static final String KEY_COUNT = "count";
	public static final String KEY_TOTAL = "total";
	public static final String KEY_REFRESH = "refresh";
	public static final String KEY_FINISHED = "finished";

    public static String getRootHttpNoSSL(Context context){
        return "http://"+getRootIp(context);
    }

   	public static String getRootHttp(Context context){
		return "https://"+getRootIp(context);
	}

	public static String getRootIp(final Context context){
		String result = Tools.getPrefString(context, getRootIpKey(context), getDefaultRootIp(context));
//		return "192.168.1.62:3000";
//		result = "50.97.204.3";
//		result = "192.168.60.108:3000";
		// result = "10.0.1.2:3000";
//		result = "117.121.25.237";
		if(result == null){
			 Api.checkDns(context);
			 result = Tools.getPrefString(context, getRootIpKey(context), null);
		}
		return result;
	}
	
	static String getDefaultRootIp(Context context){
//		String ret = "198.23.109.86";
//		String ret = "117.121.26.210";
//		String ret = "119.254.110.183";
//		String ret = "45.56.85.252";
		// String ret = "173.230.146.247";
		String ret = null;
//		if(Tools.isVip(Tools.getCurrentUser(context))) ret = "66.175.221.153" ;
//		try{
//			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
//			String country = tm.getNetworkCountryIso();
//			if(country.equalsIgnoreCase("cn")) ret = "117.121.26.210";
//		}catch(Exception e){
//			Log.e(Const.APP_NAME, "getDefaultRootIp: "+e.getMessage());    	
//		}
		return ret;
	}

	public static String getRootIpKey(Context context){
		PackageInfo pi = null;
		try {
			pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String ret = "root_ip_"+pi.versionCode;
		// if(Tools.isVip(Tools.getCurrentUser(context))) ret = "root_ip_vip_"+pi.versionCode;
		return ret;
	}
}
