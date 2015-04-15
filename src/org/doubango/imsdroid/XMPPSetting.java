package org.doubango.imsdroid;

import java.io.IOException;

import org.doubango.imsdroid.cmd.SetUIFunction;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.RobotOperationMode;
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
    private static String TAG = "william";

    public static final boolean IS_SERVER = true; // Server: true, Client: false
    public static final String SERVER_NAME = "rdc01";
    public static final String CLIENT_NAME = "rdc02";
    //public static final String[] CLIENT_LIST = {"rdc02", "rdc03", "rdc04"};

    //private static final String XMPP_SERVER_NAME = "61.222.245.149;
    private static final String XMPP_SERVER_NAME = "ea-xmppserver.cloudapp.net";
    private static final String XMPP_PORT = "5222";
    private static final String XMPP_DOMAIN_NAME = "ea-xmppserver";
    private static final String XMPP_RESOURCE_NAME = "";

	private static XMPPConnection connection;
	private UartCmd UCmd = UartCmd.getInstance();
	private boolean LogSuc = false;
	
	public Game game = new Game();
	public static GameView _gameView;
	public static SetUIFunction setUIfunction;

	//public XMPPSetting(ScreenAV xmppClient)
	public XMPPSetting()
	{
		//this.xmppClient = xmppClient;
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
		            if (message.getBody() != null) {
		                String fromName = StringUtils.parseBareAddress(message.getFrom());
		                String[] inM = message.getBody().split("\\s+");

		                Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName + "]" );
		                if (inM[0].equals("source"))
		                {
		                    updateSource(inM[1], inM[2]);
		                    _gameView.postInvalidate();
		                }
		                else if (inM[0].equals("target"))
		                {
		                    updateTarget(inM[1], inM[2]);
		                    _gameView.postInvalidate();
		                }
		                else if (inM[0].equals("semiauto"))
		                {
		                    updateTrackPos(inM[1], inM[2], inM[3]);
		                    _gameView.postInvalidate();
		                }
		                else if (inM[0].equals("auto"))
		                {
		                    updateTrackPos(inM[1], inM[2], inM[3]);
		                    _gameView.postInvalidate();
		                }
		                else if (inM[0].equals("mode"))
		                {
		                    updateRobotModeState(inM[1]);
		                    _gameView.postInvalidate();
		                }
		                else if (inM[0].equals("ScreenSize")){
		                	
		                	_gameView.setRemoteScreenSize(Integer.valueOf(inM[1]), Integer.valueOf(inM[2]));
		                }
		                else if (inM[0].equals("coord")){
		                	
		                	_gameView.transRemoteCoord(Double.valueOf(inM[1]), Double.valueOf(inM[2]));
		                }
		                else
		                {
							try {
								byte[] cmdByte = UCmd.GetAllByte(inM);
								//Log.i(TAG, "Got text [" + message.getBody() + "] from [" + fromName + "]" + " Func num = " + cmdByte[1] + " Direc = " + cmdByte[2]);
								//Do JNI here , We got correct data format here.
								//String decoded = new String(cmdByte, "ISO-8859-1");
								UCmd.SendMsgUart(1,cmdByte);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		                }
		                
		                //We receive message here.
		                
		            }
		        }
		    }, filter);
		}
    }

	public void XMPPSendText(String xmppText)
	{
	    if (IS_SERVER) {
	        XMPPSendText(CLIENT_NAME, xmppText);
	    } else {
	        XMPPSendText(SERVER_NAME, xmppText);
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
        MapList.source[0] = Integer.parseInt(x);
        MapList.source[1] = Integer.parseInt(y);
	}

	private void updateTarget(String x, String y) {
	    MapList.target[0][0] = Integer.parseInt(x);
	    MapList.target[0][1] = Integer.parseInt(y);
	}

	private void updateTrackPos(String action, String x, String y) {
	    int tempTarget[][] = {{Integer.parseInt(x), Integer.parseInt(y)}};

	    if (action.equals(RobotOperationMode.ACTION_TARGET_ADD)) {
	        RobotOperationMode.targetQueue.offer(tempTarget);
	        //Log.i(TAG, "Offer targetQueue, size= "+MapList.targetQueue.size());
	    }else if (action.equals(RobotOperationMode.ACTION_TARGET_REMOVE)) {
	        int trackIndex = RobotOperationMode.getIndexInTrackList(tempTarget, RobotOperationMode.targetQueue);
	        if (trackIndex != -1) RobotOperationMode.targetQueue.remove(trackIndex);
	        //Log.i(TAG, "Remove targetQueue, size= "+MapList.targetQueue.size());
	    }
	}

	private void updateRobotModeState(String mode) {
	    //Using handler to update ImageButton
	    android.os.Message message1 = modeButtonHandler.obtainMessage(1, mode);
	    modeButtonHandler.sendMessage(message1);
	}

	private Handler modeButtonHandler = new Handler(){
	    public void handleMessage(android.os.Message msg){
	        setUIfunction.updateRobotModeState(Integer.valueOf((String)msg.obj), false);
	        super.handleMessage(msg);
	    }
	};
}
