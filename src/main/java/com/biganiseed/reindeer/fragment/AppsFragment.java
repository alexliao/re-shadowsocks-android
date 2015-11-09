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
import com.biganiseed.reindeer.AppHelper;
import com.biganiseed.reindeer.BuildConfig;
import com.biganiseed.reindeer.DownloaderEx;
import com.biganiseed.reindeer.LocalAppsAdapter;
import com.biganiseed.reindeer.ReindeerBaseAdapter;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.NasAdapter;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.ReindeerVpnService;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.data.App;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
public class AppsFragment extends ReindeerListFragment {
	protected CacheProgressBroadcastReceiver mCacheProgressReceiver = new CacheProgressBroadcastReceiver();
   	Cursor cursor;
	SQLiteDatabase db;
	private View viewProgress;
	private ProgressBar progressBar;
	private TextView txtPrompt;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	AppHelper helper = new AppHelper(a());
    	db = helper.getHelper().getReadableDatabase();
    	super.onCreate(savedInstanceState);
//    	a().registerReceiver(mRefreshAppProgressReceiver, new IntentFilter(Const.BROADCAST_REFRESH_APP));
      a().registerReceiver(mCacheProgressReceiver, new IntentFilter(Const.BROADCAST_CACHE_APPS_PROGRESS));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
    	View v = super.onCreateView(inflater, container, savedInstanceState);
//        bind(v);
    	viewProgress = v.findViewById(R.id.viewProgress);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        txtPrompt = (TextView) v.findViewById(R.id.txtPrompt);
        bindPrompt();
        return v;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
        HeaderFragment.setTitle(a(), getString(R.string.menu_apps));
    }

    @Override 
    public void onStop(){
    	super.onStop();
    }

    @Override
    public void onDestroy(){
    	try{
//    		a().unregisterReceiver(mRefreshAppProgressReceiver);
    		a().unregisterReceiver(mCacheProgressReceiver);
    	}catch (Exception e){
    		e.printStackTrace();
    	}
		super.onDestroy();
    }

    protected void bindPrompt(){
    	int count = App.getSelectedAppsCount(a());
    	if(count == 0)
    		txtPrompt.setText(R.string.prompt_select_apps);
    	else
    		txtPrompt.setText(String.format(getString(R.string.prompt_select_apps_x), count));
    }

    @Override
	protected String loadStream(String url, String lastId) {
		Log.d(Const.APP_NAME, Const.APP_NAME + " LocalApps loadStream lastId: " + lastId);
		String err = null;
		try{
			AppHelper helper = new AppHelper(a());
			cursor = helper.getApps(db, false);
		}catch (Exception e){
			err = e.getMessage();
			Log.e(Const.APP_NAME, Const.APP_NAME + " LocalApps loadStream err: " + err);
		}
		return err;
	}


	@Override
    protected void setData(){
		((LocalAppsAdapter) mAdapter).setData(cursor);
    }

	@Override
	protected String getUrl() {
		return "";
	}

	@Override
	protected JSONArray getListArray(String result) throws JSONException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ReindeerBaseAdapter getAdapter() {
		return new LocalAppsAdapter(ga(), mListData, mLoadingImages);
	}

	@Override
	protected void onClickItem(int position) throws JSONException {
		JSONObject item = (JSONObject) ((LocalAppsAdapter) mAdapter).getItem(position);
    	App app = new App(item);
    	app.saveAsSelected(a(), !app.isSelected(a()));
		this.mAdapter.notifyDataSetChanged();
        bindPrompt();
	}

	@Override
	protected void setContent() {
	   setContentView(R.layout.re_apps_fragment);
	}
	
	@Override
	protected void loadedMore(boolean successed) {
//		bind(getView());
	}
    
  protected class CacheProgressBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(Const.BROADCAST_CACHE_APPS_PROGRESS)){
      	int count = intent.getIntExtra(Const.KEY_COUNT, 0);
      	int total = intent.getIntExtra(Const.KEY_TOTAL, 1);
  		//if(count > 0) txtHeader.setText(String.format(getString(R.string.app_count), count));
  		boolean finished = intent.getBooleanExtra(Const.KEY_FINISHED, false);
  		
  		if(finished){
  			refreshWithoutLoading();
  			viewProgress.setVisibility(View.GONE);
  		}else{
  			viewProgress.setVisibility(View.VISIBLE);
  			int percent = count*100/total;
  			if(percent >= 100){
      			progressBar.setIndeterminate(true);
  			}else{
      			progressBar.setIndeterminate(false);
      			progressBar.setProgress(percent);
  			}
//  			try {
//					JSONObject json = new JSONObject(intent.getStringExtra(Const.KEY_APP));
//					JSONArray left = new JSONArray();
//					left.put(json);
//					mListData = JSONUtils.appendArray(left, mListData);
//					mAdapter.setData(mListData);
//					refreshListView();
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
  		}
      }
  }
}



}
