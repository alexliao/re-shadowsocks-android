package com.biganiseed.reindeer;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.biganiseed.reindeer.util.AsyncImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class NasAdapter extends ReindeerBaseAdapter {

	public NasAdapter(ReindeerActivity context, JSONArray stream,	HashMap<String, Integer> loadingImages) {
		super(context, stream, loadingImages);
	}

	public void bindView(View viewInfo, final JSONObject info) {
//		handleDivider(viewInfo);
		try {
			View viewItem = viewInfo.findViewById(R.id.viewItem);
			JSONObject nas = Tools.getCurrentNas(mContext);
			if(nas != null && info.optString("region").equals(nas.optString("region")) && info.optString("city").equals(nas.optString("city")))
//				viewInfo.setBackgroundColor(mContext.getResources().getColor(R.color.main_color));
				viewItem.setPressed(true);
			else
//				viewInfo.setBackgroundColor(Color.TRANSPARENT);
				viewItem.setPressed(false);
			
			ImageView iv = (ImageView) viewInfo.findViewById(R.id.imgFlag);
			new AsyncImageLoader(mContext, iv, mPosition).setThreadPool(mLoadImageThreadPool).loadUrl(info.optString("flag"));
			
			TextView tv;
			tv = (TextView) viewInfo.findViewById(R.id.txtName);
			tv.setText(info.optString("city_name"));
//			tv.setTypeface(mContext.mBoldFont);

			tv = (TextView) viewInfo.findViewById(R.id.txtPublicInfo);
			tv.setText(String.format(mContext.getString(R.string.route_public_info_x), info.optInt("online_count"),  info.optDouble("avg_speed")*8/1024));

			View viewProgress = viewInfo.findViewById(R.id.viewProgress);
			ProgressBar progressBar = (ProgressBar) viewInfo.findViewById(R.id.progressBar);
			TextView txtSizeSent = (TextView) viewInfo.findViewById(R.id.txtSizeSent);
			
			if(info.optBoolean(DownloaderEx.KEY_STARTED, false)){
				viewProgress.setVisibility(View.VISIBLE);
			}else{
				viewProgress.setVisibility(View.GONE);
			}
			
    		long speed = info.optLong(DownloaderEx.KEY_SPEED, 0);
    		txtSizeSent.setText(String.format(mContext.getString(R.string.transfer_speed), (float)speed*8/1024/1024));
    		int percent = info.optInt(DownloaderEx.KEY_PERCENT, 0);
    		if(percent > 0 ){
        		progressBar.setIndeterminate(false);
        		progressBar.setProgress(percent);
//            		txtSizeSent.setText(String.format(mContext.getString(R.string.transfer_progress), speed/1024, Tools.getFriendlyTime(mContext, remainTime)));
    		}else{
        		progressBar.setIndeterminate(true);
    		}

    		String errMsg = info.optString(DownloaderEx.KEY_FAILED);
    		if(errMsg != null){
    			if(!errMsg.equals("")) txtSizeSent.setText(errMsg);
    		}
    		
        	boolean finished = info.optBoolean(DownloaderEx.KEY_FINISHED, false);
    		RatingBar ratingBar = (RatingBar) viewInfo.findViewById(R.id.ratingBar);
        	if(finished){
     			ratingBar.setVisibility(View.VISIBLE);
        		ratingBar.setRating(getRatingBy(speed));
				progressBar.setVisibility(View.GONE);
     		}else{
     			ratingBar.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
     		}
         			
				
//			final View btnFollow = holder.btnFollow;
//			final View btnUnfollow = holder.btnUnfollow;
//			if(Utils.getCurrentUserId(mContext).equals(info.optString("id"))){
//				btnFollow.setVisibility(View.GONE);
//				btnUnfollow.setVisibility(View.GONE);
//			}else{
//				boolean isFollowed = info.optBoolean("is_followed", false); 
//				setStatus(btnFollow, btnUnfollow, isFollowed);
//				btnUnfollow.setOnClickListener(new View.OnClickListener(){
//					@Override
//					public void onClick(View v) {
//				        if(mContext.redirectAnonymous(false)) return;
//				        mContext.transitWidth(btnUnfollow, btnFollow);
//						Utils.follow(mContext, info.optString("id"), info.optString("name"), false, null, false);
//						try {
//							info.put("is_followed", false);
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//					}
//				});
//				btnFollow.setOnClickListener(new View.OnClickListener(){
//					@Override
//					public void onClick(View v) {
//				        if(mContext.redirectAnonymous(false)) return;
//				        mContext.transitWidth(btnFollow, btnUnfollow);
//						Utils.follow(mContext, info.optString("id"), info.optString("name"), true, null, false);
//						try {
//							info.put("is_followed", true);
//						} catch (JSONException e) {
//							e.printStackTrace();
//						}
//					}
//				});
//			}
		} catch (Exception e) {
//			Log.d(Const.APP_NAME, Const.APP_NAME + " UsersAdapter - bindView err: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
//	private void setStatus(View btnFollow, View btnUnfollow, boolean isFollowed){
//		if(isFollowed){
//			btnFollow.setVisibility(View.GONE);
//			btnUnfollow.setVisibility(View.VISIBLE);
//		}else{
//			btnFollow.setVisibility(View.VISIBLE);
//			btnUnfollow.setVisibility(View.GONE);
//			
//		}
//	}
	
	public View newView(ViewGroup parent) {
		View ret = null;
		int resId = R.layout.re_item_nas;
		ret = mInflater.inflate(resId, parent, false);
		return ret;
	}

//	@Override
//	protected Object newViewHolder(View convertView){
//		ViewHolder holder = new ViewHolder();
//		holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
//		holder.txtUsername = (TextView) convertView.findViewById(R.id.txtUsername);
//		holder.txtName = (TextView) convertView.findViewById(R.id.txtName);
//
//		holder.btnFollow = convertView.findViewById(R.id.btnFollow);
//		holder.btnUnfollow = convertView.findViewById(R.id.btnUnfollow);
//		return holder;
//	}
//	
//	static class ViewHolder	{
//		ImageView avatar;
//		TextView txtUsername;
//		TextView txtName;
//		View btnFollow;
//		View btnUnfollow;
//	}
	
	float getRatingBy(long speed){
		double FIVE_STAR = 500; // KB/s
		return (float) (5 * Math.sqrt(speed/1024)/Math.sqrt(FIVE_STAR));
	}
	
}
