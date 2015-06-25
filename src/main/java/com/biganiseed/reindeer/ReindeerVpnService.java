package com.biganiseed.reindeer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.github.shadowsocks.ShadowsocksVpnService;
import com.github.shadowsocks.utils.Action;

import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ReindeerVpnService extends ShadowsocksVpnService {
	Timer simuseControlTimer;
	Timer expirationTimer = new Timer();
	protected BroadcastReceiver closeBroadcastReceiver;

	@Override
	public void onCreate (){
		super.onCreate();
		registerCloseReceiver();
	}

	void registerCloseReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Action.CLOSE());
		closeBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, final Intent intent) {
				String action = intent.getAction();
				if (Action.CLOSE().equals(action)) {
					stopRunner();
					stopTimer();
				}

			}
		};
		registerReceiver(closeBroadcastReceiver, filter);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Const.APP_NAME, "ReindeerVpnService onStartCommand");
		Tools.addLog(this, "ReindeerVpnService onStartCommand");

		checkExpiration();
		startSimuseControlTimer(intent);

		return super.onStartCommand(intent, flags, startId);
//		super.onStartCommand(intent, flags, startId);
//		return Service.START_REDELIVER_INTENT;
	}

	void startSimuseControlTimer(Intent intent){
		if(intent != null){
			int simuseControlInterval = intent.getIntExtra("simuse_control_interval", 0);
			if(simuseControlInterval > 0){
				final String simuseControlServer = intent.getStringExtra("simuse_control_server");
				if(simuseControlTimer != null) simuseControlTimer.cancel();
				simuseControlTimer = new Timer();
				simuseControlTimer.scheduleAtFixedRate(new TimerTask(){
					@Override
					public void run() {
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
	}

	// make sure expiration works
	void checkExpiration(){
		if(expirationTimer != null) expirationTimer.cancel();
		expirationTimer = new Timer();
		expirationTimer.scheduleAtFixedRate(new TimerTask(){
			public void run() {
				JSONObject user = Tools.getCurrentUser(getApplicationContext());
				boolean needClose = true;
				if(user != null){
					if(Tools.isVip(user)) needClose = false;
					else if (user.optBoolean("disconnect_when_expire", true)) {
						Date expireTime = new Date(user.optLong("expiration") * 1000);
						if ((new Date()).getTime() < expireTime.getTime()) {
							needClose = false;
						}
					}
				}
				if(needClose){
					getApplicationContext().sendBroadcast(new Intent(Action.CLOSE()));
					Log.d(Const.APP_NAME, "ReindeerVpnService expirationTimer do CLOSE");
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
		stopTimer();
		if (closeBroadcastReceiver != null) {
			unregisterReceiver(closeBroadcastReceiver);
		}
		super.onDestroy();
	}

	void stopTimer(){
		if(simuseControlTimer != null){
			simuseControlTimer.cancel();
			if(BuildConfig.DEBUG) Log.v(Const.APP_NAME, Const.APP_NAME + " ReindeerVpnService simuse control stopped.");
		}
		if(expirationTimer != null){
			expirationTimer.cancel();
			if(BuildConfig.DEBUG) Log.v(Const.APP_NAME, Const.APP_NAME + " ReindeerVpnService expiration control stopped.");
		}
	}

	public static boolean isServiceStarted(Context context) {
		return com.github.shadowsocks.utils.Utils.isServiceStarted("com.biganiseed.reindeer.ReindeerVpnService", context);
	}

}
