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
		// TODO Auto-generated method stub
		int loopcount = 0;
		String correctStr = null;
		Log.i(TAG, " Send command = " + inString);

		if (inString.equals("left")) {
			loopcount = 90;
			correctStr = inString;
		}
		else if (inString.equals("right")) {
			loopcount = 90;
			correctStr = inString;
		} else if (inString.equals("backward")) {
			loopcount = 13;
			correctStr = inString;
		} else if (inString.equals("direction forward")) {
			loopcount = 32;
			correctStr = inString;
		}
		// for 45 , 135 , 225 , 315 angle 
		else {
			loopcount = 45;
			if (inString.equals("bacRig") || inString.equals("bacLeft"))
				correctStr = "backward";
		}
	
		////////////////////////////////////
		// Send command here .            //
		///////////////////////////////////
		for (int i=0;i< loopcount ; i++)
		{
			if (!arduinoDebug) {
	
				synchronized (inXMPPSet) {
					try {
						inXMPPSet.XMPPSendText("james1", "direction "
								+ inString);
	
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
				}
			}
			else
			{
				//UartMsg.SendMsgUartNano(inString + "\n");
				inXMPPSet.XMPPSendText("james1", inString + "\n");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
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
