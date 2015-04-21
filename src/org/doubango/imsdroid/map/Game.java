package org.doubango.imsdroid.map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;
import android.widget.Button;//�ޤJ�������O
import android.widget.TextView;//�ޤJ�������O
public class Game {
	public int algorithmId=0;
	int mapId = 0;
	static int[][] map;// = MapList.customized_map2[mapId];
	public int[] source = MapList.source;
	public int[] target = MapList.target;
	public GameView gameView;
	public Button runButton;
	public TextView BSTextView;
	private static ArrayList<int[][]> searchProcess=new ArrayList<int[][]>();
	Stack<int[][]> stack=new Stack<int[][]>();
	HashMap<String,int[][]> hm=new HashMap<String,int[][]>();
	LinkedList<int[][]> queue=new LinkedList<int[][]>();
	PriorityQueue<int[][]> astarQueue=new PriorityQueue<int[][]>(100,new AStarComparator(this));
	HashMap<String,ArrayList<int[][]>> hmPath=new HashMap<String,ArrayList<int[][]>>();
	int[][] length=new int[MapList.map[mapId].length][MapList.map[mapId][0].length];
	int[][] visited=new int[MapList.map[0].length][MapList.map[0][0].length];
	int[][] sequence={
		{0,1},{0,-1},
		{-1,0},{1,0}//,
//		{-1,1},{-1,-1},
//		{1,-1},{1,1}
	};
	private static boolean pathFlag=false;
	int timeSpan=10;
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	public void reloadMap(int number , GameView gv)
	{
		Log.i("william","change map to " + number);
		
		if (map == null) {
			mapId = 0;
			//map = MapList.customized_map3[mapId];
			//map = MapList.customized_map2[mapId];
			map = MapList.nkg_lobby[mapId];
			gv.postInvalidate();
		} else {
			synchronized (map) {
				try {
					mapId = 0;
					//map = MapList.customized_map3[mapId];
					//map = MapList.customized_map2[mapId];
					map = MapList.nkg_lobby[mapId];
					gv.setVIEW_WIDTH(640);
					gv.setVIEW_HEIGHT(640);
					gv.requestLayout();
					gv.postInvalidate();

				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void clearState(){
		gameView.algorithmDone = false;
		gameView.PathQueueClear();
		setPathFlag(false);	
		getSearchProcess().clear();
		stack.clear();
		queue.clear();
		astarQueue.clear();
		hm.clear();
		visited=new int[MapList.map[mapId].length][MapList.map[mapId][0].length];
		hmPath.clear();
		for(int i=0;i<length.length;i++){
			for(int j=0;j<length[0].length;j++){
				length[i][j]=9999;
			}
		}
	}

	public void runAlgorithm() {
		clearState();
		if (map != null) {
		}
	}

	public boolean isPathFlag() {
		return pathFlag;
	}

	public void setPathFlag(boolean pathFlag) {
		this.pathFlag = pathFlag;
	}

	public ArrayList<int[][]> getSearchProcess() {
		return searchProcess;
	}

	public void setSearchProcess(ArrayList<int[][]> searchProcess) {
		this.searchProcess = searchProcess;
	}
}
