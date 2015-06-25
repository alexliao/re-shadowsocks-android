package com.biganiseed.reindeer;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

import com.biganiseed.reindeer.fragment.OrderFragment;
import com.biganiseed.reindeer.fragment.SwitcherFragment;
import com.biganiseed.reindeer.googlebilling.GoogleBilling;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

public class MainActivity extends ReindeerActivity {
	public static final int TERMS = 101;
    protected static final String TAG = "Reindeer";
//    private VpnConnector vpnConnector;
//    private TextView txtTitle;
    ViewGroup viewBody;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		MobclickAgent.openActivityDurationTrack(false);
//		MobclickAgent.setDebugMode( true );
		super.onCreate(savedInstanceState);
		setContentView(R.layout.re_main_activity);

		Tools.createTempDirectory(this);        

//		checkDns(this);

//		// hack to force overflow button shows on 4.x with hardware menu key
//		try {
//	        ViewConfiguration config = ViewConfiguration.get(this);
//	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
//	        if(menuKeyField != null) {
//	            menuKeyField.setAccessible(true);
//	            menuKeyField.setBoolean(config, false);
//	        }
//	    } catch (Exception ex) {
//	        // Ignore
//	    }		
		
		

//		if(!isShadowsocksServiceStarted()){
////			Utils.reset(getApplicationContext());
//			new Thread() {
//				public void run(){
//					ReindeerUtils.crash_recovery(Const.EXEC_PATH);
//				}
//			}.start();
//
//			if(Tools.getPrefString(getApplicationContext(), "assets_copied", "false").equals("false")){
//				ayncRun(new Runnable(){
//					@Override public void run() {
//				        ReindeerUtils.copyAssets(getApplicationContext(), Const.EXEC_PATH, ReindeerUtils.getABI());
//				        ReindeerUtils.chmodAssets(Const.EXEC_PATH);
//				        Tools.setPrefString(getApplicationContext(), "assets_copied", "true");
//					}
//				}, null);		
//			}
//		}
	
		
		if(Tools.getPrefString(this, "terms_accepted", "false").equals("false")){
			startActivityForResult(new Intent(MainActivity.this, TermsConfirm.class), TERMS);
//			Tools.confirm(MainActivity.this, null, getString(R.string.terms_confirm)).setPositiveButton(R.string.terms_yes, new DialogInterface.OnClickListener(){
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//			        Tools.setPrefString(getApplicationContext(), "terms_accepted", "true");
//				}
//			}).setNegativeButton(R.string.terms_no, new DialogInterface.OnClickListener(){
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					finish();
//				}
//			})
//			.setCancelable(false)
//			.show();
		}
	
		viewBody = (ViewGroup) findViewById(R.id.viewBody);
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		
		SwitcherFragment switcherFragment = new SwitcherFragment();
		fragmentTransaction.add(R.id.viewBody, switcherFragment, "switcher");
		fragmentTransaction.addToBackStack("switcher");
		fragmentTransaction.commit();


		UmengUpdateAgent.update(this);

//		PushAgent pushAgent = PushAgent.getInstance(this);
//		pushAgent.enable();

	}
	

//    @Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main_activity, menu);
////		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    if (item.getItemId() == android.R.id.home) {
//			//	            // app icon in action bar clicked; go home
////	            Intent intent = new Intent(this, MainActivity.class);
////	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////	            startActivity(intent);
////	        	Toast.makeText(getApplicationContext(), "home", Toast.LENGTH_SHORT).show();
//			return true;
//		} else if (item.getItemId() == R.id.menu_share) {
//			//	        	Toast.makeText(getApplicationContext(), getString(R.string.menu_share), Toast.LENGTH_SHORT).show();
//			share(getString(R.string.share_ladder_via), String.format(getString(R.string.share_ladder_desc_x), Const.PLAY_STORE_URL));
//			return true;
////		}else if (item.getItemId() == R.id.menu_plans) {
////				if(!HttpTest()) return true;
////				startActivityForResult(new Intent(this, PlansActivity.class), SwitcherFragment.CONNECT);
////				return true;
//		} else if (item.getItemId() == R.id.menu_account) {
////			startActivity(new Intent(this, Account.class));
//			return true;
//		} else if (item.getItemId() == R.id.menu_faq) {
////			startActivity(new Intent(this, Faq.class));
//			return true;
//		} else if (item.getItemId() == R.id.menu_support) {
////			//	        	Toast.makeText(getApplicationContext(), getString(R.string.menu_share), Toast.LENGTH_SHORT).show();
//////	        	share(getString(R.string.share_ladder_via), String.format(getString(R.string.share_ladder_desc_x), Const.PLAY_STORE_URL));
////			try {
////				Uri uri = Uri.parse("mailto:biganiseed@gmail.com");  
////				Intent intent = new Intent(Intent.ACTION_SENDTO, uri);  
////				intent.putExtra(android.content.Intent.EXTRA_TEXT, getSupportInfo(this));
////				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////				startActivity(intent);
////			} catch (Exception e) {
////				Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
////			}
//			return true;
//		} else if (item.getItemId() == R.id.menu_terms) {
//			startActivity(new Intent(this, Terms.class));
//			return true;
//		} else {
//			return super.onOptionsItemSelected(item);
//		}
//	}

	@Override
	protected void onActivityResult(final int requestCode,	final int resultCode, final Intent data) {
		if(requestCode == TERMS){
			if(Tools.getPrefString(this, "terms_accepted", "false").equals("false")){
				finish();
			}
		}else if(requestCode == GoogleBilling.GOOGLE_BILLING_REQUEST) {
			FragmentManager fragmentManager = getFragmentManager();
			OrderFragment fragment = (OrderFragment) fragmentManager.findFragmentByTag("order");
			if(fragment != null) fragment.mGoogleBilling.handleActivityResult(requestCode, resultCode, data);
		}else {
			FragmentManager fragmentManager = getFragmentManager();
			SwitcherFragment fragment = (SwitcherFragment) fragmentManager.findFragmentByTag("switcher");
			if(fragment != null) fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	
	
	private void showDialog(DialogFragment dialog) {
		android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.addToBackStack(null);
		dialog.show(ft, null);
	}
	

	

//	  boolean isShadowsocksServiceStarted(){
//		  return ReindeerVpnService.isServiceStarted(this);
//	  }
	
	  @Override
	  public void onBackPressed() {
	      final Fragment switcherFragment = getFragmentManager().findFragmentByTag("switcher");
	      if (switcherFragment != null && switcherFragment.isVisible()) {
	    	  finish();
	      }else{
	    	  super.onBackPressed();
	      }
	  }
}
