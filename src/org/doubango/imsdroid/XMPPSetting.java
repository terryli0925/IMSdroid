package org.doubango.imsdroid;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.doubango.imsdroid.cmd.SetUIFunction;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.RobotOperationMode;
import org.doubango.imsdroid.map.transformScreenFormula;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.os.Handler;
import android.util.Log;

public class XMPPSetting {
    private static String TAG = "xmpp";

    public static final boolean IS_SERVER = false; // Server: true, Client: false
    public static final String[] ROBOT_ACCOUNT = {"rdc01", "rdc02", "rdc03"};
    public static final String[] ROBOT_PASSWORD = {"rdc01", "rdc02", "rdc03"};
    public static final String[] USER_ACCOUNT = {"rdc04", "rdc05", "rdc06", "rdc07", "rdc08", "rdc09"};
    public static final String[] USER_PASSWORD = {"rdc04", "rdc05", "rdc06", "rdc07", "rdc08", "rdc09"};

    //private static final String XMPP_SERVER_NAME = "61.222.245.149";   //Our Server IP
    private static final String XMPP_SERVER_NAME = "ea-xmppserver.cloudapp.net";
    private static final String XMPP_PORT = "5222";
    //private static final String XMPP_DOMAIN_NAME = "james-pc";
    private static final String XMPP_DOMAIN_NAME = "ea-xmppserver";
    private static final String XMPP_RESOURCE_NAME = "";

    private static XMPPSetting instance;
	private static XMPPConnection connection;
	private boolean LogSuc = false;

	public GameView gameView;
	public SetUIFunction setUIfunction;

	public int mSelectedRobotId = 0;
	public int mCurrentUserId = 0;
	
    transformScreenFormula obj = transformScreenFormula.getInstance();

    private int mWalkableCount = 0;
    private String mCurrentSchedule;

	public static XMPPSetting getInstance() {
//         if (instance == null){
//             instance = new XMPPSetting();
//         }
	    return instance;
	}

	//public XMPPSetting(ScreenAV xmppClient)
	public XMPPSetting()
	{
		//this.xmppClient = xmppClient;
	    instance = this;
	}

	public boolean XMPPStart(String Name , String Pass)
	{
	    String host = XMPP_SERVER_NAME;
	    String port = XMPP_PORT;

	     String username = Name;
	     String password = Pass;
	     LogSuc = false;	
	     // Create a connection

	     ConnectionConfiguration connConfig =
	             new ConnectionConfiguration(host, Integer.parseInt(port));
	     connection = new XMPPConnection(connConfig);
	     Log.i(TAG, "Name= " + username + " Pass = " + Pass);

	     try {
	         connection.connect();
	         Log.i(TAG, "[SettingsDialog] Connected to " + connection.getHost());
	     } catch (XMPPException ex1) {
	         Log.e(TAG, "[SettingsDialog] Failed to connect to " + connection.getHost());
	         Log.e(TAG, ex1.toString());
	        // xmppClient.setConnection(null);
	         setConnection(null);
	     }

	     try {
	         connection.login(username, password);
	         Log.i(TAG, "Logged in as " + connection.getUser());
	
	         // Set the status to available
	         Presence presence = new Presence(Presence.Type.available);
	         connection.sendPacket(presence);
	        // xmppClient.setConnection(connection);
	         setConnection(connection);
	         LogSuc = true;
	     } catch (XMPPException ex) {
	         Log.e(TAG, "[SettingsDialog] Failed to log in as " + username);
	         Log.e(TAG, ex.toString());
	             //xmppClient.setConnection(null);
	         setConnection(null);
	     }

	     return LogSuc;
	}

	public void setConnection(XMPPConnection connection) {
		if (connection != null) {
		    // Add a packet listener to get messages sent to us
		    PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
		    connection.addPacketListener(new PacketListener() {
		        public void processPacket(Packet packet) {
		            Message message = (Message) packet;
		            if (message.getBody() != null && setUIfunction != null && gameView != null) {
		                String fromName = StringUtils.parseBareAddress(message.getFrom());
		                String[] inM = message.getBody().split("\\s+");
		                Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName + "]" );

		                /*
		                 * userID: Get it from other user which he will does video conference with.
		                 * source: Get source position from robot server.
		                 * semiauto: Get message from robot server in semi-auto mode.
		                 * auto: Get message from robot server in auto mode.
		                 */
		                if (inM[0].equals("userID"))
		                {
		                    mCurrentUserId = Integer.parseInt(inM[1]);
		                }
		                else if (inM[0].equals("source"))
		                {
		                    updateSource(inM[1], inM[2]);
		                    // Avoid we don't get robot's compass
		                    if (inM.length == 4) MapList.robotCompassDegree = Integer.parseInt(inM[3]);
		                    else MapList.robotCompassDegree = 0;

		                    if (setUIfunction.naviStartPhase == RobotOperationMode.NAVI_START
		                            || setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] == RobotOperationMode.NAVI_START) {
		                        gameView.isSourceVisible = !gameView.isSourceVisible;
		                    }
		                    gameView.postInvalidate();
		                }
		                else if (inM[0].equals("semiauto"))
		                {
		                    if (inM[1].equals("walkable")) {
		                        if (inM[2].equals("1")) {}
		                        else if (inM[2].equals("0")) {
		                            cleanSemiAutoSetting();

		                            //Using handler to show toast message
		                            android.os.Message message1 = modeButtonHandler.obtainMessage(3, "The target is not walkable.\n Please try again.");
		                            modeButtonHandler.sendMessage(message1);
		                        }
		                    } else if (inM[1].equals("corner")) {
		                        if (inM[2].equals("start")) {}
		                        else if (inM[2].equals("end")) {
		                            RobotOperationMode.targetQueue.offer(new int[][]{{MapList.target[0], MapList.target[1]}});
		                            setUIfunction.naviStartPhase = RobotOperationMode.NAVI_START;

		                            XMPPSendText("semiauto start");   //Notify robot to start navigating
		                            //Using handler to show toast message
		                            android.os.Message message1 = modeButtonHandler.obtainMessage(3, "Navi Start");
		                            modeButtonHandler.sendMessage(message1);
		                        } else {
		                            obj.transform2ScreenGird(Integer.parseInt(inM[2]),Integer.parseInt(inM[3]));

		                            int tempTarget[][] = {{obj.getX_grid(), obj.getY_grid()}};
		                            RobotOperationMode.targetQueue.offer(tempTarget);
		                        }
		                    } else if (inM[1].equals("end")) {
		                        MapList.source[0] = MapList.target[0];
		                        MapList.source[1] = MapList.target[1];
		                        gameView.isSourceVisible = true;
		                        cleanSemiAutoSetting();
		                        gameView.postInvalidate();
		                    }
		                }
		                else if (inM[0].equals("auto"))
		                {
		                    if (inM[1].equals("walkable")) {
		                        if (inM[2].equals("1")) {
		                            mWalkableCount++;
		                            if (mWalkableCount == RobotOperationMode.autoTargetSettingQueue.size()) {
	                                    android.os.Message message1 = modeButtonHandler.obtainMessage(2, "1");
	                                    modeButtonHandler.sendMessage(message1);
	                                    mWalkableCount = 0;
		                            }
		                        } else if (inM[2].equals("0")) {
		                            android.os.Message message1 = modeButtonHandler.obtainMessage(2, "0");
		                            modeButtonHandler.sendMessage(message1);
		                            mWalkableCount = 0;
		                        }
		                    } else if (inM[1].equals("start")) {
		                        Calendar tempCal = Calendar.getInstance();
		                        SimpleDateFormat timeFormat = new SimpleDateFormat(RobotOperationMode.DATE_FORMAT, Locale.getDefault());
		                        mCurrentSchedule = timeFormat.format(tempCal.getTime());
		                        RobotOperationMode.autoTargetQueue = RobotOperationMode.RobotScheduleHashMap.get(mCurrentSchedule);
		                        int[][] tempTarget = RobotOperationMode.autoTargetQueue.getLast();
		                        MapList.target[0] = tempTarget[0][0];
		                        MapList.target[1] = tempTarget[0][1];
		                        setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] = RobotOperationMode.NAVI_START;

		                        android.os.Message message1 = modeButtonHandler.obtainMessage(3, "Navi Start");
		                        modeButtonHandler.sendMessage(message1);
		                    } else if (inM[1].equals("end")) {
		                        MapList.source[0] = MapList.target[0];
		                        MapList.source[1] = MapList.target[1];
		                        gameView.isSourceVisible = true;
		                        android.os.Message message1 = modeButtonHandler.obtainMessage(4);
		                        modeButtonHandler.sendMessage(message1);
		                    }
		                }
		                else if(inM[0].equals("demo")){
		                	selectDemoMode(inM[1]);
		                }
		                
		            }
		        }
		    }, filter);
		}
    }

	public void XMPPSendText(String xmppText)
	{
	    if (IS_SERVER) {
	        XMPPSendText(USER_ACCOUNT[mCurrentUserId], xmppText);
	    } else {
	        XMPPSendText(ROBOT_ACCOUNT[mSelectedRobotId], xmppText);
	    }
	}

	public void XMPPSendText(String to, String xmppText)
	{
	    //Server name , can't be removed here.
	    //user@domain/resource
	    //String Reci = to+"@james-pc/Smack";
	    String Reci;
	    if (XMPP_RESOURCE_NAME.equals(""))
	        Reci = to+"@"+XMPP_DOMAIN_NAME;
	    else
	        Reci = to+"@"+XMPP_DOMAIN_NAME+"/"+XMPP_RESOURCE_NAME;

	    Log.i(TAG, "Sending text [" + xmppText + "] to [" + Reci + "]");
	    Message msg = new Message(Reci, Message.Type.chat);
	    msg.setBody(xmppText);
	    connection.sendPacket(msg);
	}

	public XMPPConnection GetConnection()
	{
		return this.connection;
	}

	public boolean isConnected() {
	    return connection.isConnected();
	}

	private void updateSource(String x, String y) {
	    obj.transform2ScreenGird(Integer.parseInt(x),Integer.parseInt(y));
	    
	    MapList.source[0] = obj.getX_grid();
	    MapList.source[1] = obj.getY_grid();
	    
	    if (setUIfunction.currRobotMode == RobotOperationMode.SEMI_AUTO_MODE
	            && setUIfunction.naviStartPhase == RobotOperationMode.NAVI_START) {
	        int[][] tempTarget = RobotOperationMode.targetQueue.getFirst();
	        if (MapList.source[0] == tempTarget[0][0] && MapList.source[1] == tempTarget[0][1])
	            RobotOperationMode.targetQueue.remove();
	    } else if (setUIfunction.currRobotMode == RobotOperationMode.AUTO_MODE
	            && setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] == RobotOperationMode.NAVI_START) {
	        int[][] tempTarget = RobotOperationMode.autoTargetQueue.getFirst();
	        if (MapList.source[0] == tempTarget[0][0] && MapList.source[1] == tempTarget[0][1])
	            RobotOperationMode.autoTargetQueue.remove();
	    }
	}

	private void cleanSemiAutoSetting() {
	    MapList.target[0] = -1;
	    MapList.target[1] = -1;
	    RobotOperationMode.targetQueue.clear();
	    setUIfunction.naviStartPhase = RobotOperationMode.NAVI_SETTING;
	}

	private void cleanAutoSetting() {
	    MapList.target[0] = -1;
	    MapList.target[1] = -1;
	    RobotOperationMode.autoTargetQueue.clear();
	    setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] = RobotOperationMode.NAVI_SETTING;
	}

	private Handler modeButtonHandler = new Handler(){
	    public void handleMessage(android.os.Message msg){
	        /*
	         * Define message what value
	         * 1: For update imageButton
	         * 2: For auto mode setting
	         * 3: For show toast message
	         * 4: For remove schedule and update list
	         */
	        if(msg.what == 1) {
	            setUIfunction.updateRobotModeState(Integer.valueOf((String)msg.obj));
	        } else if (msg.what == 2) {
	            if (Integer.valueOf((String)msg.obj) == 1) setUIfunction.setScheduleAlarm();
	            else if (Integer.valueOf((String)msg.obj) == 0) {
	                cleanAutoSetting();
	                setUIfunction.showToastMessage("The target is not walkable.\n Please try again.");
	            }
	            setUIfunction.revertRobotModeStatus(RobotOperationMode.AUTO_MODE);
	        } else if (msg.what == 3) {
	            setUIfunction.showToastMessage((String)msg.obj);
	        } else if (msg.what == 4) {
	            setUIfunction.removeSchedule(mCurrentSchedule);
	            cleanAutoSetting();
	        }
	        gameView.postInvalidate();
	        super.handleMessage(msg);
	    }
	};
	
	private void selectDemoMode(String temp){
		int demo_x, demo_y;
		if(temp.equals("a")){
			demo_x = 400;
			demo_y = 250;
			workflowforDemo(demo_x, demo_y);
		}
		else if(temp.equals("b")){
			demo_x=0;
			demo_y=0;
			workflowforDemo(demo_x, demo_y);
		}
		else if(temp.equals("c")){
			demo_x=0;
			demo_y=0;
			workflowforDemo(demo_x, demo_y);
		}
		
	}
	
	private void workflowforDemo(int demo_x, int demo_y){
	    obj.transform2ScreenGird(demo_x, demo_y);
	    MapList.target[0] = obj.getX_grid();
	    MapList.target[1] = obj.getY_grid();

        XMPPSendText("semiauto coordinate start");
        XMPPSendText("semiauto coordinate" +" "+ demo_x +" "+ demo_y);
        XMPPSendText("semiauto coordinate end");
        
        setUIfunction.naviStartPhase = RobotOperationMode.NAVI_SETUP_DONE;
        gameView.postInvalidate();
	}
	
	
}
