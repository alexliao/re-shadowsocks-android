package com.biganiseed.reindeer.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.biganiseed.reindeer.Api;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;

//import com.actionbarsherlock.view.Menu;
//import com.actionbarsherlock.view.MenuItem;

//public class Update extends WithHeaderActivity {
public class AboutFragment extends BodyFragment {
//	protected ListView listChanges;
//	protected Button btnUpgrade;
//	protected Button btnUpdates;
	protected TextView txtVersionName;
//	protected TextView txtUp2Date;
//	protected TextView txtNotUp2Date;
//	protected View checking;
	protected int mCurrentVersion = 0;
//	protected int mNewVersion;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        

//        View txtContact = this.findViewById(R.id.txtContact);
//        txtContact.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Toast.makeText(getApplicationContext(), "contact", Toast.LENGTH_SHORT).show();
//			}
//		});
//		
		
		//        bind();
   
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_about_fragment, container, false);
        bind(v);
//        final Handler handler = new Handler();
//		new Thread() {
//			public void run(){
//				try {
//					final boolean ret = Api.checkVersion(a());
//					handler.post(new Runnable(){
//						public void run(){
//							checking.setVisibility(View.GONE);
//							if(ret){
//								bind(getView());
//							}else{
//								txtUp2Date.setVisibility(View.VISIBLE);
//							}
//						}
//					});
//				} catch (final Exception e) {
//					handler.post(new Runnable(){
//						public void run(){
//							checking.setVisibility(View.GONE);
//							Toast.makeText(a().getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
//						}
//					});
//				}
//			}
//		}.start();
        return v;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
        HeaderFragment.setTitle(a(), getString(R.string.title_about));
    }

    protected void bind(View v){
    	if(v == null) return;
    	
    	View txtTerms = v.findViewById(R.id.txtTerms);
    	txtTerms.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Api.terms(a());
			}
		});
    	
//        btnUpgrade = (Button) v.findViewById(R.id.btnUpgrade);
//        btnUpgrade.setOnClickListener(new OnClickListener_btnUpgrade());
//        btnUpdates = (Button) v.findViewById(R.id.btnUpdates);
//        btnUpdates.setOnClickListener(new OnClickListener_btnUpdates());
        txtVersionName = (TextView) v.findViewById(R.id.txtVersionName);
//        txtUp2Date = (TextView) v.findViewById(R.id.txtUp2Date);
//        txtNotUp2Date = (TextView) v.findViewById(R.id.txtNotUp2Date);
//        checking = v.findViewById(R.id.checking);
//        listChanges=(ListView) v.findViewById(R.id.listChanges);
//
    	PackageInfo pi;
		try {
			pi = a().getPackageManager().getPackageInfo(a().getPackageName(), PackageManager.GET_META_DATA);
	        txtVersionName.setText(String.format(getString(R.string.version_name), pi.versionName)+ (Const.IS_RESELLER ? ".rs" : "") );
	        mCurrentVersion = pi.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
//
//		boolean up2date = true;
//    	ArrayList<HashMap<String, String>> changes = new ArrayList<HashMap<String, String>>();
//        String strChanges = Tools.getPrefString(a(), "version_changes", null);
//        if(strChanges != null){
//        	JSONArray jsonChanges;
//			try {
//				jsonChanges = new JSONArray(strChanges);
//				if(jsonChanges.length() > 0){
//					mNewVersion = jsonChanges.getJSONObject(0).getInt("code");
//					if(mNewVersion > mCurrentVersion){
//						up2date = false;
//						String versionName = ""+(mNewVersion/1000.0);
//						txtNotUp2Date.setText(String.format(txtNotUp2Date.getText().toString(), versionName));
//			        	for(int i=0;i<jsonChanges.length();i++){
//			        		JSONObject jsonChange = jsonChanges.getJSONObject(i);
//			                HashMap<String, String> hashChange = new HashMap<String, String>();
//			                hashChange.put("change", jsonChange.getString("note").trim());
//			        		changes.add(hashChange);
//			        	}
//						SimpleAdapter sa = new SimpleAdapter(a(), changes, R.layout.re_change_row, new String[] { "change"}, new int[] { R.id.txtChange});
//						listChanges.setAdapter(sa);
//					}
//				}
//				if(up2date){
//					btnUpgrade.setVisibility(View.GONE);
//					btnUpdates.setVisibility(View.VISIBLE);
//					txtNotUp2Date.setVisibility(View.GONE);
//					txtUp2Date.setVisibility(View.VISIBLE);
//				}else{
//					btnUpgrade.setVisibility(View.VISIBLE);
//					btnUpdates.setVisibility(View.GONE);
//					txtNotUp2Date.setVisibility(View.VISIBLE);
//					txtUp2Date.setVisibility(View.GONE);
//				}
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//        }
    }
    
//    private class OnClickListener_btnUpgrade implements Button.OnClickListener {
//		@Override
//		public void onClick(View v) {
////			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Const.getRootHttp(getApplicationContext()) + "/downloads/ladder"+mNewVersion+".apk")));
//			Uri uri = Uri.parse(Const.getRootHttp(a()) + "/account/upgrade?" + Tools.getClientParameters(a()) );
//			startActivity(new Intent(Intent.ACTION_VIEW, uri));
//			Toast.makeText(a(), getString(R.string.downloading_prompt), Toast.LENGTH_LONG).show();
//		}
//    }
//
//    private class OnClickListener_btnUpdates implements Button.OnClickListener {
//		@Override
//		public void onClick(View v) {
//			navigate(new UpdatesFragment(), "updates");
//		}
//    }


}
