package com.example.ddd;

import java.util.HashMap;

import com.example.Types.FileInfo;

import android.app.Application;
import android.util.DisplayMetrics;
import android.util.Log;

public class Myreader extends Application {
	
	private HashMap<String,FileInfo> files = new HashMap<String,FileInfo>();//暂时储存扫描后的txt书籍
	private int widthPixels;
	private int heightPixels;
	private float density; 
	private int screenWidth;
	private int screenHeight;
	 
	public static String dirPath = "Myreader"; 
	
	public HashMap<String,FileInfo> getFiles() {
		return files;
	}

	public void setFiles(HashMap<String,FileInfo> files) {
		this.files = files;
	}
	
	public int getWidthPixels() {
		return widthPixels;
	}

	public void setWidthPixels(int widthPixels) {
		this.widthPixels = widthPixels;
	}

	public int getHeightPixels() {
		return heightPixels;
	}

	public void setHeightPixels(int heightPixels) {
		this.heightPixels = heightPixels;
	}

	public float getDensity() {
		return density;
	}

	public void setDensity(float density) {
		this.density = density;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		getScreenPixels();
	}

	public void getScreenPixels(){
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
        widthPixels= dm.widthPixels;
        heightPixels= dm.heightPixels;
        density = dm.density;
        screenWidth = (int) (widthPixels*density);
        screenHeight = (int) (heightPixels*density);
        Log.d("TAG", "screenWidth:"+screenWidth+"screenHeight:"+screenHeight);
	}
	
}
