package com.biganiseed.reindeer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import com.github.shadowsocks.ReindeerUtils;
import com.github.shadowsocks.aidl.Config;
import com.github.shadowsocks.aidl.IShadowsocksService;
import com.github.shadowsocks.aidl.IShadowsocksServiceCallback;
import com.github.shadowsocks.utils.Action;
import com.github.shadowsocks.utils.Key;
import com.github.shadowsocks.utils.Route;

import org.json.JSONException;
import org.json.JSONObject;

public class ShadowsocksConnector extends VpnConnector {
	public static final String ACTION_VPN_CONNECTIVITY = "com.biganiseed.reindeer.vpn.connectivity";
	public static final String UPDATE_STATE = "com.biganiseed.reindeer.UPDATE_STATE";
	public static final String STATE = "com.biganiseed.reindeer.STATE";
	JSONObject profile;

	IShadowsocksService bgService = null;
	IShadowsocksServiceCallback callback = new IShadowsocksServiceCallback.Stub() {
		@Override
		public void stateChanged(int i, String s) throws RemoteException {
			sendBroadcast(i);
		}
	};
	ServiceConnection connection = new ServiceConnection(){
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			bgService = IShadowsocksService.Stub.asInterface(service);
			try{
				bgService.registerCallback(callback);
			}catch (Exception e){
				e.printStackTrace();
			}
			// TODO
			// update the UI
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			//TODO
			// update the UI
			try{
				if (bgService != null) bgService.unregisterCallback(callback);
			}catch (Exception e){
				e.printStackTrace();
			}
			bgService = null;
		}
	};

	void attachService() {
		if (bgService == null) {
			Intent intent = new Intent(activity, ReindeerVpnService.class);
			intent.setAction(Action.SERVICE());
			activity.bindService(intent, connection, Context.BIND_AUTO_CREATE);
		}
	}

	void deattachService() {
		if (bgService != null) {
			try {
				bgService.unregisterCallback(callback);
			}catch (Exception e){
				e.printStackTrace();
			}
			bgService = null;
			activity.unbindService(connection);
		}
	}


	public ShadowsocksConnector(ReindeerActivity aContext, Runnable anOnExpiredRunable,
			Runnable anOnOutOfUseRunnale) {
		super(aContext, anOnExpiredRunable, anOnOutOfUseRunnale);
	}

	@Override
	public void init() {
//		ShadowVpnService.setBase(Const.EXEC_PATH);
		if(!Tools.getPrefBoolean(activity, "shadowsocks_assets_copied", false)){
			activity.ayncRun(new Runnable(){
				@Override public void run() {
					ReindeerUtils.reset(activity);
					Tools.setPrefBoolean(activity, "shadowsocks_assets_copied", true);
				}
			}, null);
		}

		attachService();
	}

	@Override
	public void uninit() {
		// TODO Auto-generated method stub
		deattachService();
	}

	@Override
	public void disconnect() {
		sendBroadcastConnecting();
		activity.sendBroadcast(new Intent(Action.CLOSE()));
//		if (bgService != null) try {
//			bgService.stop();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void broadcastStatus() {
//		if(ReindeerVpnService.isServiceStarted(activity))
		if(bgService == null) return;
		try {
			if(bgService.getState() == com.github.shadowsocks.utils.State.CONNECTED())
                sendBroadcast(com.github.shadowsocks.utils.State.CONNECTED());
            else
                sendBroadcast(com.github.shadowsocks.utils.State.STOPPED());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onStart() {
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onActivityResult(Intent data) {
		doConnect();
	}
	
	void doConnect(){
		JSONObject server = profile.optJSONObject("server");

		Tools.addLog(activity, "ShadowsocksConnector.doConnect with " + server.optString("ip"));
		
		sendBroadcastConnecting();

		Intent intent = new Intent(activity, ReindeerVpnService.class);
//
//	    intent.putExtra(Key.isGlobalProxy(), true);
//	    intent.putExtra(Key.isGFWList(), server.optBoolean("smart_route", Tools.getPrefBoolean(activity, Key.isGFWList(), Const.DEFAULT_SMART_ROUTE)));
//	    intent.putExtra(Key.isBypassApps(), false);
//	    intent.putExtra(Key.isTrafficStat(), true);
//	    intent.putExtra(Key.proxied(), "");
//	    intent.putExtra(Key.localPort(), 1080);
//	    intent.putExtra("isVip", profile.optBoolean("isVip", false));
//
//	    intent.putExtra(Key.proxy(), server.optString("ip"));
//	    intent.putExtra(Key.sitekey(), profile.optString("password"));
//	    intent.putExtra(Key.encMethod(), profile.optString("encrypt_method"));
//	    intent.putExtra(Key.remotePort(), profile.optInt("port"));
//	    intent.putExtra(Key.profileName(), server.optString("city_name"));
//	    intent.putExtra("simuse_control_interval", profile.optInt("simuse_control_interval", 0));
//	    intent.putExtra("simuse_control_server", profile.optString("simuse_control_server", Const.getRootIp(activity)));
//	    intent.putExtra("expires_after", profile.optInt("expires_after", 0));
//
//
//	    activity.startService(intent);


		Config config = new Config(
				true,
				false, // obsolete parameter, no effect
				false,
				true,
				false,
				server.optString("city_name"),
				server.optString("ip"),
				profile.optString("password"),
				profile.optString("encrypt_method"),
				"",
				server.optBoolean("smart_route", Tools.getPrefBoolean(activity, Key.isGFWList(), Const.DEFAULT_SMART_ROUTE)) ? Route.BYPASS_CHN() : Route.ALL(),
				profile.optInt("port"),
				1080
		);

		try {
			bgService.start(config);
			// start expiration and simuse control
		    intent.putExtra("simuse_control_interval", profile.optInt("simuse_control_interval", 0));
		    intent.putExtra("simuse_control_server", profile.optString("simuse_control_server", Const.getRootIp(activity)));
		    activity.startService(intent);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		Tools.setCurrentNas(activity, server);
	}

	@Override
	public void registerReceiver(final OnStateChanged callback) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(UPDATE_STATE);
        stateBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();

				if (UPDATE_STATE.equals(action)) {
//                    Log.d(Const.APP_NAME, "Reindeer receiver UPDATE_STATE intent:" + intent); //$NON-NLS-1$
            		final int s = intent.getIntExtra(STATE, com.github.shadowsocks.utils.State.INIT());
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
        Intent intent = new Intent(UPDATE_STATE);
		intent.putExtra(STATE, state);
		return intent;
    }

	private void sendBroadcast(int state) {
        activity.sendBroadcast(getBroadcast(state));
    }

}
