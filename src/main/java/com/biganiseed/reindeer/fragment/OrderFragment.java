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

//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuItem;
import com.biganiseed.reindeer.Api;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
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
	
//	private TextView txtPlan;
	RadioButton radioPlan;
	TextView txtPrice;
//	PaypalButton paypalButton;
	View alipayButton;
	View friendpayButton;
	View pcpayButton;
//	Alipay alipay;
//	private View alipayButton;
	AlertDialog friendPayDialog;
	AlertDialog pcpayDialog;
	
	public GoogleBilling mGoogleBilling;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		try {
////			uuid = getIntent().getExtras().getString("uuid");
////			orderId = getIntent().getExtras().getString("order_id");
////			String s = getIntent().getExtras().getString("plan");
////			if(s != null) plan = new JSONObject(s);
////			if(plan == null) getOrderFromUrl(getIntent());
//			orderString = this.getArguments().getString("order_string");
//			getOrder();
////			if(plan != null) bindView();
//		
//		} catch (Exception e) {
//			e.printStackTrace();
//			Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
////			finish();
//		}
		
		HeaderFragment.setTitle(a(), String.format(getString(R.string.pay_for_x), humanName));

//		mHelper = new IabHelper(a(), GoogleBilling.APPLICATION_PUBLIC_KEY);
//		mHelper.enableDebugLogging(true);
//        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//            public void onIabSetupFinished(IabResult result) {
//                if (!result.isSuccess()) {
//                    // Oh noes, there was a problem.
//                    Tools.alert(a(), null, "Problem setting up in-app billing: " + result).show();
//                    return;
//                }
//                // Have we been disposed of in the meantime? If so, quit.
//                if (mHelper == null) return;
//                // IAB is fully set up. Now, let's get an inventory of stuff we own.
//                mHelper.queryInventoryAsync(mGotInventoryListener);
//            }
//        });
	}

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_order_fragment, container, false);
//        bindView(v);
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
//					navigate(new AccountFragment(), "account");
				}
			}, 1000);
		}else{
			v.findViewById(R.id.txtPaid).setVisibility(View.GONE);
			v.findViewById(R.id.viewPayButtons).setVisibility(View.VISIBLE);
			
//			paypalButton = (PaypalButton) findViewById(R.id.paypalButton);
//			if(plan.optDouble("price_usd") > 0){
//				paypalButton.setVisibility(View.VISIBLE);
//				paypalButton.setPayment(orderId, Tools.genOrderString(username, orderId, plan, "paypal"), plan.optDouble("price_usd"), plan.optString("description_en") + (humanName == null ? "" : " for " + humanName ) );
//				paypalButton.init();
//			}else paypalButton.setVisibility(View.GONE);
//	//		txtPlan = (TextView) findViewById(R.id.txtPlan);
//	//		txtPlan.setText(getIntent().getExtras().getString("desc"));
//	
//			alipayButton = findViewById(R.id.alipayButton);
//			if(plan.optDouble("price_rmb") > 0){
//				alipayButton.setVisibility(View.VISIBLE);
//				alipay = new Alipay(this);
//				alipay.setPayment(orderId, Tools.genOrderString(username, orderId, plan, "alipay"), plan.optDouble("price_rmb"),  (humanName == null ? "" : "代" + humanName ) + "充值Ladder " + plan.optString("description_zh"));
//				alipay.init(alipayButton, new Handler() {
//					public void handleMessage(Message msg) {
//							if(Alipay.RESULT_SUCCESS == msg.what) onSuccess("alipay");
//					}
//				});
//			}else alipayButton.setVisibility(View.GONE);
			
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
					// if(dealerVersion() < 0){
					// 	confirmDownloadDealer(getString(R.string.dealer_download_prompt));
					// }else if(dealerVersion() < latestDealerVersion){
					// 	confirmDownloadDealer(getString(R.string.dealer_upgrade_prompt));
					// }else{
					// 	String url = "reindeer://reindeer.com/orders/inline/"+orderString;
					// 	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					// 	intent.putExtra("root_ip", Const.getRootIp(a()));
			  //           startActivity(intent);
					// }
					String url = "http://"+Const.getRootIp(a())+"/orders/inline/"+ orderString;
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					intent.putExtra("root_ip", Const.getRootIp(a()));
		            startActivity(intent);
				}
			});
//			if(dealerVersion() < latestDealerVersion){
//	    		Drawable d = this.getResources().getDrawable(R.drawable.btn_check_buttonless_on);
//	        	d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
//	    		Drawable p = this.getResources().getDrawable(R.drawable.placeholder); // for making button text center aligned
//	        	p.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
//				btnPayOnPhone.setCompoundDrawables(d, null, p, null);
//			}
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
//			getSupportActionBar().setTitle(String.format(getString(R.string.pay_for_x), humanName));
			HeaderFragment.setTitle(a(), String.format(getString(R.string.pay_for_x), humanName));
		}
	}
	
    void confirmDownloadDealer(String prompt){
    	Tools.confirm(a(), null, prompt).setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Uri uri = Uri.parse(Const.getRootHttpNoSSL(a()) + "/account/download_dealer?" + Tools.getClientParameters(a()) );
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
//	    	List<String> params = data.getPathSegments();
	    	//String controller = params.get(0); // "orders"
    		// get parameters
    	
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
//								removeDialog(0);
//								Log.d(Const.APP_NAME, "OrderActivity removeDialog(0)");
//								if(order.optBoolean("paid")){
//									Tools.alert(OrderActivity.this, null, getString(R.string.pay_for_redundant))
//									.setPositiveButton(android.R.string.ok,  new DialogInterface.OnClickListener(){
//									    public void onClick(DialogInterface dialog, int which){
//											finish();
//									    }       
//									}).show();
//								}else{
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
//						handler.post(new Runnable(){
//							@Override
//							public void run() {
//								hideProgressDialog();
//							}
//						});
					}
				}
			}.start();
			
    }
	
//	void proceedWithOrderInfo(String orderInfo){
//		try {
//			final Date now = new Date();
//	    	uuid = orderInfo.split("-")[0];
//	    	final String planName = orderInfo.split("-")[2];
//	    	String latest_plan = Tools.getPrefString(getApplicationContext(), "latest_plan_"+planName, null);
//	    	String latest_plan_time = Tools.getPrefString(getApplicationContext(), "latest_plan_time_"+planName, null);
//	    	if(latest_plan != null && (now.getTime() - Long.parseLong(latest_plan_time)) < 10*60*1000){
//					plan = new JSONObject(latest_plan);
//					bindView();
//	    	}else{
//				ayncRun(new Runnable(){
//					@Override public void run() {
//						try {
//							plan = Api.getPlan(getApplicationContext(), planName);
//							Tools.setPrefString(getApplicationContext(), "latest_plan_"+planName, plan.toString());
//							Tools.setPrefString(getApplicationContext(), "latest_plan_time_"+planName, ""+now.getTime());
//						} catch (Exception e) {	throw new RuntimeException(e);	}
//					}
//				}, new Runnable(){@Override	public void run() {	
//					try {
//						bindView();
//					} catch (Exception e) {	throw new RuntimeException(e); }	
//				}}, false);
//	    	}
//		} catch (Exception e) {
//			Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//		}
//	}
	
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

	
//	String genDesc(JSONObject plan, String lang){
//		String result;
//		String or;
//		
//		if(lang.equalsIgnoreCase("zh")){
//			result = plan.optString("description_zh") + "\n";
//			or = "或";
//		}else{
//			result = plan.optString("description_en") + "\n";
//			or = "or";
//		}
//		
//		// add "or" if need
//		double priceUSD = plan.optDouble("price_usd", 0.0);
//		double priceRMB = plan.optDouble("price_rmb", 0.0);
//		if (priceRMB > 0) result += "￥" + Tools.formatPrice(priceRMB) + " (支付宝)";
//		if (priceRMB > 0 && priceUSD > 0) result += " " + or + " ";
//		if (priceUSD > 0) result += "$" + Tools.formatPrice(priceUSD) + " (PayPal)";
//
//		return result;
//	}
	
//	private void onSuccess(final String gateway, final boolean silence){
//		try {
//			String orderString = Tools.genOrderString(username, orderId, plan, gateway);
//			onSuccessWithOrderString(orderString, silence);
//		} catch (Exception e) {	throw new RuntimeException(e);	}
//	}

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
