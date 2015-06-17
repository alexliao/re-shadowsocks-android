package com.biganiseed.reindeer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.shadowsocks.utils.*;
import com.github.shadowsocks.ShadowsocksVpnService;

import com.github.shadowsocks.R;
import com.github.shadowsocks.BuildConfig;

public class ReindeerVpnService extends ShadowsocksVpnService {
	Timer simuseControlTimer;
	Timer expirationTimer = new Timer();
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Const.APP_NAME, "ReindeerVpnService onStartCommand");
		Tools.addLog(this, "ReindeerVpnService onStartCommand");
//		startService(new Intent(this, Checker.class));
		
//		// set expiration
//		long expiresAfter = intent.getLongExtra("expires_after", 0);
////		long expiresAfter = 15;
//		
//		expirationTimer = new Timer();
//		expirationTimer.schedule(new TimerTask(){
//    		@Override
//    		public void run(){
//    			sendBroadcast(new Intent(Action.CLOSE()));
//    			Log.d(Const.APP_NAME, "ReindeerVpnService expirationTimer do");
//    		}
//    	}, expiresAfter*1000);
		
//	   	AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
//    	PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(Action.CLOSE()), 0);
//    	long timeToRun = SystemClock.elapsedRealtime() + expiresAfter*1000;
//    	alarms.set(AlarmManager.ELAPSED_REALTIME, timeToRun, alarmIntent);

		checkExpiration();
		
		if(intent != null){
			int simuseControlInterval = intent.getIntExtra("simuse_control_interval", 0); 
			if(simuseControlInterval > 0){
				final String simuseControlServer = intent.getStringExtra("simuse_control_server");
				simuseControlTimer = new Timer();
				simuseControlTimer.scheduleAtFixedRate(new TimerTask(){
					@Override
					public void run() {
	//					Intent intent = new Intent(Const.BROADCAST_CACHE_APPS_PROGRESS);
	//					intent.putExtra(Const.KEY_LOADING, true);
	//					context.sendBroadcast(intent);
						if(BuildConfig.DEBUG) Log.v(Const.APP_NAME, Const.APP_NAME + " ReindeerVpnService simuse control updating, server: " + simuseControlServer);
						String err = Api.simuseControl(getApplicationContext(), simuseControlServer);
						if(!err.isEmpty()){
							sendBroadcast(new Intent(Action.CLOSE()));
							notifyDisconnect(getApplicationContext(), err);
						}
					}
		    	}, 10*1000, simuseControlInterval*1000);
			}
		}
		return super.onStartCommand(intent, flags, startId);
//		super.onStartCommand(intent, flags, startId);
//		return Service.START_REDELIVER_INTENT;
	}
	
	// make sure expiration works
	void checkExpiration(){
		final JSONObject user = Tools.getCurrentUser(getApplicationContext());
		expirationTimer.scheduleAtFixedRate(new TimerTask(){
			public void run() {
				if(user != null){
					if(user.optBoolean("disconnect_when_expire", true) && !Tools.isVip(user)){
						Date expireTime =  new Date(user.optLong("expiration")*1000);
						if((new Date()).getTime() > expireTime.getTime()){
							getApplicationContext().sendBroadcast(new Intent(Action.CLOSE()));
			    			Log.d(Const.APP_NAME, "ReindeerVpnService expirationTimer do CLOSE");
						}
					}
				}
			}
		}, 5000, 10000);
		
	}
	
    static private void notifyDisconnect(Context context, String msg) {
    	String text = msg;
		String expandedText = msg;
		String expandedTitle = context.getString(R.string.disconnected);
		Intent i;
		i = new Intent(context, MainActivity.class);

    	NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = Tools.getDefaultNotification(text);
		PendingIntent launchIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
		notification.setLatestEventInfo(context, expandedTitle, expandedText, launchIntent);
		nm.notify(123456, notification);
	}
	

	@Override
	public void onDestroy() {
		Log.d(Const.APP_NAME, "ReindeerVpnService onDestroy");
		Tools.addLog(this, "ReindeerVpnService onDestroy");
		if(simuseControlTimer != null){
			simuseControlTimer.cancel();
			if(BuildConfig.DEBUG) Log.v(Const.APP_NAME, Const.APP_NAME + " ReindeerVpnService simuse control stopped.");
		}
		if(expirationTimer != null){
			expirationTimer.cancel();
			if(BuildConfig.DEBUG) Log.v(Const.APP_NAME, Const.APP_NAME + " ReindeerVpnService expiration control stopped.");
		}
		super.onDestroy();
	}
	
	public static boolean isServiceStarted(Context context) {
		return com.github.shadowsocks.utils.Utils.isServiceStarted("com.biganiseed.reindeer.ReindeerVpnService", context);
	}

}
