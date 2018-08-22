package com.biganiseed.reindeer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Notification;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.biganiseed.reindeer.util.Base64;
import com.biganiseed.reindeer.util.DownloadImage;
import com.biganiseed.reindeer.util.GeneralHash;
import com.biganiseed.reindeer.util.Rotate3dAnimation;
import com.github.shadowsocks.utils.Key;
import com.github.shadowsocks.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.nineoldandroids.view.animation.AnimatorProxy;

import java.util.List;
import org.json.JSONArray;
import com.biganiseed.reindeer.data.App;
import android.database.sqlite.SQLiteDatabase;
import android.content.pm.PackageManager.NameNotFoundException;

import android.bluetooth.BluetoothAdapter;
import android.provider.Settings.Secure;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Tools {

    static public int getVersionCode(Context context){
		int ret = 0;
    	PackageInfo pi;
		try {
			pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			ret = pi.versionCode; 
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;	
	}

    static public String getClientParameters(Context context){
		return getClientParameters(context, null);	
	}

    static public String getClientParameters(Context context, String lang){
		String ret = "";
    	PackageInfo pi;
		try {
			pi = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			Locale lc = Locale.getDefault();
			if(lang == null) lang = lc.getLanguage();
//			ret = "client_version=" + pi.versionCode + "&lang=" + lang + "&country=" + lc.getCountry();
			ret = "o=t" 
					+ "&client_version=" + pi.versionCode 
					+ "&lang=" + lang 
					+ "&country=" + lc.getCountry() 
					+ "&sdk=" + Build.VERSION.SDK_INT 
					+ "&model="+URLEncoder.encode(Build.MODEL) 
					+ "&uuid=" + Tools.getDeviceUUID(context)
					;
            if(Tools.getCurrentUsername(context) != null)
                ret += "&username_encoded=" + URLEncoder.encode(Tools.xor_decrypt(Tools.getCurrentUsername(context)));
 			if(Tools.getCurrentUser(context) != null) ret += "&password_encrypted=" + URLEncoder.encode(Tools.getCurrentUser(context).optString("password_encrypted"));
			if(Const.IS_RESELLER) ret += "&is_reseller=true";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;	
	}
    
    static public String getLang(){
		Locale lc = Locale.getDefault();
		return lc.getLanguage();
    }
    
    static public String getZHorEN(){
    	String lang = getLang();
    	return lang.equals("zh") ? "zh" : "en";
    }

    static public String getDeviceUUID(Context context) throws UnsupportedEncodingException{
//    	String result = loadDeviceId(context);
    	String result = null;
    	if(result == null){
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
			String deviceId = tm.getDeviceId(); // need READ_PHONE_STATE access permission
			if(deviceId == null){
				WifiManager wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
				WifiInfo info = wifi.getConnectionInfo();
				deviceId = info.getMacAddress(); // need ACCESS_WIFI_STATE access permission
			}
			if(deviceId == null) deviceId = getCombinedDeviceId(context);
			if(deviceId == null) throw new RuntimeException("Can't get device ID");
// deviceId = "test1234567890b";
			result = new String(Base64.encode(deviceId.getBytes()));
			result = Tools.urlSafeEscape(result);
//			saveDeviceId(context, result);
    	}
    	return result;
    }

    static  public String getCombinedDeviceId(Context context){

        // 1. The IMEI: （仅仅只对Android手机有效）
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String m_szImei = tm.getDeviceId();

        // 2. Pseudo-Unique ID：（在任何Android手机中都有效）
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI

                Build.BOARD.length()%10 +
                Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 +
                Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 +
                Build.HOST.length()%10 +
                Build.ID.length()%10 +
                Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 +
                Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 +
                Build.TYPE.length()%10 +
                Build.USER.length()%10 ; // 13 digits


        // 3. The Android ID：通常被认为不可信，因为它有时为null。
        String m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        // 4. The WLAN MAC Address string：另一个唯一ID。但是需要为工程加入android.permission.ACCESS_WIFI_STATE 权限
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

        // 5. The BT MAC Address string: 只在有蓝牙的设备上运行。需要加入android.permission.BLUETOOTH 权限.
        BluetoothAdapter m_BluetoothAdapter = null; // Local Bluetooth adapter
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String m_szBTMAC = m_BluetoothAdapter.getAddress();


        String m_szLongID = m_szImei + m_szDevIDShort  + m_szAndroidID+ m_szWLANMAC + m_szBTMAC;
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(m_szLongID.getBytes(), 0, m_szLongID.length());

        // get md5 bytes
        byte p_md5Data[] = m.digest();

        // create a hex string
        String m_szUniqueID = new String();
        for (int i = 0; i < p_md5Data.length; i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF)
                m_szUniqueID += "0";
                // add number to string
            m_szUniqueID += Integer.toHexString(b);
        }   // hex string to uppercase
        m_szUniqueID = m_szUniqueID.toUpperCase();

        return m_szUniqueID;
    }

    public static String getCurrentUsername(Context context){
    	String result = null;
        JSONObject user = getCurrentUser(context);
        if(user != null) result = user.optString("username");
    	return result;
    }
    
	public static String getBindingUsername(Context context){
		String result = null;
//		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
//		result = pref.getString("bindingEmail", null);
		JSONObject user = getCurrentUser(context);
		if(user == null) return null;
		
		String username = user.optString("username");
        boolean isAuto = user.optBoolean("is_auto");
        String uuid = user.optString("uuid");
        try{
            if(!isAuto || !getDeviceUUID(context).equals(uuid)) result = username;
        }catch(UnsupportedEncodingException e){
			e.printStackTrace();
		}
		return result;
//		return "alex197445@gmail.com";
	}
    
//	public static void setBindingEmail(Context context, String email){
//		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
//		SharedPreferences.Editor editor = pref.edit();
//		editor.putString("bindingEmail", email);
//		editor.commit();
//	}

    static public boolean is4x(){
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}

    static public boolean is5x(){
        return Build.VERSION.SDK_INT >= 21;
	}

	public static JSONObject getCurrentUser(Context context){
		JSONObject user = null;
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		String str = pref.getString("currentUser", null);
		if(str != null){
			try {
				user = new JSONObject(str);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return user;
	}

	public static void setCurrentUser(Context context, JSONObject user){
		Log.d(Const.APP_NAME, Const.APP_NAME + " Utils.setCurrentUser");
		//adjust phone time against server time
		Date expirationLocal = new Date();
		expirationLocal.setTime((new Date()).getTime() + user.optLong("expires_after")*1000); 
		try {
			user.put("expiration", expirationLocal.getTime()/1000);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("currentUser", user.toString());
		editor.commit();
	}

	public static String loadDeviceId(Context context){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		return pref.getString("DeviceId", null);
	}

	public static void saveDeviceId(Context context, String UUID){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("DeviceId", UUID);
		editor.commit();
	}


	
	//	public static String formatExpirationTime(Date time){
//        DateFormat formatter;
//        Date now = new Date();
//        if(time.getDate() == now.getDate())
//        	//formatter = new SimpleDateFormat(todayFormat);
//        	formatter = DateFormat.getTimeInstance(DateFormat.SHORT);
//        else
//        	//formatter = new SimpleDateFormat();
////        	formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
//        	formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
//        
//		return formatter.format(time);
//	}

	public static String formatExpirationTime(Context context, Date time){
        Date now = new Date();
        long distance = (time.getTime() - now.getTime())/1000;
        return formatExpirationTime(context, distance);
	}
	
	public static String formatExpirationTime(Context context, long distance){
        Date now = new Date();
        Date time = new Date();
        Date distanceAsTime = new Date();
        distanceAsTime.setTime(distance*1000);
        time.setTime(now.getTime() + distance * 1000);
		String ret;
		String f;
        if(distance < 0){
//        	ret = context.getString(R.string.expired);
        	ret = "";
        }else{
    		long d;
        	if(distance < 3600){
        		SimpleDateFormat format = new SimpleDateFormat("mm:ss");
	        	ret = format.format(distanceAsTime);
        	}else if(distance <3600*24){
        		f = context.getString(R.string.x_hours);
        		d = distance/3600;
            	ret = String.format(f, d);
        	}else if(distance <3600*24*30){
        		f = context.getString(R.string.x_days);
        		d = distance/3600/24;
            	ret = String.format(f, d);
        	}else{
	        	ret = DateFormat.getDateInstance(DateFormat.MEDIUM).format(time);
        	}
        }
        return ret;
	}
	
	public final static void showToastLong(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_LONG).show();
	}

	public final static void showToastShort(Context context, String message){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public final static Builder alert(Context context, String title, String message){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if(title != null) builder.setTitle(title);
		builder.setMessage(message);
		return builder;
	}

	static public Builder confirm(Context context, String title, String message){
		return alert(context, title, message).setNegativeButton(android.R.string.cancel, null);
    }

	static public void flipViewOut(View view, final Runnable callback){
		flipView(view, 0, 90, 300, new AccelerateInterpolator(), callback);
	}
	
	static public void flipViewIn(View view, final Runnable callback){
		flipView(view, 270, 360, 300, new DecelerateInterpolator(), callback);
	}

	static public void flipView(View view, float startAngle, float endAngle, int duration, Interpolator interpolator, final Runnable callback) {
//    	float startAngle = 0;
//    	float endAngle = 360;
        // Find the center of the container
        final float centerX = view.getWidth() / 2.0f;
        final float centerY = view.getHeight() / 2.0f;

        // Create a new 3D rotation with the supplied parameter
        // The animation listener is used to trigger the next animation
        final Rotate3dAnimation rotation =
                new Rotate3dAnimation(startAngle, endAngle, centerX, centerY, 0.0f, true);
        rotation.setDuration(1000);
        rotation.setFillAfter(true);
        rotation.setInterpolator(interpolator);
        rotation.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				if(callback != null) callback.run();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});

        view.startAnimation(rotation);
    }

    static public void flipView(final View front, final View back, final Runnable callback) {
    	final long duration = 600;

		front.setVisibility(View.VISIBLE);
		back.setVisibility(View.GONE);
        float centerX = front.getWidth() / 2.0f;
        float centerY = front.getHeight() / 2.0f;
        Rotate3dAnimation rota1 = new Rotate3dAnimation(0, 90, centerX, centerY, 0.0f, false);
        rota1.setDuration(duration/2);
//        rota1.setFillAfter(true);
        rota1.setInterpolator(new AccelerateInterpolator());
        rota1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				front.setVisibility(View.GONE);
				back.setVisibility(View.VISIBLE); 
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});
        front.startAnimation(rota1);

//        centerX = back.getWidth() / 2.0f;
//        centerY = back.getHeight() / 2.0f;
        Rotate3dAnimation rota2 = new Rotate3dAnimation(270, 360, centerX, centerY, 0.0f, false);
        rota2.setDuration(duration/2);
//        rota2.setFillAfter(true);
        rota2.setInterpolator(new DecelerateInterpolator());
        rota2.setStartOffset(duration/2);
        rota2.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				if(callback != null) callback.run();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});
        back.startAnimation(rota2);
    }

    static public void FadeInOutView(final View front, final View back, final Runnable callback) {
    	final long duration = 300;

		front.setVisibility(View.VISIBLE);
		back.setVisibility(View.VISIBLE);
        Animation rota1 = new AlphaAnimation(1f, 0f);
        rota1.setDuration(duration);
//        rota1.setFillAfter(true);
        rota1.setInterpolator(new AccelerateInterpolator());
        rota1.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				front.setVisibility(View.GONE);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});
        front.startAnimation(rota1);

        Animation rota2 = new AlphaAnimation(0f, 1f);
        rota2.setDuration(duration);
//        rota2.setFillAfter(true);
//        rota2.setInterpolator(new DecelerateInterpolator());
        rota2.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) {
				if(callback != null) callback.run();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
		});
        back.startAnimation(rota2);
    }

    static public void setAlpha(View view, float value){
//		if(Build.VERSION.SDK_INT >= Build.VER/SION_CODES.HONEYCOMB)
			view.setAlpha(value);
//		else
//			AnimatorProxy.wrap(view).setAlpha(value);
	}

    static public String formatPrice(double dPrice){
		int iPrice = (int) dPrice;
		String result = (iPrice == dPrice) ? ""+iPrice : ""+dPrice;  
    	return result;
    }

	/** 
     * 检测网络是否连接（注：需要在配置文件即AndroidManifest.xml加入权限） 
     *  
     * @param context 
     * @return true : 网络连接成功 
     * @return false : 网络连接失败 
     * */  
	public static boolean isNetworkAvailable(Context context) {  
	    // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）  
	   ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
	   if (connectivity != null) {  
	       // 获取网络连接管理的对象  
		   NetworkInfo info = connectivity.getActiveNetworkInfo();  
		   if (info != null) {  
		       // 判断当前网络是否已经连接
	//           if (info.getState() == NetworkInfo.State.CONNECTED) {  
	//               return true;  
	//           }  
			   return info.isAvailable();
	       }  
		}
	   return false;  
	}

	// escape all characters except for A-Z, a-z, 0-9
	public static String urlSafeEscape(String string) throws UnsupportedEncodingException{
		Pattern p = Pattern.compile("[^a-zA-Z0-9]");
		Matcher m = p.matcher(string);
		StringBuffer sb = new StringBuffer();
		while (m.find()){
			byte b;
			b = m.group().getBytes("ASCII")[0];
			m.appendReplacement(sb, "_"+b+"_");
		}
		m.appendTail(sb);
		return sb.toString();
	}
	
	public static String urlSafeUnescape(String string) {
		Pattern p = Pattern.compile("_(\\d+)_");
		Matcher m = p.matcher(string);
		StringBuffer sb = new StringBuffer();
		while (m.find()){
			int c;
			c = Integer.parseInt(m.group(1));
			m.appendReplacement(sb, ""+(char)c);
		}
		m.appendTail(sb);
		return sb.toString();
	}



	// simple decrypting by xor each character
//	static public String xor_decrypt(String encrypted){
//		byte KEY = 39;
//		StringBuilder result = new StringBuilder();
//		byte[] bytes = encrypted.getBytes();
//		for(int i=0; i<bytes.length; i++){
//			byte[] b = new byte[1];
//			b[0] = (byte) (bytes[i]^KEY);
//			result.append(new String(b));
//		}
//		return result.toString();
//	}
	
	// New version correspond to backend Tools.xor_encrypt2
	static public String xor_decrypt(String encrypted){
		byte KEYA = 32;
		byte KEYB = 25;

		byte[] bytes = encrypted.getBytes();
		StringBuilder result = new StringBuilder();
		for(int i=0; i<bytes.length; i++){
			byte[] b = new byte[1];
			b[0] = (byte) (bytes[i]^KEYB);
			result.append(new String(b));
		}
		
		bytes = result.toString().getBytes();
		result = new StringBuilder();
		for(int i=0; i<bytes.length; i++){
			byte[] b = new byte[1];
			b[0] = (byte) (bytes[i]^KEYA);
			result.append(new String(b));
		}

		return result.toString();
	}

	static public void saveCommonConfiguration(Context context, JSONObject configuration){
		Tools.setCurrentUser(context, configuration.optJSONObject("user"));
		Tools.setPrefString(context, "last_nas", configuration.optString("nas"));
		Tools.setPrefString(context, "last_humanizer_question", configuration.optString("humanizer_question"));
	}

	public static String getPrefString(Context context, String key, String defValue){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		return pref.getString(key, defValue);
	}

	public static void setPrefString(Context context, String key, String value){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static boolean getPrefBoolean(Context context, String key, boolean defValue){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		return pref.getBoolean(key, defValue);
	}

	public static void setPrefBoolean(Context context, String key, boolean value){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public static String genOrderString(String username, String orderId, JSONObject plan, String gateway) throws Exception{
		String result;
		String orderStr = Tools.urlSafeEscape(username)+"-"+orderId+"-"+plan.getString("name")+"-"+gateway;
		String sign = ""+GeneralHash.ELFHash(orderStr+Const.MINI_KEY); //sign by hash algorithm with pre-shared key
		result = sign + "--" + orderStr;
		return result;
	}
	
	public static boolean verify_signature(String orderInfo, String sign) {
		return sign.equals(""+GeneralHash.ELFHash(orderInfo+Const.MINI_KEY));	
	}
	
	public static String getNetworkName(Context context){
		String result = null;
		try{
	        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
			if (connectivity != null) {  
			   NetworkInfo info = connectivity.getActiveNetworkInfo();
			   if(info != null){
				   result = info.getTypeName();
			   }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
        return result;
	}
	
	public static boolean isWiFiActive(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);  
		if (connectivity != null) {  
		    NetworkInfo[] info = connectivity.getAllNetworkInfo();  
		    if (info != null) {  
		        for (int i = 0; i < info.length; i++) {  
		            if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {  
		                return true;  
		            }  
		        }  
		    }  
		}  
		return false;
	}

    /**
     * IP地址<br>
     * code from:
     * http://www.droidnova.com/get-the-ip-address-of-your-device,304.html <br>
     * 
     * @return 如果返回null，证明没有网络链接。 如果返回String，就是设备当前使用的IP地址，不管是WiFi还是3G
     */
    public static String getLocalIpAddress() {
        try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                                .getNetworkInterfaces(); en.hasMoreElements();) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf
                                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (!inetAddress.isLoopbackAddress()) {
                                        return inetAddress.getHostAddress().toString();
                                }
                        }
                }
        } catch (SocketException ex) {
                Log.e("getLocalIpAddress", ex.toString());
        }
        return null;
    }


    public static boolean isVip(JSONObject user){
    	if(user == null) return false;
    	boolean result = false;
    	long expiration = user.optLong("expiration");
        Date now = new Date();
        long distance = expiration - now.getTime()/1000;
    	if(distance > 60*60) result = true;
    	return result;
    }

    public final static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        } else {
        	if(Build.VERSION.SDK_INT < 8) return true;
        	else return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    static public void setTouchAnim(final Context context, View v){
		v.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN){
					Animation anim  = AnimationUtils.loadAnimation(context, R.anim.re_shrink);
					anim.setFillAfter(true);
					v.startAnimation(anim);
				}else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL){
					Animation anim  = AnimationUtils.loadAnimation(context, R.anim.re_grow);
					anim.setFillAfter(true);
					v.startAnimation(anim);
				}
				return false;
			}
		});
	}

	public static boolean HttpTest(final Context context)
	{ 
		boolean ret = true;
	    if( !Tools.isNetworkAvailable(context) ){
	    	ret = false;
	      AlertDialog.Builder builders = new AlertDialog.Builder(context);
	      builders.setTitle(context.getString(R.string.err_no_network_title));
	      builders.setMessage(context.getString(R.string.err_no_network_message));
	      //LayoutInflater _inflater = LayoutInflater.from(mActivity);
	      //View convertView = _inflater.inflate(R.layout.error,null);
	      //builders.setView(convertView);
	      builders.setPositiveButton(context.getString(R.string.menu_settings),  new DialogInterface.OnClickListener(){
		      public void onClick(DialogInterface dialog, int which)
		      {
		    	  context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS)); 		      
		      }       
	      });
	      builders.setNegativeButton(context.getString(android.R.string.cancel), null);
	      builders.show();
	    }
	    return ret;
	}	

	public static String genPlanDesc(JSONObject plan, String lang){
		String result;
		if(lang.equalsIgnoreCase("zh")){
			result = plan.optString("description_zh");
		}else{
			result = plan.optString("description_en");
		}
		return result;
	}
	
	public static String genPlanPrice(JSONObject plan, String lang){
		String result;
		if(lang.equalsIgnoreCase("zh")){
			result = "￥" + Tools.formatPrice(plan.optDouble("price_rmb", 0.0));
		}else{
			result = "$" + Tools.formatPrice(plan.optDouble("price_usd", 0.0));
		}
		return result;
	}

	public static String getSupportInfo(Context context){
		String[] NetworkTypes = {"Unknown", "GRPS", "EDGE", "UMTS", "CDMA", "EVDO_0", "EVDO_A", "1xRTT", "HSDPA", "HSUPA", "HSPA", "iDen", "EVDO_B", "LTE", "eHRPD", "HSPA+"};
		String[] PhoneTypes = {"None", "GSM", "CDMA", "SIP"};
		
		StringBuilder result = new StringBuilder();
		result.append(context.getString(R.string.support_prompt));
		result.append("\n\n\n\nReindeer technical information:");
		result.append("\n");
		result.append("basic: ");
		result.append(Tools.getClientParameters(context));
		
    	final String bindingEmail = Tools.getBindingUsername(context);
    	if(bindingEmail != null){
    		result.append("\n");
			result.append("account: ");
    		result.append(bindingEmail); 
    	}
    	
    	JSONObject user = Tools.getCurrentUser(context);
    	if(user != null){
    		Date date = new Date(user.optLong("expiration")*1000);
    		result.append("\n");
			result.append("expire in: ");
    		result.append(Tools.formatExpirationTime(context, date)); 
    		result.append("\n");
			result.append("link: ");
    		result.append("http://"+Const.getRootIp(context)+"/u/"+user.optInt("id")); 
    	}
    	
    	try{
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
			result.append("\n");
			result.append("network: ");
			result.append(Tools.getNetworkName(context));
			result.append(",");
			result.append(Tools.getLocalIpAddress());
	//		result.append("\n");
	//		result.append(tm.getLine1Number());
			
			result.append("\n");
			result.append("phone: ");
			result.append(tm.getNetworkCountryIso());
	//		result.append("\n");
	//		result.append(tm.getNetworkOperator());
			result.append(",");
			result.append(tm.getNetworkOperatorName());
			result.append(",");
			result.append(NetworkTypes[tm.getNetworkType()]);
			result.append(",");
			result.append(PhoneTypes[tm.getPhoneType()]);

			result.append("\n");
			result.append("root: " + Const.getRootIp(context));
			result.append(", ");
			result.append(Tools.getPrefString(context, "root_valid", "false"));
			
			JSONObject nas = Tools.getCurrentNas(context);
			if(nas != null){
				result.append("\n");
				result.append("nas: ");
				result.append(nas.optString("region"));
				result.append(", ");
				result.append(nas.optString("city"));
				result.append(", ");
				result.append(nas.optString("ip"));
			}

			result.append("\n");
			result.append("bypass: " + Tools.getPrefBoolean(context, Key.isGFWList(), Const.DEFAULT_SMART_ROUTE));

	//		result.append("\n");
	//		result.append(tm.getSimCountryIso());
	//		result.append("\n");
	//		result.append(tm.getSimOperator());
	//		result.append("\n");
	//		result.append(tm.getSimOperatorName());
	//		result.append("\n");
	//		result.append(tm.getSimSerialNumber());
	//		result.append("\n");
	//		result.append(tm.getSimState());
	//		result.append("\n");
	//		result.append(tm.getSubscriberId());
	//		result.append("\n");
	//		result.append(tm.getAllCellInfo().toString());

			result.append("\n");
			result.append("\n");
			result.append("Log:");
			result.append(Tools.getLog(context));
    	
    	}catch(Exception e){
			result.append("\n");
			result.append(e.getLocalizedMessage());
		}
		return result.toString();
	}
	
	public static void setTextEnabled(Context context, TextView view, boolean enabled){
		if(enabled)
			view.setTextColor(context.getResources().getColor(R.color.main_color));
		else
			view.setTextColor(context.getResources().getColor(android.R.color.primary_text_light));
	}


	static protected boolean createTempDirectoryBeforeFroyo(Context context) {
		Const.TMP_FOLDER = "/sdcard/"+Const.APP_NAME;
	    File tempdir = new File(Const.TMP_FOLDER);
	    if (!tempdir.exists()) {
	        if (!tempdir.mkdirs()) {
	        	Toast.makeText(context, context.getString(R.string.err_no_SD), Toast.LENGTH_LONG).show();
	            //Log.d(Const.APP_NAME, "Cannot create directory: " + Const.TMP_FOLDER);
	            return false;
	        }
	    }
	    return true;
	}

	@SuppressLint("NewApi")
	static public boolean createTempDirectory(Context context) {
		if(!Const.TMP_FOLDER.equals("")) return true;
		if(Build.VERSION.SDK_INT >= 8){ 
		    File tempdir = context.getExternalFilesDir(null); // this function needs API level 8
		    if (tempdir == null) {
	        	Toast.makeText(context, context.getString(R.string.err_no_SD), Toast.LENGTH_LONG).show();
	        	return false;
		    }else{
		    	Const.TMP_FOLDER = tempdir.getAbsolutePath();
			    return true;
		    }
		}else return createTempDirectoryBeforeFroyo(context);
	}

	public static String getFriendlyTime(Context context, long diffInSeconds) {
		StringBuffer sb = new StringBuffer();
		
	    long sec = (diffInSeconds >= 60 ? diffInSeconds % 60 : diffInSeconds);
	    long min = (diffInSeconds = (diffInSeconds / 60)) >= 60 ? diffInSeconds % 60 : diffInSeconds;
	    long hrs = (diffInSeconds = (diffInSeconds / 60)) >= 24 ? diffInSeconds % 24 : diffInSeconds;
	    long days = (diffInSeconds = (diffInSeconds / 24)) >= 30 ? diffInSeconds % 30 : diffInSeconds;
	    long months = (diffInSeconds = (diffInSeconds / 30)) >= 12 ? diffInSeconds % 12 : diffInSeconds;
	    long years = (diffInSeconds = (diffInSeconds / 12));

	    String name;
	    if (hrs > 0) {
	    	name = String.format(context.getString(R.string.x_hours), hrs); 
	    } else if (min > 0) {
	    	name = String.format(context.getString(R.string.x_minutes), min); 
	    } else {
	    	name = String.format(context.getString(R.string.x_seconds), sec); 
	    }
	    sb.append(name);
	    return sb.toString();
	}

	public static JSONObject getCurrentNas(Context context){
		JSONObject nas = null;
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		String str = pref.getString("currentNas", null);
		if(str != null){
			try {
				nas = new JSONObject(str);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return nas;
	}

	public static void setCurrentNas(Context context, JSONObject nas){
		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString("currentNas", nas.toString());
		editor.commit();
	}

//	public static String getCurrentRouteRegion(Context context){
//		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
//		return pref.getString("currentRegion", null);
//	}
//
//	public static String getCurrentRouteCity(Context context){
//		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
//		return pref.getString("currentCity", null);
//	}

//	public static void setCurrentRoute(Context context, String region, String city){
//		SharedPreferences pref = context.getSharedPreferences(Const.PREFS, Activity.MODE_PRIVATE);
//		SharedPreferences.Editor editor = pref.edit();
//		editor.putString("currentRegion", region);
//		editor.putString("currentCity", city);
//		editor.commit();
//	}

	public static Boolean saveImageToFile(Context context, String relative_url, int timeout){
		Boolean ret = false;
		String fileName = getImageFileName(relative_url);
		File f = new File(fileName);
		if(f.length() == 0){ // not exists or size is 0
				Log.v(Const.APP_NAME, Const.APP_NAME + " Utils saving Image: " + relative_url);
				try {
					//DownloadImage.toFile(Const.HTTP_PREFIX + relative_url , f, timeout);
					String url = relative_url;
					if(relative_url.substring(0, 1).equals("/")) url = Const.getRootHttpNoSSL(context) + relative_url;
					DownloadImage.toFile(url , f, timeout);
					ret = true;
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e){
//					Utils.showToast(context, "Out of memory");
					e.printStackTrace();
				}
				Log.v(Const.APP_NAME, Const.APP_NAME + " Utils saved Image: " + relative_url);
		}
		return ret;
	}
	
	public static String getImageFileName(String relative_url){
		return Const.TMP_FOLDER + "/" + relative_url.replaceAll("[\\/\\\\:*?\"<>|\\[\\]]", "_");
	}

	static public Bitmap getImageFromFile(Context context, String relative_url){
		if(relative_url == null) return null;
		String pathName = getImageFileName(relative_url);
		return getImageFromFile(context, pathName, 0, 0);
	}
	
	static public Bitmap getImageFromFile(Context context, String pathName, int reqWidth, int reqHeight){
		Bitmap bm = null;
		try{
		    // First decode with inJustDecodeBounds=true to check dimensions
		    final BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(pathName, options);

		    // Calculate inSampleSize
		    options.inSampleSize = (reqWidth == 0 && reqHeight == 0) ? 1 : calculateInSampleSize(options, reqWidth, reqHeight);

		    // Decode bitmap with inSampleSize set
		    options.inJustDecodeBounds = false;
		    bm = BitmapFactory.decodeFile(pathName, options);

		}catch(OutOfMemoryError e){
			e.printStackTrace();
		}
		return bm;
	}

	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if(reqWidth == 0){
	    	inSampleSize = Math.round((float)height / (float)reqHeight);
	    }else if(reqHeight == 0){
            inSampleSize = Math.round((float)width / (float)reqWidth);
	    }else if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}


	 public static boolean isWifi(Context context) {   
        ConnectivityManager connectivityManager = (ConnectivityManager) context   
                .getSystemService(Context.CONNECTIVITY_SERVICE);   
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();   
        if (activeNetInfo != null   
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {   
            return true;   
        }   
        return false;   
    }   

	public static Boolean imageDownloaded(Context context, String relative_url){
		Boolean ret = false;
		String fileName = getImageFileName(relative_url);
		File f = new File(fileName);
		if(f.length() != 0){ // not exists or size is 0
			ret = true;
		}
		return ret;
	}

	public static void clearLog(Context context){
		Tools.setPrefString(context, "log", "");
	}
	
	public static void addLog(Context context, String log){
		String time = new Date().toGMTString();
		String row = "\n" + time+" " + log;
		Tools.setPrefString(context, "log", getLog(context) + row);
	}
	
	public static String getLog(Context context){
		return Tools.getPrefString(context, "log", "");
	}

	public static String getDnsUrl(Context context){
		String ret = Const.DNS_URL_DEFAULT;
		try{
//			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);  
//			String country = tm.getNetworkCountryIso();
//			if(country.equalsIgnoreCase("cn")) ret = Const.DNS_URL_ZH;
			if(Tools.isVip(Tools.getCurrentUser(context))) ret = Const.DNS_URL_VIP ;
		}catch(Exception e){
			Log.e(Const.APP_NAME, "getDnsUrl: "+e.getMessage());    	
		}
		return ret;
//		return Const.DNS_URL_ZH;
	}


	public static boolean toggleAirplaneMode(Context context) {
		Utils.toggleAirplaneMode(context);
		return true;
	}

//	protected static boolean toggleAboveApiLevel17() {
//	    // Android 4.2 and above
//
//	    return Utils.runRootCommand("ndc resolver flushdefaultif\n"
//	                       + "ndc resolver flushif wlan0\n");
//
//	    //Utils.runRootCommand("settings put global airplane_mode_on 1\n"
//	    //  + "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true\n"
//	    //  + "settings put global airplane_mode_on 0\n"
//	    //  + "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false\n")
//	}
//
//	protected static boolean toggleBelowApiLevel17(Context context) {
//		// Android 4.2 below
//		Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 1);
//		Intent enableIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//		enableIntent.putExtra("state", true);
//		context.sendBroadcast(enableIntent);
//		try {
//			Thread.sleep(3000);
//			Settings.System.putInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0);
//			Intent disableIntent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
//			disableIntent.putExtra("state", false);
//		    context.sendBroadcast(disableIntent);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		    return false;
//		}
//		return true;
//	}

	public static Notification getDefaultNotification(String text){
		int icon = R.drawable.re_ic_launcher;
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, text, when);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		//notification.defaults = Notification.DEFAULT_SOUND;
		return notification;
	}

	public static boolean saveLocalApkIcon(PackageManager pm, String packageName){
		boolean ret = true;
		PackageInfo info;
		try {
			info = pm.getPackageInfo(packageName, 0);
			if(App.saveIcon(pm, info) != null) ret = true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}

	static public boolean isCaching = false;
	static public void cacheMyApps(Context context) {
		Log.v(Const.APP_NAME, Const.APP_NAME + " caching apps...");
		try{
			isCaching = true;
			JSONArray ret = new JSONArray();
			PackageManager pm = context.getPackageManager();
			List<PackageInfo> pckInfos = pm.getInstalledPackages(PackageManager.GET_META_DATA | PackageManager.GET_SIGNATURES);
			AppHelper helper = new AppHelper(context);
			helper.clearAll();
			SQLiteDatabase db = helper.getHelper().getWritableDatabase();
			int count = 0;
			int total = getAppsCount(context);
			for(int i=0; i<pckInfos.size(); i++){
				PackageInfo info = pckInfos.get(i);
	        	if(!info.applicationInfo.sourceDir.matches("/system.*")){
					App app = new App(pm, info);
					ret.put(app.getJSON());
					helper.addApp(db, app);
					count++;
					Intent intent = new Intent(Const.BROADCAST_CACHE_APPS_PROGRESS);
					intent.putExtra(Const.KEY_COUNT, count);
					intent.putExtra(Const.KEY_TOTAL, total);
					intent.putExtra(Const.KEY_APP, app.getJSON().toString());
					context.sendBroadcast(intent);
//					Log.d("", app.getName() + ": " + info.applicationInfo.flags);
				}
			}
//			Utils.closeDB(db);
			Log.v(Const.APP_NAME, Const.APP_NAME + " cached apps");
			Intent intent = new Intent(Const.BROADCAST_CACHE_APPS_PROGRESS);
			intent.putExtra(Const.KEY_FINISHED, true);
			context.sendBroadcast(intent);
			isCaching = false;
		}finally{
			isCaching = false;
		}
//		return ret;
	}
	static public int getAppsCount(Context context){
		PackageManager pm = context.getPackageManager();
		List<PackageInfo> pckInfos = pm.getInstalledPackages(PackageManager.GET_META_DATA | PackageManager.GET_SIGNATURES);
		int total = 0;
		for(int i=0; i<pckInfos.size(); i++){
			PackageInfo info = pckInfos.get(i);
	    	if(!info.applicationInfo.sourceDir.matches("/system.*")){
	    		total ++;
			}
		}
		return total;
	}

    public static boolean isExpired(JSONObject user){
        if(user == null) return false;
        boolean result = false;
        long expiration = user.optLong("expiration");
        Date now = new Date();
        long distance = expiration - now.getTime()/1000;
        if(distance < 0) result = true;
        return result;
    }

}
