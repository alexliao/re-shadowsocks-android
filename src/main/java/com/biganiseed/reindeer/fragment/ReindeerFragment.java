package com.biganiseed.reindeer.fragment;

import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.ReindeerActivity;
import com.biganiseed.reindeer.Tools;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class ReindeerFragment extends Fragment {

	ReindeerActivity ga(){
		return (ReindeerActivity) getActivity();
	}
	
	Activity a(){
		return getActivity();
	}

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


	protected void navigate(Fragment fragment, String tag) {
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//		fragmentTransaction.setCustomAnimations(R.animator.slide_in_from_top, R.animator.slide_out_to_top, R.animator.slide_in_from_top, R.animator.slide_out_to_top);
		fragmentTransaction.replace(R.id.viewBody, fragment, tag);
		fragmentTransaction.addToBackStack(tag);
		fragmentTransaction.commit();
    }


	@Override
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onPageStart(this.getClass().getSimpleName()); //统计页面
	}

	@Override
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPageEnd(this.getClass().getSimpleName()); 
	}
}
