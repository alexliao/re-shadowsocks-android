package com.biganiseed.reindeer;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.shadowsocks.ReindeerUtils;
import com.github.shadowsocks.ShadowsocksVpnService;
import com.github.shadowsocks.utils.*;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.VpnService;
import android.os.SystemClock;
import android.util.Log;

public class ShadowsocksConnector extends VpnConnector {
	public static final String ACTION_VPN_CONNECTIVITY = "com.biganiseed.reindeer.vpn.connectivity";
	public static final String CONNECTION_STATE = "com.biganiseed.reindeer.connection_state";
	JSONObject profile;
	
	public ShadowsocksConnector(ReindeerActivity aContext, Runnable anOnExpiredRunable,
			Runnable anOnOutOfUseRunnale) {
		super(aContext, anOnExpiredRunable, anOnOutOfUseRunnale);
	}

	@Override
	public void init() {
//		ShadowsocksVpnService.setBase(Const.EXEC_PATH);
		
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnect() {
		sendBroadcastConnecting();
		activity.sendBroadcast(new Intent(Action.CLOSE()));
	}

	@Override
	public void boradcastStatus() {
		if(ReindeerVpnService.isServiceStarted(activity))
			sendBroadcast(com.github.shadowsocks.utils.State.CONNECTED());
		else
			sendBroadcast(com.github.shadowsocks.utils.State.STOPPED());
	}

	@Override
	public void onStart() {
		if(!ReindeerVpnService.isServiceStarted(activity)){
//			Utils.reset(getApplicationContext());
			final String exec_path = ReindeerUtils.getExecPath(activity.getApplication());
			new Thread() {
				public void run(){
					ReindeerUtils.crash_recovery(exec_path);
				}
			}.start();

			if(!Tools.getPrefBoolean(activity, "shadowsocks_assets_copied", false)){
				activity.ayncRun(new Runnable(){
					@Override public void run() {
				        ReindeerUtils.copyAssets(activity, exec_path, ReindeerUtils.getABI());
				        ReindeerUtils.chmodAssets(exec_path);
				        Tools.setPrefBoolean(activity, "shadowsocks_assets_copied", true);
					}
				}, null);		
			}
		}
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onActivityResult(Intent data) {
//		if (openvpnProfile == null) {
//			Log.w(MainActivity.TAG, "profile is null");
//			return;
//		}
//		if (mIVpnService == null){
//			Toast.makeText(activity, "Havn't bound to vpn service",	Toast.LENGTH_LONG).show();
//			return;
//		}
//		try {
////			mIVpnService.connect(mConnectingProfile, mConnectingUsername, mConnectingPassword);
//			mIVpnService.connect(openvpnProfile, null, null);
//		} catch (RemoteException e) {
//			Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//		}
		doConnect();
	}
	
	void doConnect(){
		JSONObject server = profile.optJSONObject("server");

		Tools.addLog(activity, "ShadowsocksConnector.doConnect with " + server.optString("ip"));
		
		sendBroadcastConnecting();

		Intent intent = new Intent(activity, ReindeerVpnService.class);
		
	    intent.putExtra(Key.isGlobalProxy(), true);
	    intent.putExtra(Key.isGFWList(), server.optBoolean("smart_route", Tools.getPrefBoolean(activity, Key.isGFWList(), Const.DEFAULT_SMART_ROUTE)));
	    intent.putExtra(Key.isBypassApps(), false);
	    intent.putExtra(Key.isTrafficStat(), true);
	    intent.putExtra(Key.proxied(), "");
	    intent.putExtra(Key.localPort(), 1080);
	    intent.putExtra("isVip", profile.optBoolean("isVip", false));

//	    intent.putExtra(Key.proxy(), "192.241.236.202");
//	    intent.putExtra(Key.sitekey(), "123456");
//	    intent.putExtra(Key.encMethod(), "rc4");
//	    intent.putExtra(Key.remotePort(), 10000);
	    intent.putExtra(Key.proxy(), server.optString("ip"));
	    intent.putExtra(Key.sitekey(), profile.optString("password"));
	    intent.putExtra(Key.encMethod(), profile.optString("encrypt_method"));
	    intent.putExtra(Key.remotePort(), profile.optInt("port"));
	    intent.putExtra(Key.profileName(), server.optString("city_name"));
	    intent.putExtra("simuse_control_interval", profile.optInt("simuse_control_interval", 0));
	    intent.putExtra("simuse_control_server", profile.optString("simuse_control_server", Const.getRootIp(activity)));
	    intent.putExtra("expires_after", profile.optInt("expires_after", 0));

	    
	    activity.startService(intent);
	    
		Tools.setCurrentNas(activity, server);
	}

	@Override
	public void registerReceiver(final OnStateChanged callback) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Action.UPDATE_STATE());
        stateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();

                if (Action.UPDATE_STATE().equals(action)) {
//                    Log.d(Const.APP_NAME, "Reindeer receiver UPDATE_STATE intent:" + intent); //$NON-NLS-1$
            		final int s = intent.getIntExtra(Extra.STATE(), com.github.shadowsocks.utils.State.INIT());
        	        activity.runOnUiThread(new Runnable() {
        	            @Override
        	            public void run() {
        	            	callback.run(translateState(s));
        	            }
        	        });
                } else {
                    Log.d(Const.APP_NAME, "Reindeer receiver ignores intent:" + intent); //$NON-NLS-1$
                }
            }
        };
        activity.registerReceiver(stateBroadcastReceiver, filter);
	}

	@Override
	void connectProfile() {
		Intent intent = VpnService.prepare(activity.getApplicationContext());
		if (intent != null) {
			activity.startActivityForResult(intent, REQUEST_CONNECT);
		}else{
			doConnect();
		}		
	}

	@Override
	void sendBroadcastConnecting() {
		sendBroadcast(com.github.shadowsocks.utils.State.CONNECTING());
	}

	@Override
	void sendBroadcastUnusable() {
		sendBroadcast(com.github.shadowsocks.utils.State.INIT());
	}

	@Override
	void initProfile() {
		// TODO Auto-generated method stub

	}

	@Override
	void setProfileFromConfiguration(JSONObject data) throws JSONException {
		profile = data.getJSONObject("tunnel");
//		profile.put("expires_after", data.optJSONObject("user").optLong("expires_after"));
		profile.put("isVip", Tools.isVip(data.optJSONObject("user")));
		
		if(data.optJSONObject("user").optBoolean("disconnect_when_expire", true)){
	//		long expiration = Tools.getCurrentUser(activity).getLong("expiration")*1000; // this is adjusted time
	//		long now = new Date().getTime();
	//		long distance = expiration - now;
			long distance = data.optJSONObject("user").optLong("expires_after")*1000;
			if(/*distance < 86400*1000 &&*/ distance > 0){
	//			Timer expirationTimer = new Timer();
	//			expirationTimer.schedule(new TimerTask(){
	//	    		@Override
	//	    		public void run(){
	//	    			disconnect();
	//					Api.stopAccounting(activity, "Session-Timeout");
	//	    		}
	//	    	}, expiration - now);
			   	AlarmManager alarms = (AlarmManager)activity.getSystemService(Context.ALARM_SERVICE);
		    	PendingIntent alarmIntent = PendingIntent.getBroadcast(activity, 0, new Intent(Action.CLOSE()), 0);
		    	long timeToRun = SystemClock.elapsedRealtime() + distance;
		    	alarms.set(AlarmManager.ELAPSED_REALTIME, timeToRun, alarmIntent);
			}
		}		
	}

	@Override
	String getControllerName() {
		// TODO Auto-generated method stub
		return "ssc";
	}


    private State translateState(final int state) {
    	State result;
    	
  		if(com.github.shadowsocks.utils.State.CONNECTING() == state)
  			result = State.LOADING; 
  		else if(com.github.shadowsocks.utils.State.CONNECTED() == state)
  			result = State.ON; 
  		else
  			result = State.OFF; 
    	return result;
    }

    private Intent getBroadcast(int state){
        Intent intent = new Intent(Action.UPDATE_STATE());
		intent.putExtra(Extra.STATE(), state);
		return intent;
    }
    
	private void sendBroadcast(int state) {
        activity.sendBroadcast(getBroadcast(state));
    }

}
