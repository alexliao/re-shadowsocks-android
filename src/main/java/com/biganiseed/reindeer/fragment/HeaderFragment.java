package com.biganiseed.reindeer.fragment;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.biganiseed.reindeer.Const;
import com.biganiseed.reindeer.R;
import com.biganiseed.reindeer.Tools;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

public class HeaderFragment extends ReindeerFragment {
	public static final String BROADCAST_UPDATE_TITLE = "com.biganiseed.reindeer.broadcast.update_title";
	public static final String KEY_TITLE = "title";
	public static final String BROADCAST_COLLAPSE = "com.biganiseed.reindeer.broadcast.collapse";
	public static final String KEY_COLLAPSE = "collapse";
	
	TextView txtTitle;
	View btnMenu, btnClose;
	View viewFront, viewBack;
	View btnAccount, btnHelp, btnLogo, btnHome, imgHeader;
	
	UpdateTitleBroadcastReceiver updateTitleReceiver = new UpdateTitleBroadcastReceiver();
	CollapseBroadcastReceiver collapseReceiver = new CollapseBroadcastReceiver();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getActivity().registerReceiver(updateTitleReceiver, new IntentFilter(BROADCAST_UPDATE_TITLE));
		getActivity().registerReceiver(collapseReceiver, new IntentFilter(BROADCAST_COLLAPSE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.re_header_fragment, container, false);
        bind(v);
        return v;
    }

	@Override
    public void onDestroy(){
		getActivity().unregisterReceiver(updateTitleReceiver);
		getActivity().unregisterReceiver(collapseReceiver);
    	super.onDestroy();
    }

	void bind(View v){
    	if(v == null) return;
		txtTitle = (TextView) v.findViewById(R.id.txtTitle);
//		txtTitle.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				startActivity(new Intent(getActivity(), Account.class));
//			}
//        });
//    	
		
		viewFront = v.findViewById(R.id.viewFront);
		viewBack = v.findViewById(R.id.viewBack);
		
		btnMenu = v.findViewById(R.id.btnMenu);
		btnMenu.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
//				uncollapse();
//				Tools.flipView(viewFront, viewBack, null);
			    PopupMenu popup = new PopupMenu(a(), v);
			    MenuInflater inflater = popup.getMenuInflater();
			    inflater.inflate(R.menu.re_main_activity, popup.getMenu());
			    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						onOptionsItemSelected(item);
						return false;
					}
			    });
			    popup.show();
			}
        });

//		btnClose = v.findViewById(R.id.btnClose);
//		btnClose.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				Tools.flipView(viewBack, viewFront, null);
//		        HeaderFragment.collapse(a(), getCurrentFragment().collapseHeader());
////				Toast.makeText(getActivity(), getCurrentFragment().getTag(), Toast.LENGTH_SHORT).show();
//			}
//        });

//		btnAccount = v.findViewById(R.id.btnAccount);
//		btnAccount.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				navigate(new AccountFragment(), "account");
//			}
//        });

//		btnHelp = v.findViewById(R.id.btnHelp);
//		btnHelp.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				navigate(new HelpFragment(), "help");
//			}
//        });

		btnLogo = v.findViewById(R.id.btnLogo);
		btnLogo.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				navigate(new AboutFragment(), "about");
			}
        });
		
		btnHome = v.findViewById(R.id.btnHome);
		btnHome.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				navigate(new SwitcherFragment(), "switcher");
			}
        });

        
    	View btnShare = v.findViewById(R.id.btnShare);
    	btnShare.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v) {
				share();
			}
        });

		//		imgHeader = v.findViewById(R.id.imgHeader);
//		imgHeader.setOnClickListener(new View.OnClickListener(){
//			@Override
//			public void onClick(View v) {
//				if(isCollapsed()) uncollapse();
//				else collapse();
//			}
//        });
	}

	void share(){
		ga().share(getString(R.string.share_ladder_via), String.format(getString(R.string.share_ladder_desc_x), Const.SHARE_URL));
	}

    public class UpdateTitleBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (txtTitle == null) return;
            if(intent.getAction().equals(BROADCAST_UPDATE_TITLE)){
            	String title = intent.getStringExtra(KEY_TITLE);
            	txtTitle.setText(title);
            }
        }
    }

    public class CollapseBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (txtTitle == null) return;
            if(intent.getAction().equals(BROADCAST_COLLAPSE)){
            	boolean collapse = intent.getBooleanExtra(KEY_COLLAPSE, false);
        		if(collapse) collapse();
        		else uncollapse();
            }
        }
    }

    public static void setTitle(Context context, String title){
    	context.sendBroadcast(new Intent(HeaderFragment.BROADCAST_UPDATE_TITLE).putExtra(HeaderFragment.KEY_TITLE, title));
    }
    
    public static void collapse(Context context, boolean collpase){
    	context.sendBroadcast(new Intent(HeaderFragment.BROADCAST_COLLAPSE).putExtra(HeaderFragment.KEY_COLLAPSE, collpase));
    }

    
    boolean isCurrentFragment(String tag){
//    	boolean ret = false;
//		FragmentManager fragmentManager = getFragmentManager();
//    	Fragment fragment = fragmentManager.findFragmentByTag(tag);
//    	if(fragment != null){
//    		ret = fragment.isVisible();
//    	}
//    	return ret;

    	return tag.equalsIgnoreCase(getCurrentFragment().getTag());
    }
    
    void collapse(){
    	View view = this.getView();
		if(!isCollapsed()){
	        float density = a().getResources().getDisplayMetrics().density;
//			btnMenu.setTranslationY(145f*density);
	        btnHome.setVisibility(View.VISIBLE);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", -150f*density);
			animator.setDuration(getResources().getInteger(R.integer.config_longAnimTime));
			animator.start();
			
		}
    }

    void uncollapse(){
    	View view = this.getView();
    	if(isCollapsed()){
//			btnMenu.setTranslationY(0f);
	        btnHome.setVisibility(View.INVISIBLE);
			ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", 0f);
			animator.setDuration(getResources().getInteger(R.integer.config_longAnimTime));
			animator.start();
    	}
    }

    boolean isCollapsed(){
    	View view = this.getView();
    	return view.getTranslationY() < 0;
    }

    public BodyFragment getCurrentFragment() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            return (BodyFragment) getFragmentManager().findFragmentByTag("switcher");
        }
        String tag = getFragmentManager().getBackStackEntryAt(getFragmentManager().getBackStackEntryCount() - 1).getName();
        return (BodyFragment) getFragmentManager().findFragmentByTag(tag);
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_account) {
			navigate(new AccountFragment(), "account");
			return true;
		} else if (item.getItemId() == R.id.menu_plans) {
			navigate(new PlansFragment(), "plans");
			return true;
		} else if (item.getItemId() == R.id.menu_nas) {
			navigate(new NasFragment(), "nas");
			return true;
//		} else if (item.getItemId() == R.id.menu_plans) {
//			navigate(new PlansFragment(), "plans");
//			return true;
		} else if (item.getItemId() == R.id.menu_apps) {
			navigate(new AppsFragment(), "apps");
			return true;
		} else if (item.getItemId() == R.id.menu_help) {
			navigate(new HelpFragment(), "help");
			return true;
		} else if (item.getItemId() == R.id.menu_share) {
//			navigate(new AboutFragment(), "about");
//			ga().share(getString(R.string.share_ladder_via), String.format(getString(R.string.share_ladder_desc_x), Const.SHARE_URL));
			share();
			return true;
		} else if (item.getItemId() == R.id.menu_updates) {
			navigate(new UpdatesFragment(), "updates");
			return true;
		} else if (item.getItemId() == R.id.menu_about) {
			Tools.showToastShort(a(), getString(R.string.prompt_version_checking));
			UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
				@Override
				public void onUpdateReturned(int i, UpdateResponse updateResponse) {
					if (i == UpdateStatus.No)
						Tools.showToastShort(a(), getString(R.string.prompt_version_uptodate));
				}
			});
			UmengUpdateAgent.forceUpdate(a());
			navigate(new AboutFragment(), "about");
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
    
}
