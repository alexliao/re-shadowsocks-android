package com.biganiseed.reindeer.fragment;

import java.util.Date;

import org.json.JSONObject;

import com.biganiseed.reindeer.Api;
import com.biganiseed.reindeer.Binding;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.EditPassword;
import com.github.shadowsocks.R;
import com.biganiseed.reindeer.Signin;
import com.biganiseed.reindeer.Signup;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.googlebilling.GoogleBilling;
import com.biganiseed.reindeer.googlebilling.util.Purchase;
import com.biganiseed.reindeer.util.ParamRunnable;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AccountFragment extends BodyFragment {
	GoogleBilling mGoogleBilling;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // check if we have purchased item not consumed, and consume it.
        try{
    		mGoogleBilling = new GoogleBilling(a(), new ParamRunnable(){
    			@Override
    			public void run() {
    				final Purchase purchase = (Purchase)param;
    				String orderString = purchase.getDeveloperPayload();
    				param = false;
    				JSONObject user;
    				try {
    					user = Api.ensurePayment(a(), orderString, purchase.getOrderId(), purchase.getToken()).getJSONObject("user");
    					if(user.optString("username").equals(Tools.getCurrentUsername(a()))){
    						Tools.setCurrentUser(a(), user);
    						param = true;
    					}
    				} catch (Exception e) {
    					e.printStackTrace();
    				}
    				
    			}
    		}, null);
    		mGoogleBilling.setup(true);
        }catch(Exception e){
        	e.printStackTrace();
        }
        
    }
    
    @Override
    public void onStart(){
    	super.onStart();
        HeaderFragment.setTitle(a(), getString(R.string.menu_account));
		ga().ayncRun(new Runnable(){
			@Override public void run() {
				try {
					JSONObject user = Api.ensureUser(a());
					Tools.setCurrentUser(a(), user);
				} catch (Exception e) {	throw new RuntimeException(e);	}
			}
		}, new Runnable(){@Override	public void run() {	bind(getView());}});
    }

    @Override
    public void onResume(){
    	super.onResume();
        bind(getView());
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_account_fragment, container, false);
        return v;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        // very important:
        try{
        	mGoogleBilling.dispose();
        }catch(Exception e){
        	e.printStackTrace();
        }
    }

    void bind(View v){
    	if(v == null) return;
    	TextView txtNoBinding = (TextView) v.findViewById(R.id.txtNoBinding);
    	TextView txtBindingEmail = (TextView) v.findViewById(R.id.txtBindingEmail);
    	TextView txtType = (TextView) v.findViewById(R.id.txtType);
    	TextView txtExpires = (TextView) v.findViewById(R.id.txtExpires);
    	View btnSignin = v.findViewById(R.id.btnSignin);
    	View btnSignup = v.findViewById(R.id.btnSignup);
    	View btnSignout = v.findViewById(R.id.btnSignout);
    	View btnBuy = v.findViewById(R.id.btnBuy);
    	
    	final JSONObject user = Tools.getCurrentUser(a());
    	
    	btnBuy.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
//				startActivity(new Intent(a(), PlansActivity.class));
				navigate(new PlansFragment(), "plans");
			}
		});

    	final String bindingUsername = Tools.getBindingUsername(a());
    	if(bindingUsername == null){
//    		txtNoBinding.setVisibility(View.VISIBLE);
    		txtBindingEmail.setVisibility(View.GONE);
    		btnSignin.setVisibility(View.VISIBLE);
    		btnSignup.setVisibility(View.VISIBLE);
    		btnSignout.setVisibility(View.GONE);
    		txtExpires.setCompoundDrawables(null, null, null, null);
    		
    		btnSignin.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					startActivity(new Intent(a(), Signin.class));
//					Tools.setBindingEmail(getApplicationContext(), "alex197445@gmail.com");
//					bind();
				}
			});
    		btnSignup.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					startActivity(new Intent(a(), Signup.class));
//					Tools.setBindingEmail(getApplicationContext(), "alex197445@gmail.com");
//					bind();
				}
			});
    	}else{
//    		txtNoBinding.setVisibility(View.GONE);
    		txtBindingEmail.setVisibility(View.VISIBLE);
    		btnSignin.setVisibility(View.GONE);
    		btnSignup.setVisibility(View.GONE);
    		btnSignout.setVisibility(View.VISIBLE);
    		
    		txtBindingEmail.setText(String.format(getString(R.string.account_binding_email_x), bindingUsername));
    		txtBindingEmail.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
					startActivity(new Intent(a(), EditPassword.class));
				}
			});

    		Drawable d = this.getResources().getDrawable(R.drawable.re_email);
        	d.setBounds(0, 0, d.getMinimumWidth(), d.getMinimumHeight());
    		txtExpires.setCompoundDrawables(null, null, d, null);
    		

    		btnSignout.setOnClickListener(new View.OnClickListener(){
				@Override
				public void onClick(View v) {
//					Tools.setBindingEmail(getApplicationContext(), null);
					ga().showProgressDialog();
					final Handler handler = new Handler();
					new Thread() {
						public void run(){
							try {
								JSONObject user = Api.signout(a(), Tools.getBindingUsername(a()));
								Tools.setCurrentUser(a(), user);
								handler.post(new Runnable(){
									@Override
									public void run() {
//										Toast.makeText(Account.this, getString(R.string.account_transfer_back), Toast.LENGTH_LONG).show();
										bind(getView());
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
			});
    	}

    	if(Tools.isVip(user)){
        	txtType.setText(String.format(getString(R.string.account_type_x), getString(R.string.account_type_vip)));
        	txtExpires.setText(String.format(getString(R.string.account_expires_x), Tools.formatExpirationTime(a(), new Date(user.optLong("expiration")*1000))));
        	txtExpires.setVisibility(View.VISIBLE);
    	}else{
        	txtType.setText(String.format(getString(R.string.account_type_x), getString(R.string.account_type_trial)));
        	txtExpires.setVisibility(View.INVISIBLE);
    	}
    	
    	if(Const.IS_RESELLER){
    		btnSignup.setVisibility(View.GONE);
    		btnBuy.setVisibility(View.GONE);
    		txtBindingEmail.setCompoundDrawables(null, null, null, null);
    		txtBindingEmail.setOnClickListener(null);
    	}
    	
    }

//	public void getPPTPSettings(final String flag){
//		ga().showProgressDialog();
//		final Handler handler = new Handler();
//		new Thread() {
//			public void run(){
//				JSONObject configuration;
//				try {
//					configuration = Api.getConfiguration(a(), "pptp_configurations", flag);
//					final JSONObject data = configuration.getJSONObject("tunnel");
//					handler.post(new Runnable(){
//						@Override
//						public void run() {
//							Tools.alert(a(), getString(R.string.account_instruction), 
//									String.format(getString(R.string.account_others_prompt_x), data.optString("server"), data.optString("username"), data.optString("password")))
//									.setPositiveButton(android.R.string.ok, null).show();
//						}
//					});
//				} catch (final Exception e) {
//					handler.post(new Runnable(){
//						@Override
//						public void run() {
//							Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//						}
//					});
//				}finally{
//					handler.post(new Runnable(){
//						@Override
//						public void run() {
//							ga().hideProgressDialog();					
//						}
//					});
//				}
//			}
//		}.start();
//	}

//	@Override
//    protected int needHeightDp(){
//    	return 500;
//    }
}
