package com.biganiseed.reindeer;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.shadowsocks.R;
import com.umeng.analytics.MobclickAgent;

public class ReindeerActivity extends Activity {
	ProgressDialogFragment progressDialog = new ProgressDialogFragment();
	
    @Override
	public void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	
    @Override
	public void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
		
    @Override
    protected Dialog onCreateDialog(int id) {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.loading));
        dialog.setIndeterminate(true);
        //dialog.setCancelable(false);
        //dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }


    public void ayncRun(final Runnable task, final Runnable succeededUI){
    	ayncRun(task, succeededUI, true);
    }
    
    public void ayncRun(final Runnable task, final Runnable succeededUI, final boolean showLoading){
		final Handler handler = new Handler();
//		if(showLoading) showProgressDialog();
//		if(showLoading) showDialog(0);
		if(showLoading) showLoading();
		new Thread() {
			public void run(){
				try {
					task.run();
					handler.post(succeededUI);
				} catch (final Exception e) {
					handler.post(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				} finally{
					handler.post(new Runnable(){
						@Override
						public void run() {
//							if(showLoading) hideProgressDialog();
//							if(showLoading) removeDialog(0);
							if(showLoading) hideLoading();
						}
					});
				}
			}
		}.start();
    	
    }

	public boolean HttpTest()
	{ 
		boolean ret = true;
	    if( !Tools.isNetworkAvailable(this) ){
	    	ret = false;
	      AlertDialog.Builder builders = new AlertDialog.Builder(this);
	      builders.setTitle(getString(R.string.err_no_network_title));
	      builders.setMessage(getString(R.string.err_no_network_message));
	      //LayoutInflater _inflater = LayoutInflater.from(mActivity);
	      //View convertView = _inflater.inflate(R.layout.error,null);
	      //builders.setView(convertView);
	      builders.setPositiveButton(getString(R.string.menu_settings),  new DialogInterface.OnClickListener(){
		      public void onClick(DialogInterface dialog, int which)
		      {
		    	  startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)); 		      
		      }       
	      });
	      builders.setNegativeButton(getString(android.R.string.cancel), null);
	      builders.show();
	    }
	    return ret;
	}	

    public void share(String title, String content){
		try {
	        Intent intent = new Intent(Intent.ACTION_SEND);
	        intent.setType("text/plain");
	        intent.putExtra(Intent.EXTRA_TEXT, content);
	    	Intent i = Intent.createChooser(intent, title);
	    	startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    
	public void showProgressDialog() {
		Log.d(Const.APP_NAME, "LadderActivity showProgressDialog");
//		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//		ft.addToBackStack(null);

//	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//	    Fragment prev = getSupportFragmentManager().findFragmentByTag("progress");
//	    if (prev != null) {
//	        ft.remove(prev);
//	    }
//	    ft.addToBackStack(null);
//		progressDialog.show(ft, "progress");
		progressDialog.show(getFragmentManager(), null);
	}
	
	public void hideProgressDialog() {
		try{
			Log.d(Const.APP_NAME, "LadderActivity hideProgressDialog");
			progressDialog.dismiss();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
////		getSupportMenuInflater().inflate(R.menu.main_purchase, menu);
//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		return true;
//	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
    
	public void showLoading(){
		View loading = findViewById(R.id.loading);
		if(loading != null) loading.setVisibility(View.VISIBLE);
	}

	public void hideLoading(){
		View loading = findViewById(R.id.loading);
		if(loading != null) loading.setVisibility(View.INVISIBLE);
	}

	public int getScreenHeightDp(){
		DisplayMetrics dm = new DisplayMetrics();  
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		return (int) (dm.heightPixels/dm.density);  
	}


}
