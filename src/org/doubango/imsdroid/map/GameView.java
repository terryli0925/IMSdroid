package org.doubango.imsdroid.map;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.cmd.SetUIFunction;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class GameView extends View {

	private String TAG = "william";
	private static int VIEW_WIDTH = 640;
	private static int VIEW_HEIGHT = 640;

	public Game game;
	public SetUIFunction setUIfunction;
	GameView GV;
	public XMPPSetting _XMPPSet;
	private NetworkStatus loggin;
	public Spinner mySpinner;// Spinner���ޥ�
	public TextView CDTextView;
	int span = 10;
	int theta = 0;

	public static int mL = 0, mR = 0, mT = 0, mB = 0;
	
	Bitmap source = BitmapFactory.decodeResource(getResources(),
			R.drawable.source);
	Bitmap target = BitmapFactory.decodeResource(getResources(),
			R.drawable.target);
	Bitmap redBall = BitmapFactory.decodeResource(getResources(),
            R.drawable.bullet_ball_glass_red_16);
	Bitmap greenBall = BitmapFactory.decodeResource(getResources(),
	        R.drawable.bullet_ball_glass_green_16);
	Paint paint = new Paint();

	// William Added
	int touchX = 0, touchY = 0;
	int x, y;
	int tempwidth = 0;
	int tempheight = 0;
	String inStr = "test";
	String inStr2 = "test2";
	int fixMapData = 5;
	
	/* Edit */
	int fixWidthMapData = 5, fixHeightMapData = 5;
	int gridX = 0, gridY = 0;
	int row = 0, col = 0;
	Game gamejava = new Game();
	int drawBaseLine = 100, drawIncrease = 20;

	public static int drawCount = 5; // For drawcircle position

	double rX = 0, rY = 0;
	int[][] map;
	int[] old_pos;
	MapList maplist = new MapList();

	public boolean refreshFlag = false, doubleCmd = false,
			algorithmDone = false, mapTouchSize = false;
	private ExecutorService singleThreadExecutor = Executors
			.newSingleThreadExecutor();
	public static ShowThread st;

	/*
	 * [0] : Original position X [1] : Original position Y [2] : Next position X
	 * [3] : Next position Y
	 */
	private static ArrayList<int[][]> pathQueue = new ArrayList<int[][]>();

	Canvas gcanvas;

	/* shinhua add */
	Context mContext;
    int width, height, screenWidth, screenHeight, mapWidth, mapHeight;
	int xcoordinate = 5, ycoordinate = 5;
	private boolean touchDown = false, zoomout = false, isZoom = false;
	public int remoteCoordX, remoteCoordY, remoteScreenWidth, remoteScreenHeight;

	/* Drawing BaseMap */
	Bitmap baseMap = BitmapFactory.decodeResource(getResources(), R.drawable.basemap);

	public GameView(Context context, AttributeSet attrs) {// �غc����
		super(context, attrs);
		if (isInEditMode()) {
			return;
		}
		mContext = context;
		st = new ShowThread();
		getScreenSize();

		loggin = NetworkStatus.getInstance();
		_XMPPSet = XMPPSetting.getInstance();
	}

	protected void onDraw(Canvas canvas) {
		try {
			gcanvas = canvas;
			onMyDraw(canvas);

		} catch (Exception e) {
		}
	}

	public void RunThreadTouch(boolean inFlag) {
		st = new ShowThread();
		refreshFlag = inFlag;
		singleThreadExecutor.execute(st);
	}

	public void SetRobotAxis(double x, double y) {
		rX = x;
		rY = y;
	}

	@SuppressLint("WrongCall")
	protected void onMyDraw(Canvas canvas) {
		super.onDraw(canvas);

		//canvas.drawColor(Color.GRAY); // gray background, annotate this line, the view don't show
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);

		/* Draw BaseMap */
		Log.i("shinhua" , "Map" + mapWidth + ", " + mapHeight);
		reDrawBitmapSize(canvas, paint, baseMap, fixWidthMapData, fixHeightMapData, mapWidth, mapHeight);
		
		
		for (int i = 0; i < row; i++) {
			for (int j = 0; j < col; j++) {
				if (map[i][j] == 0) {
					paint.setColor(Color.WHITE);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setStrokeWidth(5); 
//					canvas.drawRect(fixWidthMapData + j * (span + 1),
//							fixHeightMapData + i * (span + 1), fixWidthMapData
//									+ j * (span + 1) + span, fixHeightMapData
//									+ i * (span + 1) + span, paint);
				} else if (map[i][j] == 1) {// �¦�
					paint.setColor(Color.BLACK);
					//paint.setStyle(Style.FILL);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setStrokeWidth(5); 
					canvas.drawRect(fixWidthMapData + j * (span + 1),
							fixHeightMapData + i * (span + 1), fixWidthMapData
									+ j * (span + 1) + span, fixHeightMapData
									+ i * (span + 1) + span, paint);
				}else if (map[i][j] == 2) {// �¦�
//					paint.setColor(Color.LTGRAY);
//					//paint.setStyle(Style.FILL);
//					paint.setStyle(Style.FILL_AND_STROKE);
//					paint.setStrokeWidth(5); 
//					canvas.drawRect(fixWidthMapData + j * (span + 1),
//							fixHeightMapData + i * (span + 1), fixWidthMapData
//									+ j * (span + 1) + span, fixHeightMapData
//									+ i * (span + 1) + span, paint);
				}
			}
		}

		// Canvas drawLine: Navigation path
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(2);
		int[] lastTarget = {MapList.source[0], MapList.source[1]};
		if (setUIfunction.currRobotMode == RobotOperationMode.SEMI_AUTO_MODE
		        && setUIfunction.naviStartPhase == RobotOperationMode.NAVI_START) {
		    for (int i = 0; i < RobotOperationMode.targetQueue.size(); i++) {
		        int[][] tempTarget = RobotOperationMode.targetQueue.get(i);
 
		        canvas.drawLine(fixWidthMapData + lastTarget[0] * (span + 1) + span / 2,
		                fixHeightMapData + lastTarget[1] * (span + 1) + span / 2,
		                fixWidthMapData + tempTarget[0][0] * (span + 1) + span / 2,
		                fixHeightMapData + tempTarget[0][1] * (span + 1) + span / 2, paint);

		        lastTarget[0] = tempTarget[0][0];
		        lastTarget[1] = tempTarget[0][1];
		    }
		} else if (setUIfunction.currRobotMode == RobotOperationMode.AUTO_MODE
                && setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] == RobotOperationMode.NAVI_START) {
		    for (int i = 0; i < RobotOperationMode.autoTargetQueue.size(); i++) {
		        int[][] tempTarget = RobotOperationMode.autoTargetQueue.get(i);

		        canvas.drawLine(fixWidthMapData + lastTarget[0] * (span + 1) + span / 2,
		                fixHeightMapData + lastTarget[1] * (span + 1) + span / 2,
		                fixWidthMapData + tempTarget[0][0] * (span + 1) + span / 2,
		                fixHeightMapData + tempTarget[0][1] * (span + 1) + span / 2, paint);

		        lastTarget[0] = tempTarget[0][0];
		        lastTarget[1] = tempTarget[0][1];
            }
		}

		// Canvas drawBitmap: Track point
		if (setUIfunction.currRobotMode == RobotOperationMode.SEMI_AUTO_MODE
		        && setUIfunction.naviStartPhase != RobotOperationMode.NAVI_SETUP_DONE) {
		    for (int i = 0; i < RobotOperationMode.targetQueue.size(); i++) {
		        int[][] tempTarget = RobotOperationMode.targetQueue.get(i);
//		        if (i == RobotOperationMode.targetQueue.size() -1) {
//		            canvas.drawBitmap(redBall,
//		                    fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
//		                    + tempTarget[0][1] * (span + 1), paint);
//		        } else {
		        canvas.drawBitmap(greenBall,
		                fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
		                + tempTarget[0][1] * (span + 1), paint);
//		        }
		    }
		} else	if (setUIfunction.currRobotMode == RobotOperationMode.AUTO_MODE) {
		    if (setUIfunction.isClickSchedule || setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] == RobotOperationMode.NAVI_START) {
		        for (int i = 0; i < RobotOperationMode.autoTargetQueue.size(); i++) {
		            int[][] tempTarget = RobotOperationMode.autoTargetQueue.get(i);
//		            if (i == RobotOperationMode.autoTargetQueue.size() -1) {
//		                canvas.drawBitmap(redBall,
//		                        fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
//		                        + tempTarget[0][1] * (span + 1), paint);
//		            } else {
		            canvas.drawBitmap(greenBall,
		                    fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
		                    + tempTarget[0][1] * (span + 1), paint);
//		            }
		        }
		    } else {
		        for (int i = 0; i < RobotOperationMode.autoTargetSettingQueue.size(); i++) {
		            int[][] tempTarget = RobotOperationMode.autoTargetSettingQueue.get(i);
//		            if (i == RobotOperationMode.autoTargetSettingQueue.size() -1) {
//		                canvas.drawBitmap(redBall,
//		                        fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
//		                        + tempTarget[0][1] * (span + 1), paint);
//		            } else {
		            canvas.drawBitmap(greenBall,
		                    fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
		                    + tempTarget[0][1] * (span + 1), paint);
//		            }
		        }
		    }
		}

		// Canvas drawBitmap: Source
		canvas.drawBitmap(source,
				fixWidthMapData + game.source[0] * (span + 1), fixHeightMapData
						+ game.source[1] * (span + 1), paint);
					
		// Canvas drawBitmap: Target
		if (setUIfunction.currRobotMode != RobotOperationMode.MANUAL_MODE
		        && game.target[0] != 0 && game.target[1] != 0) {
		    canvas.drawBitmap(target,
		            fixWidthMapData + game.target[0] * (span + 1), fixHeightMapData
		            + game.target[1] * (span + 1), paint);
		}
	}
	
	private void reDrawBitmapSize(Canvas mCanvas, Paint mPaint, Bitmap mBitmap, int xCoordinate, int yCoordinate, int newWidth, int newHeight){
		
		/* Get Original map size */
		int width = mBitmap.getWidth();
		int height = mBitmap.getHeight();
		
		/* Calculate Scale */
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight =((float) newHeight) / height;
		
		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);

		Bitmap newBasemap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
		mCanvas.drawBitmap(newBasemap, xCoordinate, yCoordinate, mPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(event.getX() >= fixWidthMapData && event.getY() <= fixWidthMapData){
				changeMapZoomIn(true);
			}
			drawZoomMap(event);
		}
			
		return true;

	}

    public void changeMapZoomIn(boolean zoomIn) {
        if (zoomIn) {
            span = 30;
            getMapSize();

            xcoordinate = (int) ((screenWidth / 2) - (mapWidth / 2)); 
            ycoordinate = (int) ((screenHeight / 2) - mapHeight);

            fixWidthMapData = xcoordinate;    // ZoomIn Screen in the right
            //fixWidthMapData = 0;            // ZoomIn Screen in the middle
            fixHeightMapData = ycoordinate;

            isZoom = true;
        }else {
            span = 10;
            xcoordinate = ycoordinate = 5;
            fixWidthMapData = fixHeightMapData = 5;
            
            // Let map screen change back into small size
            isZoom = false;
        }
        requestLayout();
    }
	
	private void drawZoomMap(MotionEvent event) {
		int pointerCount = event.getPointerCount();

		// Avoid thread competition , when user touch 2 points at the same time
		// only one touch point can enter this scope.
		if (pointerCount > 1)
			pointerCount = 1;
		{
			for (int i = 0; i < pointerCount; i++) {
				touchX = (int) event.getX();
				touchY = (int) event.getY();

				tempwidth = touchX - x;
				tempheight = touchY - y;

				int[] pos = getPosW(event);
				// Draw Grid position on canvas
				gridX = pos[0];
				gridY = pos[1];
				//Log.i(TAG, "touch target draw before");

				if ( pos[0] != -1 && pos[1] != -1) {
				    if (setUIfunction.currRobotMode == RobotOperationMode.SEMI_AUTO_MODE
				            && setUIfunction.naviStartPhase == RobotOperationMode.NAVI_SETTING) {
				        int[][] tempTarget = {{pos[0], pos[1]}};
				        int trackIndex = RobotOperationMode.getIndexInTrackList(tempTarget, RobotOperationMode.targetQueue);

				        if (trackIndex == -1) {     //Add this new target in track list
				            // Only one target
				            if (RobotOperationMode.targetQueue.isEmpty())
				                RobotOperationMode.targetQueue.offer(tempTarget);
				            else
				                RobotOperationMode.targetQueue.set(0, tempTarget);
				            //Log.i(TAG, "Offer targetQueue, size= "+MapList.targetQueue.size());
				        }else {
				            RobotOperationMode.targetQueue.remove(trackIndex);
				            //Log.i(TAG, "Remove targetQueue, size= "+MapList.targetQueue.size());
				        }
				    } else if (setUIfunction.currRobotMode == RobotOperationMode.AUTO_MODE
				                    && setUIfunction.naviStartPhase1[setUIfunction.currRobotMode] == RobotOperationMode.NAVI_SETTING) {
				        int[][] tempTarget = {{pos[0], pos[1]}};
				        int trackIndex = RobotOperationMode.getIndexInTrackList(tempTarget, RobotOperationMode.autoTargetSettingQueue);
				        if (trackIndex == -1) {     //Add this new target in track list
				            // Only one target
				            if (RobotOperationMode.autoTargetSettingQueue.size() < RobotOperationMode.MAX_TARGET)
				                RobotOperationMode.autoTargetSettingQueue.offer(tempTarget);
				        } else {
				            RobotOperationMode.autoTargetSettingQueue.remove(trackIndex);
				        }
				    }
					zoomout = true;
				}

				// Update Target bitmap position
				postInvalidate();

				// Log.i(TAG,"Thread ID = " + android.os.Process.myTid());
				avoidThreadCompetition(20);
			}
		}
	}
	
	// Avoid thread competition , when user touch 2 points at the same time
	private void avoidThreadCompetition(long millis){ 
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

	public int[] getPos(MotionEvent e) {// ±N®y¼Ð´«ºâ¦¨°}¦Cªººû¼Æ
		int[] pos = new int[2];
		double x = e.getX();// ±o¨ìÂIÀ»¦ì¸mªºx®y¼Ð
		double y = e.getY();// ±o¨ìÂIÀ»¦ì¸mªºy®y¼Ð
		if (x > 4 && y > 4 && x < 326 && y < 321) {// ÂIÀ»ªº¬O´Ñ½L®ÉrefreshFlag
		// pos[0] = Math.round((float)((y-21)/36));//¨ú±o©Ò¦bªº¦æ
		// pos[1] = Math.round((float)((x-21)/35));//¨ú±o©Ò¦bªº¦C
			pos[0] = Math.round((float) ((x - 8) / 14));// ¨ú±o©Ò¦bªº¦C
			pos[1] = Math.round((float) ((y - 8) / 14));// ¨ú±o©Ò¦bªº¦æ
		} else {// ÂIÀ»ªº¦ì¸m¤£¬O´Ñ½L®É
			pos[0] = -1;// ±N¦ì¸m³]¬°¤£¥i¥Î
			pos[1] = -1;
		}
		return pos;// ±N®y¼Ð°}¦Cªð¦^
	}

	public int[] getPosW(MotionEvent e) {
		int[] pos = new int[2];
		double x = e.getX();
		double y = e.getY();

		// /////////////////////////////////////////////////////////////
		// (col*(span+1)+fixMapData) = X total length //
		// (row*(span+1)+fixMapData) = Y total length //
		// /////////////////////////////////////////////////////////////

		// int xGridSize = (col*(span+1)+fixWidthMapData) / col;
		// int yGridSize = (row*(span+1)+fixHeightMapData) / row;
		int xGridSize = (col * (span + 1)) / col;
		int yGridSize = (row * (span + 1)) / row;

		if (x > fixWidthMapData && y > fixHeightMapData
				&& x < (col * (span + 1) + fixWidthMapData)
				&& y < (row * (span + 1) + fixHeightMapData)) {

			int xPos = ((int) x - fixWidthMapData) / xGridSize;
			int yPos = ((int) y - fixHeightMapData) / yGridSize;
			// Log.i(TAG,"( xPos , yPos ) = ( " + xPos + " , " + yPos + " )");

			// Avoid map object be used on onMyDraw function
			synchronized (map) {
				try {
					if (map[yPos][xPos] == 0) {
						pos[0] = xPos;
						pos[1] = yPos;
					} else {
						pos[0] = -1;
						pos[1] = -1;
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		} else {
			pos[0] = -1;
			pos[1] = -1;
		}
		return pos;
	}

	// Use this thread for update canvas information frequently
	// We don't use this now.
	public class ShowThread implements Runnable {
		int delayTime = 50;

		public ShowThread() {
			refreshFlag = true;
		}

		public void run() {
			while (refreshFlag) {
				synchronized (inStr) {
					try {
						postInvalidate();
						// Log.i(TAG,"Thread ID = " +
						// android.os.Process.myTid());

						// Avoid thread competition , when user touch 2 points
						// at the same time
						Thread.sleep(delayTime);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (game.map != null) {
			setGridSize();
		} else {
			setVIEW_WIDTH((int) (screenWidth / 2));
			setVIEW_HEIGHT((int) (screenHeight / 2));
		}
		setMeasuredDimension(VIEW_WIDTH, VIEW_HEIGHT);
	}

	@SuppressLint("NewApi") 
	public void getScreenSize() {
		WindowManager wm = (WindowManager) mContext
				.getSystemService(mContext.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
	}

	public void setGridSize(){
		/* The setGridSize */
		getMapSize();
		
		/* Draw Map position on the upper left */
		if(isZoom){
			width = (col * (span + 1)) + xcoordinate;
			height = (row * (span + 1)) + ycoordinate;
			setVIEW_WIDTH(width);
			setVIEW_HEIGHT(height);
			
		}	
		/* Draw Map position on the upper right */
		else{ 
			fixWidthMapData = screenWidth - (col * (span + 1));
			height = (row * (span + 1)) + ycoordinate;
			
			setVIEW_WIDTH(screenWidth);
			//setVIEW_WIDTH(width);
			setVIEW_HEIGHT(height);
		}

	}
	
	public void getMapSize() {
		map = game.map;
		row = map.length;
		col = map[0].length;
		
		mapWidth = (col * (span + 1));
		mapHeight = (row * (span + 1));
	}

	public static int getVIEW_WIDTH() {
		return VIEW_WIDTH;
	}

	public static void setVIEW_WIDTH(int vIEW_WIDTH) {
		VIEW_WIDTH = vIEW_WIDTH;
	}

	public static int getVIEW_HEIGHT() {
		return VIEW_HEIGHT;
	}

	public static void setVIEW_HEIGHT(int vIEW_HEIGHT) {
		VIEW_HEIGHT = vIEW_HEIGHT;
	}

	public static ArrayList<int[][]> getPathQueue() {
		return pathQueue;
	}

	public void setPathQueue(ArrayList<int[][]> pathQueue) {
		this.pathQueue = pathQueue;
	}

	public void PathQueueClear() {
		this.pathQueue.clear();
	}
	
	public void transRemoteCoord(double CoordX, double CoordY){
		this.remoteCoordX = (int)((CoordX / remoteScreenWidth) * screenWidth);
		this.remoteCoordY = (int)((CoordY / remoteScreenHeight) * screenHeight);
		Log.i("shinhua", "transRemoteCoord: "+remoteCoordX+" "+ remoteCoordY);
	}

	public void setRemoteScreenSize(int width, int height){
		this.remoteScreenWidth = width;
		this.remoteScreenHeight = height;
		Log.i("shinhua", "RemoteScreenSize: "+ remoteScreenWidth + " " + remoteScreenHeight);
	}
	
}
