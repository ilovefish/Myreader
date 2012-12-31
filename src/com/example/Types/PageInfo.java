package com.example.Types;

import java.text.DecimalFormat;
import java.util.Vector;

import com.example.ddd.R;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

public class PageInfo {
	
	private Bitmap mBitmap;//页面
	private Canvas mCanvas;//用于绘制页面
	private Paint mPaint;//画笔
	private int mWidth;//page宽
	private int mHeight;//page高
	private int mMarginWidth = 15; // 左右边缘的距离
	private int mMarginHeight = 20; // 上下边缘的距离
	private int mFontMarginWidth = 1;//文字间距离
	private int mFontMarginHeight = 1;//行间距
	private String mCharsetName = "GBK";//文字编码
	private Align mAlign = Align.LEFT;//文字对齐方式
	private int mFontSize = 20;//文字大小
	private int mTextColor = Color.BLACK;//文字颜色
	private float mVisibleHeight;// 绘制内容的高
	private float mVisibleWidth;// 绘制内容的宽
	private int mLineCount; // 每页可以显示的行数
	private int mPageBegin;//页面开始处字节
	private int mPageEnd;//页面结尾处字节
	private int mBookLong;//整本书的长度
	private Vector<String> mlines = new Vector<String>();//保存当前页面行的string
	private int mBackColor = 0xffff9e85; // 背景颜色
	private Bitmap mBookBg = null;//页面背景图片
		
	public PageInfo(int w,int h,Context context){
		mWidth = w;
		mHeight = h;
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
		mPaint.setTextAlign(mAlign);
		mPaint.setTextSize(mFontSize);
		mPaint.setColor(mTextColor);
		mVisibleWidth = mWidth - mMarginWidth * 2;
		mVisibleHeight = mHeight - mMarginHeight * 2;
		mLineCount = (int) (mVisibleHeight/(mFontSize+mFontMarginHeight));
	}
	
	/**@function 判断改页面宽度实际能包容多少str*/
	public int breakText(String str){
		return mPaint.breakText(str, true, mVisibleWidth, null);
	}
	
	/**@function 获得字符串宽度*/
	public float getTextWidths(String str){
		float[] f = new float[str.length()];;
		mPaint.getTextWidths(str, f);
		return f[0];
	}

	/**@function 绘制该页面*/
	public void onDraw(){
		if (mBookBg == null){
			mCanvas.drawColor(mBackColor);
		}else{
			mCanvas.drawBitmap(mBookBg, 0, 0, null);	
		}
		onDrawFont();
		onDrawProgress();
	}
	
	/**@function 绘制文字*/
	private void onDrawFont(){
		int y = mMarginHeight;
		if(mlines.size()>0){
			for(String str : mlines){
				y += mFontSize + mFontMarginHeight;
				mCanvas.drawText(str, mMarginWidth, y, mPaint);
			}
		}
	}
	
	/**@function 绘制显示进度的百分比*/
	private void onDrawProgress(){
		if(mBookLong!=0){
			mPaint.setTextSize(12);
			float fPercent = (float) (mPageBegin * 1.0 / mBookLong);//目前页
			DecimalFormat df = new DecimalFormat("#0.0");
			String strPercent = df.format(fPercent * 100) + "%";//小数点后一位的字符串
			int nPercentWidth = (int) mPaint.measureText("999.9%") + 1;//返回进度字符串的宽度
			mCanvas.drawText(strPercent, mWidth - nPercentWidth, mHeight - 5, mPaint);//在canvas中写出
			mPaint.setTextSize(mFontSize);
		}
	}
	
	/**@function 根据xy的坐标返回对应的字符的坐标*/
	public FontRectInfo getRectByXY(int x,int y){
		FontRectInfo f = new FontRectInfo();
 		int lineNum = 0;
		int lineD = mMarginHeight;
		int fontD = mMarginWidth;
		String s = "";
		if(mlines!=null){
			lineNum = mlines.size();
			for(int i = 0;i < lineNum; i++){
				lineD += mFontSize + mFontMarginHeight;
				if(y <= lineD){
					lineNum = i;
					f.setLine(lineNum);
					s = mlines.get(lineNum);
					break;
				}else{
					if(i == lineNum - 1){
						lineNum = i;
						f.setLine(lineNum);
						s = mlines.get(lineNum);
					}
				}
			}
			
			if(!s.equals("")){
				for(int i = 0;i < s.length(); i++){
					fontD += getTextWidths(s.substring(i,i+1));
					if(x <= fontD){
						f.setIndex(i);
						break;
					}else{
						if(i == s.length()-1){
							f.setIndex(i);
						}
					}	
				}
			}
			return f;
		}
		return null;
	}
	
	/**@funtion 通过指定的字符位置返回需要画制的rect[]*/
	public Rect[] returnPickFont(FontRectInfo f,FontRectInfo t){
		FontRectInfo fromRect = null;
		FontRectInfo toRect = null;
		if(t.getLine() > f.getLine()){
			fromRect = f;
			toRect = t;
		}else if(t.getLine() == f.getLine()){
			if(t.getIndex() >= f.getIndex()){
				fromRect = f;
				toRect = t;
			}else{
				fromRect = t;
				toRect = f;
			}
		}else{
			fromRect = t;
			toRect = f;
		}
		
		int num = toRect.getLine() - fromRect.getLine() + 1;
		Rect[] rect = new Rect[num];
		for(int i = 0;i < num; i++){
			rect[i] = new Rect();
			if(num == 1){
				Rect from = returnFontRect(fromRect.getLine(),fromRect.getIndex());
				Rect to = returnFontRect(toRect.getLine(), toRect.getIndex());
				rect[0].left = from.left;
				rect[0].right = to.right;
				rect[0].top = from.top;
				rect[0].bottom = from.bottom;
			}else{
				if(i == 0){
					Rect from = returnFontRect(fromRect.getLine(),fromRect.getIndex());
					String s = mlines.get(fromRect.getLine());
					Rect to = returnFontRect(fromRect.getLine(), s.length() - 1);
					rect[i].left = from.left;
					rect[i].right = to.right;
					rect[i].top = from.top;
					rect[i].bottom = from.bottom;
				}else if(i == num-1){
					Rect to = returnFontRect(toRect.getLine(),toRect.getIndex());
					Rect from = returnFontRect(fromRect.getLine(), 0);
					rect[i].left = from.left;
					rect[i].right = to.right;
					rect[i].top = to.top;
					rect[i].bottom = to.bottom;
				}else{
					Rect from = returnFontRect(fromRect.getLine() + i, 0);
					String s = mlines.get(fromRect.getLine() + i);
					Rect to = returnFontRect(fromRect.getLine() + i,s.length() - 1);
					rect[i].left = from.left;
					rect[i].right = to.right;
					rect[i].top = to.top;
					rect[i].bottom = to.bottom;
				}
			}
		}
		return rect;
	}
	
	/**@function 返回单个字体rect*/
	public Rect returnFontRect(int line,int index){
		Rect rect = new Rect();
		rect.bottom = mMarginHeight + (mFontSize + mFontMarginHeight)*(line + 1) + 3;
		rect.top = rect.bottom - (mFontSize + mFontMarginHeight - 1);
		rect.right = mMarginWidth;
		String s = mlines.get(line);
		for(int i = 0;i < index + 1; i++){
			rect.right += getTextWidths(s.substring(i,i+1));
			rect.left = (int) (rect.right - getTextWidths(s.substring(i,i+1)));
		}
		return rect;
	}
	
	/**@function 返回选中文字*/
	public String returnString(FontRectInfo f,FontRectInfo t){
		FontRectInfo fromRect = null;
		FontRectInfo toRect = null;
		if(t.getLine() > f.getLine()){
			fromRect = f;
			toRect = t;
		}else if(t.getLine() == f.getLine()){
			if(t.getIndex() >= f.getIndex()){
				fromRect = f;
				toRect = t;
			}else{
				fromRect = t;
				toRect = f;
			}
		}else{
			fromRect = t;
			toRect = f;
		}
		String s = new String();
		int num = toRect.getLine() - fromRect.getLine() + 1;
		for(int i = 0;i < num; i++){
			if(num == 1){
				String pro = mlines.get(fromRect.getLine());
				s += pro.substring(fromRect.getIndex(), toRect.getIndex()+1);
			}else{
				if(i == 0){
					String pro = mlines.get(fromRect.getLine());
					s += pro.substring(fromRect.getIndex(), pro.length());
				}else if(i == num - 1){
					String pro = mlines.get(toRect.getLine());
					s += pro.substring(0, toRect.getIndex()+1);
				}else{
					String pro = mlines.get(fromRect.getLine()+i);
					s += pro.substring(0, pro.length());
				}	
			}
		}
		return s;	
	}
	
	/**@function 清理本页line*/
	public void clear(){
		mlines.removeAllElements();
	}
	
	public int getmLineCount() {
		return mLineCount;
	}

	public String getmCharsetName() {
		return mCharsetName;
	}

	public void setmCharsetName(String mCharsetName) {
		this.mCharsetName = mCharsetName;
	}

	public int getmPageBegin() {
		return mPageBegin;
	}

	public void setmPageBegin(int mPageBegin) {
		this.mPageBegin = mPageBegin;
	}

	public int getmPageEnd() {
		return mPageEnd;
	}

	public void setmPageEnd(int mPageEnd) {
		this.mPageEnd = mPageEnd;
	}

	public Vector<String> getMlines() {
		return mlines;
	}

	public void setMlines(Vector<String> mlines) {
		this.mlines = mlines;
	}

	public int getmBookLong() {
		return mBookLong;
	}

	public void setmBookLong(int mBookLong) {
		this.mBookLong = mBookLong;
	}
	
	public Bitmap getmBitmap() {
		return mBitmap;
	}
}
