package org.doubango.imsdroid.map;

import android.util.Log;

public class Game {
	int mapId = 0;
	static int[][] map;// = MapList.customized_map2[mapId];
	public int[] source = MapList.source;
	public int[] target = MapList.target;
	
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
}
