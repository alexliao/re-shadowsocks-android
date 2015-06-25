package com.biganiseed.reindeer;


import java.util.Date;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public abstract class Binding extends ReindeerActivity {
	EditText editEmail;
	EditText editPassword;
	EditText editPasswordConfirm;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.re_binding);

        editEmail = (EditText) findViewById(R.id.editEmail); 
        editPassword = (EditText) findViewById(R.id.editPassword); 
        editPasswordConfirm = (EditText) findViewById(R.id.editPasswordConfirm); 
        
        editEmail.setText(Tools.getPrefString(this, "lastBindingEmail", null));
        editPassword.setText(Tools.getPrefString(this, "lastBindingPassword", null));
        
    	View btnOk = findViewById(R.id.btnOk);
    	btnOk.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				String errMsg = null;
				final String email = editEmail.getText().toString().trim();
				final String password = editPassword.getText().toString().trim();
				final String passwordConfirm = editPasswordConfirm.getText().toString().trim();
				errMsg = validate(email, password, passwordConfirm);
				if(errMsg != null){
					Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_SHORT).show();
					return;
				}

				showProgressDialog();
				final Handler handler = new Handler();
				new Thread() {
					public void run(){
						try {
//							JSONObject user = Api.signin(getApplicationContext(), email, password);
							JSONObject user = callApi(email, password);
							Tools.setCurrentUser(getApplicationContext(), user);
							Tools.setPrefString(getApplicationContext(), "lastBindingEmail", email);
							Tools.setPrefString(getApplicationContext(), "lastBindingPassword", password);
							handler.post(new Runnable(){
								@Override
								public void run() {
									onSucceeded();
								}
							});
							finish();
						} catch (final BindFailedException e) {
							handler.post(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
								}
							});
						} catch (final Exception e) {
							handler.post(new Runnable(){
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
								}
							});
						} finally{
							handler.post(new Runnable(){
								@Override
								public void run() {
									hideProgressDialog();
								}
							});
						}
					}
				}.start();
					
			}
		});
        
    	View btnCancel = findViewById(R.id.btnCancel);
    	btnCancel.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				finish();
			}
		});
    }
    
    protected String validate(String email, String password, String passwordConfirm){
    	String errMsg = null;
		if(password.equals("")){
			errMsg = getString(R.string.err_no_password);
			editPassword.requestFocus();
		}
		if(!Tools.isValidEmail(email)){
			errMsg = getString(R.string.err_invalid_email);
			editEmail.requestFocus();
		}

    	return errMsg;
    }
    
    // return user
    abstract protected JSONObject callApi(String username, String password) throws Exception;

	protected void onSucceeded() {}
    	
}
