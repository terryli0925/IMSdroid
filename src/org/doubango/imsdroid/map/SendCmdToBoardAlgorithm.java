package org.doubango.imsdroid.map;

import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.cmd.SetUIFunction;

import android.util.Log;

public class SendCmdToBoardAlgorithm {

	private String TAG = "william";
	
	//private static XMPPSetting XMPPSet;
	public UartCmd uartCmd = UartCmd.getInstance();
	boolean arduinoDebug = false;
	int nextX = 0 , nextY = 0;
	int originalX = 0, originalY = 0;
	MapList mapList;

	SetUIFunction setUIfunction = SetUIFunction.getInstance();
	
	public static GameView _gameView ;
	public static Game _game ;
	
	//GameView gameView;
	
	public void SendCommand(XMPPSetting inXMPPSet, String inString) {

	}

	public static void SetCompass()
	{

	}

	public void RobotStart(final GameView gameView , final Game game , final XMPPSetting inXMPPSet) 
	{
		Log.i("william", "Robot start running");
		
		new Thread() {
			public void run() {
			    gameView.postInvalidate();				
			}
		}.start();
	}
}
