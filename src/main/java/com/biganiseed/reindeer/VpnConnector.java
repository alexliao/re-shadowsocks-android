package com.biganiseed.reindeer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.shadowsocks.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

// manager connecting life cycle and Android version difference
public abstract class VpnConnector {
	
    protected static final String PROFILE_ID = "com.biganiseed.ladder";
    protected static final String PROFILE_NAME = "Ladder";
    public static final int REQUEST_CONNECT = 2;
   
    protected ReindeerActivity activity;
    Runnable onExpiredRunable;
    Runnable onOutOfUseRunable;
    protected BroadcastReceiver stateBroadcastReceiver;

	abstract public void init();
	abstract public void uninit();
	abstract public void disconnect();
	abstract public void boradcastStatus();
	abstract public void onStart();
	abstract public void onStop();
	abstract public void onActivityResult(Intent data);
	abstract public void registerReceiver(OnStateChanged callback);
	
	abstract void connectProfile();
	abstract void sendBroadcastConnecting();
	abstract void sendBroadcastUnusable();
	abstract void initProfile();
	abstract void setProfileFromConfiguration(JSONObject data) throws JSONException;
	abstract String getControllerName();
	
	VpnConnector(ReindeerActivity aContext, Runnable anOnExpiredRunable, Runnable anOnOutOfUseRunnale){
		activity = aContext;
		onExpiredRunable = anOnExpiredRunable;
		onOutOfUseRunable = anOnOutOfUseRunnale;
	}

	public void connect(){
		final Handler handler = new Handler();
		new Thread() {
			public void run(){
				// get configuration
				sendBroadcastConnecting();
				JSONObject configuration;
				try {
					configuration = Api.getConfiguration(activity, getControllerName(), null);
					setProfileFromConfiguration(configuration);
					connectProfile();
					final String message = configuration.optString("message", null);
					if(message != null){
						handler.post(new Runnable(){
							@Override
							public void run() {
								Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
							}
						});
					}
				} catch (final ExpiredException e) {
					sendBroadcastUnusable();
					if(onExpiredRunable != null) handler.post(onExpiredRunable);
				} catch (final OutOfUseException e) {
					sendBroadcastUnusable();
					if(onOutOfUseRunable != null) handler.post(onOutOfUseRunable);
				} catch (final Exception e) {
					Log.d(Const.APP_NAME, "VpnConnector.connect err: "+e.getMessage());
					Tools.addLog(activity, "VpnConnector.connect err: "+e.getMessage());
					if(Api.checkDns(activity)){
						handler.post(new Runnable(){
							@Override
							public void run() {
								connect();
							}
						});
					}else{
						sendBroadcastUnusable();
						handler.post(new Runnable(){
							@Override
							public void run() {
								Toast.makeText(activity, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
							}
						});
					}
			}
			}
		}.start();
	}
	
//	private void checkDns(final Context context){
//    	long lastCheckTime = Long.parseLong(Tools.getPrefString(context, "refresh_root_ip_time", "0"));
//    	if(System.currentTimeMillis() - lastCheckTime > 60*1000){
//    		new Thread(new Runnable(){
//				@Override
//				public void run() {
//		    		String ip = Api.getRootIp(context);
//		    		if(ip != null){
//		    			Tools.setPrefString(context, "root_ip", ip);
//		    			Tools.setPrefString(context, "refresh_root_ip_time", ""+System.currentTimeMillis());
//		    		}
//				}
//    		}).start();
//    	}
//	}
	

	//	private JSONObject getConfiguration() throws Exception {
//		JSONObject ret = null;
//		try {
//			Log.d(Const.APP_NAME, Const.APP_NAME + " getting configuration...");
//			String strResult = null;
//			String url = Const.ROOT_HTTP + "/" + getControllerName() + "/" + Tools.getDeviceUUID(activity) + ".json?" + Tools.getClientParameters(activity);
//			HttpGet httpReq = new HttpGet(url);
//			HttpParams httpParameters = new BasicHttpParams();
//			HttpConnectionParams.setConnectionTimeout(httpParameters, Const.HTTP_TIMEOUT);
//			httpReq.setParams(httpParameters);
//			HttpResponse httpResp;
//			httpResp = new DefaultHttpClient().execute(httpReq);
//			strResult = EntityUtils.toString(httpResp.getEntity());
//			if(httpResp.getStatusLine().getStatusCode() == 200){
//				JSONObject json = new JSONObject(strResult);
//				ret = json;
//				saveCommonConfiguration(json);
//			}else if(httpResp.getStatusLine().getStatusCode() == 402){
//				JSONObject json = new JSONObject(strResult);
//				saveCommonConfiguration(json);
//				throw new Exception(activity.getString(R.string.expiration_prompt));
//			}else{
////				throw new Exception(json.optString("error_message","error"));
//				throw new Exception(strResult);
//			}
//			Log.d(Const.APP_NAME, Const.APP_NAME + " got configuration.");
//			return ret;
//		} catch (Exception e) {
//			throw new Exception(e.getMessage());
//		}
//	}
	 
//	protected String getAssetFilePath(String fileName){
//		File file = new File(activity.getCacheDir(), fileName);
//		String path = file.getAbsolutePath();
//		if(!file.exists()){
//			InputStream in;
//			try {
//				in = activity.getAssets().open(fileName);
//				FileOutputStream out = new FileOutputStream(path);
//				IOUtils.copy(in, out);
//				out.close();
//				in.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//			
//		return path;
//		
//	}

//	private String AssetToString(String fileName){
//		File file = new File(activity.getCacheDir(), fileName);
//		String path = file.getAbsolutePath();
//		String ret = null;
//		if(!file.exists()){
//			InputStream in;
//			try {
//				in = activity.getAssets().open(fileName);
//				byte[] buffer = new byte[1024]; // ! make sure this size is enough
//				in.read(buffer);
//				ret = IOUtils.toString(buffer, "ASCII");
//				in.close();
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//		}
//		return ret;
//	}

	public void unregisterReceiver() {
        if (stateBroadcastReceiver != null) {
            activity.unregisterReceiver(stateBroadcastReceiver);
        }
    }

	
	public enum State {
	    ON, OFF, LOADING;
	}

//	static void reconnect(){
//		VpnConnector vpnConnector;
//		if(Tools.is4x())
//			vpnConnector = new OpenvpnConnector(PlansActivity.this);
//		else
//			vpnConnector = new PptpConnector(PlansActivity.this);
//		vpnConnector.init();
//		vpnConnector.connect();
//	}

}

