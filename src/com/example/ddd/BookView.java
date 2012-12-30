package com.example.ddd;

import com.example.Types.PageInfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class BookView extends View {

	private PageInfo mCurrentPageInfo;//保存当前页
	private PageInfo mNextPageInfo;//保存下一页
	private Paint mPaint;//画笔
	private int mRectColor = Color.WHITE;//选中文字遮拦颜色
	private Rect[] mRect = null;//文字遮拦的Rect
	private int mWidth;//View宽
	private int mHeight;//View高
	private int state;//状态 0：点击换页 1：拖拽选中文字
	
	public BookView(Context context) {
		super(context);
		init();
		// TODO Auto-generated constructor stub
	}
	
	public BookView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
		// TODO Auto-generated constructor stub
	}

	public BookView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public void init(){
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//抗锯齿
		mPaint.setColor(mRectColor);
		mPaint.setAlpha(100);
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setBitmaps(PageInfo p1, PageInfo p2) {
		mCurrentPageInfo = p1;
		mNextPageInfo = p2;
		postInvalidate();
	}
	
	protected void onDraw(Canvas canvas){
		canvas.drawColor(0xFFAAAAAA);
		canvas.save();
		canvas.drawBitmap(mCurrentPageInfo.getmBitmap(), 0, 0, null);
		onDrawRect(canvas);
		canvas.restore();
		
	}
	
	/**@function 设置View大小*/
	public void setScreen(int w, int h) {
		mWidth = w;
		mHeight = h;
	}
	
	/**@function false代表去上一页，ture代表去下一页*/
	public boolean getDirection(int x){
		return x > mWidth/2;
	}
	
	/**@function 更新绘制选中文字的图层的rect*/
	public void createRect(Rect[] rect){
		mRect = rect;
		invalidate();
	}
	
	/**@function 画遮罩文字的矩形*/
	private void onDrawRect(Canvas canvas){
		if(state == 1&&mRect!=null){
			for(int i = 0;i < mRect.length; i++){
				canvas.drawRect(mRect[i], mPaint);
			}
		}
	}
}
