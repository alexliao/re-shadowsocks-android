package com.biganiseed.reindeer;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class ReindeerSwitcher extends RelativeLayout {
	View viewOff, viewOn;
	View imgCircleOff, imgCircleOn, imgBodyOff, imgBodyOn;
	private OnClickListener onClickListener;
	private float density;

	public ReindeerSwitcher(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ReindeerSwitcher(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.re_reindeer_switcher, this);	

        density = context.getResources().getDisplayMetrics().density;
        
        loadView();
	}

	public ReindeerSwitcher(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	void loadView(){
		viewOff = findViewById(R.id.viewOff);
		viewOn = findViewById(R.id.viewOn);
		imgCircleOff = findViewById(R.id.imgCircleOff);
		imgCircleOn = findViewById(R.id.imgCircleOn);
		imgBodyOff = findViewById(R.id.imgBodyOff);
		imgBodyOn = findViewById(R.id.imgBodyOn);
		
		viewOff.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				imgCircleOff.setX(moveDistancePx());
				imgCircleOff.startAnimation(getMoveAnimation(0, moveDistancePx()));
				
				imgCircleOn.setX(moveDistancePx());
				onClickListener.onClick(v);
			}
		});
		
		viewOn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				imgCircleOn.setX(0);
				imgCircleOff.startAnimation(getMoveAnimation(moveDistancePx(), 0));

				
				imgCircleOff.setX(0);
				onClickListener.onClick(v);
			}
		});

	}
	
	Animation getWorkingAnimation(){
		Animation animation = new AlphaAnimation(1f, 0.7f);
		animation.setDuration(getResources().getInteger(R.integer.config_shortAnimTime));
		animation.setFillAfter(true);
		animation.setInterpolator(new LinearInterpolator());
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		return animation;
	}

	Animation getMoveAnimation(int fromX, int toX){
		Animation animation = new TranslateAnimation(fromX, toX, 0, 0);
		animation.setDuration(getResources().getInteger(R.integer.config_shortAnimTime));
		animation.setFillAfter(true);
		animation.setInterpolator(new AccelerateInterpolator());
		return animation;
	}
	
	int moveDistancePx(){
		int totalWidth = viewOff.getWidth();
		int circleWidth = imgCircleOff.getWidth();
		return totalWidth - circleWidth;
	}

	public boolean isChecked() {
		// TODO Auto-generated method stub
		return viewOn.isShown();
	}

	public void setWorking(boolean b) {
		if(b){
			this.startAnimation(getWorkingAnimation());
			viewOff.setEnabled(false);
			viewOn.setEnabled(false);
		}else{
			this.clearAnimation();
			viewOff.setEnabled(true);
			viewOn.setEnabled(true);
		}
	}

	public void setChecked(boolean b) {
		if(b){
			viewOff.setVisibility(View.INVISIBLE);
			viewOn.setVisibility(View.VISIBLE);
//			imgCircleOn.startAnimation(getMoveAnimation((int)imgCircleOn.getX(), moveDistancePx()));
		}else{
			viewOff.setVisibility(View.VISIBLE);
			viewOn.setVisibility(View.INVISIBLE);
			
			final Handler handler = new Handler();
			Timer timer = new Timer();
	    	timer.schedule(new TimerTask(){
	    		@Override
	    		public void run(){
	    			handler.post(new Runnable(){
						@Override
						public void run() {
							imgCircleOff.startAnimation(getMoveAnimation((int)imgCircleOff.getX(), 0));
						}
	    			});
	    		}
	    	}, getResources().getInteger(R.integer.config_shortAnimTime) + 50); // delay execution to avoid breaking the animation
		}
	}
	
	public void setOnClickListener(View.OnClickListener listener){
		onClickListener = listener;
	}
	
	public void click(){
		if(isChecked()){
			viewOn.performClick();
		}else{
			viewOff.performClick();
		}
	}
}
