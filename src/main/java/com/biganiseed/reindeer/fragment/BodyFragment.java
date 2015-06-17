package com.biganiseed.reindeer.fragment;

import android.app.FragmentManager;

public class BodyFragment extends ReindeerFragment {

    @Override
    public void onStart(){
    	super.onStart();
        HeaderFragment.collapse(a(), collapseHeader());
    }

    public boolean collapseHeader(){
//    	return needHeightDp() > ga().getScreenHeightDp();
    	return true;
    }

	protected void finishFragment() {
		FragmentManager fragmentManager = getFragmentManager();
	    fragmentManager.popBackStack();
	}

//  @Override
//  public void onResume(){
//  	super.onResume();
//      HeaderFragment.collapse(a(), collapseHeader());
//  }
  
  
//  // this fragment layout need the minimal screen height in dip to display full content, override it to provide specific value.
//  protected int needHeightDp(){
//  	return 0;
//  }
}
