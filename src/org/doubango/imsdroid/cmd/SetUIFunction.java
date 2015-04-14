package org.doubango.imsdroid.cmd;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
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

import us.justek.sdk.core.CoreStatus;
import us.justek.sdk.core.CoreStatusListener;
import us.justek.sdk.core.ExtraInfo;
import us.justek.sdk.core.JustekSDKCore;
import us.justek.sdk.core.PhoneService;
import us.justek.sdk.core.phone.ClientCall;
import us.justek.sdk.core.phone.ClientCallListener;
import us.justek.sdk.core.phone.ClientCallStatus;
import us.justek.sdk.core.phone.IncomingCallListener;
import us.justek.sdk.core.phone.MediaType;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
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
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	private Button getAxisBtn,jsRunBtn,btHang;

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

	// Auto mode
	private ListView listView;
	private ArrayList<String> list = new ArrayList<String>();
	private ArrayAdapter<String> listAdapter;
	private String schedulingTime;

	/* Temporary declare */
	JustekSDKCore mCore;
	private String[] account = {"40023", "40024"};
	private String[] password = {"Oh5xLN6m", "Kiu72Reo"};
	private String serverURL = "https://58.248.15.221:8443/justek_auth/authentication";
	private Button connect, call;
	private ProgressDialog dialog; 
	
	private final int mCoreStatusIdle = 0;
	private final int mCoreStatusConnecting = 1;
	private final int mCoreStatusConnected = 2;
	private final int mCoreStatusDisconnecting = 3;
	private final int mCoreStatusDisconnected = 4;
	
	private RelativeLayout rl_remote, rl_local ;
	private FrameLayout fl_portrait ;
	private PhoneService mPhoneService;
	
	private ClientCall nowClientCall ;
	private ClientCallStatus nowClientCallStatus ;
	private final int mIncomingCall = 0;
	private final int Unanswered = 1;
	
	
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
		loggin = NetworkStatus.getInstance();

		gameView = (GameView) globalActivity.findViewById(R.id.gameView1);
		game = new Game();
		
		XMPPSet = new XMPPSetting();
		XMPPSetting.setUIfunction = this;
		gameView.setXMPPSetting(XMPPSet);

		SendAlgo = new SendCmdToBoardAlgorithm();

		getScreenSize(globalActivity);

		declarJoyStick();
		declareRobot();
		declareSlideRobotMenu();
		declareTextView();
		declareButton();
		declareImageButton();
		declareListView();
		delcareViedoConferenceFunction();
		

//		if(XMPPSet.IS_SERVER){
//			videoConferenceSignIn(account[0], password[0]);
//		} else {
//			videoConferenceSignIn(account[1], password[1]);
//		}


		/*--------------------------------------------------*/
		/* Temporary */
		
		uartRec = new UartReceive();
		uartRec.RunRecThread();

		// Broadcast for auto mode triggered
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RobotOperationMode.ACTION_INTENT_ALARM);
		globalActivity.registerReceiver(autoTriggerReceiver, intentFilter);
	}

	@SuppressLint("NewApi")
	private void getScreenSize(Activity v) {
		Display display = v.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		
        if (XMPPSet.isConnected()) {
            if (XMPPSetting.IS_SERVER) {
                XMPPSet.XMPPSendText("william1", "ScreenSize "+ width + " " + height);
            } else {
                XMPPSet.XMPPSendText(XMPPSetting.SERVER_NAME, "ScreenSize "+ width + " " + height);
            }
        } else Toast.makeText(mContext, "Lost XMPP Connection", Toast.LENGTH_LONG).show();
	}

	private void declarJoyStick(){
		layout_joystick = (RelativeLayout) globalActivity.findViewById(R.id.layout_joystick);
		setJoyStickParameter(globalActivity);
		layout_joystick.setOnTouchListener(joystickListener);
		
		layout_joystick.setVisibility(View.GONE);
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

	private void declareRobot(){
		
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

	private void declareTextView(){
	    Axis_show_X = (TextView) globalActivity.findViewById(R.id.Axis_show_X);
	    Axis_show_Y = (TextView) globalActivity.findViewById(R.id.Axis_show_Y);
	}

	private void declareButton() {
	    getAxisBtn = (Button) globalActivity.findViewById(R.id.getAxisBtn);
	    jsRunBtn = (Button) globalActivity.findViewById(R.id.runjs);
	    btHang = (Button)globalActivity.findViewById(R.id.hangupbtn);

	    getAxisBtn.setOnClickListener(onClickListener);
	    jsRunBtn.setOnClickListener(onClickListener);
	    btHang.setOnClickListener(onClickListener);
	}
	
	private void declareImageButton(){
	   
		/* Declare manual, semiauto, auto, navistart, reset, setup */
		manual    = (ImageButton) globalActivity.findViewById(R.id.img_manual);
		semiauto  = (ImageButton) globalActivity.findViewById(R.id.img_semiauto);
		auto      = (ImageButton) globalActivity.findViewById(R.id.img_auto);
		navistart = (ImageButton) globalActivity.findViewById(R.id.img_navi);
		reset 	  = (ImageButton) globalActivity.findViewById(R.id.img_reset);
		setup 	  = (ImageButton) globalActivity.findViewById(R.id.img_setup);
		
		
		//adjustButtonSize(manual);
		
		manual.setOnClickListener(onClickListener);
		semiauto.setOnClickListener(onClickListener);
		auto.setOnClickListener(onClickListener);
		navistart.setOnClickListener(onClickListener);
		reset.setOnClickListener(onClickListener);
		setup.setOnClickListener(onClickListener);
	}
	
	private void adjustButtonSize(ImageButton mButton){
		
		
		Bitmap bitmapOrg = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.manual0);
		
		int width = bitmapOrg.getWidth();
		int height = bitmapOrg.getHeight();
		int newWidth = 200;
		int newHeight = 200;
		
		
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		
		Bitmap newBasemap = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height, matrix, true);
		
		BitmapDrawable bmd = new BitmapDrawable(newBasemap);
		ImageView imageView = new ImageView(mContext);
		imageView.setImageDrawable(bmd);
		
		
		int buttonWidth = (int)(width / 4.0);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.setMargins(buttonWidth, 0, 0, 0);
		mButton.setLayoutParams(params);
		

		
		 // add ImageView to the Layout
		
	}
	
	

	private void declareListView() {
	    listView = (ListView)globalActivity.findViewById(R.id.listView);
	    //listAdapter = new ArrayAdapter(globalActivity,android.R.layout.simple_list_item_1,list);
	    listAdapter = new ArrayAdapter<String>(mContext, R.layout.automode_list, list);
	    listView.setAdapter(listAdapter);
	    listView.setOnItemClickListener(onItemClickListener);
	    listView.setOnItemLongClickListener(onItemLongClickListener);	    
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
			        updateRobotModeState(RobotOperationMode.MANUAL_MODE, true);
			        gameView.postInvalidate();
			    }
			    break;
			case R.id.img_semiauto:
			    if (currRobotMode != RobotOperationMode.SEMI_AUTO_MODE) {
			        updateRobotModeState(RobotOperationMode.SEMI_AUTO_MODE, true);
			        revertAutoModeStatus(RobotOperationMode.SEMI_AUTO_MODE);
			        gameView.postInvalidate();
			    }
			    break;
			case R.id.img_auto:
			    if (currRobotMode != RobotOperationMode.AUTO_MODE) {
			        updateRobotModeState(RobotOperationMode.AUTO_MODE, true);
			        revertAutoModeStatus(RobotOperationMode.AUTO_MODE);
			        gameView.postInvalidate();
			    }
			    break;
			case R.id.img_navi:
				Toast.makeText(mContext, "Navi Start", Toast.LENGTH_LONG).show();
				break;
			case R.id.img_reset:
			    if (currRobotMode == RobotOperationMode.SEMI_AUTO_MODE) {
			        revertAutoModeStatus(RobotOperationMode.SEMI_AUTO_MODE);
			        gameView.postInvalidate();
			    } else if (currRobotMode == RobotOperationMode.AUTO_MODE) {
			        revertAutoModeStatus(RobotOperationMode.AUTO_MODE);
			        gameView.postInvalidate();
			    }
				break;
			case R.id.img_setup:
			    if (currRobotMode == RobotOperationMode.AUTO_MODE) {
			        setTriggerAlarm();

			        //Store this schedule to HashMap
			        LinkedList<int[][]> tempQueue = (LinkedList<int[][]>) RobotOperationMode.autoTargetSettingQueue.clone();
			        RobotOperationMode.RobotScheduleHashMap.put(schedulingTime, tempQueue);

			        revertAutoModeStatus(RobotOperationMode.AUTO_MODE);
			        gameView.postInvalidate();
			    }
				break;
			default:
				break;

			}
		}

	};

	//TODO: When click show source and target
	private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position,
	            long id) {
	        String clickSchedule = (String)listView.getItemAtPosition(position);
	        Log.i("terry", "Show Schedule "+clickSchedule+" from list");

	        RobotOperationMode.autoTargetQueue = RobotOperationMode.RobotScheduleHashMap.get(clickSchedule);
	        gameView.isClickSchedule = true;
	        gameView.postInvalidate();
	    }
	};

	private AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {

	    @Override
	    public boolean onItemLongClick(AdapterView<?> parent, View view,
	            int position, long id) {
	        list.remove(position);
	        listAdapter.notifyDataSetChanged();

	        revertAutoModeStatus(RobotOperationMode.AUTO_MODE);
	        gameView.postInvalidate();

	        return true;
	    }
	};

	private final BroadcastReceiver autoTriggerReceiver	= new BroadcastReceiver() {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        final String action = intent.getAction();
	        if(RobotOperationMode.ACTION_INTENT_ALARM.equals(action)){
	            Log.i("terry", "Get intent "+ RobotOperationMode.ACTION_INTENT_ALARM);

	            //For test
	            game.source[0] = game.source[0] +1;
	            game.source[1] = game.source[1] +1;
	            gameView.postInvalidate();
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

	public void updateRobotModeState(int mode, boolean XMPPSendIsNeed) {
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

        if (XMPPSendIsNeed) {
            if (XMPPSet.isConnected())
                XMPPSet.XMPPSendText("mode "+ currRobotMode);
            else Toast.makeText(mContext, "Lost XMPP Connection", Toast.LENGTH_LONG).show();
        }
	}

	private void setTriggerAlarm() {
	    //TODO: Get scheduling time
	    //For test
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTime(new Date());
	    calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
	    calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE));
	    calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + 10);
	    Log.i("terry", "Get scheduling time: "+calendar.getTime());

	    //Set alarm to trigger robot
	    AlarmManager alarmManager = (AlarmManager) globalActivity.getSystemService(Context.ALARM_SERVICE);
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(RobotOperationMode.ACTION_INTENT_ALARM), PendingIntent.FLAG_UPDATE_CURRENT);
	    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

	    //Update List
	    SimpleDateFormat timeFormat = new SimpleDateFormat("MM-dd HH:mm:ss", Locale.getDefault());
	    schedulingTime = timeFormat.format(calendar.getTime());
	    list.add(schedulingTime);
	    listAdapter.notifyDataSetChanged();
	    Log.i("terry", "Show scheduling time: "+schedulingTime);
	}

	private void revertAutoModeStatus(int mode) {
	    if (mode == RobotOperationMode.SEMI_AUTO_MODE) {
	        RobotOperationMode.targetQueue.clear();
	    } else if (mode == RobotOperationMode.AUTO_MODE) {
	        RobotOperationMode.autoTargetSettingQueue.clear();
	        gameView.isClickSchedule = false;
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
	    if (!XMPPSetting.IS_SERVER) {
	        if (XMPPSet.isConnected())
	            XMPPSet.XMPPSendText("james1", inStr);
	        else Toast.makeText(mContext, "Lost XMPP Connection", Toast.LENGTH_LONG).show();
	    } else {
	        String[] inM = inStr.split("\\s+");
	        byte[] cmdByte = uartCmd.GetAllByte(inM);
	        //String decoded = new String(cmdByte, "ISO-8859-1");
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
		        //game.source[0] = game.source[0] + 1;
		        //game.source[1] = game.source[1] + 1;
		        if (XMPPSet.isConnected())
		            XMPPSet.XMPPSendText("william1", "source " + game.source[0] +" " + game.source[1]);
		        else Toast.makeText(mContext, "Lost XMPP Connection", Toast.LENGTH_LONG).show();

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
	
	
	
	/* Integrate CR VideoConferenceCall package */
	private void delcareViedoConferenceFunction(){
		connect = (Button)globalActivity.findViewById(R.id.connectbtn);
		connect.setOnClickListener(videoClickListener);
		
		btHang = (Button)globalActivity.findViewById(R.id.hangupbtn);
		btHang.setOnClickListener(videoClickListener);
		
		
		rl_remote = (RelativeLayout) globalActivity.findViewById(R.id.rl_remote);
		rl_local = (RelativeLayout) globalActivity.findViewById(R.id.rl_local);
		fl_portrait = (FrameLayout) globalActivity.findViewById(R.id.fl_portrait);
		fl_portrait.setVisibility(View.GONE);
		
		/* Login: Server, auto connection */ 
		if(XMPPSet.IS_SERVER){
			connect.setVisibility(View.INVISIBLE);
			connect.performClick();
		}
				
	}
	
	private Button.OnClickListener videoClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.connectbtn:

					if(XMPPSet.IS_SERVER){
						videoConferenceSignIn(account[0], password[0]);
					} else if(!XMPPSet.IS_SERVER){
						videoConferenceSignIn(account[1], password[1]);
					}	
					
				break;
				
				case R.id.hangupbtn:
					hangupComingCall();
					break;
			}
			
		}
		
	};
	
	
	private void videoConferenceSignIn(String account, String password){
		mCore = JustekSDKCore.getInstance();
		mCore.signIn(mContext,
				account,      // account 
				password,   // password 
				serverURL, //server
				null,
				new CoreStatusListener() {
					@Override
					public void onCoreStatusChanged(CoreStatus coreStatus, ExtraInfo extraInfo) {
						switch(coreStatus){
							case CoreStatusIdle : 
									myHandler.obtainMessage(mCoreStatusIdle , 0, -1, null).sendToTarget();
								break;
							case CoreStatusConnecting : 
									myHandler.obtainMessage(mCoreStatusConnecting , 0, -1, null).sendToTarget();
								break;
							case CoreStatusConnected : 
									myHandler.obtainMessage(mCoreStatusConnected , 0, -1, null).sendToTarget();
								break;
							case CoreStatusDisconnecting : 
									myHandler.obtainMessage(mCoreStatusDisconnecting , 0, -1, null).sendToTarget();
								break;
							case CoreStatusDisconnected : 
									myHandler.obtainMessage(mCoreStatusDisconnected , 0, -1, null).sendToTarget();
								break;
						}
					}
			});
	}
	
	
	Handler myHandler = new Handler(){   
        public void handleMessage(Message msg) {  
        	super.handleMessage(msg);
        	switch(msg.what){
		    	case mCoreStatusIdle :
		    		Toast.makeText(globalActivity, "CoreStatusIdle", Toast.LENGTH_SHORT).show();
		    		break;
		    	case mCoreStatusConnecting :
		    		Toast.makeText(globalActivity, "CoreStatusConnecting", Toast.LENGTH_SHORT).show();
		    		break;
		    	case mCoreStatusConnected :
		    		Toast.makeText(globalActivity, "CoreStatusConnected", Toast.LENGTH_SHORT).show();
		    		
		    		comingCallService();
		    		clientNewLetter();
		    		
		    		break;
		    	case mCoreStatusDisconnecting :
		    		Toast.makeText(globalActivity, "CoreStatusDisconnecting", Toast.LENGTH_SHORT).show();
		    		break;
		    	case mCoreStatusDisconnected :
		    		Toast.makeText(globalActivity, "CoreStatusDisconnected", Toast.LENGTH_SHORT).show();
		    		break;
        	}
		}
	};
	
	private void comingCallService(){
		if(mCore != null){
			mPhoneService = mCore.getPhoneService();
			mPhoneService.addIncomingCallListener(new IncomingCallListener() {
				@Override
				public void onIncomingCall(final ClientCall clientCall) {
					nowClientCall = clientCall;
					nowClientCall.addCallStatusListener(new ClientCallListener() {
						@Override
						public void onCallStateChanged(ClientCall arg0, ClientCallStatus clientCallStatus, ExtraInfo arg2) {
							if(clientCallStatus == ClientCallStatus.CallEnded){
								nowClientCall = null ;
							}
						}
					});
					
					if(nowClientCall != null){
						ComingCallHandler.obtainMessage(mIncomingCall , 0, -1, null).sendToTarget();
					}
				}
			});
		}
	}
	
	Handler ComingCallHandler = new Handler(){   
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case mIncomingCall:
				if (nowClientCall != null) {
					Toast.makeText(globalActivity, "BuildComingCall",Toast.LENGTH_SHORT).show();
					fl_portrait.setVisibility(View.VISIBLE);
					nowClientCall.answer(MediaType.Video,
							new ClientCallListener() {
								@Override
								public void onCallStateChanged(
										ClientCall clientCall,
										ClientCallStatus clientCallStatus,
										ExtraInfo arg2) {

									switch (clientCallStatus) {
									case CallConnected:
										Log.i("shinhua",
												"CallConnected Success!");
										clientCall.setLocalVideoView(
												globalActivity, rl_local,
												new Point(rl_local.getWidth(),
														rl_local.getHeight()));
										clientCall.setRemoteVideoView(
												globalActivity,
												rl_remote,
												new Point(fl_portrait
														.getWidth(),
														fl_portrait.getHeight()));
										break;
									case CallEnded:
										try {
											rl_local.removeAllViews();
											rl_remote.removeAllViews();
										} catch (Exception e) {

										}
										fl_portrait.setVisibility(View.GONE);
										nowClientCall = null;
										break;
									}

								}
							});
				}

				break;
			case Unanswered:
				Toast.makeText(globalActivity, " Unanswered pls try agin ",Toast.LENGTH_SHORT).show();
				break;

			}
		}
	};
	
	private void clientNewLetter(){
		if(XMPPSet.IS_SERVER == false){
			callnumber(account[0]);
		}
	}
	
	private void callnumber(String number){
		if(nowClientCall == null){
			fl_portrait.setVisibility(View.VISIBLE);
			nowClientCall = mPhoneService.makeCall(number,
					MediaType.Video, 
		     		new ClientCallListener() {
						@Override
						public void onCallStateChanged(ClientCall ClientCall, ClientCallStatus clientCallStatus, ExtraInfo arg2) {
							Log.i("info", "clientCallStatus : "+ clientCallStatus );
							nowClientCallStatus = clientCallStatus ;
							switch (clientCallStatus) {
//								case CallOriginating:
//									nowClientCall.setLocalVideoView(SecondActivity.this, rl_local, new Point(rl_local.getWidth(), rl_local.getHeight()));
//									break;
								case CallRinging:
									startTimer();
									break;
								case CallConnected:
									dialog.cancel();
									nowClientCall.setLocalVideoView(globalActivity, rl_local, new Point(rl_local.getWidth(), rl_local.getHeight()));
									nowClientCall.setRemoteVideoView(globalActivity, rl_remote, new Point(fl_portrait.getWidth(), fl_portrait.getHeight()));
									break;
								case CallEnded:
									Toast.makeText(globalActivity, " CallEnded pls try agin ", Toast.LENGTH_SHORT).show();
									dialog.cancel();
									if(nowClientCall != null){
										nowClientCall = null ;
										rl_local.removeAllViews();
     									rl_remote.removeAllViews();
										fl_portrait.setVisibility(View.GONE);
									}
									break;
							}
						}
					});
			dialog = new ProgressDialog(globalActivity);
			dialog.setTitle("Dialing");
    		dialog.setMessage("pls wait");
    		dialog.setCancelable(false);
    		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(nowClientCall != null){
						nowClientCall.end();
						nowClientCall = null ;
						fl_portrait.setVisibility(View.GONE);
					}
				}
			});
    		dialog.show();
		}
		
	}
	
	private void startTimer(){  
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {   
                    Thread.sleep(15000);  
                } catch (InterruptedException e) {  
                }     
                if(nowClientCallStatus == ClientCallStatus.CallRinging){
                	 myHandler.obtainMessage(Unanswered, 0, -1, null).sendToTarget();
                }     
			}
		}).start();
    }
	
	private void hangupComingCall(){
		
		if(nowClientCall != null){
			nowClientCall.end();
			nowClientCall = null ;
			rl_local.removeAllViews();
			rl_remote.removeAllViews();
			fl_portrait.setVisibility(View.GONE);
		}
	}
	
	
	
}
