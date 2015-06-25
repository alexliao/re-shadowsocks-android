package com.biganiseed.reindeer;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;


public class ProgressDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(R.string.loading));
        dialog.setIndeterminate(true);
        //dialog.setCancelable(false);
        //dialog.setCanceledOnTouchOutside(true);
        return dialog;
	}

}
