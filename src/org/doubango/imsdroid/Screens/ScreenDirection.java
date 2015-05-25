package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.cmd.SetUIFunction;

import android.os.Bundle;
import android.util.Log;

public class ScreenDirection extends BaseScreen{
	private static String TAG = ScreenDirection.class.getCanonicalName();

	private SetUIFunction setUI;
	private XMPPSetting XMPPSet;
	
	public ScreenDirection() {
		super(SCREEN_TYPE.DIALER_T, TAG);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* Screen JayStick function of shinhua's code */
		setContentView(R.layout.screen_directionjs);

		setUI = new SetUIFunction(this);
		setUI.StartUIFunction();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		XMPPSet = XMPPSetting.getInstance();
		XMPPSet.XMPPDisconnect();
	}

	@Override
	protected void onPause() {
		super.onPause();
//		((Main)getEngine().getMainActivity()).exit();
//		System.runFinalizersOnExit(true);
//		System.exit(0);
	}
}
