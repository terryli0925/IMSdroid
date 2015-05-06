package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;

import android.content.BroadcastReceiver;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ScreenXYZsignin extends BaseScreen {
	
	private static String TAG = ScreenXYZsignin.class.getCanonicalName();
	
	private final INgnConfigurationService mConfigurationService;
	private final INgnSipService mSipService;
	private BroadcastReceiver mSipBroadCastRecv;
	
	private ImageButton signinBtn, createAccountBtn;
	int Screen_width, Screen_height;
	int horizontalscope, verticalscope;	
	
	public ScreenXYZsignin() {
		super(SCREEN_TYPE.HOME_T, TAG);
		
		mSipService = getEngine().getSipService();
		mConfigurationService = getEngine().getConfigurationService();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xyz_signin);

		// *************** Get each field from default configure , 
		// *************** if we modify it on view,  will save to default configure on ClickListener function.
		
		setScreenBackground();
		getScreenParameter();
		
		signinBtn = (ImageButton)findViewById(R.id.signin_btn);
		setupButtonPosition(signinBtn, 10, 3, 3);
		createAccountBtn = (ImageButton) findViewById(R.id.account_btn);
		setupButtonPosition(createAccountBtn, 10, 6, 3);

		signinBtn.setOnClickListener(ClickListener);
		createAccountBtn.setOnClickListener(ClickListener);
	}
	
	private void setScreenBackground(){
		View v = this.findViewById(R.id.screen_linear_layout);
		v.setBackgroundResource(R.drawable.xyzlogin);
	}
	
	private void getScreenParameter(){
		float desity = getResources().getDisplayMetrics().density;
		float dpi = getResources().getDisplayMetrics().densityDpi;
		setdisplayparams();
	}
	
	public void setdisplayparams(){	
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		Screen_width  = size.x;
		Screen_height = size.y;
	}
	
	
	public void setupButtonPosition(ImageButton btn, int GAP, int horizontalGAP, int verticalGAP){
		RelativeLayout.LayoutParams loginLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		horizontalscope = Screen_width / GAP; 
		verticalscope = Screen_height / GAP;		
		loginLayoutParams.setMargins(horizontalscope * horizontalGAP, verticalscope * 6, 0, 0);
		btn.setLayoutParams(loginLayoutParams);
	}
	
	private OnClickListener ClickListener = new OnClickListener(){
		@Override
		public void onClick(final View v) {
			// TODO Auto-generated method stub
		    	switch(v.getId()){
		    
	    		case R.id.signin_btn:
	    			Log.i("shinhua", "Login signin button was pressed.");
	    			mScreenService.show(ScreenXYZLogin.class, "ScreenXYZLogin");
	    			break;	
	 		
	    		case R.id.account_btn:
	    			
	    			break;
	    		default:
	    			Log.i(TAG,"Invaild Button function");
	    			break;			
			}
		}
	};

	@Override
	protected void onDestroy() {
		if (mSipBroadCastRecv != null) {
			unregisterReceiver(mSipBroadCastRecv);
			mSipBroadCastRecv = null;
		}
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		Log.i(TAG, "onBackPressed--");
	}
	
	// Define the Handler that receives messages from the thread and update the progress
    private final Handler handler = new Handler() {
    	
		public void handleMessage(Message msg) {
			String aResponse = msg.getData().getString("message");
			if (aResponse == "Loggin Fail") {
				Toast.makeText(getBaseContext(), aResponse, Toast.LENGTH_SHORT)
						.show();
			} else if (aResponse == "ok") {

				if (mSipService.getRegistrationState() == ConnectionState.CONNECTING
						|| mSipService.getRegistrationState() == ConnectionState.TERMINATING) {
					mSipService.stopStack();
				} else if (mSipService.isRegistered()) {
					mSipService.unRegister();
				} else {
					mSipService.register(ScreenXYZsignin.this);

				}

			}
		}
    };
	
	
}
