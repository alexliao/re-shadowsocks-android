package com.biganiseed.reindeer.fragment;

//import eu.erikw.PullToRefreshListView.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.biganiseed.reindeer.ReindeerBaseAdapter;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.util.JSONUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ResourceCursorAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.biganiseed.reindeer.Api;

public abstract class ReindeerListFragment extends BodyFragment {
	private int mLayoutId;
	protected ListView mList = null;
	protected ReindeerBaseAdapter mAdapter;
	protected JSONArray mListData = new JSONArray();
	protected int mLastLoaded = -1;
	protected String mData;
	protected HashMap<String, Integer> mLoadingImages = new HashMap<String, Integer>();
//	private View viewFooter;
//	private View loadingMore;
//	private TextView txtMore;
//	private TextView txtNoMore;
//	private boolean mIsScrolling = false;
//	private boolean mIsDirty = false;
	protected boolean mIsLoading = false;
	protected int mFirstVisibleItem = 0;
	protected int mVisibleItemCount = 1000;
	private View mListHeader;
	
	ArrayList<AsyncTask<Void, Void, Long>> mLoadTasks = new ArrayList<AsyncTask<Void, Void, Long>>();   

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mAdapter = getAdapter();
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		Utils.logV(this, "CloudFragment onCreateView: " + savedInstanceState);
		setContent(); // set mLayoutId in sub class
		View v = inflater.inflate(mLayoutId, container, false);

		View lv = v.findViewById(R.id.list);
		// Log.d(Const.APP_NAME, Const.APP_NAME + " list class: " +
		// v1.getClass().toString());
//		if (lv.getClass() == PullToRefreshListView.class) {
//			mListContainer = (PullToRefreshListView) lv;
//			mList = mListContainer.getRefreshableView();
//		} else
			mList = (ListView) lv;
		mListHeader = getListHeader();
		if (mListHeader != null)
			mList.addHeaderView(mListHeader);
		View vt = getRowTop();
		if (vt != null)
			mList.addHeaderView(vt);
		View vb = getRowBottom();
		if (vb != null)
			mList.addFooterView(vb);
//		viewFooter = LayoutInflater.from(this.getActivity()).inflate(
//				R.layout.list_footer, null);
//		mList.addFooterView(viewFooter);
//		loadingMore = (View) viewFooter.findViewById(R.id.loadingMore);
//		txtMore = (TextView) viewFooter.findViewById(R.id.txtMore);
//		txtNoMore = (TextView) viewFooter.findViewById(R.id.txtNoMore);
//		txtMore.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				loadMore();
//			}
//		});

//		mList.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScrollStateChanged(AbsListView view, int scrollState) {
//				onListScrollStateChanged(view, scrollState);
//			}
//
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//				onListScroll(view, firstVisibleItem, visibleItemCount,
//						totalItemCount);
//				// Log.v(Const.APP_NAME, Const.APP_NAME + " CloudListFragment onScroll");
//			}
//		});

		mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, final View arg1,
					int position, long arg3) {
				try {
					int index = position - mList.getHeaderViewsCount();
					if(index < 0) onClickHeader();
					else onClickItem(index);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

//		if (mListContainer != null)
//			mListContainer.setOnRefreshListener(new OnRefreshListener() {
//				@Override
//				public void onRefresh() {
//					refreshWithoutLoading();
//				}
//			});

		mList.setAdapter(mAdapter);

    	return v;
	}

    @Override
    public void onActivityCreated (Bundle savedInstanceState){
    	super.onActivityCreated(savedInstanceState);
		loadData();
    }
    
//    @Override
//    public void onResume(){
//    	super.onResume();
//    	this.refreshListView();
//    }
    
    @Override
    public void onStop(){
    	super.onStop();
//		if(mAdapter instanceof CloudInplaceActionsAdapter) ((CloudInplaceActionsAdapter) mAdapter).mHelper.hideActionsAnim();
    }

    @Override
    public void onDestroy(){
    	for(AsyncTask<Void, Void, Long> task : mLoadTasks){
    		task.cancel(true);
    	}
    	super.onDestroy();
    }
    
    protected void loadData(){
    	final Handler handler = new Handler();
		Thread t = new Thread() {
			public void run(){
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} // delay for sliding menu close animation
//				final String strCache = loadCache();
//				try {
//					if (strCache != null) mListData = new JSONArray(strCache);
			    	handler.post(new Runnable(){
						@Override
						public void run() {
							try {
//								if (strCache != null) {
//									mAdapter.setData(mListData);
//									refreshListView();
//								}
								if (getUrl() != null){
//									if(strCache == null || (System.currentTimeMillis() - getCacheAt()) > getCacheExpiresIn()) 
										refresh();
								}
							} catch (Exception e2) {
								e2.printStackTrace();
							}
						}
			    	});
//				} catch (JSONException e1) {
//					e1.printStackTrace();
//				}
			}
    	};
    	t.setPriority(Thread.MIN_PRIORITY);
		t.start();
    }

//	public void onListScrollStateChanged(AbsListView view, int scrollState) {
//		Log.v(Const.APP_NAME, Const.APP_NAME + " CloudListFragment onScrollStateChanged: "
//				+ scrollState);
//		if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
//			mIsScrolling = true;
//		} else {
//			mIsScrolling = false;
//			if (mIsDirty)
//				refreshListView();
//			if (view.getCount() >= Const.LIST_SIZE
//					&& view.getLastVisiblePosition() > (view.getCount() - 2)) {
//				Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment try load");
//				loadMore();
//			}
//		}
//	}

//	protected void onListScroll(AbsListView view, int firstVisibleItem,
//			int visibleItemCount, int totalItemCount) {
//		Log.v(Const.APP_NAME, Const.APP_NAME + " CloudListFragment onListScroll: "
//				+ firstVisibleItem);
//		mFirstVisibleItem = firstVisibleItem;
//		mVisibleItemCount = visibleItemCount;
//	}

//	public boolean isItemVisible(int position) {
//		return position >= mFirstVisibleItem
//				&& position <= mFirstVisibleItem + mVisibleItemCount;
//	}

	abstract protected void setContent();

	protected void setContentView(int layoutId) {
		mLayoutId = layoutId;
	}

//	protected void loadMore() {
//		String lastId = null;
//		if (txtNoMore.getVisibility() != View.VISIBLE
//				&& loadingMore.getVisibility() != View.VISIBLE) {
//			try {
//				if (mListData.length() > 0)
//					lastId = mListData.getJSONObject(mListData.length() - 1)
//							.getString(getIdName());
//				loadingMore.setVisibility(View.VISIBLE);
//				txtMore.setVisibility(View.GONE);
//				txtNoMore.setVisibility(View.GONE);
//				loadList(getUrl(), lastId);
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
	protected void loadedMore(boolean successed) {
//		loadingMore.setVisibility(View.GONE);
//		txtMore.setVisibility(View.GONE);
//		txtNoMore.setVisibility(View.GONE);
//		if (successed) {
//			if (mLastLoaded == 0)
//				txtNoMore.setVisibility(View.VISIBLE);
//			else
//				txtNoMore.setVisibility(View.INVISIBLE);
//		} else {
//			txtMore.setVisibility(View.VISIBLE);
//		}
	}

	protected void loadList(final String url, final String lastId) {
		AsyncTask<Void, Void, Long> loadTask = new AsyncTask<Void, Void, Long>() {
			private String mErr = null;

			protected Long doInBackground(Void... params) {
//				Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment loadList start: " + url);
				mIsLoading = true;
				mErr = loadStream(url, lastId);
				mIsLoading = false;
				return null;
			}

			protected void onPostExecute(Long result) {
//				Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment loadList end: " + url);
//				if (ca() == null){
//					Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment ca()==null");
//					return;
//				}
				if(ga() != null) ga().hideLoading();
//				if (mListContainer != null)
//					mListContainer.onRefreshComplete();
				if (mErr == null) {
//					if (getCacheId() != null)
//						cacheData(mListData.toString());
					mAdapter.setData(mListData);
					refreshListView();
					loadedMore(true);
				} else {
//					Toast.makeText(a(), getString(R.string.err_load_failed));
//					Log.d("Nappst",
//							Const.APP_NAME + " CloudListFragment loadList error: " + mErr);
//					loadedMore(false);
//					txtMore.setVisibility(View.VISIBLE);
				}
			}
		};
		mLoadTasks.add(loadTask);
		loadTask.execute();
	}

	protected String loadStream(String url, String lastId) {
//		Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment loadStream: " + lastId);
		if (lastId != null)
			url += "&max_id=" + lastId;
		String err = null;
		String strResult = null;
		try {
			HttpGet httpReq = new HttpGet(url);
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters,
					Const.HTTP_TIMEOUT);
			httpReq.setParams(httpParameters);
			HttpResponse httpResp = Api.initHttpClient(httpParameters).execute(httpReq);
			strResult = EntityUtils.toString(httpResp.getEntity());
			int code = httpResp.getStatusLine().getStatusCode();
			if (code == 200) {
//				Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListActivity loadStream ok: "
//						+ lastId);
				mData = strResult;
				// mListData = JSONUtils.appendArray(mListData,
				// getListArray(strResult));
				JSONArray loaded = getListArray(strResult);
//				cacheItems(loaded);
				mLastLoaded = loaded.length();
				if (lastId == null) {
					mLoadingImages.clear();
					mListData = loaded;
				} else {
					mListData = JSONUtils.appendArray(mListData, loaded);
				}
			} else {
				onHttpError(strResult, code);
			}
		} catch (Exception e) {
			// Utils.alertTitle(this, getString(R.string.err_no_network_title),
			// e.getMessage());
			err = e.getMessage();
//			Log.e(Const.APP_NAME, Const.APP_NAME + " CloudListFragment loadStream err: " + err);
		}
		return err;
	}

//	protected void cacheItems(JSONArray loaded) {
//	}

	abstract protected String getUrl();

	protected String onHttpError(String strResult, int code)
			throws JSONException {
		JSONObject json = new JSONObject(strResult);
		String err = json.getString("error_message");
//		Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment loadStream err: " + err);
		return err;
	}


//	protected boolean saveImage(String imageUrl) {
//		return Utils.saveImageToFile(getActivity(), imageUrl,
//				Const.HTTP_TIMEOUT_LONG, null);
//	}

	protected int getImageCount(JSONObject item) throws JSONException {
		return 1;
	}

//	protected abstract String getImageUrl(JSONObject item, int index)
//			throws JSONException;

	protected void refresh() {
		ga().showLoading();
//		loadingMore.setVisibility(View.VISIBLE);
		refreshWithoutLoading();
	}

	protected void refreshWithoutLoading() {
//		if(mAdapter instanceof CloudInplaceActionsAdapter) ((CloudInplaceActionsAdapter) mAdapter).mHelper.hideActionsAnim();
		loadList(getUrl(), null);
//		mList.setSelection(0);
	}

//	@Override
//	protected void onDataChanged(int item) {
//		Log.d(Const.APP_NAME, Const.APP_NAME + " CloudListFragment onDataChanged item:" + item
//				+ " mIsScrolling:" + mIsScrolling + " isItemVisible:"
//				+ isItemVisible(item));
//		mIsDirty = true;
//		if (!mIsScrolling) {
//			if (item < 0 || isItemVisible(item))
//				refreshListView();
//		}
//
//	}

	protected void refreshListView() {
		mAdapter.notifyDataSetChanged();
//		mIsDirty = false;
	}

	protected String getIdName() {
		return "id";
	}

	protected JSONArray getListArray(String result) throws JSONException {
		return new JSONArray(result);
	}

	protected void onClickHeader(){};

	abstract protected ReindeerBaseAdapter getAdapter();

	protected abstract void onClickItem(int position) throws JSONException;

	protected CharSequence getListTitle() {
		return null;
	}

	protected View getListHeader() {
		return null;
	}

	protected View getRowTop() {
		return null;
	}

	protected View getRowBottom() {
		return null;
	}

	protected void showList() {
//		if (mListContainer != null)
//			mListContainer.setVisibility(View.VISIBLE);
//		else
			mList.setVisibility(View.VISIBLE);
	}

	protected void hideList() {
//		if (mListContainer != null)
//			mListContainer.setVisibility(View.GONE);
//		else
			mList.setVisibility(View.GONE);
	}
}