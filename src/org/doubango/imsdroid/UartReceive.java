package org.doubango.imsdroid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.cmd.EncoderCmd;

import android.os.Handler;
import android.util.Log;

public class UartReceive {
	
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	boolean debugNanoQueue = false , debugEncoderQueue = false;
	
	// We could modify here , to chage how many data should we get from queue.
	public static int getNanoDataSize = 3 , getEncoderDataSize = 1 , beSentMessage = 13;

	private int nanoInterval = 100 , encoderWriteWiatInterval = 50 , 
				encoderReadWaitInterval = 300 , encoderWaitInterval = 350, combineInterval = 400;
	public static int fd,nanoFd,encFd;
	
	byte [] ReByteEnco = new byte[11];
	
	public static float robotLocation[]={0,0,0,0};
	
	public static float[] nanoFloat = new float[getNanoDataSize];
	public static float[] nanoFloat_1 = new float[getNanoDataSize];
	
	/*
	 * [0] = DW1000 anchor 1
	 * [1] = DW1000 anchor 2
	 * [2] = DW1000 anchor 3
	 * */ 
	public static float[] nanoFloat3Datas = new float[3];
	
	byte [] encoderDataByteArr = new byte[11];
	
	private Handler handler = new Handler();
	
	Runnable rNano = new NanoThread();
	Runnable rWEncoder = new EncoderWriteThread();
	Runnable rREncoder = new EncoderReadThread();
	Runnable rCombine = new CombineThread();
	Runnable rEncoder = new EncoderThreadPool();

	UartCmd uartCmd = UartCmd.getInstance();
	
	EncoderCmd encoderCmd = new EncoderCmd();
	
	public static int[] tempInt = new int[3]; // L Wheel , R Wheel , Compass
	
	// for DBG , save X Y data to file
	boolean nanoStart = false , 
			 encoderStart = false , 
			 combineStart = false;
	List<Point> AxisPointData = new ArrayList<Point>();
	
	long  encoderLSum = 0;
	long  encoderRSum = 0;
	
	private static String TAG = "App";
	
	public void RunRecThread() {
		
		nanoStart = true;
		encoderStart = true;
		combineStart = true;
	
		handler.postDelayed(rNano, nanoInterval);
	
		handler.postDelayed(rEncoder, encoderReadWaitInterval);

       	handler.postDelayed(rCombine, combineInterval);
		
	}
	
	public class NanoThread implements Runnable {
		   
		public void run() {			
		    handler.postDelayed(rNano,nanoInterval);
		}
	}

	public class EncoderThreadPool implements Runnable {

		public void run() {

			Log.i(TAG, "EncoderThreadPool");
			singleThreadExecutor.execute(rWEncoder);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			singleThreadExecutor.execute(rREncoder);

			if (encoderStart)
				//handler.postDelayed(rEncoder, 350);
				handler.postDelayed(rEncoder, encoderWaitInterval);
		}

	}
	
	public class EncoderWriteThread implements Runnable {

		public void run() {

		}
	}
	
	
	public class EncoderReadThread implements Runnable {

		public void run() {

		}
	}

	public class CombineThread implements Runnable {

		public void run() {				
		    handler.postDelayed(rCombine, combineInterval);
		}
	}
	
	public static ArrayList<byte[]> getEncoderRange(ArrayList<byte[]> list, int start, int last) {

		ArrayList<byte[]> temp = new ArrayList<byte[]>();
		return temp;
	}
	
	public static float[] getDW1000NewData(ArrayList<float[]> list) {
		
		return nanoFloat3Datas;
	}
	
	public static ArrayList<float[]> getNanoRange(ArrayList<float[]> list, int start, int last) {

		ArrayList<float[]> temp = new ArrayList<float[]>();
		return temp;
	}
}