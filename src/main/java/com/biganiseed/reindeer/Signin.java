package com.biganiseed.reindeer;

import org.json.JSONObject;

public class Signin extends Binding {

	@Override
	protected JSONObject callApi(String username, String password) throws Exception {
		return Api.signin(getApplicationContext(), username, password);
	}

}
