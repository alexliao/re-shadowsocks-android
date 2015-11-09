package com.biganiseed.reindeer.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.Tools;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class AsyncImageLoader {
	Context mContext;
	ImageView mImageView;
	int mInitPosition;
	ExecutorService mThreadPool;
	int mReqWidth = 0;
	int mReqHeight = 0;
	Runnable mCallback = null;
	
	public AsyncImageLoader(Context context, ImageView iv, int initPosition){
		mContext = context;
		mInitPosition = initPosition;
		mImageView = iv;
		iv.setTag(initPosition);
	}

	public AsyncImageLoader setThreadPool(ExecutorService threadPool){
		mThreadPool = threadPool;
		return this;
	}
	
	private ExecutorService getThreadPool(){
		if(mThreadPool == null) mThreadPool = Executors.newFixedThreadPool(Const.MULITI_DOWNLOADING); 
		return mThreadPool;
	}
	

	public AsyncImageLoader setRequestSize(int reqWidth, int reqHeight){
		mReqWidth = reqWidth;
		mReqHeight = reqHeight;
		return this;
	}

	public AsyncImageLoader setCallback(Runnable callback){
		mCallback = callback;
		return this;
	}
		
	public void loadApkPath(final String packageName, final PackageManager packageManager){
		load(packageName, new Loader(){
			@Override
			public void load(String uri) {
				Tools.saveLocalApkIcon(packageManager, uri);
			}
		});
	}
	
	public void loadUrl(final String url){
		load(url, new Loader(){
			@Override
			public void load(String uri) {
				Tools.saveImageToFile(mContext, uri, Const.HTTP_TIMEOUT);
			}
		});
	}
	
	public void load(final String uri, final Loader loader){
		if(uri == null) return;
		final String pathName = Tools.getImageFileName(uri);
		Bitmap bm = Tools.getImageFromFile(mContext, pathName, mReqWidth, mReqHeight);
		if(bm != null){
			mImageView.setImageBitmap(bm);
			if(mCallback != null) mCallback.run();
		}else{
			final Handler handler = new Handler();
			ParamRunnable pr = new ParamRunnable(){
		    	public void run() {
		    		loader.load(uri);
            		int position = (Integer) mImageView.getTag();
//		        			Utils.logV(AsyncImageLoader.this, "position:" + position + " initPosition:" + mInitPosition);
            		if(position == mInitPosition){
    					final Bitmap bm = Tools.getImageFromFile(mContext, pathName, mReqWidth, mReqHeight);
    					if(bm != null){
    						handler.post(new Runnable(){
    							@Override
    							public void run() {
    		            			mImageView.setImageBitmap(bm);
    		            			Animation anim = new AlphaAnimation(0.3f, 1f);
    		            			anim.setDuration(mContext.getResources().getInteger(R.integer.config_mediumAnimTime));
    		            			mImageView.startAnimation(anim);
    		            			if(mCallback != null) mCallback.run();
    							}
    						});
    					}
            		}
//            		else
//            			Utils.logV(AsyncImageLoader.this, "position changed");
		    	}
			};
			getThreadPool().execute(pr);
		}
	}

	static interface Loader{
		public void load(String uri);
	}

//	public void loadContactAvatar(String url){
//		load(url, new Loader(){
//			@Override
//			public void load(String uri) {
//				try {
//					ContentResolver cr = mContext.getContentResolver();
//					long id = Long.parseLong(uri.substring(uri.lastIndexOf("/")+1));
//					Uri uriContact =ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
//				    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uriContact);
//				    Bitmap contactPhoto = BitmapFactory.decodeStream(input);
//					String fileName = Utils.getImageFileName(uri);
//					File f = new File(fileName);
//			        FileOutputStream out = new FileOutputStream(f);   
//			        contactPhoto.compress(Bitmap.CompressFormat.PNG, 100, out);
//				} catch (Exception e) {
//					e.printStackTrace();
//				} catch (OutOfMemoryError e){
//					e.printStackTrace();
//				}
//			}
//		});
//	}

}
