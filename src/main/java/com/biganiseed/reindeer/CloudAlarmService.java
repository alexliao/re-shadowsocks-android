package com.biganiseed.reindeer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

public abstract class CloudAlarmService extends IntentService {
	public static double DELAY_FACTOR = 1.618;
	//private String mSinceTime = "";
	//private int mLastCheckCount = 0;
	//private int mNextInterval = 1;
	//private double mLastFetchTime = 0;

	public CloudAlarmService() {
		super(Const.APP_NAME + " CloudAlarmService");
	}

	public CloudAlarmService(Context context) {
		super(Const.APP_NAME + " CloudAlarmService");
	}

	@Override
	public void onCreate(){
		super.onCreate();
        //SimpleDateFormat datetimeFormat = new SimpleDateFormat(Const.UPLOAD_DATE_TIME_FORMAT);
		//mSinceTime = datetimeFormat.format(new Date()).replaceAll(" ", "%20");
		//mFeedHelper.open();
		Log.d(Const.APP_NAME, Const.APP_NAME + " CloudAlarmService - create service");
	}

	@Override
	public void onDestroy(){
		Log.d(Const.APP_NAME, Const.APP_NAME + " CloudAlarmService - destroy service");
		//mFeedHelper.close();
    	super.onDestroy();
	}

//	@Override
//	public void onStart(Intent intent, int startId){
//		super.onStart(intent, startId);
//	}
	
	abstract public void run();
	
	protected void increaseInterval() {
		int nextInterval = getNextInterval();
		nextInterval *= DELAY_FACTOR;
    	if(nextInterval > getMaxInterval()) nextInterval = getMaxInterval();
    	if(nextInterval < getMinInterval()) nextInterval = getMinInterval();
    	setNextInterval(this, nextInterval);
	}

    public void resetInterval(Context context) {
    	setNextInterval(context, 1);
	}

    public void setNextRun(Class<?> cls){
    	Log.d(Const.APP_NAME, Const.APP_NAME + " "+cls.getName()+" - setNextRun after: " + getNextInterval());
    	AlarmManager alarms = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    	//PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, new Intent(CheckStarter.ACTION_CHECK), 0);
    	PendingIntent alarmIntent = PendingIntent.getService(this, 0, new Intent(this, cls), 0);
    	long timeToRun = SystemClock.elapsedRealtime() + getNextInterval();
    	alarms.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timeToRun, alarmIntent);
    }
    
    static public void cancelNextRun(Context context, Class<?> cls){
    	AlarmManager alarms = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	PendingIntent alarmIntent = PendingIntent.getService(context, 0, new Intent(context, cls), 0);
    	alarms.cancel(alarmIntent);
    	Log.d(Const.APP_NAME, Const.APP_NAME + " "+cls.getName()+" - Next run canceled");
    }
    
	public int getNextInterval(){
		String str = Tools.getPrefString(this, this.getClass().getName()+"nextInterval", "1");
		int ret = Integer.parseInt(str);
		return ret;
	}
	public void setNextInterval(Context context, int value){
		setNextInterval(context, this.getClass().getName(), value);
	}
	
	static public void setNextInterval(Context context, String className, int value){
		Tools.setPrefString(context, className+"nextInterval", Integer.toString(value));
	}
	
	abstract protected int getMinInterval();
	abstract protected int getMaxInterval();
}
