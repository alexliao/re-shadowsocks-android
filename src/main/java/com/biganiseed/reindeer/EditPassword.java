package com.biganiseed.reindeer;


import java.util.Date;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class EditPassword extends Binding {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editEmail.setEnabled(false);
        editEmail.setSelectAllOnFocus(false);
    }

    @Override
	protected JSONObject callApi(String username, String password) throws Exception {
		return Api.editUser(getApplicationContext(), username, password);
	}
    
    @Override
    protected void onSucceeded(){
    	Toast.makeText(EditPassword.this, getString(R.string.edit_password_success), Toast.LENGTH_SHORT).show();
    }


}
