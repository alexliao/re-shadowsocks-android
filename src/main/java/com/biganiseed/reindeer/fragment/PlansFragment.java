package com.biganiseed.reindeer.fragment;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuItem;
import com.biganiseed.reindeer.Api;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.Signin;
import com.biganiseed.reindeer.Signup;
import com.biganiseed.reindeer.Tools;

public class PlansFragment extends BodyFragment {
	static public int REQUEST_CODE = 100;
	
	TextView txtMessage;
	Button btnPay;
	RadioButton radioPlan1, radioPlan2, radioPlan3, radioPlan4;
	TextView txtPrice1, txtPrice2, txtPrice3, txtPrice4;
	ArrayList<RadioButton> radioButtons= new ArrayList<RadioButton>();
	public int currentPlanIndex = 1;
	protected JSONArray plans = null;
	String message = null;
	int trialTime = 0;
	protected String fullVersionUrl;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ga().ayncRun(new Runnable(){
			@Override public void run() {
				try {
					JSONObject data;
					data = Api.getPlans(a());
					plans = data.getJSONArray("plans");
					message = data.getString("message");
					trialTime = data.getInt("trial_time");
					fullVersionUrl = data.getString("full_version_url");
					currentPlanIndex = data.getInt("default_plan") - 1;
				} catch (Exception e) {	throw new RuntimeException(e);	}
			}
		}, new Runnable(){@Override	public void run() {	bindView(getView());	}});		
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_plans_fragment, container, false);
        return v;
    }

    @Override
    public void onResume(){
    	super.onResume();
        HeaderFragment.setTitle(a(), getString(R.string.buy));
        initView(getView());
    }
	
    void initView(View v){
    	if(v == null) return;
    	txtPrice1 = (TextView) v.findViewById(R.id.txtPrice1);
    	txtPrice2 = (TextView) v.findViewById(R.id.txtPrice2);
    	txtPrice3 = (TextView) v.findViewById(R.id.txtPrice3);
    	txtPrice4 = (TextView) v.findViewById(R.id.txtPrice4);
		radioPlan1 = (RadioButton) v.findViewById(R.id.radioPlan1);
		radioPlan2 = (RadioButton) v.findViewById(R.id.radioPlan2);
		radioPlan3 = (RadioButton) v.findViewById(R.id.radioPlan3);
		radioPlan4 = (RadioButton) v.findViewById(R.id.radioPlan4);
		radioPlan1.setOnClickListener(new OnPlanClickListener());
		radioPlan2.setOnClickListener(new OnPlanClickListener());
		radioPlan3.setOnClickListener(new OnPlanClickListener());
		radioPlan4.setOnClickListener(new OnPlanClickListener());
		radioButtons.add(radioPlan1);
		radioButtons.add(radioPlan2);
		radioButtons.add(radioPlan3);
		radioButtons.add(radioPlan4);

		btnPay = (Button) v.findViewById(R.id.btnPay);
		btnPay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(Tools.getBindingUsername(a()) == null){
					Tools.confirm(a(), null, getString(R.string.pay_no_signin_prompt))
					.setPositiveButton(getString(R.string.signup), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(a(), Signup.class));
						}
					})
					.setNegativeButton(getString(R.string.signin), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							startActivity(new Intent(a(), Signin.class));
						}
					})
					.show();
				}else{
					try {
	//					url = "reindeer://reindeer.com/orders/inline/"+Tools.genOrderString(Tools.getCurrentUsername(a()), genOrderId(), plans.optJSONObject(currentPlanIndex), "");
	//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
	//		            startActivity(intent);

						finishFragment();
						Bundle bundle = new Bundle();
						bundle.putString("order_string", Tools.genOrderString(Tools.getCurrentUsername(a()), genOrderId(), plans.optJSONObject(currentPlanIndex), ""));
						Fragment fragment = new OrderFragment();
						fragment.setArguments(bundle);
						navigate(fragment, "order");
						
						
					} catch (Exception e) {
						Toast.makeText(a(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		if(plans != null) bindView(v);
    }

    protected void bindView(View v){
		if(v == null) return;
		txtMessage = (TextView) v.findViewById(R.id.txtMessage);
		txtMessage.setText(message);
//        HeaderFragment.setTitle(a(), message);
		
		String lang = Tools.getLang();
		String desc;
		JSONObject plan;
		
		plan = plans.optJSONObject(0);
		if(plan != null){
			radioPlan1.setText(Tools.genPlanDesc(plan, lang));
			txtPrice1.setText(Tools.genPlanPrice(plan, lang));
		}
		else{
			radioPlan1.setVisibility(View.GONE);
			txtPrice1.setVisibility(View.GONE);
		}
		
		plan = plans.optJSONObject(1);
		if(plan != null){
			radioPlan2.setText(Tools.genPlanDesc(plan, lang));
			txtPrice2.setText(Tools.genPlanPrice(plan, lang));
		}
		else{ 
			radioPlan2.setVisibility(View.GONE);
			txtPrice2.setVisibility(View.GONE);
		}
		
		plan = plans.optJSONObject(2);
		if(plan != null){
			radioPlan3.setText(Tools.genPlanDesc(plan, lang));
			txtPrice3.setText(Tools.genPlanPrice(plan, lang));
		}
		else{
			radioPlan3.setVisibility(View.GONE);
			txtPrice3.setVisibility(View.GONE);
		}
		
		plan = plans.optJSONObject(3);
		if(plan != null){
			radioPlan4.setText(Tools.genPlanDesc(plan, lang));
			txtPrice4.setText(Tools.genPlanPrice(plan, lang));
		}
		else{
			radioPlan4.setVisibility(View.GONE);
			txtPrice4.setVisibility(View.GONE);
		}
		
		radioButtons.get(currentPlanIndex).setChecked(true);

//		TextView txtFullVersion = (TextView) v.findViewById(R.id.txtFullVersion);
//		txtFullVersion.setText(String.format(getString(R.string.full_version_x), fullVersionUrl));

		btnPay.setEnabled(true);
		Tools.setTextEnabled(a(), btnPay, (Tools.getBindingUsername(a()) != null));
	
	}
	
	protected String genOrderId(){
		SimpleDateFormat format = new SimpleDateFormat(Const.ORDER_TIME_FORMAT);
		Date date = new Date();
		String strKey = format.format(date);

		java.util.Random r = new java.util.Random();
		strKey = strKey + Math.abs(r.nextInt());
		strKey = Const.ORDER_PREFIX + strKey.substring(0, Const.ORDER_TIME_FORMAT.length()+1); // keep 1 byte random number
		return strKey;
	}
	
	class OnPlanClickListener implements RadioButton.OnClickListener {
		@Override
		public void onClick(View v) {
			RadioButton current = (RadioButton)v;
			radioPlan1.setChecked(false);
			radioPlan2.setChecked(false);
			radioPlan3.setChecked(false);
			radioPlan4.setChecked(false);
			current.setChecked(true);
			currentPlanIndex  = Integer.parseInt((String)current.getTag())-1;
		}
	}

//    @Override
//    public boolean collapseHeader(){
//    	return true;
//    }

}
