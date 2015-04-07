package org.doubango.imsdroid.cmd;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Screens.ScreenAV;
import org.doubango.imsdroid.Screens.ScreenDraw;
import org.doubango.imsdroid.Screens.ScreenUIJoyStick;
import org.doubango.imsdroid.Screens.ScreenUISildeMenu;
import org.doubango.imsdroid.Screens.ScreenUIVerticalSeekBar;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.RobotOperationMode;
import org.doubango.imsdroid.map.SendCmdToBoardAlgorithm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.capricorn.ArcMenu;

public class SetUIFunction {
	public static int currRobotMode = RobotOperationMode.NONE;

	static Activity globalActivity;
	Context mContext;

	private String TAG = "App";
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	public UartCmd uartCmd = UartCmd.getInstance();
	
//	public UartCmd uartCmd;
	
	private UartReceive uartRec;

	// For map use
	private Button jsRunBtn,btHang;

	GameView gameView;
	TextView Axis_show_X, Axis_show_Y;
	EditText Axis_TestAxisInput;
	Game game;
	
	// End for Map use
	MapList map = new MapList();

	SendCmdToBoardAlgorithm SendAlgo;

	
	int height, width;
	
	/* ThreadPool declare for JoyStick operate */
	private ExecutorService newService = Executors.newFixedThreadPool(1);
	private ExecutorService cleanService = Executors.newFixedThreadPool(1);
	private ScheduledExecutorService wifiService = Executors.newScheduledThreadPool(1);

	/* Parameter declare */
	private volatile boolean isContinue = false;
	private int joystickAction;
	private String[] str = { "stop", "forward", "forRig", "right", "bacRig",
			"backward", "bacLeft", "left", "forLeft" };
	private int instructor; /* Robot Commands Direction Instructor */
	int colorValue;
	
	/* JoyStick object declare */
	RelativeLayout layout_joystick, layout_menu, layout_robot;
	ScreenUIJoyStick js;
	ScreenDraw myDraw;

	/* ARC Menu object declare */
	ArcMenu arcMenu;
	int arcLayoutsize;
	private static final int[] ITEM_DRAWABLES = { R.drawable.robot_bowhead,
			R.drawable.robot_normal, R.drawable.robot_headup };
	private static final String[] message = { "Head up", "Normal", "Bow head" };

	/* Robot vertical seekbar object declare */
	ScreenUIVerticalSeekBar seekbar = null;
	
	/* TextView vsProgress; */
	RelativeLayout seekbarlayout;
	LayoutParams seekbarparams, seekBarlayoutparams;

	/* Robot body - WiFI*/
	private ImageView wifistatus1, wifistatus2, wifistatus3, wifistatus4;

	/* SlideMenu */
	private ImageButton arrow;
	LinearLayout slideLayout;
	private ScreenUISildeMenu mAinmMenuOpen, mAinmMenuClose;
	private boolean isMenuOpen = false;
	
	/* ImageButton */
	private ImageButton manual, semiauto, auto, navistart, reset, setup;
	
	
	
	private Handler handler = new Handler();
	
	/* Detect Robot Location */
	Runnable Axis_trigger_thread = new Axis_thread();
	
	/* Navigation parameter */ 
	public int Axis_InputY_fromDW1000;
	public int Axis_InputX_fromDW1000;
	public int Axis_BRSserchArray_Index_Y = 0;
	public int Axis_BRSserchArray_Index_X = 0;

	//private int Axis_GetPollTime = 1000;
	private int Axis_GetPollTime = 5000;   //For test

	ScreenAV _ScreenAV;

	/* Temporary declare */

	private static SetUIFunction instance;
	
	public SetUIFunction(Activity activity) {
		globalActivity = activity;
		mContext = activity.getWindow().getDecorView().getContext();
	}
	
	public static SetUIFunction getInstance() {
		 if (instance == null){
	            synchronized(SetUIFunction.class){
	                if(instance == null) {
	                     instance = new SetUIFunction(globalActivity);
	                }
	            }
	        }
	        return instance;
	}
	
	public void SaveAVSession(ScreenAV screenAV){
		_ScreenAV = screenAV;
	}

	@SuppressLint("NewApi") 
	public void StartUIFunction() {

		Axis_show_X = (TextView) globalActivity.findViewById(R.id.Axis_show_X);
		Axis_show_Y = (TextView) globalActivity.findViewById(R.id.Axis_show_Y);
		//Axis_TestAxisInput = (EditText) globalActivity
			//	.findViewById(R.id.Axis_TestInputAxis);

		loggin = NetworkStatus.getInstance();

		gameView = (GameView) globalActivity.findViewById(R.id.gameView1);
		game = new Game();
		
		XMPPSet = new XMPPSetting();
		XMPPSetting.setUIfunction = this;
		gameView.setXMPPSetting(XMPPSet);

		SendAlgo = new SendCmdToBoardAlgorithm();

		getScreenSize(globalActivity);

		/* Joy Stick */
		layout_joystick = (RelativeLayout) globalActivity
				.findViewById(R.id.layout_joystick);
		setJoyStickParameter(globalActivity);
		layout_joystick.setOnTouchListener(joystickListener);
		
		layout_joystick.setVisibility(View.GONE);

		/* Button declare */
		jsRunBtn = (Button) globalActivity.findViewById(R.id.runjs);
		jsRunBtn.setOnClickListener(onClickListener);

		btHang = (Button)globalActivity.findViewById(R.id.hangupbtn);
		btHang.setOnClickListener(onClickListener);

		delareRobot();
		declareSlideRobotMenu();
		declareImageButton();
		
		// SlideRobot Menu
		slideLayout = (LinearLayout) globalActivity.findViewById(R.id.linearLayout1);
		arrow = (ImageButton) globalActivity.findViewById(R.id.img_arrow);
		arrow.setOnClickListener(onClickListener);
		
		ViewGroup.LayoutParams param = slideLayout.getLayoutParams();
		param.width = 0;
		slideLayout.setLayoutParams(param);
		
		mAinmMenuOpen = new ScreenUISildeMenu(slideLayout, 500, 0, (int)(width/6));
		mAinmMenuClose = new ScreenUISildeMenu(slideLayout, 500, (int)(width/6), 0);

		/*--------------------------------------------------*/
		/* Temporary */
		Button getAxisBtn = (Button) globalActivity
				.findViewById(R.id.getAxisBtn);
		getAxisBtn.setOnClickListener(onClickListener);
		
		/* Temporary - Wifi */		
		WifiManager wifi = (WifiManager) globalActivity.getSystemService(mContext.WIFI_SERVICE);
		
		uartRec = new UartReceive();
		uartRec.RunRecThread();
	}

	@SuppressLint("NewApi")
	private void getScreenSize(Activity v) {
		Display display = v.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
	}

	private void setJoyStickParameter(Activity v) {
		js = new ScreenUIJoyStick(v.getApplicationContext(), layout_joystick,
				R.drawable.joystick);

		js.setStickSize(200, 200);
		js.setStickAlpha(150);
		// js.setLayoutSize(250, 250);
		js.setLayoutSize(300, 300);
		js.setLayoutAlpha(150);
		js.setoffset(70);
		js.setMinimumDistance(70); /* JoyStick Sensitivity */
		js.drawStickDefault(); /* Draw JoyStick function */
	}

	private void delareRobot(){
		
		/* Arc Menu */
		/* Set layout size & position */
		this.arcLayoutsize = width / 6;
		LayoutParams params = new RelativeLayout.LayoutParams(arcLayoutsize, arcLayoutsize);
		RelativeLayout layout = (RelativeLayout) globalActivity.findViewById(R.id.layout_robot);

		arcMenu = (ArcMenu) globalActivity.findViewById(R.id.arc_menu);
		arcMenu.setLayoutParams(params);
		initArcMenu(arcMenu, ITEM_DRAWABLES, globalActivity);

		/* Robot seekbar */
		seekbar = (ScreenUIVerticalSeekBar) globalActivity.findViewById(R.id.robotseekbar);
		seekbarlayout = (RelativeLayout) globalActivity.findViewById(R.id.layout_seekbar);
		setSeekbarParameter();
		
		/* Wifi */
		wifistatus1 = (ImageView) globalActivity.findViewById(R.id.wifi_status1);
		wifistatus2 = (ImageView) globalActivity.findViewById(R.id.wifi_status2);
		wifistatus3 = (ImageView) globalActivity.findViewById(R.id.wifi_status3);
		wifistatus4 = (ImageView) globalActivity.findViewById(R.id.wifi_status4);

		// WiFi & BlueTooth Monitor 
		wifiService.scheduleAtFixedRate(new wifiMonitorThread(), 5000, 5000, TimeUnit.MILLISECONDS);
	}
	
	private void setSeekbarParameter() {
		seekBarlayoutparams = seekbarlayout.getLayoutParams();
		seekBarlayoutparams.height = (int) ((width / 6) * 1.5);
		seekBarlayoutparams.width = width / 6;

		seekbarparams = seekbar.getLayoutParams();
		seekbarparams.height = (int) ((width / 6) * 2);
		seekbar.setMax(1);
		seekbar.setProgress(0);
		seekbar.setOnSeekBarChangeListener(seekbarListener);

	}
	
	private void declareSlideRobotMenu(){
		
		slideLayout = (LinearLayout) globalActivity.findViewById(R.id.linearLayout1);
		arrow = (ImageButton) globalActivity.findViewById(R.id.img_arrow);
		arrow.setOnClickListener(onClickListener);
		
		ViewGroup.LayoutParams param = slideLayout.getLayoutParams();
		param.width = 0;
		slideLayout.setLayoutParams(param);
		
		mAinmMenuOpen = new ScreenUISildeMenu(slideLayout, 500, 0, (int)(width/6));
		mAinmMenuClose = new ScreenUISildeMenu(slideLayout, 500, (int)(width/6), 0);
	}
	
	private void declareImageButton(){
	   
		/* Declare manual, semiauto, auto, navistart, reset, setup */
		manual   = (ImageButton) globalActivity.findViewById(R.id.img_manual);
		semiauto = (ImageButton) globalActivity.findViewById(R.id.img_semiauto);
		auto     = (ImageButton) globalActivity.findViewById(R.id.img_auto);
		navistart = (ImageButton) globalActivity.findViewById(R.id.img_navi);
		reset = (ImageButton) globalActivity.findViewById(R.id.img_reset);
		setup = (ImageButton) globalActivity.findViewById(R.id.img_setup);
		

		
		manual.setOnClickListener(onClickListener);
		semiauto.setOnClickListener(onClickListener);
		auto.setOnClickListener(onClickListener);
		navistart.setOnClickListener(onClickListener);
		reset.setOnClickListener(onClickListener);
		setup.setOnClickListener(onClickListener);
	}
	
	/* The OnTouchListener of Draw JoyStick */
	OnTouchListener joystickListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			joystickAction = event.getAction();

			/* Draw JoyStick */
			js.drawStick(event);

			switch (joystickAction) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				isContinue = true;
				instructor = js.get8Direction();
				if (instructor != 0) {
					useThreadPool(newService, str[instructor]);
				}

				break;
			case MotionEvent.ACTION_UP:
				isContinue = true;
				useThreadPool(newService, str[0]);
				isContinue = false;
				break;
			default:
				isContinue = false;
				break;
			}

			return true;
		}

	};

	/* Set Navigation & others Button onClickListener */
	private Button.OnClickListener onClickListener = new OnClickListener() {
		int indicator;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			indicator = v.getId();
			switch (indicator) {
			case R.id.hangupbtn:
				if (_ScreenAV != null)
					_ScreenAV.hangUpCall();
				break;
			
			case R.id.getAxisBtn:
				SendCmdToBoardAlgorithm.SetCompass();
				handler.postDelayed(Axis_trigger_thread, Axis_GetPollTime);

				break;

			case R.id.runjs:
				synchronized (SendAlgo) {
					try {
						SendAlgo.RobotStart(gameView, game, XMPPSet);
					
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//SendCmdToBoardAlgorithm.SetCompass();

				break;
			
			case R.id.img_arrow:
				if(isMenuOpen){
					slideLayout.startAnimation(mAinmMenuClose);
					isMenuOpen = false;
				}else if(!isMenuOpen){
					slideLayout.startAnimation(mAinmMenuOpen);
					isMenuOpen = true;
				}
				break;
				
			case R.id.img_manual:
			    if (currRobotMode != RobotOperationMode.MANUAL_MODE) {
			        updateRobotMode(RobotOperationMode.MANUAL_MODE, true);
			        gameView.postInvalidate();
			    }
			    break;
			case R.id.img_semiauto:
			    if (currRobotMode != RobotOperationMode.SEMI_AUTO_MODE) {
			        updateRobotMode(RobotOperationMode.SEMI_AUTO_MODE, true);
			        gameView.postInvalidate();
			    }
			    break;
			case R.id.img_auto:
			    if (currRobotMode != RobotOperationMode.AUTO_MODE) {
			        updateRobotMode(RobotOperationMode.AUTO_MODE, true);
			        gameView.postInvalidate();
			    }
			    break;
			case R.id.img_navi:
				Toast.makeText(mContext, "Navi Start", Toast.LENGTH_LONG).show();
				break;
			case R.id.img_reset:
				Toast.makeText(mContext, "Navi Reset", Toast.LENGTH_LONG).show();
				break;
			case R.id.img_setup:
				Toast.makeText(mContext, "Setup", Toast.LENGTH_LONG).show();
				break;
			default:
				break;

			}
		}

	};
	
	private void revertImageButton(){
		manual.setImageResource(R.drawable.manual0);
		semiauto.setImageResource(R.drawable.semiauto0);
		auto.setImageResource(R.drawable.auto0);
	}

	/* Arc Menu */
	private void initArcMenu(final ArcMenu menu, int[] itemDrawables, Activity v) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(v);
			item.setImageResource(itemDrawables[i]);

			final int position = i;

			/* Add arcMenu child */
			menu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					setPanelPosition(position);
				}
			});
		}
	}

	public void updateRobotMode(int mode, boolean XMPPSendIsNeed) {
        if (mode == RobotOperationMode.MANUAL_MODE) {
            manual.setImageResource(R.drawable.manual1);
            semiauto.setImageResource(R.drawable.semiauto0);
            auto.setImageResource(R.drawable.auto0);
            layout_joystick.setVisibility(View.VISIBLE);
            currRobotMode = RobotOperationMode.MANUAL_MODE;
        }else if (mode == RobotOperationMode.SEMI_AUTO_MODE) {
            manual.setImageResource(R.drawable.manual0);
            semiauto.setImageResource(R.drawable.semiauto1);
            auto.setImageResource(R.drawable.auto0);
            layout_joystick.setVisibility(View.GONE);
            currRobotMode = RobotOperationMode.SEMI_AUTO_MODE;
        }else if (mode == RobotOperationMode.AUTO_MODE) {
            manual.setImageResource(R.drawable.manual0);
            semiauto.setImageResource(R.drawable.semiauto0);
            auto.setImageResource(R.drawable.auto1);
            layout_joystick.setVisibility(View.GONE);
            currRobotMode = RobotOperationMode.AUTO_MODE;
        }

        if (XMPPSendIsNeed && loggin.GetLogStatus()) {
            if (XMPPSetting.IS_SERVER) {
                XMPPSet.XMPPSendText("william1", "mode "+ currRobotMode);
            } else {
                XMPPSet.XMPPSendText(XMPPSetting.SERVER_NAME, "mode "+ currRobotMode);
            }
        }
	}

	/* Control Robot panel position */
	private void setPanelPosition(int position) {
		switch (position) {
		case 0:
			Log.i(TAG, "angleBottom");
			sendCommands("pitchAngle bottom");
			break;
			
		case 1:
			Log.i(TAG, "angleMiddle");
			sendCommands("pitchAngle middle");
			break;
		
		case 2:
			Log.i(TAG, "angleTop");
			sendCommands("pitchAngle top");
			break;
		}
	}

	/* Robot Seekbar Listener */
	SeekBar.OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			if (progress < 1) {
				sendCommands("stretch bottom");
			} else if (progress == 1) {
				sendCommands("stretch top");
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	};
	
	private void sendCommands(String message){
		try {
			SendToBoard(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	

	/* XMPP Sendfunction */
	public void SendToBoard(String inStr) throws IOException {
		 Log.i(TAG," loggin status = " + loggin.GetLogStatus());

		if (loggin.GetLogStatus())
			XMPPSet.XMPPSendText("james1", inStr);
		else {
			String[] inM = inStr.split("\\s+");
			byte[] cmdByte = uartCmd.GetAllByte(inM);
//			 String decoded = new String(cmdByte, "ISO-8859-1");
			UartCmd.SendMsgUart(1, cmdByte);
		}
	}

	/* Use thread pool for XMPP communication */
	public class MyThread implements Runnable {
		String SendMsg;

		public MyThread(String SendMsg) {
			// store parameter for later user
			this.SendMsg = SendMsg;
		}

		public void run() {
			while (isContinue) {
				try {
					// Using SCTP transmit message
					Log.i(TAG, "Send message" + SendMsg);

					String sub = SendMsg.substring(SendMsg.indexOf("/") + 1);
					if (SendMsg.equals("stop"))
						SendToBoard("stop stop");
					else
						SendToBoard("direction " + sub);
					Thread.sleep(100l);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class Axis_thread implements Runnable {
		@SuppressLint("UseValueOf") public void run() {

		    //Client don't need to get robot position
		    if (XMPPSetting.IS_SERVER) {
		        //TODO: Get source position
		        //For test
		        game.source[0] = game.source[0] + 1;
		        game.source[1] = game.source[1] + 1;

		        if (loggin.GetLogStatus()) {
		            XMPPSet.XMPPSendText("william1", "source " + game.source[0] +" " + game.source[1]);
		        }
		        gameView.postInvalidate();
		        handler.postDelayed(Axis_trigger_thread, Axis_GetPollTime);
		    }
		}
	}

	/* Create ThreadPool to fix thread quantity */
	private void useThreadPool(ExecutorService service, String Msg) {
		service.execute(new MyThread(Msg));
	}

	/* Monitor wifi signal */
	private class wifiMonitorThread implements Runnable{
		int rssi, level;
		String tempString;
		Message message;
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			WifiManager wifi = (WifiManager) globalActivity.getSystemService(mContext.WIFI_SERVICE);
			rssi = wifi.getConnectionInfo().getRssi();
			level = wifi.calculateSignalLevel(rssi, 4);
			tempString = Integer.toString(rssi);
		
			message = wifiUIHandler.obtainMessage(1,tempString);
			wifiUIHandler.sendMessage(message);
		}
		
	}
		
	/* Update UI Handler */
	private Handler wifiUIHandler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			/* Change wifi UI display */ 
			wifiIconStatus( Integer.valueOf((String)msg.obj) );
		}
	};
	
	/* WiFi signal display icon */
	private void wifiIconStatus(int level){
		
		if(level < 0 && level >= -50){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.VISIBLE);
			wifistatus4.setVisibility(View.VISIBLE);
		}else if(level < -50 && level >= -100){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.VISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}else if(level < -100 && level >= -150){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.INVISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}else if(level < -150 && level >= -200){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.INVISIBLE);
			wifistatus3.setVisibility(View.INVISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}
	}
}
