package com.biganiseed.reindeer.fragment;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import com.biganiseed.reindeer.Api;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.OnStateChanged;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.ReindeerSwitcher;
import com.biganiseed.reindeer.ShadowsocksConnector;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.VpnConnector;
import com.biganiseed.reindeer.VpnConnector.State;
import com.biganiseed.reindeer.util.AsyncImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.widget.CheckBox;
import com.github.shadowsocks.utils.*;

public class SwitcherFragment extends BodyFragment {
	public static final int CONNECT = 100;
    private ReindeerSwitcher connectToggleButton;
    private VpnConnector vpnConnector;
    private View twitter, facebook, youtube, more, webIcons;
    TextView txtAccount;
    long stateUpdatedAt = 0;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Runnable onExpired = new Runnable(){
			@Override
			public void run() {
				Tools.confirm(getActivity(), null, getString(R.string.expiration_prompt)).setPositiveButton(R.string.buy, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
//			            Intent intent = new Intent(getActivity(), PlansActivity.class);
//			            getActivity().startActivityForResult(intent, CONNECT);
						navigate(new PlansFragment(), "plans");
					}
				}).setNegativeButton(R.string.another_trial, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						trialAgain();
					}
				}).show();
			}
		};
		
		Runnable onOutOfUse = new Runnable(){
			@Override
			public void run() {
				Tools.alert(getActivity(), null, getString(R.string.outofuse_prompt)).setPositiveButton(android.R.string.ok, null).show();
			}
		};
	
		vpnConnector = new ShadowsocksConnector(ga(), onExpired, onOutOfUse);
		vpnConnector.init();

		vpnConnector.registerReceiver(new OnStateChanged() {
			@Override
			public void run(State aState) {
				stateChanged(aState);		
			}
		});
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_switcher_fragment, container, false);
        bind(v);

		// ga().ayncRun(new Runnable(){
		// 	@Override public void run() {
		// 		try {
		// 			// if(Tools.getPrefString(a(), "root_valid", "false").equalsIgnoreCase("false")
		// 			// 	||	Tools.getPrefString(a(), Const.getRootIpKey(a()), null) == null) 
		// 			// 	Api.checkDns(a());
		// 			Api.checkDns(a()); // refresh root ip each time open the app;

		// 			Tools.setPrefString(a(), "root_valid", "false");
		// 			JSONObject user = Api.ensureUser(a());
		// 			Tools.setCurrentUser(a(), user);
		// 			Tools.setPrefString(a(), "root_valid", "true");
		// 		} catch (Exception e) {	/*throw new RuntimeException(e);*/	}
		// 	}
		// }, new Runnable(){@Override	public void run() {	initShortcuts(); }});
		initShortcuts();

		return v;
    }
    
    void bind(View v){
    	if(v == null) return;

        final CheckBox chkBypass = (CheckBox) v.findViewById(R.id.chkBypass);
        chkBypass.setChecked(Tools.getPrefBoolean(a(), Key.isGFWList(), Const.DEFAULT_SMART_ROUTE));
        chkBypass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(chkBypass.isChecked())
					Toast.makeText(a(), getString(R.string.bypass_chn_on), Toast.LENGTH_LONG).show();
				else
					Toast.makeText(a(), getString(R.string.bypass_chn_off), Toast.LENGTH_LONG).show();
				Tools.setPrefBoolean(a(), Key.isGFWList(), chkBypass.isChecked());
				a().sendBroadcast(new Intent(Action.CLOSE()));
			}
		});
    	
    	txtAccount = (TextView) v.findViewById(R.id.txtAccount);
    	txtAccount.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				navigate(new AccountFragment(), "account");
			}
        });
		initWebIcon(v);
		bindFlag(v);
        connectToggleButton = (ReindeerSwitcher) v.findViewById(R.id.switcher);
        connectToggleButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), ""+connectToggleButton.isChecked(), Toast.LENGTH_SHORT).show();
				if(!Tools.HttpTest(getActivity())) return;
				if(connectToggleButton.isChecked()){
					vpnConnector.disconnect();
					new Thread() {
						public void run(){
//							Api.stopAccounting(getActivity(), "User-Request");
						}
					}.start();
				}else{
					Tools.setPrefString(a(), Const.getRootIpKey(a())+"history", "");
					Tools.clearLog(a());
					if(Long.parseLong(Tools.getPrefString(a(), "speedtested_at", "0")) == 0){
						Tools.confirm(a(), null, getString(R.string.test_prompt)).setPositiveButton(R.string.test_now, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								navigate(new NasFragment(), "nas");
							}
						}).setNegativeButton(R.string.test_later, new DialogInterface.OnClickListener(){
							@Override
							public void onClick(DialogInterface dialog, int which) {
								vpnConnector.connect();
							}
						})
						.setCancelable(false)
						.show();
					}else{
						vpnConnector.connect();
					}
				}
			}
        });
        
        
    }
    
    void bindFlag(View v){
    	if(v == null) return;
    	
		ImageView btnRoutes = (ImageView) v.findViewById(R.id.btnRoutes);
		btnRoutes.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				navigate(new NasFragment(), "nas");
			}
        });
		JSONObject currentNas = Tools.getCurrentNas(a());
		if(currentNas != null){
			new AsyncImageLoader(a(), btnRoutes, 0).loadUrl(currentNas.optString("flag"));
		}
    }
    
	@Override
	public void onStart(){
		super.onStart();
//		if(Tools.getBindingEmail(this) != null){
//			Drawable d = this.getResources().getDrawable(R.drawable.email);
//	    	d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
//	    	txtAccountTime.setCompoundDrawables(null, null, d, null);
//		}else{
//			txtAccountTime.setCompoundDrawables(null, null, null, null);
//		}
		HeaderFragment.setTitle(getActivity(), getString(R.string.slogon));
		vpnConnector.onStart();
	}

	@Override
	public void onStop() {
		vpnConnector.onStop();
		super.onStop();
	}

	@Override
	public void onDestroy() {
        //Log.d(TAG, "VpnSettings onDestroy"); //$NON-NLS-1$
    	vpnConnector.unregisterReceiver();
        vpnConnector.uninit();
        super.onDestroy();
    }

	private void initWebIcon(View v){
        twitter = v.findViewById(R.id.twitter);
        Tools.setTouchAnim(getActivity(), twitter);
        twitter.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!openApp("com.twitter.android"))
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		});
        
        facebook = v.findViewById(R.id.facebook);
        Tools.setTouchAnim(getActivity(), facebook);
        facebook.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!openApp("com.facebook.katana"))
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		});
        
        youtube = v.findViewById(R.id.youtube);
        Tools.setTouchAnim(getActivity(), youtube);
        youtube.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!openApp("com.google.android.youtube"))
					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}
		});
        
        more = v.findViewById(R.id.more);
        Tools.setTouchAnim(getActivity(), more);
        more.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				Tools.confirm(getActivity(), null, getString(R.string.more_prompt)).setPositiveButton(R.string.exit_ladder, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) {
						getActivity().finish();
					}
				}).show();
			}
		});
		
        webIcons = v.findViewById(R.id.webIcons);
        
        setWebIconEnabled(false);
    }
    
    private void setWebIconEnabled(boolean enabled){
		twitter.setEnabled(enabled);
		facebook.setEnabled(enabled);
		youtube.setEnabled(enabled);
		more.setEnabled(enabled);
		if(enabled) setWebIconAlpha(true);
    }
    
    private void setWebIconAlpha(boolean enabled){
		Tools.setAlpha(webIcons, enabled ? 1f : 0f);
    }
    
    private void animateWebIcon(final boolean enabled){
//		webIcons.setAlpha(value ? 1f : 0.1f);
		boolean oldEnabled = connectToggleButton.isChecked();
		if(oldEnabled == enabled) return;
		
////		if(enabled){
//			Tools.flipViewOut(twitter, new Runnable(){
//				public void run() {
//					Tools.flipViewIn(twitter, null);
//				}
//			});
//			Tools.flipViewOut(facebook, new Runnable(){
//				public void run() {
//					Tools.flipViewIn(facebook, null);
//				}
//			});
//			Tools.flipViewOut(youtube, new Runnable(){
//				public void run() {
//					Tools.flipViewIn(youtube, null);
//				}
//			});
//			Tools.flipViewOut(more, new Runnable(){
//				public void run() {
//					setWebIconAlpha(enabled);
//					Tools.flipViewIn(more, null);
//				}
//			});
////		}
    	if(enabled){
    		Tools.flipViewIn(twitter, null);
    		Tools.flipViewIn(facebook, null);
    		Tools.flipViewIn(youtube, null);
    		Tools.flipViewIn(more, null);
    	}else{
    		Tools.flipViewOut(twitter, null);
    		Tools.flipViewOut(facebook, null);
    		Tools.flipViewOut(youtube, null);
			Tools.flipViewOut(more, null);
    	}
    }

    Timer expirationPromptTimer;
	@Override
	public void onResume() {
		super.onResume();
        expirationPromptTimer = new Timer();
		expirationPromptTimer.schedule(new TimerTask(){
			public void run() {
				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run() {
						try{
							refreshExpriationTime();
							if(System.currentTimeMillis() - stateUpdatedAt > 1000) vpnConnector.broadcastStatus();
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				});
			}
		}, new Date(), 1000);
	}

	@Override
	public void onPause() {
		expirationPromptTimer.cancel();
		super.onPause();
	}

	private void refreshExpriationTime(){
		JSONObject user = Tools.getCurrentUser(getActivity());
		if(user == null){
//			txtAccountTime.setText(getString(R.string.slogon));
//			txtSlogon.setVisibility(View.VISIBLE);
		}else{
//			txtAccountTime.setVisibility(View.VISIBLE);
//			txtSlogon.setVisibility(View.GONE);
			if(user.optBoolean("disconnect_when_expire", true) || Tools.isVip(user)){
				txtAccount.setVisibility(View.VISIBLE);
				Date expireTime =  new Date(user.optLong("expiration")*1000);
				String timeStr;
				if((new Date()).getTime() > expireTime.getTime())
	//				timeStr = getString(R.string.expired);
					timeStr = "";
				else
					timeStr = Tools.formatExpirationTime(a(), expireTime);
				if(Tools.isVip(user))
					txtAccount.setText(String.format(getString(R.string.vip_expire_at_x), timeStr));
				else
					txtAccount.setText(String.format(getString(R.string.trial_expire_at_x), timeStr));
			}else{
				txtAccount.setVisibility(View.GONE);
			}
		}
	}

    private void stateChanged(final VpnConnector.State state) {
    	if(connectToggleButton == null) return;
      Log.d(Const.APP_NAME, "stateChanged, state: " + state); 
      switch(state){
      case LOADING:
      	connectToggleButton.setWorking(true);
      	setWebIconEnabled(false);
      	break;
      case ON:
      	animateWebIcon(true);
      	connectToggleButton.setWorking(false);
      	connectToggleButton.setChecked(true);
      	setWebIconEnabled(true);
      	bindFlag(getView());
      	break;
      default:
      	animateWebIcon(false);
      	connectToggleButton.setWorking(false);
      	connectToggleButton.setChecked(false);
      	setWebIconEnabled(false);
      	break;
      }
      stateUpdatedAt = System.currentTimeMillis();
  }

	private boolean openApp(String packageName) {
		boolean result = false;
		PackageManager pm = getActivity().getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo(packageName, 0);
			Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
			resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			resolveIntent.setPackage(pi.packageName);
	
			List<ResolveInfo> apps = pm.queryIntentActivities(resolveIntent, 0);
	
			ResolveInfo ri = apps.iterator().next();
			if (ri != null ) {
		//		String packageName = ri.activityInfo.packageName;
				String className = ri.activityInfo.name;
		
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		
				ComponentName cn = new ComponentName(packageName, className);
		
				intent.setComponent(cn);
				startActivity(intent);
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}	


	// handle the event from parent Activity. should be called in parentActivity.onActivityResult()
	public void onActivityResult(final int requestCode,	final int resultCode, final Intent data) {
		if (requestCode == VpnConnector.REQUEST_CONNECT) {
			if (resultCode == getActivity().RESULT_OK) {
        		vpnConnector.onActivityResult(data);
			}
		}else if(requestCode == CONNECT){
			if (resultCode == getActivity().RESULT_OK) {
//        		vpnConnector.connect();
				connectToggleButton.performClick();
			}
		} else {
			throw new RuntimeException("unknown request code: " + requestCode);
		}
	}

	void trialAgain(){
		final EditText edit = new EditText(a());
new AlertDialog.Builder(a())
.setTitle(a().getString(R.string.humanizer_title))
.setMessage(Tools.getPrefString(a(), "last_humanizer_question", ""))
.setView(edit)
.setNegativeButton(a().getString(android.R.string.cancel), null)
.setPositiveButton(a().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
     public void onClick(DialogInterface dialog, int which) {
	    final String answer=edit.getText().toString();
	    if(answer.isEmpty()) return;
		final Handler handler = new Handler();
		ga().showProgressDialog();
		new Thread() {
			public void run(){
				try {
					final JSONObject user = Api.trialAgain(a(), answer);
					Tools.setCurrentUser(a(), user);
					handler.post(new Runnable(){
						@Override
						public void run() {
							connectToggleButton.click();
						}
					});
				} catch (final Exception e) {
					handler.post(new Runnable(){
						@Override
						public void run() {
							Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
						}
					});
				} finally{
					handler.post(new Runnable(){
						@Override
						public void run() {
							ga().hideProgressDialog();
						}
					});
				}
			}
		}.start();

     }
})
.show();

return;

		
	}
	
	@Override
    public boolean collapseHeader(){
    	return false;
    }

	void initShortcuts(){
		JSONObject user  = Tools.getCurrentUser(a());
		if(user == null) return;
		try {
			JSONArray shortcuts = user.optJSONArray("shortcuts");
			if(shortcuts == null) return;
			
			for(int i=0; i<shortcuts.length(); i++){
				JSONObject shortcut = shortcuts.getJSONObject(i);
				String tag = shortcut.getString("tag");
				final String icon = shortcut.getString("icon");
				final String link = shortcut.getString("link");
				
				if(Tools.imageDownloaded(a(), icon)){
					ImageButton btn = (ImageButton) findShortcutByTag(tag);
					btn.setImageBitmap(Tools.getImageFromFile(a(), icon));
					btn.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
						}
					});
				}else{
					final Handler handler = new Handler();
					new Thread(new Runnable(){
						@Override
						public void run() {
							Tools.saveImageToFile(a(), icon, Const.HTTP_TIMEOUT);
							handler.post(new Runnable(){
								@Override
								public void run() {
									initShortcuts();
								}
							});
						}
					}).start();
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private View findShortcutByTag(String tag){
		View result = null;
		if("twitter".equalsIgnoreCase(tag)) result = twitter;
		if("facebook".equalsIgnoreCase(tag)) result = facebook;
		if("youtube".equalsIgnoreCase(tag)) result = youtube;
		if("more".equalsIgnoreCase(tag)) result = more;
		return result;
	}

}
