package com.biganiseed.reindeer;

import org.json.JSONException;
import org.json.JSONObject;

import com.biganiseed.reindeer.data.App;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class LocalAppRefresher extends BroadcastReceiver {
	static public int claimCode = 1958355937;

    @Override
    public void onReceive(final Context context, Intent intent) {
    	if(Tools.isCaching) return;
       	//Log.d("", Const.APP_NAME + " AppRefresher working...");  
       	//Utils.cacheMyApps(context);
       	//Log.d("", Const.APP_NAME + " AppRefresher done.");
       	final String action = intent.getAction();
       	final String packageName = intent.getData().getSchemeSpecificPart();
       	Log.d("", Const.APP_NAME + " AppRefresher receive: " + action + " " + packageName);
       	final Handler handle = new Handler(); 
       	new Thread(new Runnable(){
			@Override
			public void run() {
		       	PackageManager pm = context.getPackageManager();
				try {
					AppHelper helper = new AppHelper(context);
					App app = null;
			        if(action.equals(Intent.ACTION_PACKAGE_ADDED)){
						app = new App(pm, packageName);
//	            			Utils.cancelNotify(context, app); 
			        	helper.addApp(app);
//				        	Utils.clearCache(context, LocalAppsFragment.cacheId());
			        	Log.v("", Const.APP_NAME + " AppRefresher add: " + packageName);
			        }else if(action.equals(Intent.ACTION_PACKAGE_REMOVED)){
			        	app = helper.deleteApp(packageName);
			        	//Utils.reportRemove(context, app);
//			        	Utils.clearCache(context, LocalAppsFragment.cacheId());
				       	Log.v("", Const.APP_NAME + " AppRefresher delete: " + packageName);  
			        }else if(action.equals(Intent.ACTION_PACKAGE_REPLACED)){
						app = new App(pm, packageName);
//            			Utils.cancelNotify(context, app); 
			        	helper.updateOrAddApp(app);
//			        	Utils.clearCache(context, LocalAppsFragment.cacheId());
				       	Log.v("", Const.APP_NAME + " AppRefresher update: " + packageName);  
			        }
			        if(app != null){
						Intent i = new Intent(Const.BROADCAST_REFRESH_APP);
						i.putExtra(Const.KEY_PACKAGE, app.getPackage());
						context.sendBroadcast(i);
			        }
		//				// refresh app list
		//				Intent i = new Intent(Const.BROADCAST_CACHE_APPS_PROGRESS);
		//				i.putExtra(Const.KEY_FINISHED, true);
		//				context.sendBroadcast(i);
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
			}
       	}).start();
        
    }

}