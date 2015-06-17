package com.biganiseed.reindeer.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuItem;
import com.biganiseed.reindeer.Api;
import com.github.shadowsocks.BuildConfig;
import com.biganiseed.reindeer.DownloaderEx;
import com.biganiseed.reindeer.ReindeerBaseAdapter;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.NasAdapter;
import com.github.shadowsocks.R;
import com.biganiseed.reindeer.ReindeerVpnService;
import com.biganiseed.reindeer.Tools;
import com.github.shadowsocks.utils.*;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

//public class Update extends WithHeaderActivity {
public class NasFragment extends ReindeerListFragment {
	public static final int TEST_BYTES = 10000000;
	public static final int TEST_SECONDES = 6;
	public static final String TEST_FILE = "test.data";
//	protected ListView list;
//	protected Button btnUpgrade;
//	protected Button btnDone;
//	protected TextView txtVersionName;
//	protected TextView txtUp2Date;
//	protected TextView txtNotUp2Date;
	protected View btnStart, btnStop;
//	protected int mCurrentVersion = 0;
//	protected int mNewVersion;
//	private ProgressBar progressBar;
//	private TextView txtSizeSent;

	
	DownloaderExServiceConnection mConnection;
	protected ProgressBroadcastReceiver mProgressReceiver = new ProgressBroadcastReceiver();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		a().registerReceiver(mProgressReceiver, new IntentFilter(Const.BROADCAST_DOWNLOAD_PROGRESS));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	View v = super.onCreateView(inflater, container, savedInstanceState);
//        bind(v);
        return v;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
        HeaderFragment.setTitle(a(), getString(R.string.menu_nas));
        Intent intent = new Intent(a(), DownloaderEx.class);
        mConnection = new DownloaderExServiceConnection();
        a().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override 
    public void onStop(){
    	super.onStop();
    	if(mConnection.getService() != null) a().unbindService(mConnection);
		Intent intent = new Intent(a(), DownloaderEx.class);
		a().stopService(intent);
    }

    @Override
    public void onDestroy(){
		a().unregisterReceiver(mProgressReceiver);
		super.onDestroy();
    }

    protected void bind(View v){
    	if(v == null) return;
    	
//        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
//        txtSizeSent = (TextView) v.findViewById(R.id.txtSizeSent);

        btnStart = v.findViewById(R.id.btnStart); 
    	btnStop = v.findViewById(R.id.btnStop); 
    	
    	btnStart.setEnabled(true);
    	btnStart.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!Tools.isWifi(a())){
			    	Tools.confirm(a(), null, getString(R.string.test_confirm)).setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startTest();
						}
					}).show();
				}else
					startTest();
					
			}
		});
    	
    	btnStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				btnStart.setVisibility(View.VISIBLE);
				btnStop.setVisibility(View.GONE);

				for(int i=0; i<mListData.length(); i++){
					JSONObject nas = mListData.optJSONObject(i);
//					try {
//						nas.put("testing", false);
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
					String ip = nas.optString("ip");
					if(mConnection.getService() != null){
			        	mConnection.getService().cancel(ip);
	//		        	Utils.cancelNotify(DownloadingApp.this, header.getApp());
			        }
				}
			
			}
		});
    }

    void startTest(){
		btnStart.setVisibility(View.GONE);
		btnStop.setVisibility(View.VISIBLE);
		
		if(ReindeerVpnService.isServiceStarted(a())){
			a().sendBroadcast(new Intent(Action.CLOSE()));
//			try {
//				Thread.sleep(500);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

		int prevTestingRoute = Integer.parseInt(Tools.getPrefString(a(), "testingRouteIndex", "-1"));
		for(int i=0; i<mListData.length(); i++){
			int index = (i+prevTestingRoute+1) % mListData.length();
			JSONObject nas = mListData.optJSONObject(index);
			try {
				nas.put("testing", true);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String ip = nas.optString("ip");
			String url = "http://" + ip + "/" + TEST_FILE + "?" + Math.random();
			Intent intent = new Intent(a(), DownloaderEx.class);
			intent.putExtra(DownloaderEx.KEY_ID, ip);
			intent.putExtra(DownloaderEx.KEY_URL, url);
			intent.putExtra(DownloaderEx.KEY_MAX_BYTES, TEST_BYTES);
			intent.putExtra(DownloaderEx.KEY_MAX_SECONDES, TEST_SECONDES);
			a().startService(intent);
		}
		mAdapter.setData(mListData);
		mAdapter.notifyDataSetChanged();

		Tools.setPrefString(a(), "speedtested_at", ""+System.currentTimeMillis());

    }
    
    
	@Override
	protected String getUrl() {
		String url = Const.getRootHttp(a()) + "/routes.json?" + Tools.getClientParameters(a());
		if(BuildConfig.DEBUG) url += "&debug=true";
		return url;
	}

	@Override
	protected ReindeerBaseAdapter getAdapter() {
		return new NasAdapter(ga(), mListData, mLoadingImages);
	}

	@Override
	protected void onClickItem(int position) throws JSONException {
		JSONObject item = mListData.getJSONObject(position);
//		Tools.setCurrentNas(a(), item.optString("ip"));
		if(nasIsAvailable(item)){
			Tools.setCurrentNas(a(), item);
			mAdapter.notifyDataSetChanged();
			if(ReindeerVpnService.isServiceStarted(a())){
				TimerTask task = new TimerTask(){
				    public void run(){
						a().sendBroadcast(new Intent(Action.CLOSE()));
				    }
				};
				(new Timer()).schedule(task, 500);
			}
			navigate(new SwitcherFragment(), "switcher");
		}else{
			Tools.confirm(getActivity(), null, getString(R.string.route_not_available)).setPositiveButton(R.string.buy, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					navigate(new PlansFragment(), "plans");
				}
			}).show();
		}
	}

	@Override
	protected void setContent() {
	   setContentView(R.layout.re_nas_fragment);
	}
	
	@Override
	protected void loadedMore(boolean successed) {
		bind(getView());
	}
    

    protected class ProgressBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
    		Log.d(Const.APP_NAME, Const.APP_NAME + " ProgressBroadcastReceiver onReceive: " + intent.toString());
            if(intent.getAction().equals(Const.BROADCAST_DOWNLOAD_PROGRESS)){
            	String id = intent.getStringExtra(DownloaderEx.KEY_ID);
            	if(id == null){
    				boolean finished = intent.getBooleanExtra(DownloaderEx.KEY_FINISHED, false);
    				if(finished){
    					if(btnStop != null) btnStop.setVisibility(View.GONE);
    					if(btnStart != null) btnStart.setVisibility(View.VISIBLE);
    				}
            	}else{
            		setProgressData(id, intent);
            		if(intent.getBooleanExtra(DownloaderEx.KEY_STARTED, false)) Tools.setPrefString(a(), "testingRouteIndex", ""+getRouteIndex(id));
            		if(intent.getBooleanExtra(DownloaderEx.KEY_FINISHED, false)) reportSpeed(id, intent);
            	}
            	mAdapter.setData(mListData);
            	mAdapter.notifyDataSetChanged();
            	
//            		int percent = intent.getIntExtra(DownloaderEx.KEY_PERCENT, 0);
//            		long sizeReceived = intent.getLongExtra(DownloaderEx.KEY_SIZE_TRANSFERRED, 0);
//            		if(percent > 0 ){
//                		progressBar.setIndeterminate(false);
//                		progressBar.setProgress(percent);
//                		long speed = intent.getLongExtra(DownloaderEx.KEY_SPEED, 0);
//                		long remainTime = intent.getLongExtra(DownloaderEx.KEY_REMAIN_TIME, 0);
////	            		txtSizeSent.setText(String.format(getString(R.string.size_received), percent, sizeReceived/1024));
//	            		txtSizeSent.setText(String.format(getString(R.string.transfer_progress), speed/1024, Tools.getFriendlyTime(context, remainTime)));
//            		}
//            		String errMsg = intent.getStringExtra(DownloaderEx.KEY_FAILED);
//            		if(errMsg != null){
//            			if(!errMsg.equals("")) Toast.makeText(a(), errMsg, Toast.LENGTH_LONG);
//            		}else{
//	            		boolean finished = intent.getBooleanExtra(DownloaderEx.KEY_FINISHED, false);
//	            		if(finished){
//	                		long speed = intent.getLongExtra(DownloaderEx.KEY_SPEED, 0);
//		            		txtSizeSent.setText(String.format(getString(R.string.transfer_speed), speed/1024));
//	            			btnStop.setVisibility(View.GONE);
//	            			btnStart.setVisibility(View.VISIBLE);
//	            		}
//            		}
            	
            }
        }

		private void setProgressData(String id, Intent intent) {
			JSONObject json = getNasByIp(id);
			if(json == null) return;
			
			try {
//				json.put(DownloaderEx.KEY_PERCENT, intent.getIntExtra(DownloaderEx.KEY_PERCENT, 0));
				json.put(DownloaderEx.KEY_PERCENT, 100*intent.getLongExtra(DownloaderEx.KEY_ELAPSED, 0)/1000/TEST_SECONDES);
				json.put(DownloaderEx.KEY_SPEED, intent.getLongExtra(DownloaderEx.KEY_SPEED, 0));
//				json.put(DownloaderEx.KEY_REMAIN_TIME, Math.max(0, (TEST_SECONDES*1000-intent.getLongExtra(DownloaderEx.KEY_ELAPSED, 0))/1000));
				if(intent.getStringExtra(DownloaderEx.KEY_FAILED) != null){
					json.put(DownloaderEx.KEY_FAILED, getString(R.string.err_download_failed));
//					json.put(DownloaderEx.KEY_PERCENT, 100);
					json.put(DownloaderEx.KEY_SPEED, 0);
					json.put(DownloaderEx.KEY_FINISHED, true);
				}
				if(intent.getBooleanExtra(DownloaderEx.KEY_STARTED, false)){
					json.put(DownloaderEx.KEY_STARTED, true);
					json.put(DownloaderEx.KEY_FINISHED, false);
				}
				if(intent.getBooleanExtra(DownloaderEx.KEY_FINISHED, false)){
					json.put(DownloaderEx.KEY_FINISHED, true);
					JSONObject nas = fastestNas();
					if(nas != null && nasIsAvailable(nas) ) Tools.setCurrentNas(a(), nas);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    boolean nasIsAvailable(JSONObject nas){
    	if(Tools.getCurrentUser(a()) == null) return true;
    	return !Tools.getCurrentUser(a()).optBoolean("is_limited") || nas.optBoolean("free") ;
    }
    
	private void reportSpeed(String id, final Intent intent) {
		final JSONObject json = getNasByIp(id);
		if(json == null) return;
		
		Log.v(Const.APP_NAME, Const.APP_NAME + " reportSpeed: " + id);
		new Thread() {
			public void run(){
				TelephonyManager tm = (TelephonyManager)a().getSystemService(Context.TELEPHONY_SERVICE);  
				try {
					Api.reportSpeed(a(), 
						Tools.getDeviceUUID(a()),
						json.optString("region"), 
						json.optString("city"), 
						json.optString("hosting"), 
						json.optString("ip"), 
						Tools.getLocalIpAddress(), 
						tm.getNetworkCountryIso(),
						Tools.getNetworkName(a()), 
						intent.getLongExtra(DownloaderEx.KEY_SPEED, 0), 
						intent.getLongExtra(DownloaderEx.KEY_ELAPSED, 0)/1000);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
		
	}

//	private String fastestNasIp() {
//		int maxSpeed = 0;
//		String result = null;
//    	for(int i=0; i<mListData.length(); i++){
//    		JSONObject nas = mListData.optJSONObject(i);
//    		int speed = nas.optInt(DownloaderEx.KEY_SPEED);
//    		if( speed > maxSpeed){
//    			maxSpeed = speed;
//    			result = nas.optString("ip");
//    		}
//    	}
//    	return result;
//	}

	private JSONObject fastestNas() {
		int maxSpeed = 0;
		JSONObject result = null;
    	for(int i=0; i<mListData.length(); i++){
    		JSONObject nas = mListData.optJSONObject(i);
    		int speed = nas.optInt(DownloaderEx.KEY_SPEED);
    		if( speed > maxSpeed){
    			maxSpeed = speed;
    			result = nas;
    		}
    	}
    	return result;
	}

	JSONObject getNasByIp(String ip){
    	JSONObject result = null;
    	for(int i=0; i<mListData.length(); i++){
    		JSONObject nas = mListData.optJSONObject(i);
    		if(nas.optString("ip").equals(ip)){
    			result = nas;
    			break;
    		}
    	}
    	return result;
    }
    
	// return -1 if not found
	int getRouteIndex(String ip){
    	int result = -1;
    	for(int i=0; i<mListData.length(); i++){
    		JSONObject nas = mListData.optJSONObject(i);
    		if(nas.optString("ip").equals(ip)){
    			result = i;
    			break;
    		}
    	}
    	return result;
    }

	private class DownloaderExServiceConnection implements ServiceConnection{
    	private DownloaderEx mService = null;
    	
    	public DownloaderEx getService(){
    		return mService;
    	}
    	
        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            DownloaderEx.LocalBinder binder = (DownloaderEx.LocalBinder) service;
            mService = binder.getService();
//            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
//            mBound = false;
        }
    }

}
