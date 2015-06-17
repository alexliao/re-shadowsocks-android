package com.biganiseed.reindeer;

import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;

public class Signup extends Binding {
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editEmail.setText("");
        editPassword.setText("");
//        editPasswordConfirm.setVisibility(View.VISIBLE);
    }

//    @Override
//    protected String validate(String email, String password, String passwordConfirm){
//    	String errMsg = null;
//		if(password.equals(passwordConfirm)){
//			errMsg = super.validate(email, password, passwordConfirm);
//		}else{
//			errMsg = getString(R.string.err_password_not_equal);
//			editPassword.requestFocus();
//		}
//
//    	return errMsg;
//    }
    
    @Override
	protected JSONObject callApi(String username, String password) throws Exception {
		return Api.signup(getApplicationContext(), username, password);
	}

}
