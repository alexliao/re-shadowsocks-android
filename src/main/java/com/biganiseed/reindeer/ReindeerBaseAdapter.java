package com.biganiseed.reindeer;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public abstract class ReindeerBaseAdapter extends BaseAdapter {
	protected ReindeerActivity mContext;
	protected LayoutInflater mInflater;
	protected JSONArray mListData;
	protected HashMap<String, Integer> mLoadingImages;
	private boolean mIsFirst = false;
	private boolean mIsLast = false;
	protected int mPosition = -1;
	ExecutorService mLoadImageThreadPool = Executors.newFixedThreadPool(2);

	public ReindeerBaseAdapter(ReindeerActivity context, JSONArray list, HashMap<String, Integer> loadingImages){
		this.mContext = context;
        mInflater = LayoutInflater.from(context);
		this.mListData = list;
		mLoadingImages = loadingImages;
	}

	@Override
	public int getCount() {
		return mListData.length();
	}

	@Override
	public Object getItem(int arg0) {
		Object ret = null;
		try {
			ret = mListData.get(arg0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView (int position, View convertView, ViewGroup parent){
		Object holder;
		if(convertView == null){
			convertView = newView(parent);
			holder = newViewHolder(convertView);
			convertView.setTag(holder);
		}
//		ListView lv = (ListView) parent;
		mIsFirst = (position == 0);
		mIsLast = (position == getCount()-1);
		mPosition = position;
		bindView(convertView, (JSONObject)getItem(position));
		return convertView;
	}

	public boolean isFirst(){
		return mIsFirst;
	}
	public boolean isLast(){
		return mIsLast;
	}
	
// obsoleted
//	protected void handleDivider(View viewInfo){
//		// handle divider
//		View vd;
//		vd = viewInfo.findViewById(R.id.topDivider);
//		if(vd != null){
//			if(isFirst()) vd.setVisibility(View.GONE);
//			else vd.setVisibility(View.VISIBLE);
//		}
//		vd = viewInfo.findViewById(R.id.bottomDivider);
//		if(vd != null){
//			if(isLast()) vd.setVisibility(View.GONE);
//			else vd.setVisibility(View.VISIBLE);
//		}
//	}

	protected abstract void bindView(View v, JSONObject jsonObject);

	protected abstract View newView(ViewGroup parent);

	protected Object newViewHolder(View convertView){
		return null;
	}
	

	public void setData(JSONArray data) {
		mListData = data;
	}
}
