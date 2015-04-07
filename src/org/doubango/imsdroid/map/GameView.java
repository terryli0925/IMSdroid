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

public class GameView extends View {

	private String TAG = "william";
	private static int VIEW_WIDTH = 640;
	private static int VIEW_HEIGHT = 640;

	public Game game;
	GameView GV;
	public XMPPSetting _XMPPSet;
	private NetworkStatus loggin;
	public Spinner mySpinner;// Spinner���ޥ�
	public TextView CDTextView;
	int span = 16;
	int theta = 0;
	public static boolean drawCircleFlag = false, turnToBigMap = false;

	public static void setDrawCircleFlag(boolean drawCircleFlag) {
		GameView.drawCircleFlag = drawCircleFlag;
	}

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

	public void onDrawText(Canvas canvas) {
		paint.setARGB(255, 255, 0, 0);
		paint.setStyle(Style.STROKE);
		paint.setTextSize(15);
		canvas.drawText("Tx = " + touchX + " Ty = " + touchY, 380, drawBaseLine
				+ drawIncrease, paint);
		canvas.drawText("tempX,Y : " + tempwidth + "," + tempheight, 380,
				drawBaseLine + drawIncrease * 2, paint);
		canvas.drawText("GridX,Y : " + gridX + "," + gridY, 380, drawBaseLine
				+ drawIncrease * 3, paint);
		canvas.drawText("RX : " + String.format("%.3f", rX), 380, drawBaseLine
				+ drawIncrease * 4, paint);
		canvas.drawText("RY : " + String.format("%.3f", rY), 380, drawBaseLine
				+ drawIncrease * 5, paint);

	}

	// Draw robot position
	public void DrawRobotPosition(final Canvas canvas) {

		// We get this from our self algorithm
		int[][] tempA = getPathQueue().get(drawCount);

		paint.setStyle(Style.FILL);
		paint.setColor(Color.RED);
		canvas.drawCircle(
				tempA[0][0] * (span + 1) + span / 2 + fixWidthMapData,
				tempA[0][1] * (span + 1) + span / 2 + fixHeightMapData,
				span / 2, paint);

		//Log.i(TAG, "Draw Circle X , Y ( " + tempA[0][0] + " " + tempA[0][1] + " )");
	}

	@SuppressLint("WrongCall")
	protected void onMyDraw(Canvas canvas) {
		super.onDraw(canvas);

		//canvas.drawColor(Color.GRAY); // gray background, annotate this line, the view don't show
		paint.setColor(Color.BLACK);
		paint.setStyle(Style.STROKE);

		/* Draw BaseMap */
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
//					canvas.drawRect(fixWidthMapData + j * (span + 1),
//							fixHeightMapData + i * (span + 1), fixWidthMapData
//									+ j * (span + 1) + span, fixHeightMapData
//									+ i * (span + 1) + span, paint);
				}else if (map[i][j] == 2) {// �¦�
					paint.setColor(Color.LTGRAY);
					//paint.setStyle(Style.FILL);
					paint.setStyle(Style.FILL_AND_STROKE);
					paint.setStrokeWidth(5); 
//					canvas.drawRect(fixWidthMapData + j * (span + 1),
//							fixHeightMapData + i * (span + 1), fixWidthMapData
//									+ j * (span + 1) + span, fixHeightMapData
//									+ i * (span + 1) + span, paint);
				}
			}
		}

		// Canvas drawBitmap: Track point
		if (SetUIFunction.currRobotMode == RobotOperationMode.SEMI_AUTO_MODE) {
		    for (int i = 0; i < RobotOperationMode.nextTargetQueue.size(); i++) {
		        int[][] tempTarget = RobotOperationMode.nextTargetQueue.get(i);
		        if (i == RobotOperationMode.nextTargetQueue.size() -1) {
		            canvas.drawBitmap(redBall,
		                    fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
		                    + tempTarget[0][1] * (span + 1), paint);
		        } else {
		            canvas.drawBitmap(greenBall,
		                    fixWidthMapData + tempTarget[0][0] * (span + 1), fixHeightMapData
		                    + tempTarget[0][1] * (span + 1), paint);
		        }
		    }
		}
		// Canvas drawBitmap: Source
		canvas.drawBitmap(source,
				fixWidthMapData + game.source[0] * (span + 1), fixHeightMapData
						+ game.source[1] * (span + 1), paint);
		// Canvas drawBitmap: Target
		/*canvas.drawBitmap(target,
		        fixWidthMapData + game.target[0] * (span + 1), fixHeightMapData
		        + game.target[1] * (span + 1), paint);*/

		// William Added
		//onDrawText(canvas);

		//Log.i(TAG,"drawcircleflag = " + drawCircleFlag );
		if (drawCircleFlag == true) {
			DrawRobotPosition(canvas);
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
            ycoordinate = (int) ((screenHeight / 2) - (mapHeight / 2));

            //fixWidthMapData = xcoordinate;    // ZoomIn Screen in the right
            fixWidthMapData = 0;            // ZoomIn Screen in the middle
            fixHeightMapData = ycoordinate;

            isZoom = true;
        }else {
            // Let map screen change back into small size
            isZoom = !isZoom;

            span = 15;
            xcoordinate = ycoordinate = 5;
            fixWidthMapData = fixHeightMapData = 5;
            
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
				    if (SetUIFunction.currRobotMode == RobotOperationMode.MANUAL_MODE) {
				        MapList.target[0][0] = pos[0];
				        MapList.target[0][1] = pos[1];

				        if (loggin.GetLogStatus()) {
				            if (XMPPSetting.IS_SERVER) {
                                _XMPPSet.XMPPSendText("william1", "target " + MapList.target[0][0] +" " + MapList.target[0][1]);
				            } else {
				                _XMPPSet.XMPPSendText(XMPPSetting.SERVER_NAME, "target " + MapList.target[0][0] +" " + MapList.target[0][1]);
				            }
				        }
				    } else if (SetUIFunction.currRobotMode == RobotOperationMode.SEMI_AUTO_MODE) {
				        int[][] tempTarget = {{pos[0], pos[1]}};
				        int trackIndex = RobotOperationMode.getIndexInTrackList(tempTarget);
				        String action;

				        if (trackIndex == -1) {     //Add this new target in track list
				            RobotOperationMode.nextTargetQueue.offer(tempTarget);
				            //Log.i(TAG, "Offer nextTargetQueue, size= "+MapList.nextTargetQueue.size());
				            action = "add";
				        }else {
				            RobotOperationMode.nextTargetQueue.remove(trackIndex);
				            //Log.i(TAG, "Remove nextTargetQueue, size= "+MapList.nextTargetQueue.size());
				            action = "remove";
				        }

				        if (loggin.GetLogStatus()) {
				            if (XMPPSetting.IS_SERVER) {
				                _XMPPSet.XMPPSendText("william1", "track "+ action +" "+ tempTarget[0][0] +" "+ tempTarget[0][1]);
				            } else {
				                _XMPPSet.XMPPSendText(XMPPSetting.SERVER_NAME, "track "+ action +" "+ tempTarget[0][0] +" " + tempTarget[0][1]);
				            }
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

	/*
	 * @Override protected void onLayout(boolean changed, int left, int top, int
	 * right, int bottom) { // TODO Auto-generated method stub
	 * 
	 * }
	 */

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

	public void getMapSize() {
		map = game.map;
		row = map.length;
		col = map[0].length;
		mapWidth = (col * (span + 1));
		mapHeight = (row * (span + 1));
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

	public void setXMPPSetting(XMPPSetting xmppSetting) {
	    _XMPPSet = xmppSetting;
	}
}
