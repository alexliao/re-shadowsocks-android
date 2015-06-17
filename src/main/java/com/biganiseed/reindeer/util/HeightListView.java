package com.biganiseed.reindeer.util;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
public class HeightListView extends ListView {
	/**
	 * 
	 */
	private static final String TAG = HeightListView.class.getSimpleName();
	/**
	 * This height come with normal layout, don`t know where take this constant  
	 */
	//private static final int WIDE_HEIGHT = -2147483218;
	private static final int MAX_HEIGHT = 10000;

	public HeightListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HeightListView(Context context) {
		super(context);
	}

	public HeightListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		if (Log.isLoggable(TAG, Log.DEBUG)) {
//			Log.d(TAG, "onMeasure, widthMeasureSpec=" + widthMeasureSpec
//					+ ", heightMeasureSpec=" + heightMeasureSpec);
//			Log.d(TAG, "onMeasure, widthMeasureSpec size:" + MeasureSpec.getSize(widthMeasureSpec)
//					+ ", heightMeasureSpec size:" + MeasureSpec.getSize(heightMeasureSpec));
//		}
		//int size = MeasureSpec.getSize(heightMeasureSpec);
		int heightSpec = MeasureSpec.makeMeasureSpec(MAX_HEIGHT, View.MeasureSpec.AT_MOST);
		//Log.d(TAG, "onMeasure, set heightMeasureSpec=" + heightSpec);
		super.onMeasure(widthMeasureSpec, heightSpec);
	}
}
