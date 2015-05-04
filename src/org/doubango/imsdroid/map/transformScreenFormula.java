package org.doubango.imsdroid.map;

import android.util.Log;

public class transformScreenFormula {

	private int x_axis;
	private int y_axis;
	private int x_grid;
	private int y_grid;
	private double scalex = 16.84;
	private double scaley = 16.36;
	private double halfx = scalex / 2;
	private double halfy = scaley / 2;
	
	private static transformScreenFormula mInstance;
	
	public static transformScreenFormula getInstance(){
		if(mInstance == null){
			mInstance = new transformScreenFormula();
		}
		return mInstance;
	}
	
	public transformScreenFormula(){	
	}
	
	public void transform2ScreenAxis(int index_x, int index_y){
	    Log.i("shinhua", "Grid transform2ScreenAxis: "+ index_x + " " + index_y);
		this.x_axis = (int)(index_x * scalex - halfx);
		this.y_axis = (int)(index_y * scaley - halfy);
		Log.i("shinhua", "Axis transform2ScreenAxis: "+ x_axis + " " + y_axis);
	}
	
	public int getXaxis(){
		return x_axis;
	}
	
	public int getYaxis(){
		return y_axis;
	}
	
	public void transform2ScreenGird(int x_axis, int y_axis){
		Log.i("shinhua", "Axis transform2ScreenGird: "+ x_axis + " " + y_axis);
		this.x_grid = (int)Math.ceil(x_axis / this.scalex);
		this.y_grid = (int)Math.ceil(y_axis / this.scaley);
		Log.i("shinhua", "Grid transform2ScreenGird: "+ x_grid + " " + y_grid);
	}

	public int getX_grid() {
		return x_grid;
	}

	public int getY_grid() {
		return y_grid;
	}
	
	public void setScalex(double scalex) {
		this.scalex = scalex;
	}

	public void setScaley(double scaley) {
		this.scaley = scaley;
	}
	
}
