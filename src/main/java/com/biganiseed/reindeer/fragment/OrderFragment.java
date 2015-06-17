package com.biganiseed.reindeer.fragment;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.biganiseed.reindeer.Api;
import com.biganiseed.reindeer.Const;
import com.github.shadowsocks.R;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.VpnConnector;
import com.biganiseed.reindeer.googlebilling.GoogleBilling;
import com.biganiseed.reindeer.googlebilling.util.IabHelper;
import com.biganiseed.reindeer.googlebilling.util.IabResult;
import com.biganiseed.reindeer.googlebilling.util.Inventory;
import com.biganiseed.reindeer.googlebilling.util.Purchase;
import com.biganiseed.reindeer.util.ParamRunnable;

public class OrderFragment extends BodyFragment {
	// order info
	private String orderString = null;
	private JSONObject plan = null;
	private String orderId = null;
	private String username = null;
	private String humanName = null;
	private boolean paid = false;
	private int latestDealerVersion = 0;
	
	RadioButton radioPlan;
	TextView txtPrice;
	View alipayButton;
	View friendpayButton;
	View pcpayButton;
	AlertDialog friendPayDialog;
	AlertDialog pcpayDialog;
	
	public GoogleBilling mGoogleBilling;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HeaderFragment.setTitle(a(), String.format(getString(R.string.pay_for_x), humanName));
	}

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_order_fragment, container, false);
        return v;
    }
	
    @Override
    public void onStart(){
    	super.onStart();
        HeaderFragment.setTitle(a(), getString(R.string.pay));
		try {
			orderString = this.getArguments().getString("order_string");
			getOrder();
		
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
    }

    @Override
    public void onResume(){
    	super.onResume();
        if(plan != null) bindView(getView());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // very important:
        if(mGoogleBilling != null) mGoogleBilling.dispose();
    }
    
    
    void bindView(View v){
    	if(v == null) return;
		radioPlan = (RadioButton) v.findViewById(R.id.radioPlan);
		radioPlan.setText(Tools.genPlanDesc(plan, Tools.getLang()));
		txtPrice = (TextView) v.findViewById(R.id.txtPrice);
		txtPrice.setText(Tools.genPlanPrice(plan, Tools.getLang()));

		if(paid){
			v.findViewById(R.id.txtPaid).setVisibility(View.VISIBLE);
			v.findViewById(R.id.viewPayButtons).setVisibility(View.INVISIBLE);
			
			Timer timer = new Timer();
			timer.schedule(new TimerTask(){
				@Override
				public void run() {
					finishFragment();
				}
			}, 1000);
		}else{
			v.findViewById(R.id.txtPaid).setVisibility(View.GONE);
			v.findViewById(R.id.viewPayButtons).setVisibility(View.VISIBLE);
			
			
	        Button btnPayViaGoogle = (Button) v.findViewById(R.id.btnPayViaGoogle);
	        btnPayViaGoogle.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
			        try {
			        	// lazy setup
			        	if(null == mGoogleBilling){
			        		mGoogleBilling = new GoogleBilling(a(), null, new ParamRunnable(){
			        			@Override
			        			public void run() {
			        				final Purchase purchase = (Purchase)param;
			        				String orderString = purchase.getDeveloperPayload();
			        				onSuccessWithOrderString(orderString, purchase.getOrderId(), purchase.getToken(), new Runnable(){
			        					@Override
			        					public void run() {
			        						mGoogleBilling.consumeAsync(purchase);
			        					}
			        				});
			        			}
			        		});
			        		mGoogleBilling.setup(false);
			        	}
						mGoogleBilling.launchPurchaseFlow(plan.optString("name"), Tools.genOrderString(username, orderId, plan, "google"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			
	        Button btnPayOnPhone = (Button) v.findViewById(R.id.btnPayOnPhone);
	        btnPayOnPhone.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(dealerVersion() < 0){
						confirmDownloadDealer(getString(R.string.dealer_download_prompt));
					}else if(dealerVersion() < latestDealerVersion){
						confirmDownloadDealer(getString(R.string.dealer_upgrade_prompt));
					}else{
						String url = "reindeer://reindeer.com/orders/inline/"+orderString;
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						intent.putExtra("root_ip", Const.getRootIp(a()));
			            startActivity(intent);
					}
				}
			});
			Tools.setTextEnabled(a(), btnPayOnPhone, dealerVersion() >= latestDealerVersion);

	        pcpayDialog = preparePcpayDialog();
	        pcpayButton = v.findViewById(R.id.pcpayButton);
	        pcpayButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if(pcpayDialog != null) pcpayDialog.show();
				}
			});
		
			friendPayDialog = prepareFriendPayDialog();
	        friendpayButton = v.findViewById(R.id.friendpayButton);
			friendpayButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					friendPayDialog.show();
				}
			});
		}
		
		if(humanName != null){
			HeaderFragment.setTitle(a(), String.format(getString(R.string.pay_for_x), humanName));
		}
	}
	
    void confirmDownloadDealer(String prompt){
    	Tools.confirm(a(), null, prompt).setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri uri = Uri.parse(Const.getRootHttp(a()) + "/account/download_dealer?" + Tools.getClientParameters(a()) );
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				Toast.makeText(a(), getString(R.string.downloading_prompt), Toast.LENGTH_LONG).show();
			}
		}).show();
    }
    
    int dealerVersion(){
    	int result = -1;
		PackageManager pm = getActivity().getPackageManager();
		PackageInfo pi;
		try {
			pi = pm.getPackageInfo("com.biganiseed.reindeer.dealer", 0);
			result = pi.versionCode;
		} catch (NameNotFoundException e) {
		}
    	return result;
    }
    
    private void getOrder() throws ParseException, JSONException{
    	
	    	String sign = orderString.split("--")[0];
	    	final String orderInfo = orderString.split("--")[1];
	    	if(!Tools.verify_signature(orderInfo, sign)) throw new RuntimeException(getString(R.string.err_order_invalid_sign));

	    	orderId = orderInfo.split("-")[1];
	    	String timeStr = orderId.substring(Const.ORDER_PREFIX.length(), Const.ORDER_PREFIX.length()+Const.ORDER_TIME_FORMAT.length());
			SimpleDateFormat format = new SimpleDateFormat(Const.ORDER_TIME_FORMAT);
			Date orderTime = format.parse(timeStr);
			final Date now = new Date();
	    	if (orderTime.getTime() - now.getTime() > 2*86400*1000) throw new RuntimeException(getString(R.string.err_order_expired)); // 2 days
	    	
	    	//check if the order is paid
			final Handler handler = new Handler();
//			showProgressDialog();
			ga().showLoading();
			new Thread() {
				public void run(){
					try {
						final JSONObject order = Api.getOrder(a(), orderId, orderString);
						username = Tools.urlSafeUnescape(orderInfo.split("-")[0]);
						plan = order.getJSONObject("plan");
						paid = order.optBoolean("paid");
						latestDealerVersion = order.optInt("latest_dealer_version");
						handler.post(new Runnable(){
							@Override
							public void run() {
								if(ga() != null) ga().hideLoading();
								try {
									bindView(getView());
								} catch (Exception e) {
									Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
								}
							}
						});
					} catch (final Exception e) {
						handler.post(new Runnable(){
							@Override
							public void run() {
								if(ga() != null) ga().hideLoading();
								Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
							}
						});
					} finally{
					}
				}
			}.start();
			
    }
	
	
	AlertDialog prepareFriendPayDialog(){
        LayoutInflater factory = LayoutInflater.from(a());
        final View textEntryView = factory.inflate(R.layout.re_friend_pay_dialog, null);
        String name = Tools.getPrefString(a(), "human_name", null);
        if(name != null){
        	((EditText) textEntryView.findViewById(R.id.editName)).setText(name);
        }
        AlertDialog dialog = new AlertDialog.Builder(a())
        .setTitle(R.string.friend_pay)
        .setView(textEntryView)
        .setPositiveButton(R.string.proceed, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Field field;
				try {
					field = friendPayDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                	field.setAccessible(true);
                	field.set(friendPayDialog, false);
                	String name = ((EditText) textEntryView.findViewById(R.id.editName)).getText().toString();
                	if(name.trim().equals("")){
                		Toast.makeText(a(), getString(R.string.err_order_empty_name), Toast.LENGTH_SHORT).show();
                	}else{
	                	field.set(friendPayDialog, true);
	                	String url = "http://"+Const.getRootIp(a())+"/orders/inline/"+Tools.genOrderString(username, orderId, plan, "friend")+"?name="+URLEncoder.encode(name);
	        	        Intent intent = new Intent(Intent.ACTION_SEND);
	        	        intent.setType("text/plain");
	        	        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.friend_pay_request) + " " + url);
	        	        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.friend_pay_request));
	        	    	Intent i = Intent.createChooser(intent, getString(R.string.friend_pay_send_via));
	        	    	startActivity(i);
	        	    	
	        	    	Tools.setPrefString(a(), "human_name", name);
                	}
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	Field field;
				try {
					field = friendPayDialog.getClass().getSuperclass().getDeclaredField("mShowing");
                	field.setAccessible(true);
                	field.set(friendPayDialog, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        })
        .create();
        return dialog;
	}
	
	AlertDialog preparePcpayDialog(){
		try {
	    	final String url = "http://"+Const.getRootIp(a())+"/orders/inline/"+Tools.genOrderString(username, orderId, plan, "pc");
	        LayoutInflater factory = LayoutInflater.from(a());
	        final View textEntryView = factory.inflate(R.layout.re_pc_pay_dialog, null);
	       	((EditText) textEntryView.findViewById(R.id.editName)).setText(url);
	        AlertDialog dialog = new AlertDialog.Builder(a())
	        .setTitle(R.string.pc_pay)
	        .setView(textEntryView)
	        .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	Field field;
					try {
						field = pcpayDialog.getClass().getSuperclass().getDeclaredField("mShowing");
	                	field.setAccessible(true);
	                	field.set(pcpayDialog, true);
	    				Uri uri = Uri.parse("mailto:");  
	    				Intent intent = new Intent(Intent.ACTION_SENDTO, uri);  
	        	        intent.putExtra(Intent.EXTRA_TEXT, url);
	        	        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.send_payment_subject));
	    				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    				startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
					}
	            }
	        })
	        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int whichButton) {
	            	Field field;
					try {
						field = pcpayDialog.getClass().getSuperclass().getDeclaredField("mShowing");
	                	field.setAccessible(true);
	                	field.set(pcpayDialog, true);
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        })
	        .create();
	        return dialog;
		} catch (Exception e1) {
			Toast.makeText(a(), e1.getMessage(), Toast.LENGTH_LONG).show();
			return null;
		}
	}


	private void onSuccessWithOrderString(final String orderString, final String extOrderId, final String token, final Runnable onSuccess){
		ga().showProgressDialog();
		ga().ayncRun(new Runnable(){
			@Override public void run() {
				try {
					final JSONObject user = Api.ensurePayment(a(), orderString, extOrderId, token).getJSONObject("user");
					if(username.equals(Tools.getCurrentUsername(a()))){
						Tools.setCurrentUser(a(), user);
					}else{
						throw new RuntimeException("Ensure payment failed.");
					}
				} catch (Exception e) {	throw new RuntimeException(e);	}
			}
		}, new Runnable(){@Override	public void run() {
			ga().hideProgressDialog();
			try {
				if(onSuccess != null) onSuccess.run();
				Tools.alert(a(), null, getString(R.string.paid_success))
				.setPositiveButton(android.R.string.ok,  new DialogInterface.OnClickListener(){
			      public void onClick(DialogInterface dialog, int which){
			    	  finishFragment();
			      }       
		      }).show();
			} catch (Exception e) {	throw new RuntimeException(e);	}
		}});		
	}

}
