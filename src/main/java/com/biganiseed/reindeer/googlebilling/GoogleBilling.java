package com.biganiseed.reindeer.googlebilling;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.github.shadowsocks.R;
import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.Tools;
import com.biganiseed.reindeer.googlebilling.util.IabHelper;
import com.biganiseed.reindeer.googlebilling.util.IabResult;
import com.biganiseed.reindeer.googlebilling.util.Inventory;
import com.biganiseed.reindeer.googlebilling.util.Purchase;
import com.biganiseed.reindeer.util.ParamRunnable;

public class GoogleBilling {
    public static String TAG = Const.APP_NAME + " Google Billing";
    public static String APPLICATION_PUBLIC_KEY =  "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAl0mKgXdQ/jf2VYT55kuv/cAc7SR6o7R2xiZgtaRHozqqzh/PKA3fBu7Yqt2HKCbqImDVD5zLfI0PPXKPsj9r9RIqh4iVxxshuoCNBeqkwf04HadKNNjFOOxvsrjfBO8VT0/19pSgzchjAQ21+uY+pjufQlTb/a9wZxpEtKDmSaPmhgzNRhcqOn0xmPQ2f7zJUwA/L0Y82aipTRoBEpmKvP4kHCig65FdC54IVzA5hyOXNg3pHIp2M970pOood5z/ixm/ny3NfR8B+zHV0txeIlvnVWKKdLfSra5maX072tcHm/WZUBAbc8bVHFYzAASSNd7k9rnRbv1lDTgjALkg/wIDAQAB";

	public static final int GOOGLE_BILLING_REQUEST = 39483;
	IabHelper mHelper;
	Activity mActivity;
	ParamRunnable mConsumeCallback;
	ParamRunnable mPurchaseCallback;
	
	public GoogleBilling(Activity activity, ParamRunnable consumeCallback, ParamRunnable purchaseCallback){
		mActivity = activity;
		mConsumeCallback = consumeCallback;
		mPurchaseCallback = purchaseCallback;
	}
	
	public void setup(final boolean silience){
		mHelper = new IabHelper(mActivity, GoogleBilling.APPLICATION_PUBLIC_KEY);
//		mHelper.enableDebugLogging(true);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
//                    if(!silience) Tools.alert(mActivity, mActivity.getString(R.string.err_google_checkout_unavailable), result.toString()).show();
                  if(!silience) Tools.alert(mActivity, null, mActivity.getString(R.string.err_google_checkout_unavailable)).show();
                    return;
                }
                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
		
	}
	
    public void dispose() {
        // very important: to be called in Activity onDestroy()
        Log.d(Const.APP_NAME, "Destroying helper.");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }
    
    public void launchPurchaseFlow(String sku, String payload){
        mHelper.launchPurchaseFlow(mActivity, sku, GOOGLE_BILLING_REQUEST,
                mPurchaseFinishedListener, payload);
    }

	// Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, final Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                Tools.showToastLong(mActivity, "Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check
             * the developer payload to see if it's correct! See
             * verifyDeveloperPayload().
             */

//            // Do we have the premium upgrade?
//            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
//            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
//            Log.d(APP_NAME, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));
//
//            // Do we have the infinite gas plan?
//            Purchase infiniteGasPurchase = inventory.getPurchase(SKU_INFINITE_GAS);
//            mSubscribedToInfiniteGas = (infiniteGasPurchase != null &&
//                    verifyDeveloperPayload(infiniteGasPurchase));
//            Log.d(APP_NAME, "User " + (mSubscribedToInfiniteGas ? "HAS" : "DOES NOT HAVE")
//                        + " infinite gas subscription.");
//            if (mSubscribedToInfiniteGas) mTank = TANK_MAX;
//
//            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
//            Purchase gasPurchase = inventory.getPurchase(SKU_GAS);
//            if (gasPurchase != null && verifyDeveloperPayload(gasPurchase)) {
//                Log.d(APP_NAME, "We have gas. Consuming it.");
//                mHelper.consumeAsync(inventory.getPurchase(SKU_GAS), mConsumeFinishedListener);
//                return;
//            }
//
            if(mConsumeCallback != null){
	            // Check for gas delivery -- if we own gas, we should fill up the tank immediately
            	final Handler handler = new Handler();
				new Thread() {
					public void run(){
			            String[] skus = "day week month season halfyear year twoyear".split(" ");
			            for(int i=0; i<skus.length; i++){
			                Purchase purchase = inventory.getPurchase(skus[i]);
			                if (purchase != null && verifyDeveloperPayload(purchase)) {
			                    Log.d(TAG, "We have [" + skus[i] + "]. Consuming it.");
			                    mConsumeCallback.param = purchase;
			                    mConsumeCallback.run(); // call pay_order to add time on backend, throw exception if failed.
			                    if((Boolean)(mConsumeCallback.param)){
			                    	final Purchase finalPurchase = purchase;
			                    	handler.post(new Runnable(){
										@Override
										public void run() {
					                    	mHelper.consumeAsync(finalPurchase, mConsumeFinishedListener);
										}
			                    	});
			                    }
//			                    break;
			                }
			            }
					}
				}.start();
            }
            
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };

    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means filling the gas tank a bit
                Log.d(TAG, "Consumption successful. Provisioning.");
            }
            else {
                Tools.showToastLong(mActivity, "Error while consuming: " + result);
            }
            Log.d(TAG, "End consumption flow.");
        }
    };
    
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
            	if(!result.getMessage().contains("-1005"))
            		Tools.showToastLong(mActivity, "Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
            	Tools.showToastLong(mActivity, "Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d(TAG, "Purchase successful.");

            
            if(mPurchaseCallback != null){
//            	mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//            	mPurchaseCallback.param = purchase.getDeveloperPayload();
//                Log.d(TAG, "Purchase token: " + purchase.getToken());
            	mPurchaseCallback.param = purchase;
            	mPurchaseCallback.run();
            }
//            if (purchase.getSku().equals(plan.optString("name"))) {
//                // bought 1/4 tank of gas. So consume it.
//                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
//            }
//            else if (purchase.getSku().equals(SKU_PREMIUM)) {
//                // bought the premium upgrade!
//                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
//                alert("Thank you for upgrading to premium!");
//                mIsPremium = true;
//                updateUi();
//                setWaitScreen(false);
//            }
//            else if (purchase.getSku().equals(SKU_INFINITE_GAS)) {
//                // bought the infinite gas subscription
//                Log.d(TAG, "Infinite gas subscription purchased.");
//                alert("Thank you for subscribing to infinite gas!");
//                mSubscribedToInfiniteGas = true;
//                mTank = TANK_MAX;
//                updateUi();
//                setWaitScreen(false);
//            }
        }
    };
    
    public void consumeAsync(Purchase purchase){
    	mHelper.consumeAsync(purchase, mConsumeFinishedListener);
    }

	
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }

    // handle the event from parent Activity. should be called in parentActivity.onActivityResult()
	// return false if not handled.
	public boolean handleActivityResult(final int requestCode,	final int resultCode, final Intent data) {
		if (requestCode == GOOGLE_BILLING_REQUEST) {
	        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
	        if (mHelper == null) return true;

	        // Pass on the activity result to the helper for handling
	        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
	            // not handled, so handle it ourselves (here's where you'd
	            // perform any handling of activity results not related to in-app
	            // billing...
	            return false;
	        }
	        else {
	            Log.d(TAG, "onActivityResult handled by IABUtil.");
	        }
		} else {
			throw new RuntimeException("unknown request code: " + requestCode);
		}
		return true;
	}



}
