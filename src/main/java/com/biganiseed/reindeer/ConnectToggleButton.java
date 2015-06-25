package com.biganiseed.reindeer;

import com.biganiseed.reindeer.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ConnectToggleButton extends ToggleButton {
	private boolean isRotating;
	private Animation rotateAnimation;
	
	public ConnectToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ConnectToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ConnectToggleButton(Context context) {
		super(null);
		init();
	}
	
	private void init(){
//		rotateAnimation  = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_center);
//		rotateAnimation.setFillAfter(false);
//		rotateAnimation.setInterpolator(new LinearInterpolator());
	}

//	@Override
//	public void setOnClickListener(final View.OnClickListener listener){
//		super.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				setChecked(!isChecked()); // keep old status no change  
//				listener.onClick(v); 
//			}
//		});
//	}
	
	public void setWorking(boolean value){
		setEnabled(!value);
		if(value == true){
			isRotating = true;
//			startAnimation(rotateAnimation);
		}else{
			isRotating = false;
		}
	}

	@Override
	protected void onAnimationEnd(){
		super.onAnimationEnd();
		//keep rotate until the status changed
//		if(isRotating) startAnimation(rotateAnimation);
	}
}
