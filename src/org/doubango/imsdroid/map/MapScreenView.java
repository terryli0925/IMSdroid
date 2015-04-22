package org.doubango.imsdroid.map;

import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;


import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Button;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartReceive;

public class MapScreenView{
	
	private static String TAG = "Shinhua";
	
	private UartReceive uartRec;

	GameView gameView;		
	Game game;
	SendCmdToBoardAlgorithm sendCmdToBoardAlgorithm;

	private NetworkStatus loggin;
	
	private String mName;	//For XMPP thread user name
	private String mPass;	//For XMPP thread user password
	
	private XMPPSetting XMPPSet;

	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;
	
	
	Button jsRunBtn;
	
	
	public void MapScreenView(Activity v) {

		gameView = (GameView) v.findViewById(R.id.gameView1);
		sendCmdToBoardAlgorithm = new SendCmdToBoardAlgorithm();
		game = new Game();
		game.reloadMap(0,gameView);
		
		jsRunBtn = (Button) v.findViewById(R.id.runjs);
		
		initIoc();
	}
	

	

	public void signin(String name) {
		mName = name;
		mPass = "0000";

		loggin = NetworkStatus.getInstance();
		XMPPSet = new XMPPSetting();
	}
    
    public void initIoc(){
    	gameView.game = this.game;
    	game.gameView = this.gameView;
    	game.runButton = this.jsRunBtn;
    	sendCmdToBoardAlgorithm._gameView = this.gameView;
    	sendCmdToBoardAlgorithm._game = this.game;
    	XMPPSetting._gameView = this.gameView;
    }
}
