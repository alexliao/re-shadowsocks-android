package com.biganiseed.reindeer.fragment;

import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.ReindeerActivity;
import com.biganiseed.reindeer.ReindeerVpnService;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.R.id;
import com.biganiseed.reindeer.R.layout;
import com.github.shadowsocks.ReindeerUtils;
import com.github.shadowsocks.utils.*;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.Toast;

//public class Update extends WithHeaderActivity {
public class HelpFragment extends BodyFragment {
	protected WebView webview;
	protected String url;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_help_fragment, container, false);
        webview = (WebView) v.findViewById(R.id.webview);
        webview.setBackgroundColor(Color.TRANSPARENT);
        url = Const.getRootHttpNoSSL(a())+"/faq_"+(Tools.getLang().equals("zh")? "zh" : "en") + "?" + System.currentTimeMillis();
        url += "&" + Tools.getClientParameters(a());

        View btnFlushDns = v.findViewById(R.id.btnFlushDns);
        btnFlushDns.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!ReindeerVpnService.isServiceStarted(a())){
					Toast.makeText(a(), getString(R.string.flush_dns_must_connect), Toast.LENGTH_LONG).show();
					return;
				}
		    	Tools.confirm(a(), null, getString(R.string.flush_dns_prompt)).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ga().showProgressDialog();
						final Handler handler = new Handler();
						new Thread() {
							public void run(){
								boolean result = Tools.toggleAirplaneMode(a());
								handler.post(new Runnable(){
									@Override
									public void run() {
										ga().hideProgressDialog();
									}
								});
								if(false == result){
									handler.post(new Runnable(){
										@Override
										public void run() {
											Toast.makeText(a(), getString(R.string.flush_dns_failed), Toast.LENGTH_LONG).show();
										}
									});
								}
							}
						}.start();
					}
				}).show();
			}
		});

        View btnSupport = v.findViewById(R.id.btnSupport);
        btnSupport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Uri uri = Uri.parse("mailto:vpnreindeer@gmail.com");  
					Intent intent = new Intent(Intent.ACTION_SENDTO, uri);  
					intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.support_subject));
					intent.putExtra(android.content.Intent.EXTRA_TEXT, Tools.getSupportInfo(a()));
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				} catch (Exception e) {
					Toast.makeText(a(), getString(R.string.err_no_email_client), Toast.LENGTH_LONG).show();
				}
			}
		});

        final CheckBox chkBypass = (CheckBox) v.findViewById(R.id.chkBypass);
        chkBypass.setChecked(Tools.getPrefBoolean(a(), Key.isGFWList(), Const.DEFAULT_SMART_ROUTE));
        chkBypass.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Tools.setPrefBoolean(a(), Key.isGFWList(), chkBypass.isChecked());
				a().sendBroadcast(new Intent(Action.CLOSE()));
			}
		});
        
        return v;
    }

    @Override
    public void onStart(){
    	super.onStart();
        webview.loadUrl(url);
        HeaderFragment.setTitle(a(), getString(R.string.menu_help));
    }

//    @Override
//    public boolean collapseHeader(){
//    	return true;
//    }
    
}
