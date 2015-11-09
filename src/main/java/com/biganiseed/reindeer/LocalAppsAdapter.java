package com.biganiseed.reindeer;

import java.util.Date;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.biganiseed.reindeer.data.App;
import com.biganiseed.reindeer.util.AsyncImageLoader;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocalAppsAdapter extends ReindeerBaseAdapter {
   	PackageManager mPackageManager;
   	Cursor cursor;
	 

	public LocalAppsAdapter(ReindeerActivity context, JSONArray stream,	HashMap<String, Integer> loadingImages) {
		super(context, stream, loadingImages);
		mPackageManager = context.getPackageManager();
	}

	public void bindView(View view, final JSONObject jsonApp) {
		try {
			//String id = update.getString("id");
			//FeedHelper.bindUpdate(mContext, view, update, this, mLoadingImages, false);
			final App app = new App(jsonApp);
			ViewHolder holder = (ViewHolder) view.getTag();
			
			holder.txtName.setText(app.getName());

			bindIcon(view, holder.icon, app);
			
			boolean isSelected = app.isSelected(mContext);
			holder.checkbox.setChecked(isSelected);
//			if(isSelected) holder.txtName.setTextColor(Color.BLACK);
//			else holder.txtName.setTextColor(Color.GRAY);
			if(isSelected) view.setBackgroundColor(Color.LTGRAY);
			else view.setBackgroundColor(Color.TRANSPARENT);
			
			
		} catch (Exception e) {
			Log.d(Const.APP_NAME, Const.APP_NAME + " StreamAdapter - bindView err: " + e.getMessage());
		}
	}
	
	void bindIcon(View view, ImageView iv, App app){
		new AsyncImageLoader(mContext, iv, mPosition).setThreadPool(mLoadImageThreadPool).loadApkPath(app.getPackage(), mPackageManager);
	}

	public void setData(Cursor aCursor) {
		cursor = aCursor;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if(cursor == null) return 0;
		return cursor.getCount();
	}

	@Override
	public Object getItem(int arg0) {
		Object ret = null;
		try {
			cursor.moveToPosition(arg0);
			ret = new JSONObject(cursor.getString(cursor.getColumnIndexOrThrow(AppHelper.DETAILS)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}

	public View newView(ViewGroup parent) {
		View ret = null;
		int resId = R.layout.re_app_row;
		ret = mInflater.inflate(resId, parent, false);
		return ret;
	}

	@Override
	protected Object newViewHolder(View convertView){
		ViewHolder holder = new ViewHolder();
		holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
		holder.icon = (ImageView) convertView.findViewById(R.id.icon);
		holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
		return holder;
	}
	
	static class ViewHolder {
		ImageView icon;
		TextView txtName;
		CheckBox checkbox;
	}
}
