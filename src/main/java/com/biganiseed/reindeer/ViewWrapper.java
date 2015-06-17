package com.biganiseed.reindeer;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;

public class ViewWrapper {
	private View mView;
	public ViewWrapper(View v){
		mView = v;
	}
	
	public void setWidth(int value){
		LayoutParams lp = mView.getLayoutParams();
		lp.width = value;
		mView.setLayoutParams(lp);
	}

	public int getWidth(){
		return mView.getWidth();
	}

	public void setHeight(int value){
		LayoutParams lp = mView.getLayoutParams();
		lp.height = value;
		mView.setLayoutParams(lp);
	}

	public int getHeight(){
		return mView.getHeight();
	}
}
