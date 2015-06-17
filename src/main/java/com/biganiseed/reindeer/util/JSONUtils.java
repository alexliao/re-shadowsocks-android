package com.biganiseed.reindeer.util;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
	public static Map<String, String> toMap(String str){
		JSONObject jsonObject = null;
		Map<String, String> ret = null;
		try {
			jsonObject = new JSONObject(str);
			ret = toMap(jsonObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static String toJSONString(Map<String, String> map){
		JSONObject jsonObject = new JSONObject(map);
		return jsonObject.toString();
	}
	
	public static Map<String, String> toMap(JSONObject jsonObject){
		Map<String, String> data = new HashMap<String, String>();
		Iterator it = jsonObject.keys();
		while(it.hasNext()){
			String key = String.valueOf(it.next());
			String value = "";
			try {
				value = jsonObject.getString(key);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			data.put(key, value);
		}
		return data;
	}

	public static JSONArray appendArray(JSONArray left, JSONArray right) {
		if(left == null) left = new JSONArray();
		for(int i=0; i<right.length(); i++)
			try {
				left.put(right.get(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return left;
	}

	public static String joinArray(JSONArray array, String splitor){
		String ret = "";
		for(int i=0; i<array.length(); i++){
			ret += array.optString(i);
			if(i < array.length()-1) ret += splitor;
		}
		return ret;
	}
	
	public static JSONArray arrayDelete(JSONArray array, int index){
		JSONArray ret = new JSONArray();
		for(int i=0; i<array.length(); i++){
			if(i != index)
				try {
					ret.put(array.getJSONObject(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}
		}
		return ret;
	}
}
