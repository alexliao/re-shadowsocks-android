package com.biganiseed.reindeer.fragment;

import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.ReindeerActivity;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.R.id;
import com.biganiseed.reindeer.R.layout;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

//public class Update extends WithHeaderActivity {
public class UpdatesFragment extends BodyFragment {
	protected WebView webview;
	protected String url;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_updates_fragment, container, false);

        View btnAbout = v.findViewById(R.id.btnAbout);
        btnAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				navigate(new AboutFragment(), "about");
			}
		});
        
        
        webview = (WebView) v.findViewById(R.id.webview);
        webview.setBackgroundColor(Color.TRANSPARENT);
        url = Const.getRootHttpNoSSL(a())+"/updates_"+(Tools.getLang().equals("zh")? "zh" : "en") + "?" + System.currentTimeMillis();
        url += "&" + Tools.getClientParameters(a());

        return v;
    }

    @Override
    public void onStart(){
    	super.onStart();
        webview.loadUrl(url);
        HeaderFragment.setTitle(a(), getString(R.string.updates));
    }

//    @Override
//    public boolean collapseHeader(){
//    	return true;
//    }
    
}
