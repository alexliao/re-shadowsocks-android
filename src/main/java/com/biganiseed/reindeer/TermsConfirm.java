package com.biganiseed.reindeer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.biganiseed.reindeer.R;

public class TermsConfirm extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.re_terms_confirm);
        
        View btnYes = this.findViewById(R.id.btnYes);
        btnYes.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
		        Tools.setPrefString(getApplicationContext(), "terms_accepted", "true");
				finish();
			}
		});

        View btnNo = this.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

        View txtDesc = this.findViewById(R.id.txtDesc);
        txtDesc.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				startActivity(new Intent(TermsConfirm.this, Terms.class));
				Api.terms(getApplicationContext());
			}
		});
    }

}
