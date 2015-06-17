package com.biganiseed.reindeer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

public class DownloaderEx extends Service {
	public static final String KEY_ID = "id";
	public static final String KEY_PERCENT = "percent";
	public static final String KEY_STARTED = "started";
	public static final String KEY_FINISHED = "finished";
	public static final String KEY_FAILED = "failed";
	public static final String KEY_SIZE_TRANSFERRED = "size_sent";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_URL = "url";
	public static final String KEY_SPEED = "speed";
	public static final String KEY_ELAPSED = "elapsed";
	public static final String KEY_REMAIN_TIME = "remain_time";
	public static final String KEY_MAX_BYTES = "max_bytes";
	public static final String KEY_MAX_SECONDES = "max_seconds";

	
	public static final int MULITI_DOWNLOADING = 1;
	public static final int STEP = 1024; // bytes
	public static final int UPDATE_INTERVAL = 300; // mill-second
//	MyThreadPoolExecutor mPool;
	ExecutorService mPool;
	NotificationManager mNotificationManager;
	static public int mCount = 0;
//	HashMap<String, Boolean> mPackageCanceled = new HashMap<String, Boolean>();
	private HashMap<String, Future<?>> mTasks = new HashMap<String, Future<?>>(); 
	private final IBinder mBinder = new LocalBinder();
	boolean enableCheckTask = true;
	
// begin for binding ----------------------------------
	public class LocalBinder extends Binder {
    	public DownloaderEx getService() {
            return DownloaderEx.this;
        }
    }
    
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public void cancel(String id){
		Future<?> task = mTasks.get(id);
		if(task != null){
			boolean canceled = task.cancel(true);
			mTasks.remove(id);
//			if(!canceled) mPackageCanceled.put(id, true); // we need to finish the task in logic because the running task thread may not be canceled really.
		}
	}
// end for binding ----------------------------------

	@Override
    public void onCreate() {
		Log.d(Const.APP_NAME, Const.APP_NAME + " DownloaderEx - onCreate");
		mPool = Executors.newFixedThreadPool(MULITI_DOWNLOADING);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		checkTask();
		
   }
	
	// loop check if all tasks are done then stop service
	void checkTask(){
		new Thread() {
			public void run(){
				while(enableCheckTask){
					Log.d(Const.APP_NAME, Const.APP_NAME + " DownloaderEx - checking task");
					boolean allDone = true;
					for(Future<?> task : mTasks.values()){
						if(!task.isDone()){ 
							allDone = false;
							break;
						}
					}
					if(allDone){
//						enableCheckTask = false;
						Intent intent = new Intent(Const.BROADCAST_DOWNLOAD_PROGRESS);
						intent.putExtra(KEY_FINISHED, true);
						sendBroadcast(intent);
						stopSelf();
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	int ret = super.onStartCommand(intent, flags, startId); 
		final String id = intent.getStringExtra(KEY_ID);
		final String url = intent.getStringExtra(KEY_URL);
		final int maxBytes = intent.getIntExtra(KEY_MAX_BYTES, Integer.MAX_VALUE);
		final int maxSeconds = intent.getIntExtra(KEY_MAX_SECONDES, Integer.MAX_VALUE);
		Log.d(Const.APP_NAME, Const.APP_NAME + " DownloaderEx - onStartCommand: url: "+url+" startId: "+startId);
		if(!mTasks.containsKey(id)){
//				final Notification noti = createNotifyDownloading(app);
//				mNotificationManager.notify(app.getPackage().hashCode(), noti);
			Future<?> task = mPool.submit(new Runnable(){
	        	public void run() {
	    			download(id, url, maxBytes, maxSeconds);
	        	}
	        });
			mTasks.put(id, task);
		}
		
		return this.START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
    	for(String id : mTasks.keySet()){
    		mTasks.get(id).cancel(true);
//    		mPackageCanceled.put(id, true);
    	}
    	enableCheckTask = false;
		Log.d(Const.APP_NAME, Const.APP_NAME + " DownloaderEx - onDestroy");
    }

	protected void download(final String id, final String url, int maxBytes, int maxSeconds){
//		if(maxBytes <= 0) maxBytes = Integer.MAX_VALUE;
//		if(maxSeconds <= 0) maxSeconds = Integer.MAX_VALUE;

		String tmpFileName = Const.TMP_FOLDER + "/" + System.currentTimeMillis() + ".tmp";
		File tmpFile = new File(tmpFileName);
		Intent i = new Intent(Const.BROADCAST_DOWNLOAD_PROGRESS);
		try {
			sendBroadcast(new Intent(Const.BROADCAST_DOWNLOAD_PROGRESS)
				.putExtra(KEY_STARTED, true)
				.putExtra(KEY_ID, id)
				);

			HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
			InputStream in = con.getInputStream();
			int totalSize = con.getContentLength();
			totalSize = Math.min(totalSize, maxBytes);
			FileOutputStream out = new FileOutputStream(tmpFile);
			byte[] bytes = new byte[STEP];
			long sizeReceived = 0;
			int c;
			long startTime = System.currentTimeMillis();
			long lastTime = startTime;
//				int lastPercent = 0;
			long lastReceived = 0;
			long speed = 0;
			long elapsed = 0;
			int percent = 0;
			long remainTime = 0;
			while((c = in.read(bytes)) != -1 && sizeReceived < totalSize && elapsed < maxSeconds*1000){
//				if(mPackageCanceled.containsKey(id)){ 
				Future task = mTasks.get(id); 
				if(task == null || task.isCancelled()) break;

				out.write(bytes, 0 ,c);
				sizeReceived += c;
				 
				percent = (int)(sizeReceived*100/totalSize);
        		Log.v(Const.APP_NAME, Const.APP_NAME + " download progress: " + percent + " of "+url);
        		// send broadcast not more frequently to avoid UI scrolling feel not smooth 
    			long now = System.currentTimeMillis();
    			long dur = now - lastTime;
    			elapsed = now - startTime;
    			if(dur > UPDATE_INTERVAL){
    				speed = (sizeReceived)*1000/elapsed;
        			remainTime = speed > 0 ? (totalSize-sizeReceived)/speed : -1;
//	            		if(percent > 0 && percent < 100){
//	            			if(percent - lastPercent >= MULITI_DOWNLOADING){ // avoid produce lots of notifications, make sure not more than 100 notifications of them
//		            			//noti.contentView.setTextViewText(R.id.txtPercent, ""+percent+"%");
//			            		noti.contentView.setProgressBar(R.id.progressBar, 100, percent, false);
//			        			mNotificationManager.notify(app.getPackage().hashCode(), noti);
//			        			lastPercent = percent;
//	            			}
//	            		}
					i.putExtra(KEY_SIZE_TRANSFERRED, sizeReceived);
					i.putExtra(KEY_PERCENT, percent);
					i.putExtra(KEY_SPEED, speed);
					i.putExtra(KEY_ELAPSED, elapsed);
					i.putExtra(KEY_REMAIN_TIME, remainTime);
					i.putExtra(KEY_ID, id);
					sendBroadcast(i);
        			lastTime = now;
        			lastReceived = sizeReceived;
    			}
			}
			in.close();
			out.close();
//				tmpFile.renameTo(new File(app.getInstallPath()));
			tmpFile.delete();
//			mPackageCanceled.remove(id);
			mTasks.remove(id);

			Intent j = new Intent(Const.BROADCAST_DOWNLOAD_PROGRESS);
			j.putExtra(KEY_FINISHED, true);
			j.putExtra(KEY_SIZE_TRANSFERRED, sizeReceived);
			j.putExtra(KEY_PERCENT, percent);
			j.putExtra(KEY_SPEED, speed);
			j.putExtra(KEY_ELAPSED, elapsed);
			j.putExtra(KEY_REMAIN_TIME, remainTime);
			j.putExtra(KEY_ID, id);
			sendBroadcast(j);
//				notifyFinished(app);
		} catch (final Exception e) {
			String errMsg = "";
			if(e.getClass() == CancellationException.class){
//				Intent j = new Intent(Const.BROADCAST_DOWNLOAD_PROGRESS);
//				j.putExtra(KEY_FINISHED, true);
//				j.putExtra(KEY_URL, url);
//				sendBroadcast(j);
			}else{
				errMsg = e.getMessage();
//					notifyFailed(app, errMsg);
			}
			i = new Intent(Const.BROADCAST_DOWNLOAD_PROGRESS);
			i.putExtra(KEY_FAILED, errMsg);
			i.putExtra(KEY_ID, id);
			sendBroadcast(i);
		}
	}
	
//	protected Notification createNotifyDownloading(App app){
//		String text = String.format(getString(R.string.downloading_app), app.getName());
//		
//		Notification noti = Utils.getDefaultNotification(text);
//		noti.flags = Notification.FLAG_ONGOING_EVENT;
//		noti.contentView = new RemoteViews(getPackageName(),R.layout.download_notification);
//		noti.contentView.setTextViewText(R.id.txtTitle, app.getName());
//		Bitmap bm = null;
//		String url = app.getIcon();
//		bm = Utils.getImageFromFile(this, url); // file store 
//		if(bm != null) noti.contentView.setImageViewBitmap(R.id.icon, bm);
//		
//		Intent i = new Intent(this, DownloadingApp.class);
//		String str = app.getJSON().toString();
//		i.putExtra(Const.KEY_APP, str);
//		PendingIntent launchIntent = PendingIntent.getActivity(this, app.getPackage().hashCode(), i, PendingIntent.FLAG_UPDATE_CURRENT);
//		noti.contentIntent = launchIntent;
//		return noti;
//	}
//	
//	protected void notifyFinished(App app){
//		String text = String.format(getString(R.string.app_downloaded), app.getName());
//		String expandedText = getString(R.string.downloaded);
//		String expandedTitle = app.getName();
//		
//		Notification noti = Utils.getDefaultNotification(text);
//
//		Intent i = new Intent();
//		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		i.setAction(Intent.ACTION_VIEW);
//		String type = "application/vnd.android.package-archive";
//		i.setDataAndType(Uri.fromFile(new File(app.getInstallPath())), type);
//		PendingIntent launchIntent = PendingIntent.getActivity(this, app.getPackage().hashCode(), i, PendingIntent.FLAG_ONE_SHOT);
//		
//		noti.setLatestEventInfo(this, expandedTitle, expandedText, launchIntent);
//		
//		mNotificationManager.notify(app.getPackage().hashCode(), noti);
//	}
//
//	protected void notifyFailed(App app, String result){
//		String text = result;
//		String expandedText = result;
//		String expandedTitle = app.getName();
//		
//		Notification noti = Utils.getDefaultNotification(text);
//
//		Intent i = new Intent(this, goofy2.swably.fragment.App.class);
//		String str = app.getJSON().toString();
//		i.putExtra(Const.KEY_APP, str);
//		PendingIntent launchIntent = PendingIntent.getActivity(this, app.getPackage().hashCode(), i, PendingIntent.FLAG_ONE_SHOT);
//		
//		noti.setLatestEventInfo(this, expandedTitle, expandedText, launchIntent);
//		
//		mNotificationManager.notify(app.getPackage().hashCode(), noti);
//	}

}
