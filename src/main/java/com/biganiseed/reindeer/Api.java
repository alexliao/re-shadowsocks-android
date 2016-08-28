package com.biganiseed.reindeer;

import com.biganiseed.reindeer.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

public class Api {

	static public JSONObject getConfiguration(Context context, String controllerName, String flag) throws Exception {
		JSONObject ret = null;
		Log.d(Const.APP_NAME, Const.APP_NAME + " getting configuration from " + Const.getRootIp(context));
		Tools.addLog(context, "getting configuration from " + Const.getRootIp(context));
		String strResult = null;
		
		String url = Const.getRootHttp(context) + "/" + controllerName + "/" + Tools.getDeviceUUID(context) + ".json?" + Tools.getClientParameters(context);
		if(flag != null) url += "&flag=" + flag;
//		String lastNas = Tools.getPrefString(context, "last_nas", null);
//		if(lastNas != null) url += "&last_nas=" + lastNas;
		String bindingEmail = Tools.getBindingUsername(context); 
		if(bindingEmail != null) url += "&username=" + URLEncoder.encode(bindingEmail);
//		String preferNas = Tools.getCurrentNas(context);
//		if(preferNas != null) url += "&prefer_nas=" + preferNas;
		
		JSONObject nas = Tools.getCurrentNas(context);
		if(nas != null){
			url += "&preferred_region=" + nas.optString("region");
			url += "&preferred_city=" + nas.optString("city");
		}

		HttpResponse httpResp = get(url, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			strResult = Tools.xor_decrypt(strResult);
			JSONObject json = new JSONObject(strResult);
			ret = json;
			Tools.saveCommonConfiguration(context, json);
			Tools.setPrefString(context, "root_valid", "true");
		}else if(httpResp.getStatusLine().getStatusCode() == 402){
			strResult = Tools.xor_decrypt(strResult);
			JSONObject json = new JSONObject(strResult);
			Tools.saveCommonConfiguration(context, json);
			Tools.setPrefString(context, "root_valid", "true");
			throw new ExpiredException(strResult);
		}else if(httpResp.getStatusLine().getStatusCode() == 403){
			strResult = Tools.xor_decrypt(strResult);
			JSONObject json = new JSONObject(strResult);
			Tools.saveCommonConfiguration(context, json);
			Tools.setPrefString(context, "root_valid", "true");
			throw new OutOfUseException(strResult);
		}else{
//				throw new Exception(json.optString("error_message","error"));
			Tools.setPrefString(context, "root_valid", "false");
			throw new Exception(strResult);
		}
		Log.d(Const.APP_NAME, Const.APP_NAME + " got configuration.");
		Tools.addLog(context, "got configuration.");
		return ret;
	}

	static public JSONObject trialAgain(Context context, String humanizer_answer) throws Exception {
		JSONObject ret = null;
		try {
			String strResult = null;
			
			String url = Const.getRootHttp(context) + "/users/" + Tools.getDeviceUUID(context) + "/trial_again.json?" + Tools.getClientParameters(context);
			String bindingEmail = Tools.getBindingUsername(context); 
			if(bindingEmail != null) url += "&username=" + URLEncoder.encode(bindingEmail);
			
			List <NameValuePair> params = new ArrayList <NameValuePair>();
			params.add(new BasicNameValuePair("_method", "PUT"));
//			params.add(new BasicNameValuePair("trial", "true"));
			params.add(new BasicNameValuePair("humanizer_answer", humanizer_answer));
			HttpResponse httpResp = post(url, params, context);
			strResult = EntityUtils.toString(httpResp.getEntity());
			if(httpResp.getStatusLine().getStatusCode() == 200){
				JSONObject json = new JSONObject(strResult);
				ret = json;
			}else{
//				throw new Exception(json.optString("error_message","error"));
//				strResult = EntityUtils.toString(httpResp.getEntity());
				throw new Exception(strResult);
			}
			return ret;
		} catch (Exception e) {
//			if(Api.checkDns(context)){
//				return trialAgain(context);
//			}else
				throw new Exception(e.getMessage());
		}
	}

	static public JSONObject editUser(Context context, String username, String password) throws Exception {
		JSONObject ret = null;
		try {
			String strResult = null;
			
			JSONObject user = Tools.getCurrentUser(context);
			String url = Const.getRootHttp(context) + "/users/" + user.getString("id") + ".json?" + Tools.getClientParameters(context);
			
			List <NameValuePair> params = new ArrayList <NameValuePair>();
			params.add(new BasicNameValuePair("_method", "PUT"));
			params.add(new BasicNameValuePair("user[username]", username));
			params.add(new BasicNameValuePair("user[password]", password));
			HttpResponse httpResp = post(url, params, context);
			strResult = EntityUtils.toString(httpResp.getEntity());
			if(httpResp.getStatusLine().getStatusCode() == 200){
				JSONObject json = new JSONObject(strResult);
				ret = json;
			}else{
//				throw new Exception(json.optString("error_message","error"));
//				strResult = EntityUtils.toString(httpResp.getEntity());
				throw new Exception(strResult);
			}
			return ret;
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	static public JSONObject getPlans(Context context) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		String url = Const.getRootHttp(context) + "/plans.json?" + Tools.getClientParameters(context);
		HttpResponse httpResp = get(url, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
		}else{
//				throw new Exception(json.optString("error_message","error"));
			throw new Exception(strResult);
		}
		return ret;
	}
	
	// need the user exists before call this api
	static public JSONObject getUser(Context context) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		String url = Const.getRootHttp(context) + "/users/" + Tools.getCurrentUser(context).getString("id") + ".json?" + Tools.getClientParameters(context);
		HttpResponse httpResp = get(url, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
		}else{
//				throw new Exception(json.optString("error_message","error"));
			throw new Exception(strResult);
		}
		return ret;
	}

	static public JSONObject getPlan(Context context, String planName) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		String url = Const.getRootHttp(context) + "/plans/" + planName + ".json?" + Tools.getClientParameters(context);
		HttpResponse httpResp = get(url, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
		}else{
//				throw new Exception(json.optString("error_message","error"));
			throw new Exception(strResult);
		}
		return ret;
	}

	// return new order or paid order 
	static public JSONObject getOrder(Context context, String orderId, String orderString) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		String url = Const.getRootHttp(context) + "/orders/" + orderId + ".json?order=" + orderString + "&" + Tools.getClientParameters(context);
		HttpResponse httpResp = get(url, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
//		}else if(httpResp.getStatusLine().getStatusCode() == 404){
		}else{
//				throw new Exception(json.optString("error_message","error"));
			throw new Exception(strResult);
		}
		return ret;
	}

	//	static public JSONObject addPayment(Context context, double amount, String currency, int days, String planName, String gateway) throws Exception {
//	JSONObject ret = null;
//	try {
//		String strResult = null;
//		String url = Const.ROOT_HTTP + "/payments.json?" + Tools.getClientParameters(context);
//		HttpPost httpReq = new HttpPost(url);
//		HttpParams httpParameters = new BasicHttpParams();
//		HttpConnectionParams.setConnectionTimeout(httpParameters, Const.HTTP_TIMEOUT);
//		httpReq.setParams(httpParameters);
//		List <NameValuePair> params = new ArrayList <NameValuePair>();
//		params.add(new BasicNameValuePair("uuid", Tools.getDeviceUUID(context)));
//		params.add(new BasicNameValuePair("payment[amount]", ""+amount));
//		params.add(new BasicNameValuePair("payment[currency]", currency));
//		params.add(new BasicNameValuePair("payment[days]", ""+days));
//		params.add(new BasicNameValuePair("payment[plan]", planName));
//		params.add(new BasicNameValuePair("payment[gateway]", gateway));
//		httpReq.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
//		HttpResponse httpResp;
//		httpResp = new DefaultHttpClient().execute(httpReq);
//		strResult = EntityUtils.toString(httpResp.getEntity());
//		if(httpResp.getStatusLine().getStatusCode() == 201){
//			JSONObject json = new JSONObject(strResult);
//			ret = json;
//		}else{
////			throw new Exception(json.optString("error_message","error"));
////			strResult = EntityUtils.toString(httpResp.getEntity());
//			throw new Exception(strResult);
//		}
//		return ret;
//	} catch (Exception e) {
//		throw new Exception(e.getMessage());
//	}
//}
	
	
	static protected HttpResponse get(String url, Context context) throws ClientProtocolException, IOException{
		HttpGet httpReq = new HttpGet(url);
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, Const.HTTP_TIMEOUT);
		httpReq.setParams(httpParameters);
		return initHttpClient(httpParameters, context).execute(httpReq);
	}

	static protected synchronized HttpResponse post(String url, List <NameValuePair> params, Context context) throws ClientProtocolException, IOException{
		HttpPost httpReq = new HttpPost(url);
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, Const.HTTP_TIMEOUT);
		httpReq.setParams(httpParameters);
		if (params != null) httpReq.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		return initHttpClient(httpParameters, context).execute(httpReq);
	}

    private static HttpClient client = null; 
     /** 
      * 初始化HttpClient对象,信任所有https证书 
      * @param params 
      * @return 
      */ 
	public static synchronized HttpClient initHttpClient(HttpParams params, Context context) {
		if(client == null){
			try {
				InputStream in = context.getAssets().open("ca.crt");
				Certificate cer = CertificateFactory.getInstance("X.509").generateCertificate(in);
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null, null);
				trustStore.setCertificateEntry("an_alias", cer);

				SSLSocketFactory sf = new SSLSocketFactory(trustStore);
				//允许所有主机的验证
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
				// 设置http和https支持
				SchemeRegistry registry = new SchemeRegistry();
				registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
				registry.register(new Scheme("https", sf, 443));

				ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

				return new DefaultHttpClient(ccm, params);
			} catch (Exception e) {
				e.printStackTrace();
				return new DefaultHttpClient(params);
			}
		}
		return client;
	}

	static public boolean checkVersion(Context context) throws Exception{
		boolean isObsolete = false;
    	String actionURL = Const.getRootHttpNoSSL(context) + "/account/app_version.json?"+Tools.getClientParameters(context);
//	Log.d("","Bannka checkVersion: " + actionURL);	    	
		HttpGet httpReq = new HttpGet(actionURL);
//			Log.d("Bannka", "Bannka BannkaActitivy checking version ... ");
		final HttpResponse httpResp = new DefaultHttpClient().execute(httpReq);
		if(httpResp.getStatusLine().getStatusCode() == 200){
			String result = EntityUtils.toString(httpResp.getEntity());
			Tools.setPrefString(context, "version_changes", result);
			Tools.setPrefString(context, "check_version_time", ""+System.currentTimeMillis());
			JSONArray changes = new JSONArray(result);
			if(changes.length() > 0) isObsolete = true;
		}else throw new RuntimeException(context.getString(R.string.err_backend));
		return isObsolete;
	}

	static public String getRootIp(Context context) {
		String result = null;
    	String url = Tools.getDnsUrl(context);
    	String body = null;
    	Log.d(Const.APP_NAME,"getRootIp... from " + url.substring(0, 21));
    	Tools.addLog(context,"getRootIp... from " + url.substring(0, 21));
		HttpResponse httpResp;
		try {
			httpResp = get(url, context);
			body = EntityUtils.toString(httpResp.getEntity());
			if(httpResp.getStatusLine().getStatusCode() == 200){
				Pattern p = Pattern.compile("\\[dns\\](.*?)\\[dns\\]");
				Matcher m = p.matcher(body);
				if(m.find()){
					String ips = m.group(1);
					String[] ip_list = ips.split(",");
//					result = ip_list[(int) Math.round(Math.random()*ip_list.length)]; // random select an ip from the list
					for(int i=0; i<ip_list.length; i++){
						String ip = ip_list[i].trim(); // improve robust
						String history = Tools.getPrefString(context, Const.getRootIpKey(context)+"history", "");
						if(!history.contains(ip)){
							result = ip;
							break;
						}
					}
				}
				Log.d(Const.APP_NAME,"gotRootIp: "+result);	    	
		    	Tools.addLog(context, "gotRootIp: "+result);
			}else{
				Log.d(Const.APP_NAME,"getRootIp err: "+httpResp.getStatusLine().getStatusCode());	    	
		    	Tools.addLog(context, "getRootIp err: "+httpResp.getStatusLine().getStatusCode());
			}
		} catch (Exception e) {
	    	Tools.addLog(context, "getRootIp err: "+e.getMessage());
		}
		return result;
	}

	static public void stopAccounting(Context context, String acctterminatecause){
		try {
			String strResult = null;
			String url = Const.getRootHttp(context) + "/openvpn_configurations/stop_accounting";
			String username = Tools.getCurrentUser(context).getString("username");
			String acctsessionid = Tools.getPrefString(context, "acctsessionid", "");
			List <NameValuePair> params = new ArrayList <NameValuePair>();
//			params.add(new BasicNameValuePair("acctterminatecause", "User-Request"));
			params.add(new BasicNameValuePair("acctterminatecause", acctterminatecause));
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("acctsessionid", acctsessionid));
//			HttpResponse httpResp = post(url, params);
			post(url, params, context);
		} catch (Exception e) {
//			throw new Exception(e.getMessage());
			e.printStackTrace();
		}
	}

//	static public JSONObject bind(Context context, String username, String password) throws Exception {
//		JSONObject ret = null;
//		String strResult = null;
//		
//		String url = Const.getRootHttp(context) + "/account/bind.json";
////		List <NameValuePair> params = new ArrayList <NameValuePair>();
////		params.add(new BasicNameValuePair("username", username));
////		params.add(new BasicNameValuePair("password", password));
////		HttpResponse httpResp = post(url, params);
//		url += "?username=" + URLEncoder.encode(username); 
//		url += "&password=" + URLEncoder.encode(password); 
//		url += "&" + Tools.getClientParameters(context);
//		HttpResponse httpResp = post(url, null);
//		strResult = EntityUtils.toString(httpResp.getEntity());
//		if(httpResp.getStatusLine().getStatusCode() == 200){
//			JSONObject json = new JSONObject(strResult);
//			ret = json;
//		}else if(httpResp.getStatusLine().getStatusCode() == 401){
//			throw new BindFailedException(strResult);
//		}else{
//			throw new Exception(strResult);
//		}
//		return ret;
//	}
//
//	static public JSONObject unbind(Context context, String username) throws Exception {
//		JSONObject ret = null;
//		String strResult = null;
//		
//		String url = Const.getRootHttp(context) + "/account/unbind.json";
//		url += "?username=" + URLEncoder.encode(username); 
//		url += "&" + Tools.getClientParameters(context);
//		HttpResponse httpResp = post(url, null);
//		strResult = EntityUtils.toString(httpResp.getEntity());
//		if(httpResp.getStatusLine().getStatusCode() == 200){
//			JSONObject json = new JSONObject(strResult);
//			ret = json;
//		}else{
//			throw new Exception(strResult);
//		}
//		return ret;
//	}
	
	static public JSONObject signin(Context context, String username, String password) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		
		String url = Const.getRootHttp(context) + "/account/signin.json";
//		List <NameValuePair> params = new ArrayList <NameValuePair>();
//		params.add(new BasicNameValuePair("username", username));
//		params.add(new BasicNameValuePair("password", password));
//		HttpResponse httpResp = post(url, params);
		url += "?username=" + URLEncoder.encode(username); 
		url += "&password=" + URLEncoder.encode(password); 
		url += "&" + Tools.getClientParameters(context);
		HttpResponse httpResp = get(url, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
		}else if(httpResp.getStatusLine().getStatusCode() == 401){
			JSONObject json = new JSONObject(strResult);
			throw new BindFailedException(json.optString("error_message"));
		}else if(httpResp.getStatusLine().getStatusCode() == 404){
			JSONObject json = new JSONObject(strResult);
			throw new BindFailedException(json.optString("error_message"));
		}else{
			throw new Exception(strResult);
		}
		return ret;
	}

	static public JSONObject signup(Context context, String username, String password) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		
		String url = Const.getRootHttp(context) + "/account/signup.json";
		url += "?" + Tools.getClientParameters(context);
		List <NameValuePair> params = new ArrayList <NameValuePair>();
		params.add(new BasicNameValuePair("username", username));
		params.add(new BasicNameValuePair("password", password));
		HttpResponse httpResp = post(url, params, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
		}else if(httpResp.getStatusLine().getStatusCode() == 406){
			JSONObject json = new JSONObject(strResult);
			throw new BindFailedException(json.optString("error_message"));
		}else{
			throw new Exception(strResult);
		}
		return ret;
	}

	static public JSONObject signout(Context context, String username) throws Exception {
		JSONObject ret = null;
		String strResult = null;
		
		String url = Const.getRootHttp(context) + "/account/signout.json";
		url += "?username=" + URLEncoder.encode(username); 
		url += "&" + Tools.getClientParameters(context);
		HttpResponse httpResp = post(url, null, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() == 200){
			JSONObject json = new JSONObject(strResult);
			ret = json;
		}else{
			throw new Exception(strResult);
		}
		return ret;
	}

	// return if main ip changed
	static public boolean checkDns(final Context context){
		boolean changed = false;
		String ip = Api.getRootIp(context);
		String ipKey = Const.getRootIpKey(context);
		if(ip != null){
//			if(!ip.equalsIgnoreCase(Tools.getPrefString(context, ipKey, ""))){
				Log.v(Const.APP_NAME, Const.APP_NAME + " dns changed to " + ip);
				Tools.addLog(context, "dns changed to " + ip);
				Tools.setPrefString(context, ipKey, ip);
				Tools.setPrefString(context, ipKey+"history", Tools.getPrefString(context, ipKey+"history", "") + "," + ip); // save change history
				changed = true;
//			}
		}
		return changed;
	}

	static public JSONObject ensureUser(Context context) throws Exception{
		JSONObject user;
	    if(Tools.getCurrentUser(context) == null)
	    	user = Api.trialAgain(context, "");
	    else
	    	user = Api.getUser(context);
		return user;
	}

	static public void terms(final Context context){
		String url = Const.getRootHttpNoSSL(context) + "/terms_" + Tools.getZHorEN() + ".html";
		context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	// report online status and return "" tells caller to ignore, return error tells caller to stop vpn service when response is 401.
	static public String simuseControl(Context context, String server){
		String ret = "";
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Locale lc = Locale.getDefault();
			String lang = lc.getLanguage();
			String strResult = null;
			String url = "https://" + server + "/ssc/sc";
			String username = Tools.getCurrentUser(context).getString("username");
			String password = Tools.getCurrentUser(context).optString("password_encrypted");
			url += "?username=" + URLEncoder.encode(username); 
			url += "&password_encrypted=" + URLEncoder.encode(password); 
//			url += "&uuid=" + (Tools.getDeviceUUID(context)); 
//			url += "&model=" + URLEncoder.encode(Build.MODEL);
//			url += "&nas=" + URLEncoder.encode(Tools.getPrefString(context, "last_nas", ""));
			url += "&nas_ip=" + URLEncoder.encode(Tools.getCurrentNas(context).optString("ip"));
			url += "&nas_city=" + URLEncoder.encode(Tools.getCurrentNas(context).optString("city"));
//			url += "&lang=" + lang;
			url += "&" + Tools.getClientParameters(context);
			HttpResponse httpResp = get(url, context);
			if(httpResp.getStatusLine().getStatusCode() == 401){
				ret = EntityUtils.toString(httpResp.getEntity()).trim();
			}
		} catch (Exception e) {
//			throw new Exception(e.getMessage());
			e.printStackTrace();
		}
		return ret;
	}

	static public void reportSpeed(Context context, String uuid, String region, String city, String hosting, String nas_ip, String client_ip, String client_country, String network_type, long speed, long elapsed) throws Exception {
		String url = Const.getRootHttp(context) + "/speeds.json";
		url += "?" + Tools.getClientParameters(context);
		List <NameValuePair> params = new ArrayList <NameValuePair>();
		params.add(new BasicNameValuePair("speed[uuid]", uuid));
		params.add(new BasicNameValuePair("speed[region]", region));
		params.add(new BasicNameValuePair("speed[city]", city));
		params.add(new BasicNameValuePair("speed[city]", city));
		params.add(new BasicNameValuePair("speed[hosting]", hosting));
		params.add(new BasicNameValuePair("speed[nas_ip]", nas_ip));
		params.add(new BasicNameValuePair("speed[client_ip]", client_ip));
		params.add(new BasicNameValuePair("speed[client_country]", client_country));
		params.add(new BasicNameValuePair("speed[network_type]", network_type));
		params.add(new BasicNameValuePair("speed[speed]", ""+speed));
		params.add(new BasicNameValuePair("speed[elapsed]", ""+elapsed));
		HttpResponse httpResp = post(url, params, context);
//		strResult = EntityUtils.toString(httpResp.getEntity());
//		if(httpResp.getStatusLine().getStatusCode() == 200){
//			JSONObject json = new JSONObject(strResult);
//			ret = json;
//		}
//		return ret;
	}

	static public JSONObject ensurePayment(Context context, String orderString, String extOrderId, String token) throws Exception {
		JSONObject ret = null;
		String strResult = null;
//		String url = Const.getRootHttp(context) + "/payments/pay_order/" + orderString + "?order_memo=client_ensure_payment&" + Tools.getClientParameters(context);
		String url = Const.getRootHttp(context) + "/payments/pay_order/" + orderString + "?ext_order_id=" + extOrderId + "&token=" + token + "&" + Tools.getClientParameters(context);
		HttpResponse httpResp = post(url, null, context);
		strResult = EntityUtils.toString(httpResp.getEntity());
		if(httpResp.getStatusLine().getStatusCode() < 300){
			JSONObject json = new JSONObject(strResult);
			ret = json;
			return ret;
		}else{
//					throw new Exception(json.optString("error_message","error"));
			throw new Exception(strResult);
		}
	}

}
